package com.threesix.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.util.Hand;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.hit.HitResult.Type;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.ClickBlockType;
import com.threesix.setting.ModeSetting02;
import com.threesix.data.SpawnerOrderState;

public final class SpawnerOrderBotModule extends ModuleBase {
   public static final int minDelayMillis = 100;
   public static final int maxDelayMillis = 2000;
   public static final int defaultDelayMillis = 300;
   public static final int chestRowCount = 36;
   public static final int zeroDelayConstant = 0;
   public static final long dropConfirmTimeoutMillis = 4000L;
   public final ModeSetting02 modeSetting = new ModeSetting02("Mode", "Spawner", "Spawner", "Orders");
   public final ClientSetting delaySetting = new ClientSetting("Delay", 300, 100, 2000);
   public SpawnerOrderState state = SpawnerOrderState.SPAWNER_OPEN_MENU;
   public String activeMode = (String)this.modeSetting.getValue();
   public long nextActionTime;
   public int dropperCountAtClick;
   public long dropperClickTime;
   public boolean isScreenChanged;

   public SpawnerOrderBotModule() {
      super("BoneDropper", ModuleCategory.DONUT);
      this.registerSetting(this.modeSetting);
      this.registerSetting(this.delaySetting);
   }

   @Override
   public void onEnable() {
      this.resetBotState();
   }

