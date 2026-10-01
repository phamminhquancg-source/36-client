package com.threesix.module;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.util.WorldToScreenUtil;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.data.LineSegmentBounds;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class PearlEspModule extends ModuleBase {
   public static PearlEspModule instance;
   public final ClientSetting alphaSetting = new ClientSetting("Alpha", 180.0, 0.0, 255.0);
   public final ClientSetting tracersSetting = new ClientSetting("Tracers", true);
   public final ClientSetting tracerWidthSetting = new ClientSetting("Tracer Width", 1.5, 0.5, 5.0);
   public final ClientSetting showOwnerSetting = new ClientSetting("Show Owner", true);
   public final ClientSetting colorSetting = new ClientSetting("Color", new Color(148, 0, 211));
   public final List pearlEntities = new CopyOnWriteArrayList();

   public PearlEspModule() {
      super("Pearl ESP", ModuleCategory.RENDER);
      instance = this;
      this.registerSetting(this.alphaSetting);
      this.registerSetting(this.tracersSetting);
      this.registerSetting(this.tracerWidthSetting);
      this.registerSetting(this.showOwnerSetting);
      this.registerSetting(this.colorSetting);
   }

   public static void call1(DrawContext arg, float floatVal) {
      PearlEspModule instanceSnapshot = instance;
      if (instanceSnapshot != null
         && instanceSnapshot.isEnabled()
         && (Boolean)instanceSnapshot.showOwnerSetting.getValue()
         && minecraftClient.world != null
         && minecraftClient.player != null
         && !minecraftClient.options.hudHidden) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {

            for (Entity class1297 : minecraftClient.world.getEntities()) {
               if (class1297 instanceof EnderPearlEntity local2 && local2.getOwner() != null) {
                  String var7Value = local2.getOwner().getName().getString();
                  if (!var7Value.isEmpty()) {
                     double class3532Value = MathHelper.lerp(floatVal, local2.lastRenderX, local2.getX());
                     double class3532Value2 = MathHelper.lerp(floatVal, local2.lastRenderY, local2.getY()) + 0.5;
                     double class3532Value3 = MathHelper.lerp(floatVal, local2.lastRenderZ, local2.getZ());
                     LineSegmentBounds lineSegmentBoundsInst = new LineSegmentBounds();
                     if (WorldToScreenUtil.projectLineSegment(WorldToScreenUtil.modelViewMatrix, WorldToScreenUtil.projectionMatrix2, class3532Value, class3532Value2, class3532Value3, lineSegmentBoundsInst)
                        && lineSegmentBoundsInst.isVisible
                        && !(lineSegmentBoundsInst.lineHitX < 0.0)
                        && !(lineSegmentBoundsInst.lineHitX > 1.0)
                        && !(lineSegmentBoundsInst.healthBarWidth <= 0.0)) {
                        float floatVal2 = (float)(0.5 * minecraftClient.getWindow().getScaledWidth() * 0.025 / lineSegmentBoundsInst.healthBarWidth);
                        if (Float.isFinite(floatVal2) && !(floatVal2 <= 0.0F)) {
                           Matrix3x2fStack var0Value = arg.getMatrices();
                           var0Value.pushMatrix();
                           var0Value.translate((float)lineSegmentBoundsInst.x, (float)lineSegmentBoundsInst.y);
                           var0Value.scale(floatVal2, floatVal2);
                           Color local = (Color)instanceSnapshot.colorSetting.getValue();
                           int intVal = 0xFF000000 | local.getRed() << 16 | local.getGreen() << 8 | local.getBlue();
                           int minecraftClientValue = minecraftClient.textRenderer.getWidth(var7Value);
                           arg.drawText(minecraftClient.textRenderer, var7Value, -(minecraftClientValue / 2), -4, intVal, true);
                           var0Value.popMatrix();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            Vec3d textStyleUtilValue3 = TextStyleUtil.getForwardVector(textStyleUtilValue);
            Vec3d textStyleUtilValue4 = TextStyleUtil.getRightVector(textStyleUtilValue);
            Vec3d textStyleUtilValue5 = TextStyleUtil.cross(textStyleUtilValue3, textStyleUtilValue4);
            Vec3d var5Value = textStyleUtilValue3.multiply(150.0);
            int maxValue = Math.max(0, Math.min(255, (int)Math.round((Double)this.alphaSetting.getValue())));
            Color local = (Color)this.colorSetting.getValue();
            Color colorInst = new Color(local.getRed(), local.getGreen(), local.getBlue(), maxValue);
            Color colorInst2 = new Color(local.getRed(), local.getGreen(), local.getBlue(), 255);
            boolean falseSnapshot = false;

            for (Entity class1297 : minecraftClient.world.getEntities()) {
               if (class1297 instanceof EnderPearlEntity) {
                  falseSnapshot = true;
                  break;
               }
            }

            if (falseSnapshot) {
               arg.push();
               GL11.glDisable(2929);

               try {
                  WorldShapeRenderer textStyleUtilValue6 = TextStyleUtil.acquireRenderer(arg);

                  for (Entity class12972 : minecraftClient.world.getEntities()) {
                     if (class12972 instanceof EnderPearlEntity local2) {
                        double class3532Value = MathHelper.lerp(floatVal, local2.lastRenderX, local2.getX()) - textStyleUtilValue2.x;
                        double class3532Value2 = MathHelper.lerp(floatVal, local2.lastRenderY, local2.getY()) - textStyleUtilValue2.y;
                        double class3532Value3 = MathHelper.lerp(floatVal, local2.lastRenderZ, local2.getZ()) - textStyleUtilValue2.z;
                        double doubleVal = 0.25;
                        textStyleUtilValue6.fillBox(class3532Value - doubleVal, class3532Value2 - doubleVal, class3532Value3 - doubleVal, class3532Value + doubleVal, class3532Value2 + doubleVal, class3532Value3 + doubleVal, colorInst);
                        textStyleUtilValue6.strokeBox(class3532Value - doubleVal, class3532Value2 - doubleVal, class3532Value3 - doubleVal, class3532Value + doubleVal, class3532Value2 + doubleVal, class3532Value3 + doubleVal, colorInst2);
                        if ((Boolean)this.tracersSetting.getValue()) {
                           Vec3d textStyleUtilValue7 = TextStyleUtil.buildBillboardMatrix(class3532Value, class3532Value2 + doubleVal, class3532Value3, textStyleUtilValue3, textStyleUtilValue4, textStyleUtilValue5, 24.0, 2.75);
                           textStyleUtilValue6.drawLine(colorInst2, var5Value, textStyleUtilValue7, ((Double)this.tracerWidthSetting.getValue()).floatValue());
                        }
                     }
                  }
               } finally {
                  GL11.glEnable(2929);
               }

               arg.pop();
            }
         }
      }
   }

}
