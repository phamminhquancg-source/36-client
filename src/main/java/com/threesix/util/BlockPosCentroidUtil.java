package com.threesix.util;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public class BlockPosCentroidUtil {
   public final int count;
   public final List<BlockPos> positions;

   public BlockPosCentroidUtil(List<BlockPos> list) {
      this.positions = list;
      this.count = list.size();
   }

   public BlockPos computeCentroid() {

      long longVal = 0L;
      long longVal2 = 0L;
      long longVal3 = 0L;

      for (BlockPos class2338 : this.positions) {
         longVal += class2338.getX();
         longVal2 += class2338.getY();
         longVal3 += class2338.getZ();
      }

      return new BlockPos((int)(longVal / this.positions.size()), (int)(longVal2 / this.positions.size()), (int)(longVal3 / this.positions.size()));
   }

}
