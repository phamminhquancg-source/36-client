package com.threesix.render;

import java.awt.Color;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import com.threesix.util.XorBitUtils;
import com.threesix.render.EspRenderPipelines;
import com.threesix.util.StringVaultDecoder;

public final class WorldShapeRenderer {
   public final BufferAllocator fillBuffers = new BufferAllocator(524288);
   public final Immediate fillBuffer = VertexConsumerProvider.immediate(this.fillBuffers);
   public final BufferAllocator lineBuffers = new BufferAllocator(524288);
   public final Immediate lineBuffer = VertexConsumerProvider.immediate(this.lineBuffers);
   public MatrixStack context;
   private boolean hasPendingShapes = false;

   public void setDrawContext(MatrixStack arg) {
      this.context = arg;
   }

   public void fillBox(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, Color color) {

      int intVal = toArgb(color);
      Entry local = this.context.peek();
      float floatVal = (float)doubleVal;
      float floatVal2 = (float)doubleVal2;
      float floatVal3 = (float)doubleVal3;
      float floatVal4 = (float)doubleVal4;
      float floatVal5 = (float)doubleVal5;
      float floatVal6 = (float)doubleVal6;
      VertexConsumer local2 = this.fillBuffer.getBuffer(EspRenderPipelines.getFillPipeline());
      local2.vertex(local, floatVal, floatVal2, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal2, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal2, floatVal6).color(intVal);
      local2.vertex(local, floatVal, floatVal2, floatVal6).color(intVal);
      local2.vertex(local, floatVal, floatVal5, floatVal3).color(intVal);
      local2.vertex(local, floatVal, floatVal5, floatVal6).color(intVal);
      local2.vertex(local, floatVal4, floatVal5, floatVal6).color(intVal);
      local2.vertex(local, floatVal4, floatVal5, floatVal3).color(intVal);
      local2.vertex(local, floatVal, floatVal2, floatVal3).color(intVal);
      local2.vertex(local, floatVal, floatVal5, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal5, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal2, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal2, floatVal6).color(intVal);
      local2.vertex(local, floatVal4, floatVal5, floatVal6).color(intVal);
      local2.vertex(local, floatVal, floatVal5, floatVal6).color(intVal);
      local2.vertex(local, floatVal, floatVal2, floatVal6).color(intVal);
      local2.vertex(local, floatVal, floatVal2, floatVal6).color(intVal);
      local2.vertex(local, floatVal, floatVal5, floatVal6).color(intVal);
      local2.vertex(local, floatVal, floatVal5, floatVal3).color(intVal);
      local2.vertex(local, floatVal, floatVal2, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal2, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal5, floatVal3).color(intVal);
      local2.vertex(local, floatVal4, floatVal5, floatVal6).color(intVal);
      local2.vertex(local, floatVal4, floatVal2, floatVal6).color(intVal);
   }

   public void renderQuad(Color color, Vec3d arg, Vec3d arg2, Vec3d arg3, Vec3d arg4) {
      int intVal = color.getRGB();
      Entry local = this.context.peek();
      VertexConsumer local2 = this.fillBuffer.getBuffer(EspRenderPipelines.getFillPipeline());
      local2.vertex(local, (float)arg.x, (float)arg.y, (float)arg.z).color(intVal);
      local2.vertex(local, (float)arg2.x, (float)arg2.y, (float)arg2.z).color(intVal);
      local2.vertex(local, (float)arg3.x, (float)arg3.y, (float)arg3.z).color(intVal);
      local2.vertex(local, (float)arg4.x, (float)arg4.y, (float)arg4.z).color(intVal);
      local2.vertex(local, (float)arg.x, (float)arg.y, (float)arg.z).color(intVal);
      local2.vertex(local, (float)arg4.x, (float)arg4.y, (float)arg4.z).color(intVal);
      local2.vertex(local, (float)arg3.x, (float)arg3.y, (float)arg3.z).color(intVal);
      local2.vertex(local, (float)arg2.x, (float)arg2.y, (float)arg2.z).color(intVal);
   }

   public void drawMarkerText(Color color, Vec3d arg, Vec3d arg2, Vec3d arg3, Vec3d arg4, RenderLayer arg5) {
      int intVal = color.getRGB();
      Entry local = this.context.peek();
      VertexConsumer local2 = this.fillBuffer.getBuffer(arg5);
      local2.vertex(local, (float)arg.x, (float)arg.y, (float)arg.z).color(intVal).texture(0.0F, 1.0F);
      local2.vertex(local, (float)arg2.x, (float)arg2.y, (float)arg2.z).color(intVal).texture(1.0F, 1.0F);
      local2.vertex(local, (float)arg3.x, (float)arg3.y, (float)arg3.z).color(intVal).texture(1.0F, 0.0F);
      local2.vertex(local, (float)arg4.x, (float)arg4.y, (float)arg4.z).color(intVal).texture(0.0F, 0.0F);
      local2.vertex(local, (float)arg.x, (float)arg.y, (float)arg.z).color(intVal).texture(0.0F, 1.0F);
      local2.vertex(local, (float)arg4.x, (float)arg4.y, (float)arg4.z).color(intVal).texture(0.0F, 0.0F);
      local2.vertex(local, (float)arg3.x, (float)arg3.y, (float)arg3.z).color(intVal).texture(1.0F, 0.0F);
      local2.vertex(local, (float)arg2.x, (float)arg2.y, (float)arg2.z).color(intVal).texture(1.0F, 1.0F);
   }

