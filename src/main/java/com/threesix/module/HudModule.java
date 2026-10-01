package com.threesix.module;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.entity.player.SkinTextures;
import org.joml.Matrix4f;
import com.threesix.module.RegionMapModule;
import com.threesix.module.SpotifyHudModule;
import com.threesix.manager.ConfigManager;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.data.HudElementType;
import com.threesix.module.StaffDetectorModule;
import com.threesix.manager.CustomFontManager;
import com.threesix.data.ChunkClusterRecord;
import com.threesix.module.SusChunkFinderModule;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.HudToastEntry;
import com.threesix.internal.ModuleBase;
import com.threesix.util.GuiRenderUtil;

public final class HudModule extends ModuleBase {
   public static HudModule instance;
   public final ClientSetting watermarkSetting = new ClientSetting("threesix+", true);
   public final ClientSetting coordinatesSetting = new ClientSetting("Coordinates", true);
   public final ClientSetting fpsSetting = new ClientSetting("FPS", true);
   public final ClientSetting pingSetting = new ClientSetting("Ping", true);
   public final ClientSetting timeSetting = new ClientSetting("Time", true);
   public final ClientSetting moduleListSetting = new ClientSetting("Module List", true);
   public final ClientSetting potionEffectsSetting = new ClientSetting("Potion Effects", true);
   public final ClientSetting armorSetting = new ClientSetting("Armor", true);
   public final ClientSetting keybindsSetting = new ClientSetting("Keybinds", true);
   public final ClientSetting radarSetting = new ClientSetting("Radar", true);
   public final ClientSetting opacitySetting = new ClientSetting("Opacity", 0.8F, 0.0F, 1.0F);
   public final ClientSetting rainbowSetting = new ClientSetting("Rainbow", false);
   public final ClientSetting rainbowSpeedSetting = new ClientSetting("Rainbow Speed", 2.0F, 0.1F, 10.0F);
   public final ClientSetting radarSizeSetting = new ClientSetting("Radar Size", 110, 60, 200);
   public final ClientSetting radarRangeSetting = new ClientSetting("Radar Range", 64, 16, 128);
   public final ClientSetting radarPlayersSetting = new ClientSetting("Radar Players", true);
   public final ClientSetting radarHostileSetting = new ClientSetting("Radar Hostile", false);
   public final ClientSetting radarPassiveSetting = new ClientSetting("Radar Passive", false);
   public final ClientSetting radarRotateSetting = new ClientSetting("Radar Rotate", true);
   public final ClientSetting hudScaleSetting = new ClientSetting("HUD Scale", 1.0F, 0.5F, 3.0F);
   public final ClientSetting notificationsSetting = new ClientSetting("Notifications", true);
   public static final Color brandColor = new Color(65, 185, 255, 255);
   public static final int darkPanelColor = 2302755;
   public static Color accentColor = new Color(65, 185, 255);
   public static Matrix4f contextProjectionMatrix = new Matrix4f();
   public static long lastRenderMillis = 0L;
   public static List radarEntities = new ArrayList();
   public static long lastRadarRefresh = 0L;
   public static final long hundredMillisConstant = 100L;
   public static final EnumMap elementPositions = new EnumMap<>(HudElementType.class);
   public static final EnumMap elementScales = new EnumMap<>(HudElementType.class);
   public static final int zeroRgb = 0;
   public static final Map generatedHeadCache = new ConcurrentHashMap();
   public static final Set failedTextureCache = Collections.newSetFromMap(new ConcurrentHashMap());
   public static int headTextureCounter = 0;
   public static final Map playerSkinCache = new ConcurrentHashMap();
   public static final Identifier watermarkTexture = Identifier.of("threesix", "textures/gui/hud/watermark.png");
   public static final Identifier potionTexture = Identifier.of("threesix", "textures/gui/hud/potion.png");
   public static final Identifier cubeTexture = Identifier.of("threesix", "textures/gui/hud/cube.png");
   public static final Identifier fpsTexture = Identifier.of("threesix", "textures/gui/hud/fps.png");
   public static final Identifier pingTexture = Identifier.of("threesix", "textures/gui/hud/ping.png");
   public static final Identifier clockTexture = Identifier.of("threesix", "textures/gui/hud/clock.png");
   public static final Identifier coordinateTexture = Identifier.of("threesix", "textures/gui/hud/coordinate.png");
   public static final Identifier heartTexture = Identifier.of("threesix", "textures/gui/hud/heart.png");
   public static final Identifier shieldTexture = Identifier.of("threesix", "textures/gui/hud/shield.png");
   public static final Identifier swordsTexture = Identifier.of("threesix", "textures/gui/hud/swords.png");
   public static final Color mutedTextColor = new Color(150, 150, 168);
   public static final Map moduleListAnimations = new ConcurrentHashMap();
   private static long lastFrameNanos = 0L;
   private static long lastFpsRefresh = 0L;
   private static String fpsText = null;
   private static int fpsTextWidth = -1;
   private static long lastPingRefresh = 0L;
   private static String pingText = null;
   private static int pingTextWidth = -1;
   private static int lastTimeSecond = -1;
   private static String timeText = null;
   private static int timeTextWidth = -1;
   private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
   private static long lastCoordsRefresh = 0L;
   private static long lastLayoutRefresh = 0L;
   private static ArrayList sortedStatusEffects = null;
   private static ArrayList statusEffectLabels = null;
   private static int statusEffectHash = 0;
   private static long lastStatusRefresh = 0L;
   private static final Color armorHighColor = new Color(74, 222, 128);
   private static final Color armorMidColor = new Color(250, 204, 21);
   private static final Color armorLowColor = new Color(239, 68, 68);
   private static String coordXText = null;
   private static String coordYText = null;
   private static String coordZText = null;
   public static final ConcurrentLinkedQueue toastQueue = new ConcurrentLinkedQueue();
   public static final int maxToastCount = 5;
   public static final long toastSlideInMillis = 220L;
   public static final long toastHoldMillis = 1000L;
   public static final long toastSlideOutMillis = 220L;
   public static final int toastEnabledColor = new Color(80, 220, 120).getRGB();
   public static final int toastDisabledColor = new Color(255, 90, 90).getRGB();

   public void call1(DrawContext arg, String string, float floatVal, float floatVal2, int intVal) {
      CustomFontManager.INSTANCE7.drawText(arg, string, floatVal, floatVal2, intVal);
   }

   public int call2(String string) {
      return CustomFontManager.INSTANCE7.getStringWidth(string);
   }

   public static int[] getSavedElementPos(HudElementType hudElementType) {
      return (int[])elementPositions.computeIfAbsent(hudElementType, (java.util.function.Function<HudElementType, int[]>)(k -> HudModule.getDefaultElementPos(k)));
   }

   public static void call3(HudElementType hudElementType, int intVal, int intVal2) {
      int[] local = call4(hudElementType, intVal, intVal2);
      elementPositions.put(hudElementType, local);
      ConfigManager.INSTANCE.notifyLayoutChanged();
   }

