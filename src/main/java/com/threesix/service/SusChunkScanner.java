package com.threesix.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkSectionPos;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.LightLevelUtil;
import com.threesix.util.StringVaultDecoder;
import com.threesix.data.ChunkClusterRecord;
import com.threesix.data.ChunkScanRecord;
import com.threesix.data.ScoredChunkRecord;
import com.threesix.module.SusChunkFinderModule;
import com.threesix.util.ChunkSectionCache;

public final class SusChunkScanner {
   public static final int scoreMultiplier = 5;
   private static final int unscannedLevel = -1;
   private static final int maxLightY = 60;
   private final Map<Long, ChunkScanRecord> scannedChunks;
   private final Deque<Long> pendingQueue;
   private final Set<Long> newChunkKeys;
   private volatile List<ScoredChunkRecord> scoredResults;
   private volatile List<ChunkClusterRecord> clusterResults;
   private ChunkPos lastCenterPos;
   private int tickCounter;
   private final ExecutorService scanExecutor;
   private final AtomicInteger activeScans;
   private volatile int scanGeneration;
   private static final int batchSize = 12;
   private static final int threadCount = 3;
   private static final int[][] spreadShapes = new int[][]{{1, 4}, {2, 4}, {1, 2}, {3, 1}, {5, 4}, {5, 6}};

   private final SusChunkFinderModule this$0;

   public SusChunkScanner(SusChunkFinderModule susChunkFinderModule) {
      this.this$0 = susChunkFinderModule;
      this.scannedChunks = new ConcurrentHashMap();
      this.pendingQueue = new ConcurrentLinkedDeque();
      this.newChunkKeys = ConcurrentHashMap.newKeySet();
      this.scoredResults = List.of();
      this.clusterResults = List.of();
      this.scanExecutor = Executors.newFixedThreadPool(3, item -> {
         Thread local = new Thread(item, "threesix-suscan");
         local.setDaemon(true);
         return local;
      });
      this.activeScans = new AtomicInteger(0);
      this.scanGeneration = 0;
   }

   public List<ScoredChunkRecord> getScoredChunks() {
      return this.scoredResults;
   }

   public List<ChunkClusterRecord> call1() {
      return this.clusterResults;
   }

   public int getMinScore() {

      return (Integer)this.this$0.sensitivitySetting.getValue() * 5;
   }

   public void call2() {
      this.scanGeneration++;
      this.scannedChunks.clear();
      this.pendingQueue.clear();
      this.newChunkKeys.clear();
      this.scoredResults = List.of();
      this.clusterResults = List.of();
      this.lastCenterPos = null;
      this.tickCounter = 0;
   }

   public void call3() {
      MinecraftClient moduleBaseValue = ModuleBase.minecraftClient;
      if (moduleBaseValue.world != null && moduleBaseValue.player != null) {
         try {
            this.enqueueNearbyChunks(moduleBaseValue);
            boolean flag = this.dropUnloadedChunks(moduleBaseValue);
            int intVal = !this.newChunkKeys.isEmpty() ? 1 : 0;
            if (!this.newChunkKeys.isEmpty()) {
               for (Long long2 : this.newChunkKeys) {
                  if (this.scannedChunks.remove(long2) != null && !this.pendingQueue.contains(long2)) {
                     this.pendingQueue.addFirst(long2);
                  }
               }

               this.newChunkKeys.clear();
            }

            this.dispatchScanBatches(moduleBaseValue);
            if (++this.tickCounter >= 20 || (intVal != 0 || flag) && this.tickCounter >= 3) {
               this.tickCounter = 0;
               this.publishResults(moduleBaseValue);
            }
         } catch (Exception error) {
         }
      }
   }

   private boolean dropUnloadedChunks(MinecraftClient arg) {
      if (this.scannedChunks.isEmpty()) {
         return false;
      }

      ArrayList nullSnapshot = null;

      try {
         for (Long long2 : this.scannedChunks.keySet()) {
            Object nullSnapshot2 = null;

            try {
               nullSnapshot2 = arg.world.getChunkManager().getWorldChunk(ChunkPos.getPackedX(long2), ChunkPos.getPackedZ(long2), false);
            } catch (Throwable error) {
               continue;
            }

            if (nullSnapshot2 == null) {
               if (nullSnapshot == null) {
                  nullSnapshot = new ArrayList();
               }

               nullSnapshot.add(long2);
            }
         }
      } catch (Exception error2) {
         return false;
      }

      if (nullSnapshot != null && !nullSnapshot.isEmpty()) {
         for (Long long3 : (Iterable<Long>)nullSnapshot) {
            this.scannedChunks.remove(long3);
         }

         return true;
      } else {
         return false;
      }
   }

