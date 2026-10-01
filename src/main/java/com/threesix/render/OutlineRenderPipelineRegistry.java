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
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import com.threesix.util.StringVaultDecoder;
import com.threesix.util.GuiRenderUtil;

public class OutlineRenderPipelineRegistry {
   public static RenderPipeline outlinePipeline;
   public static GpuBuffer uniformBuffer;
   public static final int uniformBufferSize = 256;

   public static void initOutlinePipeline() {
      if (outlinePipeline == null) {
         try {
            outlinePipeline = RenderPipeline.builder(new Snippet[0])
               .withLocation(Identifier.of("threesix", "outline"))
               .withVertexShader(Identifier.of("threesix", "outline_vertex"))
               .withFragmentShader(Identifier.of("threesix", "outline_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), DrawMode.TRIANGLES)
               .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> {

               return "Outline2D Uniforms";
            }, 136, 256L);
         } catch (Exception error) {
         }
      }
   }

   public static void call1(
      Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, float floatVal8, float floatVal9, float floatVal10, int... local
   ) {
      if (outlinePipeline == null) {
         initOutlinePipeline();
      }

      if (outlinePipeline != null && uniformBuffer != null) {
         int[] local2 = expandVertexColors(local);
         ByteBuffer memoryUtilValue = MemoryUtil.memAlloc(256);
         memoryUtilValue.putFloat(matrix4f.m00()).putFloat(matrix4f.m01()).putFloat(matrix4f.m02()).putFloat(matrix4f.m03());
         memoryUtilValue.putFloat(matrix4f.m10()).putFloat(matrix4f.m11()).putFloat(matrix4f.m12()).putFloat(matrix4f.m13());
         memoryUtilValue.putFloat(matrix4f.m20()).putFloat(matrix4f.m21()).putFloat(matrix4f.m22()).putFloat(matrix4f.m23());
         memoryUtilValue.putFloat(matrix4f.m30()).putFloat(matrix4f.m31()).putFloat(matrix4f.m32()).putFloat(matrix4f.m33());
         memoryUtilValue.position(64);
         memoryUtilValue.putFloat(floatVal).putFloat(floatVal2).putFloat(floatVal3).putFloat(floatVal4);
         memoryUtilValue.putFloat(floatVal6).putFloat(floatVal7).putFloat(floatVal5).putFloat(floatVal8);
         memoryUtilValue.position(96);
         memoryUtilValue.putFloat(floatVal9).putFloat(floatVal10).putFloat(0.0F).putFloat(0.0F);
         memoryUtilValue.position(112);

         for (int index = 0; index < 9; index++) {
            int intVal = local2[index];
            memoryUtilValue.putFloat((intVal >> 16 & 0xFF) / 255.0F);
            memoryUtilValue.putFloat((intVal >> 8 & 0xFF) / 255.0F);
            memoryUtilValue.putFloat((intVal & 0xFF) / 255.0F);
            memoryUtilValue.putFloat((intVal >> 24 & 0xFF) / 255.0F);
         }

         memoryUtilValue.flip();
         CommandEncoder renderSystemValue = RenderSystem.getDevice().createCommandEncoder();
         renderSystemValue.writeToBuffer(uniformBuffer.slice(), memoryUtilValue);
         MemoryUtil.memFree(memoryUtilValue);
         Framebuffer class310Value = MinecraftClient.getInstance().getFramebuffer();
         RenderPass renderSystemValueValue = renderSystemValue.createRenderPass(() -> {
            return "Outline2D";
         }, class310Value.getColorAttachmentView(), OptionalInt.empty(), class310Value.getDepthAttachmentView(), OptionalDouble.empty());

         try {
            GuiRenderUtil.applyScissorToPass(renderSystemValueValue);
            renderSystemValueValue.setPipeline(outlinePipeline);
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
      }
   }

   public static int[] expandVertexColors(int[] int2) {

      if (int2.length == 1) {
         int intVal = int2[0];
         return new int[]{intVal, intVal, intVal, intVal, intVal, intVal, intVal, intVal, intVal};
      }

      if (int2.length >= 9) {
         return int2;
      }

      int[] local = new int[9];

      for (int index = 0; index < 9; index++) {
         local[index] = index < int2.length ? int2[index] : int2[int2.length - 1];
      }

      return local;
   }

   public static void disposeOutlinePipeline() {
      if (uniformBuffer != null) {
         uniformBuffer.close();
         uniformBuffer = null;
      }

      outlinePipeline = null;
   }

}
