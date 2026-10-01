package com.threesix.data;

import java.util.ArrayList;
import java.util.List;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ChunkScanRecord {
   public final long recordChunkKey;
   public final List positions = new ArrayList();
   public double signalCount;
   public double recordScore;
   public int darkSpotCount;
   public int tieredCount;
   public int levelFiveCount;
   public int bedrockCount;

   public ChunkScanRecord(long longVal) {
      this.recordChunkKey = longVal;
   }

   public void markDarkSpot() {
      this.signalCount++;
      this.darkSpotCount++;
   }

   public void markTiered() {
      this.signalCount++;
      this.tieredCount++;
   }

   public void markLevelFive() {
      this.signalCount++;
      this.levelFiveCount++;
   }

   public void markBedrock() {
      this.signalCount++;
      this.bedrockCount++;
   }

   public void finalizeScan() {
      this.recordScore = this.signalCount;
   }

}
