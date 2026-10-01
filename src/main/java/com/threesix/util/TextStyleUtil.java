package com.threesix.util;

import java.awt.Color;
import java.lang.reflect.Method;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.util.WorldToScreenUtil;
import com.threesix.util.StringVaultDecoder;

public class TextStyleUtil {
   public static final Matrix4f projectionMatrix = new Matrix4f();
   public static final FrustumIntersection frustum = new FrustumIntersection();
   public static boolean hasFrustum;
   public static double cameraX;
   public static double cameraY;
   public static double cameraZ;
   public static final ThreadLocal rendererHolder = ThreadLocal.withInitial(WorldShapeRenderer::new);
   public static Method rotationMethod;

   public static WorldShapeRenderer acquireRenderer(MatrixStack arg) {
      WorldShapeRenderer local = (WorldShapeRenderer)rendererHolder.get();
      local.setDrawContext(arg);
      return local;
   }

   public static void setProjection(Matrix4f matrix4f, Matrix4f matrix4f2, Vec3d arg) {
      if (matrix4f != null && matrix4f2 != null && arg != null) {
         matrix4f2.mul(matrix4f, projectionMatrix);
         frustum.set(projectionMatrix);
         cameraX = arg.x;
         cameraY = arg.y;
         cameraZ = arg.z;
         hasFrustum = true;
      } else {
         hasFrustum = false;
      }
   }

   public static boolean isAabbVisible(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6) {
      if (!hasFrustum) {
         return true;
      }

      int frustumValue = frustum.intersectAab(
         (float)(doubleVal - cameraX),
         (float)(doubleVal2 - cameraY),
         (float)(doubleVal3 - cameraZ),
         (float)(doubleVal4 - cameraX),
         (float)(doubleVal5 - cameraY),
         (float)(doubleVal6 - cameraZ)
      );
      return frustumValue == -1 || frustumValue == -2;
   }

   public static Camera getGameRenderer() {
      return MinecraftClient.getInstance().gameRenderer.getCamera();
   }

   public static Vec3d getCameraRotation(Camera arg) {
      if (rotationMethod == null) {
         for (Method method : Camera.class.getMethods()) {
            if (method.getReturnType() == Vec3d.class && method.getParameterCount() == 0) {
               rotationMethod = method;
               break;
            }
         }
      }

      try {
         return (Vec3d)rotationMethod.invoke(arg);
      } catch (Exception error) {
         return MinecraftClient.getInstance().player.getCameraPosVec(1.0F);
      }
   }

   public static void fillBox(MatrixStack arg, double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, Color color) {

      WorldShapeRenderer local = (WorldShapeRenderer)rendererHolder.get();
      local.setDrawContext(arg);
      local.fillBox(doubleVal, doubleVal2, doubleVal3, doubleVal4, doubleVal5, doubleVal6, color);
   }

   public static void strokeBox(MatrixStack arg, double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, Color color) {
      WorldShapeRenderer local = (WorldShapeRenderer)rendererHolder.get();
      local.setDrawContext(arg);
      local.strokeBox(doubleVal, doubleVal2, doubleVal3, doubleVal4, doubleVal5, doubleVal6, color);
      local.flush();
   }

   public static void drawLine(MatrixStack arg, Color color, Vec3d arg2, Vec3d arg3) {
      drawThickLine(arg, color, arg2, arg3, 2.0F);
   }

   public static void drawThickLine(MatrixStack arg, Color color, Vec3d arg2, Vec3d arg3, float floatVal) {
      WorldShapeRenderer local = (WorldShapeRenderer)rendererHolder.get();
      local.setDrawContext(arg);
      local.drawLine(color, arg2, arg3, floatVal);
      local.flush();
   }

   public static Vec3d getForwardVector(Camera arg) {
      return new Vec3d(0.0, 0.0, 1.0)
         .rotateX(-((float)Math.toRadians(arg.getPitch())))
         .rotateY(-((float)Math.toRadians(arg.getYaw())))
         .normalize();
   }

   public static Vec3d getRightVector(Camera arg) {
      return new Vec3d(1.0, 0.0, 0.0).rotateY(-((float)Math.toRadians(arg.getYaw()))).normalize();
   }

