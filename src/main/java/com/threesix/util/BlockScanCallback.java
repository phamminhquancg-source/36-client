package com.threesix.util;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface BlockScanCallback {
   void accept(int arg1, int arg2, int arg3, BlockState arg4);
}
