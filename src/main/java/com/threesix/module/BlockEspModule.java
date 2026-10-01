package com.threesix.module;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.module.FreecamModule;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.BlockListSetting;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.BlockEspRenderPoint;
import com.threesix.manager.NotificationHudManager;

public final class BlockEspModule extends ModuleBase {
   public static final int rescanIntervalTicks = 200;
   public static final int maxChunksPerTick = 6;
   public static final long notifyCooldownMs = 750L;
   public static final double blockInset = 0.0625;
   public final BlockListSetting blocksSetting = new BlockListSetting("Blocks", Blocks.SPAWNER);
   public final ClientSetting notificationSetting = new ClientSetting("Notification", true);
   public final ClientSetting tracersSetting = new ClientSetting("Tracers", true);
   public final ClientSetting maxRenderSetting = new ClientSetting("Max Render", 500, 10, 2000);
   public final Map<Long, Set<BlockPos>> chunkPositions = new ConcurrentHashMap();
   public final Map<BlockPos, Block> positionBlocks = new ConcurrentHashMap();
   public final Map<Long, Long> lastNotifyTimes = new ConcurrentHashMap();
   public final ArrayDeque pendingChunks = new ArrayDeque();
   public final Set queuedChunks = new HashSet();
   public final Object queueLock = new Object();
   public final ConcurrentHashMap customColors = new ConcurrentHashMap();
   public volatile Set selectedBlocks = Collections.emptySet();
   public long lastChangeCount = -1L;
   public int tickCounter = 0;
   public boolean isFirstScan = true;
   public ChunkPos lastChunk;
   public int lastRadius = -1;
   public final List<BlockEspRenderPoint> renderPoints = new ArrayList();

   public BlockEspModule() {
      super("Block ESP", ModuleCategory.RENDER);
      this.registerSetting(this.blocksSetting);
      this.registerSetting(this.notificationSetting);
      this.registerSetting(this.tracersSetting);
      this.registerSetting(this.maxRenderSetting);
   }

   @Override
   public void onEnable() {
      this.clearCache();
      this.lastChangeCount = -1L;
      this.isFirstScan = true;
      this.tickCounter = 0;
      this.lastChunk = null;
      this.lastRadius = -1;
   }

   @Override
   public void onDisable() {
      this.clearCache();
      this.lastChunk = null;
      this.lastRadius = -1;
   }

