package com.threesix.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.joml.Matrix4f;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.module.ClickGuiModule;
import com.threesix.render.GpuBlurRenderer;
import com.threesix.util.StringVaultDecoder;

public class ScreenBlurPipeline {
   private static long lastFrameTime = -1L;
   private static float lastBlurStrength = 0.3F;
   public static final int[] mipLevelTable = new int[]{1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5};
   public static final float[] blurRadiusTable = new float[]{
      1.25F, 2.25F, 2.0F, 3.0F, 4.25F, 2.5F, 3.25F, 4.25F, 5.5F, 3.25F, 4.0F, 5.0F, 6.0F, 7.25F, 8.25F, 4.5F, 5.25F, 6.25F, 7.25F, 8.5F
   };
   public static boolean isBlurActive;
   public static long blurStateTime = -1L;
   public static final int mipLevelCount = 6;
   public static GpuTexture[] mipTextures = new GpuTexture[6];
   public static GpuTextureView[] mipViews = new GpuTextureView[6];
   public static int[] mipWidths = new int[6];
   public static int[] mipHeights = new int[6];
   public static int lastWidth = 0;
   public static int lastHeight = 0;

   public static boolean isBlurEnabled() {
      try {
         return ClickGuiModule.INSTANCE2 != null && (Boolean)ClickGuiModule.INSTANCE2.blurSetting.getValue();
      } catch (Throwable error) {
         return true;
      }
   }

   public static boolean shouldBlurScreen(Screen arg) {
      try {
         if (!isBlurEnabled() || arg == null) {
            return false;
         } else if (arg instanceof ClickGuiScreen) {
            return true;
         } else if (arg instanceof HandledScreen) {
            return true;
         } else {
            return arg instanceof ChatScreen ? false : false;
         }
      } catch (Throwable error) {
         return false;
      }
   }

   public static void call1(DrawContext arg, Screen arg2) {

      boolean flag = shouldBlurScreen(arg2);
      long systemValue = System.currentTimeMillis();
      byte byteVal = 100;
      if (isBlurActive) {
         if (!flag) {
            if (blurStateTime == -1L) {
               blurStateTime = systemValue + byteVal;
            }

            if (systemValue >= blurStateTime) {
               isBlurActive = false;
               blurStateTime = -1L;
            }
         }
      } else if (flag) {
         isBlurActive = true;
         blurStateTime = systemValue + byteVal;
      }

      if (isBlurActive) {
         double local = 1.0;
         if (systemValue >= blurStateTime || byteVal <= 0) {
            blurStateTime = -1L;
         } else if (flag) {
            local = 1.0 - (double)(blurStateTime - systemValue) / byteVal;
         } else {
            local = (double)(blurStateTime - systemValue) / byteVal;
         }

         if (!(local <= 0.0)) {
            byte byteVal2 = 5;
            int maxValue = Math.max(1, Math.min(20, 1 + (int)Math.round((byteVal2 - 1) * local)));

            try {
               GpuTextureView local2 = buildBlurChain(blurRadiusTable[maxValue - 1], mipLevelTable[maxValue - 1]);
               if (local2 == null) {
                  return;
               }

               compositeBlurTexture(GuiRenderUtil.createProjectionMatrix(), local2, GuiRenderUtil.getScaledWidth(), GuiRenderUtil.getScaledHeight());
            } catch (Throwable error) {
            }
         }
      }
   }

   public static void ensureMipTextures(int intVal, int intVal2) {
      if (intVal != lastWidth || intVal2 != lastHeight || mipViews[0] == null) {
         for (int index = 0; index < 6; index++) {
            try {
               if (mipViews[index] != null) {
                  mipViews[index].close();
                  mipViews[index] = null;
               }
            } catch (Throwable error) {
            }

            try {
               if (mipTextures[index] != null) {
                  mipTextures[index].close();
                  mipTextures[index] = null;
               }
            } catch (Throwable error2) {
            }
         }

         for (int index2 = 0; index2 < 6; index2++) {
            int maxValue = Math.max(1, intVal >> index2 + 1);
            int maxValue2 = Math.max(1, intVal2 >> index2 + 1);
            mipWidths[index2] = maxValue;
            mipHeights[index2] = maxValue2;
            int var8Snapshot = index2;
            mipTextures[index2] = RenderSystem.getDevice().createTexture(() -> "threesix:blur_mip_" + var8Snapshot, 13, TextureFormat.RGBA8, maxValue, maxValue2, 1, 1);
            mipViews[index2] = RenderSystem.getDevice().createTextureView(mipTextures[index2]);
         }

         lastWidth = intVal;
         lastHeight = intVal2;
      }
   }

