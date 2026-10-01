package com.threesix.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.data.PositionTimestamp;
import com.threesix.setting.ClientSetting;

public final class JumpCirclesModule extends ModuleBase {
   public static final int circleSegments = 128;
   public final ClientSetting lifetimeSetting = new ClientSetting("Lifetime (s)", 1.5F, 0.1F, 5.0F);
   public final ClientSetting startRadiusSetting = new ClientSetting("Start Radius", 0.55F, 0.1F, 3.0F);
   public final ClientSetting endRadiusSetting = new ClientSetting("End Radius", 1.8F, 0.2F, 6.0F);
   public final ClientSetting lineWidthSetting = new ClientSetting("Line Width", 2.0F, 0.5F, 6.0F);
   public final ClientSetting colorSetting = new ClientSetting("Color", new Color(120, 220, 255, 255));
   public final ClientSetting glowModeSetting = new ClientSetting("Glow Mode", false);
   public final ClientSetting glowFilledSetting = new ClientSetting("Glow Filled", false);
   public final List<PositionTimestamp> jumpPositions = new ArrayList<>();
   public boolean wasOnGround = true;
   public double x = 0.0;
   public double y = 0.0;
   public double z = 0.0;

   public JumpCirclesModule() {
      super("JumpCircles", ModuleCategory.RENDER);
      this.registerSetting(this.lifetimeSetting);
      this.registerSetting(this.startRadiusSetting);
      this.registerSetting(this.endRadiusSetting);
      this.registerSetting(this.lineWidthSetting);
      this.registerSetting(this.colorSetting);
      this.registerSetting(this.glowModeSetting);
      this.registerSetting(this.glowFilledSetting);
   }

   @Override
   public void onEnable() {

      this.jumpPositions.clear();
      this.wasOnGround = true;
   }

