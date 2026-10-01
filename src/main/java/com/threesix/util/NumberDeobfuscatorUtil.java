package com.threesix.util;

public final class NumberDeobfuscatorUtil {
   private NumberDeobfuscatorUtil() {
   }

   public static int di2(int intVal) {
      return Integer.reverseBytes(intVal ^ 1597676561);
   }

   public static long dl2(long longVal) {
      return Long.reverse(longVal ^ -1597676561L);
   }
}
