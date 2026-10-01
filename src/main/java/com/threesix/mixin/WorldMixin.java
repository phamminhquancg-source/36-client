package com.threesix.mixin;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.NoRenderModule;

@Mixin(World.class)
public class WorldMixin {
   @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
   private void threesix$hideRainGradient(float floatVal, CallbackInfoReturnable callbackInfoReturnable) {
      if (NoRenderModule.isRainHidden()) {
         callbackInfoReturnable.setReturnValue(0.0F);
      }
   }

   @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
   private void threesix$hideThunderGradient(float floatVal, CallbackInfoReturnable callbackInfoReturnable) {
      if (NoRenderModule.isThunderActive()) {
         callbackInfoReturnable.setReturnValue(0.0F);
      }
   }

   @Inject(method = "playSoundClient(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V", at = @At("HEAD"), cancellable = true)
   private void threesix$cancelWeatherPointSound(
      double doubleVal, double doubleVal2, double doubleVal3, SoundEvent arg, SoundCategory arg2, float floatVal, float floatVal2, boolean flag, CallbackInfo callbackInfo
   ) {
      if (NoRenderModule.shouldCancelWeatherSound(arg)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "playSoundAtBlockCenterClient(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V", at = @At("HEAD"), cancellable = true)
   private void threesix$cancelWeatherBlockSound(BlockPos arg, SoundEvent arg2, SoundCategory arg3, float floatVal, float floatVal2, boolean flag, CallbackInfo callbackInfo) {
      if (NoRenderModule.shouldCancelWeatherSound(arg2)) {
         callbackInfo.cancel();
      }
   }
}