   @Override
   public void onDisable() {
      this.jumpPositions.clear();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null) {
         boolean minecraftClientValue = minecraftClient.player.isOnGround();
         if (minecraftClientValue) {
            this.x = minecraftClient.player.getX();
            this.y = minecraftClient.player.getY();
            this.z = minecraftClient.player.getZ();
         }

         if (this.wasOnGround && !minecraftClientValue && minecraftClient.player.getVelocity().y > 0.0) {
            this.jumpPositions.add(new PositionTimestamp(this.x, this.y + 0.02, this.z, System.currentTimeMillis()));
         }

         this.wasOnGround = minecraftClientValue;
         long systemValue = System.currentTimeMillis();
         long longVal = (long)((Float)this.lifetimeSetting.getValue() * 1000.0F);
         Iterator local = this.jumpPositions.iterator();

         while (local.hasNext()) {
            if (systemValue - ((PositionTimestamp)local.next()).timestamp >= longVal) {
               local.remove();
            }
         }
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null && !this.jumpPositions.isEmpty()) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            long systemValue = System.currentTimeMillis();
            long longVal = (long)((Float)this.lifetimeSetting.getValue() * 1000.0F);
            double doubleVal = ((Float)this.startRadiusSetting.getValue()).floatValue();
            double doubleVal2 = ((Float)this.endRadiusSetting.getValue()).floatValue();
            float floatVal2 = (Float)this.lineWidthSetting.getValue();
            Color local = (Color)this.colorSetting.getValue();
            int intVal = local.getAlpha();
            arg.push();
            WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);

            for (PositionTimestamp positionTimestamp : this.jumpPositions) {
               float floatVal3 = (float)(systemValue - positionTimestamp.timestamp) / (float)longVal;
               if (floatVal3 < 0.0F) {
                  floatVal3 = 0.0F;
               }

               if (floatVal3 > 1.0F) {
                  floatVal3 = 1.0F;
               }

               float floatVal4 = 1.0F - (1.0F - floatVal3) * (1.0F - floatVal3) * (1.0F - floatVal3);
               double var9Var11Var9FloatVal4Valu = doubleVal + (doubleVal2 - doubleVal) * floatVal4;
               int intVal2 = (int)(intVal * (1.0F - floatVal4));
               if (intVal2 > 0) {
                  Color colorInst = new Color(local.getRed(), local.getGreen(), local.getBlue(), intVal2);
                  double positionTimestampValue = positionTimestamp.x - textStyleUtilValue2.x;
                  double positionTimestampValue2 = positionTimestamp.y - textStyleUtilValue2.y;
                  double positionTimestampValue3 = positionTimestamp.z - textStyleUtilValue2.z;
                  Vec3d[] local2 = new Vec3d[129];

                  for (int index = 0; index <= 128; index++) {
                     double doubleVal3 = (Math.PI * 2) * (index / 128.0);
                     local2[index] = new Vec3d(positionTimestampValue + Math.cos(doubleVal3) * var9Var11Var9FloatVal4Valu, positionTimestampValue2, positionTimestampValue3 + Math.sin(doubleVal3) * var9Var11Var9FloatVal4Valu);
                  }

                  if ((Boolean)this.glowFilledSetting.getValue()) {
                     int intVal3 = local.getRed();
                     int intVal4 = local.getGreen();
                     int intVal5 = local.getBlue();
                     byte byteVal = 32;
                     float var138Value = floatVal2 * 8.0F;

                     for (int index2 = 1; index2 <= byteVal; index2++) {
                        double index2Value = (double)index2 / byteVal;
                        double var211Value = var9Var11Var9FloatVal4Valu * (1.0 - index2Value);
                        if (!(var211Value < 0.05)) {
                           int maxValue = Math.max(1, (int)(intVal2 * (1.0 - index2Value * 0.6) * 0.55));
                           Color colorInst2 = new Color(intVal3, intVal4, intVal5, maxValue);
                           Vec3d[] local3 = new Vec3d[129];

                           for (int index3 = 0; index3 <= 128; index3++) {
                              double doubleVal4 = (Math.PI * 2) * (index3 / 128.0);
                              local3[index3] = new Vec3d(positionTimestampValue + Math.cos(doubleVal4) * var211Value, positionTimestampValue2, positionTimestampValue3 + Math.sin(doubleVal4) * var211Value);
                           }

                           renderRingLine(textStyleUtilValue3, local3, colorInst2, var138Value);
                        }
                     }
                  }

                  if ((Boolean)this.glowModeSetting.getValue()) {
                     int intVal6 = local.getRed();
                     int intVal7 = local.getGreen();
                     int intVal8 = local.getBlue();
                     Color colorInst3 = new Color(intVal6, intVal7, intVal8, Math.max(1, intVal2 / 16));
                     Color colorInst4 = new Color(intVal6, intVal7, intVal8, Math.max(1, intVal2 / 12));
                     Color colorInst5 = new Color(intVal6, intVal7, intVal8, Math.max(1, intVal2 / 9));
                     Color colorInst6 = new Color(intVal6, intVal7, intVal8, Math.max(1, intVal2 / 6));
                     Color colorInst7 = new Color(intVal6, intVal7, intVal8, Math.max(1, intVal2 / 4));
                     Color colorInst8 = new Color(intVal6, intVal7, intVal8, Math.max(1, intVal2 / 2));
                     Color colorInst9 = new Color(intVal6, intVal7, intVal8, Math.min(255, (int)(intVal2 * 1.0F)));
                     Color colorInst10 = new Color(255, 255, 255, Math.min(255, (int)(intVal2 * 1.4F)));
                     renderRingLine(textStyleUtilValue3, local2, colorInst3, floatVal2 * 18.0F);
                     renderRingLine(textStyleUtilValue3, local2, colorInst4, floatVal2 * 14.0F);
                     renderRingLine(textStyleUtilValue3, local2, colorInst5, floatVal2 * 11.0F);
                     renderRingLine(textStyleUtilValue3, local2, colorInst6, floatVal2 * 8.5F);
                     renderRingLine(textStyleUtilValue3, local2, colorInst7, floatVal2 * 6.0F);
                     renderRingLine(textStyleUtilValue3, local2, colorInst8, floatVal2 * 4.0F);
                     renderRingLine(textStyleUtilValue3, local2, colorInst9, floatVal2 * 2.8F);
                     renderRingLine(textStyleUtilValue3, local2, colorInst10, floatVal2 * 1.6F);
                  } else {
                     renderRingLine(textStyleUtilValue3, local2, colorInst, floatVal2);
                  }
               }
            }

            arg.pop();
         }
      }
   }

   public static void renderRingLine(WorldShapeRenderer worldShapeRenderer, Vec3d[] arg, Color color, float floatVal) {

      for (int index = 1; index < arg.length; index++) {
         worldShapeRenderer.drawLine(color, arg[index - 1], arg[index], floatVal);
      }
   }

}
