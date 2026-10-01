package com.threesix.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.FreeLookModule;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
   @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
   private void onRenderFirstPersonItem(CallbackInfo callbackInfo) {
      if (FreeLookModule.freeLookInstance != null && FreeLookModule.freeLookInstance.isThirdPersonActive()) {
         callbackInfo.cancel();
      }
   }
}
