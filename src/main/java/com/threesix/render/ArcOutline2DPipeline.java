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
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;

public class ArcOutline2DPipeline {
   public static RenderPipeline pipeline2;
   public static GpuBuffer uniformBuffer;
   public static final int uniformSize = 160;

   public static void ensurePipeline() {
      if (pipeline2 == null) {
         try {
            pipeline2 = RenderPipeline.builder(new Snippet[0])
               .withLocation(Identifier.of("threesix", "arc_outline"))
               .withVertexShader(Identifier.of("threesix", "arc_outline_vertex"))
               .withFragmentShader(Identifier.of("threesix", "arc_outline_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), DrawMode.TRIANGLES)
               .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> {

               return "ArcOutline2D Uniforms";
            }, 136, 160L);
         } catch (Exception error) {
         }
      }
   }

   public static void call1(
      Matrix4f matrix4f, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, float floatVal6, float floatVal7, int intVal, int intVal2, float floatVal8
   ) {
      if (pipeline2 == null) {
         ensurePipeline();
      }

      if (pipeline2 != null && uniformBuffer != null) {
         float floatVal9 = (intVal >> 16 & 0xFF) / 255.0F;
         float floatVal10 = (intVal >> 8 & 0xFF) / 255.0F;
         float floatVal11 = (intVal & 0xFF) / 255.0F;
         float floatVal12 = (intVal >> 24 & 0xFF) / 255.0F;
         float floatVal13 = (intVal2 >> 16 & 0xFF) / 255.0F;
         float floatVal14 = (intVal2 >> 8 & 0xFF) / 255.0F;
         float floatVal15 = (intVal2 & 0xFF) / 255.0F;
         float floatVal16 = (intVal2 >> 24 & 0xFF) / 255.0F;
         ByteBuffer memoryUtilValue = MemoryUtil.memAlloc(160);
         memoryUtilValue.putFloat(matrix4f.m00()).putFloat(matrix4f.m01()).putFloat(matrix4f.m02()).putFloat(matrix4f.m03());
         memoryUtilValue.putFloat(matrix4f.m10()).putFloat(matrix4f.m11()).putFloat(matrix4f.m12()).putFloat(matrix4f.m13());
         memoryUtilValue.putFloat(matrix4f.m20()).putFloat(matrix4f.m21()).putFloat(matrix4f.m22()).putFloat(matrix4f.m23());
         memoryUtilValue.putFloat(matrix4f.m30()).putFloat(matrix4f.m31()).putFloat(matrix4f.m32()).putFloat(matrix4f.m33());
         memoryUtilValue.position(64);
         memoryUtilValue.putFloat(floatVal).putFloat(floatVal2).putFloat(floatVal3).putFloat(floatVal3);
         memoryUtilValue.putFloat(floatVal3).putFloat(floatVal4).putFloat(floatVal5).putFloat(floatVal6);
         memoryUtilValue.putFloat(floatVal8).putFloat(floatVal7).putFloat(0.0F).putFloat(0.0F);
         memoryUtilValue.putFloat(floatVal9).putFloat(floatVal10).putFloat(floatVal11).putFloat(floatVal12);
         memoryUtilValue.putFloat(floatVal13).putFloat(floatVal14).putFloat(floatVal15).putFloat(floatVal16);
         memoryUtilValue.flip();
         CommandEncoder renderSystemValue = RenderSystem.getDevice().createCommandEncoder();
         renderSystemValue.writeToBuffer(uniformBuffer.slice(), memoryUtilValue);
         MemoryUtil.memFree(memoryUtilValue);
         Framebuffer class310Value = MinecraftClient.getInstance().getFramebuffer();
         RenderPass renderSystemValueValue = renderSystemValue.createRenderPass(() -> {

            return "ArcOutline2D";
         }, class310Value.getColorAttachmentView(), OptionalInt.empty(), class310Value.getDepthAttachmentView(), OptionalDouble.empty());

         try {
            GuiRenderUtil.applyScissorToPass(renderSystemValueValue);
            renderSystemValueValue.setPipeline(pipeline2);
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

   public static void release() {

      if (uniformBuffer != null) {
         uniformBuffer.close();
         uniformBuffer = null;
      }

      pipeline2 = null;
   }

}
