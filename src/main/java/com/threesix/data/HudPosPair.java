package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class HudPosPair {
   public final int regionId;
   public final int y;

   public HudPosPair(int intVal, int intVal2) {
      this.regionId = intVal;
      this.y = intVal2;
   }

}
