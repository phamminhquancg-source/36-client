package com.threesix.util;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderPass;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.Window;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import com.threesix.render.GpuBlurRenderer;
import com.threesix.render.Rect2DRenderPipeline;
import com.threesix.util.StringVaultDecoder;
import com.threesix.render.ArcOutline2DPipeline;
import com.threesix.render.Arc2DPipeline;
import com.threesix.render.ThreesixTexturePipeline;
import com.threesix.render.OutlineRenderPipelineRegistry;

public class GuiRenderUtil {
   public static final List<Runnable> drawQueue = new ArrayList<>();
   public static final float DEFAULT_Z = 0.0F;
   public static final int BATCH_LIMIT = 1;
   public static boolean isScissorActive = false;
   public static int scissorX;
   public static int scissorY;
   public static int scissorWidth;
   public static int scissorHeight;

   public static int getScaledWidth() {
      Window class310Value = MinecraftClient.getInstance().getWindow();
      return (int)Math.ceil(class310Value.getWidth() / 1.0);
   }

   public static int getScaledHeight() {
      Window class310Value = MinecraftClient.getInstance().getWindow();
      return (int)Math.ceil(class310Value.getHeight() / 1.0);
   }

   public static float getGuiScale() {
      Window class310Value = MinecraftClient.getInstance().getWindow();
      int var0Value = class310Value.getScaleFactor();
      return var0Value / 1.0F;
   }

   public static float scaleWidth(float floatVal) {
      return floatVal * getGuiScale();
   }

   public static float scaleHeight(float floatVal) {
      return floatVal * getGuiScale();
   }

   public static float scaleValue(float floatVal) {
      return floatVal * getGuiScale();
   }

   public static Matrix4f createProjectionMatrix() {
      return new Matrix4f().ortho(0.0F, getScaledWidth(), getScaledHeight(), 0.0F, -1000.0F, 1000.0F);
   }

   public static Matrix4f getContextProjectionMatrix(DrawContext arg) {
      Window class310Value = MinecraftClient.getInstance().getWindow();
      Matrix3x2fStack var0Value = arg.getMatrices();
      return new Matrix4f()
         .ortho(0.0F, class310Value.getScaledWidth(), class310Value.getScaledHeight(), 0.0F, -1000.0F, 1000.0F)
         .mul(new Matrix4f(var0Value.m00, var0Value.m01, 0.0F, 0.0F, var0Value.m10, var0Value.m11, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, var0Value.m20, var0Value.m21, 0.0F, 1.0F));
   }

