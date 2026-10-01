package com.threesix.data;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public class BaseFinderProbeData {
   public final BlockPos probePos;
   public final Hand hand;
   public final BlockHitResult hitResult;
   public final long createdAt;
   public int remainingRotations = -1;

   public BaseFinderProbeData(BlockPos arg, Hand arg2, BlockHitResult arg3) {
      this.probePos = arg;
      this.hand = arg2;
      this.hitResult = arg3;
      this.createdAt = System.currentTimeMillis();
   }

}
