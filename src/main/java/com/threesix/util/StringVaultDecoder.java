package com.threesix.util;

import java.nio.charset.Charset;

public final class StringVaultDecoder {
   private StringVaultDecoder() {
   }

   public static String d(String string, int intVal) {
      byte[] local = new byte[string.length()];
      int intVal2 = -1480934423 ^ intVal;

      for (int index = 0; index < string.length(); index++) {
         local[index] = (byte)(string.charAt(index) ^ intVal2 >>> 16 ^ index * 1);
         intVal2 = intVal2 * 31 + 7;
      }

      return new String(local, Charset.forName("UTF-8"));
   }

   public static String e(String string, int intVal) {
      byte[] local = new byte[string.length()];
      int intVal2 = -35593805 ^ intVal;

      for (int index = 0; index < string.length(); index++) {
         local[index] = (byte)(string.charAt(index) ^ intVal2 >>> 8 ^ index * 2);
         intVal2 = intVal2 * 17 + 13;
      }

      return new String(local, Charset.forName("UTF-8"));
   }

   public static String f(String string, int intVal) {
      byte[] local = new byte[string.length()];
      int intVal2 = -1464486682 ^ intVal;

      for (int index = 0; index < string.length(); index++) {
         local[index] = (byte)(string.charAt(index) ^ intVal2 >>> 24 ^ index * 3);
         intVal2 = intVal2 * 23 + 3;
      }

      return new String(local, Charset.forName("UTF-8"));
   }

   public static String g(String string, int intVal) {
      byte[] local = new byte[string.length()];
      int intVal2 = -1685682219 ^ intVal;

      for (int index = 0; index < string.length(); index++) {
         local[index] = (byte)(string.charAt(index) ^ intVal2 >>> 0 ^ index * 4);
         intVal2 = intVal2 * 29 + 11;
      }

      return new String(local, Charset.forName("UTF-8"));
   }
}
