package com.threesix.module;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.PlayerInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.ItemSelectSetting;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.internal.ModuleBase;

public final class AutoSellModule extends ModuleBase {
   public static final ItemSelectSetting sellItemsSetting = new ItemSelectSetting("Sell Items", Items.CHEST);
   public static final ClientSetting sellDelaySetting = new ClientSetting("Sell Delay", 5, 1, 40);
   public static final ClientSetting statsEnabledSetting = new ClientSetting("Stats Enabled", true);
   public static final ClientSetting valuePerSaleSetting = new ClientSetting("Value Per Sale", "114k");
   public static final ClientSetting chatParsingSetting = new ClientSetting("Chat Parsing", true);
   public static final ClientSetting webhookEnabledSetting = new ClientSetting("Webhook Enabled", false);
   public static final ClientSetting webhookNameSetting = new ClientSetting("Webhook Name", "AutoSell Report");
   public static final ClientSetting webhookAvatarSetting = new ClientSetting("Webhook Avatar", "");
   public static final ClientSetting webhookUrlSetting = new ClientSetting("Webhook Url", "");
   public static final ClientSetting webhookIntervalSetting = new ClientSetting("Webhook Interval Min", 10, 1, 180);
   public static final ClientSetting antiAdminSetting = new ClientSetting("Anti Admin", false);
   public static final ClientSetting antiAdminRangeSetting = new ClientSetting("Anti Admin Range", 8, 2, 20);
   public static final ClientSetting antiAdminOutDelaySetting = new ClientSetting("Anti Admin Out Delay", 10, 5, 30);
   private int delayTicks;
   private boolean wasSneaking;
   private long sessionStartTime;
   private long lastWebhookPostTime;
   private long lastStatusPostTime;
   private int saleCount;
   private double totalEarned;
   private String webhookMessageId;
   private long adminDetectedTime = 0L;
   private String adminName = null;
   private boolean isAdminPending = false;
   private static AutoSellModule instance;

   public AutoSellModule() {
      super("AutoSell", ModuleCategory.DONUT);
      this.registerSetting(sellItemsSetting);
      this.registerSetting(sellDelaySetting);
      this.registerSetting(statsEnabledSetting);
      this.registerSetting(valuePerSaleSetting);
      this.registerSetting(chatParsingSetting);
      this.registerSetting(webhookEnabledSetting);
      this.registerSetting(webhookNameSetting);
      this.registerSetting(webhookAvatarSetting);
      this.registerSetting(webhookUrlSetting);
      this.registerSetting(webhookIntervalSetting);
      this.registerSetting(antiAdminSetting);
      this.registerSetting(antiAdminRangeSetting);
      this.registerSetting(antiAdminOutDelaySetting);
      this.delayTicks = 0;
      this.wasSneaking = false;
      instance = this;
   }

   public static AutoSellModule getInstance() {
      return instance;
   }

   public static void handleChatMessage(String string) {
      try {
         if (string != null && instance != null && instance.isEnabled() && (Boolean)chatParsingSetting.getValue()) {
            double instanceValue = instance.parseEarnedAmount(string);
            if (instanceValue > 0.0) {
               instance.totalEarned += instanceValue;
               instance.postStatusWebhook();
            }
         }
      } catch (Exception error) {
      }
   }

