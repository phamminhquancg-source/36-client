package com.threesix.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3fc;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.BlockListSetting;
import com.threesix.setting.ClientSetting;

public final class StorageEspModule extends ModuleBase {
   public static StorageEspModule instance;
   public final ClientSetting chestSetting = new ClientSetting("Chest", true);
   public final ClientSetting enderChestSetting = new ClientSetting("Ender Chest", true);
   public final ClientSetting spawnerSetting = new ClientSetting("Spawner", true);
   public final ClientSetting shulkerBoxSetting = new ClientSetting("Shulker Box", true);
   public final ClientSetting useShulkerDyesSetting = new ClientSetting("Use Shulker Dyes", true);
   public final ClientSetting furnaceSetting = new ClientSetting("Furnace", true);
   public final ClientSetting barrelSetting = new ClientSetting("Barrel", true);
   public final ClientSetting enchantingTableSetting = new ClientSetting("Enchanting Table", true);
   public final ClientSetting movingPistonSetting = new ClientSetting("Moving Piston", true);
   public final ClientSetting hopperSetting = new ClientSetting("Hopper", true);
   public final ClientSetting tracersSetting = new ClientSetting("Tracers", true);
   public final BlockListSetting blocksSetting = new BlockListSetting("Blocks");
   public final Map<Block, Color> blockColors = new ConcurrentHashMap();
   public final Map<BlockPos, Block> trackedBlocks = new ConcurrentHashMap();
   public final Map<BlockPos, Color> positionColors = new ConcurrentHashMap();
   public final Map<ChunkPos, Map<BlockPos, Block>> blocksByChunk = new ConcurrentHashMap();
   public final Map<ChunkPos, Map<BlockPos, Color>> colorsByChunk = new ConcurrentHashMap();
   public final ExecutorService scanExecutor = Executors.newSingleThreadExecutor(local -> {

      Thread threadInst = new Thread(local, "storageESP-scan");
      threadInst.setDaemon(true);
      return threadInst;
   });
   public final AtomicBoolean isScanning = new AtomicBoolean(false);
   public int scanTickCounter = 0;
   public static final int maxRenderCount = 10;

   public StorageEspModule() {
      super("Storage ESP", ModuleCategory.RENDER);
      instance = this;
      this.registerSetting(this.chestSetting);
      this.registerSetting(this.enderChestSetting);
      this.registerSetting(this.spawnerSetting);
      this.registerSetting(this.shulkerBoxSetting);
      this.registerSetting(this.useShulkerDyesSetting);
      this.registerSetting(this.furnaceSetting);
      this.registerSetting(this.barrelSetting);
      this.registerSetting(this.enchantingTableSetting);
      this.registerSetting(this.movingPistonSetting);
      this.registerSetting(this.hopperSetting);
      this.registerSetting(this.tracersSetting);
      this.registerSetting(this.blocksSetting);
   }

   public Map getBuiltinColors() {

      LinkedHashMap linkedHashMapInst = new LinkedHashMap();
      if ((Boolean)this.chestSetting.getValue()) {
         linkedHashMapInst.put(Blocks.CHEST, this.blockColors.getOrDefault(Blocks.CHEST, new Color(156, 91, 0)));
         linkedHashMapInst.put(Blocks.TRAPPED_CHEST, this.blockColors.getOrDefault(Blocks.TRAPPED_CHEST, new Color(200, 91, 0)));
      }

      if ((Boolean)this.enderChestSetting.getValue()) {
         linkedHashMapInst.put(Blocks.ENDER_CHEST, this.blockColors.getOrDefault(Blocks.ENDER_CHEST, new Color(117, 0, 255)));
      }

      if ((Boolean)this.spawnerSetting.getValue()) {
         linkedHashMapInst.put(Blocks.SPAWNER, this.blockColors.getOrDefault(Blocks.SPAWNER, new Color(138, 126, 166)));
      }

      if ((Boolean)this.shulkerBoxSetting.getValue()) {
         linkedHashMapInst.put(Blocks.PURPLE_SHULKER_BOX, this.blockColors.getOrDefault(Blocks.PURPLE_SHULKER_BOX, new Color(134, 0, 158)));
      }

      if ((Boolean)this.furnaceSetting.getValue()) {
         linkedHashMapInst.put(Blocks.FURNACE, this.blockColors.getOrDefault(Blocks.FURNACE, new Color(125, 125, 125)));
      }

      if ((Boolean)this.barrelSetting.getValue()) {
         linkedHashMapInst.put(Blocks.BARREL, this.blockColors.getOrDefault(Blocks.BARREL, new Color(255, 140, 140)));
      }

      if ((Boolean)this.enchantingTableSetting.getValue()) {
         linkedHashMapInst.put(Blocks.ENCHANTING_TABLE, this.blockColors.getOrDefault(Blocks.ENCHANTING_TABLE, new Color(80, 80, 255)));
      }

      if ((Boolean)this.movingPistonSetting.getValue()) {
         linkedHashMapInst.put(Blocks.PISTON, this.blockColors.getOrDefault(Blocks.PISTON, new Color(35, 226, 0)));
      }

      if ((Boolean)this.hopperSetting.getValue()) {
         linkedHashMapInst.put(Blocks.HOPPER, this.blockColors.getOrDefault(Blocks.HOPPER, new Color(35, 226, 0)));
      }

      return linkedHashMapInst;
   }

