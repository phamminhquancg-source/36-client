package com.threesix.module;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.module.FreecamModule;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.BlockListSetting;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.internal.ModuleBase;

public final class BlockNotifierModule extends ModuleBase {
   public static final int maxScannedBlocks = 32;
   public static final int rescanInterval = 5;
   public static final double tracerWidth = 0.035;
   public final BlockListSetting blocksSetting = new BlockListSetting("Blocks", Blocks.HOPPER);
   public final ClientSetting chatNotifySetting = new ClientSetting("Chat Notify", true);
   public final ClientSetting soundSetting = new ClientSetting("Sound", true);
   public final ClientSetting espSetting = new ClientSetting("ESP", true);
   public final ClientSetting tracersSetting = new ClientSetting("Tracers", false);
   public final ClientSetting scanRadiusSetting = new ClientSetting("Scan Radius", 8, 1, 16);
   public final ClientSetting fillAlphaSetting = new ClientSetting("Fill Alpha", 80, 0, 255);
   public final Map<Long, Set<BlockPos>> positionsByChunk = new ConcurrentHashMap<>();
   public final Map<BlockPos, Block> blockTypeByPos = new ConcurrentHashMap<>();
   public final Set<BlockPos> notifiedBlocks = ConcurrentHashMap.newKeySet();
   public final ArrayDeque<Long> notifyQueue = new ArrayDeque<>();
   public final Set<Long> notifiedPositions = new HashSet<>();
   public final Object scanLock = new Object();
   public volatile Set<Block> selectedBlocks = Collections.emptySet();
   public long lastChangeCount = -1L;
   public int rescanCounter = 0;
   public ChunkPos lastCenter = null;
   public int cachedRadius = -1;
   public int cachedCount = -1;
   public int cachedSetHash = 0;

   public BlockNotifierModule() {
      super("Block Notifier", ModuleCategory.RENDER);
      this.registerSetting(this.blocksSetting);
      this.registerSetting(this.chatNotifySetting);
      this.registerSetting(this.soundSetting);
      this.registerSetting(this.espSetting);
      this.registerSetting(this.tracersSetting);
      this.registerSetting(this.scanRadiusSetting);
      this.registerSetting(this.fillAlphaSetting);
   }

   @Override
   public void onEnable() {
      this.clearScanResults();
      this.lastChangeCount = -1L;
      this.rescanCounter = 0;
      this.lastCenter = null;
      this.cachedRadius = -1;
      this.cachedCount = -1;
      this.cachedSetHash = 0;
      this.rescanNow();
   }

   @Override
   public void onDisable() {

      this.clearScanResults();
      this.lastCenter = null;
      this.cachedRadius = -1;
      this.cachedCount = -1;
      this.cachedSetHash = 0;
   }

