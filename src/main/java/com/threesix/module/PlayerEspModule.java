package com.threesix.module;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.module.FreecamModule;
import com.threesix.util.TextStyleUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.module.FriendsModule;
import com.threesix.data.EspBoxRenderData;
import com.threesix.internal.ModuleBase;

public final class PlayerEspModule extends ModuleBase {
   public static final double tracerMaxDistance = 150.0;
   public static final double tracerLength = 24.0;
   public static final double tracerThickness = 2.75;
   public final ClientSetting fillAlphaSetting = new ClientSetting("Fill Alpha", 180.0, 0.0, 255.0);
   public final ClientSetting rangeSetting = new ClientSetting("Range", 256.0, 16.0, 512.0);
   public final ClientSetting tracersSetting = new ClientSetting("Tracers", false);
   public final ClientSetting fillColorSetting = new ClientSetting("Fill color", new Color(255, 0, 0));
   public final ClientSetting tracerColorSetting = new ClientSetting("Tracer color", new Color(255, 0, 0));

   public PlayerEspModule() {
      super("Player ESP", ModuleCategory.RENDER);
      this.registerSetting(this.fillAlphaSetting);
      this.registerSetting(this.rangeSetting);
      this.registerSetting(this.tracersSetting);
      this.registerSetting(this.fillColorSetting);
      this.registerSetting(this.tracerColorSetting);
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            double textStyleUtilValue2PosY = textStyleUtilValue2.x;
            double textStyleUtilValue2PosX = textStyleUtilValue2.y;
            double textStyleUtilValue2PosZ = textStyleUtilValue2.z;
            double doubleVal = (Double)this.rangeSetting.getValue() * (Double)this.rangeSetting.getValue();
            int intVal = this.clampAlpha((Double)this.fillAlphaSetting.getValue());
            boolean flag = (Boolean)this.tracersSetting.getValue();
            Color local = this.applyAlphaToColor((Color)this.fillColorSetting.getValue(), intVal);
            Color local2 = flag ? this.applyAlphaToColor((Color)this.tracerColorSetting.getValue(), 255) : null;
            Vec3d local3 = flag ? TextStyleUtil.getForwardVector(textStyleUtilValue) : null;
            Vec3d local4 = flag ? FreecamModule.getFreecamEyePos(textStyleUtilValue2, floatVal) : null;
            Vec3d local5 = flag ? (local4.equals(textStyleUtilValue2) ? local3.multiply(150.0) : local4.subtract(textStyleUtilValue2)) : null;
            ArrayList<EspBoxRenderData> arrayListInst = new ArrayList();

            for (PlayerEntity class1657 : minecraftClient.world.getPlayers()) {
               if (class1657 != minecraftClient.player && class1657.isAlive() && !class1657.isSpectator()) {
                  boolean friendsModuleValue = FriendsModule.isEspColorEnabled() && FriendsModule.isFriend(this.getProfileName(class1657));
                  Vec3d local6 = this.getPlayerRenderPos(class1657, floatVal);
                  double local6PosY = local6.x;
                  double local6PosX = local6.y;
                  double local6PosZ = local6.z;
                  double var25Var5Value = local6PosY - textStyleUtilValue2PosY;
                  double var27Var7Value = local6PosX - textStyleUtilValue2PosX;
                  double var29Var9Value = local6PosZ - textStyleUtilValue2PosZ;
                  double var31Var31Var33Var33Var35V = var25Var5Value * var25Var5Value + var27Var7Value * var27Var7Value + var29Var9Value * var29Var9Value;
                  if (!(var31Var31Var33Var33Var35V > doubleVal)) {
                     Color local7;
                     Color local8;
                     if (friendsModuleValue) {
                        Color friendsModuleValue2 = FriendsModule.getFriendColor();
                        local7 = this.applyAlphaToColor(friendsModuleValue2, intVal);
                        local8 = flag ? this.applyAlphaToColor(friendsModuleValue2, 255) : null;
                     } else {
                        local7 = local;
                        local8 = local2;
                     }

                     double class1657Value = class1657.getWidth() / 2.0;
                     double class1657Value2 = class1657.getHeight();
                     boolean trueSnapshot = true;
                     double var33Class1657Value = var27Var7Value + class1657.getHeight() * 0.5;
                     arrayListInst.add(new EspBoxRenderData(var25Var5Value, var27Var7Value, var29Var9Value, var33Class1657Value, class1657Value, class1657Value2, local7, local8, trueSnapshot));
                  }
               }
            }

            if (!arrayListInst.isEmpty()) {
               arg.push();
               WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);

               for (EspBoxRenderData espBoxRenderData : arrayListInst) {
                  if (espBoxRenderData.isFilled) {
                     textStyleUtilValue3.fillBox(
                        espBoxRenderData.boxX - espBoxRenderData.boxHalfWidth,
                        espBoxRenderData.minY,
                        espBoxRenderData.boxZ - espBoxRenderData.boxHalfWidth,
                        espBoxRenderData.boxX + espBoxRenderData.boxHalfWidth,
                        espBoxRenderData.minY + espBoxRenderData.height,
                        espBoxRenderData.boxZ + espBoxRenderData.boxHalfWidth,
                        espBoxRenderData.fillColor
                     );
                  }
               }

               if (flag) {
                  WorldShapeRenderer textStyleUtilValue4 = TextStyleUtil.acquireRenderer(arg);

                  for (EspBoxRenderData espBoxRenderData2 : arrayListInst) {
                     if (espBoxRenderData2.tracerColor != null) {
                        Vec3d local9 = new Vec3d(espBoxRenderData2.boxX, espBoxRenderData2.centerY, espBoxRenderData2.boxZ);
                        textStyleUtilValue4.drawLine(espBoxRenderData2.tracerColor, local5, local9, ClickGuiModule.getTracerWidth());
                     }
                  }
               }

               arg.pop();
            }
         }
      }
   }

   public int clampAlpha(double doubleVal) {
      int intVal = (int)Math.round(doubleVal);
      if (intVal < 0) {
         return 0;
      } else {
         return intVal > 255 ? 255 : intVal;
      }
   }

   public Color applyAlphaToColor(Color color, int intVal) {
      int maxValue = Math.max(0, Math.min(255, Math.round(color.getAlpha() / 255.0F * intVal)));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), maxValue);
   }

   public Vec3d getPlayerRenderPos(PlayerEntity arg, float floatVal) {
      try {
         return arg.getLerpedPos(floatVal);
      } catch (Throwable error) {
         double class3532Value = MathHelper.lerp(floatVal, arg.lastRenderX, arg.getX());
         double class3532Value2 = MathHelper.lerp(floatVal, arg.lastRenderY, arg.getY());
         double class3532Value3 = MathHelper.lerp(floatVal, arg.lastRenderZ, arg.getZ());
         return new Vec3d(class3532Value, class3532Value2, class3532Value3);
      }
   }

   public String getProfileName(PlayerEntity arg) {
      if (arg == null) {
         return "";
      }

      try {
         GameProfile var1Value = arg.getGameProfile();
         if (var1Value != null) {
            try {
               if (var1Value.getClass().getMethod("getName").invoke(var1Value) instanceof String local && !local.isBlank()) {
                  return local;
               }
            } catch (Throwable error) {
            }

            try {
               if (var1Value.getClass().getMethod("name").invoke(var1Value) instanceof String local2 && !local2.isBlank()) {
                  return local2;
               }
            } catch (Throwable error2) {
            }
         }
      } catch (Throwable error3) {
      }

      return arg.getName().getString();
   }

}
