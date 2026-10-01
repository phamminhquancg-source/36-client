package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public class BlockRotationConstraint {
   public final Float yaw;
   public final Float pitch;

   public BlockRotationConstraint(Float float2, Float float3) {
      this.yaw = float2;
      this.pitch = float3;
   }

}
