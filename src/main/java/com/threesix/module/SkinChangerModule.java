package com.threesix.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo.TextureAsset;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.PlayerSkinInfo;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.AssetTextureBinding;
import com.threesix.data.SkinTextureInfo;

public final class SkinChangerModule extends ModuleBase {
   public static final Duration httpTimeout = Duration.ofSeconds(10L);
   public static final long skinRetryDelayMillis = 600L;
   public static final String mojangProfileUrl = "https://api.mojang.com/users/profiles/minecraft/";
   public static final String nameLookupUrl = "https://api.minecraftservices.com/minecraft/profile/lookup/name/";
   public static final String sessionProfileUrl = "https://sessionserver.mojang.com/session/minecraft/profile/";
   public static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(httpTimeout).followRedirects(Redirect.NORMAL).build();
   public static volatile SkinTextures spoofedSkin;
   public static volatile TextureAsset spoofedTexture;
   public final ClientSetting playerNameSetting = new ClientSetting("Player Name", "");
   public final AtomicInteger requestGeneration = new AtomicInteger();
   public PlayerSkinTextureDownloader skinAssetManager;
   public String pendingPlayerName = "";
   public String appliedPlayerName = "";
   public long lastNameChangeTime;

   public SkinChangerModule() {
      super("SkinChanger", ModuleCategory.MISC);
      this.registerSetting(this.playerNameSetting);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.pendingPlayerName = trimOrEmpty((String)this.playerNameSetting.getValue());
      this.appliedPlayerName = "";
      this.lastNameChangeTime = System.currentTimeMillis();
      if (this.pendingPlayerName.isEmpty()) {
         clearSkin();
      } else {
         this.requestSkinForPlayer(this.pendingPlayerName);
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.requestGeneration.incrementAndGet();
      this.appliedPlayerName = "";
      clearSkin();
   }

   @Override
   public void onTick() {
      String local = trimOrEmpty((String)this.playerNameSetting.getValue());
      if (!Objects.equals(local, this.pendingPlayerName)) {
         this.pendingPlayerName = local;
         this.lastNameChangeTime = System.currentTimeMillis();
      } else if (!local.isEmpty()) {
         if (!Objects.equals(local, this.appliedPlayerName) && System.currentTimeMillis() - this.lastNameChangeTime >= 600L) {
            this.requestSkinForPlayer(local);
         }
      } else if (spoofedSkin != null || spoofedTexture != null) {
         this.appliedPlayerName = "";
         clearSkin();
      }
   }

   public static SkinTextures getOverrideSkin(UUID uUID) {
      if (spoofedSkin != null && uUID != null) {
         UUID local = getLocalPlayerUuid();
         return local != null && local.equals(uUID) ? spoofedSkin : null;
      } else {
         return null;
      }
   }

   public void requestSkinForPlayer(String string) {
      this.appliedPlayerName = string;
      int intVal = this.requestGeneration.incrementAndGet();
      CompletableFuture.<PlayerSkinInfo>supplyAsync(() -> {
         return this.lookupPlayerSkin(string);
      }, Util.getIoWorkerExecutor().named("skinchanger-lookup")).thenCompose(item -> {
         return this.getSkinAssetManager().downloadAndRegisterTexture(this.buildSkinAssetId(item), this.buildCachedSkinPath(item.uuid()), item.textureUrl(), true).thenApply(var1xx -> {
            return new AssetTextureBinding(item, var1xx);
         });
      }).whenComplete((local, local2) -> {
         minecraftClient.execute(() -> {
            if (intVal == this.requestGeneration.get() && this.isEnabled()) {
               if (local2 != null) {
                  this.sendStatusMessage("Failed to apply skin for " + string + ": " + describeThrowable(local2));
               } else {
                  applySkinAsset(local.textureAsset(), local.lookup().skinType());
                  this.sendStatusMessage("Applied skin from " + local.lookup().playerName() + ".");
               }
            } else if (local != null) {
               releaseSkinAsset(local.textureAsset());
            }
         });
      });
   }

   public PlayerSkinTextureDownloader getSkinAssetManager() {
      if (this.skinAssetManager == null) {
         this.skinAssetManager = new PlayerSkinTextureDownloader(minecraftClient.getNetworkProxy(), minecraftClient.getTextureManager(), minecraftClient::execute);
      }

      return this.skinAssetManager;
   }

   public PlayerSkinInfo lookupPlayerSkin(String string) {
      try {
         UUID local = this.fetchPlayerUuid(string);
         SkinTextureInfo local2 = this.fetchSkinTextureInfo(local);
         return new PlayerSkinInfo(string, local, local2.textureUrl(), local2.skinType());
      } catch (InterruptedException interruptedException) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("Request interrupted", interruptedException);
      } catch (IOException iOException) {
         throw new IllegalStateException(iOException.getMessage(), iOException);
      }
   }

