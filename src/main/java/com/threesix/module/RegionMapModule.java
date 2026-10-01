package com.threesix.module;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.module.HudModule;
import com.threesix.util.GuiRenderUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.data.HudElementType;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.HudPosPair;

public final class RegionMapModule extends ModuleBase {
   public static RegionMapModule field1;
   public final ClientSetting cellSizeSetting = new ClientSetting("Cell Size", 12.0, 8.0, 40.0);
   public final ClientSetting showGridSetting = new ClientSetting("Show Grid", false);
   public final ClientSetting showPlayerSetting = new ClientSetting("Show Player", true);
   public final ClientSetting alphaSetting = new ClientSetting("Alpha", 140, 0, 255);
   private static final int gridSize = 9;
   private static final double regionBlockSpan = 50000.0;
   private static final double worldMinCoord = 225000.0;
   public static final float cellTextScale = 8.0F;
   public static final float mapTextScale = 5.0F;
   public static final int gridLineWidth = 8;
   public static final int legendPadding = 22;
   public static final int mapWidth = 17;
   public static final int mapHeight = 19;
   public static final int cellPadding = 2;
   public static final int borderWidth = 3;
   public static final int legendRowHeight = 12;
   public static final int legendSwatchSize = 10;
   public static final int hudLayoutCellSize = 12;
   private static final int[][] hudLayoutCells = new int[][]{
      {82, 5},
      {100, 3},
      {101, 3},
      {102, 3},
      {103, 2},
      {104, 2},
      {105, 2},
      {106, 2},
      {91, 2},
      {83, 5},
      {44, 3},
      {75, 3},
      {42, 3},
      {41, 2},
      {40, 2},
      {39, 2},
      {38, 2},
      {92, 2},
      {84, 5},
      {45, 3},
      {14, 3},
      {13, 3},
      {12, 2},
      {11, 2},
      {10, 2},
      {37, 2},
      {93, 2},
      {85, 5},
      {46, 5},
      {74, 5},
      {3, 3},
      {2, 2},
      {1, 2},
      {25, 2},
      {36, 2},
      {94, 2},
      {86, 4},
      {47, 4},
      {72, 4},
      {71, 4},
      {5, 2},
      {4, 2},
      {24, 2},
      {35, 2},
      {95, 2},
      {87, 4},
      {51, 1},
      {17, 1},
      {9, 0},
      {8, 0},
      {7, 0},
      {23, 0},
      {34, 0},
      {96, 2},
      {88, 4},
      {54, 1},
      {18, 1},
      {61, 0},
      {62, 0},
      {21, 0},
      {22, 0},
      {33, 0},
      {97, 0},
      {89, 0},
      {26, 1},
      {27, 0},
      {28, 0},
      {29, 0},
      {30, 0},
      {59, 0},
      {32, 0},
      {98, 0},
      {90, 0},
      {107, 1},
      {108, 1},
      {109, 1},
      {110, 1},
      {111, 1},
      {112, 1},
      {113, 1},
      {99, 0}
   };
   private static final String[] regionNames = new String[]{"EU C", "EU W", "NA E", "NA W", "Asia", "Oceania"};
   private static final Color[] serverTypeColors = new Color[]{
      new Color(159, 206, 99), new Color(0, 166, 99), new Color(79, 173, 234), new Color(47, 110, 186), new Color(245, 194, 66), new Color(252, 136, 3)
   };
   private static final Color knownGroupColor = new Color(168, 85, 247);
   private static final Color rivalGroupColor = new Color(239, 68, 68);
   private static final Color allyGroupColor = new Color(34, 197, 94);
   private static final Color unknownGroupColor = new Color(0, 0, 0);
   private static final Set knownGroupIds = Set.of(89, 90, 101, 103, 104);
   private static final Set rivalGroupIds = Set.of(2, 7, 86, 88, 28, 29, 109, 110, 59, 32, 98, 112, 113, 99, 34);
   private static final Set allyGroupIds = Set.of(5, 4, 8, 30);
   private static final String[] legendLabels = new String[]{"glitched (no rtp)", "good for base", "good for media/baltop"};
   private static final Color[] legendColors = new Color[]{knownGroupColor, rivalGroupColor, allyGroupColor};
   private final Map hudCellPositions = new HashMap();
   private static final Map textWidthCache = new ConcurrentHashMap();

