package com.threesix.mixin;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.NoRenderModule;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
   @Inject(method = "getLightningTicksLeft", at = @At("HEAD"), cancellable = true)
   private void threesix$hideLightningFlash(CallbackInfoReturnable callbackInfoReturnable) {
      if (NoRenderModule.isThunderActive()) {
         callbackInfoReturnable.setReturnValue(0);
      }
   }
}