   public static void blurMipPass(
      GpuTextureView gpuTextureView, GpuTextureView gpuTextureView2, int intVal, int intVal2, int intVal3, int intVal4, float floatVal, CommandEncoder commandEncoder, GpuSampler arg, GpuBufferSlice gpuBufferSlice
   ) {

      GpuBlurRenderer.writeBlurUniforms(intVal, intVal2, intVal3, intVal4, 1.0F, floatVal);
      commandEncoder.writeToBuffer(GpuBlurRenderer.uniformBuffer.slice(), GpuBlurRenderer.uniformData);
      RenderPass commandEncoderValue = commandEncoder.createRenderPass(() -> {
         return "threesix:blur_mip";
      }, gpuTextureView, OptionalInt.empty(), null, OptionalDouble.empty());

      try {
         commandEncoderValue.setPipeline(GpuBlurRenderer.blurPipeline);
         commandEncoderValue.setVertexBuffer(0, GpuBlurRenderer.vertexBuffer);
         commandEncoderValue.bindTexture("Sampler0", gpuTextureView2, arg);
         RenderSystem.bindDefaultUniforms(commandEncoderValue);
         commandEncoderValue.setUniform("DynamicTransforms", gpuBufferSlice);
         commandEncoderValue.setUniform("BlurData", GpuBlurRenderer.uniformBuffer);
         commandEncoderValue.draw(0, 6);
      } catch (Throwable error) {
         if (commandEncoderValue != null) {
            try {
               commandEncoderValue.close();
            } catch (Throwable error2) {
               error.addSuppressed(error2);
            }
         }

         throw error;
      }

      if (commandEncoderValue != null) {
         commandEncoderValue.close();
      }
   }

