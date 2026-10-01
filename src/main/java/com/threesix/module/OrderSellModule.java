package com.threesix.module;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.Element;
import net.minecraft.registry.Registries;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.AutoSellState;
import com.threesix.setting.ItemSelectSetting;

public final class OrderSellModule extends ModuleBase {
   public final ItemSelectSetting itemSetting = new ItemSelectSetting("Item", Items.TOTEM_OF_UNDYING);
   public final ClientSetting sellPriceSetting = new ClientSetting("Sell Price", "114k");
   public final ClientSetting orderPriceSetting = new ClientSetting("Order Price", "104k");
   public final ClientSetting webhookSetting = new ClientSetting("Webhook", false);
   public final ClientSetting webhookUrlSetting = new ClientSetting("Webhook URL", "");
   public final ClientSetting webhookNameSetting = new ClientSetting("Webhook Name", "TotemMeta Report");
   public final ClientSetting chatLogSetting = new ClientSetting("Chat Log", true);
   private static final int minDelayTicks = 8;
   private static final int maxDelayTicks = 15;
   private static final long guiTimeoutMs = 3000L;
   private AutoSellState state = AutoSellState.CHECK_HOTBAR;
   private long stateStartTime = 0L;
   private int cooldownTicks = 0;
   private int lastTotemCount = 0;
   private int batchSoldCount = 0;
   private int totalSoldCount = 0;
   private long totalProfit = 0L;
   private long sessionStartTime = 0L;

   public OrderSellModule() {
      super("OrderSell", ModuleCategory.DONUT);
      this.registerSetting(this.itemSetting);
      this.registerSetting(this.sellPriceSetting);
      this.registerSetting(this.orderPriceSetting);
      this.registerSetting(this.webhookSetting);
      this.registerSetting(this.webhookUrlSetting);
      this.registerSetting(this.webhookNameSetting);
      this.registerSetting(this.chatLogSetting);
   }

   @Override
   public void onEnable() {
      this.state = AutoSellState.CHECK_HOTBAR;
      this.stateStartTime = System.currentTimeMillis();
      this.cooldownTicks = this.randomDelayTicks();
      this.lastTotemCount = 0;
      this.batchSoldCount = 0;
      this.totalSoldCount = 0;
      this.totalProfit = 0L;
      this.sessionStartTime = System.currentTimeMillis();
      this.log("Enabled - checking hotbar...");
   }

