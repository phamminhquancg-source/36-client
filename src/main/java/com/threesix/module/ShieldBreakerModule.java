package com.threesix.module;

import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.ClientSetting;

public final class ShieldBreakerModule extends ModuleBase {
   public final ClientSetting switchBackSetting = new ClientSetting("Switch Back", true);
   public final ClientSetting switchDelaySetting = new ClientSetting("Switch Delay", 0.0F, 0.0F, 500.0F);
   public boolean isAttacking = false;
   public long attackStartMillis = -1L;
   public boolean isSwitched = false;
   public boolean shouldSwitchBack = false;
   public int previousSlot = -1;

   public ShieldBreakerModule() {
      super("Shield Breaker", ModuleCategory.COMBAT);
      this.registerSetting(this.switchBackSetting);
      this.registerSetting(this.switchDelaySetting);
   }

   @Override
   public void onEnable() {
      this.resetState();
      super.onEnable();
   }

   @Override
   public void onDisable() {

      if (this.isSwitched && (Boolean)this.switchBackSetting.getValue() && this.shouldSwitchBack && minecraftClient.player != null) {
         this.switchToSlot(this.previousSlot);
      }

      this.resetState();
      super.onDisable();
   }

   public void resetState() {

      this.isAttacking = false;
      this.attackStartMillis = -1L;
      this.isSwitched = false;
      this.shouldSwitchBack = false;
      this.previousSlot = -1;
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         PlayerEntity local = this.getBestTarget();
         if (local != null && local.isBlocking()) {
            if (!this.isAttacking) {
               this.isAttacking = true;
               this.attackStartMillis = System.currentTimeMillis();
            }

            long longVal = ((Float)this.switchDelaySetting.getValue()).longValue();
            if (!this.isSwitched && this.attackStartMillis >= 0L && System.currentTimeMillis() - this.attackStartMillis >= longVal) {
               int intVal = this.findBestAxeSlot();
               if (intVal != -1) {
                  this.previousSlot = minecraftClient.player.getInventory().getSelectedSlot();
                  if (this.previousSlot != intVal) {
                     this.switchToSlot(intVal);
                     this.shouldSwitchBack = true;
                  }

                  minecraftClient.interactionManager.attackEntity(minecraftClient.player, local);
                  minecraftClient.player.swingHand(Hand.MAIN_HAND);
                  this.isSwitched = true;
               }
            }
         } else {
            if (this.isAttacking) {
               this.isAttacking = false;
               this.attackStartMillis = -1L;
            }

            if (this.isSwitched) {
               if ((Boolean)this.switchBackSetting.getValue() && this.shouldSwitchBack && this.previousSlot != -1) {
                  this.switchToSlot(this.previousSlot);
               }

               this.isSwitched = false;
               this.shouldSwitchBack = false;
               this.previousSlot = -1;
            }
         }
      }
   }

   public PlayerEntity getBestTarget() {
      return minecraftClient.crosshairTarget != null
            && minecraftClient.crosshairTarget.getType() == Type.ENTITY
            && ((EntityHitResult)minecraftClient.crosshairTarget).getEntity() instanceof PlayerEntity local
            && local != minecraftClient.player
         ? local
         : null;
   }

   public int findBestAxeSlot() {
      int var5Snapshot = -1;
      int var3Snapshot = -1;
      for (int index = 0; index < 9; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (minecraftClientValue.getItem() instanceof AxeItem) {
            int intVal = this.getAxeCount(minecraftClientValue);
            if (intVal > var5Snapshot) {
               var5Snapshot = intVal;
               var3Snapshot = index;
            }
         }
      }

      return var3Snapshot;
   }

   public int getAxeCount(ItemStack arg) {
      if (arg.isOf(Items.NETHERITE_AXE)) {
         return 6;
      } else if (arg.isOf(Items.DIAMOND_AXE)) {
         return 5;
      } else if (arg.isOf(Items.IRON_AXE)) {
         return 4;
      } else if (arg.isOf(Items.GOLDEN_AXE)) {
         return 3;
      } else if (arg.isOf(Items.STONE_AXE)) {
         return 2;
      } else {
         return arg.isOf(Items.WOODEN_AXE) ? 1 : 0;
      }
   }

   public void switchToSlot(int intVal) {

      if (minecraftClient.player != null && intVal >= 0 && intVal <= 8) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
      }
   }

}
