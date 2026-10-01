package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class PositionTimestamp {
   public final double x;
   public final double y;
   public final double z;
   public final long timestamp;

   public PositionTimestamp(double doubleVal, double doubleVal2, double doubleVal3, long longVal) {
      this.x = doubleVal;
      this.y = doubleVal2;
      this.z = doubleVal3;
      this.timestamp = longVal;
   }

}
