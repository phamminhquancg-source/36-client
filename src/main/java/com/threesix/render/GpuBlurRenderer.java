package com.threesix.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;
import com.threesix.util.StringVaultDecoder;

public class GpuBlurRenderer {
   public static final int maxPasses = 5;
   public static final int passCount = 2;
   public static final int uniformBufferSize = 256;
   public static final RenderPipeline blurPipeline = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation(Identifier.of("threesix", "pipeline/blur_pass"))
         .withVertexShader(Identifier.of("threesix", "blur_pass_vertex"))
         .withFragmentShader(Identifier.of("threesix", "blur_pass_fragment"))
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   public static final RenderPipeline compositePipeline = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation(Identifier.of("threesix", "pipeline/blur_final"))
         .withVertexShader(Identifier.of("threesix", "blur_final_vertex"))
         .withFragmentShader(Identifier.of("threesix", "blur_final_fragment"))
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   public static final Vector4f screenTintVec4 = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   public static final Vector3f fogColorVec3 = new Vector3f(0.0F, 0.0F, 0.0F);
   public static final Matrix4f textMatrixScratch = new Matrix4f();
   public static GpuBuffer uniformBuffer;
   public static GpuBuffer vertexBuffer;
   public static ByteBuffer uniformData;
   public static GpuTexture sceneTexture;
   public static GpuTextureView sceneView;
   public static GpuTexture[] pingPongTextures = new GpuTexture[2];
   public static GpuTextureView[] pingPongViews = new GpuTextureView[2];
   public static int width = 0;
   public static int height = 0;
   public static boolean isInitialized = false;
   public static long lastPassNanos = -1L;
   public static int pingPongIndex = 0;
   public static float lastRadius = 0.0F;

   public static void ensureGpuResources() {

      if (!isInitialized) {
         uniformData = MemoryUtil.memAlloc(256);
         ByteBuffer memoryUtilValue = MemoryUtil.memAlloc(4);
         memoryUtilValue.putInt(0);
         memoryUtilValue.flip();
         vertexBuffer = RenderSystem.getDevice().createBuffer(() -> {
            return "threesix:blur_dummy_vertex";
         }, 32, memoryUtilValue);
         MemoryUtil.memFree(memoryUtilValue);
         isInitialized = true;
      }
   }

   public static void captureScreen() {
      MinecraftClient mc = MinecraftClient.getInstance();
      int var0Value = mc.getFramebuffer().textureWidth;
      int var0Value2 = mc.getFramebuffer().textureHeight;
      ensureTargets(var0Value, var0Value2);
      CommandEncoder renderSystemValue = RenderSystem.getDevice().createCommandEncoder();
      renderSystemValue.copyTextureToTexture(mc.getFramebuffer().getColorAttachment(), sceneTexture, 0, 0, 0, 0, 0, var0Value, var0Value2);
   }

   public static void ensureTargets(int intVal, int intVal2) {

      int var02Value = intVal / 2;
      int var12Value = intVal2 / 2;
      if (sceneTexture == null || intVal != width || intVal2 != height) {
         if (sceneView != null) {
            sceneView.close();
            sceneView = null;
         }

         if (sceneTexture != null) {
            sceneTexture.close();
            sceneTexture = null;
         }

         sceneTexture = RenderSystem.getDevice().createTexture(() -> {
            return "threesix:blur_copy";
         }, 5, TextureFormat.RGBA8, intVal, intVal2, 1, 1);
         sceneView = RenderSystem.getDevice().createTextureView(sceneTexture);

         for (int index = 0; index < 2; index++) {
            if (pingPongViews[index] != null) {
               pingPongViews[index].close();
               pingPongViews[index] = null;
            }

            if (pingPongTextures[index] != null) {
               pingPongTextures[index].close();
               pingPongTextures[index] = null;
            }

            int var4Snapshot = index;
            pingPongTextures[index] = RenderSystem.getDevice().createTexture(() -> "threesix:blur_pp_" + var4Snapshot, 13, TextureFormat.RGBA8, var02Value, var12Value, 1, 1);
            pingPongViews[index] = RenderSystem.getDevice().createTextureView(pingPongTextures[index]);
         }

         width = intVal;
         height = intVal2;
      }
   }

