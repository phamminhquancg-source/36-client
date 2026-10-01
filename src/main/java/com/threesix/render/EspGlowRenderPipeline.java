package com.threesix.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;
import com.threesix.util.StringVaultDecoder;

public final class EspGlowRenderPipeline {
   public static RenderPipeline espGlowPipeline;
   public static GpuBuffer uniformBuffer;
   public static final int uniformBufferSize = 128;
   public static float boxSize = 12.0F;

   public static void initEspGlowPipeline() {
      if (espGlowPipeline == null) {
         try {
            espGlowPipeline = RenderPipeline.builder(new Snippet[0])
               .withLocation(Identifier.of("threesix", "esp_glow_box"))
               .withVertexShader(Identifier.of("threesix", "esp_glow_box_vertex"))
               .withFragmentShader(Identifier.of("threesix", "esp_glow_box_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), DrawMode.TRIANGLES)
               .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> {
               return "ESPGlow Uniforms";
            }, 136, 128L);
         } catch (Exception error) {
         }
      }
   }

   public static void renderEspGlowBox(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, Color color, float floatVal5, float floatVal6) {

      if (espGlowPipeline == null) {
         initEspGlowPipeline();
      }

      if (espGlowPipeline != null && uniformBuffer != null) {
         ByteBuffer memoryUtilValue = MemoryUtil.memAlloc(128);

         try {
            memoryUtilValue.putFloat(matrix4f.m00()).putFloat(matrix4f.m01()).putFloat(matrix4f.m02()).putFloat(matrix4f.m03());
            memoryUtilValue.putFloat(matrix4f.m10()).putFloat(matrix4f.m11()).putFloat(matrix4f.m12()).putFloat(matrix4f.m13());
            memoryUtilValue.putFloat(matrix4f.m20()).putFloat(matrix4f.m21()).putFloat(matrix4f.m22()).putFloat(matrix4f.m23());
            memoryUtilValue.putFloat(matrix4f.m30()).putFloat(matrix4f.m31()).putFloat(matrix4f.m32()).putFloat(matrix4f.m33());
            memoryUtilValue.putFloat(floatVal).putFloat(floatVal2).putFloat(floatVal3).putFloat(floatVal4);
            memoryUtilValue.putFloat(color.getRed() / 255.0F);
            memoryUtilValue.putFloat(color.getGreen() / 255.0F);
            memoryUtilValue.putFloat(color.getBlue() / 255.0F);
            memoryUtilValue.putFloat(color.getAlpha() / 255.0F);
            memoryUtilValue.putFloat(floatVal5);
            memoryUtilValue.putFloat(floatVal6);
            memoryUtilValue.putFloat(0.0F).putFloat(0.0F);
            memoryUtilValue.flip();
            CommandEncoder renderSystemValue = RenderSystem.getDevice().createCommandEncoder();
            renderSystemValue.writeToBuffer(uniformBuffer.slice(), memoryUtilValue);
            Framebuffer class310Value = MinecraftClient.getInstance().getFramebuffer();
            RenderPass renderSystemValueValue = renderSystemValue.createRenderPass(() -> {
               return "ESPGlow";
            }, class310Value.getColorAttachmentView(), OptionalInt.empty(), class310Value.getDepthAttachmentView(), OptionalDouble.empty());

            try {
               GuiRenderUtil.applyScissorToPass(renderSystemValueValue);
               renderSystemValueValue.setPipeline(espGlowPipeline);
               renderSystemValueValue.setUniform("Uniforms", uniformBuffer);
               renderSystemValueValue.draw(0, 6);
            } catch (Throwable error) {
               if (renderSystemValueValue != null) {
                  try {
                     renderSystemValueValue.close();
                  } catch (Throwable error2) {
                     error.addSuppressed(error2);
                  }
               }

               throw error;
            }

            if (renderSystemValueValue != null) {
               renderSystemValueValue.close();
            }
         } finally {
            MemoryUtil.memFree(memoryUtilValue);
         }
      }
   }

   public static void disposeEspGlowPipeline() {

      if (uniformBuffer != null) {
         uniformBuffer.close();
         uniformBuffer = null;
      }

      espGlowPipeline = null;
   }

}