   @Override
   public void onTick() {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         this.refreshBlockSelection();
         if (this.selectedBlocks.isEmpty()) {
            this.clearScanResults();
         } else {
            ChunkPos minecraftClientValue = minecraftClient.player.getChunkPos();
            int minValue = Math.min((Integer)this.scanRadiusSetting.getValue(), minecraftClient.options.getClampedViewDistance());
            int intVal = this.countFoundBlocks(minecraftClientValue, minValue);
            int intVal2 = this.selectedBlocks.hashCode();
            int intVal3 = this.lastCenter != null && this.lastCenter.equals(minecraftClientValue) ? 0 : 1;
            boolean flag = this.cachedRadius != minValue;
            boolean flag2 = this.cachedCount != intVal;
            boolean flag3 = this.cachedSetHash != intVal2;
            boolean flag4 = ++this.rescanCounter >= 5;
            if (intVal3 != 0 || flag || flag2 || flag3 || flag4 || this.notifyQueue.isEmpty()) {
               this.rescanCounter = 0;
               this.handleScan(minecraftClientValue, minValue);
               this.lastCenter = minecraftClientValue;
               this.cachedRadius = minValue;
               this.cachedCount = intVal;
               this.cachedSetHash = intVal2;
            }

            for (int index = 0; index < 32; index++) {
               Long local;
               synchronized (this.scanLock) {
                  local = (Long)this.notifyQueue.pollFirst();
                  if (local != null) {
                     this.notifiedPositions.remove(local);
                  }
               }

               if (local == null) {
                  break;
               }

               int class1923Value = ChunkPos.getPackedX(local);
               int class1923Value2 = ChunkPos.getPackedZ(local);
               WorldChunk minecraftClientValue2 = minecraftClient.world.getChunkManager().getWorldChunk(class1923Value, class1923Value2, false);
               if (minecraftClientValue2 != null) {
                  this.scanChunk(minecraftClientValue2);
               }
            }
         }
      }
   }

   public void refreshBlockSelection() {
      long longVal = this.blocksSetting.getChangeCount();
      if (longVal != this.lastChangeCount) {
         this.lastChangeCount = longVal;
         this.selectedBlocks = Set.copyOf((Set<Block>)this.blocksSetting.getSelectedBlocks());
         this.clearScanResults();
         if (minecraftClient.world != null && minecraftClient.player != null) {
            this.rescanNow();
         }
      }
   }

   public void rescanNow() {

      if (minecraftClient.world != null && minecraftClient.player != null) {
         ChunkPos minecraftClientValue = minecraftClient.player.getChunkPos();
         int minValue = Math.min((Integer)this.scanRadiusSetting.getValue(), minecraftClient.options.getClampedViewDistance());
         this.handleScan(minecraftClientValue, minValue);
         this.lastCenter = minecraftClientValue;
         this.cachedRadius = minValue;
         this.cachedCount = this.countFoundBlocks(minecraftClientValue, minValue);
      }
   }

   public int countFoundBlocks(ChunkPos arg, int intVal) {
      int local = 0;
      for (int index = -intVal; index <= intVal; index++) {
         for (int index2 = -intVal; index2 <= intVal; index2++) {
            WorldChunk minecraftClientValue = minecraftClient.world.getChunkManager().getWorldChunk(arg.x + index, arg.z + index2, false);
            if (minecraftClientValue != null && !minecraftClientValue.isEmpty()) {
               local++;
            }
         }
      }

      return local;
   }

   public void handleScan(ChunkPos arg, int intVal) {
      ArrayList<WorldChunk> arrayListInst = new ArrayList<>();
      HashSet<Long> hashSetInst = new HashSet<>();

      for (int index = -intVal; index <= intVal; index++) {
         for (int index2 = -intVal; index2 <= intVal; index2++) {
            WorldChunk minecraftClientValue = minecraftClient.world.getChunkManager().getWorldChunk(arg.x + index, arg.z + index2, false);
            if (minecraftClientValue != null && !minecraftClientValue.isEmpty()) {
               arrayListInst.add(minecraftClientValue);
               hashSetInst.add(minecraftClientValue.getPos().toLong());
            }
         }
      }

      arrayListInst.sort(Comparator.comparingInt(item -> {
         return this.getBlockDistanceSq(arg, item.getPos());
      }));
      synchronized (this.scanLock) {
         this.notifyQueue.removeIf(toRemove -> {

            return !hashSetInst.contains(toRemove);
         });
         this.notifiedPositions.retainAll(hashSetInst);

         for (WorldChunk class2818 : arrayListInst) {
            long var14Value = class2818.getPos().toLong();
            if (this.notifiedPositions.add(var14Value)) {
               this.notifyQueue.addLast(var14Value);
            }
         }
      }

      this.removeChunkData(arg, intVal);
   }

   public int getBlockDistanceSq(ChunkPos arg, ChunkPos arg2) {
      int intVal = arg.x - arg2.x;
      int intVal2 = arg.z - arg2.z;
      return intVal * intVal + intVal2 * intVal2;
   }

   public void scanChunk(WorldChunk arg) {

      Set<Block> selectedBlocksSnapshot = this.selectedBlocks;
      if (!selectedBlocksSnapshot.isEmpty() && minecraftClient.world != null) {
         ChunkPos var1Value = arg.getPos();
         long var3Value = var1Value.toLong();
         Set<BlockPos> local = this.positionsByChunk.get(var3Value);
         HashSet<BlockPos> hashSetInst = new HashSet<>();
         int minecraftClientValue = minecraftClient.world.getBottomSectionCoord();
         ChunkSection[] var1Value2 = arg.getSectionArray();

         for (int index = 0; index < var1Value2.length; index++) {
            int intVal = (minecraftClientValue + index) * 16;
            int var1115Value = intVal + 15;
            if (intVal > -2) {
               break;
            }

            if (var1115Value >= minecraftClient.world.getBottomY()) {
               ChunkSection local2 = var1Value2[index];
               if (local2 != null && !local2.isEmpty() && local2.hasAny(toRemove -> {

                  return selectedBlocksSnapshot.contains(toRemove.getBlock());
               })) {
                  for (int index2 = 0; index2 < 16; index2++) {
                     int var11Var14Value = intVal + index2;
                     if (var11Var14Value <= -2) {
                        for (int index3 = 0; index3 < 16; index3++) {
                           for (int index4 = 0; index4 < 16; index4++) {
                              BlockState var13Value = local2.getBlockState(index3, index2, index4);
                              Block var18Value = var13Value.getBlock();
                              if (selectedBlocksSnapshot.contains(var18Value)) {
                                 BlockPos local3 = new BlockPos(var1Value.getStartX() + index3, var11Var14Value, var1Value.getStartZ() + index4);
                                 hashSetInst.add(local3);
                                 this.blockTypeByPos.put(local3, var18Value);
                                 if (this.notifiedBlocks.add(local3)) {
                                    this.notifyBlockFound(var18Value, local3);
                                 }
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
                  this.blockTypeByPos.remove(class2338);
                  this.notifiedBlocks.remove(class2338);
               }
            }
         }

         if (hashSetInst.isEmpty()) {
            this.positionsByChunk.remove(var3Value);
         } else {
            this.positionsByChunk.put(var3Value, hashSetInst);
         }
      }
   }

   public void notifyBlockFound(Block arg, BlockPos arg2) {
      String local = this.getBlockName(arg) + " found (" + arg2.getX() + ", " + arg2.getY() + ", " + arg2.getZ() + ")";
      if ((Boolean)this.chatNotifySetting.getValue() && minecraftClient.inGameHud != null) {
         minecraftClient.inGameHud.getChatHud().addMessage(Text.literal("§ §f" + local));
      }

      try {
         HudModule.pushToast(
            this.getBlockName(arg) + " found", arg2.getX() + " " + arg2.getY() + " " + arg2.getZ(), HudModule.toastEnabledColor, null
         );
      } catch (Throwable error) {
      }

      if ((Boolean)this.soundSetting.getValue() && minecraftClient.world != null && minecraftClient.player != null) {
         minecraftClient.world
            .playSound(
               minecraftClient.player,
               minecraftClient.player.getX(),
               minecraftClient.player.getY(),
               minecraftClient.player.getZ(),
               SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
               SoundCategory.MASTER,
               0.55F,
               1.1F
            );
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null
         && minecraftClient.player != null
         && !this.positionsByChunk.isEmpty()
         && ((Boolean)this.espSetting.getValue() || (Boolean)this.tracersSetting.getValue())) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            Vec3d textStyleUtilValue3 = TextStyleUtil.getForwardVector(textStyleUtilValue);
            Vec3d textStyleUtilValue4 = TextStyleUtil.getRightVector(textStyleUtilValue);
            Vec3d textStyleUtilValue5 = TextStyleUtil.cross(textStyleUtilValue3, textStyleUtilValue4);
            Vec3d freecamModuleValue = FreecamModule.getFreecamEyePos(textStyleUtilValue2, floatVal);
            Vec3d local = freecamModuleValue.equals(textStyleUtilValue2) ? textStyleUtilValue3.multiply(0.1) : freecamModuleValue.subtract(textStyleUtilValue2);
            WorldShapeRenderer textStyleUtilValue6 = TextStyleUtil.acquireRenderer(arg);
            byte byteVal = 0;
            arg.push();
            GL11.glDisable(2929);

            try {
               for (Set<BlockPos> set : this.positionsByChunk.values()) {
                  for (BlockPos class2338 : set) {
                     if (minecraftClient.world.getChunkManager().getWorldChunk(class2338.getX() >> 4, class2338.getZ() >> 4, false) != null) {
                        Block local2 = (Block)this.blockTypeByPos.get(class2338);
                        if (local2 != null && this.selectedBlocks.contains(local2) && minecraftClient.world.getBlockState(class2338).isOf(local2)) {
                           Color local3 = this.getBlockColor(local2, (Integer)this.fillAlphaSetting.getValue());
                           Color local4 = this.getBlockColor(local2, 255);
                           double class2338Value = class2338.getX() - textStyleUtilValue2.x;
                           double class2338Value2 = class2338.getY() - textStyleUtilValue2.y;
                           double class2338Value3 = class2338.getZ() - textStyleUtilValue2.z;
                           if ((Boolean)this.espSetting.getValue()) {
                              Color colorInst = new Color(local3.getRed(), local3.getGreen(), local3.getBlue(), 90);
                              textStyleUtilValue6.fillBox(class2338Value + 0.0625, class2338Value2 + 0.0625, class2338Value3 + 0.0625, class2338Value + 0.9375, class2338Value2 + 0.9375, class2338Value3 + 0.9375, colorInst);
                           }

                           if ((Boolean)this.tracersSetting.getValue()) {
                              double var190Value = class2338Value + 0.5;
                              double var210Value = class2338Value2 + 0.5;
                              double var230Value = class2338Value3 + 0.5;
                              Vec3d local5 = new Vec3d(var190Value, var210Value, var230Value);
                              boolean var31Value = local5.dotProduct(textStyleUtilValue3) <= 0.0;
                              Vec3d textStyleUtilValue7;
                              if (var31Value) {
                                 textStyleUtilValue7 = TextStyleUtil.buildBillboardMatrix(var190Value, var210Value, var230Value, textStyleUtilValue3, textStyleUtilValue4, textStyleUtilValue5, 24.0, 1.2);
                              } else {
                                 Vec3d var31Value2 = local5.subtract(local).normalize();
                                 textStyleUtilValue7 = local5.subtract(var31Value2.multiply(0.55));
                              }

                              textStyleUtilValue6.drawLine(local4, local, textStyleUtilValue7, 1.0F);
                           }

                        }
                     }
                  }
               }

               if (byteVal != 0) {
               }
            } finally {
               GL11.glEnable(2929);
               arg.pop();
            }
         }
      }
   }

   public void removeChunkData(ChunkPos arg, int intVal) {
      ArrayList<Long> arrayListInst = new ArrayList<>();

      for (Long long2 : this.positionsByChunk.keySet()) {
         ChunkPos local = new ChunkPos(ChunkPos.getPackedX(long2), ChunkPos.getPackedZ(long2));
         if (Math.abs(local.x - arg.x) > intVal || Math.abs(local.z - arg.z) > intVal) {
            arrayListInst.add(long2);
         }
      }

      for (Long long3 : arrayListInst) {
         Set<BlockPos> local2 = this.positionsByChunk.remove(long3);
         if (local2 != null) {
            for (BlockPos class2338 : local2) {
               this.blockTypeByPos.remove(class2338);
               this.notifiedBlocks.remove(class2338);
            }
         }
      }
   }

   public void clearScanResults() {
      this.positionsByChunk.clear();
      this.blockTypeByPos.clear();
      this.notifiedBlocks.clear();
      synchronized (this.scanLock) {
         this.notifyQueue.clear();
         this.notifiedPositions.clear();
      }
   }

   public Color getBlockColor(Block arg, int intVal) {
      return new Color(255, 255, 255, intVal);
   }

   public String getBlockName(Block arg) {
      try {
         return arg.getName().getString();
      } catch (Throwable error) {
         Identifier local = Registries.BLOCK.getId(arg);
         return local == null ? "Block" : local.toString();
      }
   }

}
