package com.threesix.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.util.math.ChunkSectionPos;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ChunkSectionScanner {
   private static final ExecutorService SCAN_EXECUTOR = Executors.newSingleThreadExecutor(local -> {
      Thread threadInst = new Thread(local, "threesix-scan-worker");
      threadInst.setDaemon(true);
      threadInst.setPriority(3);
      return threadInst;
   });

   private ChunkSectionScanner() {
   }

   public static void submit(Runnable runnable) {
      SCAN_EXECUTOR.execute(runnable);
   }

   public static void scanChunk(WorldChunk arg, int intVal, int intVal2, BlockScanCallback blockScanCallback) {
      scanChunkFiltered(arg, intVal, intVal2, null, blockScanCallback);
   }

   public static void scanChunkFiltered(WorldChunk arg, int intVal, int intVal2, Predicate predicate, BlockScanCallback blockScanCallback) {
      int var0Value = arg.getPos().getStartX();
      int var0Value2 = arg.getPos().getStartZ();
      ChunkSection[] var0Value3 = arg.getSectionArray();
      int var0Value4 = arg.getBottomSectionCoord();
      int class4076Value = ChunkSectionPos.getSectionCoord(intVal);
      int class4076Value2 = ChunkSectionPos.getSectionCoord(intVal2);

      for (int index = 0; index < var0Value3.length; index++) {
         int var8Var11Value = var0Value4 + index;
         if (var8Var11Value >= class4076Value && var8Var11Value <= class4076Value2) {
            ChunkSection local = var0Value3[index];
            if (local != null && !local.isEmpty() && (predicate == null || local.getBlockStateContainer().hasAny(predicate))) {
               int intVal3 = var8Var11Value << 4;

               for (int index2 = 0; index2 < 16; index2++) {
                  int var14Var15Value = intVal3 + index2;
                  if (var14Var15Value >= intVal) {
                     if (var14Var15Value > intVal2) {
                        break;
                     }

                     for (int index3 = 0; index3 < 16; index3++) {
                        for (int index4 = 0; index4 < 16; index4++) {
                           blockScanCallback.accept(var0Value + index3, var14Var15Value, var0Value2 + index4, local.getBlockState(index3, index2, index4));
                        }
                     }
                  }
               }
            }
         }
      }
   }

}
