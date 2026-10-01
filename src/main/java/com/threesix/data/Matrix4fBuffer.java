package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class Matrix4fBuffer {
   public final float rectX;
   public final float rectY;
   public final float rectWidth;
   public final float rectHeight;
   public final float searchX;
   public final float searchY;
   public final float searchWidth;
   public final float searchHeight;
   public final float clearButtonX;
   public final float clearButtonY;
   public final float clearButtonWidth;
   public final float clearButtonHeight;
   public final float listX;
   public final float listY;
   public final float listWidth;
   public final float listHeight;

   public Matrix4fBuffer(
      float floatVal,
      float floatVal2,
      float floatVal3,
      float floatVal4,
      float floatVal5,
      float floatVal6,
      float floatVal7,
      float floatVal8,
      float floatVal9,
      float floatVal10,
      float floatVal11,
      float floatVal12,
      float floatVal13,
      float floatVal14,
      float floatVal15,
      float floatVal16
   ) {
      this.rectX = floatVal;
      this.rectY = floatVal2;
      this.rectWidth = floatVal3;
      this.rectHeight = floatVal4;
      this.searchX = floatVal5;
      this.searchY = floatVal6;
      this.searchWidth = floatVal7;
      this.searchHeight = floatVal8;
      this.clearButtonX = floatVal9;
      this.clearButtonY = floatVal10;
      this.clearButtonWidth = floatVal11;
      this.clearButtonHeight = floatVal12;
      this.listX = floatVal13;
      this.listY = floatVal14;
      this.listWidth = floatVal15;
      this.listHeight = floatVal16;
   }

}
