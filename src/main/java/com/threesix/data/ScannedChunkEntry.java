package com.threesix.data;

import com.threesix.util.XorBitUtils;

public final class ScannedChunkEntry {
   public final int chunkX;
   public final int chunkZ;
   public boolean isInRange;

   public ScannedChunkEntry(int intVal, int intVal2) {
      this.chunkX = intVal;
      this.chunkZ = intVal2;
      this.isInRange = true;
   }

}