   public static boolean isInScreenGui() {
      MinecraftClient mc = MinecraftClient.getInstance();
      return mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen);
   }

   public static void fillRoundedRect(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, int intVal, boolean flag) {
      fillRoundedRectMatrix(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, intVal, flag);
   }

   public static void fillGradient(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, boolean flag, int... local) {
      queueRoundedRect(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, flag, local);
   }

   public static void fillPerCornerGradient(
      DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, float floatVal8, boolean flag, int... local
   ) {
      queueRoundedRect(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, flag, local);
   }

   public static void fillRoundedRectMatrix(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, int intVal, boolean flag) {
      queueRoundedRect(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, flag, intVal);
   }

   public static void queueRoundedRect(
      Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, float floatVal8, boolean flag, int... local
   ) {
      if (flag) {
         drawQueue.add(() -> {
            Rect2DRenderPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, 0.0F, local);
         });
      } else {
         Rect2DRenderPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, 0.0F, local);
      }
   }

   public static void fillRoundedCorners(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, boolean flag, int... local) {
      queueRoundedRect(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, flag, local);
   }

   public static void strokeRoundedRect(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, int intVal, boolean flag) {
      strokeRoundedRectRawMatrix(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, floatVal6, intVal, flag);
   }

   public static void strokeRoundedRectRaw(
      DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, float floatVal8, float floatVal9, int intVal, boolean flag
   ) {
      strokeRoundedRectRawMatrix(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, floatVal9, intVal, flag);
   }

   public static void strokeGradient(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, boolean flag, int... local) {
      strokeRoundedRectGradient(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, floatVal6, flag, local);
   }

   public static void strokeGradientRaw(
      DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, float floatVal8, float floatVal9, boolean flag, int... local
   ) {
      strokeRoundedRectGradient(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, floatVal9, flag, local);
   }

   public static void strokeRoundedRectMatrix(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, int intVal, boolean flag) {
      strokeRoundedRectRawMatrix(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, floatVal6, intVal, flag);
   }

   public static void strokeRoundedRectRawMatrix(
      Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, float floatVal8, float floatVal9, int intVal, boolean flag
   ) {
      if (flag) {
         drawQueue.add(() -> {
            OutlineRenderPipelineRegistry.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, floatVal9, 0.0F, intVal);
         });
      } else {
         OutlineRenderPipelineRegistry.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, floatVal9, 0.0F, intVal);
      }
   }

   public static void strokeRoundedRectOutlines(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, boolean flag, int... local) {
      strokeRoundedRectGradient(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, floatVal6, flag, local);
   }

   public static void strokeRoundedRectGradient(
      Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, float floatVal8, float floatVal9, boolean flag, int... local
   ) {
      if (flag) {
         drawQueue.add(() -> {
            OutlineRenderPipelineRegistry.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, floatVal9, 0.0F, local);
         });
      } else {
         OutlineRenderPipelineRegistry.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, floatVal8, floatVal9, 0.0F, local);
      }
   }

   public static void renderBlur(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, boolean flag) {
      Matrix4f local = getContextProjectionMatrix(arg);
      if (flag) {
         drawQueue.add(() -> {
            GpuBlurRenderer.renderGpuBlur(local, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, 0.0F);
         });
      } else {
         GpuBlurRenderer.renderGpuBlur(local, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, 0.0F);
      }
   }

   public static void renderArcRaw(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, int intVal, boolean flag) {
      drawArc(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, intVal, flag);
   }

   public static void renderArcGradient(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, boolean flag, int... local) {
      drawArcGradient(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, flag, local);
   }

   public static void drawArc(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, int intVal, boolean flag) {
      if (flag) {
         drawQueue.add(() -> {
            Arc2DPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, 0.0F, intVal);
         });
      } else {
         Arc2DPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, 0.0F, intVal);
      }
   }

   public static void drawArcGradient(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, boolean flag, int... local) {
      if (flag) {
         drawQueue.add(() -> {
            Arc2DPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, 0.0F, local);
         });
      } else {
         Arc2DPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, 0.0F, local);
      }
   }

   public static void strokeArcRaw(
      DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, int intVal, int intVal2, boolean flag
   ) {
      strokeArc(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, intVal, intVal2, flag);
   }

   public static void strokeArc(
      Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, int intVal, int intVal2, boolean flag
   ) {
      if (flag) {
         drawQueue.add(() -> {
            ArcOutline2DPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, intVal, intVal2, 0.0F);
         });
      } else {
         ArcOutline2DPipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal6, floatVal7, intVal, intVal2, 0.0F);
      }
   }

   public static void drawTexture(DrawContext arg, float floatVal, float floatVal2, float floatVal3, Identifier arg2, int intVal, float floatVal4, boolean flag) {
      drawTextureRaw(getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, arg2, intVal, floatVal4, flag);
   }

   public static void drawTextureRaw(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, Identifier arg, int intVal, float floatVal4, boolean flag) {
      MinecraftClient mc = MinecraftClient.getInstance();
      AbstractTexture var8Value = mc.getTextureManager().getTexture(arg);
      if (var8Value != null) {
         if (flag) {
            drawQueue.add(() -> {
               ThreesixTexturePipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, var8Value.getGlTextureView(), intVal, floatVal4, 0.0F);
            });
            return;
         }

         ThreesixTexturePipeline.call1(matrix4f, floatVal, floatVal2, floatVal3, var8Value.getGlTextureView(), intVal, floatVal4, 0.0F);
      }
   }

   public static void enqueue(Runnable runnable) {
      drawQueue.add(runnable);
   }

   public static void flush(DrawContext arg) {
      if (!drawQueue.isEmpty()) {
         GL11.glDisable(2929);
         drawQueue.forEach(Runnable::run);
         drawQueue.clear();
         GL11.glEnable(2929);
         isScissorActive = false;
         GlStateManager._disableScissorTest();
      }
   }

   public static void clearQueue() {
      drawQueue.clear();
   }

   public static boolean hasPendingDraws() {
      return !drawQueue.isEmpty();
   }

   public static void pushScissor(float floatVal, float floatVal2, float floatVal3, float floatVal4, boolean flag) {
      if (flag) {
         drawQueue.add(() -> {
            applyScissor(floatVal, floatVal2, floatVal3, floatVal4);
         });
      } else {
         applyScissor(floatVal, floatVal2, floatVal3, floatVal4);
      }
   }

   public static void applyScissor(float floatVal, float floatVal2, float floatVal3, float floatVal4) {
      Window class310Value = MinecraftClient.getInstance().getWindow();
      double class310ValueValue = class310Value.getScaleFactor();
      int var4Value = class310Value.getFramebufferWidth();
      int var4Value2 = class310Value.getFramebufferHeight();
      isScissorActive = true;
      scissorX = (int)floatVal;
      scissorY = (int)floatVal2;
      scissorWidth = (int)floatVal3;
      scissorHeight = (int)floatVal4;
      int intVal = (int)Math.round(scissorX * class310ValueValue);
      int intVal2 = (int)Math.round(scissorY * class310ValueValue);
      int intVal3 = (int)Math.round(scissorWidth * class310ValueValue);
      int intVal4 = (int)Math.round(scissorHeight * class310ValueValue);
      int var8Var10Var12Value = var4Value2 - (intVal2 + intVal4);
      int maxValue = Math.max(0, intVal);
      int maxValue2 = Math.max(0, var8Var10Var12Value);
      int minValue = Math.min(var4Value, intVal + intVal3);
      int minValue2 = Math.min(var4Value2, var8Var10Var12Value + intVal4);
      int maxValue3 = Math.max(0, minValue - maxValue);
      int maxValue4 = Math.max(0, minValue2 - maxValue2);
      if (maxValue3 != 0 && maxValue4 != 0) {
         GlStateManager._enableScissorTest();
         GlStateManager._scissorBox(maxValue, maxValue2, maxValue3, maxValue4);
      } else {
         isScissorActive = false;
         GlStateManager._disableScissorTest();
      }
   }

   public static void applyScissorToPass(RenderPass renderPass) {
      if (isScissorActive) {
         renderPass.enableScissor(scissorX, scissorY, scissorX + scissorWidth, scissorY + scissorHeight);
      }
   }

   public static void popScissor(boolean flag) {
      if (flag) {
         drawQueue.add(() -> {
            isScissorActive = false;
            GlStateManager._disableScissorTest();
         });
      } else {
         isScissorActive = false;
         GlStateManager._disableScissorTest();
      }
   }

   public static boolean isGuiScreen() {
      MinecraftClient mc = MinecraftClient.getInstance();
      return mc.currentScreen == null || mc.currentScreen instanceof ChatScreen;
   }

   public static void applyGuiScale(DrawContext arg) {
      Window class310Value = MinecraftClient.getInstance().getWindow();
      double class310ValueValue = class310Value.getScaleFactor();
      arg.getMatrices().scale((float)(1.0 / class310ValueValue), (float)(1.0 / class310ValueValue));
   }

   public static void unapplyGuiScale(DrawContext arg) {
      Window class310Value = MinecraftClient.getInstance().getWindow();
      double class310ValueValue = class310Value.getScaleFactor();
      arg.getMatrices().scale((float)class310ValueValue, (float)class310ValueValue);
   }

   public static int multiplyRgb(int intVal, float floatVal) {
      int intVal2 = intVal & 0xFF000000;
      float floatVal2 = (intVal >> 16 & 0xFF) / 255.0F;
      float floatVal3 = (intVal >> 8 & 0xFF) / 255.0F;
      float floatVal4 = (intVal & 0xFF) / 255.0F;
      floatVal2 = Math.min(floatVal2 * floatVal, 1.0F);
      floatVal3 = Math.min(floatVal3 * floatVal, 1.0F);
      floatVal4 = Math.min(floatVal4 * floatVal, 1.0F);
      int intVal3 = (int)(floatVal2 * 255.0F);
      int intVal4 = (int)(floatVal3 * 255.0F);
      int intVal5 = (int)(floatVal4 * 255.0F);
      return intVal2 | intVal3 << 16 | intVal4 << 8 | intVal5;
   }

   public static int lerpColorArgb(int intVal, int intVal2, float floatVal) {
      int intVal3 = intVal >> 24 & 0xFF;
      int intVal4 = intVal >> 16 & 0xFF;
      int intVal5 = intVal >> 8 & 0xFF;
      int intVal6 = intVal & 0xFF;
      int intVal7 = intVal2 >> 24 & 0xFF;
      int intVal8 = intVal2 >> 16 & 0xFF;
      int intVal9 = intVal2 >> 8 & 0xFF;
      int intVal10 = intVal2 & 0xFF;
      int intVal11 = (int)(intVal3 + (intVal7 - intVal3) * floatVal);
      int intVal12 = (int)(intVal4 + (intVal8 - intVal4) * floatVal);
      int intVal13 = (int)(intVal5 + (intVal9 - intVal5) * floatVal);
      int intVal14 = (int)(intVal6 + (intVal10 - intVal6) * floatVal);
      return intVal11 << 24 | intVal12 << 16 | intVal13 << 8 | intVal14;
   }

}
