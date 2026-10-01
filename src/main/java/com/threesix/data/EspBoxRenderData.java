package com.threesix.data;

import java.awt.Color;
import com.threesix.util.StringVaultDecoder;

public final class EspBoxRenderData {
   public final double boxX;
   public final double minY;
   public final double boxZ;
   public final double centerY;
   public final double boxHalfWidth;
   public final double height;
   public final Color fillColor;
   public final Color tracerColor;
   public final boolean isFilled;

   public EspBoxRenderData(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5, double doubleVal6, Color color, Color color2, boolean flag) {
      this.boxX = doubleVal;
      this.minY = doubleVal2;
      this.boxZ = doubleVal3;
      this.centerY = doubleVal4;
      this.boxHalfWidth = doubleVal5;
      this.height = doubleVal6;
      this.fillColor = color;
      this.tracerColor = color2;
      this.isFilled = flag;
   }

}
