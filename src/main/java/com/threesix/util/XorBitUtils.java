package com.threesix.util;

public final class XorBitUtils {
   private XorBitUtils() {
   }

   public static int di(int intVal, int intVal2) {
      return intVal ^ intVal2;
   }

   public static long dl(long longVal, long longVal2) {
      return longVal ^ longVal2;
   }

   public static float df(int intVal, int intVal2) {
      return Float.intBitsToFloat(intVal ^ intVal2);
   }

   public static double dd(long longVal, long longVal2) {
      return Double.longBitsToDouble(longVal ^ longVal2);
   }
}
