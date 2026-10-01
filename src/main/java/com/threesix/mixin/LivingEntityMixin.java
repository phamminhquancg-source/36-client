package com.threesix.mixin;

import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.SwingSpeedModule;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
   @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
   private void onGetHandSwingDuration(CallbackInfoReturnable callbackInfoReturnable) {
      SwingSpeedModule swingSpeedModuleValue = SwingSpeedModule.instance;
      if (swingSpeedModuleValue != null && swingSpeedModuleValue.isEnabled()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.player != null && ((Object)this) == mc.player) {
            LivingEntity local = (LivingEntity)(Object)this;
            ItemStack var4Value = local.getStackInHand(Hand.MAIN_HAND);
            int var5Value = var4Value.getSwingAnimation().duration();
            if (StatusEffectUtil.hasHaste(local)) {
               var5Value -= 1 + StatusEffectUtil.getHasteAmplifier(local);
            } else if (local.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
               var5Value += (1 + local.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2;
            }

            float floatVal = swingSpeedModuleValue.getSwingSpeedDivisor();
            int maxValue = Math.max(1, Math.round(var5Value / floatVal));
            callbackInfoReturnable.setReturnValue(maxValue);
         }
      }
   }
}
