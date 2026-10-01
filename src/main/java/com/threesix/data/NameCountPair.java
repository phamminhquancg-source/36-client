package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class NameCountPair {
   public final String name;
   public final int count;

   public NameCountPair(String string, int intVal) {
      this.name = string;
      this.count = intVal;
   }

}
