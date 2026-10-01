package com.threesix.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import com.threesix.data.LineSegmentBounds;
import com.threesix.util.StringVaultDecoder;

public class WorldToScreenUtil {
   public static final Matrix4f projectionMatrix2 = new Matrix4f();
   public static final Matrix4f modelViewMatrix = new Matrix4f();
   public static final Matrix4f viewMatrix = new Matrix4f();
   public static MinecraftClient minecraft = MinecraftClient.getInstance();

   public static Vec3d projectToScreen(Vec3d arg) {

      Camera minecraftValue = minecraft.getEntityRenderDispatcher().camera;
      int minecraftValue2 = minecraft.getWindow().getHeight();
      int[] local = new int[4];
      GL11.glGetIntegerv(2978, local);
      Vector3f vector3fInst = new Vector3f();
      double argValue = arg.x - minecraftValue.getCameraPos().x;
      double argValue2 = arg.y - minecraftValue.getCameraPos().y;
      double argValue3 = arg.z - minecraftValue.getCameraPos().z;
      Vector4f vector4fInst = new Vector4f((float)argValue, (float)argValue2, (float)argValue3, 1.0F).mul(viewMatrix);
      Matrix4f matrix4fInst = new Matrix4f(projectionMatrix2);
      Matrix4f matrix4fInst2 = new Matrix4f(modelViewMatrix);
      matrix4fInst.mul(matrix4fInst2).project(vector4fInst.x(), vector4fInst.y(), vector4fInst.z(), local, vector3fInst);
      return new Vec3d(vector3fInst.x / minecraft.getWindow().getScaleFactor(), (minecraftValue2 - vector3fInst.y) / minecraft.getWindow().getScaleFactor(), vector3fInst.z);
   }

   public static Pair projectWithBounds(Matrix4f matrix4f, Matrix4f matrix4f2, Vec3d arg) {
      if (minecraft.gameRenderer != null && minecraft.getCameraEntity() != null) {
         LineSegmentBounds lineSegmentBoundsInst = new LineSegmentBounds();
         return !projectLineSegment(matrix4f, matrix4f2, arg.x, arg.y, arg.z, lineSegmentBoundsInst)
            ? null
            : new Pair(new Vec3d(lineSegmentBoundsInst.x, lineSegmentBoundsInst.y, lineSegmentBoundsInst.lineHitX), lineSegmentBoundsInst.isVisible);
      } else {
         return null;
      }
   }

   public static boolean projectLineSegment(Matrix4f matrix4f, Matrix4f matrix4f2, double doubleVal, double doubleVal2, double doubleVal3, LineSegmentBounds lineSegmentBounds) {
      if (minecraft.gameRenderer != null && minecraft.getCameraEntity() != null && lineSegmentBounds != null) {
         Vec3d minecraftValue = minecraft.gameRenderer.getCamera().getCameraPos();
         double doubleValMinecraftValueVal = doubleVal - minecraftValue.x;
         double doubleVal2MinecraftValueVa = doubleVal2 - minecraftValue.y;
         double doubleVal3MinecraftValueVa = doubleVal3 - minecraftValue.z;
         double matrix4fValue = matrix4f.m00() * doubleValMinecraftValueVal + matrix4f.m10() * doubleVal2MinecraftValueVa + matrix4f.m20() * doubleVal3MinecraftValueVa + matrix4f.m30();
         double matrix4fValue2 = matrix4f.m01() * doubleValMinecraftValueVal + matrix4f.m11() * doubleVal2MinecraftValueVa + matrix4f.m21() * doubleVal3MinecraftValueVa + matrix4f.m31();
         double matrix4fValue3 = matrix4f.m02() * doubleValMinecraftValueVal + matrix4f.m12() * doubleVal2MinecraftValueVa + matrix4f.m22() * doubleVal3MinecraftValueVa + matrix4f.m32();
         double matrix4fValue4 = matrix4f.m03() * doubleValMinecraftValueVal + matrix4f.m13() * doubleVal2MinecraftValueVa + matrix4f.m23() * doubleVal3MinecraftValueVa + matrix4f.m33();
         double matrix4f2Value = matrix4f2.m00() * matrix4fValue + matrix4f2.m10() * matrix4fValue2 + matrix4f2.m20() * matrix4fValue3 + matrix4f2.m30() * matrix4fValue4;
         double matrix4f2Value2 = matrix4f2.m01() * matrix4fValue + matrix4f2.m11() * matrix4fValue2 + matrix4f2.m21() * matrix4fValue3 + matrix4f2.m31() * matrix4fValue4;
         double matrix4f2Value3 = matrix4f2.m02() * matrix4fValue + matrix4f2.m12() * matrix4fValue2 + matrix4f2.m22() * matrix4fValue3 + matrix4f2.m32() * matrix4fValue4;
         double matrix4f2Value4 = matrix4f2.m03() * matrix4fValue + matrix4f2.m13() * matrix4fValue2 + matrix4f2.m23() * matrix4fValue3 + matrix4f2.m33() * matrix4fValue4;
         boolean flag = matrix4f2Value4 > 0.0;
         double doubleVal4 = matrix4f2Value4 != 0.0 ? 1.0 / matrix4f2Value4 : 0.0;
         double var24Var33Value = matrix4f2Value * doubleVal4;
         double var26Var33Value = matrix4f2Value2 * doubleVal4;
         double var28Var33Value = matrix4f2Value3 * doubleVal4;
         double doubleVal5 = (var24Var33Value * 0.5 + 0.5) * minecraft.getWindow().getScaledWidth();
         double doubleVal6 = (0.5 - var26Var33Value * 0.5) * minecraft.getWindow().getScaledHeight();
         lineSegmentBounds.setBounds(doubleVal5, doubleVal6, var28Var33Value, matrix4f2Value4, flag);
         return true;
      } else {
         return false;
      }
   }

