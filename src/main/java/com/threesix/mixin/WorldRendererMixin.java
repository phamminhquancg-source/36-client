package com.threesix.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.util.memory.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.manager.ConfigManager;
import com.threesix.util.WorldToScreenUtil;
import com.threesix.util.TextStyleUtil;
import com.threesix.module.NoRenderModule;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
   private static final Matrix4f capturedMatrix = new Matrix4f();
   private static boolean hasCapturedMatrix;
   private static float capturedTickDelta = 1.0F;

   @Inject(method = "render", at = @At("HEAD"))
   private void captureRenderState(
      ObjectAllocator arg,
      RenderTickCounter arg2,
      boolean flag,
      Camera arg3,
      Matrix4f matrix4f,
      Matrix4f matrix4f2,
      Matrix4f matrix4f3,
      GpuBufferSlice gpuBufferSlice,
      Vector4f vector4f,
      boolean flag2,
      CallbackInfo callbackInfo
   ) {
      capturedMatrix.set(matrix4f);
      WorldToScreenUtil.modelViewMatrix.set(matrix4f);
      WorldToScreenUtil.viewMatrix.set(matrix4f);
      WorldToScreenUtil.projectionMatrix2.set(matrix4f2);
      TextStyleUtil.setProjection(matrix4f, matrix4f2, arg3.getCameraPos());
      hasCapturedMatrix = true;
      capturedTickDelta = arg2.getTickProgress(false);
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void onRender(CallbackInfo callbackInfo) {
      if (hasCapturedMatrix) {
         MatrixStack local = new MatrixStack();
         local.multiplyPositionMatrix(capturedMatrix);
         ConfigManager.INSTANCE.render(local, capturedTickDelta);
      }
   }

   @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
   private void threesix$skipWeatherPass(FrameGraphBuilder arg, GpuBufferSlice gpuBufferSlice, CallbackInfo callbackInfo) {
      if (NoRenderModule.shouldHideParticles()) {
         callbackInfo.cancel();
      }
   }
}
