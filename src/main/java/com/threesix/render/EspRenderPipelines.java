package com.threesix.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.threesix.RenderLayerFix;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class EspRenderPipelines {
   public static final RenderLayer ESP_FILL_PIPELINE = RenderLayerFix.of(
      "threesix:esp_fill",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_COLOR_SNIPPET})
               .withLocation("threesix/pipeline/esp_fill")
               .withCull(false)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .build()
         )
         .translucent()
         .build()
   );
   public static final RenderLayer ESP_LINES_PIPELINE = RenderLayerFix.of(
      "threesix:esp_lines",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelines.RENDERTYPE_LINES_SNIPPET})
               .withLocation("threesix/pipeline/esp_lines")
               .withDepthWrite(false)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .build()
         )
         .build()
   );
   public static final RenderLayer ESP_TEXT_PIPELINE = RenderLayerFix.of(
      "threesix:esp_text36",
      RenderSetup.builder(
            RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_TEX_COLOR_SNIPPET})
               .withLocation("threesix/pipeline/esp_text36")
               .withCull(false)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .build()
         )
         .texture("Sampler0", Identifier.of("threesix", "textures/36.png"))
         .translucent()
         .build()
   );

   private EspRenderPipelines() {
   }

   public static RenderLayer getFillPipeline() {
      return ESP_FILL_PIPELINE;
   }

   public static RenderLayer getLinesPipeline() {
      return ESP_LINES_PIPELINE;
   }

   public static RenderLayer getTextPipeline() {
      return ESP_TEXT_PIPELINE;
   }

   public static void drawBoxLines(
      Immediate arg, MatrixStack arg2, Vec3d arg3, double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, int intVal, float floatVal
   ) {
      VertexConsumer var0Value = arg.getBuffer(ESP_LINES_PIPELINE);
      Entry var1Value = arg2.peek();
      float floatVal2 = (float)(doubleVal - arg3.x);
      float floatVal3 = (float)(doubleVal2 - arg3.y);
      float floatVal4 = (float)(doubleVal3 - arg3.z);
      float floatVal5 = (float)(doubleVal4 - arg3.x);
      float floatVal6 = (float)(doubleVal5 - arg3.y);
      float floatVal7 = (float)(doubleVal6 - arg3.z);
      drawLineSegment(var0Value, var1Value, floatVal2, floatVal3, floatVal4, floatVal5, floatVal3, floatVal4, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal5, floatVal3, floatVal4, floatVal5, floatVal3, floatVal7, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal5, floatVal3, floatVal7, floatVal2, floatVal3, floatVal7, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal2, floatVal3, floatVal7, floatVal2, floatVal3, floatVal4, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal2, floatVal6, floatVal4, floatVal5, floatVal6, floatVal4, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal5, floatVal6, floatVal4, floatVal5, floatVal6, floatVal7, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal5, floatVal6, floatVal7, floatVal2, floatVal6, floatVal7, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal2, floatVal6, floatVal7, floatVal2, floatVal6, floatVal4, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal2, floatVal3, floatVal4, floatVal2, floatVal6, floatVal4, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal5, floatVal3, floatVal4, floatVal5, floatVal6, floatVal4, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal5, floatVal3, floatVal7, floatVal5, floatVal6, floatVal7, intVal, floatVal);
      drawLineSegment(var0Value, var1Value, floatVal2, floatVal3, floatVal7, floatVal2, floatVal6, floatVal7, intVal, floatVal);
   }

   public static void drawBoxFaces(
      Immediate arg, MatrixStack arg2, Vec3d arg3, double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, int intVal
   ) {
      VertexConsumer var0Value = arg.getBuffer(ESP_FILL_PIPELINE);
      Entry var1Value = arg2.peek();
      float floatVal = (float)(doubleVal - arg3.x);
      float floatVal2 = (float)(doubleVal2 - arg3.y);
      float floatVal3 = (float)(doubleVal3 - arg3.z);
      float floatVal4 = (float)(doubleVal4 - arg3.x);
      float floatVal5 = (float)(doubleVal5 - arg3.y);
      float floatVal6 = (float)(doubleVal6 - arg3.z);
      drawQuad(var0Value, var1Value, intVal, floatVal, floatVal2, floatVal3, floatVal4, floatVal2, floatVal3, floatVal4, floatVal2, floatVal6, floatVal, floatVal2, floatVal6);
      drawQuad(var0Value, var1Value, intVal, floatVal, floatVal5, floatVal3, floatVal, floatVal5, floatVal6, floatVal4, floatVal5, floatVal6, floatVal4, floatVal5, floatVal3);
      drawQuad(var0Value, var1Value, intVal, floatVal, floatVal2, floatVal3, floatVal, floatVal5, floatVal3, floatVal4, floatVal5, floatVal3, floatVal4, floatVal2, floatVal3);
      drawQuad(var0Value, var1Value, intVal, floatVal, floatVal2, floatVal6, floatVal4, floatVal2, floatVal6, floatVal4, floatVal5, floatVal6, floatVal, floatVal5, floatVal6);
      drawQuad(var0Value, var1Value, intVal, floatVal, floatVal2, floatVal3, floatVal, floatVal2, floatVal6, floatVal, floatVal5, floatVal6, floatVal, floatVal5, floatVal3);
      drawQuad(var0Value, var1Value, intVal, floatVal4, floatVal2, floatVal3, floatVal4, floatVal5, floatVal3, floatVal4, floatVal5, floatVal6, floatVal4, floatVal2, floatVal6);
   }

   public static void drawDirectionalLine(
      Immediate arg, MatrixStack arg2, Vec3d arg3, Vector3fc vector3fc, double doubleVal, double doubleVal2, double doubleVal3, int intVal, float floatVal
   ) {
      float floatVal2 = vector3fc.x();
      float floatVal3 = vector3fc.y();
      float floatVal4 = vector3fc.z();
      float floatVal5 = (float)Math.sqrt(floatVal2 * floatVal2 + floatVal3 * floatVal3 + floatVal4 * floatVal4);
      if (floatVal5 > 1.0E-6F) {
         floatVal2 /= floatVal5;
         floatVal3 /= floatVal5;
         floatVal4 /= floatVal5;
      }

      float var120Value = floatVal2 * 0.35F;
      float var130Value = floatVal3 * 0.35F;
      float var140Value = floatVal4 * 0.35F;
      float floatVal6 = (float)(doubleVal - arg3.x);
      float floatVal7 = (float)(doubleVal2 - arg3.y);
      float floatVal8 = (float)(doubleVal3 - arg3.z);
      float var19Var12Var20Var13Var21V = floatVal6 * floatVal2 + floatVal7 * floatVal3 + floatVal8 * floatVal4;
      if (var19Var12Var20Var13Var21V < 0.1F) {
         float floatVal9 = -0.25F / (var19Var12Var20Var13Var21V - 0.35F);
         floatVal6 = var120Value + (floatVal6 - var120Value) * floatVal9;
         floatVal7 = var130Value + (floatVal7 - var130Value) * floatVal9;
         floatVal8 = var140Value + (floatVal8 - var140Value) * floatVal9;
      }

      VertexConsumer var0Value = arg.getBuffer(ESP_LINES_PIPELINE);
      Entry var1Value = arg2.peek();
      drawLineSegment(var0Value, var1Value, var120Value, var130Value, var140Value, floatVal6, floatVal7, floatVal8, intVal, floatVal);
   }

   public static void flushPipelines(Immediate arg) {
      arg.draw(ESP_FILL_PIPELINE);
      arg.draw(ESP_LINES_PIPELINE);
   }

   private static void drawLineSegment(
      VertexConsumer arg, Entry arg2, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, int intVal, float floatVal7
   ) {
      Vector3f vector3fInst = new Vector3f(floatVal4 - floatVal, floatVal5 - floatVal2, floatVal6 - floatVal3);
      if (vector3fInst.lengthSquared() > 1.0E-9F) {
         vector3fInst.normalize();
      } else {
         vector3fInst.set(0.0F, 1.0F, 0.0F);
      }

      arg.vertex(arg2, floatVal, floatVal2, floatVal3).color(intVal).normal(arg2, vector3fInst).lineWidth(floatVal7);
      arg.vertex(arg2, floatVal4, floatVal5, floatVal6).color(intVal).normal(arg2, vector3fInst).lineWidth(floatVal7);
   }

   private static void drawQuad(
      VertexConsumer arg,
      Entry arg2,
      int intVal,
      float floatVal,
      float floatVal2,
      float floatVal3,
      float floatVal4,
      float floatVal5,
      float floatVal6,
      float floatVal7,
      float floatVal8,
      float floatVal9,
      float floatVal10,
      float floatVal11,
      float floatVal12
   ) {
      arg.vertex(arg2, floatVal, floatVal2, floatVal3).color(intVal);
      arg.vertex(arg2, floatVal4, floatVal5, floatVal6).color(intVal);
      arg.vertex(arg2, floatVal7, floatVal8, floatVal9).color(intVal);
      arg.vertex(arg2, floatVal10, floatVal11, floatVal12).color(intVal);
   }

}
