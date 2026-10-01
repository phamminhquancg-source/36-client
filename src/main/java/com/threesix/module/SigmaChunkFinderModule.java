package com.threesix.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.item.Items;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.manager.NotificationHudManager;
import com.threesix.setting.ClientSetting;

public final class SigmaChunkFinderModule extends ModuleBase {
   public static final double plateThickness = 0.01;
   public static final double plateY = 63.0;
   public static final long normalNotifyCooldownMs = 50000L;
   public static final long scanCooldownMs = 5000L;
   public static final long repeaterCheckIntervalMs = 200L;
   public static final float scanBusyTimeoutMs = 800.0F;
   public static final float chaseRange = 140.0F;
   public final ConcurrentHashMap<ChunkPos, String> foundChunk = new ConcurrentHashMap();
   public final AtomicBoolean isFullScanRunning = new AtomicBoolean(false);
   public ExecutorService scanExecutor;
   public long lastNormalNotify = 0L;
   public long lastFullScanTime = 0L;
   public long lastRepeaterScanTime = 0L;
   public long fullScanStartTime = 0L;
   public boolean isFullScanPending = false;
   public static SigmaChunkFinderModule instance;
   public final ClientSetting detectBeeSetting = new ClientSetting("Detect Bee", true);
   public final ClientSetting detectDeepslateSetting = new ClientSetting("Detect Deepslate", true);
   public final ClientSetting detectVinesSetting = new ClientSetting("Detect Vines", true);
   public final ClientSetting detectKelpSetting = new ClientSetting("Detect Kelp", true);
   public final ClientSetting flatChunkColorSetting = new ClientSetting("Flat Chunk Color", new Color(50, 205, 50, 120));

   public SigmaChunkFinderModule() {
      super("Sigma Chunk Finder", ModuleCategory.BASEFINDING);
      instance = this;
      this.registerSetting(this.detectBeeSetting);
      this.registerSetting(this.detectDeepslateSetting);
      this.registerSetting(this.detectVinesSetting);
      this.registerSetting(this.detectKelpSetting);
      this.registerSetting(this.flatChunkColorSetting);
   }

   @Override
   public void onEnable() {
      this.foundChunk.clear();
      this.lastNormalNotify = 0L;
      this.lastFullScanTime = 0L;
      this.isFullScanPending = false;
   }

   @Override
   public void onDisable() {
      this.foundChunk.clear();
      if (this.scanExecutor != null) {
         this.scanExecutor.shutdownNow();
      }

      this.isFullScanRunning.set(false);
      this.isFullScanPending = false;
   }