   public RegionMapModule() {
      super("Region Map", ModuleCategory.RENDER);
      this.registerSetting(this.cellSizeSetting);
      this.registerSetting(this.showGridSetting);
      this.registerSetting(this.showPlayerSetting);
      this.registerSetting(this.alphaSetting);

      for (int index = 0; index < hudLayoutCells.length; index++) {
         this.hudCellPositions.put(index, new HudPosPair(hudLayoutCells[index][0], hudLayoutCells[index][1]));
      }

      field1 = this;
   }

   public void call1(DrawContext arg, MinecraftClient arg2) {
      if (this.isEnabled()) {
         if (arg2.player != null && arg2.world != null) {
            try {
               int[] hudModuleValue = HudModule.getSavedElementPos(HudElementType.REGION_MAP);
               float hudModuleValue2 = HudModule.getScaledFactor(HudElementType.REGION_MAP);
               int maxValue = Math.max(1, (int)Math.round((Double)this.cellSizeSetting.getValue()));
               int intVal = 9 * maxValue;
               int clickGuiModuleValue = ClickGuiModule.getAccentColorArgb();
               arg.getMatrices().pushMatrix();
               arg.getMatrices().scale(hudModuleValue2, hudModuleValue2);
               int intVal2 = hudModuleValue[0];
               int intVal3 = hudModuleValue[1];
               int[] local = this.getMapSize();
               boolean falseSnapshot = false;
               float floatVal = 2.0F;

               try {
                  falseSnapshot = HudModule.instance != null && (Boolean)HudModule.instance.rainbowSetting.getValue();
               } catch (Throwable error) {
               }

               try {
                  floatVal = (Float)HudModule.instance.rainbowSpeedSetting.getValue();
               } catch (Throwable error2) {
               }

               int intVal4 = falseSnapshot ? HudModule.rainbowColorAt(floatVal, 0).getRGB() : clickGuiModuleValue;
               GuiRenderUtil.fillRoundedRect(arg, intVal2, intVal3, local[0], local[1], 2.0F, ClickGuiModule.getBackgroundColorArgb(), false);
               GuiRenderUtil.strokeRoundedRect(arg, intVal2, intVal3, local[0], local[1], 2.0F, 1.0F, intVal4, false);
               int var88Value = intVal2 + 8;
               int var98Value = intVal3 + 8;

               for (int index = 0; index < 81; index++) {
                  HudPosPair local2 = (HudPosPair)this.hudCellPositions.get(index);
                  if (local2 != null) {
                     int intVal5 = index % 9;
                     int var169Value = index / 9;
                     int var14Var18Var5Value = var88Value + intVal5 * maxValue;
                     int var15Var19Var5Value = var98Value + var169Value * maxValue;
                     Color local3 = this.getGroupColor(local2.regionId);
                     int maxValue2 = Math.max(0, Math.min(255, (Integer)this.alphaSetting.getValue()));
                     arg.fill(var14Var18Var5Value, var15Var19Var5Value, var14Var18Var5Value + maxValue, var15Var19Var5Value + maxValue, new Color(local3.getRed(), local3.getGreen(), local3.getBlue(), maxValue2).getRGB());
                  }
               }

               if ((Boolean)this.showGridSetting.getValue()) {
                  int intVal6 = withAlpha(-1, 0.25F);

                  for (int index2 = 0; index2 <= 9; index2++) {
                     arg.fill(var88Value + index2 * maxValue, var98Value, var88Value + index2 * maxValue + 1, var98Value + intVal, intVal6);
                     arg.fill(var88Value, var98Value + index2 * maxValue, var88Value + intVal, var98Value + index2 * maxValue + 1, intVal6);
                  }
               }

               for (int index3 = 0; index3 < 81; index3++) {
                  HudPosPair local4 = (HudPosPair)this.hudCellPositions.get(index3);
                  if (local4 != null) {
                     int intVal7 = index3 % 9;
                     int var339Value = index3 / 9;
                     int var14Var38Var5Value = var88Value + intVal7 * maxValue;
                     int var15Var40Var5Value = var98Value + var339Value * maxValue;
                     String stringValue = String.valueOf(local4.regionId);
                     int intVal8 = getTextWidth(stringValue);
                     if (intVal8 > maxValue - 2 && intVal8 > 0) {
                        float floatVal2 = (float)(maxValue - 2) / intVal8;
                        float var41Var52Value = var14Var38Var5Value + maxValue / 2.0F;
                        float var42Var52Value = var15Var40Var5Value + maxValue / 2.0F;
                        arg.getMatrices().pushMatrix();
                        arg.getMatrices().scale(floatVal2, floatVal2);
                        float var26Var454Value = var42Var52Value / floatVal2 - 4.0F - 1.0F;
                        float var46Var45Var442Value = var41Var52Value / floatVal2 - intVal8 / 2.0F;
                        HudModule.instance.call1(arg, stringValue, var46Var45Var442Value, var26Var454Value, -1);
                        HudModule.instance.call1(arg, stringValue, var46Var45Var442Value, var26Var454Value, -1);
                        arg.getMatrices().popMatrix();
                     } else {
                        int var41Var5Var442Value = var14Var38Var5Value + (maxValue - intVal8) / 2;
                        int var42Var5821Value = var15Var40Var5Value + (maxValue - 8) / 2 - 1;
                        HudModule.instance.call1(arg, stringValue, var41Var5Var442Value, var42Var5821Value, -1);
                        HudModule.instance.call1(arg, stringValue, var41Var5Var442Value, var42Var5821Value, -1);
                     }
                  }
               }

               if ((Boolean)this.showPlayerSetting.getValue()) {
                  this.renderPlayerMarker(arg, arg2, var88Value, var98Value, maxValue, clickGuiModuleValue);
               }

               int var15Var64Value = var98Value + intVal + 4;

               for (int index4 = 0; index4 < legendLabels.length; index4++) {
                  int var34Var3712Value = var15Var64Value + index4 * 12;
                  arg.fill(var88Value, var34Var3712Value, var88Value + 10, var34Var3712Value + 10, legendColors[index4].getRGB());
                  HudModule.instance.call1(arg, legendLabels[index4], var88Value + 10 + 3, var34Var3712Value + 2, -1);
               }

               arg.getMatrices().popMatrix();
            } catch (Exception error3) {
            }
         }
      }
   }