   public void strokeBox(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, Color color) {

      VertexConsumer local = this.lineBuffer.getBuffer(EspRenderPipelines.getLinesPipeline());
      VertexRendering.drawOutline(this.context, local, VoxelShapes.cuboid(doubleVal, doubleVal2, doubleVal3, doubleVal4, doubleVal5, doubleVal6), 0.0, 0.0, 0.0, toArgb(color), 2.0F);
   }

   public void renderBoxEdges(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, Color color, float floatVal) {

      Vec3d local = new Vec3d(doubleVal, doubleVal2, doubleVal3);
      Vec3d local2 = new Vec3d(doubleVal4, doubleVal2, doubleVal3);
      Vec3d local3 = new Vec3d(doubleVal4, doubleVal2, doubleVal6);
      Vec3d local4 = new Vec3d(doubleVal, doubleVal2, doubleVal6);
      Vec3d local5 = new Vec3d(doubleVal, doubleVal5, doubleVal3);
      Vec3d local6 = new Vec3d(doubleVal4, doubleVal5, doubleVal3);
      Vec3d local7 = new Vec3d(doubleVal4, doubleVal5, doubleVal6);
      Vec3d local8 = new Vec3d(doubleVal, doubleVal5, doubleVal6);
      this.drawLine(color, local, local2, floatVal);
      this.drawLine(color, local2, local3, floatVal);
      this.drawLine(color, local3, local4, floatVal);
      this.drawLine(color, local4, local, floatVal);
      this.drawLine(color, local5, local6, floatVal);
      this.drawLine(color, local6, local7, floatVal);
      this.drawLine(color, local7, local8, floatVal);
      this.drawLine(color, local8, local5, floatVal);
      this.drawLine(color, local, local5, floatVal);
      this.drawLine(color, local2, local6, floatVal);
      this.drawLine(color, local3, local7, floatVal);
      this.drawLine(color, local4, local8, floatVal);
   }

   public void drawLine(Color color, Vec3d arg, Vec3d arg2, float floatVal) {

      VertexConsumer local = this.lineBuffer.getBuffer(EspRenderPipelines.getLinesPipeline());
      int intVal = toArgb(color);
      Entry local2 = this.context.peek();
      Vector3f vector3fInst = new Vector3f(
            (float)(arg2.x - arg.x), (float)(arg2.y - arg.y), (float)(arg2.z - arg.z)
         )
         .normalize();
      float var4Snapshot = floatVal;
      if (var4Snapshot < 0.1F) {
         var4Snapshot = 0.1F;
      }

      if (var4Snapshot > 5.0F) {
         var4Snapshot = 5.0F;
      }

      local.vertex(local2, (float)arg.x, (float)arg.y, (float)arg.z)
         .color(intVal)
         .normal(local2, vector3fInst)
         .lineWidth(var4Snapshot);
      local.vertex(local2, (float)arg2.x, (float)arg2.y, (float)arg2.z)
         .color(intVal)
         .normal(local2, vector3fInst)
         .lineWidth(var4Snapshot);
   }

   public void drawMarkerBox(Color color, Vec3d arg, Vec3d arg2, float floatVal) {

      Color colorInst = new Color(color.getRed(), color.getGreen(), color.getBlue(), 45);
      this.drawLine(colorInst, arg, arg2, floatVal + 2.2F);
      this.drawLine(color, arg, arg2, floatVal);
   }

   public void markShapesPending() {

      this.hasPendingShapes = true;
      this.flush();
   }

   public void flush() {
      if (this.hasPendingShapes) {
         this.hasPendingShapes = false;
         boolean gL11Value = GL11.glIsEnabled(2929);
         boolean gL11Value2 = GL11.glGetBoolean(2930);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         this.lineBuffer.draw();
         GL11.glDisable(2848);
         this.fillBuffer.draw();
         GL11.glDepthMask(gL11Value2);
         if (gL11Value) {
            GL11.glEnable(2929);
         } else {
            GL11.glDisable(2929);
         }
      }
   }

   public static int toArgb(Color color) {
      return color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   public static Color brighten(Color color) {
      int minValue = Math.min(255, (int)(color.getRed() * 1.35F + 45.0F));
      int minValue2 = Math.min(255, (int)(color.getGreen() * 1.35F + 45.0F));
      int minValue3 = Math.min(255, (int)(color.getBlue() * 1.35F + 45.0F));
      return new Color(minValue, minValue2, minValue3, color.getAlpha());
   }

}
