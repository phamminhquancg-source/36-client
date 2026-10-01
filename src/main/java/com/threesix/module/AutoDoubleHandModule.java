package com.threesix.module;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AutoDoubleHandModule extends ModuleBase {
   public final ClientSetting onTotemPopSetting = new ClientSetting("On Totem Pop", true);
   public final ClientSetting onHealthSetting = new ClientSetting("On Health", true);
   public final ClientSetting healthThresholdSetting = new ClientSetting("Health Threshold", 6.0F, 1.0F, 20.0F);
   public final ClientSetting cooldownSetting = new ClientSetting("Cooldown", 5.0F, 0.0F, 40.0F);
   public boolean wasHoldingTotem = false;
   public int cooldownTicks = 0;
   public int previousSlot = -1;

   public AutoDoubleHandModule() {
      super("AutoDoubleHand", ModuleCategory.COMBAT);
      this.registerSetting(this.onTotemPopSetting);
      this.registerSetting(this.onHealthSetting);
      this.registerSetting(this.healthThresholdSetting);
      this.registerSetting(this.cooldownSetting);
   }

   @Override
   public void onEnable() {

      this.wasHoldingTotem = false;
      this.cooldownTicks = 0;
      this.previousSlot = -1;
   }

   @Override
   public void onDisable() {
      this.wasHoldingTotem = false;
      this.cooldownTicks = 0;
      this.previousSlot = -1;
   }

   @Override
   public void onTick() {

      if (minecraftClient.player != null && minecraftClient.interactionManager != null) {
         if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
         }

         PlayerInventory minecraftClientValue = minecraftClient.player.getInventory();
         boolean minecraftClientValue2 = minecraftClient.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING;
         boolean minecraftClientValue3 = minecraftClient.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
         boolean flag = minecraftClientValue2 || minecraftClientValue3;
         boolean flag2 = this.wasHoldingTotem && !flag;
         this.wasHoldingTotem = flag;
         if (this.cooldownTicks <= 0) {
            boolean falseSnapshot = false;
            if ((Boolean)this.onTotemPopSetting.getValue() && flag2) {
               falseSnapshot = true;
            }

            if ((Boolean)this.onHealthSetting.getValue() && minecraftClient.player.getHealth() <= (Float)this.healthThresholdSetting.getValue()) {
               falseSnapshot = true;
            }

            if (falseSnapshot && !minecraftClientValue2) {
               int intVal = this.findTotemSlot();
               if (intVal >= 0 && minecraftClientValue.getSelectedSlot() != intVal) {
                  this.previousSlot = minecraftClientValue.getSelectedSlot();
                  minecraftClientValue.setSelectedSlot(intVal);
                  this.cooldownTicks = ((Float)this.cooldownSetting.getValue()).intValue();
               }
            }
         }
      }
   }

   public int findTotemSlot() {
      PlayerInventory minecraftClientValue = minecraftClient.player.getInventory();

      for (int index = 0; index < 9; index++) {
         if (minecraftClientValue.getStack(index).isOf(Items.TOTEM_OF_UNDYING)) {
            return index;
         }
      }

      return -1;
   }

}
