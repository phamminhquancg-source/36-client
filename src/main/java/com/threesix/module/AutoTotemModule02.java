package com.threesix.module;

import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AutoTotemModule02 extends ModuleBase {
   public final ClientSetting delaySetting = new ClientSetting("Delay", 1.0F, 0.0F, 5.0F);
   public int cooldownTicks;

   public AutoTotemModule02() {
      super("Auto Totem", ModuleCategory.COMBAT);
      this.registerSetting(this.delaySetting);
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {

      super.onDisable();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null) {
         int intVal = this.getDelayTicks();
         if (minecraftClient.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            this.cooldownTicks = intVal;
         } else if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
         } else {
            int intVal2 = this.findTotemSlot(Items.TOTEM_OF_UNDYING);
            if (intVal2 != -1) {
               minecraftClient.interactionManager
                  .clickSlot(minecraftClient.player.currentScreenHandler.syncId, toAbsoluteSlot(intVal2), 40, SlotActionType.SWAP, minecraftClient.player);
               this.cooldownTicks = intVal;
            }
         }
      }
   }

   public int getDelayTicks() {

      double doubleVal = ((Float)this.delaySetting.getValue()).floatValue();
      return (int)Math.round(doubleVal * 20.0);
   }

   public int findTotemSlot(Item arg) {
      if (minecraftClient.player == null) {
         return -1;
      }

      for (int index = 0; index < 36; index++) {
         if (minecraftClient.player.getInventory().getStack(index).isOf(arg)) {
            return index;
         }
      }

      return -1;
   }

   public static int toAbsoluteSlot(int intVal) {
      return intVal < 9 ? 36 + intVal : intVal;
   }

}