   @Override
   public void onDisable() {
      this.state = SpawnerOrderState.SPAWNER_OPEN_MENU;
      this.nextActionTime = 0L;
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null
         && minecraftClient.world != null
         && minecraftClient.interactionManager != null
         && (minecraftClient.currentScreen == null || minecraftClient.currentScreen instanceof HandledScreen)) {
         if (!((String)this.modeSetting.getValue()).equalsIgnoreCase(this.activeMode)) {
            this.resetBotState();
         }

         long systemValue = System.currentTimeMillis();
         if (systemValue >= this.nextActionTime) {
            if (this.modeSetting.isModeSelected2("Spawner")) {
               this.tickSpawnerFlow();
            } else {
               this.tickOrdersFlow();
            }
         }
      }
   }

   public void tickSpawnerFlow() {
      switch (this.state) {
         case SPAWNER_OPEN_MENU:
            if (this.hasOpenScreen()) {
               this.state = SpawnerOrderState.SPAWNER_WAIT_MENU;
               this.scheduleNextAction();
               return;
            }

            this.openSpawnerMenu();
            this.state = SpawnerOrderState.SPAWNER_WAIT_MENU;
            this.scheduleNextAction();
            break;
         case SPAWNER_WAIT_MENU:
            if (!this.hasOpenScreen()) {
               this.state = SpawnerOrderState.SPAWNER_OPEN_MENU;
               this.scheduleNextAction();
               return;
            }

            this.state = SpawnerOrderState.SPAWNER_SCAN_GRID;
            this.scheduleNextAction();
            break;
         case SPAWNER_SCAN_GRID:
            ScreenHandler local = this.getOpenChestScreen();
            if (local == null) {
               this.state = SpawnerOrderState.SPAWNER_OPEN_MENU;
               this.scheduleNextAction();
               return;
            }

            if (!this.chestHasOnlyBones(local)) {
               this.scheduleNextAction();
               return;
            }

            this.state = SpawnerOrderState.SPAWNER_CLICK_DROPPER;
            this.scheduleNextAction();
            break;
         case SPAWNER_CLICK_DROPPER:
            ScreenHandler local2 = this.getOpenChestScreen();
            if (local2 == null) {
               this.state = SpawnerOrderState.SPAWNER_OPEN_MENU;
               this.scheduleNextAction();
               return;
            }

            Slot local3 = this.findDropperSlot(local2);
            if (local3 == null) {
               this.scheduleNextAction();
               return;
            }

            this.isScreenChanged = this.chestHasOnlyBones(local2);
            if (!this.isScreenChanged) {
               this.state = SpawnerOrderState.SPAWNER_SCAN_GRID;
               this.scheduleNextAction();
               return;
            }

            this.dropperCountAtClick = this.countItemInInventory(Items.BONE);
            this.dropperClickTime = System.currentTimeMillis();
            this.attackSlot(local3);
            this.state = SpawnerOrderState.SPAWNER_WAIT_DROP_CONFIRM;
            this.scheduleNextAction();
            break;
         case SPAWNER_WAIT_DROP_CONFIRM:
            long systemValue = System.currentTimeMillis();
            ScreenHandler local4 = this.getOpenChestScreen();
            int intVal = this.isScreenChanged && local4 != null && !this.chestHasOnlyBones(local4) ? 1 : 0;
            boolean flag = this.countItemInInventory(Items.BONE) > this.dropperCountAtClick;
            if (intVal != 0 || flag) {
               if (local4 != null) {
                  this.closeScreen();
               }

               this.state = SpawnerOrderState.SPAWNER_DONE;
               this.nextActionTime = Long.MAX_VALUE;
               return;
            }

            if (this.dropperClickTime > 0L && systemValue - this.dropperClickTime > 4000L) {
               this.state = local4 == null ? SpawnerOrderState.SPAWNER_OPEN_MENU : SpawnerOrderState.SPAWNER_SCAN_GRID;
               this.scheduleNextAction();
               return;
            }

            this.scheduleNextAction();
         case SPAWNER_DONE:
            break;
         default:
            this.state = SpawnerOrderState.SPAWNER_OPEN_MENU;
            this.scheduleNextAction();
      }
   }

   public void tickOrdersFlow() {
      switch (this.state) {
         case ORDERS_SEND_COMMAND:
            if (!this.hasOpenScreen()) {
               this.sendChatCommand("/order");
               this.state = SpawnerOrderState.ORDERS_WAIT_MENU;
               this.scheduleNextAction();
               return;
            }

            this.state = SpawnerOrderState.ORDERS_CLICK_CHEST_ONE;
            this.scheduleNextAction();
            break;
         case ORDERS_WAIT_MENU:
            if (!this.hasOpenScreen()) {
               this.sendChatCommand("/order");
               this.scheduleNextAction();
               return;
            }

            this.state = SpawnerOrderState.ORDERS_CLICK_CHEST_ONE;
            this.scheduleNextAction();
            break;
         case ORDERS_CLICK_CHEST_ONE:
            this.clickSlotAndAdvance(ClickBlockType.CHEST, SpawnerOrderState.ORDERS_CLICK_BONE);
            break;
         case ORDERS_CLICK_BONE:
            this.clickSlotAndAdvance(ClickBlockType.BONE, SpawnerOrderState.ORDERS_CLICK_CHEST_TWO);
            break;
         case ORDERS_CLICK_CHEST_TWO:
            this.clickSlotAndAdvance(ClickBlockType.CHEST, SpawnerOrderState.ORDERS_CLICK_DROPPER_ONE);
            break;
         case ORDERS_CLICK_DROPPER_ONE:
            this.clickSlotAndAdvance(ClickBlockType.DROPPER, SpawnerOrderState.ORDERS_CLICK_ARROW);
            break;
         case ORDERS_CLICK_ARROW:
            this.clickSlotAndAdvance(ClickBlockType.ARROW, SpawnerOrderState.ORDERS_CLICK_DROPPER_TWO);
            break;
         case ORDERS_CLICK_DROPPER_TWO:
            this.clickSlotAndAdvance(ClickBlockType.DROPPER, SpawnerOrderState.ORDERS_CLICK_CHEST_ONE);
            break;
         default:
            this.state = SpawnerOrderState.ORDERS_SEND_COMMAND;
            this.scheduleNextAction();
      }
   }

   public void clickSlotAndAdvance(ClickBlockType clickBlockType, SpawnerOrderState spawnerOrderState) {
      ScreenHandler local = this.getOpenChestScreen();
      if (local == null) {
         this.sendChatCommand("/order");
         this.scheduleNextAction();
      } else {
         Slot local2 = this.findSlotForType(local, clickBlockType, clickBlockType == ClickBlockType.DROPPER || clickBlockType == ClickBlockType.ARROW);
         if (local2 == null) {
            this.scheduleNextAction();
         } else {
            this.attackSlot(local2);
            this.state = spawnerOrderState;
            this.scheduleNextAction();
         }
      }
   }

   public boolean chestHasOnlyBones(ScreenHandler arg) {
      List<Slot> local = this.getFrontChestSlots(arg);
      if (local.isEmpty()) {
         return false;
      }

      for (Slot class1735 : local) {
         if (class1735.isEnabled()) {
            ItemStack var4Value = class1735.getStack();
            if (var4Value.isEmpty() || !this.isItemOfType(var4Value, ClickBlockType.BONE)) {
               return false;
            }
         }
      }

      return true;
   }

   public List<Slot> getFrontChestSlots(ScreenHandler arg) {
      List<Slot> local = this.getChestSlots(arg);
      if (local.isEmpty()) {
         return List.of();
      }

      int intVal = local.stream().mapToInt(item -> item.y).max().orElse(Integer.MIN_VALUE);
      ArrayList<Slot> arrayListInst = new ArrayList<>();

      for (Slot class1735 : local) {
         if (class1735.y < intVal) {
            arrayListInst.add(class1735);
         }
      }

      return arrayListInst.isEmpty() ? local : arrayListInst;
   }

   public Slot findDropperSlot(ScreenHandler arg) {
      List<Slot> local = this.getChestSlots(arg);
      if (local.isEmpty()) {
         return null;
      }

      int intVal = local.stream().mapToInt(item -> item.y).max().orElse(Integer.MIN_VALUE);
      Slot local2 = this.findBestSlot(local, ClickBlockType.DROPPER, true, intVal);
      if (local2 != null) {
         return local2;
      }

      Slot local3 = this.findBestSlot(local, ClickBlockType.DROPPER, true, Integer.MIN_VALUE);
      if (local3 != null) {
         return local3;
      }

      for (int index = local.size() - 1; index >= 0; index--) {
         Slot local4 = (Slot)local.get(index);
         if (local4.isEnabled()) {
            return local4;
         }
      }

      return null;
   }

   public Slot findSlotForType(ScreenHandler arg, ClickBlockType clickBlockType, boolean flag) {
      return this.findBestSlot(this.getChestSlots(arg), clickBlockType, flag, Integer.MIN_VALUE);
   }

   public Slot findBestSlot(List<Slot> list, ClickBlockType clickBlockType, boolean flag, int intVal) {
      Slot nullSnapshot = null;

      for (Slot class1735 : list) {
         if (class1735.isEnabled() && (intVal == Integer.MIN_VALUE || class1735.y == intVal) && this.isItemOfType(class1735.getStack(), clickBlockType)) {
            if (nullSnapshot == null) {
               nullSnapshot = class1735;
            } else if (flag) {
               if (class1735.y > nullSnapshot.y || class1735.y == nullSnapshot.y && class1735.x >= nullSnapshot.x) {
                  nullSnapshot = class1735;
               }
            } else if (class1735.y < nullSnapshot.y || class1735.y == nullSnapshot.y && class1735.x < nullSnapshot.x) {
               nullSnapshot = class1735;
            }
         }
      }

      return nullSnapshot;
   }

   public boolean isItemOfType(ItemStack arg, ClickBlockType clickBlockType) {
      if (arg != null && !arg.isEmpty()) {
         Item var1Value = arg.getItem();
         String var1Value2 = arg.getName().getString().toLowerCase(Locale.ROOT);

         return switch (clickBlockType) {
            case BONE -> var1Value != Items.BONE && !var1Value2.contains("bone") ? false : true;
            case CHEST -> var1Value != Items.CHEST
                  && var1Value != Items.TRAPPED_CHEST
                  && var1Value != Items.ENDER_CHEST
                  && !var1Value2.contains("chest")
                  && !var1Value2.contains("truhe")
               ? false
               : true;
            case DROPPER -> var1Value != Items.DROPPER && !var1Value2.contains("dropper") ? false : true;
            case ARROW -> var1Value != Items.ARROW && !var1Value2.contains("arrow") && !var1Value2.contains("pfeil") ? false : true;
         };
      } else {
         return false;
      }
   }

   public List<Slot> getChestSlots(ScreenHandler arg) {
      if (arg != null && arg.slots != null && !arg.slots.isEmpty()) {
         int maxValue = Math.max(0, arg.slots.size() - 36);
         if (maxValue == 0) {
            maxValue = arg.slots.size();
         }

         ArrayList<Slot> arrayListInst = new ArrayList<>(maxValue);

         for (int index = 0; index < maxValue; index++) {
            arrayListInst.add((Slot)arg.slots.get(index));
         }

         return arrayListInst;
      } else {
         return List.of();
      }
   }

   public int countItemInInventory(Item arg) {
      if (minecraftClient.player == null) {
         return 0;
      }

      int local = 0;
      for (int index = 0; index < minecraftClient.player.getInventory().size(); index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty() && minecraftClientValue.isOf(arg)) {
            local += minecraftClientValue.getCount();
         }
      }

      return local;
   }

   public void attackSlot(Slot arg) {
      if (arg != null && minecraftClient.player != null && minecraftClient.interactionManager != null) {
         minecraftClient.interactionManager.clickSlot(minecraftClient.player.currentScreenHandler.syncId, arg.id, 0, SlotActionType.PICKUP, minecraftClient.player);
      }
   }

   public void openSpawnerMenu() {
      if (minecraftClient.crosshairTarget instanceof BlockHitResult local && minecraftClient.crosshairTarget.getType() == Type.BLOCK) {
         minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, local);
         minecraftClient.player.swingHand(Hand.MAIN_HAND);
      }
   }

   public void closeScreen() {
      if (minecraftClient.player != null && minecraftClient.currentScreen instanceof HandledScreen) {
         minecraftClient.player.closeHandledScreen();
         minecraftClient.setScreen(null);
      }
   }

   public ScreenHandler getOpenChestScreen() {
      return minecraftClient.currentScreen instanceof HandledScreen && minecraftClient.player != null ? minecraftClient.player.currentScreenHandler : null;
   }

   public boolean hasOpenScreen() {
      return this.getOpenChestScreen() != null;
   }

   public void sendChatCommand(String string) {
      ClientPlayNetworkHandler minecraftClientValue = minecraftClient.player != null ? minecraftClient.player.networkHandler : minecraftClient.getNetworkHandler();
      if (minecraftClientValue != null) {
         String local = string.startsWith("/") ? string.substring(1) : string;

         try {
            minecraftClientValue.sendChatCommand(local);
         } catch (Throwable error) {
            minecraftClientValue.sendChatMessage(string);
         }
      }
   }

   public void resetBotState() {
      this.activeMode = (String)this.modeSetting.getValue();
      this.state = this.modeSetting.isModeSelected2("Spawner") ? SpawnerOrderState.SPAWNER_OPEN_MENU : SpawnerOrderState.ORDERS_SEND_COMMAND;
      this.nextActionTime = 0L;
      this.dropperCountAtClick = 0;
      this.dropperClickTime = 0L;
      this.isScreenChanged = false;
   }

   public void scheduleNextAction() {
      this.nextActionTime = System.currentTimeMillis() + ((Integer)this.delaySetting.getValue()).intValue();
   }

}