   private void dispatchScanBatches(MinecraftClient arg) {
      while (true) {
         if (this.activeScans.get() < 3 && !this.pendingQueue.isEmpty()) {
            ArrayList arrayListInst = new ArrayList(12);

            while (arrayListInst.size() < 12) {
               Long local = (Long)this.pendingQueue.pollFirst();
               if (local == null) {
                  break;
               }

               if (!this.scannedChunks.containsKey(local)) {
                  arrayListInst.add(local);
               }
            }

            if (!arrayListInst.isEmpty()) {
               int scanGenerationSnapshot = this.scanGeneration;
               this.activeScans.incrementAndGet();
               this.scanExecutor.execute(() -> {
                  try {

                     for (Long long2 : (Iterable<Long>)arrayListInst) {
                        try {
                           WorldChunk local2 = arg.world.getChunkManager().getWorldChunk(ChunkPos.getPackedX(long2), ChunkPos.getPackedZ(long2), false);
                           if (local2 != null) {
                              ChunkScanRecord local3 = this.scanChunk(arg, local2);
                              if (scanGenerationSnapshot == this.scanGeneration) {
                                 this.scannedChunks.put(long2, local3);
                              }
                           }
                        } catch (Throwable error) {
                           if (scanGenerationSnapshot == this.scanGeneration && !this.scannedChunks.containsKey(long2)) {
                              this.pendingQueue.add(long2);
                           }
                        }
                     }
                  } finally {
                     this.activeScans.decrementAndGet();
                  }
               });
               continue;
            }
         }

         return;
      }
   }

   private boolean hasOreSections(WorldChunk arg) {
      try {

         ChunkSection[] var1Value = arg.getSectionArray();
         int var1Value2 = arg.getBottomY();

         for (int index = 0; index < var1Value.length; index++) {
            ChunkSection local = var1Value[index];
            if (local != null && !local.isEmpty()) {
               int var3Var416Value = var1Value2 + index * 16;
               if (var3Var416Value + 15 >= -1 && var3Var416Value <= 64 && local.getBlockStateContainer().hasAny(item -> {
                  return item.isOf(Blocks.AMETHYST_BLOCK) || item.isOf(Blocks.BUDDING_AMETHYST) || item.isOf(Blocks.AMETHYST_CLUSTER);
               })) {
                  return true;
               }
            }
         }
      } catch (Throwable error) {
      }

      return false;
   }

   private void enqueueNearbyChunks(MinecraftClient arg) {

      ChunkPos local = arg.player.getChunkPos();
      if (this.lastCenterPos == null
         || Math.max(Math.abs(local.x - this.lastCenterPos.x), Math.abs(local.z - this.lastCenterPos.z)) >= 3
         || this.pendingQueue.isEmpty()) {
         this.lastCenterPos = local;
         int maxValue = Math.max((Integer)this.this$0.simDistanceSetting.getValue(), Math.min((Integer)arg.options.getViewDistance().getValue() + 1, 16));
         ArrayList arrayListInst = new ArrayList();
         HashSet hashSetInst = new HashSet(this.pendingQueue);

         for (int index = -maxValue; index <= maxValue; index++) {
            for (int index2 = -maxValue; index2 <= maxValue; index2++) {
               long class1923Value = ChunkPos.toLong(local.x + index, local.z + index2);
               if (!this.scannedChunks.containsKey(class1923Value) && !hashSetInst.contains(class1923Value)) {
                  arrayListInst.add(class1923Value);
               }
            }
         }

         arrayListInst.sort(Comparator.comparingDouble(item -> {
            return Math.hypot(ChunkPos.getPackedX((Long)item) - local.x, ChunkPos.getPackedZ((Long)item) - local.z);
         }));
         this.pendingQueue.addAll(arrayListInst);
      }
   }

