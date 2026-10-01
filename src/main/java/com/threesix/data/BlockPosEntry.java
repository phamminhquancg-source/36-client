package com.threesix.data;

import net.minecraft.util.math.BlockPos;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class BlockPosEntry {
   public final BlockPos blockPos;
   public final int ageTicks;

   public BlockPosEntry(BlockPos arg, int intVal) {
      this.blockPos = arg;
      this.ageTicks = intVal;
   }

}
