package com.threesix.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class DepthTestFixMixin {
   @Inject(method = "renderHand", at = @At("HEAD"))
   private void onBeforeRenderHand(float floatVal, boolean flag, Matrix4f matrix4f, CallbackInfo callbackInfo) {
      GL11.glDepthMask(true);
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void onRenderReturn(RenderTickCounter arg, boolean flag, CallbackInfo callbackInfo) {
      GL11.glDepthMask(true);
   }
}
