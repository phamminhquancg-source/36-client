package com.threesix.module;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.util.WorldToScreenUtil;
import com.threesix.internal.ModuleBase;
import com.threesix.module.HudModule;
import com.threesix.util.GuiRenderUtil;
import com.threesix.util.TextStyleUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.data.LineSegmentBounds;
import com.threesix.manager.CustomFontManager;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class SpawnerNotifierModule extends ModuleBase {
   public final ClientSetting colorSetting = new ClientSetting("Color", new Color(255, 80, 80));
   public final ClientSetting beamThicknessSetting = new ClientSetting("Beam Thickness", 0.8F, 0.2F, 3.0F);
   public final ClientSetting nametagSetting = new ClientSetting("Nametag", true);
   public final ClientSetting distanceSetting = new ClientSetting("Distance", true);
   public final ClientSetting nametagScaleSetting = new ClientSetting("Nametag Scale", 1.0F, 0.1F, 3.0F);
   public final ClientSetting nametagTextColorSetting = new ClientSetting("Nametag Text Color", new Color(255, 255, 255, 255));
   public final ClientSetting nametagBoxColorSetting = new ClientSetting("Nametag Box Color", new Color(255, 80, 80, 140));
   public final Set<BlockPos> spawnerPositions = ConcurrentHashMap.newKeySet();
   public final Map<BlockPos, String> spawnerNames = new ConcurrentHashMap();
   public static SpawnerNotifierModule instance;
   public final Set<BlockPos> notifiedSpawners = ConcurrentHashMap.newKeySet();
   public long lastSoundTime = 0L;
   public int scanTickCounter = 0;

   public SpawnerNotifierModule() {
      super("Spawner Notifier", ModuleCategory.RENDER);
      this.registerSetting(this.colorSetting);
      this.registerSetting(this.beamThicknessSetting);
      this.registerSetting(this.nametagSetting);
      this.registerSetting(this.distanceSetting);
      this.registerSetting(this.nametagScaleSetting);
      this.registerSetting(this.nametagTextColorSetting);
      this.registerSetting(this.nametagBoxColorSetting);
      instance = this;
   }

   @Override
   public void onEnable() {

      this.spawnerPositions.clear();
      this.spawnerNames.clear();
      this.notifiedSpawners.clear();
      this.scanTickCounter = 0;
   }

   @Override
   public void onDisable() {
      this.spawnerPositions.clear();
      this.spawnerNames.clear();
      this.notifiedSpawners.clear();
   }

   @Override
   public void onTick() {

      if (minecraftClient.world != null && minecraftClient.player != null) {
         this.scanTickCounter++;
         if (this.scanTickCounter % 40 == 0) {
            this.scanTickCounter = 0;
            ChunkPos minecraftClientValue = minecraftClient.player.getChunkPos();
            int minValue = Math.min(minecraftClient.options.getClampedViewDistance(), 8);
            ArrayList<BlockPos> arrayListInst = new ArrayList();

            for (int index = -minValue; index <= minValue; index++) {
               for (int index2 = -minValue; index2 <= minValue; index2++) {
                  WorldChunk minecraftClientValue2 = minecraftClient.world.getChunkManager().getWorldChunk(minecraftClientValue.x + index, minecraftClientValue.z + index2, false);
                  if (minecraftClientValue2 != null) {
                     for (BlockEntity class2586 : minecraftClientValue2.getBlockEntities().values()) {
                        if (class2586 instanceof MobSpawnerBlockEntity local) {
                           BlockPos var9Value = local.getPos();
                           arrayListInst.add(var9Value);
                           this.spawnerNames.put(var9Value, getMobName(local));
                           if (!this.notifiedSpawners.contains(var9Value)) {
                              this.notifiedSpawners.add(var9Value);

                              try {
                                 HudModule.pushToast("Spawner Found", this.spawnerNames.getOrDefault(var9Value, "Spawner"), HudModule.toastEnabledColor, null);
                              } catch (Throwable error) {
                              }

                              long systemValue = System.currentTimeMillis();
                              if (systemValue - this.lastSoundTime > 5000L) {
                                 this.lastSoundTime = systemValue;
                                 minecraftClient.world
                                    .playSound(
                                       minecraftClient.player,
                                       minecraftClient.player.getX(),
                                       minecraftClient.player.getY(),
                                       minecraftClient.player.getZ(),
                                       SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                                       SoundCategory.MASTER,
                                       1.0F,
                                       1.2F
                                    );
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.notifiedSpawners.retainAll(new HashSet(arrayListInst));
            this.spawnerPositions.clear();
            this.spawnerPositions.addAll(arrayListInst);
         }
      }
   }

   public static String getMobName(MobSpawnerBlockEntity arg) {
      try {
         Entity var0Value = arg.getLogic().getRenderedEntity(MinecraftClient.getInstance().world, arg.getPos());
         if (var0Value != null) {
            return var0Value.getType().getName().getString();
         }
      } catch (Exception error) {
      }

      String local = getSpawnDataId(arg);
      if (local != null && !local.isEmpty()) {
         String local2 = local.contains(":") ? local : "minecraft:" + local;
         Optional class1299Value = EntityType.get(local2);
         if (class1299Value.isEmpty()) {
            class1299Value = EntityType.get(local);
         }

         if (class1299Value.isPresent()) {
            return ((EntityType)class1299Value.get()).getName().getString();
         }

         int intVal = local.indexOf(58);
         String local3 = intVal >= 0 ? local.substring(intVal + 1) : local;
         return local3.substring(0, 1).toUpperCase() + local3.substring(1);
      } else {
         try {
            MobSpawnerEntry local4 = getSpawnEntry(arg);
            if (local4 != null) {
               Optional local5 = local4.entity().getString("id");
               if (local5.isPresent()) {
                  String local6 = (String)local5.get();
                  String local7 = local6.contains(":") ? local6 : "minecraft:" + local6;
                  Optional class1299Value2 = EntityType.get(local7);
                  if (class1299Value2.isEmpty()) {
                     class1299Value2 = EntityType.get(local6);
                  }

                  if (class1299Value2.isPresent()) {
                     return ((EntityType)class1299Value2.get()).getName().getString();
                  }

                  int intVal2 = local6.indexOf(58);
                  String local8 = intVal2 >= 0 ? local6.substring(intVal2 + 1) : local6;
                  return local8.substring(0, 1).toUpperCase() + local8.substring(1);
               }
            }
         } catch (Exception error2) {
         }

         return "Spawner";
      }
   }

   public static String getSpawnDataId(MobSpawnerBlockEntity arg) {
      try {
         NbtCompound var0Value = arg.toInitialChunkDataNbt(MinecraftClient.getInstance().world.getRegistryManager());
         Optional var1Value = var0Value.getCompound("SpawnData");
         if (var1Value.isPresent()) {
            Optional local = ((NbtCompound)var1Value.get()).getCompound("entity");
            if (local.isPresent()) {
               return ((NbtCompound)local.get()).getString("id").orElse("");
            }

            return ((NbtCompound)var1Value.get()).getString("id").orElse("");
         }
      } catch (Exception error) {
      }

      return "";
   }

   public static MobSpawnerEntry getSpawnEntry(MobSpawnerBlockEntity arg) {

      for (String string : new String[]{"spawnEntry", "field_9155"}) {
         try {
            Field local = MobSpawnerLogic.class.getDeclaredField(string);
            local.setAccessible(true);
            Object local2 = local.get(arg.getLogic());
            if (local2 instanceof MobSpawnerEntry) {
               return (MobSpawnerEntry)local2;
            }
         } catch (Exception error) {
         }
      }

      try {
         for (Field field : MobSpawnerLogic.class.getDeclaredFields()) {
            if (MobSpawnerEntry.class.isAssignableFrom(field.getType())) {
               field.setAccessible(true);
               Object local3 = field.get(arg.getLogic());
               if (local3 instanceof MobSpawnerEntry) {
                  return (MobSpawnerEntry)local3;
               }
            }
         }
      } catch (Exception error2) {
      }

      return null;
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null && !this.spawnerPositions.isEmpty()) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            ArrayList<BlockPos> arrayListInst = new ArrayList(this.spawnerPositions);
            arg.push();
            GL11.glDisable(2929);

            try {
               WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);
               Color local = (Color)this.colorSetting.getValue();
               short shortVal = 230;
               Color colorInst = new Color(0, 0, 0, 255);
               Color colorInst2 = new Color(local.getRed(), local.getGreen(), local.getBlue(), shortVal);

               for (BlockPos class2338 : arrayListInst) {
                  double class2338Value = class2338.getX() + 0.5 - textStyleUtilValue2.x;
                  double class2338Value2 = class2338.getZ() + 0.5 - textStyleUtilValue2.z;
                  double class2338Value3 = class2338.getY() + 1.0 - textStyleUtilValue2.y;
                  double doubleVal = 256.0 - textStyleUtilValue2.y;
                  if (!(doubleVal <= class2338Value3)) {
                     float floatVal2 = (Float)this.beamThicknessSetting.getValue() / 2.0F;
                     textStyleUtilValue3.fillBox(class2338Value - floatVal2, class2338Value3, class2338Value2 - floatVal2, class2338Value + floatVal2, doubleVal, class2338Value2 + floatVal2, colorInst);
                     textStyleUtilValue3.fillBox(class2338Value - floatVal2 - 0.15, class2338Value3, class2338Value2 - floatVal2 - 0.15, class2338Value + floatVal2 + 0.15, doubleVal, class2338Value2 + floatVal2 + 0.15, colorInst2);
                  }
               }
            } finally {
               GL11.glEnable(2929);
               arg.pop();
            }
         }
      }
   }

   public static void call3(DrawContext arg, float floatVal) {
      SpawnerNotifierModule instanceSnapshot = instance;
      if (instanceSnapshot != null
         && instanceSnapshot.isEnabled()
         && (Boolean)instanceSnapshot.nametagSetting.getValue()
         && minecraftClient.world != null
         && minecraftClient.player != null
         && !instanceSnapshot.spawnerPositions.isEmpty()) {
         float floatVal2 = (Float)instanceSnapshot.nametagScaleSetting.getValue();
         double minecraftClientValue = minecraftClient.getWindow().getScaledWidth() * 0.5 * Math.abs(WorldToScreenUtil.projectionMatrix2.m00()) * 0.02F * floatVal2;
         double minecraftClientValue2 = minecraftClient.getWindow().getScaledHeight() * 0.5 * Math.abs(WorldToScreenUtil.projectionMatrix2.m11()) * 0.02F * floatVal2;
         if (minecraftClientValue == 0.0 || minecraftClientValue2 == 0.0) {
            minecraftClientValue = minecraftClient.getWindow().getScaledWidth() * 0.5 * 0.02F * floatVal2;
            minecraftClientValue2 = minecraftClient.getWindow().getScaledHeight() * 0.5 * 0.02F * floatVal2;
         }

         Matrix3x2fStack var0Value = arg.getMatrices();
         Color local = (Color)instanceSnapshot.nametagBoxColorSetting.getValue();
         Color local2 = (Color)instanceSnapshot.nametagTextColorSetting.getValue();
         int intVal = local2.getRGB();

         for (BlockPos class2338 : instanceSnapshot.spawnerPositions) {
            String local3 = instanceSnapshot.spawnerNames.getOrDefault(class2338, "Spawner");
            if ((Boolean)instanceSnapshot.distanceSetting.getValue()) {
               try {
                  double minecraftClientValue3 = minecraftClient.player.squaredDistanceTo(class2338.getX() + 0.5, class2338.getY() + 0.5, class2338.getZ() + 0.5);
                  local3 = local3 + " " + (int)Math.sqrt(minecraftClientValue3) + "m";
               } catch (Exception error) {
               }
            }

            LineSegmentBounds lineSegmentBoundsInst = new LineSegmentBounds();
            if (WorldToScreenUtil.projectLineSegment(
                  WorldToScreenUtil.modelViewMatrix, WorldToScreenUtil.projectionMatrix2, class2338.getX() + 0.5, class2338.getY() + 1.4, class2338.getZ() + 0.5, lineSegmentBoundsInst
               )
               && lineSegmentBoundsInst.isVisible
               && !(lineSegmentBoundsInst.lineHitX < 0.0)
               && !(lineSegmentBoundsInst.lineHitX > 1.0)
               && !(lineSegmentBoundsInst.healthBarWidth <= 0.0)) {
               double minecraftClientValueLineSe = minecraftClientValue / lineSegmentBoundsInst.healthBarWidth;
               double minecraftClientValue2LineS = minecraftClientValue2 / lineSegmentBoundsInst.healthBarWidth;
               float floatVal3 = (float)((minecraftClientValueLineSe + minecraftClientValue2LineS) * 0.5);
               if (floatVal3 < 0.3F) {
                  floatVal3 = 0.3F;
               }

               if (Float.isFinite(floatVal3) && !(floatVal3 <= 0.0F)) {
                  int customFontManagerValue = CustomFontManager.INSTANCE7.getStringWidth(local3);
                  var0Value.pushMatrix();
                  var0Value.translate((float)lineSegmentBoundsInst.x, (float)lineSegmentBoundsInst.y);
                  var0Value.scale(floatVal3, floatVal3);
                  int intVal2 = local.getRGB();
                  int intVal3 = (local.getAlpha() & 0xFF) << 24 | intVal2 & 16777215;
                  GuiRenderUtil.fillRoundedRect(arg, -(customFontManagerValue / 2.0F) - 2.0F, -1.0F, customFontManagerValue + 4.0F, 12.0F, 3.0F, intVal3, false);
                  GuiRenderUtil.strokeRoundedRect(arg, -(customFontManagerValue / 2.0F) - 2.0F, -1.0F, customFontManagerValue + 4.0F, 12.0F, 3.0F, 1.0F, argbWithAlpha(ClickGuiModule.getAccentColorArgb(), 90), false);
                  CustomFontManager.INSTANCE7.drawText(arg, local3, -(customFontManagerValue / 2), -1.0F, intVal);
                  var0Value.popMatrix();
               }
            }
         }
      }
   }

   public static int argbWithAlpha(int intVal, int intVal2) {

      return (intVal2 & 0xFF) << 24 | intVal & 16777215;
   }

}