   @Override
   public void onTick() {

      if (minecraftClient.world != null && minecraftClient.player != null) {
         if (this.isFullScanPending && (float)(System.currentTimeMillis() - this.fullScanStartTime) > 800.0F) {
            this.isFullScanPending = false;
         }

         long systemValue = System.currentTimeMillis();
         if (systemValue - this.lastRepeaterScanTime >= 200L && !this.isFullScanRunning.get()) {
            this.lastRepeaterScanTime = systemValue;
            ChunkPos minecraftClientValue = minecraftClient.player.getChunkPos();
            int minValue = Math.min(minecraftClient.options.getClampedViewDistance(), 8);
            ArrayList arrayListInst = new ArrayList();
            ArrayList arrayListInst2 = new ArrayList();

            for (int index = -minValue; index <= minValue; index++) {
               for (int index2 = -minValue; index2 <= minValue; index2++) {
                  ChunkPos local = new ChunkPos(minecraftClientValue.x + index, minecraftClientValue.z + index2);
                  WorldChunk minecraftClientValue2 = minecraftClient.world.getChunkManager().getWorldChunk(local.x, local.z, false);
                  if (minecraftClientValue2 != null && !minecraftClientValue2.isEmpty()) {
                     arrayListInst.add(local);
                     arrayListInst2.add(minecraftClientValue2);
                  }
               }
            }

            if (this.scanExecutor == null || this.scanExecutor.isShutdown()) {
               this.scanExecutor = Executors.newSingleThreadExecutor(item -> {

                  Thread local6 = new Thread(item, "chunk-scan");
                  local6.setDaemon(true);
                  return local6;
               });
            }

            this.scanExecutor.submit(() -> {

               for (int index3 = 0; index3 < arrayListInst.size(); index3++) {
                  WorldChunk index9 = (WorldChunk)arrayListInst2.get(index3);
                  int local7 = 0;

                  for (ChunkSection class2826 : index9.getSectionArray()) {
                     if (class2826 != null && !class2826.isEmpty() && class2826.hasAny(item -> {
                        return item.isOf(Blocks.REPEATER);
                     })) {
                        for (int class28262 = 0; class28262 < 16; class28262++) {
                           for (int index10 = 0; index10 < 16; index10++) {
                              for (int index11 = 0; index11 < 16; index11++) {
                                 BlockState index12 = class2826.getBlockState(class28262, index11, index10);
                                 if (index12.isOf(Blocks.REPEATER) && (Boolean)index12.get(RepeaterBlock.POWERED)) {
                                    if (++local7 >= 3) {
                                       this.foundChunk.clear();
                                       this.foundChunk.put((ChunkPos)arrayListInst.get(index3), "repeater");
                                       return;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            });
         }

         if (systemValue - this.lastFullScanTime >= 20000L && this.isFullScanRunning.compareAndSet(false, true)) {
            if (this.scanExecutor == null || this.scanExecutor.isShutdown()) {
               this.scanExecutor = Executors.newSingleThreadExecutor(item -> {
                  Thread local6 = new Thread(item, "chunk-full-scan");
                  local6.setDaemon(true);
                  return local6;
               });
            }

            this.lastFullScanTime = systemValue;
            this.isFullScanPending = true;
            this.fullScanStartTime = systemValue;
            ChunkPos minecraftClientValue3 = minecraftClient.player.getChunkPos();
            int minValue2 = Math.min(minecraftClient.options.getClampedViewDistance(), 8);
            ArrayList arrayListInst3 = new ArrayList();
            ArrayList arrayListInst4 = new ArrayList();

            for (int index7 = -minValue2; index7 <= minValue2; index7++) {
               for (int index8 = -minValue2; index8 <= minValue2; index8++) {
                  ChunkPos local2 = new ChunkPos(minecraftClientValue3.x + index7, minecraftClientValue3.z + index8);
                  WorldChunk minecraftClientValue4 = minecraftClient.world.getChunkManager().getWorldChunk(local2.x, local2.z, false);
                  if (minecraftClientValue4 != null && !minecraftClientValue4.isEmpty()) {
                     arrayListInst3.add(local2);
                     arrayListInst4.add(minecraftClientValue4);
                  }
               }
            }

            this.scanExecutor
               .submit(
                  () -> {
                     try {
                        ChunkPos index3 = null;

                        label657:
                        for (int index9 = 0; index9 < arrayListInst3.size(); index9++) {
                           WorldChunk local7 = (WorldChunk)arrayListInst4.get(index9);
                           int local9 = 0;

                           for (ChunkSection class28262 : local7.getSectionArray()) {
                              if (class28262 != null && !class28262.isEmpty() && class28262.hasAny(item -> {
                                 return item.isOf(Blocks.REPEATER);
                              })) {
                                 for (int index10 = 0; index10 < 16; index10++) {
                                    for (int index11 = 0; index11 < 16; index11++) {
                                       for (int index12 = 0; index12 < 16; index12++) {
                                          BlockState local8 = class28262.getBlockState(index10, index12, index11);
                                          if (local8.isOf(Blocks.REPEATER) && (Boolean)local8.get(RepeaterBlock.POWERED)) {
                                             if (++local9 >= 3) {
                                                index3 = (ChunkPos)arrayListInst3.get(index9);
                                                break label657;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        if (index3 != null) {
                           this.lastNormalNotify = System.currentTimeMillis();
                           this.foundChunk.clear();
                           this.foundChunk.put(index3, "repeater");
                           return;
                        }

                        ChunkPos var29xSnapshot = null;
                        String local5 = "normal";

                        for (int index13 = 0; index13 < arrayListInst3.size(); index13++) {
                           ChunkPos local19 = (ChunkPos)arrayListInst3.get(index13);
                           WorldChunk local3 = (WorldChunk)arrayListInst4.get(index13);
                           boolean falseSnapshot = false;
                           if ((Boolean)this.detectBeeSetting.getValue()) {
                              for (BlockEntity class2586 : local3.getBlockEntities().values()) {
                                 BlockState var30Value = local3.getBlockState(class2586.getPos());
                                 if ((var30Value.isOf(Blocks.BEEHIVE) || var30Value.isOf(Blocks.BEE_NEST))
                                    && class2586 instanceof BeehiveBlockEntity local10
                                    && local10.getBeeCount() > 0) {
                                    falseSnapshot = true;
                                    break;
                                 }
                              }
                           }

                           boolean falseSnapshot2 = false;
                           if (!falseSnapshot && (Boolean)this.detectDeepslateSetting.getValue()) {
                              ChunkSection[] var30Value2 = local3.getSectionArray();
                              int var30Value5 = local3.getBottomY();
                              int local14 = 0;

                              label600:
                              for (int index14 = 0; index14 < var30Value2.length; index14++) {
                                 int local11 = var30Value5 + index14 * 16;
                                 if (local11 > 20) {
                                    break;
                                 }

                                 if (local11 + 16 >= 0) {
                                    ChunkSection local12 = var30Value2[index14];
                                    if (local12 != null && !local12.isEmpty() && local12.hasAny(item -> {

                                       return item.isOf(Blocks.COBBLED_DEEPSLATE);
                                    })) {
                                       for (int index15 = 0; index15 < 16; index15++) {
                                          for (int index16 = 0; index16 < 16; index16++) {
                                             for (int index17 = 0; index17 < 16; index17++) {
                                                int local13 = local11 + index17;
                                                if (local13 >= 0
                                                   && local13 <= 20
                                                   && local12.getBlockState(index15, index17, index16).isOf(Blocks.COBBLED_DEEPSLATE)) {
                                                   if (++local14 >= 50) {
                                                      falseSnapshot2 = true;
                                                      break label600;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           boolean falseSnapshot3 = false;
                           if (!falseSnapshot && (Boolean)this.detectVinesSetting.getValue()) {
                              int local15 = 0;

                              for (ChunkSection class28263 : local3.getSectionArray()) {
                                 if (class28263 != null && !class28263.isEmpty() && class28263.hasAny(item -> {
                                    return item.isOf(Blocks.VINE);
                                 })) {
                                    for (int index18 = 0; index18 < 16; index18++) {
                                       for (int index19 = 0; index19 < 16; index19++) {
                                          for (int index20 = 0; index20 < 16; index20++) {
                                             if (class28263.getBlockState(index18, index20, index19).isOf(Blocks.VINE)) {
                                                if (++local15 >= 150) {
                                                   falseSnapshot3 = true;
                                                   break;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           boolean falseSnapshot4 = false;
                           if (!falseSnapshot && !falseSnapshot3 && (Boolean)this.detectKelpSetting.getValue()) {
                              ChunkSection[] var30Value3 = local3.getSectionArray();
                              int var30Value4 = local3.getBottomY();
                              int local17 = 0;

                              for (int index21 = 0; index21 < var30Value3.length && var30Value4 + index21 * 16 <= 70; index21++) {
                                 ChunkSection local4 = var30Value3[index21];
                                 if (local4 != null && !local4.isEmpty() && local4.hasAny(item -> {

                                    return item.isOf(Blocks.SEAGRASS) || item.isOf(Blocks.TALL_SEAGRASS);
                                 })) {
                                    for (int index22 = 0; index22 < 16; index22++) {
                                       for (int index23 = 0; index23 < 16; index23++) {
                                          for (int index24 = 0; index24 < 16; index24++) {
                                             BlockState local16 = local4.getBlockState(index22, index24, index23);
                                             if (local16.isOf(Blocks.SEAGRASS) || local16.isOf(Blocks.TALL_SEAGRASS)) {
                                                if (++local17 >= 30) {
                                                   falseSnapshot4 = true;
                                                   break;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           boolean falseSnapshot5 = false;
                           if (!falseSnapshot && !falseSnapshot3 && !falseSnapshot4 && !falseSnapshot2) {
                              int local18 = 0;

                              label480:
                              for (ChunkSection class28264 : local3.getSectionArray()) {
                                 if (class28264 != null && !class28264.isEmpty() && class28264.hasAny(item -> {

                                    return item.isOf(Blocks.REPEATER);
                                 })) {
                                    for (int index25 = 0; index25 < 16; index25++) {
                                       for (int index26 = 0; index26 < 16; index26++) {
                                          for (int index27 = 0; index27 < 16; index27++) {
                                             if (class28264.getBlockState(index25, index27, index26).isOf(Blocks.REPEATER)) {
                                                if (++local18 >= 3) {
                                                   falseSnapshot5 = true;
                                                   break label480;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }

                           if (falseSnapshot || falseSnapshot3 || falseSnapshot4 || falseSnapshot2 || falseSnapshot5) {
                              var29xSnapshot = local19;
                              local5 = falseSnapshot5 ? "repeater" : "normal";
                              break;
                           }
                        }

                        boolean local20 = System.currentTimeMillis() - this.lastNormalNotify >= 50000L;
                        if (var29xSnapshot != null && "repeater".equals(local5)) {
                           this.lastNormalNotify = System.currentTimeMillis();
                           this.foundChunk.clear();
                           this.foundChunk.put(var29xSnapshot, local5);
                           MinecraftClient.getInstance().execute(() -> {
                              MinecraftClient item = MinecraftClient.getInstance();
                              if (item.player != null) {
                                 item.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                              }

                              NotificationHudManager.INSTANCE6.push("Chunk Finder", "Repeater chunk!", Items.REPEATER.getDefaultStack(), ClickGuiModule.getAccentColorArgb());
                           });
                        } else if (var29xSnapshot != null) {
                           if (local20) {
                              this.lastNormalNotify = System.currentTimeMillis();
                              this.foundChunk.clear();
                              this.foundChunk.put(var29xSnapshot, local5);
                              MinecraftClient.getInstance().execute(() -> {
                                 MinecraftClient item = MinecraftClient.getInstance();
                                 if (item.player != null) {
                                    item.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                 }

                                 NotificationHudManager.INSTANCE6
                                    .push("Chunk Finder", "Chunk active!", Items.LIME_CONCRETE.getDefaultStack(), ClickGuiModule.getAccentColorArgb());
                              });
                           }
                        }
                     } finally {
                        this.isFullScanRunning.set(false);
                     }
                  }
               );
         }
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            double doubleVal = 63.0 - textStyleUtilValue2.y;
            double var50Value = doubleVal + 0.01;
            arg.push();
            GL11.glDisable(2929);

            try {
               WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);

               for (Entry entry : this.foundChunk.entrySet()) {
                  ChunkPos local = (ChunkPos)entry.getKey();
                  String local2 = (String)entry.getValue();
                  double doubleVal2 = (local.x << 4) + 8.0 - textStyleUtilValue2.x;
                  double doubleVal3 = (local.z << 4) + 8.0 - textStyleUtilValue2.z;
                  double doubleVal4 = 32.0;
                  if ("repeater".equals(local2)) {
                     textStyleUtilValue3.fillBox(doubleVal2 - doubleVal4, doubleVal, doubleVal3 - doubleVal4, doubleVal2 + doubleVal4, var50Value, doubleVal3 + doubleVal4, new Color(50, 205, 50, 40));
                     textStyleUtilValue3.strokeBox(doubleVal2 - doubleVal4, doubleVal, doubleVal3 - doubleVal4, doubleVal2 + doubleVal4, var50Value, doubleVal3 + doubleVal4, new Color(50, 205, 50, 180));
                     double doubleVal5 = 8.0;
                     textStyleUtilValue3.fillBox(doubleVal2 - doubleVal5, doubleVal, doubleVal3 - doubleVal5, doubleVal2 + doubleVal5, var50Value, doubleVal3 + doubleVal5, new Color(50, 205, 50, 100));
                     textStyleUtilValue3.strokeBox(doubleVal2 - doubleVal5, doubleVal, doubleVal3 - doubleVal5, doubleVal2 + doubleVal5, var50Value, doubleVal3 + doubleVal5, new Color(50, 205, 50, 255));
                  } else {
                     Color local3 = (Color)this.flatChunkColorSetting.getValue();
                     textStyleUtilValue3.fillBox(
                        doubleVal2 - doubleVal4,
                        doubleVal,
                        doubleVal3 - doubleVal4,
                        doubleVal2 + doubleVal4,
                        var50Value,
                        doubleVal3 + doubleVal4,
                        new Color(local3.getRed(), local3.getGreen(), local3.getBlue(), Math.max(local3.getAlpha(), 40))
                     );
                     textStyleUtilValue3.strokeBox(
                        doubleVal2 - doubleVal4,
                        doubleVal,
                        doubleVal3 - doubleVal4,
                        doubleVal2 + doubleVal4,
                        var50Value,
                        doubleVal3 + doubleVal4,
                        new Color(local3.getRed(), local3.getGreen(), local3.getBlue(), 255)
                     );
                  }
               }
            } finally {
               GL11.glEnable(2929);
            }

            arg.pop();
         }
      }
   }

}
