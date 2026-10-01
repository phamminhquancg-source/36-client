package com.threesix.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.BlockPos.Mutable;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.module.FreecamModule;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class NetheriteDebugModule extends ModuleBase {
   public static NetheriteDebugModule instance;
   public final ClientSetting maxShownSetting = new ClientSetting("Max Shown", 20, 1, 1000);
   public final ClientSetting fillAlphaSetting = new ClientSetting("Fill Alpha", 150.0, 0.0, 255.0);
   private static final long noiseSeed = 6608149111735331168L;
   private final Map<Long, Set<BlockPos>> positionsByChunk = new ConcurrentHashMap();
   private final Set<Long> chunksWithNetherite = new HashSet();
   private final List<Long> sortedChunks = new ArrayList();

   public NetheriteDebugModule() {
      super("NetheriteDebug", ModuleCategory.DONUT);
      instance = this;
      this.registerSetting(this.maxShownSetting);
      this.registerSetting(this.fillAlphaSetting);
   }

   @Override
   public void onEnable() {
      this.positionsByChunk.clear();
      if (minecraftClient.world != null && minecraftClient.player != null) {

         int local = 12;
         try {
            local = (Integer)minecraftClient.options.getViewDistance().getValue();
         } catch (Throwable error) {
         }

         ChunkPos minecraftClientValue = minecraftClient.player.getChunkPos();

         for (int index = minecraftClientValue.x - local; index <= minecraftClientValue.x + local; index++) {
            for (int index2 = minecraftClientValue.z - local; index2 <= minecraftClientValue.z + local; index2++) {
               if (minecraftClient.world.isChunkLoaded(index, index2)) {
                  try {
                     WorldChunk minecraftClientValue2 = minecraftClient.world.getChunk(index, index2);
                     if (minecraftClientValue2 != null) {
                        this.scanChunk(minecraftClientValue2);
                     }
                  } catch (Throwable error2) {
                  }
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.positionsByChunk.clear();
      this.chunksWithNetherite.clear();
      this.sortedChunks.clear();
   }

   @Override
   public void onPacketSend(Packet arg) {
      if (minecraftClient.world != null) {
         if (arg instanceof ChunkDataS2CPacket local2) {
            try {
               WorldChunk minecraftClientValue = minecraftClient.world.getChunk(local2.getChunkX(), local2.getChunkZ());
               if (minecraftClientValue != null) {
                  this.scanChunk(minecraftClientValue);
               }
            } catch (Throwable error) {
            }
         } else if (arg instanceof ChunkDeltaUpdateS2CPacket local3) {
            local3.visitUpdates((local4, local5) -> {
               try {
                  ChunkPos local7 = new ChunkPos(local4);
                  WorldChunk local6 = minecraftClient.world.getChunk(local7.x, local7.z);
                  if (local6 != null) {
                     this.scanChunk(local6);
                  }
               } catch (Throwable error2) {
               }
            });
         } else if (arg instanceof BlockUpdateS2CPacket local7) {
            if (local7.getState().isOf(Blocks.ANCIENT_DEBRIS)) {
               return;
            }

            try {
               BlockPos var4Value = local7.getPos();
               long class1923Value = ChunkPos.toLong(var4Value.getX() >> 4, var4Value.getZ() >> 4);
               Set local = (Set)this.positionsByChunk.get(class1923Value);
               if (local != null) {
                  local.remove(var4Value);
               }
            } catch (Throwable error3) {
            }
         }
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null && !this.positionsByChunk.isEmpty()) {
         this.chunksWithNetherite.clear();

         for (Set<BlockPos> set : this.positionsByChunk.values()) {
            for (BlockPos class2338 : set) {
               this.chunksWithNetherite.add(ChunkSectionPos.asLong(class2338.getX() >> 4, class2338.getY() >> 4, class2338.getZ() >> 4));
            }
         }

         if (!this.chunksWithNetherite.isEmpty()) {
            double minecraftClientValue = minecraftClient.player.getX();
            double minecraftClientValue2 = minecraftClient.player.getY();
            double minecraftClientValue3 = minecraftClient.player.getZ();
            this.sortedChunks.clear();
            this.sortedChunks.addAll(this.chunksWithNetherite);
            this.sortedChunks.sort((local, local2) -> {
               return Double.compare(getChunkDistanceSq(local, minecraftClientValue, minecraftClientValue2, minecraftClientValue3), getChunkDistanceSq(local2, minecraftClientValue, minecraftClientValue2, minecraftClientValue3));
            });
            int minValue = Math.min((Integer)this.maxShownSetting.getValue(), this.sortedChunks.size());
            Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
            if (textStyleUtilValue != null) {
               Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
               Vec3d textStyleUtilValue3 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
               Vec3d textStyleUtilValue4 = TextStyleUtil.getForwardVector(textStyleUtilValue);
               Vec3d freecamModuleValue = FreecamModule.getFreecamEyePos(textStyleUtilValue3, floatVal);
               if (freecamModuleValue.equals(textStyleUtilValue3)) {
                  textStyleUtilValue4.multiply(0.1);
               } else {
                  freecamModuleValue.subtract(textStyleUtilValue3);
               }

               WorldShapeRenderer textStyleUtilValue5 = TextStyleUtil.acquireRenderer(arg);
               int maxValue = Math.max(0, Math.min(255, ((Double)this.fillAlphaSetting.getValue()).intValue()));
               Color colorInst = new Color(190, 90, 255, maxValue);

               for (int index = 0; index < minValue; index++) {
                  long longVal = (Long)this.sortedChunks.get(index);
                  int class4076Value = ChunkSectionPos.unpackX(longVal);
                  int class4076Value2 = ChunkSectionPos.unpackY(longVal);
                  int class4076Value3 = ChunkSectionPos.unpackZ(longVal);
                  double doubleVal = (class4076Value << 4) - textStyleUtilValue2.x;
                  double doubleVal2 = (class4076Value2 << 4) - textStyleUtilValue2.y;
                  double doubleVal3 = (class4076Value3 << 4) - textStyleUtilValue2.z;
                  double var2516Value = doubleVal + 16.0;
                  double var2716Value = doubleVal2 + 16.0;
                  double var2916Value = doubleVal3 + 16.0;
                  textStyleUtilValue5.fillBox(doubleVal, doubleVal2, doubleVal3, var2516Value, var2716Value, var2916Value, colorInst);
               }
            }
         }
      }
   }

   private static double getChunkDistanceSq(long longVal, double doubleVal, double doubleVal2, double doubleVal3) {
      double doubleVal4 = (ChunkSectionPos.unpackX(longVal) << 4) + 8 - doubleVal;
      double doubleVal5 = (ChunkSectionPos.unpackY(longVal) << 4) + 8 - doubleVal2;
      double doubleVal6 = (ChunkSectionPos.unpackZ(longVal) << 4) + 8 - doubleVal3;
      return doubleVal4 * doubleVal4 + doubleVal5 * doubleVal5 + doubleVal6 * doubleVal6;
   }

   private void scanChunk(WorldChunk arg) {
      if (minecraftClient.world != null) {
         ChunkPos var1Value = arg.getPos();
         long var2Value = var1Value.toLong();
         if (!this.positionsByChunk.containsKey(var2Value)) {
            try {
               String minecraftClientValue = minecraftClient.world.getRegistryKey().getValue().toString();
               if (!minecraftClientValue.contains("nether")) {
                  this.positionsByChunk.put(var2Value, Set.of());
                  return;
               }
            } catch (Throwable error) {
            }

            int intVal = var1Value.x << 4;
            int intVal2 = var1Value.z << 4;
            Random class5819Value = Random.create(0L);
            long class5819Value2 = Random.create(6608149111735331168L ^ intVal * 341873128712L + intVal2 * 132897987541L).nextLong();
            class5819Value = Random.create(class5819Value2);
            HashSet hashSetInst = new HashSet();

            for (int index = 0; index < 2; index++) {
               class5819Value = Random.create(class5819Value2 ^ index * 2654435769L);
               int intVal3 = 1 + class5819Value.nextBetween(0, 1);

               for (int index2 = 0; index2 < intVal3; index2++) {
                  int var7Value = class5819Value.nextInt(16) + intVal;
                  int var7Value2 = class5819Value.nextInt(16) + intVal2;
                  int intVal4 = 8 + class5819Value.nextInt(112);
                  if (index == 0) {
                     intVal4 = 8 + class5819Value.nextInt(16);
                  }

                  BlockPos local = new BlockPos(var7Value, intVal4, var7Value2);
                  hashSetInst.addAll(this.findNetheritePositions(minecraftClient.world, class5819Value, local, 3, 0.0F));
               }
            }

            this.positionsByChunk.put(var2Value, ConcurrentHashMap.newKeySet(Math.max(1, hashSetInst.size())));
            ((Set)this.positionsByChunk.get(var2Value)).addAll(hashSetInst);
         }
      }
   }

   private List findNetheritePositions(World arg, Random arg2, BlockPos arg3, int intVal, float floatVal) {
      float var2Value = arg2.nextFloat() * (float) Math.PI;
      float var48Value = intVal / 8.0F;
      int class3532Value = MathHelper.ceil((intVal / 16.0F * 2.0F + 1.0F) / 2.0F);
      double arg3Value = arg3.getX() + Math.sin(var2Value) * var48Value;
      double arg3Value2 = arg3.getX() - Math.sin(var2Value) * var48Value;
      double arg3Value3 = arg3.getZ() + Math.cos(var2Value) * var48Value;
      double arg3Value4 = arg3.getZ() - Math.cos(var2Value) * var48Value;
      double arg3Value5 = arg3.getY() + arg2.nextInt(3) - 2;
      double arg3Value6 = arg3.getY() + arg2.nextInt(3) - 2;
      int var3Value = arg3.getX() - MathHelper.ceil(var48Value) - class3532Value;
      int var3Value2 = arg3.getY() - 2 - class3532Value;
      int var3Value3 = arg3.getZ() - MathHelper.ceil(var48Value) - class3532Value;
      int intVal2 = 2 * (MathHelper.ceil(var48Value) + class3532Value);
      int intVal3 = 2 * (2 + class3532Value);
      if (var3Value2 > arg.getTopYInclusive()) {
         return List.of();
      }

      BitSet bitSetInst = new BitSet(intVal2 * intVal3 * intVal2);
      Mutable local = new Mutable();
      double[] local2 = new double[intVal * 4];
      ArrayList arrayListInst = new ArrayList();

      for (int index = 0; index < intVal; index++) {
         float floatVal2 = (float)index / intVal;
         double class3532Value2 = MathHelper.lerp(floatVal2, arg3Value, arg3Value2);
         double class3532Value3 = MathHelper.lerp(floatVal2, arg3Value5, arg3Value6);
         double class3532Value4 = MathHelper.lerp(floatVal2, arg3Value3, arg3Value4);
         double arg2Value = arg2.nextDouble() * intVal / 16.0;
         double doubleVal = ((MathHelper.sin((float) Math.PI * floatVal2) + 1.0F) * arg2Value + 1.0) / 2.0;
         local2[index * 4] = class3532Value2;
         local2[index * 4 + 1] = class3532Value3;
         local2[index * 4 + 2] = class3532Value4;
         local2[index * 4 + 3] = doubleVal;
      }

      for (int index2 = 0; index2 < intVal - 1; index2++) {
         if (!(local2[index2 * 4 + 3] <= 0.0)) {
            for (int index3 = index2 + 1; index3 < intVal; index3++) {
               if (!(local2[index3 * 4 + 3] <= 0.0)) {
                  double doubleVal2 = local2[index2 * 4] - local2[index3 * 4];
                  double doubleVal3 = local2[index2 * 4 + 1] - local2[index3 * 4 + 1];
                  double doubleVal4 = local2[index2 * 4 + 2] - local2[index3 * 4 + 2];
                  double doubleVal5 = local2[index2 * 4 + 3] - local2[index3 * 4 + 3];
                  if (doubleVal5 * doubleVal5 > doubleVal2 * doubleVal2 + doubleVal3 * doubleVal3 + doubleVal4 * doubleVal4) {
                     if (doubleVal5 > 0.0) {
                        local2[index3 * 4 + 3] = -1.0;
                     } else {
                        local2[index2 * 4 + 3] = -1.0;
                     }
                  }
               }
            }
         }
      }

      for (int index4 = 0; index4 < intVal; index4++) {
         double doubleVal6 = local2[index4 * 4 + 3];
         if (!(doubleVal6 < 0.0)) {
            double doubleVal7 = local2[index4 * 4];
            double doubleVal8 = local2[index4 * 4 + 1];
            double doubleVal9 = local2[index4 * 4 + 2];
            int maxValue = Math.max(MathHelper.floor(doubleVal7 - doubleVal6), var3Value);
            int maxValue2 = Math.max(MathHelper.floor(doubleVal8 - doubleVal6), var3Value2);
            int maxValue3 = Math.max(MathHelper.floor(doubleVal9 - doubleVal6), var3Value3);
            int maxValue4 = Math.max(MathHelper.floor(doubleVal7 + doubleVal6), maxValue);
            int maxValue5 = Math.max(MathHelper.floor(doubleVal8 + doubleVal6), maxValue2);
            int maxValue6 = Math.max(MathHelper.floor(doubleVal9 + doubleVal6), maxValue3);

            for (int index5 = maxValue; index5 <= maxValue4; index5++) {
               double doubleVal10 = (index5 + 0.5 - doubleVal7) / doubleVal6;
               if (!(doubleVal10 * doubleVal10 >= 1.0)) {
                  for (int index6 = maxValue2; index6 <= maxValue5; index6++) {
                     double doubleVal11 = (index6 + 0.5 - doubleVal8) / doubleVal6;
                     if (!(doubleVal10 * doubleVal10 + doubleVal11 * doubleVal11 >= 1.0)) {
                        for (int index7 = maxValue3; index7 <= maxValue6; index7++) {
                           double doubleVal12 = (index7 + 0.5 - doubleVal9) / doubleVal6;
                           if (!(doubleVal10 * doubleVal10 + doubleVal11 * doubleVal11 + doubleVal12 * doubleVal12 >= 1.0)) {
                              int var45Var21Var48Var22Var24V = index5 - var3Value + (index6 - var3Value2) * intVal2 + (index7 - var3Value3) * intVal2 * intVal3;
                              if (var45Var21Var48Var22Var24V >= 0 && var45Var21Var48Var22Var24V < bitSetInst.size() && !bitSetInst.get(var45Var21Var48Var22Var24V)) {
                                 bitSetInst.set(var45Var21Var48Var22Var24V);
                                 local.set(index5, index6, index7);
                                 if (!arg.isOutOfHeightLimit(index6) && arg.getBlockState(local).isAir() && this.isNetheriteAt(arg, local, floatVal, arg2)) {
                                    arrayListInst.add(new BlockPos(index5, index6, index7));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return arrayListInst;
   }

   private boolean isNetheriteAt(World arg, BlockPos arg2, float floatVal, Random arg3) {
      if (floatVal == 0.0F) {
         return true;
      }

      if (floatVal != 1.0F && arg3.nextFloat() >= floatVal) {
         return true;
      }

      for (Direction class2350 : Direction.values()) {
         if (floatVal == 1.0F) {
            break;
         }

         if (!arg.getBlockState(arg2.offset(class2350)).isAir()) {
            return false;
         }
      }

      return true;
   }

}
