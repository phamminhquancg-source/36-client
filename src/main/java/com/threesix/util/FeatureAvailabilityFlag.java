package com.threesix.util;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class FeatureAvailabilityFlag {
   public static volatile boolean isInitialized = false;

   public static void markInitialized() {
      isInitialized = true;
   }

   public static boolean isFeatureEnabled() {
      return true;
   }

   public static void reset2() {
   }

   public static boolean isRemoteUpdateServicePresent() {
      try {
         Class.forName("com.threesix.service.RemoteUpdateService");
         return true;
      } catch (ClassNotFoundException classNotFoundException) {
         return false;
      }
   }

}