   private double parseEarnedAmount(String string) {
      if (string == null) {
         return 0.0;
      }

      String local = string.toLowerCase(Locale.ROOT);
      if (!local.contains("sell")
         && !local.contains("sold")
         && !local.contains("earn")
         && !local.contains("receive")
         && !local.contains("reward")
         && !local.contains("bán")
         && !local.contains("nhận")
         && !local.contains("kiếm")
         && !local.contains("thưởng")
         && !string.contains("$")
         && !string.contains("+$")
         && !string.contains("+ $")) {
         return 0.0;
      }

      Matcher patternValue = Pattern.compile("(?i)\\$?([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]+)?|[0-9]+(?:\\.[0-9]+)?)\\s*([kmb]?)").matcher(string);
      if (patternValue.find()) {
         String local2 = patternValue.group(1).replace(",", "");
         String local3 = patternValue.group(2).trim().toLowerCase(Locale.ROOT);

         try {
            double doubleValue = Double.parseDouble(local2);
            if (local3.startsWith("k")) {
               doubleValue *= 1000.0;
            } else if (local3.startsWith("m")) {
               doubleValue *= 1000000.0;
            } else if (local3.startsWith("b")) {
               doubleValue *= 1.0E9;
            }

            return doubleValue;
         } catch (NumberFormatException numberFormatException) {
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   @Override
   public void onEnable() {
      this.delayTicks = 20;
      this.wasSneaking = false;
      this.resetStats();
      this.sessionStartTime = System.currentTimeMillis();
      this.lastWebhookPostTime = this.sessionStartTime;
      if ((Boolean)webhookEnabledSetting.getValue() && this.hasWebhookUrl()) {
         this.sendStatusWebhook("Running");
      }
   }

   @Override
   public void onDisable() {
      if ((Boolean)webhookEnabledSetting.getValue() && this.hasWebhookUrl()) {
         this.sendStoppedWebhook("Stopped");
      }

      this.resetStats();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         if (!(Boolean)antiAdminSetting.getValue() || !this.isAdminNearby()) {
            if (this.delayTicks > 0) {
               this.delayTicks--;
            } else {
               this.handleSellingTick();
            }

            long systemValue = System.currentTimeMillis();
            if ((Boolean)webhookEnabledSetting.getValue()
               && this.webhookMessageId != null
               && systemValue - this.lastWebhookPostTime >= ((Integer)webhookIntervalSetting.getValue()).intValue() * 60000L
               && this.hasWebhookUrl()) {
               this.sendStatusWebhook("Running");
               this.lastWebhookPostTime = systemValue;
            }
         }
      }
   }

   private void handleSellingTick() {
      if (minecraftClient.player != null && minecraftClient.interactionManager != null) {
         ScreenHandler minecraftClientValue = minecraftClient.player.currentScreenHandler;
         boolean flag = minecraftClientValue instanceof GenericContainerScreenHandler;
         if (!flag) {
            if (this.wasSneaking) {
               this.wasSneaking = false;
               minecraftClient.options.sneakKey.setPressed(false);
               minecraftClient.player.setSneaking(false);
            }

            if (this.hasSellableInInventory()) {
               minecraftClient.getNetworkHandler().sendChatCommand("sell");
               this.delayTicks = 20 + ThreadLocalRandom.current().nextInt(0, 3);
            } else {
               this.delayTicks = (Integer)sellDelaySetting.getValue();
            }
         } else {
            this.wasSneaking = true;
            minecraftClient.player.setSneaking(true);
            minecraftClient.options.sneakKey.setPressed(true);
            boolean flag2 = this.quickMoveSellableItems(minecraftClientValue);
            if (flag2 && !this.isChestOutputFull(minecraftClientValue) && this.hasSellableInChest(minecraftClientValue)) {
               this.delayTicks = 1;
            } else {
               minecraftClient.player.closeHandledScreen();
               this.recordSale();
               minecraftClient.options.sneakKey.setPressed(false);
               minecraftClient.player.setSneaking(false);
               this.delayTicks = 5 + ThreadLocalRandom.current().nextInt(0, 2);
            }
         }
      }
   }

   private boolean hasSellableInInventory() {

      for (int index = 0; index < 36; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty() && this.isSellableItem(minecraftClientValue.getItem())) {
            return true;
         }
      }

      return false;
   }

   private boolean isChestOutputFull(ScreenHandler arg) {
      int intVal = arg.slots.size() - 36;

      for (int index = 0; index < intVal; index++) {
         if (arg.getSlot(index).getStack().isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private boolean quickMoveSellableItems(ScreenHandler arg) {
      byte byteVal = 0;

      for (int index = arg.slots.size() - 36; index < arg.slots.size(); index++) {
         ItemStack var1Value = arg.getSlot(index).getStack();
         if (var1Value != null && !var1Value.isEmpty() && this.isSellableItem(var1Value.getItem())) {
            minecraftClient.interactionManager.clickSlot(arg.syncId, index, 0, SlotActionType.QUICK_MOVE, minecraftClient.player);
         }
      }

      return byteVal != 0;
   }

   private boolean hasSellableInChest(ScreenHandler arg) {

      for (int index = arg.slots.size() - 36; index < arg.slots.size(); index++) {
         ItemStack var1Value = arg.getSlot(index).getStack();
         if (var1Value != null && !var1Value.isEmpty() && this.isSellableItem(var1Value.getItem())) {
            return true;
         }
      }

      return false;
   }

   private boolean isAdminNearby() {
      if ((Boolean)antiAdminSetting.getValue() && minecraftClient.player != null && minecraftClient.world != null) {
         if (this.isAdminPending && this.adminName != null) {
            long longVal = (System.currentTimeMillis() - this.adminDetectedTime) / 1000L;
            if (longVal < ((Integer)antiAdminOutDelaySetting.getValue()).intValue()) {
               return false;
            }

            boolean falseSnapshot = false;

            for (PlayerEntity class1657 : minecraftClient.world.getPlayers()) {
               if (class1657.getName().getString().equalsIgnoreCase(this.adminName)
                  && class1657.distanceTo(minecraftClient.player) < (Integer)antiAdminRangeSetting.getValue() * 2) {
                  falseSnapshot = true;
                  break;
               }
            }

            if (falseSnapshot) {
               if (minecraftClient.player.networkHandler != null) {
                  minecraftClient.player.networkHandler.getConnection().disconnect(Text.literal("AntiAdmin: player not gone"));
               }

               this.isAdminPending = false;
               return true;
            }

            this.isAdminPending = false;
            this.adminName = null;
         }

         PlayerEntity nullSnapshot = null;
         double var6Snapshot = Double.MAX_VALUE;

         for (PlayerEntity class16572 : minecraftClient.world.getPlayers()) {
            if (class16572 != minecraftClient.player) {
               double class16572Value = class16572.distanceTo(minecraftClient.player);
               if (!(class16572Value > ((Integer)antiAdminRangeSetting.getValue()).intValue())) {
                  boolean flag = this.isLookingAtPlayer(class16572, minecraftClient.player);
                  boolean minecraftClientValue = minecraftClient.player.hurtTime > 0 && class16572.squaredDistanceTo(minecraftClient.player) < 9.0;
                  if ((flag || minecraftClientValue) && class16572Value < var6Snapshot) {
                     var6Snapshot = class16572Value;
                     nullSnapshot = class16572;
                  }
               }
            }
         }

         if (nullSnapshot != null) {
            this.blockAdminUntilGone(nullSnapshot);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isLookingAtPlayer(PlayerEntity arg, PlayerEntity arg2) {
      Vec3d var1Value = arg.getCameraPosVec(1.0F);
      Vec3d var1Value2 = arg.getRotationVec(1.0F).normalize();
      Vec3d var2Value = arg2.getCameraPosVec(1.0F).subtract(var1Value).normalize();
      double var1Value2Value = var1Value2.dotProduct(var2Value);
      return var1Value2Value > 0.85;
   }

   private void blockAdminUntilGone(PlayerEntity arg) {
      this.adminName = arg.getName().getString();
      this.adminDetectedTime = System.currentTimeMillis();
      this.isAdminPending = true;

      for (int index = 0; index < 2; index++) {
         minecraftClient.options.sneakKey.setPressed(true);
         if (minecraftClient.player != null) {
            minecraftClient.player.setSneaking(true);
            if (minecraftClient.player.input != null && minecraftClient.getNetworkHandler() != null) {
               PlayerInput minecraftClientValue = minecraftClient.player.input.playerInput;
               PlayerInput local = new PlayerInput(
                  minecraftClientValue.forward(), minecraftClientValue.backward(), minecraftClientValue.left(), minecraftClientValue.right(), minecraftClientValue.jump(), true, minecraftClientValue.sprint()
               );
               minecraftClient.player.input.playerInput = local;
               minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local));
            }
         }

         try {
            Thread.sleep(80L);
         } catch (InterruptedException interruptedException) {
         }

         minecraftClient.options.sneakKey.setPressed(false);
         if (minecraftClient.player != null) {
            minecraftClient.player.setSneaking(false);
            if (minecraftClient.player.input != null && minecraftClient.getNetworkHandler() != null) {
               PlayerInput minecraftClientValue2 = minecraftClient.player.input.playerInput;
               PlayerInput local2 = new PlayerInput(
                  minecraftClientValue2.forward(), minecraftClientValue2.backward(), minecraftClientValue2.left(), minecraftClientValue2.right(), minecraftClientValue2.jump(), false, minecraftClientValue2.sprint()
               );
               minecraftClient.player.input.playerInput = local2;
               minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local2));
            }
         }

         try {
            Thread.sleep(80L);
         } catch (InterruptedException interruptedException2) {
         }
      }

      if (minecraftClient.interactionManager != null) {
         minecraftClient.interactionManager.attackEntity(minecraftClient.player, arg);
      }
   }

   private boolean isSellableItem(Item arg) {
      return new ArrayList((Collection)sellItemsSetting.getValue()).contains(arg);
   }

   public void resetStats() {
      this.sessionStartTime = 0L;
      this.lastWebhookPostTime = 0L;
      this.lastStatusPostTime = 0L;
      this.saleCount = 0;
      this.totalEarned = 0.0;
      this.webhookMessageId = null;
   }

   private void recordSale() {
      if ((Boolean)statsEnabledSetting.getValue()) {
         this.saleCount++;
         if (!(Boolean)chatParsingSetting.getValue()) {
            this.totalEarned = this.totalEarned + this.parseValuePerSale((String)valuePerSaleSetting.getValue());
         }

         this.postStatusWebhook();
      }
   }

   private double parseValuePerSale(String string) {
      if (string == null) {
         return 0.0;
      }

      string = string.trim().toLowerCase(Locale.ROOT).replace(",", "").replace("$", "").replace(" ", "");
      long longVal = 1L;
      if (string.endsWith("k")) {
         string = string.substring(0, string.length() - 1);
      } else if (string.endsWith("m")) {
         string = string.substring(0, string.length() - 1);
      } else if (string.endsWith("b")) {
         string = string.substring(0, string.length() - 1);
      }

      try {
         return Double.parseDouble(string) * longVal;
      } catch (Exception error) {
         return 0.0;
      }
   }

   private void postStatusWebhook() {
      if ((Boolean)webhookEnabledSetting.getValue() && this.hasWebhookUrl() && this.webhookMessageId != null) {
         long systemValue = System.currentTimeMillis();
         if (systemValue - this.lastStatusPostTime >= 5000L) {
            this.lastStatusPostTime = systemValue;
            this.sendStatusWebhook("Running");
         }
      }
   }

   private boolean hasWebhookUrl() {
      String local = (String)webhookUrlSetting.getValue();
      return local != null && !local.isBlank();
   }

   private String buildWebhookPayload(String string) {
      String minecraftClientValue = minecraftClient.player != null ? minecraftClient.player.getName().getString() : "Unknown";
      String minecraftClientValue2 = minecraftClient.getCurrentServerEntry() != null ? minecraftClient.getCurrentServerEntry().address : "Singleplayer";
      String local = "AutoSell Report";
      int intVal = this.isEnabled() ? 1752220 : 15158332;
      long longVal = this.sessionStartTime > 0L ? (System.currentTimeMillis() - this.sessionStartTime) / 1000L : 0L;
      long var63600LValue = longVal / 3600L;
      long longVal2 = longVal % 3600L / 60L;
      long longVal3 = longVal % 60L;
      StringBuilder stringBuilderInst = new StringBuilder();
      stringBuilderInst.append("{\"title\":\"").append(this.escapeJson(local)).append("\",");
      stringBuilderInst.append("\"color\":").append(intVal).append(",");
      stringBuilderInst.append("\"fields\":[");
      stringBuilderInst.append("{\"name\":\"Status\",\"value\":\"").append(this.escapeJson(string)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Player\",\"value\":\"").append(this.escapeJson(minecraftClientValue)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Server\",\"value\":\"").append(this.escapeJson(minecraftClientValue2)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Duration\",\"value\":\"").append(String.format(Locale.US, "%dh %dm %ds", var63600LValue, longVal2, longVal3)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Earned\",\"value\":\"").append(this.formatMoney(this.totalEarned)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Sales\",\"value\":\"").append(this.saleCount).append("\",\"inline\":true}");
      stringBuilderInst.append("],\"footer\":{\"text\":\"AutoSell Utility\"}}");
      StringBuilder stringBuilderInst2 = new StringBuilder("{");
      String local2 = (String)webhookNameSetting.getValue();
      if (local2 != null && !local2.isBlank()) {
         stringBuilderInst2.append("\"username\":\"").append(this.escapeJson(local2)).append("\",");
      }

      String local3 = (String)webhookAvatarSetting.getValue();
      if (local3 != null && !local3.isBlank()) {
         stringBuilderInst2.append("\"avatar_url\":\"").append(this.escapeJson(local3)).append("\",");
      }

      stringBuilderInst2.append("\"embeds\":[").append(stringBuilderInst).append("]}");
      return stringBuilderInst2.toString();
   }

   private void sendStatusWebhook(String string) {
      if ((Boolean)webhookEnabledSetting.getValue() && this.hasWebhookUrl()) {
         String local = this.buildWebhookPayload(string);
         String local2 = ((String)webhookUrlSetting.getValue()).trim();
         int intVal = this.webhookMessageId != null ? 1 : 0;
         new Thread(() -> {
            try {
               HttpClient httpClientValue = HttpClient.newHttpClient();
               Builder local4 = HttpRequest.newBuilder().header("Content-Type", "application/json").timeout(Duration.ofSeconds(8L));
               if (intVal != 0) {
                  local4.uri(URI.create(local2 + "/messages/" + this.webhookMessageId)).method("PATCH", BodyPublishers.ofString(local, StandardCharsets.UTF_8));
               } else {
                  local4.uri(URI.create(local2 + "?wait=true")).POST(BodyPublishers.ofString(local, StandardCharsets.UTF_8));
               }

               HttpResponse<String> local5 = httpClientValue.send(local4.build(), BodyHandlers.ofString());
               if (local5.statusCode() < 400 && intVal == 0 && local5.body() != null && local5.body().contains("\"id\"")) {
                  int local3 = local5.body().indexOf("\"id\"");
                  local3 = local5.body().indexOf(58, local3);
                  local3 = local5.body().indexOf(34, local3);
                  int local6 = local5.body().indexOf(34, local3 + 1);
                  if (local3 > 0 && local6 > local3) {
                     this.webhookMessageId = local5.body().substring(local3 + 1, local6);
                     this.lastWebhookPostTime = System.currentTimeMillis();
                  }
               }
            } catch (Exception error) {
            }
         }, "AutoSell-Webhook").start();
      }
   }

   private void sendStoppedWebhook(String string) {
      if ((Boolean)webhookEnabledSetting.getValue() && this.hasWebhookUrl()) {
         String local = this.buildWebhookPayload(string);
         String local2 = ((String)webhookUrlSetting.getValue()).trim();
         int intVal = this.webhookMessageId != null ? 1 : 0;

         try {
            HttpClient httpClientValue = HttpClient.newHttpClient();
            Builder httpRequestValue = HttpRequest.newBuilder().header("Content-Type", "application/json").timeout(Duration.ofSeconds(5L));
            if (intVal != 0) {
               httpRequestValue.uri(URI.create(local2 + "/messages/" + this.webhookMessageId)).method("PATCH", BodyPublishers.ofString(local, StandardCharsets.UTF_8));
            } else {
               httpRequestValue.uri(URI.create(local2 + "?wait=true")).POST(BodyPublishers.ofString(local, StandardCharsets.UTF_8));
            }

            httpClientValue.send(httpRequestValue.build(), BodyHandlers.ofString());
         } catch (Exception error) {
         }
      }
   }

   private String escapeJson(String string) {
      return string == null ? "" : string.replace("\\", "\\\\").replace("\"", "\\\"");
   }

   private String formatMoney(double doubleVal) {
      double absValue = Math.abs(doubleVal);
      String local = doubleVal < 0.0 ? "-$" : "$";
      if (absValue >= 1.0E9) {
         return String.format(Locale.US, "%s%.2fB", local, absValue / 1.0E9);
      } else if (absValue >= 1000000.0) {
         return String.format(Locale.US, "%s%.2fM", local, absValue / 1000000.0);
      } else {
         return absValue >= 1000.0 ? String.format(Locale.US, "%s%.2fK", local, absValue / 1000.0) : String.format(Locale.US, "%s%.2f", local, absValue);
      }
   }

}
