package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ApiResponse {
   public final boolean success;
   public final int code;
   public final String message;

   public ApiResponse(boolean flag, int intVal, String string) {
      this.success = flag;
      this.code = intVal;
      this.message = string;
   }

}
