package com.threesix.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.module.FreecamModule;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class VoidEspModule extends ModuleBase {
   public final ClientSetting minVoidSizeSetting = new ClientSetting("Min Void Size", 2, 1, 50);
   public final ClientSetting showEspSetting = new ClientSetting("Show ESP", true);
   public final ClientSetting showTracersSetting = new ClientSetting("Show Tracers", false);
   public final ClientSetting espColorSetting = new ClientSetting("ESP Color", new Color(240, 85, 80, 128));
   public final ClientSetting tracerColorSetting = new ClientSetting("Tracer Color", new Color(255, 0, 0, 255));
   private static final List<Integer> overworldLevels = List.of(-64, -63, -62, -61, -60);
   private static final List<Integer> netherFloorLevels = List.of(0, 1, 2, 3, 4);
   private static final List<Integer> netherRoofLevels = List.of(123, 124, 125, 126, 127);
   public final Set<BlockPos> voidPositions = ConcurrentHashMap.newKeySet();
   private ExecutorService scanExecutor;

   public VoidEspModule() {
      super("Bedrock Hole Esp", ModuleCategory.RENDER);
      this.registerSetting(this.minVoidSizeSetting);
      this.registerSetting(this.showEspSetting);
      this.registerSetting(this.showTracersSetting);
      this.registerSetting(this.espColorSetting);
      this.registerSetting(this.tracerColorSetting);
   }

   @Override
   public void onEnable() {
      if (minecraftClient.world != null) {
         if (this.scanExecutor == null || this.scanExecutor.isShutdown()) {
            this.scanExecutor = Executors.newFixedThreadPool(4);
         }

         this.voidPositions.clear();

         for (WorldChunk class2818 : this.collectLoadedChunks()) {
            this.rescanChunk(class2818);
         }
      }
   }

   @Override
   public void onDisable() {

      if (this.scanExecutor != null) {
         this.scanExecutor.shutdownNow();
      }

      this.voidPositions.clear();
   }

   @Override
   public void onTick() {
   }

   private List<WorldChunk> collectLoadedChunks() {
      ArrayList<WorldChunk> arrayListInst = new ArrayList();
      if (minecraftClient.world != null && minecraftClient.player != null) {
         int minecraftClientValue = minecraftClient.options.getClampedViewDistance();
         ChunkPos minecraftClientValue2 = minecraftClient.player.getChunkPos();

         for (int index = -minecraftClientValue; index <= minecraftClientValue; index++) {
            for (int index2 = -minecraftClientValue; index2 <= minecraftClientValue; index2++) {
               WorldChunk minecraftClientValue3 = minecraftClient.world.getChunkManager().getWorldChunk(minecraftClientValue2.x + index, minecraftClientValue2.z + index2, false);
               if (minecraftClientValue3 != null) {
                  arrayListInst.add(minecraftClientValue3);
               }
            }
         }

         return arrayListInst;
      } else {
         return arrayListInst;
      }
   }

   private List<Integer> getScanLevels() {

      if (minecraftClient.world == null) {
         return List.of();
      } else {
         String minecraftClientValue = minecraftClient.world.getRegistryKey().getValue().toString();
         if (minecraftClientValue.equals("minecraft:overworld")) {
            return overworldLevels;
         } else if (minecraftClientValue.equals("minecraft:the_nether")) {
            ArrayList<Integer> arrayListInst = new ArrayList();
            arrayListInst.addAll(netherFloorLevels);
            arrayListInst.addAll(netherRoofLevels);
            return arrayListInst;
         } else {
            return List.of();
         }
      }
   }

   public void rescanChunk(WorldChunk arg) {
      if (minecraftClient.world != null && arg != null) {
         ChunkPos var1Value = arg.getPos();
         this.voidPositions.removeIf(item -> {
            return new ChunkPos(item).equals(var1Value);
         });
         List local = this.getScanLevels();
         if (!local.isEmpty()) {
            this.scanChunkForVoids(arg, local);
         }
      }
   }

   private void scanChunkForVoids(WorldChunk arg, List<Integer> list) {

      ChunkPos var1Value = arg.getPos();
      int var3Value = var1Value.getStartX();
      int var3Value2 = var1Value.getStartZ();
      HashSet hashSetInst = new HashSet();

      for (int int2 : list) {
         for (int index = 0; index < 16; index++) {
            for (int index2 = 0; index2 < 16; index2++) {
               BlockPos local = new BlockPos(var3Value + index, int2, var3Value2 + index2);
               if (!hashSetInst.contains(local) && !this.isBedrock(minecraftClient.world.getBlockState(local))) {
                  List local2 = this.floodFillVoid(local, list, hashSetInst);
                  if (local2.size() >= (Integer)this.minVoidSizeSetting.getValue() && this.isEnclosedRegion(local2)) {
                     this.voidPositions.addAll(local2);
                  }
               }
            }
         }
      }
   }

   private List<BlockPos> floodFillVoid(BlockPos arg, List<Integer> list, Set<BlockPos> set) {
      ArrayList<BlockPos> arrayListInst = new ArrayList();
      LinkedList linkedListInst = new LinkedList();
      linkedListInst.offer(arg);

      while (!linkedListInst.isEmpty() && arrayListInst.size() < 200) {
         BlockPos local = (BlockPos)linkedListInst.poll();
         if (!set.contains(local) && !this.isBedrock(minecraftClient.world.getBlockState(local)) && list.contains(local.getY())) {
            set.add(local);
            arrayListInst.add(local);

            for (Direction class2350 : Direction.values()) {
               BlockPos var6Value = local.offset(class2350);
               if (!set.contains(var6Value)) {
                  linkedListInst.offer(var6Value);
               }
            }
         }
      }

      return arrayListInst;
   }

   private boolean isEnclosedRegion(List<BlockPos> list) {
      HashSet hashSetInst = new HashSet(list);

      for (BlockPos class2338 : list) {
         for (Direction class2350 : Direction.values()) {
            BlockPos var4Value = class2338.offset(class2350);
            if (!hashSetInst.contains(var4Value) && !this.isBedrock(minecraftClient.world.getBlockState(var4Value))) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean isBedrock(BlockState arg) {
      return arg.isOf(Blocks.BEDROCK);
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {

      boolean flag = (Boolean)this.showEspSetting.getValue();
      boolean flag2 = (Boolean)this.showTracersSetting.getValue();
      if (flag || flag2) {
         if (!this.voidPositions.isEmpty() && minecraftClient.world != null && minecraftClient.player != null) {
            Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
            if (textStyleUtilValue != null) {
               Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
               Vec3d textStyleUtilValue3 = TextStyleUtil.getForwardVector(textStyleUtilValue);
               Vec3d textStyleUtilValue4 = TextStyleUtil.getRightVector(textStyleUtilValue);
               Vec3d textStyleUtilValue5 = TextStyleUtil.cross(textStyleUtilValue3, textStyleUtilValue4);
               Vec3d textStyleUtilValue6 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
               Vec3d freecamModuleValue = FreecamModule.getFreecamEyePos(textStyleUtilValue6, floatVal);
               Vec3d local = freecamModuleValue.equals(textStyleUtilValue6) ? textStyleUtilValue3.multiply(0.1) : freecamModuleValue.subtract(textStyleUtilValue6);
               WorldShapeRenderer textStyleUtilValue7 = TextStyleUtil.acquireRenderer(arg);
               Color local2 = (Color)this.espColorSetting.getValue();
               Color local3 = (Color)this.tracerColorSetting.getValue();
               arg.push();
               GL11.glDisable(2929);

               try {
                  if (flag) {
                     for (BlockPos class2338 : this.voidPositions) {
                        double class2338Value = class2338.getX() - textStyleUtilValue2.x;
                        double class2338Value2 = class2338.getY() - textStyleUtilValue2.y;
                        double class2338Value3 = class2338.getZ() - textStyleUtilValue2.z;
                        Color colorInst = new Color(local2.getRed(), local2.getGreen(), local2.getBlue(), 90);
                        textStyleUtilValue7.fillBox(class2338Value + 0.0625, class2338Value2 + 0.0625, class2338Value3 + 0.0625, class2338Value + 0.9375, class2338Value2 + 0.9375, class2338Value3 + 0.9375, colorInst);
                     }
                  }

                  if (flag2) {
                     for (BlockPos class23382 : this.voidPositions) {
                        double class23382Value = class23382.getX() + 0.5 - textStyleUtilValue2.x;
                        double class23382Value2 = class23382.getY() + 0.5 - textStyleUtilValue2.y;
                        double class23382Value3 = class23382.getZ() + 0.5 - textStyleUtilValue2.z;
                        Vec3d local4 = new Vec3d(class23382Value, class23382Value2, class23382Value3);
                        boolean var36Value = local4.dotProduct(textStyleUtilValue3) <= 0.0;
                        Vec3d textStyleUtilValue8;
                        if (var36Value) {
                           textStyleUtilValue8 = TextStyleUtil.buildBillboardMatrix(class23382Value, class23382Value2, class23382Value3, textStyleUtilValue3, textStyleUtilValue4, textStyleUtilValue5, 24.0, 1.2);
                        } else {
                           Vec3d var36Value2 = local4.subtract(local).normalize();
                           textStyleUtilValue8 = local4.subtract(var36Value2.multiply(0.55));
                        }

                        textStyleUtilValue7.drawLine(local3, local, textStyleUtilValue8, 1.0F);
                     }
                  }
               } finally {
                  GL11.glEnable(2929);
                  arg.pop();
               }
            }
         }
      }
   }

}
