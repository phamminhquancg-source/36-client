package com.threesix.data;

import net.minecraft.text.Style;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record StyledTextRecord02(String text, Style style) {

   public StyledTextRecord02(String text, Style style) {
      if (style == null) {
         style = Style.EMPTY;
      }

      this.text = text;
      this.style = style;
   }

   

}
