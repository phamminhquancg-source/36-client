package com.threesix.mixin;

import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.manager.MutedSoundRegistry;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
   @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
   private void threesix$renderPlayerNametag(PlayerEntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4, CallbackInfo callbackInfo) {
      if (MutedSoundRegistry.isMuted(arg)) {
         callbackInfo.cancel();
      }
   }
}
