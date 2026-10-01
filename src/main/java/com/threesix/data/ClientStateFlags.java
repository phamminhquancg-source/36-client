package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ClientStateFlags {
   public static boolean isLitematicaCompatActive = false;
   public static boolean isLitematicaInstalled = true;

   private ClientStateFlags() {
   }

   public static void setLitematicaPaused(boolean flag) {
      isLitematicaCompatActive = flag;
   }

}
