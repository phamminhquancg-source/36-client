package com.threesix.util;

import com.threesix.util.XorBitUtils;
import com.threesix.util.FeatureAvailabilityFlag;
import com.threesix.util.StringVaultDecoder;

public final class AntiTamperWatchdog {
   public static volatile int missingClassCount = 0;
   public static volatile long lastCheckMillis = 0L;
   public static volatile boolean isTamperDetected = false;
   public static final long MAGIC_HASH = -2401053089206453570L;

   public static boolean isIntact() {
      long systemValue = System.currentTimeMillis();
      if (systemValue - lastCheckMillis < 2000L + systemValue % 3000L) {
         return !isTamperDetected;
      }

      lastCheckMillis = systemValue;
      if (!FeatureAvailabilityFlag.isFeatureEnabled()) {
         missingClassCount++;
         if (missingClassCount > 3) {
            isTamperDetected = true;
         }

         return false;
      } else {
         try {
            Class.forName("com.threesix.service.RemoteUpdateService");
            Class.forName("快慢新旧美丑善恶真假.一西怒朋辱失步希天斗");
         } catch (ClassNotFoundException classNotFoundException) {
            isTamperDetected = true;
            return false;
         }

         if (missingClassCount > 0) {
            missingClassCount--;
         }

         return true;
      }
   }

   public static double applyJitter(double doubleVal) {

      return !isTamperDetected ? doubleVal : doubleVal + (Math.random() * 0.001 - 5.0E-4);
   }

   public static boolean isTampered() {
      return isTamperDetected;
   }

}
