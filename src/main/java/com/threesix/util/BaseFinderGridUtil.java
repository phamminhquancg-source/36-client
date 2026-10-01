package com.threesix.util;

import java.awt.Color;
import com.threesix.util.XorBitUtils;
import com.threesix.data.GridRegionCoord;
import com.threesix.util.StringVaultDecoder;

public final class BaseFinderGridUtil {
   public static final int GRID_SIZE2 = 6;
   public static final int REGION_SIZE = 512;
   public static final int REGION_TYPE_COUNT = 5;
   public static final String[] REGION_NAMES = new String[]{"Wilderness", "Spawner", "Temple", "Village", "Stronghold"};
   public static final Color[] REGION_COLORS = new Color[]{
      new Color(60, 80, 60), new Color(120, 60, 60), new Color(140, 120, 50), new Color(70, 100, 130), new Color(110, 70, 130)
   };

   public int getGridSize() {
      return 6;
   }

   public GridRegionCoord getRegionAtIndex(int intVal) {
      if (intVal >= 0 && intVal < 36) {
         int var16Value = intVal / 6;
         int intVal2 = intVal % 6;
         int intVal3 = (intVal2 - 3) * 512;
         int intVal4 = (var16Value - 3) * 512;
         int intVal5 = this.pickRegionType(intVal3, intVal4);
         return intVal5 < 0 ? null : new GridRegionCoord(this.hashCoords(intVal3, intVal4), intVal5, var16Value, intVal2);
      } else {
         return null;
      }
   }

   public Color getColorForType(int intVal) {
      return intVal >= 0 && intVal < REGION_COLORS.length ? REGION_COLORS[intVal] : REGION_COLORS[0];
   }

   public int[] getGridCoords(double doubleVal, double doubleVal2) {
      int intVal = (int)Math.floor(doubleVal / 512.0) + 3;
      int intVal2 = (int)Math.floor(doubleVal2 / 512.0) + 3;
      return new int[]{intVal, intVal2};
   }

   public double[] getLocalCoords(double doubleVal, double doubleVal2) {
      double doubleVal3 = doubleVal % 512.0;
      double doubleVal4 = doubleVal2 % 512.0;
      if (doubleVal3 < 0.0) {
         doubleVal3 += 512.0;
      }

      if (doubleVal4 < 0.0) {
         doubleVal4 += 512.0;
      }

      return new double[]{doubleVal3 / 512.0, doubleVal4 / 512.0};
   }

   public int getRegionTypeAt(double doubleVal, double doubleVal2) {
      int[] local = this.getGridCoords(doubleVal, doubleVal2);
      return local[0] >= 0 && local[0] < 6 && local[1] >= 0 && local[1] < 6 ? this.hashCoords((local[0] - 3) * 512, (local[1] - 3) * 512) : -1;
   }

   public String getRegionNameAt(double doubleVal, double doubleVal2) {
      int intVal = this.pickRegionType((int)Math.floor(doubleVal / 512.0) * 512, (int)Math.floor(doubleVal2 / 512.0) * 512);
      return intVal >= 0 && intVal < REGION_NAMES.length ? REGION_NAMES[intVal] : REGION_NAMES[0];
   }

   public String[] getRegionNames() {
      return REGION_NAMES;
   }

   public Color[] getRegionColors() {
      return REGION_COLORS;
   }

   private int hashCoords(int intVal, int intVal2) {
      return intVal >> 4 ^ intVal2 << 8 & 65535;
   }

   private int pickRegionType(int intVal, int intVal2) {
      int absValue = Math.abs(intVal * 31 + intVal2 * 17 + intVal * intVal2) % 101;
      if (absValue < 8) {
         return 1;
      } else if (absValue < 15) {
         return 2;
      } else if (absValue < 22) {
         return 3;
      } else {
         return absValue < 28 ? 4 : 0;
      }
   }

}
