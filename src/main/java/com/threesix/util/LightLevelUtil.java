package com.threesix.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.LightType;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import com.threesix.util.XorBitUtils;
import com.threesix.module.SusChunkFinderModule;
import com.threesix.util.StringVaultDecoder;

public final class LightLevelUtil {
   private static volatile Boolean cachedSkylightTopDown = null;
   private static long cacheTimestamp = 0L;

   private LightLevelUtil() {
   }

   public static boolean isSkylightTopDown() {
      long systemValue = System.currentTimeMillis();
      if (cachedSkylightTopDown != null && systemValue - cacheTimestamp < 1500L) {
         return cachedSkylightTopDown;
      }

      byte byteVal = 0;
      int local = 0;
      label54:
      for (byte[] byte2 : SusChunkFinderModule.field1.lightDataBySection.values()) {
         if (byte2 != null && byte2.length == 2048) {

            while (true) {
               if (local < 4096) {
                  int intVal = byte2[local >> 1] & 255;
                  int intVal2 = (local & 1) == 0 ? intVal & 15 : intVal >> 4 & 15;
                  if (intVal2 != 5) {
                     local++;
                     continue;
                  }

               }

               if (byteVal != 0) {
                  break label54;
               }
               break;
            }
         }
      }

      cachedSkylightTopDown = Boolean.valueOf(byteVal != 0);
      cacheTimestamp = systemValue;
      return byteVal != 0;
   }

   public static boolean isSkylightAvailable() {
      return isSkylightTopDown();
   }

