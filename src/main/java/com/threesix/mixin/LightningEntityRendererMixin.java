package com.threesix.mixin;

import net.minecraft.client.render.entity.state.LightningEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.NoRenderModule;

@Mixin(LightningEntityRenderer.class)
public class LightningEntityRendererMixin {
   @Inject(
      method = "render(Lnet/minecraft/client/render/entity/state/LightningEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void threesix$hideLightningEntity(LightningEntityRenderState local, MatrixStack local2, OrderedRenderCommandQueue local3, CameraRenderState local4, CallbackInfo local5) {
      if (NoRenderModule.isThunderActive()) {
         local5.cancel();
      }
   }
}
