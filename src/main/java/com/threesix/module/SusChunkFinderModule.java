package com.threesix.module;

import java.awt.Color;
import java.util.List;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.CaveVines;
import net.minecraft.util.math.BlockPos.Mutable;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.module.HudModule;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.render.EspRenderPipelines;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.ChunkClusterRecord;
import com.threesix.service.SusChunkScanner;
import com.threesix.util.ChunkSectionCache;

public final class SusChunkFinderModule extends ModuleBase {
   public final ClientSetting simDistanceSetting = new ClientSetting("Sim Distance", 5, 1, 16);
   public final ClientSetting sensitivitySetting = new ClientSetting("Sensitivity", 1, 1, 20);
   public final ClientSetting mergeRadiusSetting = new ClientSetting("Merge Radius", 3, 1, 8);
   public final ClientSetting colorSetting = new ClientSetting("Color", new Color(255, 31, 31, 200));
   public final ClientSetting alphaSetting = new ClientSetting("Alpha", 200, 0, 255);
   public final ClientSetting smartModeSetting = new ClientSetting("Smart Mode", false);
   private static final int chunkScanDistance = 62;
   private static final int mergeSearchSteps = 4;
   private static final double markerScale = 0.16;
   public static final ChunkSectionCache field1 = new ChunkSectionCache();
   private final SusChunkScanner scanner = new SusChunkScanner(this);

   public SusChunkFinderModule() {
      super("Sus Chunk Finder", ModuleCategory.BASEFINDING);
      this.registerSetting(this.simDistanceSetting);
      this.registerSetting(this.sensitivitySetting);
      this.registerSetting(this.mergeRadiusSetting);
      this.registerSetting(this.colorSetting);
      this.registerSetting(this.alphaSetting);
      this.registerSetting(this.smartModeSetting);
      try {
         java.lang.reflect.Field scannerField = SusChunkScanner.class.getDeclaredField("newChunkKeys");
         scannerField.setAccessible(true);
         java.util.Set<Long> chunkKeys = (java.util.Set<Long>)scannerField.get(this.scanner);
         field1.addChunkListener(chunkKeys::add);
      } catch (ReflectiveOperationException error) {
      }
   }

   @Override
   public void onEnable() {
      this.scanner.call2();
   }

   @Override
   public void onDisable() {

      this.scanner.call2();
   }

   @Override
   public void onTick() {
      this.scanner.call3();
   }

   public List call1() {
      try {
         return this.scanner.call1();
      } catch (Throwable error) {
         return List.of();
      }
   }