   public void setBlockColor(Block arg, Color color) {
      this.blockColors.put(arg, color);
   }

   public Map getBlockColorMap() {
      return this.blockColors;
   }

   public void replaceBlockColors(Map map) {
      this.blockColors.clear();
      this.blockColors.putAll(map);
   }

   @Override
   public void onEnable() {
      this.trackedBlocks.clear();
      this.positionColors.clear();
      this.blocksByChunk.clear();
      this.colorsByChunk.clear();
      this.scanTickCounter = 0;
   }

   @Override
   public void onDisable() {
      this.trackedBlocks.clear();
      this.positionColors.clear();
      this.blocksByChunk.clear();
      this.colorsByChunk.clear();
   }

   @Override
   public void onTick() {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         if (++this.scanTickCounter >= 10 && !this.isScanning.get()) {
            this.scanTickCounter = 0;
            this.startScan();
         }
      } else {
         this.trackedBlocks.clear();
         this.positionColors.clear();
         this.blocksByChunk.clear();
         this.colorsByChunk.clear();
      }
   }

   public void startScan() {
      this.isScanning.set(true);
      if (minecraftClient.world != null && minecraftClient.player != null) {
         BlockPos minecraftClientValue = minecraftClient.player.getBlockPos();
         int var1Value = minecraftClientValue.getX() >> 4;
         int var1Value2 = minecraftClientValue.getZ() >> 4;
         int maxValue = Math.max(4, minecraftClient.options.getClampedViewDistance() + 2);
         boolean flag = this.blocksSetting.getSelectedCount() > 0;
         ArrayList<WorldChunk> arrayListInst = new ArrayList();
         HashSet<ChunkPos> hashSetInst = new HashSet();

         try {
            for (int index = var1Value - maxValue; index <= var1Value + maxValue; index++) {
               for (int index2 = var1Value2 - maxValue; index2 <= var1Value2 + maxValue; index2++) {
                  WorldChunk minecraftClientValue2 = minecraftClient.world.getChunkManager().getWorldChunk(index, index2, false);
                  if (minecraftClientValue2 != null) {
                     arrayListInst.add(minecraftClientValue2);
                     hashSetInst.add(minecraftClientValue2.getPos());
                  }
               }
            }
         } catch (Exception error) {
            this.isScanning.set(false);
            return;
         }

         this.scanExecutor.execute(() -> {
            try {
               HashMap local11 = new HashMap();
               HashMap local13 = new HashMap();

               for (WorldChunk class2818 : arrayListInst) {
                  try {
                     ChunkPos local12 = class2818.getPos();
                     HashMap local6 = new HashMap();
                     HashMap local7 = new HashMap();

                     for (Entry entry : (Iterable<Entry>)new HashMap(class2818.getBlockEntities()).entrySet()) {
                        BlockEntity local5 = (BlockEntity)entry.getValue();
                        if (local5 != null && this.isTrackedBlockEntity(local5)) {
                           local6.put((BlockPos)entry.getKey(), local5.getCachedState().getBlock());
                           local7.put((BlockPos)entry.getKey(), this.resolveBlockEntityColor(local5));
                        }
                     }

                     if (flag) {
                        try {
                           ChunkSection[] var7xValue = class2818.getSectionArray();
                           int var7xValue2 = class2818.getPos().x << 4;
                           int var7xValue3 = class2818.getBottomY();
                           int local10 = class2818.getPos().z << 4;

                           for (int index3 = 0; index3 < var7xValue.length; index3++) {
                              ChunkSection local8 = var7xValue[index3];
                              if (local8 != null && !local8.isEmpty()) {
                                 int local9 = var7xValue3 + index3 * 16;

                                 for (int index4 = 0; index4 < 16; index4++) {
                                    for (int index5 = 0; index5 < 16; index5++) {
                                       for (int index6 = 0; index6 < 16; index6++) {
                                          Block var16xValue = local8.getBlockState(index4, index6, index5).getBlock();
                                          if (this.blocksSetting.isSelected(var16xValue)) {
                                             BlockPos local = new BlockPos(var7xValue2 + index4, local9 + index6, local10 + index5);
                                             if (!local6.containsKey(local)) {
                                                local6.put(local, var16xValue);
                                                local7.put(local, this.blockColors.getOrDefault(var16xValue, new Color(0, 200, 255)));
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        } catch (Exception error2) {
                        }
                     }

                     if (!local6.isEmpty()) {
                        local11.put(local12, local6);
                        local13.put(local12, local7);
                     }
                  } catch (Exception error3) {
                  }
               }

               for (Entry entry2 : (Iterable<Entry>)local11.entrySet()) {
                  ChunkPos local2 = (ChunkPos)entry2.getKey();
                  Map local3 = (Map)this.blocksByChunk.get(local2);
                  if (local3 != null) {
                     local3.clear();
                  }

                  this.blocksByChunk.put(local2, (Map)entry2.getValue());
                  Map local4 = (Map)this.colorsByChunk.get(local2);
                  if (local4 != null) {
                     local4.clear();
                  }

                  this.colorsByChunk.put(local2, (Map)local13.get(local2));
               }

               if (minecraftClient.world != null) {
                  for (WorldChunk class28182 : arrayListInst) {
                     ChunkPos var38Value = class28182.getPos();
                     if (!local11.containsKey(var38Value)) {
                        this.blocksByChunk.remove(var38Value);
                        this.colorsByChunk.remove(var38Value);
                     }
                  }

                  for (ChunkPos class1923 : new HashSet<>(this.blocksByChunk.keySet())) {
                     if (!hashSetInst.contains(class1923) && !minecraftClient.world.isChunkLoaded(class1923.x, class1923.z)) {
                        this.blocksByChunk.remove(class1923);
                        this.colorsByChunk.remove(class1923);
                     }
                  }
               }

               this.trackedBlocks.clear();
               this.positionColors.clear();

               for (Entry entry3 : this.blocksByChunk.entrySet()) {
                  this.trackedBlocks.putAll((Map)entry3.getValue());
               }

               for (Entry entry4 : this.colorsByChunk.entrySet()) {
                  this.positionColors.putAll((Map)entry4.getValue());
               }
            } catch (Exception error4) {
            } finally {
               this.isScanning.set(false);
            }
         });
      } else {
         this.isScanning.set(false);
      }
   }

   public boolean isTrackedBlockEntity(BlockEntity arg) {
      if (arg instanceof ChestBlockEntity && (Boolean)this.chestSetting.getValue()) {
         return true;
      } else if (arg instanceof TrappedChestBlockEntity && (Boolean)this.chestSetting.getValue()) {
         return true;
      } else if (arg instanceof EnderChestBlockEntity && (Boolean)this.enderChestSetting.getValue()) {
         return true;
      } else if (arg instanceof MobSpawnerBlockEntity && (Boolean)this.spawnerSetting.getValue()) {
         return true;
      } else if (arg instanceof ShulkerBoxBlockEntity && (Boolean)this.shulkerBoxSetting.getValue()) {
         return true;
      } else if (arg instanceof FurnaceBlockEntity && (Boolean)this.furnaceSetting.getValue()) {
         return true;
      } else if (arg instanceof BlastFurnaceBlockEntity && (Boolean)this.furnaceSetting.getValue()) {
         return true;
      } else if (arg instanceof SmokerBlockEntity && (Boolean)this.furnaceSetting.getValue()) {
         return true;
      } else if (arg instanceof BarrelBlockEntity && (Boolean)this.barrelSetting.getValue()) {
         return true;
      } else if (arg instanceof EnchantingTableBlockEntity && (Boolean)this.enchantingTableSetting.getValue()) {
         return true;
      } else {
         return arg instanceof PistonBlockEntity && (Boolean)this.movingPistonSetting.getValue() ? true : arg instanceof HopperBlockEntity && (Boolean)this.hopperSetting.getValue();
      }
   }

   public Color resolveBlockEntityColor(BlockEntity arg) {

      Block var1Value = arg.getCachedState().getBlock();
      if (this.blockColors.containsKey(var1Value)) {
         return (Color)this.blockColors.get(var1Value);
      }

      if (arg instanceof ShulkerBoxBlockEntity && (Boolean)this.shulkerBoxSetting.getValue()) {
         if ((Boolean)this.useShulkerDyesSetting.getValue()) {
            Color local = this.getShulkerDyeColor(var1Value);
            if (local != null) {
               return local;
            }
         }

         return new Color(134, 0, 158);
      } else if (arg instanceof TrappedChestBlockEntity) {
         return new Color(200, 91, 0);
      } else if (arg instanceof ChestBlockEntity) {
         return new Color(156, 91, 0);
      } else if (arg instanceof EnderChestBlockEntity) {
         return new Color(117, 0, 255);
      } else if (arg instanceof MobSpawnerBlockEntity) {
         return new Color(138, 126, 166);
      } else if (arg instanceof FurnaceBlockEntity || arg instanceof BlastFurnaceBlockEntity || arg instanceof SmokerBlockEntity) {
         return new Color(125, 125, 125);
      } else if (arg instanceof BarrelBlockEntity) {
         return new Color(255, 140, 140);
      } else if (arg instanceof EnchantingTableBlockEntity) {
         return new Color(80, 80, 255);
      } else if (arg instanceof PistonBlockEntity) {
         return new Color(35, 226, 0);
      } else {
         return arg instanceof HopperBlockEntity ? new Color(35, 226, 0) : new Color(100, 200, 255);
      }
   }

   public Color getShulkerDyeColor(Block arg) {
      if (arg == Blocks.WHITE_SHULKER_BOX) {
         return new Color(16052459);
      } else if (arg == Blocks.ORANGE_SHULKER_BOX) {
         return new Color(16098851);
      } else if (arg == Blocks.MAGENTA_SHULKER_BOX) {
         return new Color(13061821);
      } else if (arg == Blocks.LIGHT_BLUE_SHULKER_BOX) {
         return new Color(4043473);
      } else if (arg == Blocks.YELLOW_SHULKER_BOX) {
         return new Color(16369177);
      } else if (arg == Blocks.LIME_SHULKER_BOX) {
         return new Color(7648811);
      } else if (arg == Blocks.PINK_SHULKER_BOX) {
         return new Color(15760568);
      } else if (arg == Blocks.GRAY_SHULKER_BOX) {
         return new Color(4869970);
      } else if (arg == Blocks.LIGHT_GRAY_SHULKER_BOX) {
         return new Color(10396579);
      } else if (arg == Blocks.CYAN_SHULKER_BOX) {
         return new Color(2461326);
      } else if (arg == Blocks.PURPLE_SHULKER_BOX) {
         return new Color(8339380);
      } else if (arg == Blocks.BLUE_SHULKER_BOX) {
         return new Color(2964907);
      } else if (arg == Blocks.BROWN_SHULKER_BOX) {
         return new Color(8343857);
      } else if (arg == Blocks.GREEN_SHULKER_BOX) {
         return new Color(5268771);
      } else if (arg == Blocks.RED_SHULKER_BOX) {
         return new Color(10495778);
      } else {
         return arg == Blocks.BLACK_SHULKER_BOX ? new Color(1842212) : null;
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null && !this.trackedBlocks.isEmpty()) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            Vector3fc var3Value = textStyleUtilValue.getHorizontalPlane();
            short shortVal = 235;
            short shortVal2 = 20000;
            WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);
            int local7 = 0;

            for (Entry entry : this.trackedBlocks.entrySet()) {
               if (local7 >= shortVal2) {
                  break;
               }

               BlockPos local = (BlockPos)entry.getKey();
               if (minecraftClient.world.getChunkManager().getWorldChunk(local.getX() >> 4, local.getZ() >> 4, false) != null
                  && !minecraftClient.world.getBlockState(local).isAir()) {
                  Color local2 = (Color)this.positionColors.get(local);
                  if (local2 != null) {
                     Color colorInst2 = new Color(
                        Math.min(255, (int)(local2.getRed() * 1.4)),
                        Math.min(255, (int)(local2.getGreen() * 1.4)),
                        Math.min(255, (int)(local2.getBlue() * 1.4)),
                        local2.getAlpha()
                     );
                     local7++;
                     double localValue = local.getX() - textStyleUtilValue2.x;
                     double localValue2 = local.getY() - textStyleUtilValue2.y;
                     double localValue3 = local.getZ() - textStyleUtilValue2.z;
                     if (Double.isFinite(localValue) && Double.isFinite(localValue2) && Double.isFinite(localValue3)) {
                        double var15Snapshot = localValue;
                        double doubleVal = localValue2;
                        double var19Snapshot = localValue3;
                        double var151Value = localValue + 1.0;
                        double var171Value = localValue2 + 1.0;
                        double var191Value = localValue3 + 1.0;
                        Block local3 = (Block)entry.getValue();
                        if (local3 == Blocks.CHEST || local3 == Blocks.TRAPPED_CHEST) {
                           var15Snapshot += 0.0625;
                           var19Snapshot += 0.0625;
                           var151Value -= 0.0625;
                           var171Value -= 0.125;
                           var191Value -= 0.0625;
                           if (this.isMatchingBlockAt(local.getX() - 1, local.getY(), local.getZ(), local3)) {
                              var15Snapshot = localValue;
                           }

                           if (this.isMatchingBlockAt(local.getX() + 1, local.getY(), local.getZ(), local3)) {
                              var151Value = localValue + 1.0;
                           }

                           if (this.isMatchingBlockAt(local.getX(), local.getY(), local.getZ() - 1, local3)) {
                              var19Snapshot = localValue3;
                           }

                           if (this.isMatchingBlockAt(local.getX(), local.getY(), local.getZ() + 1, local3)) {
                              var191Value = localValue3 + 1.0;
                           }
                        } else if (local3 == Blocks.ENDER_CHEST) {
                           var15Snapshot += 0.0625;
                           var19Snapshot += 0.0625;
                           var151Value -= 0.0625;
                           var171Value -= 0.125;
                           var191Value -= 0.0625;
                        }

                        textStyleUtilValue3.renderBoxEdges(var15Snapshot, doubleVal, var19Snapshot, var151Value, var171Value, var191Value, withAlpha(colorInst2, shortVal), 1.0F);
                        if ((Boolean)this.tracersSetting.getValue()) {
                           double var150Value = localValue + 0.5;
                           double var170Value = localValue2 + 0.5;
                           double var190Value = localValue3 + 0.5;
                           float floatVal2 = (float)var150Value;
                           float floatVal3 = (float)var170Value;
                           float floatVal4 = (float)var190Value;
                           float floatVal5 = var3Value.x();
                           float floatVal6 = var3Value.y();
                           float floatVal7 = var3Value.z();
                           float floatVal8 = (float)Math.sqrt(floatVal5 * floatVal5 + floatVal6 * floatVal6 + floatVal7 * floatVal7);
                           if (floatVal8 > 1.0E-6F) {
                              floatVal5 /= floatVal8;
                              floatVal6 /= floatVal8;
                              floatVal7 /= floatVal8;
                           }

                           float var430Value = floatVal5 * 0.35F;
                           float var440Value = floatVal6 * 0.35F;
                           float var450Value = floatVal7 * 0.35F;
                           float var40Var43Var41Var44Var42V = floatVal2 * floatVal5 + floatVal3 * floatVal6 + floatVal4 * floatVal7;
                           Vec3d local4;
                           Vec3d local5;
                           if (var40Var43Var41Var44Var42V < 0.1F) {
                              float floatVal9 = -0.25F / (var40Var43Var41Var44Var42V - 0.35F);
                              local4 = new Vec3d(var430Value, var440Value, var450Value);
                              local5 = new Vec3d(var430Value + (floatVal2 - var430Value) * floatVal9, var440Value + (floatVal3 - var440Value) * floatVal9, var450Value + (floatVal4 - var450Value) * floatVal9);
                           } else {
                              local4 = new Vec3d(var430Value, var440Value, var450Value);
                              local5 = new Vec3d(floatVal2, floatVal3, floatVal4);
                           }

                           Color colorInst = new Color(colorInst2.getRed(), colorInst2.getGreen(), colorInst2.getBlue(), 180);
                           textStyleUtilValue3.drawLine(colorInst, local4, local5, 1.15F);
                        }

                        Color local6 = withAlpha(colorInst2, 90);
                        textStyleUtilValue3.fillBox(localValue + 0.0625, localValue2 + 0.0625, localValue3 + 0.0625, localValue + 0.9375, localValue2 + 0.9375, localValue3 + 0.9375, local6);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isMatchingBlockAt(int intVal, int intVal2, int intVal3, Block arg) {
      try {

         return minecraftClient.world != null && minecraftClient.world.getBlockState(new BlockPos(intVal, intVal2, intVal3)).getBlock() == arg;
      } catch (Throwable error) {
         return false;
      }
   }

   public static Color withAlpha(Color color, int intVal) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), intVal);
   }

   public int clampAlpha(int intVal) {
      return Math.max(0, Math.min(255, intVal));
   }

}