   public static void renderGpuBlur(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7) {

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.getFramebuffer() != null && mc.getFramebuffer().getColorAttachment() != null) {
         ensureGpuResources();
         int var8Value = mc.getFramebuffer().textureWidth;
         int var8Value2 = mc.getFramebuffer().textureHeight;
         int var92Value = var8Value / 2;
         int var102Value = var8Value2 / 2;
         ensureTargets(var8Value, var8Value2);
         long systemValue = System.nanoTime() / 16666666L;
         boolean var13LastPassNanos3LMathVa = systemValue - lastPassNanos >= 3L || Math.abs(floatVal6 - lastRadius) > 0.01F;
         GpuSampler renderSystemValue = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
         GpuBufferSlice renderSystemValue2 = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), screenTintVec4, fogColorVec3, textMatrixScratch);
         CommandEncoder renderSystemValue3 = RenderSystem.getDevice().createCommandEncoder();
         if (var13LastPassNanos3LMathVa) {
            renderSystemValue3.copyTextureToTexture(mc.getFramebuffer().getColorAttachment(), sceneTexture, 0, 0, 0, 0, 0, var8Value, var8Value2);
            writeBlurUniforms(var8Value, var8Value2, var92Value, var102Value, 1.0F, floatVal6);
            renderSystemValue3.writeToBuffer(uniformBuffer.slice(), uniformData);
            RenderPass renderSystemValue3Value = renderSystemValue3.createRenderPass(() -> {
               return "threesix:blur_downsample";
            }, pingPongViews[0], OptionalInt.empty(), null, OptionalDouble.empty());

            try {
               renderSystemValue3Value.setPipeline(blurPipeline);
               renderSystemValue3Value.setVertexBuffer(0, vertexBuffer);
               renderSystemValue3Value.bindTexture("Sampler0", sceneView, renderSystemValue);
               RenderSystem.bindDefaultUniforms(renderSystemValue3Value);
               renderSystemValue3Value.setUniform("DynamicTransforms", renderSystemValue2);
               renderSystemValue3Value.setUniform("BlurData", uniformBuffer);
               renderSystemValue3Value.draw(0, 6);
            } catch (Throwable error) {
               if (renderSystemValue3Value != null) {
                  try {
                     renderSystemValue3Value.close();
                  } catch (Throwable error2) {
                     error.addSuppressed(error2);
                  }
               }

               throw error;
            }

            if (renderSystemValue3Value != null) {
               renderSystemValue3Value.close();
            }

            int maxValue = Math.max(2, (int)(5.0F * floatVal6));
            float[] local = new float[]{1.0F, 2.0F, 2.0F, 3.0F};

            for (int index = 0; index < maxValue; index++) {
               int intVal = index % 2;
               int intVal2 = (index + 1) % 2;
               float floatVal8 = index < local.length ? local[index] : 3.0F;
               int var22Snapshot = index;
               writeBlurUniforms(var92Value, var102Value, var92Value, var102Value, floatVal8, 1.0F);
               renderSystemValue3.writeToBuffer(uniformBuffer.slice(), uniformData);
               RenderPass local2 = renderSystemValue3.createRenderPass(() -> "threesix:blur_" + var22Snapshot, pingPongViews[intVal2], OptionalInt.empty(), null, OptionalDouble.empty());

               try {
                  local2.setPipeline(blurPipeline);
                  local2.setVertexBuffer(0, vertexBuffer);
                  local2.bindTexture("Sampler0", pingPongViews[intVal], renderSystemValue);
                  RenderSystem.bindDefaultUniforms(local2);
                  local2.setUniform("DynamicTransforms", renderSystemValue2);
                  local2.setUniform("BlurData", uniformBuffer);
                  local2.draw(0, 6);
               } catch (Throwable error3) {
                  if (local2 != null) {
                     try {
                        local2.close();
                     } catch (Throwable error4) {
                        error3.addSuppressed(error4);
                     }
                  }

                  throw error3;
               }

               if (local2 != null) {
                  local2.close();
               }
            }

            pingPongIndex = maxValue % 2;
            lastPassNanos = systemValue;
            lastRadius = floatVal6;
         }

         float[] local3 = new float[]{floatVal5, floatVal5, floatVal5, floatVal5};
         int guiRenderUtilValue = GuiRenderUtil.getScaledWidth();
         int guiRenderUtilValue2 = GuiRenderUtil.getScaledHeight();
         writeCompositeUniforms(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, guiRenderUtilValue, guiRenderUtilValue2, local3, floatVal7);
         renderSystemValue3.writeToBuffer(uniformBuffer.slice(), uniformData);
         RenderPass renderSystemValue3Value2 = renderSystemValue3.createRenderPass(() -> {

            return "threesix:blur_final";
         }, mc.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), mc.getFramebuffer().getDepthAttachmentView(), OptionalDouble.of(1.0));

         try {
            GuiRenderUtil.applyScissorToPass(renderSystemValue3Value2);
            renderSystemValue3Value2.setPipeline(compositePipeline);
            renderSystemValue3Value2.setVertexBuffer(0, vertexBuffer);
            renderSystemValue3Value2.bindTexture("Sampler0", pingPongViews[pingPongIndex], renderSystemValue);
            RenderSystem.bindDefaultUniforms(renderSystemValue3Value2);
            renderSystemValue3Value2.setUniform("DynamicTransforms", renderSystemValue2);
            renderSystemValue3Value2.setUniform("BlurData", uniformBuffer);
            renderSystemValue3Value2.draw(0, 6);
         } catch (Throwable error5) {
            if (renderSystemValue3Value2 != null) {
               try {
                  renderSystemValue3Value2.close();
               } catch (Throwable error6) {
                  error5.addSuppressed(error6);
               }
            }

            throw error5;
         }

         if (renderSystemValue3Value2 != null) {
            renderSystemValue3Value2.close();
         }
      }
   }

   public static void writeBlurUniforms(int intVal, int intVal2, int intVal3, int intVal4, float floatVal, float floatVal2) {

      uniformData.clear();

      for (int index = 0; index < 16; index++) {
         uniformData.putFloat(0.0F);
      }

      uniformData.putFloat(0.0F).putFloat(0.0F).putFloat(intVal).putFloat(intVal2);
      uniformData.putFloat(intVal).putFloat(intVal2).putFloat(1.0F).putFloat(floatVal);
      uniformData.putFloat(intVal3).putFloat(intVal4).putFloat(1.0F).putFloat(floatVal2);
      uniformData.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
      uniformData.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
      uniformData.flip();
      flushUniformBuffer();
   }

   public static void writeCompositeUniforms(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, int intVal, int intVal2, float[] float2, float floatVal5) {
      uniformData.clear();
      uniformData.putFloat(matrix4f.m00()).putFloat(matrix4f.m01()).putFloat(matrix4f.m02()).putFloat(matrix4f.m03());
      uniformData.putFloat(matrix4f.m10()).putFloat(matrix4f.m11()).putFloat(matrix4f.m12()).putFloat(matrix4f.m13());
      uniformData.putFloat(matrix4f.m20()).putFloat(matrix4f.m21()).putFloat(matrix4f.m22()).putFloat(matrix4f.m23());
      uniformData.putFloat(matrix4f.m30()).putFloat(matrix4f.m31()).putFloat(matrix4f.m32()).putFloat(matrix4f.m33());
      uniformData.putFloat(floatVal).putFloat(floatVal2).putFloat(floatVal3).putFloat(floatVal4);
      uniformData.putFloat(intVal).putFloat(intVal2).putFloat(0.0F).putFloat(0.0F);
      uniformData.putFloat(intVal).putFloat(intVal2).putFloat(1.0F).putFloat(0.0F);
      uniformData.putFloat(float2[0]).putFloat(float2[1]).putFloat(float2[2]).putFloat(float2[3]);
      uniformData.putFloat(floatVal5).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
      uniformData.flip();
      flushUniformBuffer();
   }

   public static void flushUniformBuffer() {
      int uniformDataValue = uniformData.remaining();
      if (uniformBuffer == null || uniformBuffer.size() < uniformDataValue) {
         if (uniformBuffer != null) {
            uniformBuffer.close();
         }

         uniformBuffer = RenderSystem.getDevice().createBuffer(() -> {
            return "threesix:blur_uniform";
         }, 136, uniformDataValue);
      }
   }

   public static GpuTextureView getResultTexture() {
      GpuTextureView local = isInitialized && pingPongViews[pingPongIndex] != null ? pingPongViews[pingPongIndex] : null;
      return local;
   }

   public static void close() {
      if (uniformBuffer != null) {
         uniformBuffer.close();
         uniformBuffer = null;
      }

      if (vertexBuffer != null) {
         vertexBuffer.close();
         vertexBuffer = null;
      }

      if (uniformData != null) {
         MemoryUtil.memFree(uniformData);
         uniformData = null;
      }

      if (sceneView != null) {
         sceneView.close();
         sceneView = null;
      }

      if (sceneTexture != null) {
         sceneTexture.close();
         sceneTexture = null;
      }

      for (int index = 0; index < 2; index++) {
         if (pingPongViews[index] != null) {
            pingPongViews[index].close();
            pingPongViews[index] = null;
         }

         if (pingPongTextures[index] != null) {
            pingPongTextures[index].close();
            pingPongTextures[index] = null;
         }
      }

      isInitialized = false;
   }

}
