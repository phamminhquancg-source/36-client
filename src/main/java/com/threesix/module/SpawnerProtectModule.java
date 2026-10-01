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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.data.WebhookAlertPayload;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.module.FriendsModule;
import com.threesix.data.ThreatSnapshot;

public final class SpawnerProtectModule extends ModuleBase {
   public static final String silkTouchMessage = "Need a Silk Touch pickaxe in hotbar";
   public static final int maxScanRadius = 32;
   public static final double interactDistance = 5.0;
   public static final Duration webhookTimeout = Duration.ofSeconds(8L);
   public static final int colorSuccess = 5624994;
   public static final int colorThreat = 14838378;
   public static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
   public static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(webhookTimeout).followRedirects(Redirect.NORMAL).build();
   public final ClientSetting criticalDistanceSetting = new ClientSetting("Critical Distance", 5, 1, 20);
   public final ClientSetting webhookSetting = new ClientSetting("Webhook", "") {
      @Override
      public boolean isNamed(String local) {

         return super.isNamed(local) || "Webhook URL".equalsIgnoreCase(local);
      }
   };
   public BlockPos targetPos;
   public boolean isWebhookPending;
   public int minedCount;

   public SpawnerProtectModule() {
      super("SpawnerProtect", ModuleCategory.DONUT);
      this.registerSetting(this.criticalDistanceSetting);
      this.registerSetting(this.webhookSetting);
   }

   @Override
   public void onEnable() {
      this.resetState();
      if (!this.hasSilkTouchPickaxe()) {
         this.disconnectWithReason("Need a Silk Touch pickaxe in hotbar");
      }
   }