   public int call2() {
      try {
         return this.scanner.getMinScore();
      } catch (Throwable error) {
         return 9;
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            double doubleVal = 62.0;
            double var50Value = doubleVal - 0.08 - textStyleUtilValue2.y;
            double var50Value2 = doubleVal + 0.08 - textStyleUtilValue2.y;
            Color local = (Color)this.colorSetting.getValue();
            int intVal = (Integer)this.alphaSetting.getValue();
            if (intVal != local.getAlpha()) {
               local = new Color(local.getRed(), local.getGreen(), local.getBlue(), intVal);
            }

            arg.push();
            GL11.glDisable(2929);

            try {
               WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);
               boolean flag = (Boolean)this.smartModeSetting.getValue();
               long systemValue = System.currentTimeMillis();
               float floatVal2 = (float)(systemValue % 4000L) / 4000.0F * 360.0F;
               Color hudModuleValue = HudModule.rainbowColorAt(2.0F, 0);

               for (ChunkClusterRecord chunkClusterRecord : this.scanner.call1()) {
                  if (!flag) {
                     for (long long2 : chunkClusterRecord.members()) {
                        int class1923Value = ChunkPos.getPackedX(long2);
                        int class1923Value2 = ChunkPos.getPackedZ(long2);
                        double class1923Value16Value = class1923Value * 16.0 - textStyleUtilValue2.x;
                        double class1923Value216Value = class1923Value2 * 16.0 - textStyleUtilValue2.z;
                        textStyleUtilValue3.fillBox(class1923Value16Value, var50Value, class1923Value216Value, class1923Value16Value + 16.0, var50Value2, class1923Value216Value + 16.0, local);
                     }
                  } else {
                     byte byteVal = 16;
                     double byteVal2Value = byteVal / 2.0;
                     double chunkClusterRecordValue = chunkClusterRecord.centroidX() - textStyleUtilValue2.x;
                     double chunkClusterRecordValue2 = chunkClusterRecord.centroidZ() - textStyleUtilValue2.z;
                     textStyleUtilValue3.fillBox(chunkClusterRecordValue - byteVal2Value, var50Value, chunkClusterRecordValue2 - byteVal2Value, chunkClusterRecordValue + byteVal2Value, var50Value2, chunkClusterRecordValue2 + byteVal2Value, local);
                     double doubleVal2 = (var50Value + var50Value2) / 2.0;
                     drawMarker(textStyleUtilValue3, chunkClusterRecordValue, doubleVal2, chunkClusterRecordValue2, floatVal2, textStyleUtilValue2.y < doubleVal, hudModuleValue, byteVal, 1.5);
                     float floatVal3 = (float)(0.5 + 0.5 * Math.sin(systemValue / 350.0));
                     Color colorInst = new Color(hudModuleValue.getRed(), hudModuleValue.getGreen(), hudModuleValue.getBlue(), 110 + (int)(145.0F * floatVal3));
                     Vec3d local2 = new Vec3d(chunkClusterRecordValue - byteVal2Value, doubleVal2, chunkClusterRecordValue2 - byteVal2Value);
                     Vec3d local3 = new Vec3d(chunkClusterRecordValue + byteVal2Value, doubleVal2, chunkClusterRecordValue2 - byteVal2Value);
                     Vec3d local4 = new Vec3d(chunkClusterRecordValue + byteVal2Value, doubleVal2, chunkClusterRecordValue2 + byteVal2Value);
                     Vec3d local5 = new Vec3d(chunkClusterRecordValue - byteVal2Value, doubleVal2, chunkClusterRecordValue2 + byteVal2Value);
                     textStyleUtilValue3.drawMarkerBox(colorInst, local2, local3, 2.0F);
                     textStyleUtilValue3.drawMarkerBox(colorInst, local3, local4, 2.0F);
                     textStyleUtilValue3.drawMarkerBox(colorInst, local4, local5, 2.0F);
                     textStyleUtilValue3.drawMarkerBox(colorInst, local5, local2, 2.0F);
                  }
               }
            } finally {
               GL11.glEnable(2929);
            }

            arg.pop();
         }
      }
   }

   private static void drawMarker(WorldShapeRenderer worldShapeRenderer, double doubleVal, double doubleVal2, double doubleVal3, float floatVal, boolean flag, Color color, int intVal, double doubleVal4) {
      double doubleVal5 = flag ? -1.0 : 1.0;
      drawMarkerEdge(worldShapeRenderer, doubleVal, doubleVal2, doubleVal3, floatVal, doubleVal5, color, doubleVal4);
   }

   private static void drawMarkerEdge(WorldShapeRenderer worldShapeRenderer, double doubleVal, double doubleVal2, double doubleVal3, float floatVal, double doubleVal4, Color color, double doubleVal5) {
      double toRadiansValue = Math.toRadians(floatVal);
      double cosValue = Math.cos(toRadiansValue);
      double sinValue = Math.sin(toRadiansValue);
      double[] local2 = new double[]{-doubleVal5, doubleVal5, doubleVal5, -doubleVal5};
      double[] local3 = new double[]{-doubleVal5, -doubleVal5, doubleVal5, doubleVal5};
      Vec3d[] local = new Vec3d[4];

      for (int index = 0; index < 4; index++) {
         double doubleVal4Var19IndexValue = doubleVal4 * local2[index];
         double doubleVal6 = local3[index];
         local[index] = new Vec3d(doubleVal + doubleVal4Var19IndexValue * cosValue + doubleVal6 * sinValue, doubleVal2, doubleVal3 + doubleVal4Var19IndexValue * sinValue - doubleVal6 * cosValue);
      }

      worldShapeRenderer.drawMarkerText(color, local[0], local[1], local[2], local[3], EspRenderPipelines.getTextPipeline());
   }

   private static void drawMarkerLines(WorldShapeRenderer worldShapeRenderer, double doubleVal, double doubleVal2, double doubleVal3, float floatVal, Color color, double doubleVal4, double doubleVal5, float floatVal2) {
      double[] local = new double[8];
      double[] local2 = new double[8];

      for (int index = 0; index < 8; index++) {
         double toRadiansValue = Math.toRadians(floatVal + index * 45.0);
         double doubleVal6 = index % 2 == 0 ? doubleVal4 : doubleVal5;
         local[index] = doubleVal + Math.cos(toRadiansValue) * doubleVal6;
         local2[index] = doubleVal3 - Math.sin(toRadiansValue) * doubleVal6;
      }

      for (int index2 = 0; index2 < 8; index2++) {
         int intVal = (index2 + 1) % 8;
         worldShapeRenderer.drawLine(color, new Vec3d(local[index2], doubleVal2, local2[index2]), new Vec3d(local[intVal], doubleVal2, local2[intVal]), floatVal2);
      }
   }

   static boolean isTargetBlock(BlockState arg) {
      return arg.isOf(Blocks.CAVE_VINES_PLANT)
         || arg.isOf(Blocks.CAVE_VINES) && arg.contains(CaveVines.BERRIES) && (Boolean)arg.get(CaveVines.BERRIES)
         || arg.isOf(Blocks.GLOW_LICHEN)
         || arg.isOf(Blocks.SEA_PICKLE)
         || arg.isOf(Blocks.GLOWSTONE)
         || arg.isOf(Blocks.SEA_LANTERN)
         || arg.isOf(Blocks.SHROOMLIGHT)
         || arg.isOf(Blocks.JACK_O_LANTERN)
         || arg.isOf(Blocks.MAGMA_BLOCK)
         || arg.isOf(Blocks.LAVA);
   }

   static boolean isChunkLoaded(MinecraftClient arg, BlockPos arg2, Mutable arg3, int intVal) {

      for (int index = -intVal; index <= intVal; index++) {
         for (int index2 = -intVal; index2 <= intVal; index2++) {
            for (int index3 = -intVal; index3 <= intVal; index3++) {
               if (index != 0 || index2 != 0 || index3 != 0) {
                  arg3.set(arg2.getX() + index, arg2.getY() + index2, arg2.getZ() + index3);
                  if (isTargetBlock(arg.world.getBlockState(arg3))) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static double getHeightJitter(double doubleVal, double doubleVal2, int intVal) {

      long longVal = (long)(doubleVal * 1000.0) * 73856093L ^ (long)(doubleVal2 * 1000.0) * 19349663L ^ intVal * 83492791L;
      longVal ^= longVal >>> 16;
      return ((int)(longVal & 31L) - 15) / 31.0 * 16.0;
   }

}