   public static Vector3f projectToVector3f(Matrix4f matrix4f, Matrix4f matrix4f2, Vec3d arg) {
      Pair local = projectWithBounds(matrix4f, matrix4f2, arg);
      if (local == null) {
         return null;
      }

      Vec3d local2 = (Vec3d)local.getLeft();
      return new Vector3f((float)local2.x, (float)local2.y, (float)local2.z);
   }

   public static Vector3f projectPos(double doubleVal, double doubleVal2, double doubleVal3) {
      return minecraft.gameRenderer != null && minecraft.getCameraEntity() != null ? projectPosVec(new Vec3d(doubleVal, doubleVal2, doubleVal3)) : null;
   }

   public static Vector3f projectPosVec(Vec3d arg) {
      if (minecraft.gameRenderer != null && minecraft.getCameraEntity() != null) {
         Vec3d local = projectToScreen(arg);
         return !(local.z < 0.0) && !(local.z > 1.0)
            ? new Vector3f((float)local.x, (float)local.y, (float)local.z)
            : null;
      } else {
         return null;
      }
   }

   public static Vector3f projectToScreenClamped(Matrix4f matrix4f, Matrix4f matrix4f2, Vec3d arg) {
      if (minecraft.gameRenderer != null && minecraft.getCameraEntity() != null) {
         Vec3d var2Value = arg.subtract(minecraft.gameRenderer.getCamera().getCameraPos());
         if (var2Value.lengthSquared() < 1.0E-4) {
            return new Vector3f(minecraft.getWindow().getScaledWidth() / 2.0F, minecraft.getWindow().getScaledHeight() / 2.0F, 0.0F);
         }

         Vector4f vector4fInst = new Vector4f((float)var2Value.x, (float)var2Value.y, (float)var2Value.z, 1.0F);
         vector4fInst.mul(matrix4f);
         vector4fInst.mul(matrix4f2);
         boolean flag = vector4fInst.w() <= 0.0F;
         float absValue = Math.abs(vector4fInst.w());
         if (absValue < 0.001F) {
            absValue = 0.001F;
         }

         float floatVal = vector4fInst.x() / absValue;
         float floatVal2 = vector4fInst.y() / absValue;
         float minecraftValue = minecraft.getWindow().getScaledWidth();
         float minecraftValue2 = minecraft.getWindow().getScaledHeight();
         float var92Value = minecraftValue / 2.0F;
         float var102Value = minecraftValue2 / 2.0F;
         float floatVal3 = (floatVal * 0.5F + 0.5F) * minecraftValue;
         float floatVal4 = (0.5F - floatVal2 * 0.5F) * minecraftValue2;
         if (!flag && floatVal3 >= 0.0F && floatVal3 <= minecraftValue && floatVal4 >= 0.0F && floatVal4 <= minecraftValue2) {
            return new Vector3f(floatVal3, floatVal4, 0.0F);
         }

         float var13Var11Value = floatVal3 - var92Value;
         float var14Var12Value = floatVal4 - var102Value;
         if (var13Var11Value == 0.0F && var14Var12Value == 0.0F) {
            var14Var12Value = 1.0F;
         }

         float floatVal5 = 10.0F;
         float var92Value2 = minecraftValue / 2.0F - floatVal5;
         float var102Value2 = minecraftValue2 / 2.0F - floatVal5;
         float floatValue = Float.MAX_VALUE;
         float floatValue2 = Float.MAX_VALUE;
         if (var13Var11Value != 0.0F) {
            floatValue = Math.abs(var92Value2 / var13Var11Value);
         }

         if (var14Var12Value != 0.0F) {
            floatValue2 = Math.abs(var102Value2 / var14Var12Value);
         }

         float minValue = Math.min(floatValue, floatValue2);
         return new Vector3f(var92Value + var13Var11Value * minValue, var102Value + var14Var12Value * minValue, 0.0F);
      } else {
         return null;
      }
   }

}