   @Override
   public void onDisable() {
      this.cancelMining();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.interactionManager != null && !this.isWebhookPending) {
         if (!this.hasSilkTouchPickaxe()) {
            this.disconnectWithReason("Need a Silk Touch pickaxe in hotbar");
         } else {
            ThreatSnapshot local = this.scanForThreat();
            if (local.hasCriticalThreat()) {
               this.handleThreatAlert(this.buildThreatPayload(local.threat(), local.distance()));
            } else if (!local.hasAnyEnemy()) {
               this.cancelMining();
            } else {
               int intVal = this.findSilkTouchSlot();
               if (intVal == -1) {
                  this.disconnectWithReason("Need a Silk Touch pickaxe in hotbar");
               } else {
                  this.selectHotbarSlot(intVal);
                  this.enableSneak();
                  if (this.targetPos == null || !this.isSpawnerBlock(this.targetPos) || !minecraftClient.player.canInteractWithBlockAt(this.targetPos, 5.0)) {
                     this.targetPos = this.findNearestSpawner();
                     if (this.targetPos == null) {
                        this.handleThreatAlert(this.buildCompletedPayload());
                        return;
                     }
                  }

                  Direction local2 = this.getFacingForPos(this.targetPos);
                  this.rotateToTarget(this.targetPos, local2);
                  minecraftClient.interactionManager.updateBlockBreakingProgress(this.targetPos, local2);
                  minecraftClient.world.spawnBlockBreakingParticle(this.targetPos, local2);
                  minecraftClient.player.swingHand(Hand.MAIN_HAND);
                  if (!this.isSpawnerBlock(this.targetPos)) {
                     this.minedCount++;
                     this.targetPos = null;
                  }
               }
            }
         }
      }
   }

   public ThreatSnapshot scanForThreat() {
      double doubleVal = toSquaredDistance((Integer)this.criticalDistanceSetting.getValue());
      byte byteVal = 0;
      boolean falseSnapshot = false;
      PlayerEntity nullSnapshot = null;
      double var12Snapshot = -1.0;

      for (PlayerEntity class1657 : minecraftClient.world.getPlayers()) {
         if (class1657 != minecraftClient.player
            && !class1657.isSpectator()
            && !class1657.isTeammate(minecraftClient.player)
            && (!FriendsModule.isSpawnerProtect() || !FriendsModule.isFriend(class1657.getName().getString()))) {
            double minecraftClientValue = minecraftClient.player.squaredDistanceTo(class1657);
            if (minecraftClientValue <= doubleVal) {
               falseSnapshot = true;
               double sqrtValue = Math.sqrt(minecraftClientValue);
               if (nullSnapshot == null || sqrtValue < var12Snapshot) {
                  nullSnapshot = class1657;
                  var12Snapshot = sqrtValue;
               }
               break;
            }
         }
      }

      return new ThreatSnapshot(byteVal != 0, falseSnapshot, nullSnapshot, var12Snapshot);
   }

   public BlockPos findNearestSpawner() {
      ArrayList<BlockPos> arrayListInst = new ArrayList<>();
      BlockPos minecraftClientValue = minecraftClient.player.getBlockPos();
      short shortVal = 1024;

      for (int index = -32; index <= 32; index++) {
         for (int index2 = -32; index2 <= 32; index2++) {
            for (int index3 = -32; index3 <= 32; index3++) {
               int var4Var4Var5Var5Var6Var6Va = index * index + index2 * index2 + index3 * index3;
               if (var4Var4Var5Var5Var6Var6Va <= shortVal) {
                  BlockPos var2Value = minecraftClientValue.add(index, index2, index3);
                  if (this.isSpawnerBlock(var2Value) && minecraftClient.player.canInteractWithBlockAt(var2Value, 5.0)) {
                     arrayListInst.add(var2Value.toImmutable());
                  }
               }
            }
         }
      }

      return arrayListInst.stream().min(Comparator.comparingDouble(this::getEyeDistance)).orElse(null);
   }

   public boolean hasSilkTouchPickaxe() {
      return this.findSilkTouchSlot() != -1;
   }

   public int findSilkTouchSlot() {

      for (int index = 0; index < 9; index++) {
         if (this.isSilkTouchPickaxe(minecraftClient.player.getInventory().getStack(index))) {
            return index;
         }
      }

      return -1;
   }

   public boolean isSilkTouchPickaxe(ItemStack arg) {
      if (arg != null && !arg.isEmpty()) {
         String local = Registries.ITEM.getId(arg.getItem()).getPath();
         if (!local.endsWith("_pickaxe")) {
            return false;
         }

         RegistryEntry minecraftClientWorld = minecraftClient.world
            .getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT)
            .getEntry((Enchantment)minecraftClient.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).get(Enchantments.SILK_TOUCH));
         return minecraftClientWorld != null && EnchantmentHelper.getLevel(minecraftClientWorld, arg) > 0;
      } else {
         return false;
      }
   }

   public boolean isSpawnerBlock(BlockPos arg) {
      return minecraftClient.world != null && minecraftClient.world.getBlockState(arg).isOf(Blocks.SPAWNER);
   }

   public Direction getFacingForPos(BlockPos arg) {
      Vec3d minecraftClientValue = minecraftClient.player.getEyePos().subtract(Vec3d.ofCenter(arg));
      return Direction.getFacing(minecraftClientValue.x, minecraftClientValue.y, minecraftClientValue.z);
   }

   public void rotateToTarget(BlockPos arg, Direction arg2) {
      Vec3d minecraftClientValue = minecraftClient.player.getEyePos();
      Vec3d class243Value = Vec3d.ofCenter(arg);
      double class243ValueValue = class243Value.x - minecraftClientValue.x;
      double class243ValueValue2 = class243Value.y - minecraftClientValue.y;
      double class243ValueValue3 = class243Value.z - minecraftClientValue.z;
      double sqrtValue = Math.sqrt(class243ValueValue * class243ValueValue + class243ValueValue3 * class243ValueValue3);
      float floatVal = (float)(Math.toDegrees(Math.atan2(class243ValueValue3, class243ValueValue)) - 90.0);
      float floatVal2 = (float)(-Math.toDegrees(Math.atan2(class243ValueValue2, sqrtValue)));
      minecraftClient.player.setYaw(floatVal);
      minecraftClient.player.setPitch(floatVal2);
      minecraftClient.crosshairTarget = new BlockHitResult(class243Value, arg2, arg, false);
   }

   public void selectHotbarSlot(int intVal) {
      if (intVal >= 0 && intVal < 9 && minecraftClient.player.getInventory().getSelectedSlot() != intVal) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
      }
   }

   public void enableSneak() {
      minecraftClient.options.sneakKey.setPressed(true);
      minecraftClient.player.setSneaking(true);
   }

   public void cancelMining() {
      this.targetPos = null;
      if (minecraftClient.interactionManager != null && minecraftClient.interactionManager.isBreakingBlock()) {
         minecraftClient.interactionManager.cancelBlockBreaking();
      }

      if (minecraftClient.player != null) {
         minecraftClient.options.sneakKey.setPressed(false);
         minecraftClient.player.setSneaking(false);
      }
   }

   public void handleThreatAlert(WebhookAlertPayload webhookAlertPayload) {
      this.cancelMining();
      String local = this.trimWebhookUrl((String)this.webhookSetting.getValue());
      if (!this.isValidWebhookUrl(local)) {
         this.isWebhookPending = false;
         this.disconnectWithReason(webhookAlertPayload.disconnectReason());
      } else {
         this.isWebhookPending = true;
         CompletableFuture.runAsync(() -> {
            this.sendWebhook(webhookAlertPayload);
         }, Util.getIoWorkerExecutor().named("coordsnapper-send")).whenComplete((local2, local3) -> {
            minecraftClient.execute(() -> {
               this.isWebhookPending = false;
               this.disconnectWithReason(webhookAlertPayload.disconnectReason());
            });
         });
      }
   }

   public WebhookAlertPayload buildCompletedPayload() {
      String local = this.trimWebhookUrl((String)this.webhookSetting.getValue());
      return new WebhookAlertPayload(
         local,
         "[SpawnerProtect]",
         "All your spawners have been collected.",
         5624994,
         minecraftClient.player.getName().getString(),
         "",
         "",
         this.countSpawnersInInventory(),
         true,
         this.getServerLabel(),
         timeFormat.format(LocalTime.now()),
         "https://mc-heads.net/body/" + this.resolveSkinName(minecraftClient.player.getName().getString()),
         "SpawnerProtect finished"
      );
   }

   public WebhookAlertPayload buildThreatPayload(PlayerEntity arg, double doubleVal) {
      String local = this.trimWebhookUrl((String)this.webhookSetting.getValue());
      String local2 = arg != null ? arg.getName().getString() : "Unknown";
      return new WebhookAlertPayload(
         local,
         "[SpawnerProtect]",
         local2 + " came too close.",
         14838378,
         minecraftClient.player.getName().getString(),
         local2,
         String.format(Locale.ROOT, "%.1f", doubleVal),
         this.countSpawnersInInventory(),
         false,
         this.getServerLabel(),
         timeFormat.format(LocalTime.now()),
         "https://mc-heads.net/body/" + this.resolveSkinName(local2),
         "Enemy within critical distance"
      );
   }

   public void sendWebhook(WebhookAlertPayload webhookAlertPayload) {
      JsonObject jsonObjectInst = new JsonObject();
      jsonObjectInst.addProperty("username", "SpawnerProtect");
      JsonObject jsonObjectInst2 = new JsonObject();
      jsonObjectInst2.addProperty("title", webhookAlertPayload.title());
      jsonObjectInst2.addProperty("description", webhookAlertPayload.description());
      jsonObjectInst2.addProperty("color", webhookAlertPayload.value());
      JsonArray jsonArrayInst = new JsonArray();
      jsonArrayInst.add(this.buildEmbedField("Player", webhookAlertPayload.playerName(), false));
      jsonArrayInst.add(this.buildEmbedField("Time", webhookAlertPayload.time(), true));
      jsonArrayInst.add(this.buildEmbedField("Server", webhookAlertPayload.serverIp(), true));
      jsonArrayInst.add(this.buildEmbedField("All spawners mined", webhookAlertPayload.allMined() ? "✅ Yes" : "❌ No", false));
      jsonArrayInst.add(this.buildEmbedField("Spawners in bag", webhookAlertPayload.spawnersInInventory() + " spawners", false));
      if (!webhookAlertPayload.threatName().isBlank()) {
         jsonArrayInst.add(this.buildEmbedField("Threat", webhookAlertPayload.threatName() + " (" + webhookAlertPayload.distance() + " blocks)", false));
      }

      jsonObjectInst2.add("fields", jsonArrayInst);
      JsonObject jsonObjectInst3 = new JsonObject();
      jsonObjectInst3.addProperty("url", webhookAlertPayload.skinRenderUrl());
      jsonObjectInst2.add("thumbnail", jsonObjectInst3);
      JsonArray jsonArrayInst2 = new JsonArray();
      jsonArrayInst2.add(jsonObjectInst2);
      jsonObjectInst.add("embeds", jsonArrayInst2);
      HttpRequest httpRequestValue = HttpRequest.newBuilder(this.buildWebhookUri(webhookAlertPayload.webhook()))
         .timeout(webhookTimeout)
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
            throw new IllegalStateException("HTTP " + intVal + ": " + this.truncateText(local, 120));
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

   public String getServerLabel() {
      ServerInfo minecraftClientValue = minecraftClient.getCurrentServerEntry();
      if (minecraftClientValue != null && minecraftClientValue.address != null && !minecraftClientValue.address.isBlank()) {
         String local = this.stripServerAddress(minecraftClientValue.address);
         return local.isEmpty() ? "Singleplayer" : local;
      } else {
         return "Singleplayer";
      }
   }

   public String stripServerAddress(String string) {
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

   public String resolveSkinName(String string) {
      return string != null && !string.isBlank() ? string.trim() : "Steve";
   }

   public URI buildWebhookUri(String string) {
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

   public String truncateText(String string, int intVal) {
      if (string == null) {
         return "";
      }

      String local = string.replace('\n', ' ').replace('\r', ' ').trim();
      return local.length() <= intVal ? local : local.substring(0, Math.max(0, intVal - 3)) + "...";
   }

   public int countSpawnersInInventory() {
      int local = 0;
      for (int index = 0; index < minecraftClient.player.getInventory().size(); index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty() && minecraftClientValue.isOf(Blocks.SPAWNER.asItem())) {
            local += minecraftClientValue.getCount();
         }
      }

      return local;
   }

   public void disconnectWithReason(String string) {
      if (minecraftClient.getNetworkHandler() != null && minecraftClient.getNetworkHandler().getConnection() != null) {
         minecraftClient.getNetworkHandler().getConnection().disconnect(Text.literal(string));
      }

      this.setEnabled(false);
   }

   public double getEyeDistance(BlockPos arg) {
      return minecraftClient.player.squaredDistanceTo(Vec3d.ofCenter(arg));
   }

   public static double toSquaredDistance(int intVal) {
      return (double)intVal * intVal;
   }

   public void resetState() {
      this.targetPos = null;
      this.isWebhookPending = false;
      this.minedCount = 0;
   }

}