   @Override
   public void onTick() {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         this.refreshBlockSelection();
         if (this.selectedBlocks.isEmpty()) {
            this.clearCache();
         } else {
            this.tickCounter++;
            ChunkPos minecraftClientValue = minecraftClient.player.getChunkPos();
            int intVal = this.getRenderDistanceChunks();
            boolean flag = this.isFirstScan || this.tickCounter % 200 == 0;
            if (flag || this.lastChunk == null || !this.lastChunk.equals(minecraftClientValue) || this.lastRadius != intVal) {
               this.rescanChunks(flag);
               this.isFirstScan = false;
               this.lastChunk = minecraftClientValue;
               this.lastRadius = intVal;
            }

            for (int index = 0; index < 6; index++) {
               Long local;
               synchronized (this.queueLock) {
                  local = (Long)this.pendingChunks.poll();
                  if (local != null) {
                     this.queuedChunks.remove(local);
                  }
               }

               if (local == null) {
                  break;
               }

               int class1923Value = ChunkPos.getPackedX(local);
               int class1923Value2 = ChunkPos.getPackedZ(local);
               WorldChunk minecraftClientValue2 = minecraftClient.world.getChunkManager().getWorldChunk(class1923Value, class1923Value2, false);
               if (minecraftClientValue2 != null) {
                  this.processChunkSection(minecraftClientValue2);
               }
            }
         }
      }
   }

   @Override
   public void onPacketSend(Packet arg) {
      if (minecraftClient.world != null) {
         if (arg instanceof ChunkDataS2CPacket local) {
            this.onBlockChanged(ChunkPos.toLong(local.getChunkX(), local.getChunkZ()), true);
         } else if (arg instanceof ChunkDeltaUpdateS2CPacket local2) {
            local2.visitUpdates((local3, local4) -> {
               this.onBlockChanged(new ChunkPos(local3).toLong(), true);
            });
         } else if (arg instanceof BlockUpdateS2CPacket local5) {
            this.onBlockChanged(new ChunkPos(local5.getPos()).toLong(), true);
         }
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null && !this.chunkPositions.isEmpty()) {
         Set selectedBlocksSnapshot = this.selectedBlocks;
         if (!selectedBlocksSnapshot.isEmpty()) {
            Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
            if (textStyleUtilValue != null) {
               Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
               Vec3d textStyleUtilValue3 = TextStyleUtil.getForwardVector(textStyleUtilValue);
               Vec3d textStyleUtilValue4 = TextStyleUtil.getRightVector(textStyleUtilValue);
               Vec3d textStyleUtilValue5 = TextStyleUtil.cross(textStyleUtilValue3, textStyleUtilValue4);
               Vec3d freecamModuleValue = FreecamModule.getFreecamEyePos(textStyleUtilValue2, floatVal);
               Vec3d local = freecamModuleValue.equals(textStyleUtilValue2) ? textStyleUtilValue3.multiply(0.1) : freecamModuleValue.subtract(textStyleUtilValue2);
               short shortVal = 235;
               int intVal = (Integer)this.maxRenderSetting.getValue();
               double minecraftClientValue = minecraftClient.player.getX();
               double minecraftClientValue2 = minecraftClient.player.getY();
               double minecraftClientValue3 = minecraftClient.player.getZ();
               this.renderPoints.clear();

               for (Set<BlockPos> set : this.chunkPositions.values()) {
                  for (BlockPos class2338 : set) {
                     Block local2 = (Block)this.positionBlocks.get(class2338);
                     if (local2 != null
                        && selectedBlocksSnapshot.contains(local2)
                        && minecraftClient.world.getChunkManager().getWorldChunk(class2338.getX() >> 4, class2338.getZ() >> 4, false) != null
                        && minecraftClient.world.getBlockState(class2338).isOf(local2)) {
                        double class2338Value = class2338.getSquaredDistance(minecraftClientValue, minecraftClientValue2, minecraftClientValue3);
                        Color local3 = this.getBlockColor(local2, 255);
                        Color colorInst = new Color(
                           Math.min(255, (int)(local3.getRed() * 1.4)),
                           Math.min(255, (int)(local3.getGreen() * 1.4)),
                           Math.min(255, (int)(local3.getBlue() * 1.4)),
                           local3.getAlpha()
                        );
                        this.renderPoints
                           .add(
                              new BlockEspRenderPoint(
                                 class2338.getX() - textStyleUtilValue2.x,
                                 class2338.getY() - textStyleUtilValue2.y,
                                 class2338.getZ() - textStyleUtilValue2.z,
                                 colorInst,
                                 class2338Value
                              )
                           );
                     }
                  }
               }

               if (!this.renderPoints.isEmpty()) {
                  this.renderPoints.sort(Comparator.comparingDouble(item -> item.distance));
                  int minValue = Math.min(intVal, this.renderPoints.size());
                  WorldShapeRenderer textStyleUtilValue6 = TextStyleUtil.acquireRenderer(arg);
                  WorldShapeRenderer textStyleUtilValue7 = TextStyleUtil.acquireRenderer(arg);

                  for (int index = minValue - 1; index >= 0; index--) {
                     BlockEspRenderPoint local4 = (BlockEspRenderPoint)this.renderPoints.get(index);
                     Color local5 = withAlpha(local4.color, shortVal);
                     if ((Boolean)this.tracersSetting.getValue()) {
                        double local4Value = local4.x + 0.5;
                        double local4Value2 = local4.y + 0.5;
                        double local4Value3 = local4.z + 0.5;
                        Vec3d local6 = new Vec3d(local4Value, local4Value2, local4Value3);
                        boolean var33Value = local6.dotProduct(textStyleUtilValue3) <= 0.0;
                        Vec3d textStyleUtilValue8;
                        if (var33Value) {
                           textStyleUtilValue8 = TextStyleUtil.buildBillboardMatrix(local4Value, local4Value2, local4Value3, textStyleUtilValue3, textStyleUtilValue4, textStyleUtilValue5, 24.0, 1.2);
                        } else {
                           Vec3d var33Value2 = local6.subtract(local).normalize();
                           textStyleUtilValue8 = local6.subtract(var33Value2.multiply(0.55));
                        }

                        textStyleUtilValue6.drawLine(local5, local, textStyleUtilValue8, 1.0F);
                     }

                     Color local7 = withAlpha(local4.color, 90);
                     textStyleUtilValue7.fillBox(
                        local4.x + 0.0625,
                        local4.y + 0.0625,
                        local4.z + 0.0625,
                        local4.x + 0.9375,
                        local4.y + 0.9375,
                        local4.z + 0.9375,
                        local7
                     );
                  }
               }
            }
         }
      }
   }

   public static int clampAlpha(int intVal) {

      return Math.max(0, Math.min(255, intVal));
   }

   public void refreshBlockSelection() {
      long longVal = this.blocksSetting.getChangeCount();
      if (longVal != this.lastChangeCount) {
         this.lastChangeCount = longVal;
         this.selectedBlocks = Set.copyOf(this.blocksSetting.getSelectedBlocks());
         this.clearCache();
         this.isFirstScan = true;
      }
   }

   public boolean isBlockSelected(Block arg) {
      this.refreshBlockSelection();
      return this.blocksSetting.isSelected(arg);
   }

   public int getSelectedCount() {
      this.refreshBlockSelection();
      return this.blocksSetting.getSelectedCount();
   }

   public Set getSelectedBlocks() {
      return new LinkedHashSet(this.blocksSetting.getSelectedBlocks());
   }

   public boolean isNotifyEnabled() {

      return (Boolean)this.notificationSetting.getValue();
   }

   public void setNotifyEnabled(boolean flag) {

      this.notificationSetting.setValue(flag);
   }

   public boolean isTracersEnabled() {
      return (Boolean)this.tracersSetting.getValue();
   }

   public void setTracersEnabled(boolean flag) {

      this.tracersSetting.setValue(flag);
   }

   public void setSelectedBlocks(Set<Block> set) {

      this.blocksSetting.clearBlocks();

      for (Block class2248 : set) {
         this.blocksSetting.toggleBlock(class2248);
      }

      this.isFirstScan = true;
   }

   public void setBlockColors(Map map) {
      this.customColors.clear();
      this.customColors.putAll(map);
   }

   public Map getBlockColors() {

      return new LinkedHashMap(this.customColors);
   }

   public void rescanChunks(boolean flag) {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         int intVal = this.getRenderDistanceChunks();
         ChunkPos minecraftClientValue = minecraftClient.player.getChunkPos();
         ArrayList<WorldChunk> arrayListInst = new ArrayList();
         HashSet hashSetInst = new HashSet();

         for (int index = -intVal; index <= intVal; index++) {
            for (int index2 = -intVal; index2 <= intVal; index2++) {
               WorldChunk minecraftClientValue2 = minecraftClient.world.getChunkManager().getWorldChunk(minecraftClientValue.x + index, minecraftClientValue.z + index2, false);
               if (minecraftClientValue2 != null) {
                  arrayListInst.add(minecraftClientValue2);
                  hashSetInst.add(minecraftClientValue2.getPos().toLong());
               }
            }
         }

         arrayListInst.sort(Comparator.comparingInt(item -> {
            return this.chunkDistanceSq(minecraftClientValue, item.getPos());
         }));
         synchronized (this.queueLock) {
            this.pendingChunks.removeIf(toRemove -> {

               return !hashSetInst.contains(toRemove);
            });
            this.queuedChunks.retainAll(hashSetInst);

            for (WorldChunk class2818 : arrayListInst) {
               long var15Value = class2818.getPos().toLong();
               if ((flag || !this.chunkPositions.containsKey(var15Value)) && this.queuedChunks.add(var15Value)) {
                  this.pendingChunks.addLast(var15Value);
               }
            }
         }

         this.pruneFarChunks(minecraftClientValue, intVal);
      }
   }

   public void onBlockChanged(long longVal, boolean flag) {

      synchronized (this.queueLock) {
         if (flag && this.queuedChunks.contains(longVal)) {
            this.pendingChunks.remove(longVal);
            this.pendingChunks.addFirst(longVal);
         } else if (this.queuedChunks.add(longVal)) {
            if (flag) {
               this.pendingChunks.addFirst(longVal);
            } else {
               this.pendingChunks.add(longVal);
            }
         }
      }
   }

   public void processChunkSection(WorldChunk arg) {
      Set selectedBlocksSnapshot = this.selectedBlocks;
      if (!selectedBlocksSnapshot.isEmpty()) {
         int minecraftClientValue = minecraftClient.world.getBottomY();
         int minecraftClientValue2 = minecraftClient.world.getBottomY() + minecraftClient.world.getHeight();
         int minecraftClientValue3 = minecraftClient.world.getBottomSectionCoord();
         ChunkPos var1Value = arg.getPos();
         long var6Value = var1Value.toLong();
         Set<BlockPos> local = this.chunkPositions.get(var6Value);
         HashSet hashSetInst = new HashSet();
         Block nullSnapshot = null;
         BlockPos nullSnapshot2 = null;
         ChunkSection[] var1Value2 = arg.getSectionArray();

         for (int index = 0; index < var1Value2.length; index++) {
            ChunkSection local2 = var1Value2[index];
            if (local2 != null && !local2.isEmpty()) {
               int intVal = (minecraftClientValue3 + index) * 16;
               if (intVal + 16 > minecraftClientValue && intVal < minecraftClientValue2 && local2.getBlockStateContainer().hasAny(toRemove -> {
                  return selectedBlocksSnapshot.contains(toRemove.getBlock());
               })) {
                  for (int index2 = 0; index2 < 16; index2++) {
                     for (int index3 = 0; index3 < 16; index3++) {
                        for (int index4 = 0; index4 < 16; index4++) {
                           Block var15Value = local2.getBlockState(index2, index4, index3).getBlock();
                           if (selectedBlocksSnapshot.contains(var15Value)) {
                              BlockPos local3 = new BlockPos(var1Value.getStartX() + index2, intVal + index4, var1Value.getStartZ() + index3);
                              hashSetInst.add(local3);
                              this.positionBlocks.put(local3, var15Value);
                              if (nullSnapshot == null && (local == null || !local.contains(local3))) {
                                 nullSnapshot = var15Value;
                                 nullSnapshot2 = local3;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (local != null) {
            for (BlockPos class2338 : local) {
               if (!hashSetInst.contains(class2338)) {
                  this.positionBlocks.remove(class2338);
               }
            }
         }

         if (hashSetInst.isEmpty()) {
            this.removeChunk(var6Value);
            this.lastNotifyTimes.remove(var6Value);
         } else {
            this.chunkPositions.put(var6Value, hashSetInst);
            if (nullSnapshot != null) {
               this.notifyBlockFound(var6Value, nullSnapshot, nullSnapshot2, var1Value);
            }
         }
      }
   }

   public void notifyBlockFound(long longVal, Block arg, BlockPos arg2, ChunkPos arg3) {
      if ((Boolean)this.notificationSetting.getValue() && minecraftClient.player != null) {
         long systemValue = System.currentTimeMillis();
         long longVal2 = this.lastNotifyTimes.getOrDefault(longVal, 0L);
         if (systemValue - longVal2 >= 750L) {
            this.lastNotifyTimes.put(longVal, systemValue);
            NotificationHudManager.INSTANCE6
               .push(
                  this.getBlockName(arg) + " found",
                  "X " + arg2.getX() + "  Y " + arg2.getY() + "  Z " + arg2.getZ(),
                  this.getBlockState(arg),
                  this.getBlockColor(arg, 255).getRGB()
               );
            minecraftClient.world
               .playSound(
                  minecraftClient.player,
                  minecraftClient.player.getX(),
                  minecraftClient.player.getY(),
                  minecraftClient.player.getZ(),
                  SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                  SoundCategory.MASTER,
                  0.6F,
                  0.95F
               );
         }
      }
   }

   public ItemStack getBlockState(Block arg) {
      ItemStack local = new ItemStack(arg.asItem());
      return local.isEmpty() ? ItemStack.EMPTY : local;
   }

   public int chunkDistanceSq(ChunkPos arg, ChunkPos arg2) {
      int intVal = arg2.x - arg.x;
      int intVal2 = arg2.z - arg.z;
      return intVal * intVal + intVal2 * intVal2;
   }

   public int getRenderDistanceChunks() {
      return minecraftClient.options.getClampedViewDistance();
   }

   public double getMaxRenderDistanceSq() {
      double doubleVal = this.getRenderDistanceChunks() * 16.0 + 16.0;
      return doubleVal * doubleVal;
   }

   public void pruneFarChunks(ChunkPos arg, int intVal) {
      ArrayList<Long> arrayListInst = new ArrayList();

      for (Long long2 : this.chunkPositions.keySet()) {
         ChunkPos local = new ChunkPos(ChunkPos.getPackedX(long2), ChunkPos.getPackedZ(long2));
         if (Math.abs(local.x - arg.x) > intVal || Math.abs(local.z - arg.z) > intVal) {
            arrayListInst.add(long2);
         }
      }

      for (Long long3 : arrayListInst) {
         this.removeChunk(long3);
         this.lastNotifyTimes.remove(long3);
      }
   }

   public void removeChunk(long longVal) {
      Set local = (Set)this.chunkPositions.remove(longVal);
      if (local != null) {
         local.forEach(this.positionBlocks::remove);
      }
   }

   public Color getBlockColor(Block arg, int intVal) {
      Color local = (Color)this.customColors.get(arg);
      if (local != null) {
         return new Color(local.getRed(), local.getGreen(), local.getBlue(), intVal);
      } else {
         Identifier local2 = Registries.BLOCK.getId(arg);
         String local3 = local2 == null ? "" : local2.getPath();
         if (arg == Blocks.SPAWNER) {
            return new Color(138, 126, 166, intVal);
         } else if (local3.contains("diamond")) {
            return new Color(0, 255, 255, intVal);
         } else if (local3.contains("ancient_debris")) {
            return new Color(196, 120, 72, intVal);
         } else if (local3.contains("emerald")) {
            return new Color(0, 255, 127, intVal);
         } else if (local3.contains("gold")) {
            return new Color(255, 215, 0, intVal);
         } else if (local3.contains("iron")) {
            return new Color(213, 213, 213, intVal);
         } else if (local3.contains("redstone")) {
            return new Color(255, 70, 70, intVal);
         } else {
            return local3.contains("lapis") ? new Color(70, 110, 255, intVal) : new Color(255, 255, 0, intVal);
         }
      }
   }

   public static Color withAlpha(Color color, int intVal) {

      return new Color(color.getRed(), color.getGreen(), color.getBlue(), intVal);
   }

   public String getBlockName(Block arg) {
      try {
         return arg.getName().getString();
      } catch (Exception error) {
         Identifier local = Registries.BLOCK.getId(arg);
         return local == null ? "Block" : local.toString();
      }
   }

   public void clearCache() {
      this.chunkPositions.clear();
      this.positionBlocks.clear();
      this.lastNotifyTimes.clear();
      synchronized (this.queueLock) {
         this.pendingChunks.clear();
         this.queuedChunks.clear();
      }
   }

}
