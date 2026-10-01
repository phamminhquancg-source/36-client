package com.threesix.data;

import com.threesix.util.XorBitUtils;

public final class EntityBoxRenderData {
   public final double boxX;
   public final double boxBottomY;
   public final double boxZ;
   public final double tracerY;
   public final double boxHalfWidth;
   public final double height;

   public EntityBoxRenderData(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6) {
      this.boxX = doubleVal;
      this.boxBottomY = doubleVal2;
      this.boxZ = doubleVal3;
      this.tracerY = doubleVal4;
      this.boxHalfWidth = doubleVal5;
      this.height = doubleVal6;
   }

}