   public static boolean hasAdjacentLightSource(BlockPos arg, World arg2) {
      if (arg2 == null) {
         return false;
      }

      if (!arg2.getBlockState(arg).isAir()) {
         return false;
      }

      for (int index = -1; index <= 1; index++) {
         for (int index2 = -1; index2 <= 1; index2++) {
            for (int index3 = -1; index3 <= 1; index3++) {
               if (index != 0 || index2 != 0 || index3 != 0) {
                  BlockState var1Value = arg2.getBlockState(arg.add(index, index2, index3));
                  if (var1Value.isOf(Blocks.AMETHYST_BLOCK) || var1Value.isOf(Blocks.BUDDING_AMETHYST)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public static boolean hasAdjacentHighLight(BlockPos arg, World arg2) {
      if (arg2 == null) {
         return false;
      }

      for (int index = -1; index <= 1; index++) {
         for (int index2 = -1; index2 <= 1; index2++) {
            for (int index3 = -1; index3 <= 1; index3++) {
               if (index != 0 || index2 != 0 || index3 != 0) {
                  BlockPos var0Value = arg.add(index, index2, index3);
                  int var1Value = arg2.getLightLevel(LightType.BLOCK, var0Value);
                  if (var1Value == 3 || var1Value == 4) {
                     return true;
                  }

                  int susChunkFinderModuleValue = SusChunkFinderModule.field1.getLightLevel2(var0Value.getX(), var0Value.getY(), var0Value.getZ());
                  if (susChunkFinderModuleValue == 3 || susChunkFinderModuleValue == 4) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public static boolean hasOpaqueNeighbor(BlockPos arg, World arg2) {

      for (Direction class2350 : Direction.values()) {
         if (class2350.getAxis().isHorizontal()) {
            BlockState var1Value = arg2.getBlockState(arg.offset(class2350));
            if (var1Value.isOf(Blocks.STONE)
               || var1Value.isOf(Blocks.DEEPSLATE)
               || var1Value.isOf(Blocks.COBBLED_DEEPSLATE)
               || var1Value.isOf(Blocks.TUFF)
               || var1Value.isOf(Blocks.GRANITE)
               || var1Value.isOf(Blocks.DIORITE)
               || var1Value.isOf(Blocks.ANDESITE)) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean hasTransparentNeighbor(BlockPos arg, World arg2) {

      for (Direction class2350 : Direction.values()) {
         if (class2350.getAxis().isHorizontal() && !arg2.getBlockState(arg.offset(class2350)).isAir()) {
            return true;
         }
      }

      return false;
   }

   public static int getLightLevel(BlockPos arg, World arg2) {
      int var1Value = arg2.getLightLevel(LightType.BLOCK, arg);
      int susChunkFinderModuleValue = SusChunkFinderModule.field1.getLightLevel2(arg.getX(), arg.getY(), arg.getZ());
      if (susChunkFinderModuleValue != -1) {
         if (var1Value == 0 || susChunkFinderModuleValue == 0) {
            return 0;
         } else {
            return var1Value != 5 && susChunkFinderModuleValue != 5 ? susChunkFinderModuleValue : 5;
         }
      } else {
         return var1Value;
      }
   }

   public static boolean isDarkPos(BlockPos arg) {
      ClientWorld class310Value = MinecraftClient.getInstance().world;
      if (class310Value == null) {
         return false;
      } else if (getLightLevel(arg, class310Value) != 0) {
         return false;
      } else if (!hasAdjacentLightSource(arg, class310Value)) {
         return false;
      } else if (!hasAdjacentHighLight(arg, class310Value)) {
         return false;
      } else {
         return !hasTransparentNeighbor(arg, class310Value) ? false : !hasOpaqueNeighbor(arg, class310Value);
      }
   }

   public static boolean isFullyLitPos(BlockPos arg) {
      ClientWorld class310Value = MinecraftClient.getInstance().world;
      if (class310Value == null) {
         return false;
      } else if (getLightLevel(arg, class310Value) != 5) {
         return false;
      } else if (!hasAdjacentLightSource(arg, class310Value)) {
         return false;
      } else {
         return !hasTransparentNeighbor(arg, class310Value) ? false : !hasOpaqueNeighbor(arg, class310Value);
      }
   }

   public static boolean isLitAirPos(BlockPos arg) {
      ClientWorld class310Value = MinecraftClient.getInstance().world;
      if (class310Value == null) {
         return false;
      }

      BlockState var1Value = class310Value.getBlockState(arg);
      return !var1Value.isOf(Blocks.AMETHYST_CLUSTER) ? false : getLightLevel(arg, class310Value) == 5;
   }

   public static int getRenderLightLevel(BlockPos arg) {
      if (isSkylightAvailable()) {
         if (isFullyLitPos(arg)) {
            return 1;
         }
      } else if (isDarkPos(arg)) {
         return 1;
      }

      return -1;
   }

   public static int getLightState(BlockPos arg) {
      if (isDarkPos(arg)) {
         return 1;
      } else {
         return isFullyLitPos(arg) ? 1 : -1;
      }
   }

   private static List collectLightSourceBlocks(WorldChunk arg, World arg2) {
      ArrayList arrayListInst = new ArrayList();
      ChunkPos var0Value = arg.getPos();
      ChunkSection[] var0Value2 = arg.getSectionArray();
      int var0Value3 = arg.getBottomY();

      for (int index = 0; index < var0Value2.length; index++) {
         ChunkSection local = var0Value2[index];
         if (local != null && !local.isEmpty() && local.getBlockStateContainer().hasAny(item -> {
            return item.isOf(Blocks.AMETHYST_BLOCK) || item.isOf(Blocks.BUDDING_AMETHYST);
         })) {
            int var5Var616Value = var0Value3 + index * 16;

            for (int index2 = 0; index2 < 16; index2++) {
               int var8Var9Value = var5Var616Value + index2;
               if (var8Var9Value >= -64 && var8Var9Value <= 60) {
                  for (int index3 = 0; index3 < 16; index3++) {
                     for (int index4 = 0; index4 < 16; index4++) {
                        BlockPos local2 = new BlockPos(var0Value.getStartX() + index3, var8Var9Value, var0Value.getStartZ() + index4);
                        BlockState var7Value = local.getBlockState(index3, index2, index4);
                        if (var7Value.isOf(Blocks.AMETHYST_BLOCK) || var7Value.isOf(Blocks.BUDDING_AMETHYST)) {
                           arrayListInst.add(local2);
                        }
                     }
                  }
               }
            }
         }
      }

      return arrayListInst;
   }

   public static int countDarkBlocks(WorldChunk arg) {
      int local2 = 0;
      ClientWorld class310Value = MinecraftClient.getInstance().world;
      if (class310Value == null) {
         return 0;
      }

      ChunkPos var0Value = arg.getPos();

      for (int index = -64; index <= 60; index++) {
         for (int index2 = 0; index2 < 16; index2++) {
            for (int index3 = 0; index3 < 16; index3++) {
               BlockPos local = new BlockPos(var0Value.getStartX() + index2, index, var0Value.getStartZ() + index3);
               if (isDarkPos(local)) {
                  local2++;
               }
            }
         }
      }

      return local2;
   }

   public static int countBrightBlocks(WorldChunk arg) {
      int local2 = 0;
      ClientWorld class310Value = MinecraftClient.getInstance().world;
      if (class310Value == null) {
         return 0;
      }

      ChunkPos var0Value = arg.getPos();

      for (int index = -64; index <= 60; index++) {
         for (int index2 = 0; index2 < 16; index2++) {
            for (int index3 = 0; index3 < 16; index3++) {
               BlockPos local = new BlockPos(var0Value.getStartX() + index2, index, var0Value.getStartZ() + index3);
               if (isFullyLitPos(local)) {
                  local2++;
               }
            }
         }
      }

      return local2;
   }

   public static void collectCandidatePositions(WorldChunk arg, Set set, int intVal) {
      ClientWorld class310Value = MinecraftClient.getInstance().world;
      if (class310Value != null) {
         ChunkPos var0Value = arg.getPos();
         boolean flag = isSkylightAvailable();
         HashSet hashSetInst = new HashSet();

         for (int index = -64; index <= 60; index++) {
            for (int index2 = 0; index2 < 16; index2++) {
               for (int index3 = 0; index3 < 16 && set.size() < intVal; index3++) {
                  BlockPos local = new BlockPos(var0Value.getStartX() + index2, index, var0Value.getStartZ() + index3);
                  if (hashSetInst.add(local)) {
                     boolean flag2 = flag ? isFullyLitPos(local) : isDarkPos(local);
                     if (flag2 || isLitAirPos(local)) {
                        boolean falseSnapshot = false;

                        for (BlockPos class2338 : (Iterable<BlockPos>)set) {
                           if (class2338.getSquaredDistance(local) < 2.25) {
                              falseSnapshot = true;
                              break;
                           }
                        }

                        if (!falseSnapshot) {
                           set.add(local.toImmutable());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static Map findNearbyLightBlocks(int intVal) {
      ClientWorld class310Value = MinecraftClient.getInstance().world;
      if (class310Value == null) {
         return Map.of();
      }

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null) {
         return Map.of();
      }

      ChunkPos local = mc.player.getChunkPos();
      HashMap hashMapInst = new HashMap();

      for (int index = -intVal - 1; index <= intVal + 1; index++) {
         for (int index2 = -intVal - 1; index2 <= intVal + 1; index2++) {
            ChunkPos local2 = new ChunkPos(local.x + index, local.z + index2);
            WorldChunk var1Value = class310Value.getChunkManager().getWorldChunk(local2.x, local2.z, false);
            if (var1Value != null) {
               hashMapInst.put(local2.toLong(), var1Value);
            }
         }
      }

      ArrayList arrayListInst = new ArrayList();

      for (WorldChunk class2818 : (Iterable<WorldChunk>)hashMapInst.values()) {
         arrayListInst.addAll(collectLightSourceBlocks(class2818, class310Value));
      }

      HashMap hashMapInst2 = new HashMap();
      boolean flag = isSkylightAvailable();
      HashSet hashSetInst = new HashSet();

      for (BlockPos class2338 : (Iterable<BlockPos>)arrayListInst) {
         for (int index3 = -1; index3 <= 1; index3++) {
            for (int index4 = -1; index4 <= 1; index4++) {
               for (int index5 = -1; index5 <= 1; index5++) {
                  if (index3 != 0 || index4 != 0 || index5 != 0) {
                     BlockPos var10Value = class2338.add(index3, index4, index5);
                     if (var10Value.getY() >= -64 && var10Value.getY() <= 60) {
                        ChunkPos local3 = new ChunkPos(var10Value);
                        if (Math.abs(local3.x - local.x) <= intVal && Math.abs(local3.z - local.z) <= intVal && hashSetInst.add(var10Value)) {
                           boolean flag2 = flag ? isFullyLitPos(var10Value) : isDarkPos(var10Value);
                           if (flag2 || isLitAirPos(var10Value)) {
                              long var15Value = local3.toLong();
                              ((List)hashMapInst2.computeIfAbsent(var15Value, entry -> {
                                 return new ArrayList();
                              })).add(var10Value);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return hashMapInst2;
   }

   static {
      try {
         SusChunkFinderModule.field1.addChunkListener(item -> cachedSkylightTopDown = null);
      } catch (Throwable error) {
      }
   }

}
