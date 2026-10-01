package com.threesix.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.network.ServerInfo;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.data.CoordSnapperWebhookPayload;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.manager.NotificationHudManager;

public final class CoordSnapperModule extends KeybindModuleBase {
   public static final Duration requestTimeout = Duration.ofSeconds(8L);
   public static final int colorSuccess = -11152222;
   public static final int colorError = -1938838;
   public static final int colorWarn = -1002662;
   public static final ItemStack icon = new ItemStack(Items.RECOVERY_COMPASS);
   public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.ROOT);
   public static final long cooldownMs = 250L;
   public static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(requestTimeout).followRedirects(Redirect.NORMAL).build();
   public final ClientSetting webhookSetting = new ClientSetting("Webhook", "") {
      @Override
      public boolean isNamed(String local) {
         return super.isNamed(local) || "Webhook URL".equalsIgnoreCase(local);
      }
   };
   public final ClientSetting notificationSetting = new ClientSetting("Notification", true);
   public volatile long lastSendTime;

   public CoordSnapperModule() {
      super("CoordSnapper", ModuleCategory.MISC);
      this.registerSetting(this.webhookSetting);
      this.registerSetting(this.notificationSetting);
   }

   @Override
   public void onActivationKey() {
      if (this.isEnabled() && minecraftClient.player != null) {
         long systemValue = System.currentTimeMillis();
         if (systemValue - this.lastSendTime >= 250L) {
            this.lastSendTime = systemValue;
            String local = this.trimWebhookUrl((String)this.webhookSetting.getValue());
            if (!this.isValidWebhookUrl(local)) {
               this.pushNotification("Webhook invalid", "Set a Discord webhook URL.", -1002662);
            } else {
               CoordSnapperWebhookPayload local2 = this.buildPayload(local);
               CompletableFuture.runAsync(() -> {
                  this.sendWebhook(local2);
               }, Util.getIoWorkerExecutor().named("coordsnapper-send")).whenComplete((local3, local4) -> {
                  minecraftClient.execute(() -> {
                     if (local4 != null) {
                        this.pushNotification("Send failed", this.describeError(local4), -1938838);
                     } else {
                        this.pushNotification("Coords sent", local2.oR(), -11152222);
                     }
                  });
               });
            }
         }
      }
   }

   public CoordSnapperWebhookPayload buildPayload(String string) {
      int minecraftClientValue = minecraftClient.player.getBlockX();
      int minecraftClientValue2 = minecraftClient.player.getBlockY();
      int minecraftClientValue3 = minecraftClient.player.getBlockZ();
      String minecraftClientValue4 = minecraftClient.player.getName().getString();
      String local = this.getServerAddress();
      String timeFormatterValue = timeFormatter.format(ZonedDateTime.now());
      String local2 = this.getSkinName(minecraftClientValue4);
      String local3 = "https://mc-heads.net/body/" + local2;
      return new CoordSnapperWebhookPayload(string, minecraftClientValue4, minecraftClientValue, minecraftClientValue2, minecraftClientValue3, local, timeFormatterValue, local3);
   }

   public void sendWebhook(CoordSnapperWebhookPayload coordSnapperWebhookPayload) {
      JsonObject jsonObjectInst = new JsonObject();
      jsonObjectInst.addProperty("username", "CoordSnapper");
      JsonObject jsonObjectInst2 = new JsonObject();
      jsonObjectInst2.addProperty("title", "CoordSnapper");
      jsonObjectInst2.addProperty("color", 5624994);
      JsonArray jsonArrayInst = new JsonArray();
      jsonArrayInst.add(this.buildEmbedField("Name", coordSnapperWebhookPayload.playerName(), false));
      jsonArrayInst.add(this.buildEmbedField("Coords", coordSnapperWebhookPayload.oQ(), false));
      jsonArrayInst.add(this.buildEmbedField("IP", coordSnapperWebhookPayload.serverIp(), true));
      jsonArrayInst.add(this.buildEmbedField("Time", coordSnapperWebhookPayload.time(), true));
      jsonObjectInst2.add("fields", jsonArrayInst);
      JsonObject jsonObjectInst3 = new JsonObject();
      jsonObjectInst3.addProperty("url", coordSnapperWebhookPayload.skinRenderUrl());
      jsonObjectInst2.add("thumbnail", jsonObjectInst3);
      JsonArray jsonArrayInst2 = new JsonArray();
      jsonArrayInst2.add(jsonObjectInst2);
      jsonObjectInst.add("embeds", jsonArrayInst2);
      HttpRequest httpRequestValue = HttpRequest.newBuilder(this.toWebhookUri(coordSnapperWebhookPayload.webhook()))
         .timeout(requestTimeout)
         .header("Content-Type", "application/json")
         .header("Accept", "application/json")
         .header("User-Agent", "Threesix-CoordSnapper")
         .POST(BodyPublishers.ofString(jsonObjectInst.toString()))
         .build();

      HttpResponse httpClientValue;
      try {
         httpClientValue = httpClient.send(httpRequestValue, BodyHandlers.ofString());
      } catch (Exception error) {
         throw new IllegalStateException("Webhook request failed", error);
      }

      int intVal = httpClientValue.statusCode();
      if (intVal < 200 || intVal >= 300) {
         String local = (String)httpClientValue.body();
         if (local != null && !local.isBlank()) {
            throw new IllegalStateException("HTTP " + intVal + ": " + this.truncate(local, 120));
         } else {
            throw new IllegalStateException("HTTP " + intVal);
         }
      }
   }

   public JsonObject buildEmbedField(String string, String string2, boolean flag) {
      JsonObject jsonObjectInst = new JsonObject();
      jsonObjectInst.addProperty("name", string);
      jsonObjectInst.addProperty("flag", string2 != null && !string2.isBlank() ? string2 : "-");
      jsonObjectInst.addProperty("inline", flag);
      return jsonObjectInst;
   }

   public String getServerAddress() {
      ServerInfo minecraftClientValue = minecraftClient.getCurrentServerEntry();
      if (minecraftClientValue != null && minecraftClientValue.address != null && !minecraftClientValue.address.isBlank()) {
         String local = this.stripServerSuffix(minecraftClientValue.address);
         return local.isEmpty() ? "Singleplayer" : local;
      } else {
         return "Singleplayer";
      }
   }

   public String stripServerSuffix(String string) {
      String local = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
      int intVal = local.indexOf(47);
      if (intVal >= 0) {
         local = local.substring(0, intVal);
      }

      int intVal2 = local.indexOf(58);
      if (intVal2 >= 0) {
         local = local.substring(0, intVal2);
      }

      return local;
   }

   public String trimWebhookUrl(String string) {
      return string == null ? "" : string.trim();
   }

   public String getSkinName(String string) {
      return string != null && !string.isBlank() ? string.trim() : "Steve";
   }

   public URI toWebhookUri(String string) {
      URI uRIValue = URI.create(string);
      String local = uRIValue.getQuery();
      if (local != null && !local.isBlank()) {
         return local.contains("wait=") ? uRIValue : URI.create(string + "&wait=true");
      } else {
         return URI.create(string + "?wait=true");
      }
   }

   public boolean isValidWebhookUrl(String string) {
      if (string.isEmpty()) {
         return false;
      }

      try {
         URI uRIValue = URI.create(string);
         String local = uRIValue.getScheme();
         String local2 = uRIValue.getHost();
         String local3 = uRIValue.getPath();
         return ("https".equalsIgnoreCase(local) || "http".equalsIgnoreCase(local))
            && local2 != null
            && !local2.isBlank()
            && local3 != null
            && local3.contains("XXXXXXXXXXXXXX");
      } catch (Exception error) {
         return false;
      }
   }

   public void pushNotification(String string, String string2, int intVal) {
      if (minecraftClient != null && (Boolean)this.notificationSetting.getValue()) {
         minecraftClient.execute(() -> {
            NotificationHudManager.INSTANCE6.push(string, string2, icon, intVal);
         });
      }
   }

   public String describeError(Throwable throwable) {
      Throwable var1Snapshot = throwable;

      while (var1Snapshot.getCause() != null) {
         var1Snapshot = var1Snapshot.getCause();
      }

      String local = var1Snapshot.getMessage();
      return local != null && !local.isBlank() ? this.truncate(local, 120) : var1Snapshot.getClass().getSimpleName();
   }

   public String truncate(String string, int intVal) {
      if (string == null) {
         return "";
      }

      String local = string.replace('\n', ' ').replace('\r', ' ').trim();
      return local.length() <= intVal ? local : local.substring(0, Math.max(0, intVal - 3)) + "...";
   }

}
