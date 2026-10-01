package com.threesix.module;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.lwjgl.opengl.GL11;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ResourceLoadEntry;
import com.threesix.data.ModuleCategory;
import com.threesix.render.EspRenderPipelines;
import com.threesix.data.ScannedChunkEntry;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class HoleEspModule extends ModuleBase {
   public static final int maxChunksPerTick = 200;
   public static final int minHoleHeight = 7;
   public final ClientSetting fillAlphaSetting = new ClientSetting("Fill Alpha", 60.0, 0.0, 255.0);
   public final ClientSetting colorSetting = new ClientSetting("Color", new Color(255, 100, 0));
   public final ClientSetting rangeSetting = new ClientSetting("Range", 64.0, 16.0, 128.0);
   public final Map<Long, ScannedChunkEntry> scannedChunks = new ConcurrentHashMap();
   public final Queue<Long> pendingChunks = new ArrayDeque();
   public final Set<Long> queuedChunks = ConcurrentHashMap.newKeySet();
   public final Set<ResourceLoadEntry> holeBoxes = ConcurrentHashMap.newKeySet();
   public ExecutorService scanExecutor;
   public ClientWorld scanWorld;

   public HoleEspModule() {
      super("Hole ESP", ModuleCategory.RENDER);
      this.registerSetting(this.fillAlphaSetting);
      this.registerSetting(this.colorSetting);
      this.registerSetting(this.rangeSetting);
   }

   @Override
   public void onEnable() {
      this.scanWorld = minecraftClient.world;
      this.ensureExecutor();
      this.clearCache();
   }

   @Override
   public void onDisable() {
      this.shutdownExecutor();
      this.clearCache();
      this.scanWorld = null;
   }

   @Override
   public void onTick() {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         if (minecraftClient.world != this.scanWorld) {
            this.scanWorld = minecraftClient.world;
            this.clearCache();
         }

         this.ensureExecutor();
         this.updateVisibleChunks();
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null && !this.holeBoxes.isEmpty()) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            int intVal = this.clampAlpha((Double)this.fillAlphaSetting.getValue());
            BufferAllocator local = new BufferAllocator(2097152);
            Immediate class4597Value = VertexConsumerProvider.immediate(local);
            VertexConsumer var7Value = class4597Value.getBuffer(EspRenderPipelines.getFillPipeline());
            Entry var1Value = arg.peek();
            byte byteVal = 0;

            for (ResourceLoadEntry resourceLoadEntry : this.holeBoxes) {
               if (resourceLoadEntry.isLoaded()) {
                  Box local2 = resourceLoadEntry.id;
                  if (TextStyleUtil.isAabbVisible(local2.minX, local2.minY, local2.minZ, local2.maxX, local2.maxY, local2.maxZ)) {
                     Color local3 = (Color)this.colorSetting.getValue();
                     Color local4 = this.withAlpha(local3, intVal);
                     Box local5 = new Box(
                        local2.minX - textStyleUtilValue2.x,
                        local2.minY - textStyleUtilValue2.y,
                        local2.minZ - textStyleUtilValue2.z,
                        local2.maxX - textStyleUtilValue2.x,
                        local2.maxY - textStyleUtilValue2.y,
                        local2.maxZ - textStyleUtilValue2.z
                     );
                     this.renderBoxEdges(var7Value, var1Value, local5, this.toArgb(local4));
                  }
               }
            }

            if (byteVal == 0) {
               local.close();
            } else {
               boolean gL11Value = GL11.glIsEnabled(2929);
               GL11.glDisable(2929);
               GL11.glDepthMask(false);
               class4597Value.draw();
               GL11.glDepthMask(true);
               if (gL11Value) {
                  GL11.glEnable(2929);
               }

               local.close();
            }
         }
      }
   }

   public void renderBoxEdges(VertexConsumer arg, Entry arg2, Box arg3, int intVal) {
      float floatVal = (float)arg3.minX;
      float floatVal2 = (float)arg3.minY;
      float floatVal3 = (float)arg3.minZ;
      float floatVal4 = (float)arg3.maxX;
      float floatVal5 = (float)arg3.maxY;
      float floatVal6 = (float)arg3.maxZ;
      this.drawQuadFace(arg, arg2, floatVal, floatVal2, floatVal3, floatVal4, floatVal2, floatVal3, floatVal4, floatVal2, floatVal6, floatVal, floatVal2, floatVal6, intVal);
      this.drawQuadFace(arg, arg2, floatVal, floatVal5, floatVal3, floatVal, floatVal5, floatVal6, floatVal4, floatVal5, floatVal6, floatVal4, floatVal5, floatVal3, intVal);
      this.drawQuadFace(arg, arg2, floatVal, floatVal2, floatVal3, floatVal, floatVal5, floatVal3, floatVal4, floatVal5, floatVal3, floatVal4, floatVal2, floatVal3, intVal);
      this.drawQuadFace(arg, arg2, floatVal, floatVal2, floatVal6, floatVal4, floatVal2, floatVal6, floatVal4, floatVal5, floatVal6, floatVal, floatVal5, floatVal6, intVal);
      this.drawQuadFace(arg, arg2, floatVal, floatVal2, floatVal3, floatVal, floatVal2, floatVal6, floatVal, floatVal5, floatVal6, floatVal, floatVal5, floatVal3, intVal);
      this.drawQuadFace(arg, arg2, floatVal4, floatVal2, floatVal3, floatVal4, floatVal5, floatVal3, floatVal4, floatVal5, floatVal6, floatVal4, floatVal2, floatVal6, intVal);
   }

   public void renderGradientBox(VertexConsumer arg, Entry arg2, Box arg3, Color color, int intVal) {
      double maxValue3 = Math.max(0.001, arg3.maxY - arg3.minY);
      int maxValue = Math.max(1, MathHelper.ceil(maxValue3));
      int maxValue2 = Math.max(6, Math.round(intVal * 0.18F));
      float floatVal = (float)arg3.minX;
      float floatVal2 = (float)arg3.minZ;
      float floatVal3 = (float)arg3.maxX;
      float floatVal4 = (float)arg3.maxZ;
      int var25Snapshot = 0;
      int var26Snapshot = 0;

      for (int index = 0; index < maxValue; index++) {
         double indexValue = (double)index / maxValue;
         double indexValue2 = (double)(index + 1) / maxValue;
         float floatVal5 = (float)MathHelper.lerp(indexValue, arg3.minY, arg3.maxY);
         float floatVal6 = (float)MathHelper.lerp(indexValue2, arg3.minY, arg3.maxY);
         float floatVal7 = 1.0F - (float)index / Math.max(1, maxValue - 1);
         float floatVal8 = 1.0F - (float)(index + 1) / Math.max(1, maxValue);
         int intVal2 = this.toArgb(this.withAlpha(color, Math.max(maxValue2, Math.round(intVal * floatVal7))));
         int intVal3 = this.toArgb(this.withAlpha(color, Math.max(maxValue2, Math.round(intVal * floatVal8))));
         if (index == 0) {
            var25Snapshot = intVal2;
         }

         if (index == maxValue - 1) {
            var26Snapshot = intVal3;
         }

         this.drawGradientFace(arg, arg2, floatVal, floatVal5, floatVal2, floatVal, floatVal6, floatVal2, floatVal3, floatVal6, floatVal2, floatVal3, floatVal5, floatVal2, intVal2, intVal3);
         this.drawGradientFace(arg, arg2, floatVal, floatVal5, floatVal4, floatVal3, floatVal5, floatVal4, floatVal3, floatVal6, floatVal4, floatVal, floatVal6, floatVal4, intVal2, intVal3);
         this.drawGradientFace(arg, arg2, floatVal, floatVal5, floatVal2, floatVal, floatVal5, floatVal4, floatVal, floatVal6, floatVal4, floatVal, floatVal6, floatVal2, intVal2, intVal3);
         this.drawGradientFace(arg, arg2, floatVal3, floatVal5, floatVal2, floatVal3, floatVal6, floatVal2, floatVal3, floatVal6, floatVal4, floatVal3, floatVal5, floatVal4, intVal2, intVal3);
      }

      this.drawQuadFace(
         arg,
         arg2,
         floatVal,
         (float)arg3.maxY,
         floatVal2,
         floatVal,
         (float)arg3.maxY,
         floatVal4,
         floatVal3,
         (float)arg3.maxY,
         floatVal4,
         floatVal3,
         (float)arg3.maxY,
         floatVal2,
         var26Snapshot
      );
      this.drawQuadFace(
         arg,
         arg2,
         floatVal,
         (float)arg3.minY,
         floatVal2,
         floatVal3,
         (float)arg3.minY,
         floatVal2,
         floatVal3,
         (float)arg3.minY,
         floatVal4,
         floatVal,
         (float)arg3.minY,
         floatVal4,
         var25Snapshot
      );
   }

   public void drawGradientFace(
      VertexConsumer arg,
      Entry arg2,
      float floatVal,
      float floatVal2,
      float floatVal3,
      float floatVal4,
      float floatVal5,
      float floatVal6,
      float floatVal7,
      float floatVal8,
      float floatVal9,
      float floatVal10,
      float floatVal11,
      float floatVal12,
      int intVal,
      int intVal2
   ) {
      arg.vertex(arg2, floatVal, floatVal2, floatVal3).color(intVal);
      arg.vertex(arg2, floatVal4, floatVal5, floatVal6).color(intVal2);
      arg.vertex(arg2, floatVal7, floatVal8, floatVal9).color(intVal2);
      arg.vertex(arg2, floatVal10, floatVal11, floatVal12).color(intVal);
   }

   public void drawQuadFace(
      VertexConsumer arg,
      Entry arg2,
      float floatVal,
      float floatVal2,
      float floatVal3,
      float floatVal4,
      float floatVal5,
      float floatVal6,
      float floatVal7,
      float floatVal8,
      float floatVal9,
      float floatVal10,
      float floatVal11,
      float floatVal12,
      int intVal
   ) {
      arg.vertex(arg2, floatVal, floatVal2, floatVal3).color(intVal);
      arg.vertex(arg2, floatVal4, floatVal5, floatVal6).color(intVal);
      arg.vertex(arg2, floatVal7, floatVal8, floatVal9).color(intVal);
      arg.vertex(arg2, floatVal10, floatVal11, floatVal12).color(intVal);
   }

   public void updateVisibleChunks() {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         for (ScannedChunkEntry scannedChunkEntry : this.scannedChunks.values()) {
            scannedChunkEntry.isInRange = false;
         }

         int maxValue = Math.max(1, this.getRangeBlocks() / 16);
         int minecraftClientValue = minecraftClient.player.getChunkPos().x;
         int minecraftClientValue2 = minecraftClient.player.getChunkPos().z;

         for (int index = minecraftClientValue - maxValue; index <= minecraftClientValue + maxValue; index++) {
            for (int index2 = minecraftClientValue2 - maxValue; index2 <= minecraftClientValue2 + maxValue; index2++) {
               WorldChunk minecraftClientValue3 = minecraftClient.world.getChunkManager().getWorldChunk(index, index2, false);
               if (minecraftClientValue3 != null) {
                  long class1923Value = ChunkPos.toLong(index, index2);
                  ScannedChunkEntry local = (ScannedChunkEntry)this.scannedChunks.get(class1923Value);
                  if (local != null) {
                     local.isInRange = true;
                  } else if (this.queuedChunks.add(class1923Value)) {
                     this.pendingChunks.add(class1923Value);
                  }
               }
            }
         }

         this.processPendingChunks();
         this.scannedChunks.entrySet().removeIf(item -> {
            return !((ScannedChunkEntry)item.getValue()).isInRange;
         });
         Set<Long> local2 = this.scannedChunks.keySet();
         this.holeBoxes.removeIf(item2 -> {
            return !this.isBoxInLoadedChunks(item2.id, local2);
         });
      }
   }

   public boolean isBoxInLoadedChunks(Box arg, Set set) {
      int intVal = (int)Math.floor(arg.getCenter().x) >> 4;
      int intVal2 = (int)Math.floor(arg.getCenter().z) >> 4;
      return set.contains(ChunkPos.toLong(intVal, intVal2));
   }

   public void processPendingChunks() {
      if (this.scanExecutor != null && minecraftClient.world != null) {
         int local2 = 0;

         while (!this.pendingChunks.isEmpty() && local2 < 200) {
            Long local = (Long)this.pendingChunks.poll();
            if (local != null) {
               this.queuedChunks.remove(local);
               int class1923Value = ChunkPos.getPackedX(local);
               int class1923Value2 = ChunkPos.getPackedZ(local);
               WorldChunk minecraftClientValue = minecraftClient.world.getChunkManager().getWorldChunk(class1923Value, class1923Value2, false);
               if (minecraftClientValue != null) {
                  this.scannedChunks.put(local, new ScannedChunkEntry(class1923Value, class1923Value2));
                  this.scanExecutor.execute(() -> {
                     this.scanChunkSection(minecraftClientValue);
                  });
                  local2++;
               }
            }
         }
      }
   }

   public void scanChunkSection(WorldChunk arg) {
      ClientWorld minecraftClientWorld = minecraftClient.world;
      if (minecraftClientWorld != null && minecraftClientWorld == this.scanWorld && this.isEnabled()) {
         ChunkSection[] var1Value = arg.getSectionArray();
         int var2Value = minecraftClientWorld.getBottomY();
         int var2Value2 = minecraftClientWorld.getBottomY() + minecraftClientWorld.getHeight();
         int var4Snapshot = var2Value;

         for (ChunkSection class2826 : var1Value) {
            if (class2826 != null && !class2826.isEmpty()) {
               for (int index = 0; index < 16; index++) {
                  for (int index2 = 0; index2 < 16; index2++) {
                     for (int index3 = 0; index3 < 16; index3++) {
                        int var6Var13Value = var4Snapshot + index3;
                        if (var6Var13Value > var2Value && var6Var13Value < var2Value2) {
                           BlockPos local = new BlockPos(arg.getPos().getStartX() + index2, var6Var13Value, arg.getPos().getStartZ() + index);
                           this.scanOneByOneHoles(local);
                           this.scanTwoByTwoHoles(local);
                        }
                     }
                  }
               }
            }

            var4Snapshot += 16;
         }
      }
   }

   public void scanOneByOneHoles(BlockPos arg) {
      if (this.isOneByOneHoleTop(arg) && !this.isOneByOneHoleTop(arg.up())) {
         Mutable var1Value = arg.mutableCopy();

         while (this.isOneByOneHoleTop(var1Value)) {
            var1Value.move(Direction.DOWN);
         }

         int var1Value2 = arg.getY() - var1Value.getY();
         if (var1Value2 >= this.getMinHoleHeight()) {
            Box local = new Box(
               arg.getX(), var1Value.getY() + 1, arg.getZ(), arg.getX() + 1, arg.getY() + 1, arg.getZ() + 1
            );
            if (!this.isOverlappingHole(local)) {
               this.holeBoxes.add(new ResourceLoadEntry(local, var1Value2, true));
            }
         }
      }
   }

   public void scanTwoByTwoHoles(BlockPos arg) {
      if (this.isTwoByTwoHoleTopX(arg) && !this.isTwoByTwoHoleTopX(arg.up())) {
         Mutable var1Value = arg.mutableCopy();

         while (this.isTwoByTwoHoleTopX(var1Value)) {
            var1Value.move(Direction.DOWN);
         }

         int var1Value2 = arg.getY() - var1Value.getY();
         if (var1Value2 >= this.getMinHoleHeight()) {
            Box local = new Box(
               arg.getX(), var1Value.getY() + 1, arg.getZ(), arg.getX() + 3, arg.getY() + 1, arg.getZ() + 1
            );
            if (!this.isOverlappingHole(local)) {
               this.holeBoxes.add(new ResourceLoadEntry(local, var1Value2, false));
            }
         }
      }

      if (this.isTwoByTwoHoleTopZ(arg) && !this.isTwoByTwoHoleTopZ(arg.up())) {
         Mutable var1Value3 = arg.mutableCopy();

         while (this.isTwoByTwoHoleTopZ(var1Value3)) {
            var1Value3.move(Direction.DOWN);
         }

         int var1Value4 = arg.getY() - var1Value3.getY();
         if (var1Value4 >= this.getMinHoleHeight()) {
            Box local2 = new Box(
               arg.getX(), var1Value3.getY() + 1, arg.getZ(), arg.getX() + 1, arg.getY() + 1, arg.getZ() + 3
            );
            if (!this.isOverlappingHole(local2)) {
               this.holeBoxes.add(new ResourceLoadEntry(local2, var1Value4, false));
            }
         }
      }
   }

   public boolean isOverlappingHole(Box arg) {

      for (ResourceLoadEntry resourceLoadEntry : this.holeBoxes) {
         if (resourceLoadEntry.id.equals(arg) || resourceLoadEntry.id.intersects(arg)) {
            return true;
         }
      }

      return false;
   }

   public boolean isOccludingBlock(BlockState arg) {
      return arg.getBlock() == Blocks.OAK_LEAVES
         || arg.getBlock() == Blocks.SPRUCE_LEAVES
         || arg.getBlock() == Blocks.BIRCH_LEAVES
         || arg.getBlock() == Blocks.JUNGLE_LEAVES
         || arg.getBlock() == Blocks.ACACIA_LEAVES
         || arg.getBlock() == Blocks.DARK_OAK_LEAVES
         || arg.getBlock() == Blocks.CHERRY_LEAVES
         || arg.getBlock() == Blocks.MANGROVE_LEAVES
         || arg.getBlock() == Blocks.AZALEA_LEAVES
         || arg.getBlock() == Blocks.FLOWERING_AZALEA_LEAVES
         || arg.getBlock() == Blocks.GLASS
         || arg.getBlock() == Blocks.GLASS_PANE
         || arg.getBlock() == Blocks.VINE
         || arg.getBlock() == Blocks.CAVE_VINES
         || arg.getBlock() == Blocks.CAVE_VINES_PLANT
         || arg.getBlock() == Blocks.WEEPING_VINES
         || arg.getBlock() == Blocks.WEEPING_VINES_PLANT
         || arg.getBlock() == Blocks.TWISTING_VINES
         || arg.getBlock() == Blocks.TWISTING_VINES_PLANT
         || arg.getBlock() == Blocks.GLOW_LICHEN
         || arg.getBlock() == Blocks.HANGING_ROOTS
         || arg.getBlock() == Blocks.SPORE_BLOSSOM
         || arg.getBlock() == Blocks.BAMBOO
         || arg.getBlock() == Blocks.BAMBOO_SAPLING
         || arg.getBlock() == Blocks.KELP
         || arg.getBlock() == Blocks.KELP_PLANT
         || arg.getBlock() == Blocks.SEAGRASS
         || arg.getBlock() == Blocks.TALL_SEAGRASS
         || arg.getBlock() == Blocks.SHORT_GRASS
         || arg.getBlock() == Blocks.TALL_GRASS
         || arg.getBlock() == Blocks.FERN
         || arg.getBlock() == Blocks.LARGE_FERN
         || arg.getBlock() == Blocks.SUGAR_CANE
         || arg.getBlock() == Blocks.DEAD_BUSH
         || arg.getBlock() == Blocks.SWEET_BERRY_BUSH;
   }

   public boolean isOpenSpace(BlockPos arg) {
      if (minecraftClient.world == null) {
         return false;
      }

      BlockState minecraftClientValue = minecraftClient.world.getBlockState(arg);
      return !minecraftClientValue.isAir() && !this.isOccludingBlock(minecraftClientValue);
   }

   public boolean isOneByOneHoleTop(BlockPos arg) {
      return this.isOpenGap(arg)
         && this.isOpenSpace(arg.north())
         && this.isOpenSpace(arg.south())
         && this.isOpenSpace(arg.east())
         && this.isOpenSpace(arg.west());
   }

   public boolean isTwoByTwoHoleTopX(BlockPos arg) {
      return this.isOpenGap(arg)
         && this.isOpenGap(arg.east())
         && this.isOpenGap(arg.east(2))
         && this.isOpenSpace(arg.north())
         && this.isOpenSpace(arg.south())
         && this.isOpenSpace(arg.west())
         && this.isOpenSpace(arg.east(3));
   }

   public boolean isTwoByTwoHoleTopZ(BlockPos arg) {
      return this.isOpenGap(arg)
         && this.isOpenGap(arg.south())
         && this.isOpenGap(arg.south(2))
         && this.isOpenSpace(arg.east())
         && this.isOpenSpace(arg.west())
         && this.isOpenSpace(arg.north())
         && this.isOpenSpace(arg.south(3));
   }

   public boolean isOpenGap(BlockPos arg) {
      if (minecraftClient.world == null) {
         return false;
      }

      BlockState minecraftClientValue = minecraftClient.world.getBlockState(arg);
      if (!minecraftClientValue.isAir()) {
         return false;
      }

      BlockState minecraftClientValue2 = minecraftClient.world.getBlockState(arg.down());
      BlockState minecraftClientValue3 = minecraftClient.world.getBlockState(arg.up());
      return !this.isPlantBlock(minecraftClientValue2) && !this.isPlantBlock(minecraftClientValue3) && !this.isLiquidBlock(minecraftClientValue2) && !this.isLiquidBlock(minecraftClientValue3);
   }

   public boolean isPlantBlock(BlockState arg) {
      return arg.getBlock() == Blocks.KELP
         || arg.getBlock() == Blocks.KELP_PLANT
         || arg.getBlock() == Blocks.SEAGRASS
         || arg.getBlock() == Blocks.TALL_SEAGRASS
         || arg.getBlock() == Blocks.VINE
         || arg.getBlock() == Blocks.CAVE_VINES
         || arg.getBlock() == Blocks.CAVE_VINES_PLANT
         || arg.getBlock() == Blocks.WEEPING_VINES
         || arg.getBlock() == Blocks.WEEPING_VINES_PLANT
         || arg.getBlock() == Blocks.TWISTING_VINES
         || arg.getBlock() == Blocks.TWISTING_VINES_PLANT
         || arg.getBlock() == Blocks.GLOW_LICHEN
         || arg.getBlock() == Blocks.HANGING_ROOTS
         || arg.getBlock() == Blocks.SPORE_BLOSSOM;
   }

   public boolean isLiquidBlock(BlockState arg) {
      return arg.getBlock() == Blocks.RAIL
         || arg.getBlock() == Blocks.POWERED_RAIL
         || arg.getBlock() == Blocks.DETECTOR_RAIL
         || arg.getBlock() == Blocks.ACTIVATOR_RAIL
         || arg.getBlock() == Blocks.OAK_FENCE
         || arg.getBlock() == Blocks.DARK_OAK_FENCE
         || arg.getBlock() == Blocks.SPRUCE_FENCE
         || arg.getBlock() == Blocks.COBWEB;
   }

   public void clearCache() {
      this.scannedChunks.clear();
      this.pendingChunks.clear();
      this.queuedChunks.clear();
      this.holeBoxes.clear();
   }

   public void ensureExecutor() {
      if (this.scanExecutor == null || this.scanExecutor.isShutdown()) {
         this.scanExecutor = Executors.newFixedThreadPool(2, item -> {
            Thread local = new Thread(item, "threesix-hole-esp");
            local.setDaemon(true);
            return local;
         });
      }
   }

   public void shutdownExecutor() {
      ExecutorService scanExecutorSnapshot = this.scanExecutor;
      this.scanExecutor = null;
      if (scanExecutorSnapshot != null) {
         scanExecutorSnapshot.shutdown();

         try {
            if (!scanExecutorSnapshot.awaitTermination(500L, TimeUnit.MILLISECONDS)) {
               scanExecutorSnapshot.shutdownNow();
            }
         } catch (InterruptedException interruptedException) {
            scanExecutorSnapshot.shutdownNow();
            Thread.currentThread().interrupt();
         }
      }
   }

   public int getRangeBlocks() {
      return MathHelper.clamp((int)Math.round((Double)this.rangeSetting.getValue()), 16, 128);
   }

   public int getMinHoleHeight() {
      return 7;
   }

   public int clampAlpha(double doubleVal) {
      return MathHelper.clamp((int)Math.round(doubleVal), 0, 255);
   }

   public Color withAlpha(Color color, int intVal) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(intVal, 0, 255));
   }

   public int toArgb(Color color) {
      return color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

}