   public static Vec3d cross(Vec3d arg, Vec3d arg2) {
      return arg.crossProduct(arg2).normalize();
   }

   public static Vec3d buildBillboardMatrix(double doubleVal, double doubleVal2, double doubleVal3, Vec3d arg, Vec3d arg2, Vec3d arg3, double doubleVal4, double doubleVal5) {
      double doubleValArg2Value = doubleVal * arg2.x + doubleVal2 * arg2.y + doubleVal3 * arg2.z;
      double doubleValArg3Value = doubleVal * arg3.x + doubleVal2 * arg3.y + doubleVal3 * arg3.z;
      double doubleValArgValue = doubleVal * arg.x + doubleVal2 * arg.y + doubleVal3 * arg.z;
      double maxValue = Math.max(Math.abs(doubleValArgValue), 0.25);
      double var11Snapshot = doubleValArg2Value / maxValue;
      double local = doubleValArg3Value / maxValue;
      if (doubleValArgValue <= 0.0) {
         double hypotValue = Math.hypot(var11Snapshot, local);
         if (hypotValue < doubleVal5) {
            if (hypotValue < 1.0E-4) {
               var11Snapshot = doubleVal5;
               local = 0.0;
            } else {
               double doubleVal5Var25Value = doubleVal5 / hypotValue;
               var11Snapshot *= doubleVal5Var25Value;
               local *= doubleVal5Var25Value;
            }
         }
      }

      return arg.add(arg2.multiply(var11Snapshot)).add(arg3.multiply(local)).normalize().multiply(doubleVal4);
   }

   public static Vec3d buildBillboardFromOrigin(Vec3d arg, Vec3d arg2, Vec3d arg3, Vec3d arg4, double doubleVal, double doubleVal2) {

      return buildBillboardMatrix(arg.x, arg.y, arg.z, arg2, arg3, arg4, doubleVal, doubleVal2);
   }

   public static Vec3d projectLabelPosition(Vec3d arg, Vec3d arg2, Vec3d arg3, Vec3d arg4, Vec3d arg5, double doubleVal) {
      MinecraftClient mc = MinecraftClient.getInstance();
      int var7Value = mc.getWindow().getScaledWidth();
      int var7Value2 = mc.getWindow().getScaledHeight();
      Pair worldToScreenUtilValue = WorldToScreenUtil.projectWithBounds(WorldToScreenUtil.modelViewMatrix, WorldToScreenUtil.projectionMatrix2, arg2);
      if (worldToScreenUtilValue != null && (Boolean)worldToScreenUtilValue.getRight()) {
         Vec3d local = (Vec3d)worldToScreenUtilValue.getLeft();
         if (local.x >= 0.0 && local.x <= var7Value && local.y >= 0.0 && local.y <= var7Value2) {
            return arg;
         }
      }

      Vector3f worldToScreenUtilValue2 = WorldToScreenUtil.projectToScreenClamped(WorldToScreenUtil.modelViewMatrix, WorldToScreenUtil.projectionMatrix2, arg2);
      return worldToScreenUtilValue2 == null ? arg : toNdcFactor(worldToScreenUtilValue2.x, worldToScreenUtilValue2.y, var7Value, var7Value2, arg3, arg4, arg5).multiply(doubleVal);
   }

   public static Vec3d toNdcFactor(float floatVal, float floatVal2, int intVal, int intVal2, Vec3d arg, Vec3d arg2, Vec3d arg3) {
      double floatValIntVal2Value = floatVal / intVal * 2.0F - 1.0F;
      double doubleVal = 1.0F - floatVal2 / intVal2 * 2.0F;
      double worldToScreenUtilValue = WorldToScreenUtil.projectionMatrix2.m11() / WorldToScreenUtil.projectionMatrix2.m00();
      double doubleVal2 = 1.0 / WorldToScreenUtil.projectionMatrix2.m11();
      return arg.add(arg2.multiply(floatValIntVal2Value * worldToScreenUtilValue * doubleVal2)).add(arg3.multiply(doubleVal * doubleVal2)).normalize();
   }

   public static String getVaultKey5() {

      return "D";
   }

}
