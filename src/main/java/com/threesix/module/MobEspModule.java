package com.threesix.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.setting.EntityListSetting;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.EntityBoxRenderData;

public final class MobEspModule extends ModuleBase {
   public static final double tracerMaxDistance = 150.0;
   public static final double tracerLength = 24.0;
   public static final double tracerThickness = 2.75;
   public final EntityListSetting mobsSetting = new EntityListSetting("Mobs");
   public final ClientSetting alphaSetting = new ClientSetting("Alpha", 100.0, 0.0, 255.0);
   public final ClientSetting rangeSetting = new ClientSetting("Range", 128.0, 16.0, 512.0);
   public final ClientSetting tracersSetting = new ClientSetting("Tracers", false);
   public final ClientSetting outlineColorSetting = new ClientSetting("Outline color", new Color(255, 80, 80));
   public final ClientSetting fillColorSetting = new ClientSetting("Fill color", new Color(255, 80, 80));
   public final ClientSetting tracerColorSetting = new ClientSetting("Tracer color", new Color(255, 80, 80));

   public MobEspModule() {
      super("Mob ESP", ModuleCategory.RENDER);
      this.registerSetting(this.mobsSetting);
      this.registerSetting(this.alphaSetting);
      this.registerSetting(this.rangeSetting);
      this.registerSetting(this.tracersSetting);
      this.registerSetting(this.outlineColorSetting);
      this.registerSetting(this.fillColorSetting);
      this.registerSetting(this.tracerColorSetting);
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         Set local = this.mobsSetting.getSelectedEntities();
         if (!local.isEmpty()) {
            Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
            if (textStyleUtilValue != null) {
               Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
               double textStyleUtilValue2PosY = textStyleUtilValue2.x;
               double textStyleUtilValue2PosX = textStyleUtilValue2.y;
               double textStyleUtilValue2PosZ = textStyleUtilValue2.z;
               double doubleVal = (Double)this.rangeSetting.getValue() * (Double)this.rangeSetting.getValue();
               int intVal = this.clampAlpha((Double)this.alphaSetting.getValue());
               boolean flag = (Boolean)this.tracersSetting.getValue();
               Color local2 = this.applyAlphaToColor((Color)this.outlineColorSetting.getValue(), intVal);
               Color local3 = this.applyAlphaToColor((Color)this.fillColorSetting.getValue(), Math.max(0, intVal / 3));
               Color local4 = flag ? this.applyAlphaToColor((Color)this.tracerColorSetting.getValue(), 255) : null;
               Vec3d local5 = flag ? TextStyleUtil.getForwardVector(textStyleUtilValue) : null;
               Vec3d local6 = flag ? TextStyleUtil.getRightVector(textStyleUtilValue) : null;
               Vec3d local7 = flag ? TextStyleUtil.cross(local5, local6) : null;
               Vec3d local8 = flag ? local5.multiply(150.0) : null;
               ArrayList<EntityBoxRenderData> arrayListInst = new ArrayList();

               for (Entity class1297 : minecraftClient.world.getEntities()) {
                  if (class1297 != minecraftClient.player
                     && !(class1297 instanceof PlayerEntity)
                     && class1297 instanceof LivingEntity
                     && class1297.isAlive()
                     && local.contains(class1297.getType())) {
                     Vec3d local9 = this.getEntityRenderPos(class1297, floatVal);
                     double local9Value = local9.x - textStyleUtilValue2PosY;
                     double local9Value2 = local9.y - textStyleUtilValue2PosX;
                     double local9Value3 = local9.z - textStyleUtilValue2PosZ;
                     double var27Var27Var29Var29Var31V = local9Value * local9Value + local9Value2 * local9Value2 + local9Value3 * local9Value3;
                     if (!(var27Var27Var29Var29Var31V > doubleVal)) {
                        double class1297Value = class1297.getWidth() / 2.0;
                        double class1297Value2 = class1297.getHeight();
                        double var29Var370Value = local9Value2 + class1297Value2 * 0.5;
                        arrayListInst.add(new EntityBoxRenderData(local9Value, local9Value2, local9Value3, var29Var370Value, class1297Value, class1297Value2));
                     }
                  }
               }

               if (!arrayListInst.isEmpty()) {
                  arg.push();
                  WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);

                  for (EntityBoxRenderData entityBoxRenderData : arrayListInst) {
                     textStyleUtilValue3.strokeBox(
                        entityBoxRenderData.boxX - entityBoxRenderData.boxHalfWidth,
                        entityBoxRenderData.boxBottomY,
                        entityBoxRenderData.boxZ - entityBoxRenderData.boxHalfWidth,
                        entityBoxRenderData.boxX + entityBoxRenderData.boxHalfWidth,
                        entityBoxRenderData.boxBottomY + entityBoxRenderData.height,
                        entityBoxRenderData.boxZ + entityBoxRenderData.boxHalfWidth,
                        local2
                     );
                     textStyleUtilValue3.fillBox(
                        entityBoxRenderData.boxX - entityBoxRenderData.boxHalfWidth,
                        entityBoxRenderData.boxBottomY,
                        entityBoxRenderData.boxZ - entityBoxRenderData.boxHalfWidth,
                        entityBoxRenderData.boxX + entityBoxRenderData.boxHalfWidth,
                        entityBoxRenderData.boxBottomY + entityBoxRenderData.height,
                        entityBoxRenderData.boxZ + entityBoxRenderData.boxHalfWidth,
                        local3
                     );
                  }

                  if (flag) {
                     WorldShapeRenderer textStyleUtilValue4 = TextStyleUtil.acquireRenderer(arg);

                     for (EntityBoxRenderData entityBoxRenderData2 : arrayListInst) {
                        Vec3d textStyleUtilValue5 = TextStyleUtil.buildBillboardMatrix(entityBoxRenderData2.boxX, entityBoxRenderData2.tracerY, entityBoxRenderData2.boxZ, local5, local6, local7, 24.0, 2.75);
                        textStyleUtilValue4.drawLine(local4, local8, textStyleUtilValue5, ClickGuiModule.getTracerWidth());
                     }
                  }

                  arg.pop();
               }
            }
         }
      }
   }

   public int clampAlpha(double doubleVal) {
      int intVal = (int)Math.round(doubleVal);
      return Math.max(0, Math.min(255, intVal));
   }

   public Color applyAlphaToColor(Color color, int intVal) {
      int maxValue = Math.max(0, Math.min(255, Math.round(color.getAlpha() / 255.0F * intVal)));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), maxValue);
   }

   public Vec3d getEntityRenderPos(Entity arg, float floatVal) {
      try {
         return arg.getLerpedPos(floatVal);
      } catch (Throwable error) {
         double class3532Value = MathHelper.lerp(floatVal, arg.lastRenderX, arg.getX());
         double class3532Value2 = MathHelper.lerp(floatVal, arg.lastRenderY, arg.getY());
         double class3532Value3 = MathHelper.lerp(floatVal, arg.lastRenderZ, arg.getZ());
         return new Vec3d(class3532Value, class3532Value2, class3532Value3);
      }
   }

}
