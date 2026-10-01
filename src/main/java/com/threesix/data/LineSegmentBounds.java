package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class LineSegmentBounds {
   public double x;
   public double y;
   public double lineHitX;
   public double healthBarWidth;
   public boolean isVisible;

   public void setBounds(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, boolean flag) {
      this.x = doubleVal;
      this.y = doubleVal2;
      this.lineHitX = doubleVal3;
      this.healthBarWidth = doubleVal4;
      this.isVisible = flag;
   }

}
