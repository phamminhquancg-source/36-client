package com.threesix.module;

import java.util.Random;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Items;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AutoTotemModule extends ModuleBase {
   public final ClientSetting delaySetting = new ClientSetting("Delay", 2.0F, 0.0F, 20.0F);
   public final ClientSetting hotbarSetting = new ClientSetting("Hotbar", false);
   public final ClientSetting totemSlotSetting = new ClientSetting("Totem Slot", 1.0F, 1.0F, 9.0F);
   public final ClientSetting forceTotemSetting = new ClientSetting("Force Totem", false);
   public final ClientSetting autoOpenSetting = new ClientSetting("Auto Open", false);
   public final ClientSetting closeDelaySetting = new ClientSetting("Close Delay", 3.0F, 0.0F, 20.0F);
   public static final int stateIdle = 0;
   public static final int stateOpen = 1;
   public static final int stateWait = 2;
   public static final int stateClose = 3;
   public int state = 0;
   public int stateTicks = 0;
   public boolean wasHoldingTotem = true;
   public final Random random = new Random();

   public AutoTotemModule() {
      super("Auto Inv Totem", ModuleCategory.COMBAT);
      this.registerSetting(this.delaySetting);
      this.registerSetting(this.hotbarSetting);
      this.registerSetting(this.totemSlotSetting);
      this.registerSetting(this.forceTotemSetting);
      this.registerSetting(this.autoOpenSetting);
      this.registerSetting(this.closeDelaySetting);
   }

   @Override
   public void onEnable() {
      this.state = 0;
      this.stateTicks = 0;
      this.wasHoldingTotem = true;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.state = 0;
      this.stateTicks = 0;
      super.onDisable();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.interactionManager != null) {
         PlayerInventory minecraftClientValue = minecraftClient.player.getInventory();
         int minecraftClientValue2 = minecraftClient.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING ? 1 : 0;
         if ((Boolean)this.autoOpenSetting.getValue()) {
            this.handleAutoOpen(minecraftClientValue, minecraftClientValue2 != 0);
            this.wasHoldingTotem = minecraftClientValue2 != 0;
         } else {
            this.wasHoldingTotem = minecraftClientValue2 != 0;
            if (!(minecraftClient.currentScreen instanceof InventoryScreen)) {
               this.stateTicks = 0;
            } else if (this.stateTicks < ((Float)this.delaySetting.getValue()).intValue() + this.randomExtraDelay()) {
               this.stateTicks++;
            } else if (minecraftClientValue2 == 0 && this.swapTotemFromInventory(minecraftClientValue)) {
               this.stateTicks = 0;
            } else {
               if ((Boolean)this.hotbarSetting.getValue()) {
                  this.swapTotemToHotbarSlot(minecraftClientValue);
               }

               this.stateTicks = 0;
            }
         }
      }
   }

   public void handleAutoOpen(PlayerInventory arg, boolean flag) {
      switch (this.state) {
         case 0:
            if (this.wasHoldingTotem && !flag && this.findTotemSlot(arg) != -1) {
               this.state = 1;
               this.stateTicks = ((Float)this.delaySetting.getValue()).intValue() <= 0 ? 1 + this.random.nextInt(2) : 1 + this.random.nextInt(3);
            }

            if (!flag && this.state == 0 && !(minecraftClient.currentScreen instanceof InventoryScreen) && this.findTotemSlot(arg) != -1) {
               this.state = 1;
               this.stateTicks = ((Float)this.delaySetting.getValue()).intValue() <= 0 ? 1 + this.random.nextInt(2) : 1 + this.random.nextInt(3);
            }
            break;
         case 1:
            if (this.stateTicks > 0) {
               this.stateTicks--;
               return;
            }

            if (!(minecraftClient.currentScreen instanceof InventoryScreen)) {
               minecraftClient.setScreen(new InventoryScreen(minecraftClient.player));
            }

            this.state = 2;
            this.stateTicks = ((Float)this.delaySetting.getValue()).intValue() + this.randomExtraDelay();
            break;
         case 2:
            if (!(minecraftClient.currentScreen instanceof InventoryScreen)) {
               this.state = 0;
               return;
            }

            if (this.stateTicks > 0) {
               this.stateTicks--;
               return;
            }

            boolean falseSnapshot = false;
            if (!flag) {
               falseSnapshot = this.swapTotemFromInventory(arg);
            }

            boolean falseSnapshot2 = false;
            if ((Boolean)this.hotbarSetting.getValue()) {
               falseSnapshot2 = this.swapTotemToHotbarSlot(arg);
            }

            if (!falseSnapshot && !falseSnapshot2 && !flag) {
               this.state = 3;
               this.stateTicks = 1;
            } else {
               this.state = 3;
               this.stateTicks = ((Float)this.closeDelaySetting.getValue()).intValue() + this.randomExtraDelay();
            }
            break;
         case 3:
            if (this.stateTicks > 0) {
               this.stateTicks--;
               return;
            }

            if (minecraftClient.currentScreen instanceof InventoryScreen) {
               minecraftClient.player.closeHandledScreen();
               minecraftClient.setScreen(null);
            }

            this.state = 0;
      }
   }

   public boolean swapTotemFromInventory(PlayerInventory arg) {
      int intVal = this.findTotemSlot(arg);
      if (intVal == -1) {
         return false;
      }

      int intVal2 = toContainerSlot(intVal);
      minecraftClient.interactionManager.clickSlot(minecraftClient.player.currentScreenHandler.syncId, intVal2, 40, SlotActionType.SWAP, minecraftClient.player);
      return true;
   }

   public boolean swapTotemToHotbarSlot(PlayerInventory arg) {
      int intVal = ((Float)this.totemSlotSetting.getValue()).intValue() - 1;
      if (arg.getStack(intVal).getItem() == Items.TOTEM_OF_UNDYING) {
         return false;
      }

      if (!arg.getStack(intVal).isEmpty() && !(Boolean)this.forceTotemSetting.getValue()) {
         return false;
      }

      int intVal2 = this.findInvTotemSlot(arg);
      if (intVal2 == -1) {
         return false;
      }

      int intVal3 = toContainerSlot(intVal2);
      minecraftClient.interactionManager.clickSlot(minecraftClient.player.currentScreenHandler.syncId, intVal3, intVal, SlotActionType.SWAP, minecraftClient.player);
      return true;
   }

   public int findTotemSlot(PlayerInventory arg) {

      for (int index = 9; index < 36; index++) {
         if (arg.getStack(index).getItem() == Items.TOTEM_OF_UNDYING) {
            return index;
         }
      }

      for (int index2 = 0; index2 < 9; index2++) {
         if (arg.getStack(index2).getItem() == Items.TOTEM_OF_UNDYING) {
            return index2;
         }
      }

      return -1;
   }

   public int findInvTotemSlot(PlayerInventory arg) {

      for (int index = 9; index < 36; index++) {
         if (arg.getStack(index).getItem() == Items.TOTEM_OF_UNDYING) {
            return index;
         }
      }

      return -1;
   }

   public static int toContainerSlot(int intVal) {
      return intVal < 9 ? 36 + intVal : intVal;
   }

   public int randomExtraDelay() {
      return ((Float)this.delaySetting.getValue()).intValue() <= 0 ? 0 : this.random.nextInt(2);
   }

}
