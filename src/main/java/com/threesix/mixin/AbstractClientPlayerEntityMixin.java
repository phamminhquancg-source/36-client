package com.threesix.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.SkinChangerModule;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
   @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
   private void threesix$overrideOwnSkin(CallbackInfoReturnable callbackInfoReturnable) {
      AbstractClientPlayerEntity local = (AbstractClientPlayerEntity)(Object)this;
      SkinTextures skinChangerModuleValue = SkinChangerModule.getOverrideSkin(local.getUuid());
      if (skinChangerModuleValue != null) {
         callbackInfoReturnable.setReturnValue(skinChangerModuleValue);
      }
   }
}
