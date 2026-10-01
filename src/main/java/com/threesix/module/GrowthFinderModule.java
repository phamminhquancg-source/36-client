package com.threesix.module;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.enums.Thickness;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.Heightmap.Type;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.module.HudModule;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.ChunkGrowthCounts;

public final class GrowthFinderModule extends ModuleBase {
   public final ClientSetting simDistanceSetting = new ClientSetting("Sim Distance", 5, 1, 16);
   public final ClientSetting sensitivitySetting = new ClientSetting("Sensitivity", 1, 1, 10);
   public final ClientSetting kelpSetting = new ClientSetting("Kelp", true);
   public final ClientSetting vinesSetting = new ClientSetting("Vines", true);
   public final ClientSetting beehiveSetting = new ClientSetting("Beehive", true);
   public final ClientSetting dripstoneSetting = new ClientSetting("Dripstone", true);
   public final ClientSetting cobbleDeepslateSetting = new ClientSetting("Cobbled Deepslate", true);
   public final ClientSetting rotDeepslateSetting = new ClientSetting("Rotated Deepslate", true);
   public final ClientSetting endStoneSetting = new ClientSetting("End Stone", true);
   public final ClientSetting traderSetting = new ClientSetting("Trader Entity", true);
   public final ClientSetting llamaSetting = new ClientSetting("Llama Entity", true);
   public final ClientSetting alphaSetting = new ClientSetting("Alpha", 200, 0, 255);
   private static final double growthStepFraction = 0.16;
   private static final int minVineCount = 2;
   private static final int maxVineHeight = 15;
   private static final int minDeepslateCount = 14;
   private static final int sectionBlockSize = 16;
   private static final double entityScanRadius = 256.0;
   private static final int baseHeightOffset = 0;
   private static final int maxScanHeight = 128;
   private static final int[][] growthChunkOffsets = new int[][]{{1, 4}, {2, 4}, {1, 2}, {3, 1}, {5, 4}, {5, 6}};
   private final Map<Long, ChunkGrowthCounts> chunkGrowthMap = new ConcurrentHashMap();
   private final Deque<Long> pendingChunks = new ArrayDeque();
   private volatile Set<Long> highlightedChunks = Set.of();
   private ChunkPos lastScanChunk;
   private int tickCounter;
   private final Set<BlockPos> vinePositions = ConcurrentHashMap.newKeySet();
   private final Set<Long> kelpChunks = ConcurrentHashMap.newKeySet();
   private final Set<BlockPos> beehivePositions = ConcurrentHashMap.newKeySet();
   private final Set<BlockPos> upDripstonePositions = ConcurrentHashMap.newKeySet();
   private final Set<BlockPos> downDripstonePositions = ConcurrentHashMap.newKeySet();
   private final Set<String> notifiedKeys = ConcurrentHashMap.newKeySet();

   public GrowthFinderModule() {
      super("Grow Finder", ModuleCategory.BASEFINDING);
      this.registerSetting(this.simDistanceSetting);
      this.registerSetting(this.sensitivitySetting);
      this.registerSetting(this.alphaSetting);
      this.registerSetting(this.kelpSetting);
      this.registerSetting(this.vinesSetting);
      this.registerSetting(this.beehiveSetting);
      this.registerSetting(this.dripstoneSetting);
      this.registerSetting(this.cobbleDeepslateSetting);
      this.registerSetting(this.rotDeepslateSetting);
      this.registerSetting(this.endStoneSetting);
      this.registerSetting(this.traderSetting);
      this.registerSetting(this.llamaSetting);
   }

   @Override
   public void onEnable() {
      this.chunkGrowthMap.clear();
      this.pendingChunks.clear();
      this.highlightedChunks = Set.of();
      this.vinePositions.clear();
      this.kelpChunks.clear();
      this.beehivePositions.clear();
      this.upDripstonePositions.clear();
      this.downDripstonePositions.clear();
      this.notifiedKeys.clear();
      this.lastScanChunk = null;
   }

