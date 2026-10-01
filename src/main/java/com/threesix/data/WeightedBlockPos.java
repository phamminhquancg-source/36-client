package com.threesix.data;

import net.minecraft.util.math.BlockPos;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record WeightedBlockPos(BlockPos pos, double weight) {

   

}