   @Override
   public void onDisable() {
      if ((Boolean)this.webhookSetting.getValue() && this.hasWebhookUrl() && this.batchSoldCount > 0) {
         this.sendWebhook(true);
      }

      this.log("Disabled");
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.getNetworkHandler() != null) {
         if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
         } else {
            long systemValue = System.currentTimeMillis();
            switch (this.state) {
               case CHECK_HOTBAR:
                  this.checkSoldProgress();
                  if (minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                     this.log("Â§eStill in GUI, closing before sell...");
                     minecraftClient.player.closeHandledScreen();
                     this.shiftClickFirstEmpty();
                     this.cooldownTicks = this.randomDelayTicks() + 10;
                     return;
                  }

                  long longVal = this.getSellPrice();
                  long longVal2 = this.getOrderPrice();
                  if (this.hasTotemInHotbar()) {
                     if (longVal < longVal2) {
                        this.log("Â§cSell < Order, skip");
                        this.cooldownTicks = this.randomDelayTicks() * 2;
                        return;
                     }

                     int intVal = this.findHotbarTotemSlot();
                     if (intVal == -1) {
                        this.cooldownTicks = this.randomDelayTicks();
                        return;
                     }

                     if (minecraftClient.player.getInventory().getSelectedSlot() != intVal) {
                        minecraftClient.player.getInventory().setSelectedSlot(intVal);
                        this.log("Switch hotbar -> " + (intVal + 1));
                        this.cooldownTicks = this.randomDelayTicks() + 10;
                        return;
                     }

                     ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(intVal);
                     if (minecraftClientValue.isEmpty() || !this.isTargetItem(minecraftClientValue.getItem())) {
                        this.cooldownTicks = this.randomDelayTicks();
                        return;
                     }

                     if (minecraftClient.currentScreen != null) {
                        minecraftClient.player.closeHandledScreen();
                        this.cooldownTicks = this.randomDelayTicks();
                        return;
                     }

                     this.lastTotemCount = this.countTotemItems();
                     this.log("Hotbar x" + this.lastTotemCount + " -> /ah sell " + longVal);
                     minecraftClient.getNetworkHandler().sendChatCommand("ah sell " + longVal);
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks() + 30;
                  } else {
                     if (this.hasTotemInInventory()) {
                        this.log("Found totem in inv, moving to hotbar...");
                        this.moveTotemsToHotbar();
                        this.cooldownTicks = this.randomDelayTicks();
                        return;
                     }

                     this.log("No totem in hotbar/inv -> /order (collect existing)");
                     minecraftClient.getNetworkHandler().sendChatCommand("order");
                     this.state = AutoSellState.WAIT_ORDER_GUI;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                  }
                  break;
               case WAIT_ORDER_GUI:
                  if (systemValue - this.stateStartTime < 3000L) {
                     return;
                  }

                  if (!(minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler local)) {
                     String minecraftClientValue2 = minecraftClient.currentScreen == null ? "null" : minecraftClient.currentScreen.getClass().getName();
                     if (systemValue - this.stateStartTime > 9000L) {
                        this.log("Â§cOrder GUI timeout (" + minecraftClientValue2 + ")");
                        this.state = AutoSellState.CHECK_HOTBAR;
                        this.cooldownTicks = this.randomDelayTicks();
                     }

                     return;
                  }

                  int intVal2 = this.findChestSlot();
                  if (intVal2 != -1) {
                     this.log("Click chest " + intVal2 + " -> open order list");
                     this.clickSlot(intVal2);
                     this.state = AutoSellState.WAIT_TOTEM_SELECT;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  if (this.clickButtonByText("order", "chest", "orders")) {
                     this.log("Clicked chest button");
                     this.state = AutoSellState.WAIT_TOTEM_SELECT;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  if (systemValue - this.stateStartTime > 6000L) {
                     this.log("Â§cChest not found, retry");
                     this.state = AutoSellState.CHECK_HOTBAR;
                     this.cooldownTicks = this.randomDelayTicks();
                  }
                  break;
               case WAIT_TOTEM_SELECT:
                  if (systemValue - this.stateStartTime < 3000L) {
                     return;
                  }

                  if (!(minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler local2)) {
                     if (systemValue - this.stateStartTime > 9000L) {
                        this.log("Â§cTotem select GUI timeout");
                        this.state = AutoSellState.CHECK_HOTBAR;
                        this.cooldownTicks = this.randomDelayTicks();
                     }

                     return;
                  }

                  int intVal3 = this.findTotemSlot();
                  if (intVal3 != -1) {
                     this.log("Click totem " + intVal3 + " -> open chest confirm");
                     this.clickSlot(intVal3);
                     this.state = AutoSellState.WAIT_CHEST_CONFIRM;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  if (this.clickButtonByText("totem", "undying")) {
                     this.log("Clicked totem button");
                     this.state = AutoSellState.WAIT_CHEST_CONFIRM;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  if (systemValue - this.stateStartTime > 6000L) {
                     this.log("Â§cTotem not found in list, retry");
                     minecraftClient.player.closeHandledScreen();
                     this.state = AutoSellState.CHECK_HOTBAR;
                     this.cooldownTicks = this.randomDelayTicks();
                  }
                  break;
               case WAIT_CHEST_CONFIRM:
                  if (systemValue - this.stateStartTime < 3000L) {
                     return;
                  }

                  if (!(minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler local3)) {
                     if (systemValue - this.stateStartTime > 9000L) {
                        this.log("Â§cChest confirm GUI timeout");
                        this.state = AutoSellState.CHECK_HOTBAR;
                        this.cooldownTicks = this.randomDelayTicks();
                     }

                     return;
                  }

                  int intVal4 = this.findChestSlot();
                  if (intVal4 != -1) {
                     this.log("Click chest confirm " + intVal4 + " -> open stash");
                     this.clickSlot(intVal4);
                     this.state = AutoSellState.WAIT_STASH;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  if (this.clickButtonByText("chest", "confirm", "collect")) {
                     this.log("Clicked chest confirm button");
                     this.state = AutoSellState.WAIT_STASH;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  if (systemValue - this.stateStartTime > 6000L) {
                     this.log("Â§cChest confirm not found");
                     minecraftClient.player.closeHandledScreen();
                     this.state = AutoSellState.CHECK_HOTBAR;
                     this.cooldownTicks = this.randomDelayTicks();
                  }
                  break;
               case WAIT_STASH:
                  if (systemValue - this.stateStartTime < 3000L) {
                     return;
                  }

                  if (this.isInventoryFull()) {
                     this.log("Â§eInv full -> close & sell first");
                     if (minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                        minecraftClient.player.closeHandledScreen();
                     }

                     this.shiftClickFirstEmpty();
                     this.state = AutoSellState.COLLECT_TO_HOTBAR;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  if (!(minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler local4)) {
                     this.state = AutoSellState.COLLECT_TO_HOTBAR;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  boolean falseSnapshot = false;

                  for (int index = 0; index < local4.slots.size() - 36; index++) {
                     if (!local4.getSlot(index).getStack().isEmpty() && this.isTargetItem(local4.getSlot(index).getStack().getItem())) {
                        falseSnapshot = true;
                        break;
                     }
                  }

                  if (!falseSnapshot) {
                     if (systemValue - this.stateStartTime > 3000L) {
                        this.log("No totem in order stash, closing");
                        minecraftClient.player.closeHandledScreen();
                        this.state = AutoSellState.COLLECT_TO_HOTBAR;
                        this.stateStartTime = systemValue;
                        this.cooldownTicks = this.randomDelayTicks();
                     }

                     return;
                  }

                  this.log("Collecting totems to inv...");

                  for (int index2 = 0; index2 < local4.slots.size() - 36; index2++) {
                     ItemStack var3Value = local4.getSlot(index2).getStack();
                     if (!var3Value.isEmpty() && this.isTargetItem(var3Value.getItem())) {
                        this.quickMoveSlot(index2);
                     }
                  }

                  this.state = AutoSellState.WAIT_STASH;
                  this.stateStartTime = systemValue;
                  this.cooldownTicks = this.randomDelayTicks();
                  boolean falseSnapshot2 = false;

                  for (int index3 = 0; index3 < local4.slots.size() - 36; index3++) {
                     if (!local4.getSlot(index3).getStack().isEmpty() && this.isTargetItem(local4.getSlot(index3).getStack().getItem())) {
                        falseSnapshot2 = true;
                        break;
                     }
                  }

                  if (!falseSnapshot2 && systemValue - this.stateStartTime > 3000L) {
                     minecraftClient.player.closeHandledScreen();
                     this.state = AutoSellState.COLLECT_TO_HOTBAR;
                     this.stateStartTime = systemValue;
                     this.cooldownTicks = this.randomDelayTicks();
                  }
                  break;
               case COLLECT_TO_HOTBAR:
                  if (minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                     minecraftClient.player.closeHandledScreen();
                     this.shiftClickFirstEmpty();
                     this.cooldownTicks = this.randomDelayTicks();
                     return;
                  }

                  this.log("Moving totems to hotbar...");
                  this.moveTotemsToHotbar();
                  this.state = AutoSellState.CHECK_HOTBAR;
                  this.stateStartTime = systemValue;
                  this.cooldownTicks = this.randomDelayTicks() + 10;
            }
         }
      }
   }

   private void checkSoldProgress() {

      int intVal = this.countTotemItems();
      int maxValue = Math.max(0, this.lastTotemCount - intVal);
      if (maxValue > 0) {
         long longVal = this.getSellPrice() - this.getOrderPrice();
         this.batchSoldCount += maxValue;
         this.totalSoldCount += maxValue;
         this.totalProfit += maxValue * longVal;
         this.log("Sold " + maxValue + " profit $" + this.formatMoney(longVal) + " batch $" + this.formatMoney(maxValue * longVal));
         this.lastTotemCount = intVal;
         if (!this.hasTotemInInventory() && this.batchSoldCount > 0) {
            this.log("Batch done " + this.batchSoldCount + " totalProfit $" + this.formatMoney(this.totalProfit));
            if ((Boolean)this.webhookSetting.getValue() && this.hasWebhookUrl()) {
               this.sendWebhook(false);
            }

            this.batchSoldCount = 0;
         }
      }
   }

   private void moveTotemsToHotbar() {
      if (minecraftClient.player != null && minecraftClient.interactionManager != null) {
         if (!(minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            this.shiftClickFirstEmpty();
            PlayerScreenHandler minecraftClientValue = minecraftClient.player.playerScreenHandler;
            int local2 = 0;
            while (local2 < 9) {
               int var4Snapshot = -1;
               for (int index = 0; index < 9; index++) {
                  if (minecraftClient.player.getInventory().getStack(index).isEmpty()) {
                     var4Snapshot = index;
                     break;
                  }
               }

               if (var4Snapshot == -1) {
                  for (int index2 = 0; index2 < 9; index2++) {
                     if (!this.isTargetItem(minecraftClient.player.getInventory().getStack(index2).getItem())) {
                        var4Snapshot = index2;
                        break;
                     }
                  }

                  if (var4Snapshot == -1) {
                     break;
                  }
               }

               int local = -1;
               int var8Snapshot = -1;
               for (Slot class1735 : minecraftClientValue.slots) {
                  if (class1735.inventory == minecraftClient.player.getInventory()) {
                     int var7Value;
                     try {
                        var7Value = class1735.getIndex();
                     } catch (Exception error) {
                        continue;
                     }

                     if (var7Value >= 9 && var7Value < 36) {
                        ItemStack var7Value2 = class1735.getStack();
                        if (!var7Value2.isEmpty() && this.isTargetItem(var7Value2.getItem())) {
                           local = class1735.id;
                           var8Snapshot = var7Value;
                           break;
                        }
                     }
                  }
               }

               if (local == -1) {
                  break;
               }

               try {
                  minecraftClient.interactionManager.clickSlot(minecraftClientValue.syncId, local, var4Snapshot, SlotActionType.SWAP, minecraftClient.player);
                  this.log("Move inv " + var8Snapshot + " -> hotbar " + (var4Snapshot + 1));
                  local2++;
               } catch (Exception error2) {
                  this.log("Â§cSwap fail: " + error2.getMessage());
                  break;
               }

               if (local2 >= 5) {
                  break;
               }
            }

            if (local2 == 0) {
               this.log("Â§7Hotbar already has totem or no inv to move");
            } else {
               this.log("Moved " + local2 + " stack(s) to hotbar");
            }
         }
      }
   }

   private int randomDelayTicks() {

      return ThreadLocalRandom.current().nextInt(8, 16);
   }

   private boolean hasWebhookUrl() {
      String local = (String)this.webhookUrlSetting.getValue();
      return local != null && !local.isBlank() && local.startsWith("http");
   }

   private boolean hasButtonWidget() {
      try {
         if (minecraftClient.currentScreen == null) {
            return false;
         }

         for (Element class364 : minecraftClient.currentScreen.children()) {
            String local = class364.getClass().getSimpleName().toLowerCase(Locale.ROOT);
            if (local.contains("button")) {
               return true;
            }
         }

         String minecraftClientValue = minecraftClient.currentScreen.getClass().getName().toLowerCase(Locale.ROOT);
         if (minecraftClientValue.contains("button") || minecraftClientValue.contains("screen") && !minecraftClientValue.contains("handledscreen") && !minecraftClientValue.contains("genericcontainer")) {
            return minecraftClient.currentScreen.children().size() > 0;
         }
      } catch (Exception error) {
      }

      return false;
   }

   private boolean clickButtonByText(String... local) {
      try {
         if (minecraftClient.currentScreen == null) {
            return false;
         }

         for (Element class364 : minecraftClient.currentScreen.children()) {
            try {
               Method local2 = class364.getClass().getMethod("getMessage");
               Object local3 = local2.invoke(class364);
               String local4 = local3 == null ? "" : local3.toString().toLowerCase(Locale.ROOT);

               for (String string : local) {
                  if (string != null && !string.isBlank() && local4.contains(string.toLowerCase(Locale.ROOT))) {
                     try {
                        Method local5 = class364.getClass().getMethod("onPress");
                        local5.setAccessible(true);
                        local5.invoke(class364);
                        return true;
                     } catch (Exception error) {
                        try {
                           Method local6 = class364.getClass().getMethod("mouseClicked", double.class, double.class, int.class);
                           local6.invoke(class364, 0.0, 0.0, 0);
                           return true;
                        } catch (Exception error2) {
                        }
                     }
                  }
               }
            } catch (Exception error3) {
            }
         }
      } catch (Exception error4) {
         this.log("Button click fail: " + error4.getMessage());
      }

      return false;
   }

   private void log(String string) {

      if (!(Boolean)this.chatLogSetting.getValue()) {
         System.out.println("[TotemMeta] " + string);
      } else {
         try {
            if (minecraftClient.player != null) {
               minecraftClient.player.sendMessage(Text.literal("Â§7[TotemMeta] Â§f" + string), false);
            }

            System.out.println("[TotemMeta] " + string);
         } catch (Exception error) {
         }
      }
   }

   private boolean isTargetItem(Item arg) {
      return new ArrayList((Collection)this.itemSetting.getValue()).contains(arg);
   }

   private boolean hasTotemInHotbar() {

      for (int index = 0; index < 9; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty() && this.isTargetItem(minecraftClientValue.getItem())) {
            return true;
         }
      }

      return false;
   }

   private boolean hasTotemInInventory() {

      for (int index = 0; index < 36; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty() && this.isTargetItem(minecraftClientValue.getItem())) {
            return true;
         }
      }

      return false;
   }

   private boolean isInventoryFull() {

      for (int index = 0; index < 36; index++) {
         if (minecraftClient.player.getInventory().getStack(index).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private int countTotemItems() {
      if (minecraftClient.player == null) {
         return 0;
      }

      int local = 0;
      for (int index = 0; index < 36; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty() && this.isTargetItem(minecraftClientValue.getItem())) {
            local += minecraftClientValue.getCount();
         }
      }

      return local;
   }

   private int findHotbarTotemSlot() {

      for (int index = 0; index < 9; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty() && this.isTargetItem(minecraftClientValue.getItem())) {
            return index;
         }
      }

      return -1;
   }

   private String getTargetItemId() {
      if (!((Set)this.itemSetting.getValue()).isEmpty()) {
         Item local = (Item)((Set)this.itemSetting.getValue()).iterator().next();
         Identifier local2 = Registries.ITEM.getId(local);
         if (local2 != null) {
            return local2.toString();
         }
      }

      return "minecraft:totem_of_undying";
   }

   private int findChestSlot() {
      if (!(minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler local)) {
         return -1;
      } else {

         int var3Snapshot = -1;
         for (int index = 0; index < local.slots.size() - 36; index++) {
            ItemStack var1Value = local.getSlot(index).getStack();
            if (!var1Value.isEmpty()) {
               Item var4Value = var1Value.getItem();
               String var4Value2 = var1Value.getName().getString().toLowerCase(Locale.ROOT);
               boolean flag = var4Value == Items.CHEST
                  || var4Value == Items.CHEST_MINECART
                  || var4Value == Items.TRAPPED_CHEST
                  || var4Value2.contains("chest")
                  || var4Value2.contains("rÆ°Æ¡ng");
               if (flag && !var4Value2.contains("armor") && !var4Value2.contains("giÃ¡p")) {
                  if (var4Value2.contains("order") || var4Value2.contains("Ä‘Æ¡n")) {
                     return index;
                  }

                  if (var3Snapshot == -1) {
                     var3Snapshot = index;
                  }
               }
            }
         }

         return var3Snapshot;
      }
   }

   private int findTotemSlot() {
      if (minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler local) {
         for (int index = 0; index < local.slots.size() - 36; index++) {
            ItemStack var1Value = local.getSlot(index).getStack();
            if (!var1Value.isEmpty() && this.isTargetItem(var1Value.getItem())) {
               return index;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private void clickSlot(int intVal) {
      try {
         if (minecraftClient.interactionManager != null && minecraftClient.player != null) {
            minecraftClient.interactionManager.clickSlot(minecraftClient.player.currentScreenHandler.syncId, intVal, 0, SlotActionType.PICKUP, minecraftClient.player);
         }
      } catch (Exception error) {
      }
   }

   private void quickMoveSlot(int intVal) {
      try {
         if (minecraftClient.interactionManager != null && minecraftClient.player != null) {
            minecraftClient.interactionManager.clickSlot(minecraftClient.player.currentScreenHandler.syncId, intVal, 0, SlotActionType.QUICK_MOVE, minecraftClient.player);
         }
      } catch (Exception error) {
      }
   }

   private void shiftClickFirstEmpty() {
      try {
         if (minecraftClient.player != null && !minecraftClient.player.currentScreenHandler.getCursorStack().isEmpty()) {
            for (Slot class1735 : minecraftClient.player.playerScreenHandler.slots) {
               if (class1735.getStack().isEmpty() && class1735.inventory == minecraftClient.player.getInventory()) {
                  minecraftClient.interactionManager
                     .clickSlot(minecraftClient.player.currentScreenHandler.syncId, class1735.id, 0, SlotActionType.PICKUP, minecraftClient.player);
                  break;
               }
            }

            if (!minecraftClient.player.currentScreenHandler.getCursorStack().isEmpty() && minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler local) {
               for (int index = 0; index < local.slots.size() - 36; index++) {
                  if (local.getSlot(index).getStack().isEmpty()) {
                     minecraftClient.interactionManager.clickSlot(local.syncId, index, 0, SlotActionType.PICKUP, minecraftClient.player);
                     break;
                  }
               }
            }
         }
      } catch (Exception error) {
      }
   }

   private void sendWebhook(boolean flag) {

      if (this.hasWebhookUrl()) {
         String local = ((String)this.webhookUrlSetting.getValue()).trim();
         String local2 = this.buildWebhookPayload(flag);
         this.log("Sending webhook...");
         new Thread(
               () -> {
                  try {
                     HttpRequest httpRequestValue = HttpRequest.newBuilder()
                        .uri(URI.create(local))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(8L))
                        .POST(BodyPublishers.ofString(local2, StandardCharsets.UTF_8))
                        .build();
                     HttpResponse<String> local3 = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build().send(httpRequestValue, BodyHandlers.ofString());
                     this.log("Webhook res " + local3.statusCode());
                  } catch (Exception error) {
                     this.log("Webhook fail " + error.getMessage());
                  }
               },
               "TotemMeta-Webhook"
            )
            .start();
      }
   }

   private long getSellPrice() {
      return this.parsePrice((String)this.sellPriceSetting.getValue());
   }

   private long getOrderPrice() {
      return this.parsePrice((String)this.orderPriceSetting.getValue());
   }

   private long parsePrice(String string) {

      if (string == null) {
         return 0L;
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
         double doubleValue = Double.parseDouble(string);
         return (long)(doubleValue * longVal);
      } catch (Exception error) {
         return 0L;
      }
   }

   private String buildWebhookPayload(boolean flag) {

      String minecraftClientValue = minecraftClient.player != null ? minecraftClient.player.getName().getString() : "Unknown";
      String minecraftClientValue2 = minecraftClient.getCurrentServerEntry() != null ? minecraftClient.getCurrentServerEntry().address : "DonutSMP";
      long longVal = this.getOrderPrice();
      long longVal2 = this.getSellPrice();
      long var6Var4Value = longVal2 - longVal;
      long longVal3 = this.batchSoldCount * var6Var4Value;
      long longVal4 = this.sessionStartTime > 0L ? (System.currentTimeMillis() - this.sessionStartTime) / 1000L : 0L;
      long var123600LValue = longVal4 / 3600L;
      long longVal5 = longVal4 % 3600L / 60L;
      long longVal6 = longVal4 % 60L;
      String local = flag ? "TotemMeta - Stopped" : "TotemMeta - Batch Sold";
      int intVal = flag ? 15158332 : 3066993;
      StringBuilder stringBuilderInst = new StringBuilder();
      stringBuilderInst.append("{\"name\":\"Player\",\"value\":\"").append(this.escapeJson(minecraftClientValue)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Server\",\"value\":\"").append(this.escapeJson(minecraftClientValue2)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Order\",\"value\":\"$").append(this.formatMoney(longVal)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Sell\",\"value\":\"$").append(this.formatMoney(longVal2)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Profit/1\",\"value\":\"$").append(this.formatMoney(var6Var4Value)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Sold (Batch)\",\"value\":\"").append(this.batchSoldCount).append(" totem\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Profit (Batch)\",\"value\":\"$").append(this.formatMoney(longVal3)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Total Sold\",\"value\":\"").append(this.totalSoldCount).append(" totem\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Total Profit\",\"value\":\"$").append(this.formatMoney(this.totalProfit)).append("\",\"inline\":true},");
      stringBuilderInst.append("{\"name\":\"Duration\",\"value\":\"").append(String.format(Locale.US, "%dh %dm %ds", var123600LValue, longVal5, longVal6)).append("\",\"inline\":true}");
      String stringValue = String.format(Locale.US, "Batch %d totem x $%s = $%s profit", this.batchSoldCount, this.formatMoney(var6Var4Value), this.formatMoney(longVal3));
      StringBuilder stringBuilderInst2 = new StringBuilder("{");
      String local2 = (String)this.webhookNameSetting.getValue();
      if (local2 != null && !local2.isBlank()) {
         stringBuilderInst2.append("\"username\":\"").append(this.escapeJson(local2)).append("\",");
      }

      stringBuilderInst2.append("\"embeds\":[{\"title\":\"").append(this.escapeJson(local)).append("\",");
      stringBuilderInst2.append("\"description\":\"").append(this.escapeJson(stringValue)).append("\",");
      stringBuilderInst2.append("\"color\":").append(intVal).append(",");
      stringBuilderInst2.append("\"fields\":[").append(stringBuilderInst).append("],");
      stringBuilderInst2.append("\"footer\":{\"text\":\"TotemMeta â€¢ Threesix\"},");
      stringBuilderInst2.append("\"timestamp\":\"").append(Instant.now().toString()).append("\"}]}");
      return stringBuilderInst2.toString();
   }

   private String escapeJson(String string) {
      return string == null ? "" : string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
   }

   private String formatMoney(long longVal) {

      long absValue = Math.abs(longVal);
      if (absValue >= 1000000000L) {
         return String.format(Locale.US, "%.2fB", longVal / 1.0E9);
      } else if (absValue >= 1000000L) {
         return String.format(Locale.US, "%.2fM", longVal / 1000000.0);
      } else {
         return absValue >= 1000L ? String.format(Locale.US, "%.1fK", longVal / 1000.0) : String.valueOf(longVal);
      }
   }

}