   public static GpuTextureView buildBlurChain(float floatVal, int intVal) {

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.getFramebuffer() != null && mc.getFramebuffer().getColorAttachment() != null) {
         try {
            GpuBlurRenderer.ensureGpuResources();
            intVal = Math.max(1, Math.min(5, intVal));
            int var2Value = mc.getFramebuffer().textureWidth;
            int var2Value2 = mc.getFramebuffer().textureHeight;
            GpuBlurRenderer.ensureTargets(var2Value, var2Value2);
            ensureMipTextures(var2Value, var2Value2);
            if (mipViews[0] == null) {
               return null;
            }

            GpuSampler renderSystemValue = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
            GpuBufferSlice renderSystemValue3 = RenderSystem.getDynamicUniforms()
               .write(RenderSystem.getModelViewMatrix(), GpuBlurRenderer.screenTintVec4, GpuBlurRenderer.fogColorVec3, GpuBlurRenderer.textMatrixScratch);
            CommandEncoder renderSystemValue2 = RenderSystem.getDevice().createCommandEncoder();
            renderSystemValue2.copyTextureToTexture(mc.getFramebuffer().getColorAttachment(), GpuBlurRenderer.sceneTexture, 0, 0, 0, 0, 0, var2Value, var2Value2);
            blurMipPass(mipViews[0], GpuBlurRenderer.sceneView, var2Value, var2Value2, mipWidths[0], mipHeights[0], floatVal, renderSystemValue2, renderSystemValue, renderSystemValue3);

            for (int index = 0; index < intVal; index++) {
               blurMipPass(
                  mipViews[index + 1],
                  mipViews[index],
                  mipWidths[index],
                  mipHeights[index],
                  mipWidths[index + 1],
                  mipHeights[index + 1],
                  floatVal,
                  renderSystemValue2,
                  renderSystemValue,
                  renderSystemValue3
               );
            }

            for (int index2 = intVal; index2 >= 1; index2--) {
               blurMipPass(
                  mipViews[index2 - 1],
                  mipViews[index2],
                  mipWidths[index2],
                  mipHeights[index2],
                  mipWidths[index2 - 1],
                  mipHeights[index2 - 1],
                  floatVal,
                  renderSystemValue2,
                  renderSystemValue,
                  renderSystemValue3
               );
            }

            return mipViews[0];
         } catch (Throwable error) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void compositeBlurTexture(Matrix4f matrix4f, GpuTextureView gpuTextureView, int intVal, int intVal2) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.getFramebuffer() != null && gpuTextureView != null) {
         try {
            GpuBlurRenderer.ensureGpuResources();
            float[] local = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
            GpuBlurRenderer.writeCompositeUniforms(matrix4f, 0.0F, 0.0F, intVal, intVal2, intVal, intVal2, local, 0.0F);
            GpuSampler renderSystemValue = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
            GpuBufferSlice renderSystemValue3 = RenderSystem.getDynamicUniforms()
               .write(RenderSystem.getModelViewMatrix(), GpuBlurRenderer.screenTintVec4, GpuBlurRenderer.fogColorVec3, GpuBlurRenderer.textMatrixScratch);
            CommandEncoder renderSystemValue2 = RenderSystem.getDevice().createCommandEncoder();
            renderSystemValue2.writeToBuffer(GpuBlurRenderer.uniformBuffer.slice(), GpuBlurRenderer.uniformData);
            RenderPass renderSystemValue2Value = renderSystemValue2.createRenderPass(() -> {
               return "threesix:blur_composite";
            }, mc.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), mc.getFramebuffer().getDepthAttachmentView(), OptionalDouble.of(1.0));

            try {
               GuiRenderUtil.applyScissorToPass(renderSystemValue2Value);
               renderSystemValue2Value.setPipeline(GpuBlurRenderer.compositePipeline);
               renderSystemValue2Value.setVertexBuffer(0, GpuBlurRenderer.vertexBuffer);
               renderSystemValue2Value.bindTexture("Sampler0", gpuTextureView, renderSystemValue);
               RenderSystem.bindDefaultUniforms(renderSystemValue2Value);
               renderSystemValue2Value.setUniform("DynamicTransforms", renderSystemValue3);
               renderSystemValue2Value.setUniform("BlurData", GpuBlurRenderer.uniformBuffer);
               renderSystemValue2Value.draw(0, 6);
            } catch (Throwable error) {
               if (renderSystemValue2Value != null) {
                  try {
                     renderSystemValue2Value.close();
                  } catch (Throwable error2) {
                     error.addSuppressed(error2);
                  }
               }

               throw error;
            }

            if (renderSystemValue2Value != null) {
               renderSystemValue2Value.close();
            }
         } catch (Throwable error3) {
         }
      }
   }

   public static void renderDefaultBlur() {

      renderGlobalBlur(0.3F, 2);
   }

   public static void renderGlobalBlur(float floatVal, int intVal) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.getFramebuffer() != null && mc.getFramebuffer().getColorAttachment() != null) {
         try {
            floatVal = Math.max(0.1F, Math.min(1.0F, floatVal));
            intVal = Math.max(2, Math.min(6, intVal));
            GpuBlurRenderer.ensureGpuResources();
            int var2Value = mc.getFramebuffer().textureWidth;
            int var2Value2 = mc.getFramebuffer().textureHeight;
            int var32Value = var2Value / 2;
            int var42Value = var2Value2 / 2;
            GpuBlurRenderer.ensureTargets(var2Value, var2Value2);
            long systemValue = System.nanoTime() / 16666666L;
            boolean trueSnapshot = true;
            if (trueSnapshot) {
               GpuSampler renderSystemValue = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
               GpuBufferSlice renderSystemValue3 = RenderSystem.getDynamicUniforms()
                  .write(RenderSystem.getModelViewMatrix(), GpuBlurRenderer.screenTintVec4, GpuBlurRenderer.fogColorVec3, GpuBlurRenderer.textMatrixScratch);
               CommandEncoder renderSystemValue2 = RenderSystem.getDevice().createCommandEncoder();
               renderSystemValue2.copyTextureToTexture(mc.getFramebuffer().getColorAttachment(), GpuBlurRenderer.sceneTexture, 0, 0, 0, 0, 0, var2Value, var2Value2);
               GpuBlurRenderer.writeBlurUniforms(var2Value, var2Value2, var32Value, var42Value, 1.0F, floatVal);
               renderSystemValue2.writeToBuffer(GpuBlurRenderer.uniformBuffer.slice(), GpuBlurRenderer.uniformData);
               RenderPass renderSystemValue2Value = renderSystemValue2.createRenderPass(() -> {

                  return "threesix:global_downsample";
               }, GpuBlurRenderer.pingPongViews[0], OptionalInt.empty(), null, OptionalDouble.empty());

               try {
                  renderSystemValue2Value.setPipeline(GpuBlurRenderer.blurPipeline);
                  renderSystemValue2Value.setVertexBuffer(0, GpuBlurRenderer.vertexBuffer);
                  renderSystemValue2Value.bindTexture("Sampler0", GpuBlurRenderer.sceneView, renderSystemValue);
                  RenderSystem.bindDefaultUniforms(renderSystemValue2Value);
                  renderSystemValue2Value.setUniform("DynamicTransforms", renderSystemValue3);
                  renderSystemValue2Value.setUniform("BlurData", GpuBlurRenderer.uniformBuffer);
                  renderSystemValue2Value.draw(0, 6);
               } catch (Throwable error) {
                  if (renderSystemValue2Value != null) {
                     try {
                        renderSystemValue2Value.close();
                     } catch (Throwable error2) {
                        error.addSuppressed(error2);
                     }
                  }

                  throw error;
               }

               if (renderSystemValue2Value != null) {
                  renderSystemValue2Value.close();
               }

               for (int index = 0; index < intVal; index++) {
                  int intVal2 = index % 2;
                  int intVal3 = (index + 1) % 2;
                  float floatVal2 = 1.0F + floatVal * 2.0F + index * 0.5F;
                  int var28Snapshot = index;
                  GpuBlurRenderer.writeBlurUniforms(var32Value, var42Value, var32Value, var42Value, floatVal2, 1.0F);
                  renderSystemValue2.writeToBuffer(GpuBlurRenderer.uniformBuffer.slice(), GpuBlurRenderer.uniformData);
                  RenderPass renderSystemValue2Value2 = renderSystemValue2.createRenderPass(
                     () -> "threesix:global_blur_" + var28Snapshot, GpuBlurRenderer.pingPongViews[intVal3], OptionalInt.empty(), null, OptionalDouble.empty()
                  );

                  try {
                     renderSystemValue2Value2.setPipeline(GpuBlurRenderer.blurPipeline);
                     renderSystemValue2Value2.setVertexBuffer(0, GpuBlurRenderer.vertexBuffer);
                     renderSystemValue2Value2.bindTexture("Sampler0", GpuBlurRenderer.pingPongViews[intVal2], renderSystemValue);
                     RenderSystem.bindDefaultUniforms(renderSystemValue2Value2);
                     renderSystemValue2Value2.setUniform("DynamicTransforms", renderSystemValue3);
                     renderSystemValue2Value2.setUniform("BlurData", GpuBlurRenderer.uniformBuffer);
                     renderSystemValue2Value2.draw(0, 6);
                  } catch (Throwable error3) {
                     if (renderSystemValue2Value2 != null) {
                        try {
                           renderSystemValue2Value2.close();
                        } catch (Throwable error4) {
                           error3.addSuppressed(error4);
                        }
                     }

                     throw error3;
                  }

                  if (renderSystemValue2Value2 != null) {
                     renderSystemValue2Value2.close();
                  }
               }

               GpuBlurRenderer.pingPongIndex = intVal % 2;
               GpuBlurRenderer.lastPassNanos = systemValue;
               GpuBlurRenderer.lastRadius = floatVal;
               lastFrameTime = systemValue;
               lastBlurStrength = floatVal;
            }
         } catch (Throwable error5) {
         }
      }
   }

   public static void compositeGlobalBlur(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.getFramebuffer() != null) {
         try {
            GpuBlurRenderer.ensureGpuResources();
            float[] local = new float[]{floatVal5, floatVal5, floatVal5, floatVal5};
            int guiRenderUtilValue = GuiRenderUtil.getScaledWidth();
            int guiRenderUtilValue2 = GuiRenderUtil.getScaledHeight();
            GpuBlurRenderer.writeCompositeUniforms(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, guiRenderUtilValue, guiRenderUtilValue2, local, 0.0F);
            GpuSampler renderSystemValue = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
            GpuBufferSlice renderSystemValue3 = RenderSystem.getDynamicUniforms()
               .write(RenderSystem.getModelViewMatrix(), GpuBlurRenderer.screenTintVec4, GpuBlurRenderer.fogColorVec3, GpuBlurRenderer.textMatrixScratch);
            CommandEncoder renderSystemValue2 = RenderSystem.getDevice().createCommandEncoder();
            renderSystemValue2.writeToBuffer(GpuBlurRenderer.uniformBuffer.slice(), GpuBlurRenderer.uniformData);
            RenderPass renderSystemValue2Value = renderSystemValue2.createRenderPass(() -> {
               return "threesix:global_final";
            }, mc.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), mc.getFramebuffer().getDepthAttachmentView(), OptionalDouble.of(1.0));

            try {
               GuiRenderUtil.applyScissorToPass(renderSystemValue2Value);
               renderSystemValue2Value.setPipeline(GpuBlurRenderer.compositePipeline);
               renderSystemValue2Value.setVertexBuffer(0, GpuBlurRenderer.vertexBuffer);
               GpuTextureView gpuBlurRendererValue = GpuBlurRenderer.pingPongViews[GpuBlurRenderer.pingPongIndex];
               if (gpuBlurRendererValue == null) {
                  gpuBlurRendererValue = GpuBlurRenderer.sceneView;
               }

               renderSystemValue2Value.bindTexture("Sampler0", gpuBlurRendererValue, renderSystemValue);
               RenderSystem.bindDefaultUniforms(renderSystemValue2Value);
               renderSystemValue2Value.setUniform("DynamicTransforms", renderSystemValue3);
               renderSystemValue2Value.setUniform("BlurData", GpuBlurRenderer.uniformBuffer);
               renderSystemValue2Value.draw(0, 6);
            } catch (Throwable error) {
               if (renderSystemValue2Value != null) {
                  try {
                     renderSystemValue2Value.close();
                  } catch (Throwable error2) {
                     error.addSuppressed(error2);
                  }
               }

               throw error;
            }

            if (renderSystemValue2Value != null) {
               renderSystemValue2Value.close();
            }
         } catch (Throwable error3) {
         }
      }
   }

}