   public static int[] call4(HudElementType hudElementType, int intVal, int intVal2) {
      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.getWindow() != null) {
            int var3Value = mc.getWindow().getScaledWidth();
            int var3Value2 = mc.getWindow().getScaledHeight();
            if (var3Value > 0 && var3Value2 > 0) {
               float floatVal = hudElementType == HudElementType.SPOTIFY_HUD ? 1.0F : getScaledFactor(hudElementType);
               if (!(floatVal > 0.0F)) {
                  floatVal = 1.0F;
               }

               int maxValue;
               int maxValue2;
               if (hudElementType == HudElementType.MODULE_LIST) {
                  int[] local = call5();
                  maxValue = Math.max(1, local[2]);
                  maxValue2 = Math.max(1, local[3]);
               } else if (hudElementType == HudElementType.SPOTIFY_HUD) {
                  maxValue = Math.max(1, SpotifyHudModule.getSpotifyPanelWidth());
                  maxValue2 = Math.max(1, SpotifyHudModule.getSpotifyPanelHeight());
               } else {
                  int[] local2 = call8(hudElementType);
                  maxValue = Math.max(1, local2[2]);
                  maxValue2 = Math.max(1, local2[3]);
               }

               if (hudElementType == HudElementType.MODULE_LIST) {
                  int intVal3 = (int)Math.ceil(maxValue + 1.0 / floatVal);
                  int intVal4 = (int)Math.floor((var3Value - 1) / floatVal);
                  int intVal5 = (int)Math.ceil(1.0 / floatVal);
                  int intVal6 = (int)Math.floor((var3Value2 - 1 - maxValue2 * floatVal) / floatVal);
                  return intVal4 >= intVal3 && intVal6 >= intVal5
                     ? new int[]{Math.max(intVal3, Math.min(intVal4, intVal)), Math.max(intVal5, Math.min(intVal6, intVal2))}
                     : getSavedElementPos(hudElementType);
               } else {
                  int intVal7 = (int)Math.ceil(1.0 / floatVal);
                  int intVal8 = (int)Math.floor((var3Value - 1 - maxValue * floatVal) / floatVal);
                  int intVal9 = (int)Math.floor((var3Value2 - 1 - maxValue2 * floatVal) / floatVal);
                  return intVal8 >= intVal7 && intVal9 >= intVal7
                     ? new int[]{Math.max(intVal7, Math.min(intVal8, intVal)), Math.max(intVal7, Math.min(intVal9, intVal2))}
                     : getSavedElementPos(hudElementType);
               }
            } else {
               return new int[]{intVal, intVal2};
            }
         } else {
            return new int[]{intVal, intVal2};
         }
      } catch (Throwable error) {
         return new int[]{intVal, intVal2};
      }
   }

   public static float call9(HudElementType hudElementType) {
      return hudElementType == HudElementType.SPOTIFY_HUD ? SpotifyHudModule.getScale() : (float)(Float)elementScales.getOrDefault(hudElementType, 1.0F);
   }

   public static void call11(HudElementType hudElementType, float floatVal) {
      if (hudElementType == HudElementType.SPOTIFY_HUD) {
         SpotifyHudModule.setSpotifyPanelScale(Math.max(0.5F, Math.min(3.0F, floatVal)));
      } else {
         elementScales.put(hudElementType, Math.max(0.5F, Math.min(3.0F, floatVal)));
      }

      ConfigManager.INSTANCE.notifyLayoutChanged();
   }

   public static float getScaledFactor(HudElementType hudElementType) {
      return Math.max(0.5F, Math.min(3.0F, getHudScale() * call9(hudElementType)));
   }

   public static int getFallbackElementWidth(HudElementType hudElementType) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null) {
         return 100;
      }

      return switch (hudElementType) {
         case WATERMARK -> measureTextWidth("36") + measureTextWidth("client") + 7 + 32;
         case COORDINATES -> 7 + measureTextWidth("00000") + 3 + 7 + measureTextWidth("256") + 3 + 7 + measureTextWidth("00000") + 32;
         case INFO -> measureTextWidth("999 FPS  •  999 ms  •  23:59:59") + 14;
         case FPS -> measureTextWidth("999") + measureTextWidth(" FPS") + 32;
         case PING -> measureTextWidth("999") + measureTextWidth(" ms") + 32;
         case TIME -> measureTextWidth("23:59:59") + 32;
         case MODULE_LIST -> 140;
         case POTION_EFFECTS -> 130;
         case ARMOR -> measureTextWidth(" 100%") + 22;
         case KEYBINDS -> 140;
         case SPOTIFY_HUD -> SpotifyHudModule.getSpotifyPanelWidth();
         case RADAR -> instance != null ? (int)(Integer)instance.radarSizeSetting.getValue() : 110;
         case STAFF_LIST -> 180;
         case REGION_MAP -> 200;
      };
   }

   public static int[] call8(HudElementType hudElementType) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.player != null) {
         int[] local = getSavedElementPos(hudElementType);
         int[] var32Snapshot;
         switch (hudElementType) {
            case WATERMARK:
               int[] local2 = new int[]{local[0], local[1], Math.round(measureTextWidth("36") + measureTextWidth("client") + 30 + 3.5F), 12};
               var32Snapshot = local2;
               break;
            case COORDINATES:
               String stringValue = String.format("%.0f", mc.player.getX());
               String stringValue2 = String.format("%.0f", mc.player.getY());
               String stringValue3 = String.format("%.0f", mc.player.getZ());
               int intVal = stringValue.startsWith("-") ? 7 + measureTextWidth(stringValue.substring(1)) : measureTextWidth(stringValue);
               int intVal2 = stringValue2.startsWith("-") ? 7 + measureTextWidth(stringValue2.substring(1)) : measureTextWidth(stringValue2);
               int intVal3 = stringValue3.startsWith("-") ? 7 + measureTextWidth(stringValue3.substring(1)) : measureTextWidth(stringValue3);
               int roundValue = Math.round(intVal + intVal2 + intVal3 + 33 + 3.5F);
               int[] local3 = new int[]{local[0], local[1], roundValue, 12};
               var32Snapshot = local3;
               break;
            case INFO:
               int[] local4 = new int[]{local[0], local[1], measureTextWidth(mc.getCurrentFps() + " FPS  •  0 ms  •  00:00:00") + 14, 20};
               var32Snapshot = local4;
               break;
            case FPS:
               int[] local5 = new int[]{local[0], local[1], Math.round(measureTextWidth(String.valueOf(mc.getCurrentFps())) + measureTextWidth(" FPS") + 23 + 3.5F), 12};
               var32Snapshot = local5;
               break;
            case PING:
               int var44Value = 0;

               try {
                  if (mc.getNetworkHandler() != null) {
                     PlayerListEntry var1Value = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                     if (var1Value != null) {
                        var44Value = var1Value.getLatency();
                     }
                  }
               } catch (Exception error) {
               }

               int[] local6 = new int[]{local[0], local[1], Math.round(measureTextWidth(String.valueOf(var44Value)) + measureTextWidth(" ms") + 23 + 3.5F), 12};
               var32Snapshot = local6;
               break;
            case TIME:
               int[] local25 = new int[]{
                  local[0], local[1], Math.round(measureTextWidth(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))) + 23 + 3.5F), 12
               };
               var32Snapshot = local25;
               break;
            case MODULE_LIST:
               int[] local7 = call5();
               var32Snapshot = local7;
               break;
            case POTION_EFFECTS:
               ArrayList arrayListInst = new ArrayList(mc.player.getStatusEffects());
               if (arrayListInst.isEmpty()) {
                  int[] local8 = new int[]{local[0], local[1], getFallbackElementWidth(hudElementType), 20};
                  var32Snapshot = local8;
               } else {
                  int var56Snapshot = 0;

                  for (StatusEffectInstance class1293 : (Iterable<StatusEffectInstance>)arrayListInst) {
                     int roundValue2 = Math.round(measureTextWidth(formatStatusEffectName(class1293)) + 23 + 3.5F);
                     if (roundValue2 > var56Snapshot) {
                        var56Snapshot = roundValue2;
                     }
                  }

                  int[] local9 = new int[]{local[0], local[1], var56Snapshot, arrayListInst.size() * 14};
                  var32Snapshot = local9;
               }
               break;
            case ARMOR:
               int local26 = 0;
               int maxValue = 0;

               for (EquipmentSlot class1304 : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                  ItemStack local10 = mc.player.getEquippedStack(class1304);
                  if (local10 != null && !local10.isEmpty()) {
                     local26++;
                     int local10Value = local10.isDamageable() && local10.getMaxDamage() > 0
                        ? (int)Math.round((1.0 - (double)local10.getDamage() / local10.getMaxDamage()) * 100.0)
                        : 100;
                     maxValue = Math.max(maxValue, 16 + measureTextWidth(local10Value + "%") + 12);
                  }
               }

               if (local26 == 0) {
                  int[] local11 = new int[]{local[0], local[1], 70, 18};
                  var32Snapshot = local11;
               } else {
                  int[] local12 = new int[]{local[0], local[1], maxValue, local26 * 18};
                  var32Snapshot = local12;
               }
               break;
            case KEYBINDS:
               List local13 = getBoundModules();
               if (local13.isEmpty()) {
                  int[] local14 = new int[]{local[0], local[1], getFallbackElementWidth(hudElementType), 20};
                  var32Snapshot = local14;
               } else {
                  int var54Snapshot = 0;

                  for (ModuleBase moduleBase : (Iterable<ModuleBase>)local13) {
                     int roundValue3 = Math.round(measureTextWidth(moduleBase.getName2() + "  " + ClickGuiScreen.bY(moduleBase.getKeyCode()).toUpperCase()) + 8 + 3.5F);
                     if (roundValue3 > var54Snapshot) {
                        var54Snapshot = roundValue3;
                     }
                  }

                  int[] local15 = new int[]{local[0], local[1], var54Snapshot, (local13.size() + 1) * 13};
                  var32Snapshot = local15;
               }
               break;
            case SPOTIFY_HUD:
               int[] local16 = new int[]{local[0], local[1], SpotifyHudModule.getSpotifyPanelWidth(), SpotifyHudModule.getSpotifyPanelHeight()};
               var32Snapshot = local16;
               break;
            case RADAR:
               int intVal4 = instance != null ? (Integer)instance.radarSizeSetting.getValue() : 110;
               int[] local17 = new int[]{local[0], local[1], intVal4, intVal4};
               var32Snapshot = local17;
               break;
            case STAFF_LIST:
               float floatVal = 12 + measureTextWidth("STAFF") + 10 + measureTextWidth("0") + 8 + 3.5F;
               int var8Count = 0;
               int local20 = 20;

               try {
                  StaffDetectorModule local18 = (StaffDetectorModule)ConfigManager.INSTANCE.getModuleByName("Staff Detector");
                  if (local18 != null) {
                     Map local19 = local18.call8();
                     if (local19 != null) {
                        var8Count = local19.size();
                        int intVal5 = measureTextWidth(String.valueOf(var8Count)) + 8;
                        floatVal = 12 + measureTextWidth("STAFF") + 10 + intVal5 + 3.5F;

                        for (Entry entry : (Iterable<Entry>)local19.entrySet()) {
                           float floatVal2 = 26 + measureTextWidth(((String)entry.getKey()).toUpperCase()) + 16 + measureTextWidth((String)entry.getValue()) + 3.5F;
                           if (floatVal2 > floatVal) {
                              floatVal = floatVal2;
                           }
                        }

                        if (var8Count > 0) {
                           local20 = 23 + var8Count * 12 + 6;
                        }
                     }
                  }
               } catch (Throwable error2) {
               }

               int[] local21 = new int[]{local[0], local[1], Math.round(floatVal), local20};
               var32Snapshot = local21;
               break;
            case REGION_MAP:
               if (RegionMapModule.field1 != null) {
                  int[] regionMapModuleValue = RegionMapModule.field1.getMapSize();
                  int[] local22 = new int[]{local[0], local[1], regionMapModuleValue[0], regionMapModuleValue[1]};
                  var32Snapshot = local22;
               } else {
                  int[] local23 = new int[]{local[0], local[1], 200, 200};
                  var32Snapshot = local23;
               }
               break;
            default:
               throw new MatchException(null, null);
         }

         return var32Snapshot;
      } else {
         int[] local24 = getSavedElementPos(hudElementType);
         return new int[]{local24[0], local24[1], getFallbackElementWidth(hudElementType), 14};
      }
   }

   public static List getBoundModules() {
      ArrayList arrayListInst = new ArrayList();

      for (ModuleBase moduleBase : (Iterable<ModuleBase>)ConfigManager.INSTANCE.getModules()) {
         if (moduleBase.getKeyCode() != 0) {
            arrayListInst.add(moduleBase);
         }
      }

      arrayListInst.sort(Comparator.comparing(ModuleBase::getName2));
      return arrayListInst;
   }

   public static boolean call14(HudElementType hudElementType) {
      if (instance != null && instance.isEnabled()) {
         return switch (hudElementType) {
            case WATERMARK -> (Boolean)instance.watermarkSetting.getValue();
            case COORDINATES -> (Boolean)instance.coordinatesSetting.getValue();
            case INFO -> false;
            case FPS -> (Boolean)instance.fpsSetting.getValue();
            case PING -> (Boolean)instance.pingSetting.getValue();
            case TIME -> (Boolean)instance.timeSetting.getValue();
            case MODULE_LIST -> (Boolean)instance.moduleListSetting.getValue();
            case POTION_EFFECTS -> (Boolean)instance.potionEffectsSetting.getValue();
            case ARMOR -> (Boolean)instance.armorSetting.getValue();
            case KEYBINDS -> (Boolean)instance.keybindsSetting.getValue();
            case SPOTIFY_HUD -> SpotifyHudModule.isSpotifyHudEnabled();
            case RADAR -> (Boolean)instance.radarSetting.getValue();
            case STAFF_LIST -> true;
            case REGION_MAP -> RegionMapModule.field1 != null && RegionMapModule.field1.isEnabled();
         };
      } else {
         return false;
      }
   }

   public static int[] getDefaultElementPos(HudElementType hudElementType) {
      MinecraftClient mc = MinecraftClient.getInstance();
      int intVal = mc != null ? mc.getWindow().getScaledWidth() : 800;

      return switch (hudElementType) {
         case WATERMARK -> new int[]{5, 5};
         case COORDINATES -> new int[]{5, 30};
         case INFO -> new int[]{5, 55};
         case FPS -> new int[]{5, 80};
         case PING -> new int[]{5, 105};
         case TIME -> new int[]{5, 130};
         case MODULE_LIST -> new int[]{intVal - 8, 5};
         case POTION_EFFECTS -> new int[]{5, 155};
         case ARMOR -> new int[]{intVal - 70, 200};
         case KEYBINDS -> new int[]{5, 180};
         case SPOTIFY_HUD -> new int[]{5, 50};
         case RADAR -> new int[]{intVal - 125, 10};
         case STAFF_LIST -> new int[]{intVal - 190, 10};
         case REGION_MAP -> new int[]{intVal - 260, 160};
      };
   }

   public HudModule() {
      super("Hud", ModuleCategory.CLIENT);
      this.registerSetting(this.watermarkSetting);
      this.registerSetting(this.coordinatesSetting);
      this.registerSetting(this.fpsSetting);
      this.registerSetting(this.pingSetting);
      this.registerSetting(this.timeSetting);
      this.registerSetting(this.moduleListSetting);
      this.registerSetting(this.potionEffectsSetting);
      this.registerSetting(this.armorSetting);
      this.registerSetting(this.keybindsSetting);
      this.registerSetting(this.opacitySetting);
      this.registerSetting(this.rainbowSetting);
      this.registerSetting(this.rainbowSpeedSetting);
      this.registerSetting(this.radarSetting);
      this.registerSetting(this.radarSizeSetting);
      this.registerSetting(this.radarRangeSetting);
      this.registerSetting(this.radarPlayersSetting);
      this.registerSetting(this.radarHostileSetting);
      this.registerSetting(this.radarPassiveSetting);
      this.registerSetting(this.radarRotateSetting);
      this.registerSetting(this.hudScaleSetting);
      this.registerSetting(this.notificationsSetting);
      instance = this;
   }

   public static boolean call15() {
      int local = instance != null && instance.isEnabled() ? 1 : 0;
      return local != 0;
   }

   public static float getHudScale() {
      return instance == null ? 1.0F : (Float)instance.hudScaleSetting.getValue();
   }

   public static void setHudScale(float floatVal) {
      if (instance != null) {
         instance.hudScaleSetting.setValue(Math.max(0.5F, Math.min(3.0F, floatVal)));
      }
   }

   public static boolean isModuleListEnabled() {
      int local = instance != null && instance.isEnabled() && (Boolean)instance.moduleListSetting.getValue() ? 1 : 0;
      return local != 0;
   }

   public static float call16() {
      return 0.6F;
   }

   public static boolean isWatermarkHidden() {
      return false;
   }

   public static void call17(DrawContext arg) {
      if (instance != null && instance.isEnabled()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.player != null && !(mc.currentScreen instanceof ClickGuiScreen) && !mc.getDebugHud().shouldShowDebugHud()) {
            int hudPushed = 0;

            try {
            long systemValue = System.currentTimeMillis();
            if (systemValue != lastRenderMillis) {
               lastRenderMillis = systemValue;
               accentColor = ClickGuiModule.getAccentColor();
               contextProjectionMatrix = GuiRenderUtil.getContextProjectionMatrix(arg);
            }

            float floatVal = (Float)instance.opacitySetting.getValue();
            boolean flag = (Boolean)instance.rainbowSetting.getValue();
            float floatVal2 = (Float)instance.rainbowSpeedSetting.getValue();
            Color local = flag ? rainbowColorAt(floatVal2, 0) : accentColor;
            if (systemValue - lastLayoutRefresh >= 250L) {
               lastLayoutRefresh = systemValue;

               try {
                  for (HudElementType hudElementType : HudElementType.values()) {
                     if (call14(hudElementType)) {
                        int[] local2 = getSavedElementPos(hudElementType);
                        call3(hudElementType, local2[0], local2[1]);
                     }
                  }
               } catch (Throwable error) {
               }
            }

            Color clickGuiModuleValue = ClickGuiModule.getBackgroundColor();
            Color colorInst = new Color(clickGuiModuleValue.getRed(), clickGuiModuleValue.getGreen(), clickGuiModuleValue.getBlue(), (int)(floatVal * 255.0F));
            float floatVal3 = getScaledFactor(HudElementType.WATERMARK);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.watermarkSetting.getValue()) {
               int[] local3 = getSavedElementPos(HudElementType.WATERMARK);
               Color var7Snapshot = local;
               String local4 = "36";
               int intVal = measureTextWidth(local4);
               int intVal2 = measureTextWidth("client");
               float var16Var17303Value = intVal + intVal2 + 30 + 3.5F;
               byte byteVal = 12;
               queueHudPanel(arg, local3[0], local3[1], var16Var17303Value, byteVal, 2.0F, colorInst.getRGB());
               drawHudIcon(arg, local3[0] + 4, local3[1], watermarkTexture, flag ? local.getRGB() : accentColor.getRGB());
               drawIconDivider(arg, local3[0] + 4 + 12 + 3, local3[1] + 2.0F, 1627389951);
               drawTextAt(arg, local4, local3[0] + 4 + 12 + 3 + 4, local3[1] + 0.5F, var7Snapshot.getRGB());
               float floatVal4 = 3.0F;
               float floatVal5 = local3[0] + 4 + 12 + 3 + 4 + intVal + 2;
               int intVal3 = flag ? local.getRGB() : accentColor.getRGB();
               GuiRenderUtil.fillRoundedRect(arg, floatVal5, local3[1] + 6.0F - floatVal4 / 2.0F, floatVal4, floatVal4, floatVal4 / 2.0F, intVal3, false);
               drawTextAt(arg, "client", (int)(floatVal5 + floatVal4 + 2.0F), local3[1] + 0.5F, intVal3);
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.COORDINATES);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.coordinatesSetting.getValue()) {
               int[] local5 = getSavedElementPos(HudElementType.COORDINATES);
               if (systemValue - lastCoordsRefresh >= 100L || coordXText == null) {
                  lastCoordsRefresh = systemValue;
                  coordXText = Long.toString(Math.round(mc.player.getX()));
                  coordYText = Long.toString(Math.round(mc.player.getY()));
                  coordZText = Long.toString(Math.round(mc.player.getZ()));
               }

               String coordXTextSnapshot = coordXText;
               String coordYTextSnapshot = coordYText;
               String coordZTextSnapshot = coordZText;
               int intVal4 = coordXTextSnapshot.startsWith("-") ? 7 + measureTextWidth(coordXTextSnapshot.substring(1)) : measureTextWidth(coordXTextSnapshot);
               int intVal5 = coordYTextSnapshot.startsWith("-") ? 7 + measureTextWidth(coordYTextSnapshot.substring(1)) : measureTextWidth(coordYTextSnapshot);
               int intVal6 = coordZTextSnapshot.startsWith("-") ? 7 + measureTextWidth(coordZTextSnapshot.substring(1)) : measureTextWidth(coordZTextSnapshot);
               float var141Var148Var156333Value = intVal4 + intVal5 + intVal6 + 33 + 3.5F;
               queueHudPanel(arg, local5[0], local5[1], var141Var148Var156333Value, 12.0F, 2.0F, colorInst.getRGB());
               drawHudIcon(arg, local5[0] + 4, local5[1], coordinateTexture, flag ? local.getRGB() : accentColor.getRGB());
               drawIconDivider(arg, local5[0] + 4 + 12 + 3, local5[1] + 2.0F, 1627389951);
               float floatVal6 = local5[0] + 4 + 12 + 3 + 4;
               float floatVal7 = local5[1] + 0.5F;
               int intVal7 = getFontColor();
               int intVal8 = getFontColor();
               int intVal9 = getFontColor();
               if (coordXTextSnapshot.startsWith("-")) {
                  drawNegativeSign(arg, floatVal6, local5[1], intVal7);
                  String local6 = coordXTextSnapshot.substring(1);
                  drawTextF(arg, local6, floatVal6 + 7.0F, floatVal7, intVal7);
                  floatVal6 += 7 + measureTextWidth(local6);
               } else {
                  drawTextF(arg, coordXTextSnapshot, floatVal6, floatVal7, intVal7);
                  floatVal6 += measureTextWidth(coordXTextSnapshot);
               }

               floatVal6 += 5.0F;
               if (coordYTextSnapshot.startsWith("-")) {
                  drawNegativeSign(arg, floatVal6, local5[1], intVal8);
                  String local7 = coordYTextSnapshot.substring(1);
                  drawTextF(arg, local7, floatVal6 + 7.0F, floatVal7, intVal8);
                  floatVal6 += 7 + measureTextWidth(local7);
               } else {
                  drawTextF(arg, coordYTextSnapshot, floatVal6, floatVal7, intVal8);
                  floatVal6 += measureTextWidth(coordYTextSnapshot);
               }

               floatVal6 += 5.0F;
               if (coordZTextSnapshot.startsWith("-")) {
                  drawNegativeSign(arg, floatVal6, local5[1], intVal9);
                  String local8 = coordZTextSnapshot.substring(1);
                  drawTextF(arg, local8, floatVal6 + 7.0F, floatVal7, intVal9);
               } else {
                  drawTextF(arg, coordZTextSnapshot, floatVal6, floatVal7, intVal9);
               }
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.FPS);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.fpsSetting.getValue()) {
               int[] local9 = getSavedElementPos(HudElementType.FPS);
               if (systemValue - lastFpsRefresh >= 250L || fpsText == null) {
                  lastFpsRefresh = systemValue;
                  fpsText = mc.getCurrentFps() + " FPS";
                  fpsTextWidth = -1;
               }

               String fpsTextSnapshot = fpsText;
               if (fpsTextWidth < 0) {
                  fpsTextWidth = measureTextWidth(fpsTextSnapshot);
               }

               float fpsTextWidth233Value = fpsTextWidth + 23 + 3.5F;
               queueHudPanel(arg, local9[0], local9[1], fpsTextWidth233Value, 12.0F, 2.0F, colorInst.getRGB());
               drawHudIcon(arg, local9[0] + 4, local9[1], fpsTexture, flag ? local.getRGB() : accentColor.getRGB());
               drawIconDivider(arg, local9[0] + 4 + 12 + 3, local9[1] + 2.0F, 1627389951);
               drawTextAt(arg, fpsTextSnapshot, local9[0] + 4 + 12 + 3 + 4, local9[1] + 0.5F, getFontColor());
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.PING);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.pingSetting.getValue()) {
               int[] local10 = getSavedElementPos(HudElementType.PING);
               if (systemValue - lastPingRefresh >= 1000L || pingText == null) {
                  lastPingRefresh = systemValue;
                  int var116Value = 0;

                  try {
                     if (mc.getNetworkHandler() != null) {
                        PlayerListEntry var1Value = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                        if (var1Value != null) {
                           var116Value = var1Value.getLatency();
                        }
                     }
                  } catch (Exception error2) {
                  }

                  pingText = var116Value + " ms";
                  pingTextWidth = -1;
               }

               if (pingTextWidth < 0) {
                  pingTextWidth = measureTextWidth(pingText);
               }

               float pingTextWidth233Value = pingTextWidth + 23 + 3.5F;
               queueHudPanel(arg, local10[0], local10[1], pingTextWidth233Value, 12.0F, 2.0F, colorInst.getRGB());
               drawHudIcon(arg, local10[0] + 4, local10[1], pingTexture, flag ? local.getRGB() : accentColor.getRGB());
               drawIconDivider(arg, local10[0] + 4 + 12 + 3, local10[1] + 2.0F, 1627389951);
               drawTextAt(arg, pingText, local10[0] + 4 + 12 + 3 + 4, local10[1] + 0.5F, getFontColor());
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.TIME);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.timeSetting.getValue()) {
               int[] local11 = getSavedElementPos(HudElementType.TIME);
               int intVal10 = (int)(systemValue / 1000L);
               if (intVal10 != lastTimeSecond || timeText == null) {
                  lastTimeSecond = intVal10;
                  LocalTime localTimeValue = LocalTime.now();
                  timeText = localTimeValue.format(timeFormatter);
                  timeTextWidth = -1;
               }

               String timeTextSnapshot = timeText;
               if (timeTextWidth < 0) {
                  timeTextWidth = measureTextWidth(timeTextSnapshot);
               }

               float timeTextWidth233Value = timeTextWidth + 23 + 3.5F;
               queueHudPanel(arg, local11[0], local11[1], timeTextWidth233Value, 12.0F, 2.0F, colorInst.getRGB());
               drawHudIconScaled(arg, local11[0] + 5, local11[1] + 1, 10.0F, clockTexture, flag ? local.getRGB() : accentColor.getRGB());
               drawIconDivider(arg, local11[0] + 4 + 12 + 3, local11[1] + 2.0F, 1627389951);
               drawTextAt(arg, timeTextSnapshot, local11[0] + 4 + 12 + 3 + 4, local11[1] + 0.5F, getFontColor());
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.POTION_EFFECTS);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.potionEffectsSetting.getValue()) {
               ArrayList arrayListInst = new ArrayList(mc.player.getStatusEffects());
               int intVal11 = arrayListInst.size() * 1009 + 7;

               for (StatusEffectInstance class1293 : (Iterable<StatusEffectInstance>)arrayListInst) {
                  try {
                     intVal11 = intVal11 * 31 + Registries.STATUS_EFFECT.getRawId((StatusEffect)class1293.getEffectType().value()) + class1293.getAmplifier();
                  } catch (Throwable error3) {
                  }
               }

               if (sortedStatusEffects == null || intVal11 != statusEffectHash || systemValue - lastStatusRefresh >= 1000L) {
                  statusEffectHash = intVal11;
                  lastStatusRefresh = systemValue;
                  ArrayList arrayListInst2 = new ArrayList(arrayListInst);
                  arrayListInst2.sort(Comparator.comparingInt(item -> {
                     return measureTextWidth(formatStatusEffectName((StatusEffectInstance)item));
                  }));
                  ArrayList arrayListInst3 = new ArrayList(arrayListInst2.size());

                  for (StatusEffectInstance class12932 : (Iterable<StatusEffectInstance>)arrayListInst2) {
                     arrayListInst3.add(formatStatusEffectName(class12932));
                  }

                  sortedStatusEffects = arrayListInst2;
                  statusEffectLabels = arrayListInst3;
               }

               ArrayList sortedStatusEffectsSnapshot = sortedStatusEffects;
               if (!sortedStatusEffectsSnapshot.isEmpty()) {
                  int[] local12 = getSavedElementPos(HudElementType.POTION_EFFECTS);
                  int intVal12 = local12[1];

                  for (int index = 0; index < sortedStatusEffectsSnapshot.size(); index++) {
                     StatusEffectInstance local13 = (StatusEffectInstance)sortedStatusEffectsSnapshot.get(index);
                     String local14 = statusEffectLabels != null && index < statusEffectLabels.size() ? (String)statusEffectLabels.get(index) : formatStatusEffectName(local13);
                     float floatVal8 = measureTextWidth(local14) + 23 + 3.5F;
                     byte byteVal2 = 12;
                     if (flag) {
                        rainbowColorAt(floatVal2, index * 15);
                     } else {
                        lerpColor(accentColor, darkenColor(accentColor), (float)index / Math.max(1, sortedStatusEffectsSnapshot.size() - 1));
                     }

                     queueHudPanel(arg, local12[0], intVal12, floatVal8, byteVal2, 2.0F, colorInst.getRGB());
                     boolean falseSnapshot = false;

                     try {
                        Identifier local15 = Registries.STATUS_EFFECT.getId((StatusEffect)local13.getEffectType().value());
                        if (local15 != null && "minecraft".equals(local15.getNamespace())) {
                           Identifier class2960Id = Identifier.of("minecraft", "textures/mob_effect/" + local15.getPath() + ".png");
                           GuiRenderUtil.drawTexture(arg, local12[0] + 5, intVal12 + 1, 10.0F, class2960Id, -1, 1.0F, false);
                           falseSnapshot = true;
                        }
                     } catch (Throwable error4) {
                     }

                     if (!falseSnapshot) {
                        drawHudIcon(arg, local12[0] + 4, intVal12, potionTexture, flag ? local.getRGB() : accentColor.getRGB());
                     }

                     drawIconDivider(arg, local12[0] + 4 + 12 + 3, intVal12 + 2.0F, 1627389951);
                     drawTextAt(arg, local14, local12[0] + 4 + 12 + 3 + 4, intVal12 + 0.5F, getFontColor());
                     intVal12 += byteVal2 + 2;
                  }
               }
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.ARMOR);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.armorSetting.getValue()) {
               int[] local16 = getSavedElementPos(HudElementType.ARMOR);
               EquipmentSlot[] local17 = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
               int intVal13 = local16[1];

               for (EquipmentSlot class1304 : local17) {
                  ItemStack local18 = mc.player.getEquippedStack(class1304);
                  if (local18 != null && !local18.isEmpty()) {
                     int local18Value = local18.isDamageable() && local18.getMaxDamage() > 0
                        ? (int)Math.round((1.0 - (double)local18.getDamage() / local18.getMaxDamage()) * 100.0)
                        : 100;
                     Color local19 = local18Value >= 66 ? armorHighColor : (local18Value >= 33 ? armorMidColor : armorLowColor);
                     String var16588Value = local18Value + "%";
                     int intVal14 = 16 + measureTextWidth(var16588Value) + 12;
                     byte byteVal3 = 16;
                     fillRoundedPanel(arg, colorInst, local16[0], intVal13, local16[0] + intVal14, intVal13 + byteVal3, 2.0);
                     arg.drawItem(local18, local16[0] + 6, intVal13 + 1);
                     drawTextAt(arg, var16588Value, local16[0] + 24, intVal13 + 0.5F, local19.getRGB());
                     intVal13 += byteVal3 + 2;
                  }
               }
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.KEYBINDS);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.keybindsSetting.getValue()) {
               List local20 = getBoundModules();
               if (!local20.isEmpty()) {
                  int[] local21 = getSavedElementPos(HudElementType.KEYBINDS);
                  Color var7Snapshot2 = local;
                  String local22 = "HOTKEYS";
                  float floatVal9 = measureTextWidth(local22) + 6 + 3.5F;
                  queueHudPanel(arg, local21[0], local21[1], floatVal9, 12.0F, 2.0F, colorInst.getRGB());
                  drawTextAt(arg, local22, local21[0] + 6, local21[1] + 0.5F, var7Snapshot2.getRGB());
                  int intVal15 = local21[1] + 13;

                  for (int index2 = 0; index2 < local20.size(); index2++) {
                     ModuleBase local23 = (ModuleBase)local20.get(index2);
                     String local24 = local23.getName2().toUpperCase();
                     String clickGuiScreenValue = ClickGuiScreen.bY(local23.getKeyCode()).toUpperCase();
                     Color local37 = flag
                        ? rainbowColorAt(floatVal2, index2 * 15)
                        : lerpColor(accentColor, darkenColor(accentColor), (float)index2 / Math.max(1, local20.size() - 1));
                     float floatVal10 = measureTextWidth(local24) + measureTextWidth("  ") + measureTextWidth(clickGuiScreenValue) + 8 + 3.5F;
                     Color var84Snapshot = colorInst;
                     int intVal16 = local23.isEnabled() ? accentColor.getRGB() : getFontColor();
                     int intVal17 = local23.isEnabled() ? -1 : local37.getRGB();
                     queueHudPanel(arg, local21[0], intVal15, floatVal10, 12.0F, 2.0F, var84Snapshot.getRGB());
                     drawTextAt(arg, local24, local21[0] + 8, intVal15 + 0.5F, intVal16);
                     drawTextAt(arg, clickGuiScreenValue, local21[0] + 8 + measureTextWidth(local24) + measureTextWidth("  "), intVal15 + 0.5F, intVal17);
                     intVal15 += 13;
                  }
               }
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.MODULE_LIST);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.moduleListSetting.getValue()) {
               int[] local25 = getSavedElementPos(HudElementType.MODULE_LIST);
               ArrayList arrayListInst4 = new ArrayList();
               ArrayList arrayListInst5 = new ArrayList();
               long systemValue2 = System.nanoTime();
               float floatVal11 = lastFrameNanos == 0L ? 0.016F : Math.min(0.05F, (float)(systemValue2 - lastFrameNanos) / 1.0E9F);
               lastFrameNanos = systemValue2;

               for (ModuleBase moduleBase : (Iterable<ModuleBase>)ConfigManager.INSTANCE.getModules()) {
                  if (moduleBase.getCategory() != ModuleCategory.CLIENT) {
                     float floatVal12 = moduleBase.isEnabled() ? 1.0F : 0.0F;
                     float moduleListAnimationsValue = (float)(Float)moduleListAnimations.getOrDefault(moduleBase, 0.0F);
                     if (floatVal12 > moduleListAnimationsValue) {
                        moduleListAnimationsValue = Math.min(1.0F, moduleListAnimationsValue + floatVal11 / 0.28F);
                     } else if (floatVal12 < moduleListAnimationsValue) {
                        moduleListAnimationsValue = Math.max(0.0F, moduleListAnimationsValue - floatVal11 / 0.22F);
                     }

                     if (moduleListAnimationsValue > 0.02F) {
                        moduleListAnimations.put(moduleBase, moduleListAnimationsValue);
                        arrayListInst4.add(moduleBase);
                     } else {
                        arrayListInst5.add(moduleBase);
                     }
                  }
               }

               for (int index3 = 0; index3 < arrayListInst5.size(); index3++) {
                  moduleListAnimations.remove(arrayListInst5.get(index3));
               }

               arrayListInst4.sort(Comparator.<ModuleBase>comparingInt(item -> {
                  return measureTextWidth(item.getName2().toUpperCase());
               }).reversed().thenComparing(ModuleBase::getName2));
               int intVal18 = local25[1];
               byte byteVal4 = 12;

               for (int index4 = 0; index4 < arrayListInst4.size(); index4++) {
                  ModuleBase local26 = (ModuleBase)arrayListInst4.get(index4);
                  String local27 = local26.getName2().toUpperCase();
                  float moduleListAnimationsValue2 = (float)(Float)moduleListAnimations.getOrDefault(local26, 1.0F);
                  float floatVal13 = 1.0F - (1.0F - moduleListAnimationsValue2) * (1.0F - moduleListAnimationsValue2) * (1.0F - moduleListAnimationsValue2);
                  float floatVal14 = measureTextWidth(local27) + 6 + 3.5F;
                  float floatVal15 = local25[0] - floatVal14 + (1.0F - floatVal13) * (floatVal14 + 14.0F);
                  queueHudPanel(arg, floatVal15, intVal18, floatVal14, byteVal4, 2.0F, applyAlpha(colorInst.getRGB(), floatVal13));
                  Color local28 = flag ? rainbowColorAt(floatVal2, index4 * 20) : accentColor;
                  Color colorInst2 = new Color(applyAlpha(local28.getRGB(), floatVal13), true);
                  fillRoundedPanel(arg, colorInst2, floatVal15 + floatVal14 - 1.5, intVal18, floatVal15 + floatVal14, intVal18 + byteVal4, 0.75);
                  drawTextF(arg, local27, floatVal15 + 6.0F, intVal18 + 0.5F, applyAlpha(getFontColor(), floatVal13));
                  intVal18 += byteVal4 + 1;
               }
            }

            arg.getMatrices().popMatrix();
            hudPushed--;
            floatVal3 = getScaledFactor(HudElementType.RADAR);
            arg.getMatrices().pushMatrix();
            hudPushed++;
            arg.getMatrices().scale(floatVal3, floatVal3);
            if ((Boolean)instance.radarSetting.getValue() && mc.world != null) {
               int[] local29 = getSavedElementPos(HudElementType.RADAR);
               int intVal19 = (Integer)instance.radarSizeSetting.getValue();
               int intVal20 = (Integer)instance.radarRangeSetting.getValue();
               float var1132Value = intVal19 / 2.0F;
               float floatVal16 = local29[0] + var1132Value;
               float floatVal17 = local29[1] + var1132Value;
               float instanceValue = (Boolean)instance.radarRotateSetting.getValue() ? mc.player.getYaw() : 0.0F;
               if ((Boolean)instance.radarRotateSetting.getValue()) {
               } else {
               }

               Color var7Snapshot3 = local;
               Color var7Snapshot4 = local;
               int intVal21 = (int)(floatVal * 140.0F) << 24 | 1315868;
               GuiRenderUtil.fillRoundedRect(arg, local29[0], local29[1], intVal19, intVal19, intVal19 / 2.0F, intVal21, false);
               float var1340Value = var1132Value * 0.5F;
               int intVal22 = applyAlpha(var7Snapshot4.getRGB(), 0.22F);
               GuiRenderUtil.fillRoundedRect(arg, floatVal16 - var1340Value, floatVal17 - var1340Value, var1340Value * 2.0F, var1340Value * 2.0F, var1340Value, intVal22, false);
               GuiRenderUtil.fillRoundedRect(
                  arg, floatVal16 - var1340Value + 1.0F, floatVal17 - var1340Value + 1.0F, var1340Value * 2.0F - 2.0F, var1340Value * 2.0F - 2.0F, var1340Value - 1.0F, intVal21, false
               );
               int intVal23 = applyAlpha(var7Snapshot4.getRGB(), 0.18F);
               GuiRenderUtil.fillRoundedRect(arg, local29[0], floatVal17 - 0.5F, intVal19, 1.0F, 0.5F, intVal23, false);
               GuiRenderUtil.fillRoundedRect(arg, floatVal16 - 0.5F, local29[1], 1.0F, intVal19, 0.5F, intVal23, false);
               float floatVal18 = (float)Math.toRadians((Boolean)instance.radarRotateSetting.getValue() ? mc.player.getYaw() + 90.0 : 180.0);
               String[] local30 = new String[]{"N", "E", "S", "W"};
               float[][] local31 = new float[][]{{0.0F, -1.0F}, {1.0F, 0.0F}, {0.0F, 1.0F}, {-1.0F, 0.0F}};

               for (int index5 = 0; index5 < 4; index5++) {
                  float floatVal19 = rotateRadarX(local31[index5][0], local31[index5][1], floatVal18);
                  float floatVal20 = rotateRadarZ(local31[index5][0], local31[index5][1], floatVal18);
                  float var140Var32Var1345Value = floatVal16 + floatVal19 * (var1132Value - 5.0F);
                  float var147Var33Var1345Value = floatVal17 + floatVal20 * (var1132Value - 5.0F);
                  arg.getMatrices().pushMatrix();
                  hudPushed++;
                  arg.getMatrices().translate(var140Var32Var1345Value, var147Var33Var1345Value);
                  arg.getMatrices().scale(1.0F, 1.0F);
                  CustomFontManager.INSTANCE7
                     .drawText(arg, local30[index5], -CustomFontManager.INSTANCE7.getStringWidth(local30[index5]) / 2.0F, -4.0F, index5 == 0 ? var7Snapshot3.getRGB() : -1);
                  arg.getMatrices().popMatrix();
                  hudPushed--;
               }

               try {
                  ModuleBase configManagerValue = ConfigManager.INSTANCE.getModuleByName("Sus Chunk Finder");
                  if (configManagerValue instanceof SusChunkFinderModule local38 && configManagerValue.isEnabled()) {
                     float var125Snapshot = intVal20;
                     int intVal24 = local38.call2();
                     List local32 = local38.call1();
                     double mcValue = mc.player.getX();
                     double mcValue2 = mc.player.getZ();
                     float floatVal21 = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() * Math.PI * 2.0 / 1800.0));

                     for (ChunkClusterRecord chunkClusterRecord : (Iterable<ChunkClusterRecord>)local32) {
                        float floatVal22 = (float)(chunkClusterRecord.centroidX() - mcValue);
                        float floatVal23 = (float)(chunkClusterRecord.centroidZ() - mcValue2);
                        float floatVal24 = (float)Math.sqrt(floatVal22 * floatVal22 + floatVal23 * floatVal23);
                        if (!(floatVal24 < 0.5F)) {
                           boolean flag2 = floatVal24 > var125Snapshot;
                           float minValue = Math.min(floatVal24 / var125Snapshot, 1.0F);
                           float floatVal25 = flag2 ? 6.0F : 9.0F;
                           float var140RotateRadarXVar43Var = floatVal16 + rotateRadarX(floatVal22, floatVal23, floatVal18) / floatVal24 * minValue * (var1132Value - floatVal25);
                           float var147RotateRadarZVar43Var = floatVal17 + rotateRadarZ(floatVal22, floatVal23, floatVal18) / floatVal24 * minValue * (var1132Value - floatVal25);
                           float floatVal26 = intVal24 <= 0 ? 1.0F : Math.max(0.0F, Math.min(1.0F, (float)((chunkClusterRecord.maxScore() - intVal24) / (intVal24 * 2.0))));
                           float floatVal27 = (flag2 ? 2.4F : 3.0F + 1.5F * floatVal26) * (1.0F + 0.1F * floatVal21);
                           float floatVal28 = (0.72F + 0.28F * floatVal26) * (0.88F + 0.12F * floatVal21);
                           int intVal25 = applyAlpha(var7Snapshot4.getRGB(), floatVal28);
                           int intVal26 = applyAlpha(var7Snapshot4.getRGB(), (0.3F + 0.25F * floatVal26) * floatVal28);
                           GuiRenderUtil.fillRoundedRect(
                              arg, var140RotateRadarXVar43Var - floatVal27 - 3.5F, var147RotateRadarZVar43Var - floatVal27 - 3.5F, floatVal27 * 2.0F + 7.0F, floatVal27 * 2.0F + 7.0F, floatVal27 * 0.6F + 2.0F, intVal26, false
                           );
                           GuiRenderUtil.fillRoundedRect(arg, var140RotateRadarXVar43Var - floatVal27, var147RotateRadarZVar43Var - floatVal27, floatVal27 * 2.0F, floatVal27 * 2.0F, floatVal27 * 0.6F, intVal25, false);
                           GuiRenderUtil.strokeRoundedRect(
                              arg,
                              var140RotateRadarXVar43Var - floatVal27,
                              var147RotateRadarZVar43Var - floatVal27,
                              floatVal27 * 2.0F,
                              floatVal27 * 2.0F,
                              floatVal27 * 0.6F,
                              1.0F,
                              applyAlpha(var7Snapshot3.getRGB(), 0.55F * floatVal28),
                              false
                           );
                           if (flag2 && floatVal24 <= var125Snapshot * 2.5F) {
                              String local33 = (int)floatVal24 + "m";
                              int intVal27 = measureTextWidth(local33);
                              float var49Var140Var490Value = var140RotateRadarXVar43Var + (floatVal16 - var140RotateRadarXVar43Var) * 0.24F;
                              float var50Var147Var500Value = var147RotateRadarZVar43Var + (floatVal17 - var147RotateRadarZVar43Var) * 0.24F;
                              GuiRenderUtil.fillRoundedRect(
                                 arg, var49Var140Var490Value - intVal27 / 2.0F - 3.0F, var50Var147Var500Value - 5.5F, intVal27 + 6.0F, 11.0F, 5.5F, (int)(floatVal * 200.0F) << 24 | 1315868, false
                              );
                              drawTextF(arg, local33, var49Var140Var490Value - intVal27 / 2.0F, (int)(var50Var147Var500Value - 5.5F), applyAlpha(var7Snapshot3.getRGB(), 0.95F));
                           }
                        }
                     }
                  }
               } catch (Throwable error5) {
               }

               float floatVal29 = (var1132Value - 5.0F) / intVal20;
               if (systemValue - lastRadarRefresh >= 100L) {
                  lastRadarRefresh = systemValue;
                  radarEntities = new ArrayList(
                     mc.world.getEntitiesByClass(Entity.class, mc.player.getBoundingBox().expand(intVal20, intVal20, intVal20), item2 -> {
                        return item2 != mc.player;
                     })
                  );
               }

               StaffDetectorModule nullSnapshot = null;
               Map nullSnapshot2 = null;

               try {
                  if (ConfigManager.INSTANCE.getModuleByName("Staff Detector") instanceof StaffDetectorModule local39 && local39.isEnabled()) {
                     nullSnapshot = local39;
                     nullSnapshot2 = local39.call8();
                  }
               } catch (Throwable error6) {
               }

               boolean flag3 = (Boolean)instance.radarPlayersSetting.getValue();
               boolean flag4 = (Boolean)instance.radarHostileSetting.getValue();
               boolean flag5 = (Boolean)instance.radarPassiveSetting.getValue();
               boolean flag6 = (Boolean)instance.radarRotateSetting.getValue();

               for (Entity class1297 : (Iterable<Entity>)radarEntities) {
                  boolean flag7 = class1297 instanceof PlayerEntity;
                  boolean flag8 = class1297 instanceof HostileEntity;
                  boolean flag9 = class1297 instanceof PassiveEntity;
                  if ((!flag7 || flag3) && (!flag8 || flag4) && (!flag9 || flag5) && (flag7 || flag8 || flag9)) {
                     double class1297Value = class1297.getX() - mc.player.getX();
                     double class1297Value2 = class1297.getZ() - mc.player.getZ();
                     if (!(Math.sqrt(class1297Value * class1297Value + class1297Value2 * class1297Value2) > intVal20)) {
                        float floatVal30 = (float)Math.toRadians(flag6 ? 180.0 - instanceValue : -instanceValue);
                        float floatVal31 = (float)(class1297Value * Math.cos(floatVal30) - class1297Value2 * Math.sin(floatVal30));
                        float floatVal32 = (float)(class1297Value * Math.sin(floatVal30) + class1297Value2 * Math.cos(floatVal30));
                        float var140Var228Var208Value = floatVal16 + floatVal31 * floatVal29;
                        float var147Var229Var208Value = floatVal17 + floatVal32 * floatVal29;
                        float floatVal33 = (float)Math.sqrt((var140Var228Var208Value - floatVal16) * (var140Var228Var208Value - floatVal16) + (var147Var229Var208Value - floatVal17) * (var147Var229Var208Value - floatVal17));
                        if (floatVal33 > var1132Value - 4.0F) {
                           float floatVal34 = (var1132Value - 4.0F) / floatVal33;
                           var140Var228Var208Value = floatVal16 + (var140Var228Var208Value - floatVal16) * floatVal34;
                           var147Var229Var208Value = floatVal17 + (var147Var229Var208Value - floatVal17) * floatVal34;
                        }

                        if (flag7) {
                           byte byteVal5 = 7;
                           float var230Var2342Value = var140Var228Var208Value - byteVal5 / 2.0F;
                           float var231Var2342Value = var147Var229Var208Value - byteVal5 / 2.0F;
                           boolean falseSnapshot2 = false;
                           if (nullSnapshot != null && nullSnapshot2 != null) {
                              try {
                                 falseSnapshot2 = nullSnapshot2.containsKey(((PlayerEntity)class1297).getName().getString());
                              } catch (Throwable error7) {
                              }
                           }

                           AbstractClientPlayerEntity local34 = (AbstractClientPlayerEntity)class1297;
                           Identifier nullSnapshot3 = null;

                           try {
                              UUID local35 = ((PlayerEntity)class1297).getUuid();
                              nullSnapshot3 = (Identifier)playerSkinCache.get(local35);
                           } catch (Throwable error8) {
                           }

                           if (nullSnapshot3 == null) {
                              try {
                                 SkinTextures var238Value = local34.getSkin();

                                 for (Method method : var238Value.getClass().getMethods()) {
                                    if (method.getParameterCount() == 0) {
                                       method.setAccessible(true);
                                       Object nullSnapshot4 = null;

                                       try {
                                          nullSnapshot4 = method.invoke(var238Value);
                                       } catch (Exception error9) {
                                          continue;
                                       }

                                       if (nullSnapshot4 != null) {
                                          if (nullSnapshot4 instanceof Identifier) {
                                             nullSnapshot3 = (Identifier)nullSnapshot4;
                                             break;
                                          }

                                          try {
                                             for (Method method2 : nullSnapshot4.getClass().getMethods()) {
                                                if (method2.getParameterCount() == 0 && method2.getReturnType() == Identifier.class) {
                                                   method2.setAccessible(true);
                                                   nullSnapshot3 = (Identifier)method2.invoke(nullSnapshot4);
                                                   if (nullSnapshot3 != null) {
                                                      break;
                                                   }
                                                }
                                             }
                                          } catch (Exception error10) {
                                          }

                                          if (nullSnapshot3 != null) {
                                             break;
                                          }
                                       }
                                    }
                                 }
                              } catch (Exception error11) {
                              }
                           }

                           try {
                              if (nullSnapshot3 != null) {
                                 if (playerSkinCache.size() > 256) {
                                    playerSkinCache.clear();
                                 }

                                 playerSkinCache.put(((PlayerEntity)class1297).getUuid(), nullSnapshot3);
                              }
                           } catch (Throwable error12) {
                           }

                           Identifier local36 = resolveSkinHeadTexture(nullSnapshot3);
                           if (local36 != null) {
                              GuiRenderUtil.drawTexture(arg, var230Var2342Value, var231Var2342Value, byteVal5, local36, -1, 0.0F, false);
                           } else {
                              GuiRenderUtil.renderArcRaw(arg, var230Var2342Value, var231Var2342Value, byteVal5, byteVal5, 360.0F, 0.0F, var7Snapshot3.getRGB(), false);
                           }

                           if (falseSnapshot2) {
                              float floatVal35 = (float)(0.5 + 0.5 * Math.sin(systemValue / 250.0));
                              int intVal28 = (int)(floatVal35 * 150.0F);
                              GuiRenderUtil.fillRoundedRect(arg, var230Var2342Value - 2.0F, var231Var2342Value - 2.0F, byteVal5 + 4, byteVal5 + 4, 2.0F, intVal28 << 24 | 16720418, false);
                           }
                        } else {
                           GuiRenderUtil.renderArcRaw(arg, var140Var228Var208Value - 1.5F, var147Var229Var208Value - 1.5F, 3.0F, 3.0F, 360.0F, 0.0F, flag8 ? -50116 : -11477936, false);
                        }
                     }
                  }
               }

               arg.getMatrices().pushMatrix();
               hudPushed++;
               arg.getMatrices().translate(floatVal16, floatVal17);
               float maxValue = Math.max(2.0F, intVal19 * 0.027F);
               GuiRenderUtil.fillRoundedRect(
                  arg, -maxValue - 3.0F, -maxValue - 3.0F, (maxValue + 3.0F) * 2.0F, (maxValue + 3.0F) * 2.0F, maxValue + 3.0F, applyAlpha(var7Snapshot3.getRGB(), 0.3F), false
               );
               GuiRenderUtil.fillRoundedRect(arg, -maxValue, -maxValue, maxValue * 2.0F, maxValue * 2.0F, maxValue, var7Snapshot3.getRGB(), false);
               arg.getMatrices().popMatrix();
               hudPushed--;
            }

            arg.getMatrices().popMatrix();
            hudPushed--;

            try {
               renderToasts(arg);
            } catch (Throwable error13) {
            }

            StaffDetectorModule.call2(arg);
            } finally {
               // Any throw swallowed by a catch (Throwable) between a push and its
               // pop would otherwise leave Matrix3x2fStack unbalanced for the rest
               // of the session, permanently skewing every later HUD frame.
               while (hudPushed > 0) {
                  arg.getMatrices().popMatrix();
                  hudPushed--;
               }
            }
         }
      }
   }

   public static float rotateRadarX(float floatVal, float floatVal2, float floatVal3) {
      return (float)(-floatVal * Math.sin(floatVal3) + floatVal2 * Math.cos(floatVal3));
   }

   public static float rotateRadarZ(float floatVal, float floatVal2, float floatVal3) {
      return (float)(-(floatVal * Math.cos(floatVal3) + floatVal2 * Math.sin(floatVal3)));
   }

   public static Color colorFromHue(float floatVal) {
      float floatVal2 = floatVal % 360.0F / 360.0F;
      if (floatVal2 < 0.0F) {
         floatVal2++;
      }

      return Color.getHSBColor(floatVal2, 0.6F, 1.0F);
   }

   public static Color rainbowColorAt(float floatVal, int intVal) {
      float maxValue = Math.max(0.05F, floatVal);
      float floatVal2 = (float)(System.currentTimeMillis() % 12000L) / 12000.0F * maxValue + intVal % 360 / 360.0F;
      float var3FloatMathValue = floatVal2 - (float)Math.floor(floatVal2);
      var3FloatMathValue = var3FloatMathValue < 0.5F ? var3FloatMathValue * 2.0F : 2.0F - var3FloatMathValue * 2.0F;
      return Color.getHSBColor(var3FloatMathValue, 0.6F, 1.0F);
   }

   public static Color darkenColor(Color color) {
      return new Color(Math.max(0, (int)(color.getRed() * 0.6F)), Math.max(0, (int)(color.getGreen() * 0.6F)), Math.max(0, (int)(color.getBlue() * 0.6F)), 255);
   }

   public static Color lerpColor(Color color, Color color2, float floatVal) {
      floatVal = Math.max(0.0F, Math.min(1.0F, floatVal));
      return new Color(
         (int)(color.getRed() + (color2.getRed() - color.getRed()) * floatVal),
         (int)(color.getGreen() + (color2.getGreen() - color.getGreen()) * floatVal),
         (int)(color.getBlue() + (color2.getBlue() - color.getBlue()) * floatVal),
         255
      );
   }

   public static float getStoredElementScale(HudElementType hudElementType) {
      return (float)(Float)elementScales.getOrDefault(hudElementType, 1.0F);
   }

   public static int[] getElementScreenPos(HudElementType hudElementType) {
      int[] local = getSavedElementPos(hudElementType);
      return new int[]{Math.round(local[0]), Math.round(local[1])};
   }

   public static void fillRoundedPanel(DrawContext arg, Color color, double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5) {
      GuiRenderUtil.fillRoundedRect(arg, (float)doubleVal, (float)doubleVal2, (float)(doubleVal3 - doubleVal), (float)(doubleVal4 - doubleVal2), (float)doubleVal5, color.getRGB(), false);
   }

   public static void drawTextAt(DrawContext arg, String string, int intVal, float floatVal, int intVal2) {
      instance.call1(arg, string, intVal, floatVal, intVal2);
   }

   public static void drawTextF(DrawContext arg, String string, float floatVal, float floatVal2, int intVal) {
      instance.call1(arg, string, floatVal, floatVal2, intVal);
   }

   public static int measureTextWidth(String string) {
      return instance.call2(string);
   }

   public static void drawHudIcon(DrawContext arg, float floatVal, float floatVal2, Identifier arg2, int intVal) {
      try {
         GuiRenderUtil.drawTexture(arg, floatVal, floatVal2, 12.0F, arg2, intVal, 1.0F, false);
      } catch (Throwable error) {
      }
   }

   public static void drawHudIconScaled(DrawContext arg, float floatVal, float floatVal2, float floatVal3, Identifier arg2, int intVal) {
      try {
         GuiRenderUtil.drawTexture(arg, floatVal, floatVal2, floatVal3, arg2, intVal, 1.0F, false);
      } catch (Throwable error) {
      }
   }

   public static void drawIconDivider(DrawContext arg, float floatVal, float floatVal2, int intVal) {
      GuiRenderUtil.fillRoundedRect(arg, floatVal, floatVal2, 1.5F, 8.0F, 0.75F, intVal, false);
   }

   public static void drawNegativeSign(DrawContext arg, float floatVal, float floatVal2, int intVal) {
      GuiRenderUtil.fillRoundedRect(arg, floatVal, floatVal2 + 4.5F, 6.0F, 1.5F, 0.75F, intVal, false);
   }

   public static int applyAlpha(int intVal, float floatVal) {
      int maxValue = Math.max(0, Math.min(255, (int)((intVal >>> 24 & 0xFF) * floatVal)));
      return maxValue << 24 | intVal & 16777215;
   }

   public static int scaleArgbChannels(int intVal, float floatVal) {
      int intVal2 = intVal >>> 24 & 0xFF;
      int minValue = Math.min(255, (int)((intVal >> 16 & 0xFF) * floatVal));
      int minValue2 = Math.min(255, (int)((intVal >> 8 & 0xFF) * floatVal));
      int minValue3 = Math.min(255, (int)((intVal & 0xFF) * floatVal));
      return intVal2 << 24 | minValue << 16 | minValue2 << 8 | minValue3;
   }

   public static int blendArgbColors(int intVal, int intVal2) {
      int intVal3 = ((intVal >> 16 & 0xFF) + (intVal2 >> 16 & 0xFF)) / 2;
      int intVal4 = ((intVal >> 8 & 0xFF) + (intVal2 >> 8 & 0xFF)) / 2;
      int intVal5 = ((intVal & 0xFF) + (intVal2 & 0xFF)) / 2;
      int intVal6 = ((intVal >>> 24 & 0xFF) + (intVal2 >>> 24 & 0xFF)) / 2;
      return intVal6 << 24 | intVal3 << 16 | intVal4 << 8 | intVal5;
   }

   public static void queueHudPanel(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, int intVal) {
      int intVal2 = scaleArgbChannels(intVal, 1.35F);
      int intVal3 = blendArgbColors(intVal2, intVal);
      GuiRenderUtil.queueRoundedRect(
         GuiRenderUtil.getContextProjectionMatrix(arg), floatVal, floatVal2, floatVal3, floatVal4, floatVal5, floatVal5, floatVal5, floatVal5, false, intVal2, intVal2, intVal2, intVal3, intVal3, intVal3, intVal, intVal, intVal
      );
   }

   public static void drawCrosshair(DrawContext arg, float floatVal, float floatVal2, float floatVal3, int intVal) {
      float var1Var32Value = floatVal + floatVal3 / 2.0F;
      float var2Var32Value = floatVal2 + floatVal3 / 2.0F;
      float var32Value = floatVal3 / 2.0F;
      GuiRenderUtil.fillRoundedRect(arg, var1Var32Value - var32Value, var2Var32Value - 0.75F, floatVal3, 1.5F, 0.75F, intVal, false);
      GuiRenderUtil.fillRoundedRect(arg, var1Var32Value - 0.75F, var2Var32Value - var32Value, 1.5F, floatVal3, 0.75F, intVal, false);
   }

   public static void saveHudElementPos(HudElementType hudElementType, int intVal, int intVal2) {
      int[] local = call4(hudElementType, intVal, intVal2);
      elementPositions.put(hudElementType, local);
      ConfigManager.INSTANCE.notifyLayoutChanged();
   }

   public static void applyHudElementScale(HudElementType hudElementType, float floatVal) {
      if (hudElementType == HudElementType.SPOTIFY_HUD) {
         SpotifyHudModule.setSpotifyPanelScale(Math.max(0.5F, Math.min(3.0F, floatVal)));
      } else {
         elementScales.put(hudElementType, Math.max(0.5F, Math.min(3.0F, floatVal)));
      }

      ConfigManager.INSTANCE.notifyLayoutChanged();
   }

   public static float getGlobalAlphaScale() {
      return 0.6F;
   }

   public static int[] getModuleListBounds() {
      return call5();
   }

   public static boolean isHudActive() {
      return instance != null && instance.isEnabled();
   }

   public static void call18(ModuleBase moduleBase, boolean flag) {
      if (moduleBase != null && moduleBase.getCategory() != ModuleCategory.CLIENT) {
         pushToast(moduleBase.getName2(), flag ? "enabled" : "disabled", flag ? toastEnabledColor : toastDisabledColor, null);
      }
   }

   public static void call19(String string, String string2, int intVal, Identifier arg) {
      pushToast(string, string2, intVal, arg);
   }

   public static void pushToast(String string, String string2, int intVal, Identifier arg) {
      if (instance != null && instance.isEnabled()) {
         try {
            if (!(Boolean)instance.notificationsSetting.getValue()) {
               return;
            }
         } catch (Throwable error) {
            return;
         }

         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.world == null) {
               return;
            }
         } catch (Throwable error2) {
            return;
         }

         if (string == null) {
            string = "";
         }

         if (string2 == null) {
            string2 = "";
         }

         toastQueue.offer(new HudToastEntry(string, string2, intVal, arg));

         while (toastQueue.size() > 5) {
            toastQueue.poll();
         }
      }
   }

   public static void renderToasts(DrawContext arg) {
      if (instance != null && instance.isEnabled()) {
         try {
            if (!(Boolean)instance.notificationsSetting.getValue()) {
               return;
            }
         } catch (Throwable error) {
            return;
         }

         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.player != null) {
            long systemValue = System.currentTimeMillis();
            long longVal = 1440L;
            ArrayList arrayListInst = new ArrayList(toastQueue);

            for (int index = 0; index < arrayListInst.size(); index++) {
               if (systemValue - ((HudToastEntry)arrayListInst.get(index)).createdAt >= longVal) {
                  toastQueue.remove(arrayListInst.get(index));
               }
            }

            ArrayList arrayListInst2 = new ArrayList(toastQueue);
            int minValue = Math.min(arrayListInst2.size(), 5);
            if (minValue != 0) {
               int var1Value = mc.getWindow().getScaledWidth();
               int var1Value2 = mc.getWindow().getScaledHeight();
               float floatVal = (Float)instance.opacitySetting.getValue();
               Color clickGuiModuleValue = ClickGuiModule.getBackgroundColor();
               Color colorInst = new Color(clickGuiModuleValue.getRed(), clickGuiModuleValue.getGreen(), clickGuiModuleValue.getBlue(), (int)(floatVal * 255.0F));
               byte byteVal = 24;
               int var1044Value = var1Value2 - 44;

               for (int index2 = 0; index2 < minValue; index2++) {
                  HudToastEntry local = (HudToastEntry)arrayListInst2.get(arrayListInst2.size() - 1 - index2);
                  long var2Var17Value = systemValue - local.createdAt;
                  if (var2Var17Value < 0L) {
                  }

                  if (var2Var17Value < longVal) {
                     float local2;
                     float var24Snapshot;
                     float local3;
                     if (var2Var17Value < 220L) {
                        float floatVal2 = (float)var2Var17Value / 220.0F;
                        float floatVal3 = 1.0F - (1.0F - floatVal2) * (1.0F - floatVal2) * (1.0F - floatVal2);
                        local2 = 1.0F - floatVal3;
                        var24Snapshot = floatVal3;
                        local3 = 1.0F;
                     } else if (var2Var17Value < 1220L) {
                        local2 = 0.0F;
                        var24Snapshot = 1.0F;
                        local3 = 1.0F - (float)(var2Var17Value - 220L) / 1000.0F;
                     } else {
                        float floatVal4 = (float)(var2Var17Value - 220L - 1000L) / 220.0F;
                        local2 = floatVal4 * floatVal4 * floatVal4;
                        var24Snapshot = 1.0F - floatVal4;
                        local3 = 0.0F;
                     }

                     int intVal = measureTextWidth(local.title);
                     int intVal2 = measureTextWidth(" " + local.subtitle);
                     int intVal3 = local.toastIcon != null ? 16 : 0;
                     float floatVal5 = 6 + intVal3 + intVal + intVal2 + 3.5F;
                     byte byteVal2 = 18;
                     float var96Var26Var20Var2612Valu = var1Value - 6 - floatVal5 + local2 * (floatVal5 + 12.0F);
                     int var15Var16Var14Var27Value = var1044Value - index2 * byteVal - byteVal2;
                     int intVal4 = applyAlpha(colorInst.getRGB(), var24Snapshot);
                     queueHudPanel(arg, var96Var26Var20Var2612Valu, var15Var16Var14Var27Value, floatVal5, byteVal2, 2.0F, intVal4);
                     float var286Value = var96Var26Var20Var2612Valu + 6.0F;
                     if (local.toastIcon != null) {
                        try {
                           GuiRenderUtil.drawTexture(arg, var96Var26Var20Var2612Valu + 5.0F, var15Var16Var14Var27Value + 3, 12.0F, local.toastIcon, applyAlpha(-1, var24Snapshot), 1.0F, false);
                        } catch (Throwable error2) {
                        }

                        var286Value += 16.0F;
                     }

                     drawTextF(arg, local.title, var286Value, var15Var16Var14Var27Value + 0.5F, applyAlpha(getFontColor(), var24Snapshot));
                     drawTextF(arg, " " + local.subtitle, var286Value + intVal, var15Var16Var14Var27Value + 0.5F, applyAlpha(local.durationMs, var24Snapshot));
                     if (local3 > 0.0F) {
                        float var266Value = floatVal5 - 6.0F;
                        int maxValue = Math.max(2, (int)(var266Value * local3));
                        GuiRenderUtil.fillRoundedRect(arg, var96Var26Var20Var2612Valu + 3.0F, var15Var16Var14Var27Value + byteVal2 - 2, maxValue, 1.5F, 0.75F, applyAlpha(local.durationMs, var24Snapshot), false);
                     }
                  }
               }
            }
         }
      }
   }

   public static String formatStatusEffectName(StatusEffectInstance arg) {
      String local = Registries.STATUS_EFFECT.getId((StatusEffect)arg.getEffectType().value()).getPath();
      String[] local2 = local.split("_");
      StringBuilder stringBuilderInst = new StringBuilder();

      for (String string : local2) {
         if (!string.isEmpty()) {
            stringBuilderInst.append(Character.toUpperCase(string.charAt(0)));
            if (string.length() > 1) {
               stringBuilderInst.append(string.substring(1));
            }

            stringBuilderInst.append(' ');
         }
      }

      String local3 = stringBuilderInst.toString().trim();
      int var0Value = arg.getAmplifier();
      if (var0Value > 0) {
         local3 = local3 + " " + toRomanNumeral(var0Value + 1);
      }

      int var0Value2 = arg.getDuration();
      if (var0Value2 < 32767) {
         int var1020Value = var0Value2 / 20;
         local3 = local3 + " " + String.format("%d:%02d", var1020Value / 60, var1020Value % 60);
      }

      return local3;
   }

   public static String toRomanNumeral(int intVal) {
      return switch (intVal) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         case 5 -> "V";
         case 6 -> "VI";
         case 7 -> "VII";
         case 8 -> "VIII";
         case 9 -> "IX";
         case 10 -> "X";
         default -> String.valueOf(intVal);
      };
   }

   public static int[] call5() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null) {
         return new int[]{0, 0, 120, 14};
      }

      int[] local = getSavedElementPos(HudElementType.MODULE_LIST);
      ArrayList arrayListInst = new ArrayList();

      for (ModuleBase moduleBase : (Iterable<ModuleBase>)ConfigManager.INSTANCE.getModules()) {
         float floatVal = 0.0F;

         try {
            floatVal = (float)(Float)moduleListAnimations.getOrDefault(moduleBase, 0.0F);
         } catch (Throwable error) {
         }

         if ((moduleBase.isEnabled() || floatVal > 0.02F) && moduleBase.getCategory() != ModuleCategory.CLIENT) {
            arrayListInst.add(moduleBase);
         }
      }

      if (arrayListInst.isEmpty()) {
         return new int[]{local[0], local[1], 80, 18};
      }

      arrayListInst.sort(Comparator.<ModuleBase>comparingInt(item -> {
         return measureTextWidth(item.getName2().toUpperCase());
      }).reversed().thenComparing(ModuleBase::getName2));
      int roundValue = Math.round(measureTextWidth(((ModuleBase)arrayListInst.get(0)).getName2().toUpperCase()) + 6 + 3.5F);
      int intVal = arrayListInst.size() * 13;
      return new int[]{local[0] - roundValue, local[1], roundValue, intVal};
   }

   public static int getFontColor() {
      try {
         if (ClickGuiModule.INSTANCE2 != null && ClickGuiModule.INSTANCE2.fontColorSetting != null && ClickGuiModule.INSTANCE2.fontColorSetting.getValue() != null) {
            return ((Color)ClickGuiModule.INSTANCE2.fontColorSetting.getValue()).getRGB();
         }
      } catch (Exception error) {
      }

      return -1;
   }

   public static String eastRadarLabel() {
      return "E";
   }

   public static Identifier resolveSkinHeadTexture(Identifier arg) {
      if (arg == null) {
         return null;
      }

      Identifier local = (Identifier)generatedHeadCache.get(arg);
      if (local != null) {
         return local;
      }

      if (failedTextureCache.contains(arg)) {
         return null;
      }

      if (failedTextureCache.size() >= 64) {
         return null;
      }

      MinecraftClient mc = MinecraftClient.getInstance();
      AbstractTexture var2Value = mc.getTextureManager().getTexture(arg);
      if (var2Value == null) {
         return null;
      }

      failedTextureCache.add(arg);
      mc.execute(() -> {
         try {
            AbstractTexture local5 = mc.getTextureManager().getTexture(arg);
            if (local5 == null) {
               failedTextureCache.remove(arg);
               return;
            }

            NativeImage var4xValue = null;
            if (local5 instanceof NativeImageBackedTexture local6) {
               var4xValue = local6.getImage();
            }

            if (var4xValue == null) {
               for (Field field : local5.getClass().getDeclaredFields()) {
                  if (field.getType().getSimpleName().equals("NativeImage")) {
                     field.setAccessible(true);

                     try {
                        var4xValue = (NativeImage)field.get(local5);
                     } catch (Exception error) {
                     }

                     if (var4xValue != null) {
                        break;
                     }
                  }
               }
            }

            if (var4xValue == null) {
               for (Class index = local5.getClass().getSuperclass(); index != null && var4xValue == null; index = index.getSuperclass()) {
                  for (Field field2 : index.getDeclaredFields()) {
                     if (field2.getType().getSimpleName().equals("NativeImage")) {
                        field2.setAccessible(true);

                        try {
                           var4xValue = (NativeImage)field2.get(local5);
                        } catch (Exception error2) {
                        }

                        if (var4xValue != null) {
                           break;
                        }
                     }
                  }
               }
            }

            if (var4xValue == null) {
               failedTextureCache.remove(arg);
               return;
            }

            byte byteVal = 16;
            NativeImage local2 = new NativeImage(byteVal, byteVal, false);
            int var3xValue = var4xValue.getWidth();
            int var3xValue2 = var4xValue.getHeight();

            for (int index2 = 0; index2 < byteVal; index2++) {
               for (int index3 = 0; index3 < byteVal; index3++) {
                  int local7 = 8 + index3 * 8 / byteVal;
                  int local8 = 8 + index2 * 8 / byteVal;
                  if (local7 < var3xValue && local8 < var3xValue2) {
                     local2.setColorArgb(index3, index2, var4xValue.getColorArgb(local7, local8));
                  }
               }
            }

            if (generatedHeadCache.size() >= 128) {
               Identifier local3 = (Identifier)generatedHeadCache.keySet().iterator().next();
               if (local3 != null) {
                  Identifier local4 = (Identifier)generatedHeadCache.remove(local3);
                  if (local4 != null) {
                     try {
                        mc.getTextureManager().destroyTexture(local4);
                     } catch (Exception error3) {
                     }
                  }
               }
            }

            Identifier class2960Id = Identifier.of("threesix", "player_head_" + headTextureCounter++);
            mc.getTextureManager().registerTexture(class2960Id, new NativeImageBackedTexture(() -> {
               return "ph";
            }, local2));
            generatedHeadCache.put(arg, class2960Id);
            failedTextureCache.remove(arg);
         } catch (Exception error4) {
            failedTextureCache.remove(arg);
         }
      });
      return null;
   }

}
