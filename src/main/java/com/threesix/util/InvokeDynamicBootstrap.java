package com.threesix.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import com.threesix.util.StringVaultDecoder;

public final class InvokeDynamicBootstrap {
   private InvokeDynamicBootstrap() {
   }

   public static int rI(int intVal, int intVal2) {
      return intVal ^ intVal2 ^ 1862467148 ^ 1862467148;
   }

   public static long rJ(long longVal, long longVal2) {
      return longVal ^ longVal2 ^ 1862467148L ^ 1862467148L;
   }

   public static String rS(String string, int intVal) {
      return StringVaultDecoder.d(string, intVal ^ 1862467148);
   }

   public static CallSite bootstrap(Lookup lookup, String string, MethodType methodType, String string2, String string3, String string4) {
      try {
         ClassLoader invokeDynamicBootstrapValu = InvokeDynamicBootstrap.class.getClassLoader();
         Class classValue = Class.forName(string2, false, invokeDynamicBootstrapValu);
         MethodType methodTypeValue = MethodType.fromMethodDescriptorString(string4, invokeDynamicBootstrapValu);
         MethodHandle local = lookup.findVirtual(classValue, string3, methodTypeValue);
         return new ConstantCallSite(local);
      } catch (Exception error) {
         throw new RuntimeException(error);
      }
   }
}
