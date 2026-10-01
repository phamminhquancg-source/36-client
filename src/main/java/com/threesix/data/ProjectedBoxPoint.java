package com.threesix.data;

import java.awt.Color;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ProjectedBoxPoint {
   public final double x;
   public final double y;
   public final double z;
   public final Color color;
   public final double scale;

   public ProjectedBoxPoint(double doubleVal, double doubleVal2, double doubleVal3, Color color2, double doubleVal4) {
      this.x = doubleVal;
      this.y = doubleVal2;
      this.z = doubleVal3;
      this.color = color2;
      this.scale = doubleVal4;
   }

}