   public void renderPlayerMarker(DrawContext arg, MinecraftClient arg2, int intVal, int intVal2, int intVal3, int intVal4) {
      double arg2Value = arg2.player.getX();
      double arg2Value2 = arg2.player.getZ();
      int[] local = this.getCellForCoords(arg2Value, arg2Value2);
      if (local[0] >= 0 && local[0] < 9 && local[1] >= 0 && local[1] < 9) {
         double[] local2 = this.getCellOffset(arg2Value, arg2Value2);
         int var3Var110Var51Value = intVal + local[0] * intVal3 + 1;
         int var4Var111Var51Value = intVal2 + local[1] * intVal3 + 1;
         int var13Var52Value = var3Var110Var51Value + intVal3 - 2;
         int var14Var52Value = var4Var111Var51Value + intVal3 - 2;
         int maxValue = Math.max(var3Var110Var51Value + 3, Math.min(var13Var52Value - 3, (int)(intVal + local[0] * intVal3 + local2[0] * intVal3)));
         int maxValue2 = Math.max(var4Var111Var51Value + 3, Math.min(var14Var52Value - 3, (int)(intVal2 + local[1] * intVal3 + local2[1] * intVal3)));
         arg.fill(maxValue - 2, maxValue2 - 2, maxValue + 3, maxValue2 + 3, -1);
         arg.fill(maxValue - 1, maxValue2 - 1, maxValue + 2, maxValue2 + 2, intVal4 | 0xFF000000);
         double toRadiansValue = Math.toRadians(arg2.player.getYaw());
         int maxValue3 = Math.max(var3Var110Var51Value, Math.min(var13Var52Value, maxValue + (int)(-Math.sin(toRadiansValue) * 6.0)));
         int maxValue4 = Math.max(var4Var111Var51Value, Math.min(var14Var52Value, maxValue2 + (int)(Math.cos(toRadiansValue) * 6.0)));
         arg.fill(maxValue3 - 1, maxValue4 - 1, maxValue3 + 2, maxValue4 + 2, -65536);
      }
   }

   public int[] getMapSize() {
      int maxValue = Math.max(1, (int)Math.round((Double)this.cellSizeSetting.getValue()));
      int intVal = 9 * maxValue + 16;

      try {

         int maxValue2 = 0;
         for (String string : legendLabels) {
            maxValue2 = Math.max(maxValue2, getTextWidth(string));
         }

         intVal = Math.max(intVal, maxValue2 + 10 + 3 + 16);
      } catch (Throwable error) {
      }

      int intVal2 = 9 * maxValue + 8 + 4 + legendLabels.length * 12 + 8;
      return new int[]{intVal, intVal2};
   }