   private boolean hasSectionLightData() {

      for (byte[] byte2 : SusChunkFinderModule.field1.lightDataBySection.values()) {
         if (byte2 != null && byte2.length == 2048) {
            for (int index = 0; index < 4096; index++) {
               int intVal = byte2[index >> 1] & 255;
               int intVal2 = (index & 1) == 0 ? intVal & 15 : intVal >> 4 & 15;
               if (intVal2 == 5) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private ChunkScanRecord scanChunk(MinecraftClient arg, WorldChunk arg2) {
      ChunkScanRecord chunkScanRecordInst = new ChunkScanRecord(arg2.getPos().toLong());
      if (!this.hasOreSections(arg2)) {
         chunkScanRecordInst.finalizeScan();
         return chunkScanRecordInst;
      }

      ChunkPos var2Value = arg2.getPos();

      for (int index = 50; index >= -15; index--) {
         for (int index2 = 0; index2 < 16; index2++) {
            for (int index3 = 0; index3 < 16; index3++) {
               try {
                  if (arg.world
                     .getBlockState(new BlockPos(var2Value.getStartX() + index2, index, var2Value.getStartZ() + index3))
                     .isOf(Blocks.AMETHYST_CLUSTER)) {
                     chunkScanRecordInst.markTiered();
                  }
               } catch (Throwable error) {
               }
            }
         }
      }

      for (int index4 = 64; index4 >= -1; index4--) {
         for (int index5 = 0; index5 < 16; index5++) {
            for (int index6 = 0; index6 < 16; index6++) {
               BlockPos local = new BlockPos(var2Value.getStartX() + index5, index4, var2Value.getStartZ() + index6);
               int susChunkFinderModuleValue = SusChunkFinderModule.field1.getLightLevel2(local.getX(), local.getY(), local.getZ());
               int var9Snapshot = susChunkFinderModuleValue;
               if (susChunkFinderModuleValue == -1) {
                  var9Snapshot = arg.world.getLightLevel(LightType.BLOCK, local);
               }

               if (var9Snapshot == 5
                  && LightLevelUtil.hasAdjacentLightSource(local, arg.world)
                  && LightLevelUtil.hasTransparentNeighbor(local, arg.world)
                  && !LightLevelUtil.hasOpaqueNeighbor(local, arg.world)) {
                  chunkScanRecordInst.markLevelFive();
               }
            }
         }
      }

      boolean falseSnapshot = false;

      for (int index7 = ChunkSectionCache.MIN_SECTION_Y_PACKED; index7 <= ChunkSectionCache.MAX_SECTION_Y_PACKED; index7++) {
         long class4076Value = ChunkSectionPos.asLong(var2Value.x, index7, var2Value.z);
         byte[] local2 = (byte[])SusChunkFinderModule.field1.lightDataBySection.get(class4076Value);
         if (local2 != null && local2.length == 2048) {
            falseSnapshot = true;
            int intVal = index7 << 4;

            for (int index8 = 0; index8 < 4096; index8++) {
               int intVal2 = local2[index8 >> 1] & 255;
               int intVal3 = (index8 & 1) == 0 ? intVal2 & 15 : intVal2 >> 4 & 15;
               if (intVal3 == 0) {
                  int var30Var118Value = intVal + (index8 >> 8);
                  if (var30Var118Value >= -1 && var30Var118Value <= 64) {
                     int intVal4 = index8 & 15;
                     int intVal5 = index8 >> 4 & 15;
                     BlockPos local3 = new BlockPos(var2Value.getStartX() + intVal4, var30Var118Value, var2Value.getStartZ() + intVal5);
                     if (LightLevelUtil.hasAdjacentLightSource(local3, arg.world)
                        && LightLevelUtil.hasAdjacentHighLight(local3, arg.world)
                        && LightLevelUtil.hasTransparentNeighbor(local3, arg.world)
                        && !LightLevelUtil.hasOpaqueNeighbor(local3, arg.world)) {
                        chunkScanRecordInst.markDarkSpot();
                     }
                  }
               }
            }
         }
      }

      if (!falseSnapshot) {
         for (int index9 = -1; index9 <= 64; index9++) {
            for (int index10 = 0; index10 < 16; index10++) {
               for (int index11 = 0; index11 < 16; index11++) {
                  BlockPos local4 = new BlockPos(var2Value.getStartX() + index10, index9, var2Value.getStartZ() + index11);
                  if (LightLevelUtil.isDarkPos(local4)) {
                     chunkScanRecordInst.markDarkSpot();
                  }
               }
            }
         }
      }

      chunkScanRecordInst.finalizeScan();
      return chunkScanRecordInst;
   }

   private void scanColumnLightFive(MinecraftClient arg, WorldChunk arg2, ChunkScanRecord chunkScanRecord) {

      ChunkPos var2Value = arg2.getPos();

      for (int index = -64; index <= 70; index++) {
         for (int index2 = 0; index2 < 16; index2++) {
            for (int index3 = 0; index3 < 16; index3++) {
               BlockPos local = new BlockPos(var2Value.getStartX() + index2, index, var2Value.getStartZ() + index3);
               if (arg.world.getLightLevel(LightType.BLOCK, local) == 5 && hasAirNeighbor(arg, local)) {
                  chunkScanRecord.markLevelFive();
               }
            }
         }
      }
   }

   private boolean chunkHasAir(MinecraftClient arg, WorldChunk arg2) {

      ChunkPos var2Value = arg2.getPos();

      for (int index = -64; index <= 70; index++) {
         for (int index2 = 0; index2 < 16; index2++) {
            for (int index3 = 0; index3 < 16; index3++) {
               BlockPos local = new BlockPos(var2Value.getStartX() + index2, index, var2Value.getStartZ() + index3);
               BlockState local2 = arg.world.getBlockState(local);
               if (local2.isOf(Blocks.AMETHYST_BLOCK) || local2.isOf(Blocks.BUDDING_AMETHYST)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void scanLightLevels(MinecraftClient arg, WorldChunk arg2, ChunkScanRecord chunkScanRecord) {

      ChunkPos var2Value = arg2.getPos();

      for (int index = -1; index <= 60; index++) {
         for (int index2 = 0; index2 < 16; index2++) {
            for (int index3 = 0; index3 < 16; index3++) {
               BlockPos local = new BlockPos(var2Value.getStartX() + index2, index, var2Value.getStartZ() + index3);
               int intVal = arg.world.getLightLevel(LightType.BLOCK, local);
               if ((intVal == 1 || intVal == 2 || intVal == 4 || intVal == 5) && hasAirNeighbor(arg, local) && hasBrighterNeighbor(arg, local, intVal)) {
                  chunkScanRecord.markBedrock();
               }
            }
         }
      }
   }

   private static boolean hasAirNeighbor(MinecraftClient arg, BlockPos arg2) {
      Direction[] local = Direction.values();

      for (Direction class2350 : local) {
         BlockState local2 = arg.world.getBlockState(arg2.offset(class2350));
         if (local2.isOf(Blocks.AMETHYST_BLOCK) || local2.isOf(Blocks.BUDDING_AMETHYST)) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasBrighterNeighbor(MinecraftClient arg, BlockPos arg2, int intVal) {

      Direction[] local = Direction.values();

      for (Direction class2350 : local) {
         int intVal2 = arg.world.getLightLevel(LightType.BLOCK, arg2.offset(class2350));
         if (intVal2 >= intVal) {
            return true;
         }
      }

      return false;
   }

   private void scanAirLightPockets(MinecraftClient arg, WorldChunk arg2, ChunkScanRecord chunkScanRecord) {

      ChunkPos var2Value = arg2.getPos();

      for (int index = 64; index >= -15; index--) {
         for (int index2 = 0; index2 < 16; index2++) {
            for (int index3 = 0; index3 < 16; index3++) {
               BlockPos local = new BlockPos(var2Value.getStartX() + index2, index, var2Value.getStartZ() + index3);
               if (arg.world.getBlockState(local).isAir()) {
                  int intVal = arg.world.getLightLevel(LightType.BLOCK, local);
                  if (intVal == 0) {
                     boolean falseSnapshot = false;

                     for (Direction class2350 : Direction.values()) {
                        BlockState local2 = arg.world.getBlockState(local.offset(class2350));
                        if (local2.isOf(Blocks.AMETHYST_BLOCK) || local2.isOf(Blocks.BUDDING_AMETHYST)) {
                           falseSnapshot = true;
                           break;
                        }
                     }

                     if (falseSnapshot) {
                        boolean falseSnapshot2 = false;

                        for (int index4 = -1; index4 <= 1 && !falseSnapshot2; index4++) {
                           for (int index5 = -1; index5 <= 1 && !falseSnapshot2; index5++) {
                              for (int index6 = -1; index6 <= 1; index6++) {
                                 if (index4 != 0 || index5 != 0 || index6 != 0) {
                                    BlockPos var8Value = local.add(index4, index5, index6);
                                    int intVal2 = arg.world.getLightLevel(LightType.BLOCK, var8Value);
                                    if (intVal2 == 3 || intVal2 == 4) {
                                       falseSnapshot2 = true;
                                    }
                                 }
                              }
                           }
                        }

                        if (falseSnapshot2) {
                           boolean falseSnapshot3 = false;
                           boolean falseSnapshot4 = false;

                           for (Direction class23502 : Direction.values()) {
                              if (class23502.getAxis().isHorizontal()) {
                                 BlockState local3 = arg.world.getBlockState(local.offset(class23502));
                                 if (!local3.isAir()) {
                                    falseSnapshot3 = true;
                                 }

                                 if (local3.isOf(Blocks.STONE)
                                    || local3.isOf(Blocks.DEEPSLATE)
                                    || local3.isOf(Blocks.COBBLED_DEEPSLATE)
                                    || local3.isOf(Blocks.TUFF)
                                    || local3.isOf(Blocks.GRANITE)
                                    || local3.isOf(Blocks.DIORITE)
                                    || local3.isOf(Blocks.ANDESITE)) {
                                    falseSnapshot4 = true;
                                 }
                              }
                           }

                           if (falseSnapshot3 && !falseSnapshot4) {
                              chunkScanRecord.markDarkSpot();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void publishResults(MinecraftClient arg) {
      this.scannedChunks.keySet().removeIf(item -> {
         ChunkPos local = new ChunkPos(item);
         return Math.hypot(ChunkPos.getPackedX(item) - local.x, ChunkPos.getPackedZ(item) - local.z) > 1500.0;
      });
      int intVal = this.getMinScore();
      HashSet hashSetInst = new HashSet();

      for (ScoredChunkRecord scoredChunkRecord : this.scoredResults) {
         hashSetInst.add(scoredChunkRecord.chunkKey());
      }

      ArrayList arrayListInst = new ArrayList();

      for (ChunkScanRecord chunkScanRecord : this.scannedChunks.values()) {
         boolean flag = hashSetInst.contains(chunkScanRecord.recordChunkKey);
         if (chunkScanRecord.recordScore >= intVal || flag && chunkScanRecord.recordScore > 0.0) {
            arrayListInst.add(new ScoredChunkRecord(chunkScanRecord.recordChunkKey, flag ? Math.max(chunkScanRecord.recordScore, intVal) : chunkScanRecord.recordScore));
         } else if (flag && chunkScanRecord.recordScore == 0.0) {
            arrayListInst.add(new ScoredChunkRecord(chunkScanRecord.recordChunkKey, intVal));
         }
      }

      this.scoredResults = List.copyOf(arrayListInst);
      this.clusterResults = this.clusterChunks(this.expandSpreads(arrayListInst));
   }

   private List expandSpreads(List list) {

      HashMap hashMapInst = new HashMap();

      for (ScoredChunkRecord scoredChunkRecord : (Iterable<ScoredChunkRecord>)list) {
         hashMapInst.put(scoredChunkRecord.chunkKey(), scoredChunkRecord);
      }

      for (ScoredChunkRecord scoredChunkRecord2 : (Iterable<ScoredChunkRecord>)list) {
         Random randomInst = new Random(scoredChunkRecord2.chunkKey());
         int[] local = spreadShapes[randomInst.nextInt(spreadShapes.length)];
         int intVal = local[0];
         int intVal2 = local[1];
         if (randomInst.nextBoolean()) {
            int var7Snapshot = intVal;
            intVal = intVal2;
            intVal2 = var7Snapshot;
         }

         int intVal3 = -(intVal / 2);
         int intVal4 = -(intVal2 / 2);
         int class1923Value = ChunkPos.getPackedX(scoredChunkRecord2.chunkKey());
         int class1923Value2 = ChunkPos.getPackedZ(scoredChunkRecord2.chunkKey());

         for (int index = 0; index < intVal; index++) {
            for (int index2 = 0; index2 < intVal2; index2++) {
               long class1923Value3 = ChunkPos.toLong(class1923Value + intVal3 + index, class1923Value2 + intVal4 + index2);
               ScoredChunkRecord local2 = (ScoredChunkRecord)hashMapInst.get(class1923Value3);
               if (local2 == null || local2.coord() < scoredChunkRecord2.coord()) {
                  hashMapInst.put(class1923Value3, new ScoredChunkRecord(class1923Value3, scoredChunkRecord2.coord()));
               }
            }
         }
      }

      return new ArrayList(hashMapInst.values());
   }

   private List clusterChunks(List list) {

      int maxValue = Math.max(1, (Integer)this.this$0.mergeRadiusSetting.getValue());
      HashMap hashMapInst = new HashMap();

      for (ScoredChunkRecord scoredChunkRecord : (Iterable<ScoredChunkRecord>)list) {
         hashMapInst.put(scoredChunkRecord.chunkKey(), scoredChunkRecord);
      }

      ArrayList arrayListInst = new ArrayList();
      HashSet hashSetInst = new HashSet();

      for (ScoredChunkRecord scoredChunkRecord2 : (Iterable<ScoredChunkRecord>)list) {
         if (hashSetInst.add(scoredChunkRecord2.chunkKey())) {
            ArrayList arrayListInst2 = new ArrayList();
            ArrayDeque local = new ArrayDeque<>(List.of(scoredChunkRecord2));

            while (!local.isEmpty()) {
               ScoredChunkRecord local2 = (ScoredChunkRecord)local.poll();
               arrayListInst2.add(local2);
               int class1923Value = ChunkPos.getPackedX(local2.chunkKey());
               int class1923Value2 = ChunkPos.getPackedZ(local2.chunkKey());

               for (int index = -maxValue; index <= maxValue; index++) {
                  for (int index2 = -maxValue; index2 <= maxValue; index2++) {
                     if (index != 0 || index2 != 0) {
                        ScoredChunkRecord local3 = (ScoredChunkRecord)hashMapInst.get(ChunkPos.toLong(class1923Value + index, class1923Value2 + index2));
                        if (local3 != null && hashSetInst.add(local3.chunkKey())) {
                           local.add(local3);
                        }
                     }
                  }
               }
            }

            arrayListInst.add(this.buildCluster(arrayListInst2));
         }
      }

      arrayListInst.sort(Comparator.comparingDouble(ChunkClusterRecord::totalScore).reversed());
      return arrayListInst;
   }

   private ChunkClusterRecord buildCluster(List list) {
      double doubleVal = 0.0;
      double maxValue = 0.0;
      double doubleVal2 = 0.0;
      double doubleVal3 = 0.0;
      HashSet hashSetInst = new HashSet();

      for (ScoredChunkRecord scoredChunkRecord : (Iterable<ScoredChunkRecord>)list) {
         hashSetInst.add(scoredChunkRecord.chunkKey());
         doubleVal += scoredChunkRecord.coord();
         maxValue = Math.max(maxValue, scoredChunkRecord.coord());
         doubleVal2 += (ChunkPos.getPackedX(scoredChunkRecord.chunkKey()) * 16 + 8) * scoredChunkRecord.coord();
         doubleVal3 += (ChunkPos.getPackedZ(scoredChunkRecord.chunkKey()) * 16 + 8) * scoredChunkRecord.coord();
      }

      double doubleVal4 = doubleVal > 0.0 ? doubleVal2 / doubleVal : ChunkPos.getPackedX(((ScoredChunkRecord)list.get(0)).chunkKey()) * 16 + 8;
      double doubleVal5 = doubleVal > 0.0 ? doubleVal3 / doubleVal : ChunkPos.getPackedZ(((ScoredChunkRecord)list.get(0)).chunkKey()) * 16 + 8;
      return new ChunkClusterRecord(Set.copyOf(hashSetInst), doubleVal4, doubleVal5, doubleVal, maxValue, 0);
   }

}
