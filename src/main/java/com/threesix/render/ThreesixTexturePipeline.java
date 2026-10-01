package com.threesix.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;
import com.threesix.util.StringVaultDecoder;

public class ThreesixTexturePipeline {
   public static RenderPipeline pipeline2;
   public static GpuBuffer uniformBuffer;
   public static final int maxTextureSize = 128;

   public static void init() {
      if (pipeline2 == null) {
         try {
            pipeline2 = RenderPipeline.builder(new Snippet[0])
               .withLocation(Identifier.of("threesix", "texture"))
               .withVertexShader(Identifier.of("threesix", "texture_vertex"))
               .withFragmentShader(Identifier.of("threesix", "texture_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), DrawMode.TRIANGLES)
               .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
               .withSampler("Sampler0")
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> {
               return "Texture2D Uniforms";
            }, 136, 128L);
         } catch (Exception error) {
         }
      }

   }

   public static void call1(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, GpuTextureView gpuTextureView, int intVal, float floatVal4, float floatVal5) {
      if (pipeline2 == null) {
         init();
      }

      if (pipeline2 != null && uniformBuffer != null && gpuTextureView != null) {
         float floatVal6 = (intVal >> 16 & 0xFF) / 255.0F;
         float floatVal7 = (intVal >> 8 & 0xFF) / 255.0F;
         float floatVal8 = (intVal & 0xFF) / 255.0F;
         float floatVal9 = (intVal >> 24 & 0xFF) / 255.0F;
         ByteBuffer memoryUtilValue = MemoryUtil.memAlloc(128);
         memoryUtilValue.putFloat(matrix4f.m00()).putFloat(matrix4f.m01()).putFloat(matrix4f.m02()).putFloat(matrix4f.m03());
         memoryUtilValue.putFloat(matrix4f.m10()).putFloat(matrix4f.m11()).putFloat(matrix4f.m12()).putFloat(matrix4f.m13());
         memoryUtilValue.putFloat(matrix4f.m20()).putFloat(matrix4f.m21()).putFloat(matrix4f.m22()).putFloat(matrix4f.m23());
         memoryUtilValue.putFloat(matrix4f.m30()).putFloat(matrix4f.m31()).putFloat(matrix4f.m32()).putFloat(matrix4f.m33());
         memoryUtilValue.position(64);
         memoryUtilValue.putFloat(floatVal).putFloat(floatVal2).putFloat(floatVal3).putFloat(floatVal3);
         memoryUtilValue.putFloat(floatVal6).putFloat(floatVal7).putFloat(floatVal8).putFloat(floatVal9);
         memoryUtilValue.putFloat(floatVal4).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         memoryUtilValue.putFloat(floatVal5).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         memoryUtilValue.flip();
         CommandEncoder renderSystemValue = RenderSystem.getDevice().createCommandEncoder();
         renderSystemValue.writeToBuffer(uniformBuffer.slice(), memoryUtilValue);
         MemoryUtil.memFree(memoryUtilValue);
         GpuSampler renderSystemValue2 = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
         Framebuffer class310Value = MinecraftClient.getInstance().getFramebuffer();
         RenderPass renderSystemValueValue = renderSystemValue.createRenderPass(() -> {

            return "Texture2D";
         }, class310Value.getColorAttachmentView(), OptionalInt.empty(), class310Value.getDepthAttachmentView(), OptionalDouble.empty());

         try {
            GuiRenderUtil.applyScissorToPass(renderSystemValueValue);
            renderSystemValueValue.setPipeline(pipeline2);
            renderSystemValueValue.setUniform("Uniforms", uniformBuffer);
            renderSystemValueValue.bindTexture("Sampler0", gpuTextureView, renderSystemValue2);
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
      }
   }

   public static void call2(Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, GpuTextureView gpuTextureView, int intVal, float floatVal5) {

      if (pipeline2 == null) {
         init();
      }

      if (pipeline2 != null && uniformBuffer != null && gpuTextureView != null) {
         float floatVal6 = (intVal >> 16 & 0xFF) / 255.0F;
         float floatVal7 = (intVal >> 8 & 0xFF) / 255.0F;
         float floatVal8 = (intVal & 0xFF) / 255.0F;
         float floatVal9 = (intVal >> 24 & 0xFF) / 255.0F;
         ByteBuffer memoryUtilValue = MemoryUtil.memAlloc(128);
         memoryUtilValue.putFloat(matrix4f.m00()).putFloat(matrix4f.m01()).putFloat(matrix4f.m02()).putFloat(matrix4f.m03());
         memoryUtilValue.putFloat(matrix4f.m10()).putFloat(matrix4f.m11()).putFloat(matrix4f.m12()).putFloat(matrix4f.m13());
         memoryUtilValue.putFloat(matrix4f.m20()).putFloat(matrix4f.m21()).putFloat(matrix4f.m22()).putFloat(matrix4f.m23());
         memoryUtilValue.putFloat(matrix4f.m30()).putFloat(matrix4f.m31()).putFloat(matrix4f.m32()).putFloat(matrix4f.m33());
         memoryUtilValue.position(64);
         memoryUtilValue.putFloat(floatVal).putFloat(floatVal2).putFloat(floatVal3).putFloat(floatVal4);
         memoryUtilValue.putFloat(floatVal6).putFloat(floatVal7).putFloat(floatVal8).putFloat(floatVal9);
         memoryUtilValue.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         memoryUtilValue.putFloat(floatVal5).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         memoryUtilValue.flip();
         CommandEncoder renderSystemValue = RenderSystem.getDevice().createCommandEncoder();
         renderSystemValue.writeToBuffer(uniformBuffer.slice(), memoryUtilValue);
         MemoryUtil.memFree(memoryUtilValue);
         GpuSampler renderSystemValue2 = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
         Framebuffer class310Value = MinecraftClient.getInstance().getFramebuffer();
         RenderPass renderSystemValueValue = renderSystemValue.createRenderPass(() -> {
            return "AwtFont";
         }, class310Value.getColorAttachmentView(), OptionalInt.empty(), class310Value.getDepthAttachmentView(), OptionalDouble.empty());

         try {
            GuiRenderUtil.applyScissorToPass(renderSystemValueValue);
            renderSystemValueValue.setPipeline(pipeline2);
            renderSystemValueValue.setUniform("Uniforms", uniformBuffer);
            renderSystemValueValue.bindTexture("Sampler0", gpuTextureView, renderSystemValue2);
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
      }
   }

   public static void close2() {
      if (uniformBuffer != null) {
         uniformBuffer.close();
         uniformBuffer = null;
      }

      pipeline2 = null;
   }

}