   public UUID fetchPlayerUuid(String string) throws IOException, InterruptedException {
      JsonObject local = this.getJsonObject("https://api.mojang.com/users/profiles/minecraft/" + this.urlEncodeName(string));
      if (local == null) {
         local = this.getJsonObject("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + this.urlEncodeName(string));
      }

      if (local != null && local.has("id")) {
         return parseUuid(local.get("id").getAsString());
      } else {
         throw new IOException("Player not found");
      }
   }

   public SkinTextureInfo fetchSkinTextureInfo(UUID uUID) throws IOException, InterruptedException {
      JsonObject local = this.getJsonObject("https://sessionserver.mojang.com/session/minecraft/profile/" + uUID.toString().replace("-", ""));
      if (local != null && local.has("properties")) {
         for (JsonElement jsonElement : local.getAsJsonArray("properties")) {
            if (jsonElement.isJsonObject()) {
               JsonObject local2 = jsonElement.getAsJsonObject();
               if ("textures".equalsIgnoreCase(getJsonString(local2, "name")) && local2.has("value")) {
                  String stringInst = new String(Base64.getDecoder().decode(local2.get("value").getAsString()), StandardCharsets.UTF_8);
                  JsonObject jsonParserValue = JsonParser.parseString(stringInst).getAsJsonObject();
                  JsonObject local3 = jsonParserValue.getAsJsonObject("textures");
                  JsonObject local4 = local3 != null ? local3.getAsJsonObject("SKIN") : null;
                  if (local4 != null && local4.has("url")) {
                     String nullSnapshot = null;
                     JsonObject local5 = local4.getAsJsonObject("metadata");
                     if (local5 != null && local5.has("model")) {
                        nullSnapshot = local5.get("model").getAsString();
                     }

                     PlayerSkinType local6 = "slim".equalsIgnoreCase(nullSnapshot) ? PlayerSkinType.SLIM : PlayerSkinType.WIDE;
                     return new SkinTextureInfo(local4.get("url").getAsString(), local6);
                  }
                  break;
               }
            }
         }

         throw new IOException("No usable skin texture found");
      } else {
         throw new IOException("Skin profile not found");
      }
   }

   public JsonObject getJsonObject(String string) throws IOException, InterruptedException {
      HttpRequest httpRequestValue = HttpRequest.newBuilder(URI.create(string))
         .timeout(httpTimeout)
         .header("Accept", "application/json")
         .header("User-Agent", "Threesix-SkinChanger")
         .GET()
         .build();
      HttpResponse httpClientValue = httpClient.send(httpRequestValue, BodyHandlers.ofString());
      int intVal = httpClientValue.statusCode();
      if (intVal == 404 || intVal == 204) {
         return null;
      } else if (intVal >= 200 && intVal < 300) {
         String local = (String)httpClientValue.body();
         return local != null && !local.isBlank() ? JsonParser.parseString(local).getAsJsonObject() : null;
      } else {
         throw new IOException("HTTP " + intVal);
      }
   }

   public static synchronized void applySkinAsset(TextureAsset arg, PlayerSkinType arg2) {
      releaseSkinAsset(spoofedTexture);
      spoofedTexture = arg;
      spoofedSkin = SkinTextures.create(arg, null, null, arg2);
   }

   public static synchronized void clearSkin() {
      releaseSkinAsset(spoofedTexture);
      spoofedTexture = null;
      spoofedSkin = null;
   }

   public static void releaseSkinAsset(TextureAsset arg) {
      if (arg != null && minecraftClient != null) {
         try {
            minecraftClient.getTextureManager().destroyTexture(arg.texturePath());
         } catch (Throwable error) {
         }

         try {
            if (!arg.id().equals(arg.texturePath())) {
               minecraftClient.getTextureManager().destroyTexture(arg.id());
            }
         } catch (Throwable error2) {
         }
      }
   }

   public void sendStatusMessage(String string) {
      if (minecraftClient != null && minecraftClient.inGameHud != null) {
         try {
            minecraftClient.inGameHud.getChatHud().addMessage(Text.literal("[SkinChanger] " + string));
         } catch (Throwable error) {
         }
      }
   }

   public Identifier buildSkinAssetId(PlayerSkinInfo playerSkinInfo) {
      String local = playerSkinInfo.playerName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
      if (local.isEmpty()) {
         local = "player";
      }

      return Identifier.of("threesix", "skins/" + local + "_" + playerSkinInfo.uuid().toString().replace("-", ""));
   }

   public Path buildCachedSkinPath(UUID uUID) {
      return minecraftClient.runDirectory.toPath().resolve("threesix-cache").resolve("skins").resolve(uUID.toString().replace("-", "") + ".png");
   }

   public String urlEncodeName(String string) {
      return URLEncoder.encode(string, StandardCharsets.UTF_8);
   }

   public static UUID parseUuid(String string) {
      String local = string.replace("-", "");
      return UUID.fromString(local.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
   }

   public static String trimOrEmpty(String string) {
      return string == null ? "" : string.trim();
   }

   public static String getJsonString(JsonObject jsonObject, String string) {
      return jsonObject.has(string) ? jsonObject.get(string).getAsString() : "";
   }

   public static String describeThrowable(Throwable throwable) {
      Throwable var0Snapshot = throwable;

      while (var0Snapshot.getCause() != null) {
         var0Snapshot = var0Snapshot.getCause();
      }

      String local = var0Snapshot.getMessage();
      return local != null && !local.isBlank() ? local : var0Snapshot.getClass().getSimpleName();
   }

   public static UUID getLocalPlayerUuid() {
      if (minecraftClient == null) {
         return null;
      }

      if (minecraftClient.player != null) {
         return minecraftClient.player.getUuid();
      }

      UUID minecraftClientValue = minecraftClient.getSession() != null ? minecraftClient.getSession().getUuidOrNull() : null;
      return minecraftClientValue;
   }

}
