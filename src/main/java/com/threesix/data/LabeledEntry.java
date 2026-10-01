package com.threesix.data;

import com.threesix.util.XorBitUtils;

public final class LabeledEntry {
   public final String id;
   public final String label;
   public final String prefix;
   public final String tooltip;

   public LabeledEntry(String string, String string2, String string3, String string4) {
      this.id = string;
      this.label = string2;
      this.prefix = string3;
      this.tooltip = string4;
   }

   public String getDisplayLabel() {
      return this.prefix.isEmpty() ? this.label : this.prefix + " - " + this.label;
   }

}