   public Color getGroupColor(int intVal) {
      if (knownGroupIds.contains(intVal)) {
         return knownGroupColor;
      } else if (rivalGroupIds.contains(intVal)) {
         return rivalGroupColor;
      } else {
         return allyGroupIds.contains(intVal) ? allyGroupColor : unknownGroupColor;
      }
   }

   public int getGridSize() {

      return 9;
   }

   public Color getServerTypeColor(int intVal) {

      return intVal >= 0 && intVal < serverTypeColors.length ? serverTypeColors[intVal] : serverTypeColors[0];
   }

   public int[] getCellForCoords(double doubleVal, double doubleVal2) {

      return new int[]{(int)Math.floor((doubleVal + 225000.0) / 50000.0), (int)Math.floor((doubleVal2 + 225000.0) / 50000.0)};
   }

   public double[] getCellOffset(double doubleVal, double doubleVal2) {
      double doubleVal3 = (doubleVal + 225000.0) % 50000.0;
      double doubleVal4 = (doubleVal2 + 225000.0) % 50000.0;
      if (doubleVal3 < 0.0) {
         doubleVal3 += 50000.0;
      }

      if (doubleVal4 < 0.0) {
         doubleVal4 += 50000.0;
      }

      return new double[]{doubleVal3 / 50000.0, doubleVal4 / 50000.0};
   }

   public int getRegionIdAt(double doubleVal, double doubleVal2) {
      int[] local = this.getCellForCoords(doubleVal, doubleVal2);
      if (local[0] >= 0 && local[0] < 9 && local[1] >= 0 && local[1] < 9) {
         HudPosPair local2 = (HudPosPair)this.hudCellPositions.get(local[1] * 9 + local[0]);
         return local2 != null ? local2.regionId : -1;
      } else {
         return -1;
      }
   }

   public String getRegionNameAt(double doubleVal, double doubleVal2) {
      int[] local = this.getCellForCoords(doubleVal, doubleVal2);
      if (local[0] >= 0 && local[0] < 9 && local[1] >= 0 && local[1] < 9) {
         HudPosPair local2 = (HudPosPair)this.hudCellPositions.get(local[1] * 9 + local[0]);
         return local2 != null ? regionNames[local2.y] : "Unknown";
      } else {
         return "Unknown";
      }
   }

   public String[] getRegionNames() {
      return regionNames;
   }

   public Color[] getServerTypeColors() {
      return serverTypeColors;
   }

   private static int getTextWidth(String string) {
      Integer local = (Integer)textWidthCache.get(string);
      if (local == null) {
         local = HudModule.instance.call2(string);
         textWidthCache.put(string, local);
      }

      return local;
   }

   private static float drawTextAdvance(DrawContext arg, String string, float floatVal, int intVal, int intVal2) {
      HudModule.instance.call1(arg, string, floatVal, intVal, intVal2);
      return floatVal + getTextWidth(string);
   }

   private static float drawNumberedText(DrawContext arg, String string, float floatVal, int intVal, int intVal2) {

      if (string.startsWith("-")) {
         String local = string.substring(1);
         HudModule.drawNegativeSign(arg, floatVal, intVal, intVal2);
         HudModule.instance.call1(arg, local, floatVal + 7.0F, intVal, intVal2);
         return floatVal + 7.0F + getTextWidth(local);
      } else {
         HudModule.instance.call1(arg, string, floatVal, intVal, intVal2);
         return floatVal + getTextWidth(string);
      }
   }

   public static int withAlpha(int intVal, float floatVal) {
      int maxValue = Math.max(0, Math.min(255, Math.round(floatVal * 255.0F)));
      return intVal & 16777215 | maxValue << 24;
   }

   public static int brightenColor(int intVal) {
      int intVal2 = intVal >> 24 & 0xFF;
      int minValue = Math.min(255, (intVal >> 16 & 0xFF) + 10);
      int minValue2 = Math.min(255, (intVal >> 8 & 0xFF) + 10);
      int minValue3 = Math.min(255, (intVal & 0xFF) + 10);
      return intVal2 << 24 | minValue << 16 | minValue2 << 8 | minValue3;
   }

}
