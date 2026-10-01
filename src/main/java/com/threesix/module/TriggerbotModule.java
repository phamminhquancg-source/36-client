package com.threesix.module;

import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.module.FriendsModule;

public final class TriggerbotModule extends ModuleBase {
   public static final int triggerCooldown = 9;
   public final ClientSetting onlyCritSetting = new ClientSetting("Only Crit", false);
   public final ClientSetting checkShieldSetting = new ClientSetting("Check Shield", false);
   public int cooldownTicks = 0;

   public TriggerbotModule() {
      super("Triggerbot", ModuleCategory.COMBAT);
      this.registerSetting(this.onlyCritSetting);
      this.registerSetting(this.checkShieldSetting);
   }

   @Override
   public void onEnable() {
      this.cooldownTicks = 0;
   }

   @Override
   public void onDisable() {
      this.cooldownTicks = 0;
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.currentScreen == null) {
         if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
         } else if (minecraftClient.crosshairTarget != null
            && minecraftClient.crosshairTarget.getType() == Type.ENTITY
            && minecraftClient.crosshairTarget instanceof EntityHitResult local) {
            Entity var1Value = local.getEntity();
            if (var1Value instanceof LivingEntity && var1Value != minecraftClient.player) {
               if (var1Value instanceof PlayerEntity local2 && FriendsModule.isAntiTriggerbot() && FriendsModule.isFriend(local2.getName().getString())) {
                  return;
               }

               if (!minecraftClient.options.attackKey.isPressed() && this.shouldTrigger((LivingEntity)var1Value)) {
                  this.attackTarget(var1Value);
                  this.cooldownTicks = 9;
               }
            }
         }
      }
   }

   public boolean shouldTrigger(LivingEntity arg) {
      return (Boolean)this.onlyCritSetting.getValue() && !this.isReadyToCrit(minecraftClient.player) ? false : !(Boolean)this.checkShieldSetting.getValue() || !this.isHoldingShield(arg);
   }

   public boolean isHoldingShield(LivingEntity arg) {
      ItemStack var1Value = arg.getMainHandStack();
      ItemStack var1Value2 = arg.getOffHandStack();
      return var1Value.getItem() == Items.SHIELD || var1Value2.getItem() == Items.SHIELD;
   }

   public boolean isReadyToCrit(PlayerEntity arg) {
      if (arg.fallDistance <= 0.05F) {
         return false;
      } else if (arg.isOnGround()) {
         return false;
      } else {
         return !arg.isTouchingWater() && !arg.isInLava() && !arg.isClimbing() && !arg.hasVehicle() ? !arg.isSprinting() : false;
      }
   }

   public void attackTarget(Entity arg) {
      minecraftClient.interactionManager.attackEntity(minecraftClient.player, arg);
      minecraftClient.player.swingHand(Hand.MAIN_HAND);
   }

}