   @Override
   public void onDisable() {

      this.chunkGrowthMap.clear();
      this.pendingChunks.clear();
      this.highlightedChunks = Set.of();
      this.vinePositions.clear();
      this.kelpChunks.clear();
      this.beehivePositions.clear();
      this.upDripstonePositions.clear();
      this.downDripstonePositions.clear();
      this.notifiedKeys.clear();
      this.lastScanChunk = null;
   }

   @Override
   public void onTick() {
      MinecraftClient minecraftClientSnapshot = minecraftClient;
      if (minecraftClientSnapshot.world != null && minecraftClientSnapshot.player != null) {
         try {
            this.queueChunksAroundPlayer(minecraftClientSnapshot);
            byte byteVal = 8;
            long systemValue = System.nanoTime() + 2000000L;
            int local2 = 0;

            while (local2 < byteVal && !this.pendingChunks.isEmpty() && System.nanoTime() < systemValue) {
               long longVal = (Long)this.pendingChunks.pollFirst();
               if (!this.chunkGrowthMap.containsKey(longVal)) {
                  WorldChunk local = minecraftClientSnapshot.world.getChunkManager().getWorldChunk(ChunkPos.getPackedX(longVal), ChunkPos.getPackedZ(longVal), false);
                  if (local != null) {
                     try {
                        this.chunkGrowthMap.put(longVal, this.scanChunkGrowth(minecraftClientSnapshot, local));
                        local2++;
                     } catch (Exception error) {
                        this.pendingChunks.add(longVal);
                     }
                  }
               }
            }

            if (++this.tickCounter % 20 == 0) {
               this.scanNearbyEntities(minecraftClientSnapshot);
               this.updateHighlightedChunks(minecraftClientSnapshot);
            }
         } catch (Exception error2) {
         }
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {

      if (minecraftClient.world != null && minecraftClient.player != null) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            double doubleVal = 62.0;
            double var50Value = doubleVal - 0.08 - textStyleUtilValue2.y;
            double var50Value2 = doubleVal + 0.08 - textStyleUtilValue2.y;
            Color colorInst = new Color(255, 0, 0, (Integer)this.alphaSetting.getValue());
            Color colorInst2 = new Color(255, 0, 0, Math.max((Integer)this.alphaSetting.getValue(), 220));
            arg.push();
            GL11.glDisable(2929);

            try {
               WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);

               for (long long2 : this.highlightedChunks) {
                  int class1923Value = ChunkPos.getPackedX(long2);
                  int class1923Value2 = ChunkPos.getPackedZ(long2);
                  double class1923Value16Value = class1923Value * 16.0 - textStyleUtilValue2.x;
                  double class1923Value216Value = class1923Value2 * 16.0 - textStyleUtilValue2.z;
                  textStyleUtilValue3.fillBox(class1923Value16Value, var50Value, class1923Value216Value, class1923Value16Value + 16.0, var50Value2, class1923Value216Value + 16.0, colorInst);
                  textStyleUtilValue3.strokeBox(class1923Value16Value + 0.5, var50Value, class1923Value216Value + 0.5, class1923Value16Value + 15.5, var50Value2, class1923Value216Value + 15.5, colorInst2);
               }
            } finally {
               GL11.glEnable(2929);
            }

            arg.pop();
         }
      }
   }

   private void queueChunksAroundPlayer(MinecraftClient arg) {

      ChunkPos local = arg.player.getChunkPos();
      if (this.lastScanChunk == null
         || Math.max(Math.abs(local.x - this.lastScanChunk.x), Math.abs(local.z - this.lastScanChunk.z)) >= 3
         || this.pendingChunks.isEmpty()) {
         this.lastScanChunk = local;
         int maxValue = Math.max((Integer)this.simDistanceSetting.getValue(), Math.min((Integer)arg.options.getViewDistance().getValue() + 1, 16));
         ArrayList<Long> arrayListInst = new ArrayList();
         HashSet hashSetInst = new HashSet(this.pendingChunks);

         for (int index = -maxValue; index <= maxValue; index++) {
            for (int index2 = -maxValue; index2 <= maxValue; index2++) {
               long class1923Value = ChunkPos.toLong(local.x + index, local.z + index2);
               if (!this.chunkGrowthMap.containsKey(class1923Value) && !hashSetInst.contains(class1923Value)) {
                  arrayListInst.add(class1923Value);
               }
            }
         }

         arrayListInst.sort(Comparator.comparingDouble(item -> {
            return Math.hypot(ChunkPos.getPackedX(item) - local.x, ChunkPos.getPackedZ(item) - local.z);
         }));
         this.pendingChunks.addAll(arrayListInst);
      }
   }

   private ChunkGrowthCounts scanChunkGrowth(MinecraftClient arg, WorldChunk arg2) {
      ChunkGrowthCounts chunkGrowthCountsInst = new ChunkGrowthCounts();
      if ((Boolean)this.vinesSetting.getValue()) {
         this.scanVines(arg, arg2, chunkGrowthCountsInst);
      }

      if ((Boolean)this.kelpSetting.getValue()) {
         this.scanKelp(arg, arg2, chunkGrowthCountsInst);
      }

      if ((Boolean)this.beehiveSetting.getValue()) {
         this.scanBeehives(arg2, chunkGrowthCountsInst);
      }

      if ((Boolean)this.dripstoneSetting.getValue()) {
         this.scanDripstone(arg, arg2, chunkGrowthCountsInst);
      }

      this.scanDeepslateAndEndStone(arg, arg2, chunkGrowthCountsInst);
      return chunkGrowthCountsInst;
   }

   private void scanVines(MinecraftClient arg, WorldChunk arg2, ChunkGrowthCounts chunkGrowthCounts) {

      ChunkPos var2Value = arg2.getPos();
      int var4Value = var2Value.getStartX();
      int var4Value2 = var2Value.getStartZ();
      int maxValue = Math.max(arg2.getBottomY(), -64);
      int minValue = Math.min(arg2.getBottomY() + arg2.getHeight(), 320);
      HashSet<BlockPos> hashSetInst = new HashSet();

      for (int index = var4Value; index < var4Value + 16; index++) {
         for (int index2 = var4Value2; index2 < var4Value2 + 16; index2++) {
            for (int index3 = maxValue; index3 < minValue; index3++) {
               BlockPos local = new BlockPos(index, index3, index2);
               if (this.isTallVineColumn(arg2, local)) {
                  hashSetInst.add(local);
               }
            }
         }
      }

      this.vinePositions.removeIf(toRemove -> {
         return new ChunkPos(toRemove).equals(var2Value) && !hashSetInst.contains(toRemove);
      });
      int local2 = 0;

      for (BlockPos class2338 : hashSetInst) {
         if (this.vinePositions.add(class2338)) {
            local2++;
         }
      }

      chunkGrowthCounts.vines = hashSetInst.size();
   }

   private boolean isTallVineColumn(WorldChunk arg, BlockPos arg2) {

      if (arg.getBlockState(arg2).getBlock() != Blocks.VINE) {
         return false;
      }

      if (arg2.getY() >= -64 && arg2.getY() <= 320) {
         BlockPos var2Value = arg2.down();
         BlockState var1Value = arg.getBlockState(var2Value);
         if (var1Value.isAir() || var1Value.getBlock() == Blocks.VINE) {
            return false;
         } else {
            return var1Value.getCollisionShape(arg, var2Value).isEmpty() ? false : this.countVinesUpwards(arg, arg2) >= 15;
         }
      } else {
         return false;
      }
   }

   private int countVinesUpwards(WorldChunk arg, BlockPos arg2) {

      int local = 1;
      for (BlockPos index = arg2.up(); arg.getBlockState(index).getBlock() == Blocks.VINE; index = index.up()) {
         local++;
      }

      return local;
   }

   private void scanKelp(MinecraftClient arg, WorldChunk arg2, ChunkGrowthCounts chunkGrowthCounts) {
      ChunkPos var2Value = arg2.getPos();
      int var4Value = var2Value.getStartX();
      int var4Value2 = var2Value.getStartZ();
      int var2Value2 = arg2.getBottomY();
      int var7Var2Value = var2Value2 + arg2.getHeight();
      int local = 0;
      int local2 = 0;

      for (int index = var4Value; index < var4Value + 16; index++) {
         for (int index2 = var4Value2; index2 < var4Value2 + 16; index2++) {
            int var15Snapshot = -1;
            int var15Snapshot2 = -1;

            for (int index3 = var2Value2; index3 < var7Var2Value; index3++) {
               Block var2Value3 = arg2.getBlockState(new BlockPos(index, index3, index2)).getBlock();
               if (var2Value3 == Blocks.KELP || var2Value3 == Blocks.KELP_PLANT) {
                  if (var15Snapshot < 0) {
                     var15Snapshot = index3;
                  }

                  var15Snapshot2 = index3;
               }
            }

            if (var15Snapshot >= 0 && var15Snapshot2 - var15Snapshot + 1 >= 8) {
               local++;
               if (var15Snapshot2 == 62) {
                  local2++;
               }
            }
         }
      }

      if (local >= 10 && (double)local2 / local >= 0.6) {
         chunkGrowthCounts.kelp = local;
         long class1923Value = ChunkPos.toLong(var2Value.x, var2Value.z);
         this.kelpChunks.add(class1923Value);
      } else {
         long class1923Value2 = ChunkPos.toLong(var2Value.x, var2Value.z);
         this.kelpChunks.remove(class1923Value2);
      }
   }

   private void scanBeehives(WorldChunk arg, ChunkGrowthCounts chunkGrowthCounts) {
      ChunkPos var1Value = arg.getPos();
      int var3Value = var1Value.getStartX();
      int var3Value2 = var1Value.getStartZ();
      int maxValue = Math.max(arg.getBottomY(), 50);
      int minValue = Math.min(arg.getBottomY() + arg.getHeight(), 200);
      HashSet<BlockPos> hashSetInst = new HashSet();

      for (int index = var3Value; index < var3Value + 16; index++) {
         for (int index2 = var3Value2; index2 < var3Value2 + 16; index2++) {
            for (int index3 = maxValue; index3 < minValue; index3++) {
               BlockPos local = new BlockPos(index, index3, index2);
               BlockState var1Value2 = arg.getBlockState(local);
               if ((var1Value2.getBlock() == Blocks.BEEHIVE || var1Value2.getBlock() == Blocks.BEE_NEST)
                  && var1Value2.contains(Properties.HONEY_LEVEL)) {
                  int intVal = (Integer)var1Value2.get(Properties.HONEY_LEVEL);
                  if (intVal == 5) {
                     hashSetInst.add(local);
                  }
               }
            }
         }
      }

      this.beehivePositions.removeIf(toRemove -> {
         return new ChunkPos(toRemove).equals(var1Value) && !hashSetInst.contains(toRemove);
      });

      for (BlockPos class2338 : hashSetInst) {
         if (this.beehivePositions.add(class2338)) {
            chunkGrowthCounts.beehives++;
         } else {
            chunkGrowthCounts.beehives++;
         }
      }
   }

   private void scanDripstone(MinecraftClient arg, WorldChunk arg2, ChunkGrowthCounts chunkGrowthCounts) {

      ChunkPos var2Value = arg2.getPos();
      ChunkSection[] var2Value2 = arg2.getSectionArray();
      int var2Value3 = arg2.getBottomSectionCoord();

      for (int index = 0; index < var2Value2.length; index++) {
         ChunkSection local = var2Value2[index];
         if (local != null && !local.isEmpty()) {
            int var6Var74Value = var2Value3 + index << 4;

            for (int index2 = 0; index2 < 16; index2++) {
               for (int index3 = 0; index3 < 16; index3++) {
                  for (int index4 = 0; index4 < 16; index4++) {
                     BlockState var8Value = local.getBlockState(index2, index3, index4);
                     if (var8Value.getBlock() == Blocks.POINTED_DRIPSTONE) {
                        BlockPos local2 = new BlockPos(var2Value.getStartX() + index2, var6Var74Value + index3, var2Value.getStartZ() + index4);
                        if (this.isDripstoneUpHalf(var8Value)) {
                           int intVal = this.countDripstoneUpwards(arg, local2);
                           if (intVal >= 4 && this.upDripstonePositions.add(local2)) {
                              chunkGrowthCounts.dripstone++;
                           }
                        }

                        if (this.isDripstoneDownHalf(var8Value)) {
                           int intVal2 = this.countDripstoneDownwards(arg, local2);
                           if (intVal2 >= 4 && this.downDripstonePositions.add(local2)) {
                              chunkGrowthCounts.dripstone++;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isDripstoneBlock(BlockState arg) {

      return arg.getBlock() == Blocks.POINTED_DRIPSTONE;
   }

   private boolean isDripstoneUpHalf(BlockState arg) {
      if (this.isDripstoneBlock(arg) && arg.contains(Properties.THICKNESS)) {
         Thickness local = (Thickness)arg.get(Properties.THICKNESS);
         return local == Thickness.TIP_MERGE || local == Thickness.TIP;
      } else {
         return false;
      }
   }

   private boolean isDripstoneDownHalf(BlockState arg) {

      if (this.isDripstoneBlock(arg) && arg.contains(Properties.THICKNESS)) {
         Thickness local = (Thickness)arg.get(Properties.THICKNESS);
         return local == Thickness.TIP || local == Thickness.TIP_MERGE;
      } else {
         return false;
      }
   }

   private int countDripstoneUpwards(MinecraftClient arg, BlockPos arg2) {

      int local = 0;
      for (BlockPos index = arg2;
         index.getY() >= arg.world.getBottomY() && this.isDripstoneBlock(arg.world.getBlockState(index));
         index = index.down()
      ) {
         local++;
      }

      return local;
   }

   private int countDripstoneDownwards(MinecraftClient arg, BlockPos arg2) {

      int local = 0;
      for (BlockPos index = arg2;
         index.getY() < arg.world.getTopY(Type.WORLD_SURFACE, index.getX(), index.getZ())
            && this.isDripstoneBlock(arg.world.getBlockState(index));
         index = index.up()
      ) {
         local++;
      }

      return local;
   }

   private void scanDeepslateAndEndStone(MinecraftClient arg, WorldChunk arg2, ChunkGrowthCounts chunkGrowthCounts) {
      ChunkPos var2Value = arg2.getPos();
      byte byteVal = 0;
      int minValue = Math.min(arg2.getBottomY() + arg2.getHeight(), 128);
      ChunkSection[] var2Value2 = arg2.getSectionArray();
      int var2Value3 = arg2.getBottomSectionCoord();
      byte byteVal2 = 0;
      boolean flag = arg.world.getRegistryKey().getValue().getPath().equals("the_end");
      int local3 = 0;
      int local4 = 0;
      int local5 = 0;
      int local6 = 0;

      for (int index = 0; index < var2Value2.length && local3 < 50; index++) {
         ChunkSection local = var2Value2[index];
         if (local != null && !local.isEmpty()) {
            int var8Var154Value = var2Value3 + index << 4;
            int maxValue = Math.max(0, byteVal - var8Var154Value);
            int minValue2 = Math.min(15, minValue - var8Var154Value);
            if (maxValue <= 15 && minValue2 >= 0) {
               for (int index2 = 0; index2 < 16; index2++) {
                  for (int index3 = 0; index3 < 16; index3++) {
                     for (int index4 = maxValue; index4 <= minValue2; index4++) {
                        BlockState var16Value = local.getBlockState(index2, index4, index3);
                        int var17Var22Value = var8Var154Value + index4;
                        BlockPos local2 = new BlockPos(var2Value.getStartX() + index2, var17Var22Value, var2Value.getStartZ() + index3);
                        if (this.isObsidianLike(var16Value)) {
                           if (++local3 >= 50) {
                              return;
                           }
                        } else {
                           boolean flag2 = this.isCaveOpening(arg, local2);
                           if ((Boolean)this.cobbleDeepslateSetting.getValue() && this.isCobbledDeepslate(var16Value) && !flag2) {
                              local4++;
                           }

                           if ((Boolean)this.rotDeepslateSetting.getValue() && this.isRotatedDeepslate(var16Value) && !flag2) {
                              local5++;
                           }

                           if ((Boolean)this.endStoneSetting.getValue() && this.isEndStone(var16Value) && !flag && !flag2) {
                              local6++;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (local3 < 50) {
         chunkGrowthCounts.unusedGrowthCount = byteVal2;
         chunkGrowthCounts.cobbledDeepslate = local4;
         chunkGrowthCounts.rotatedDeepslate = local5;
         chunkGrowthCounts.endStone = local6;
      }
   }

   private boolean isObsidianLike(BlockState arg) {
      Block var1Value = arg.getBlock();
      return var1Value == Blocks.TUFF_BRICKS
         || var1Value == Blocks.WAXED_COPPER_BLOCK
         || var1Value == Blocks.WAXED_EXPOSED_COPPER
         || var1Value == Blocks.WAXED_WEATHERED_COPPER
         || var1Value == Blocks.WAXED_OXIDIZED_COPPER
         || var1Value == Blocks.WAXED_CUT_COPPER
         || var1Value == Blocks.WAXED_EXPOSED_CUT_COPPER
         || var1Value == Blocks.WAXED_WEATHERED_CUT_COPPER
         || var1Value == Blocks.WAXED_OXIDIZED_CUT_COPPER;
   }

   private boolean isCaveOpening(MinecraftClient arg, BlockPos arg2) {
      if (arg.world == null) {
         return false;
      }

      for (Direction class2350 : Direction.values()) {
         BlockPos var2Value = arg2.offset(class2350);
         if (var2Value.getY() >= arg.world.getBottomY() && var2Value.getY() < arg.world.getBottomY() + arg.world.getHeight()) {
            BlockState local = arg.world.getBlockState(var2Value);
            if (local.isAir()) {
               return true;
            }

            if (!local.getFluidState().isEmpty()) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean hasOpenPortalFrame(MinecraftClient arg, BlockPos arg2, int intVal) {
      if (arg.world == null) {
         return false;
      }

      int intVal2 = intVal > -8 ? 50 : 20;
      int local = 1;
      int local2 = 1;
      int local3 = 1;

      for (int index = 1; index < intVal2; index++) {
         BlockPos var2Value = arg2.offset(Direction.EAST, index);
         if (var2Value.getY() < arg.world.getBottomY()
            || var2Value.getY() >= arg.world.getBottomY() + arg.world.getHeight()
            || !this.isEndPortalFrameBlock(arg.world.getBlockState(var2Value))) {
            break;
         }

         local++;
      }

      for (int index2 = 1; index2 < intVal2; index2++) {
         BlockPos var2Value2 = arg2.offset(Direction.WEST, index2);
         if (var2Value2.getY() < arg.world.getBottomY()
            || var2Value2.getY() >= arg.world.getBottomY() + arg.world.getHeight()
            || !this.isEndPortalFrameBlock(arg.world.getBlockState(var2Value2))) {
            break;
         }

         local++;
      }

      if (local >= intVal2) {
         return true;
      }

      for (int index3 = 1; index3 < intVal2; index3++) {
         BlockPos var2Value3 = arg2.offset(Direction.SOUTH, index3);
         if (!this.isEndPortalFrameBlock(arg.world.getBlockState(var2Value3))) {
            break;
         }

         local2++;
      }

      for (int index4 = 1; index4 < intVal2; index4++) {
         BlockPos var2Value4 = arg2.offset(Direction.NORTH, index4);
         if (!this.isEndPortalFrameBlock(arg.world.getBlockState(var2Value4))) {
            break;
         }

         local2++;
      }

      if (local2 >= intVal2) {
         return true;
      }

      if (intVal > 0) {

         for (int index5 = 1; index5 < intVal2; index5++) {
            BlockPos var2Value5 = arg2.offset(Direction.UP, index5);
            if (!this.isEndPortalFrameBlock(arg.world.getBlockState(var2Value5))) {
               break;
            }

            local3++;
         }

         for (int index6 = 1; index6 < intVal2; index6++) {
            BlockPos var2Value6 = arg2.offset(Direction.DOWN, index6);
            if (!this.isEndPortalFrameBlock(arg.world.getBlockState(var2Value6))) {
               break;
            }

            local3++;
         }

         if (local3 >= intVal2) {
            return true;
         }
      }

      return false;
   }

   private boolean isInWorldHeight(MinecraftClient arg, BlockPos arg2) {
      return arg2.getY() >= arg.world.getBottomY() && arg2.getY() < arg.world.getBottomY() + arg.world.getHeight();
   }

   private boolean isEndPortalFrameBlock(BlockState arg) {

      if (arg.getBlock() != Blocks.DEEPSLATE) {
         return false;
      } else {
         return !arg.contains(Properties.AXIS) ? false : arg.get(Properties.AXIS) == Axis.Y;
      }
   }

   private boolean isCobbledDeepslate(BlockState arg) {

      return arg.getBlock() == Blocks.COBBLED_DEEPSLATE;
   }

   private boolean isRotatedDeepslate(BlockState arg) {

      if (arg.getBlock() != Blocks.DEEPSLATE) {
         return false;
      } else {
         return !arg.contains(Properties.AXIS) ? false : arg.get(Properties.AXIS) != Axis.Y;
      }
   }

   private boolean isEndStone(BlockState arg) {
      return arg.getBlock() == Blocks.END_STONE;
   }

   private void scanNearbyEntities(MinecraftClient arg) {
      if (arg.world != null && arg.player != null) {
         BlockPos local = arg.player.getBlockPos();
         Box local2 = new Box(local).expand(256.0);
         List<Entity> local3 = arg.world.getOtherEntities(arg.player, local2, item -> true);
         HashSet hashSetInst = new HashSet();

         for (Entity class1297 : local3) {
            if (!(class1297.getY() <= 16.0)) {
               int other = (!(Boolean)this.traderSetting.getValue() || !(class1297 instanceof WanderingTraderEntity)) && (!(Boolean)this.llamaSetting.getValue() || !(class1297 instanceof TraderLlamaEntity))
                  ? 0
                  : 1;
               if (other != 0 && !(arg.player.squaredDistanceTo(class1297) > 65536.0)) {
                  long class1923Value = ChunkPos.toLong((int)class1297.getX() >> 4, (int)class1297.getZ() >> 4);
                  ChunkGrowthCounts local4 = this.chunkGrowthMap.computeIfAbsent(class1923Value, item -> {
                     return new ChunkGrowthCounts();
                  });
                  local4.traderOrLlama++;
                  hashSetInst.add(class1923Value);
               }
            }
         }

         if (!hashSetInst.isEmpty()) {
            this.updateHighlightedChunks(arg);
         }
      }
   }

   private void updateHighlightedChunks(MinecraftClient arg) {

      this.chunkGrowthMap.keySet().removeIf(item -> {
         return arg.world.getChunkManager().getWorldChunk(ChunkPos.getPackedX(item), ChunkPos.getPackedZ(item), false) == null;
      });
      HashSet<Long> hashSetInst = new HashSet();
      int local = 5;

      try {
         local = (Integer)this.sensitivitySetting.getValue() * 5;
      } catch (Throwable error) {
      }

      for (Entry entry : this.chunkGrowthMap.entrySet()) {
         Long local2 = (Long)entry.getKey();
         ChunkGrowthCounts local3 = (ChunkGrowthCounts)entry.getValue();
         int local3Value = local3.kelp * 5
            + local3.vines * 5
            + local3.dripstone * 5
            + local3.beehives * 10
            + local3.cobbledDeepslate * 10
            + local3.rotatedDeepslate * 10
            + local3.endStone * 10;
         boolean flag = local3Value >= local || local3.traderOrLlama > 0;
         if (flag) {
            hashSetInst.add(local2);
            this.reportGrowthFindings(arg, local2, local3);
         }
      }

      HashSet<Long> hashSetInst2 = new HashSet(hashSetInst);

      for (Long long2 : hashSetInst) {
         Random randomInst = new Random(long2);
         int[] local4 = growthChunkOffsets[randomInst.nextInt(growthChunkOffsets.length)];
         int intVal = local4[0];
         int intVal2 = local4[1];
         if (randomInst.nextBoolean()) {
            int var23Snapshot = intVal;
            intVal = intVal2;
            intVal2 = var23Snapshot;
         }

         int intVal3 = -(intVal / 2);
         int intVal4 = -(intVal2 / 2);
         int class1923Value = ChunkPos.getPackedX(long2);
         int class1923Value2 = ChunkPos.getPackedZ(long2);

         for (int index = 0; index < intVal; index++) {
            for (int index2 = 0; index2 < intVal2; index2++) {
               hashSetInst2.add(ChunkPos.toLong(class1923Value + intVal3 + index, class1923Value2 + intVal4 + index2));
            }
         }
      }

      this.highlightedChunks = Set.copyOf(hashSetInst2);
   }

   private void reportGrowthFindings(MinecraftClient arg, Long long2, ChunkGrowthCounts chunkGrowthCounts) {
      if (arg.player != null) {
         int class1923Value = ChunkPos.getPackedX(long2) * 16 + 8;
         int class1923Value2 = ChunkPos.getPackedZ(long2) * 16 + 8;
         if (chunkGrowthCounts.kelp >= 10) {
            this.sendFindingNotification(arg, long2, "Kelp", class1923Value, class1923Value2);
         }

         if (chunkGrowthCounts.vines >= 2) {
            this.sendFindingNotification(arg, long2, "Vines", class1923Value, class1923Value2);
         }

         if (chunkGrowthCounts.beehives > 0) {
            this.sendFindingNotification(arg, long2, "Beehive", class1923Value, class1923Value2);
         }

         if (chunkGrowthCounts.dripstone > 0) {
            this.sendFindingNotification(arg, long2, "Dripstone", class1923Value, class1923Value2);
         }

         if (chunkGrowthCounts.cobbledDeepslate >= 4) {
            this.sendFindingNotification(arg, long2, "CobbledDeepslate", class1923Value, class1923Value2);
         }

         if (chunkGrowthCounts.rotatedDeepslate >= 3) {
            this.sendFindingNotification(arg, long2, "RotatedDeepslate", class1923Value, class1923Value2);
         }

         if (chunkGrowthCounts.endStone >= 2) {
            this.sendFindingNotification(arg, long2, "EndStone", class1923Value, class1923Value2);
         }

         if (chunkGrowthCounts.traderOrLlama > 0) {
            this.sendFindingNotification(arg, long2, "Trader/Llama", class1923Value, class1923Value2);
         }
      }
   }

   private void sendFindingNotification(MinecraftClient arg, Long long2, String string, int intVal, int intVal2) {
      String var222Var3Value = long2 + ":" + string;
      if (!this.notifiedKeys.contains(var222Var3Value)) {
         this.notifiedKeys.add(var222Var3Value);
         arg.player.sendMessage(Text.literal("§lthreesix client§r - detect §e" + string + "§r toa do §b" + intVal + " §b" + intVal2), false);

         try {
            HudModule.pushToast("Grow Finder", string + " " + intVal + " " + intVal2, HudModule.toastEnabledColor, null);
         } catch (Throwable error) {
         }
      }
   }

}
