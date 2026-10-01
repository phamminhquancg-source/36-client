package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public enum ClientFontType {
   XUONG("Xuong", "/assets/threesix/font/xuong.ttf", 17.5F, 0),
   VANILLA("Vanilla", null, 17.5F, -1);

   public final String displayName;
   public final String resourcePath;
   public final float size;
   public final int fontType;

   ClientFontType(String string, String string2, float floatVal, int intVal) {
      this.displayName = string;
      this.resourcePath = string2;
      this.size = floatVal;
      this.fontType = intVal;
   }

}
