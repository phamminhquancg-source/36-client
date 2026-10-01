package com.threesix.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongConsumer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.network.packet.s2c.play.LightData;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ChunkSectionCache {
   public static final int MIN_SECTION_Y = -12;
   public static final int MAX_SECTION_Y = 80;
   public static final int MIN_SECTION_Y_PACKED = ChunkSectionPos.getSectionCoord(-12);
   public static final int MAX_SECTION_Y_PACKED = ChunkSectionPos.getSectionCoord(80);
   public final ConcurrentHashMap<Long, byte[]> lightDataBySection = new ConcurrentHashMap<>();
   private final Set knownChunks = ConcurrentHashMap.newKeySet();
   private final List<LongConsumer> chunkListeners = new CopyOnWriteArrayList<>();

   public void clear() {
      this.lightDataBySection.clear();
      this.knownChunks.clear();
   }

   public boolean forgetChunk(int intVal, int intVal2) {
      return this.knownChunks.remove(ChunkPos.toLong(intVal, intVal2));
   }

   public void addChunkListener(LongConsumer longConsumer) {
      this.chunkListeners.add(longConsumer);
   }

   public void markChunkChanged(int intVal, int intVal2) {

      long class1923Value = ChunkPos.toLong(intVal, intVal2);
      this.knownChunks.add(class1923Value);

      for (LongConsumer longConsumer : this.chunkListeners) {
         longConsumer.accept(class1923Value);
      }
   }

   public void onLightUpdate(int intVal, int intVal2, LightData arg, World arg2) {
      if (arg2 != null) {
         int var4Value = arg2.getBottomSectionCoord() - 1;
         BitSet var3Value = arg.getInitedBlock();
         BitSet var3Value2 = arg.getUninitedBlock();
         List var3Value3 = arg.getBlockNibbles();
         boolean falseSnapshot = false;
         int local2 = 0;
         for (int index = var3Value.nextSetBit(0); index >= 0; index = var3Value.nextSetBit(index + 1)) {
            byte[] local = local2 < var3Value3.size() ? (byte[])var3Value3.get(local2) : null;
            local2++;
            int var5Var11Value = var4Value + index;
            if (local != null && local.length == 2048 && var5Var11Value >= MIN_SECTION_Y_PACKED && var5Var11Value <= MAX_SECTION_Y_PACKED) {
               long class4076Value = ChunkSectionPos.asLong(intVal, var5Var11Value, intVal2);
               this.lightDataBySection.put(class4076Value, (byte[])local.clone());
               falseSnapshot = true;
            }
         }

         for (int index2 = var3Value2.nextSetBit(0); index2 >= 0; index2 = var3Value2.nextSetBit(index2 + 1)) {
            int var5Var16Value = var4Value + index2;
            if (var5Var16Value >= MIN_SECTION_Y_PACKED && var5Var16Value <= MAX_SECTION_Y_PACKED) {
               long class4076Value2 = ChunkSectionPos.asLong(intVal, var5Var16Value, intVal2);
               if (this.lightDataBySection.remove(class4076Value2) != null) {
                  falseSnapshot = true;
               }
            }
         }

         if (falseSnapshot) {
            this.markChunkChanged(intVal, intVal2);
         }
      }
   }

   public int getLightLevel2(int intVal, int intVal2, int intVal3) {

      if (intVal2 >= -12 && intVal2 <= 80) {
         byte[] local = (byte[])this.lightDataBySection
            .get(ChunkSectionPos.asLong(ChunkSectionPos.getSectionCoord(intVal), ChunkSectionPos.getSectionCoord(intVal2), ChunkSectionPos.getSectionCoord(intVal3)));
         if (local == null) {
            return -1;
         }

         int intVal4 = (intVal2 & 15) << 8 | (intVal3 & 15) << 4 | intVal & 15;
         int intVal5 = local[intVal4 >> 1] & 255;
         return (intVal4 & 1) == 0 ? intVal5 & 15 : intVal5 >> 4 & 15;
      } else {
         return -1;
      }
   }

   public boolean isFullyLit(int intVal, int intVal2, int intVal3) {
      return this.getLightLevel2(intVal, intVal2, intVal3) == 5;
   }

   public int countLightBlocks(int intVal, int intVal2) {
      int local2 = 0;
      for (int index = MIN_SECTION_Y_PACKED; index <= MAX_SECTION_Y_PACKED; index++) {
         byte[] local = (byte[])this.lightDataBySection.get(ChunkSectionPos.asLong(intVal, index, intVal2));
         if (local != null) {
            for (int index2 = 0; index2 < 4096; index2++) {
               int intVal3 = local[index2 >> 1] & 255;
               int intVal4 = (index2 & 1) == 0 ? intVal3 & 15 : intVal3 >> 4 & 15;
               if (intVal4 >= 1 && intVal4 <= 5) {
                  local2++;
               }
            }
         }
      }

      return local2;
   }

   public List findBlocksAboveLevel(int intVal, int intVal2, int intVal3, int intVal4, int intVal5) {
      ArrayList nullSnapshot = null;
      int intVal6 = intVal << 4;
      int intVal7 = intVal2 << 4;

      for (int index = MIN_SECTION_Y_PACKED; index <= MAX_SECTION_Y_PACKED; index++) {
         byte[] local = (byte[])this.lightDataBySection.get(ChunkSectionPos.asLong(intVal, index, intVal2));
         if (local != null) {
            int intVal8 = index << 4;

            for (int index2 = 0; index2 < 4096; index2++) {
               int intVal9 = local[index2 >> 1] & 255;
               int intVal10 = (index2 & 1) == 0 ? intVal9 & 15 : intVal9 >> 4 & 15;
               if (intVal10 > intVal3) {
                  int var11Var128Value = intVal8 + (index2 >> 8);
                  if (var11Var128Value >= intVal4 && var11Var128Value <= intVal5) {
                     if (nullSnapshot == null) {
                        nullSnapshot = new ArrayList();
                     }

                     nullSnapshot.add(new BlockPos(intVal6 + (index2 & 15), var11Var128Value, intVal7 + (index2 >> 4 & 15)));
                  }
               }
            }
         }
      }

      return nullSnapshot == null ? List.of() : nullSnapshot;
   }

   public List findBlocksInLevelRange(int intVal, int intVal2, int intVal3, int intVal4, int intVal5, int intVal6) {
      ArrayList nullSnapshot = null;
      int intVal7 = intVal << 4;
      int intVal8 = intVal2 << 4;

      for (int index = MIN_SECTION_Y_PACKED; index <= MAX_SECTION_Y_PACKED; index++) {
         byte[] local = (byte[])this.lightDataBySection.get(ChunkSectionPos.asLong(intVal, index, intVal2));
         if (local != null) {
            int intVal9 = index << 4;

            for (int index2 = 0; index2 < 4096; index2++) {
               int intVal10 = local[index2 >> 1] & 255;
               int intVal11 = (index2 & 1) == 0 ? intVal10 & 15 : intVal10 >> 4 & 15;
               if (intVal11 >= intVal3 && intVal11 <= intVal4) {
                  int var12Var138Value = intVal9 + (index2 >> 8);
                  if (var12Var138Value >= intVal5 && var12Var138Value <= intVal6) {
                     if (nullSnapshot == null) {
                        nullSnapshot = new ArrayList();
                     }

                     nullSnapshot.add(new BlockPos(intVal7 + (index2 & 15), var12Var138Value, intVal8 + (index2 >> 4 & 15)));
                  }
               }
            }
         }
      }

      return nullSnapshot == null ? List.of() : nullSnapshot;
   }

   public boolean hasCachedChunk(int intVal, int intVal2) {

      for (int index = MIN_SECTION_Y_PACKED; index <= MAX_SECTION_Y_PACKED; index++) {
         if (this.lightDataBySection.containsKey(ChunkSectionPos.asLong(intVal, index, intVal2))) {
            return true;
         }
      }

      return false;
   }

   public int size() {
      return this.lightDataBySection.size();
   }

}
