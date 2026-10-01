package com.threesix.module;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import com.threesix.util.XorBitUtils;
import com.threesix.data.SpotifyTrackState;
import com.threesix.internal.ModuleBase;
import com.threesix.module.HudModule;
import com.threesix.util.GuiRenderUtil;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.data.HudElementType;
import com.threesix.manager.HudLayoutManager;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.service.SpotifyBridgeService;

public final class SpotifyHudModule extends ModuleBase {
   public static SpotifyHudModule instance;
   public final ClientSetting scaleSetting = new ClientSetting("Scale", 1.0F, 0.6F, 2.0F);
   public final ClientSetting showControlsSetting = new ClientSetting("Show Controls", true);
   public final ClientSetting hideWhenIdleSetting = new ClientSetting("Hide When Idle", false);
   public static final int baseWidth = 270;
   public static final int baseHeight = 64;
   public static final Identifier artTexture = Identifier.of("threesix", "spotify_art");
   public static final Identifier prevIcon = Identifier.of("threesix", "textures/gui/hud/sp_prev.png");
   public static final Identifier playIcon = Identifier.of("threesix", "textures/gui/hud/sp_play.png");
   public static final Identifier pauseIcon = Identifier.of("threesix", "textures/gui/hud/sp_pause.png");
   public static final Identifier nextIcon = Identifier.of("threesix", "textures/gui/hud/sp_next.png");
   public static final Identifier progressDotIcon = Identifier.of("threesix", "textures/gui/hud/sp_dot.png");
   public volatile boolean hasArtwork = false;
   public volatile int artVersion2 = -1;
   public volatile long artLastModified = 0L;
   public static volatile int[] panelRect = new int[]{0, 0, 0, 0};
   public static volatile int[] prevButtonRect = new int[]{0, 0, 0, 0};
   public static volatile int[] playPauseButtonRect = new int[]{0, 0, 0, 0};
   public static volatile int[] nextButtonRect = new int[]{0, 0, 0, 0};
   public static volatile int[] progressBarRect = new int[]{0, 0, 0, 0};
   public static volatile boolean isSeekBarActive = false;

   public SpotifyHudModule() {
      super("Spotify HUD", ModuleCategory.CLIENT);
      this.registerSetting(this.scaleSetting);
      this.registerSetting(this.showControlsSetting);
      this.registerSetting(this.hideWhenIdleSetting);
      instance = this;
   }

   @Override
   public void onEnable() {
      try {
         SpotifyBridgeService.start();
      } catch (Throwable error) {
      }
   }

   @Override
   public void onDisable() {
      try {
         SpotifyBridgeService.stop();
      } catch (Throwable error) {
      }
   }

   public static boolean isSpotifyHudEnabled() {

      return instance != null && instance.isEnabled();
   }

   public static float getScale() {
      float floatVal = instance == null ? 1.0F : (Float)instance.scaleSetting.getValue();
      return floatVal;
   }

   public static void setSpotifyPanelScale(float floatVal) {
      if (instance != null) {
         float instanceValue = instance.scaleSetting.getMinValue() instanceof Float local ? local : 0.6F;
         float instanceValue2 = instance.scaleSetting.getMaxValue() instanceof Float local2 ? local2 : 2.0F;
         instance.scaleSetting.setValue(Math.max(instanceValue, Math.min(instanceValue2, floatVal)));
         int[] hudModuleValue = HudModule.getSavedElementPos(HudElementType.SPOTIFY_HUD);
         int[] hudModuleValue2 = HudModule.call4(HudElementType.SPOTIFY_HUD, hudModuleValue[0], hudModuleValue[1]);
         if (hudModuleValue2[0] != hudModuleValue[0] || hudModuleValue2[1] != hudModuleValue[1]) {
            HudModule.call3(HudElementType.SPOTIFY_HUD, hudModuleValue2[0], hudModuleValue2[1]);
         }
      }
   }

   public static int getSpotifyPanelWidth() {
      return Math.round(270.0F * getScale());
   }

   public static int getSpotifyPanelHeight() {

      return Math.round(64.0F * getScale());
   }

   private static int scalePx(float floatVal) {
      return Math.round(floatVal * getScale());
   }

   private static String truncateToWidth(String string, int intVal) {
      if (string == null) {
         return "";
      }

      if (HudModule.instance != null && HudModule.measureTextWidth(string) > intVal && intVal > 10) {
         String var0Snapshot = string;

         while (var0Snapshot.length() > 1 && HudModule.measureTextWidth(var0Snapshot + "…") > intVal) {
            var0Snapshot = var0Snapshot.substring(0, var0Snapshot.length() - 1);
         }

         return var0Snapshot + "…";
      } else {
         return string;
      }
   }

   private static String formatElapsed(long longVal) {
      long maxValue = Math.max(0L, longVal / 1000L);
      return maxValue / 60L + ":" + String.format("%02d", maxValue % 60L);
   }

   private static void refreshArtwork(SpotifyTrackState spotifyTrackState) {
      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc == null) {
            return;
         }

         if (spotifyTrackState.artVersion() == instance.artVersion2 && instance.hasArtwork) {
            File spotifyBridgeServiceValue = SpotifyBridgeService.getArtFilePath() != null ? SpotifyBridgeService.getArtFilePath().toFile() : null;
            if (spotifyBridgeServiceValue == null || !spotifyBridgeServiceValue.isFile() || spotifyBridgeServiceValue.lastModified() == instance.artLastModified) {
               return;
            }
         }

         File spotifyBridgeServiceValue2 = SpotifyBridgeService.getArtFilePath() != null ? SpotifyBridgeService.getArtFilePath().toFile() : null;
         if (spotifyBridgeServiceValue2 == null || !spotifyBridgeServiceValue2.isFile()) {
            instance.hasArtwork = false;
            return;
         }

         try (FileInputStream local = new FileInputStream(spotifyBridgeServiceValue2)) {
            NativeImage class1011Value = NativeImage.read(local);
            if (class1011Value != null) {
               NativeImageBackedTexture local2 = new NativeImageBackedTexture(() -> {
                  return "spotify_art";
               }, class1011Value);

               try {
                  mc.getTextureManager().destroyTexture(artTexture);
               } catch (Throwable error) {
               }

               mc.getTextureManager().registerTexture(artTexture, local2);
               instance.hasArtwork = true;
               instance.artVersion2 = spotifyTrackState.artVersion();
               instance.artLastModified = spotifyBridgeServiceValue2.lastModified();
               return;
            }

            instance.hasArtwork = false;
         }

         return;
      } catch (Throwable error2) {
         try {
            instance.hasArtwork = false;
         } catch (Throwable error3) {
         }
      }
   }

   public static void renderSpotifyHud(DrawContext arg) {
      if (instance != null && instance.isEnabled()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.player != null && !(mc.currentScreen instanceof ClickGuiScreen) && !mc.getDebugHud().shouldShowDebugHud()) {
            SpotifyTrackState spotifyBridgeServiceValue = SpotifyBridgeService.getTrackState();
            if (!spotifyBridgeServiceValue.active()) {
               try {
                  if ((Boolean)instance.hideWhenIdleSetting.getValue()) {
                     return;
                  }
               } catch (Throwable error) {
               }
            }

            if (spotifyBridgeServiceValue.active()) {
               refreshArtwork(spotifyBridgeServiceValue);
            } else {
               instance.hasArtwork = false;
            }

            int[] hudModuleValue = HudModule.getSavedElementPos(HudElementType.SPOTIFY_HUD);
            int intVal = hudModuleValue[0];
            int intVal2 = hudModuleValue[1];
            int intVal3 = getSpotifyPanelWidth();
            int intVal4 = getSpotifyPanelHeight();
            panelRect = new int[]{intVal, intVal2, intVal3, intVal4};
            isSeekBarActive = false;
            Color colorInst = new Color(12, 12, 16, 255);

            try {
               if (HudModule.instance != null) {
                  float floatVal = (Float)HudModule.instance.opacitySetting.getValue();
                  Color clickGuiModuleValue = ClickGuiModule.getBackgroundColor();
                  colorInst = new Color(clickGuiModuleValue.getRed(), clickGuiModuleValue.getGreen(), clickGuiModuleValue.getBlue(), (int)(floatVal * 255.0F));
               }
            } catch (Throwable error2) {
            }

            int clickGuiModuleValue2 = ClickGuiModule.getAccentColorArgb();
            int hudModuleValue2 = HudModule.getFontColor();
            int colorInst2 = new Color(150, 150, 168).getRGB();
            HudModule.queueHudPanel(arg, intVal, intVal2, intVal3, intVal4, 2.0F, colorInst.getRGB());
            int var4ScalePx8Value = intVal + scalePx(8.0F);
            int var5ScalePx10Value = intVal2 + scalePx(10.0F);
            int intVal5 = scalePx(44.0F);
            if (spotifyBridgeServiceValue.active() && instance.hasArtwork) {
               try {
                  GuiRenderUtil.drawTexture(arg, var4ScalePx8Value, var5ScalePx10Value, intVal5, artTexture, -1, 1.0F, false);
               } catch (Throwable error3) {
                  GuiRenderUtil.fillRoundedRect(arg, var4ScalePx8Value, var5ScalePx10Value, intVal5, intVal5, 2.0F, clickGuiModuleValue2, false);
               }
            } else {
               GuiRenderUtil.fillRoundedRect(arg, var4ScalePx8Value, var5ScalePx10Value, intVal5, intVal5, 2.0F, clickGuiModuleValue2, false);
               float var12Var142Value = var4ScalePx8Value + intVal5 / 2.0F;
               float var13Var142Value = var5ScalePx10Value + intVal5 / 2.0F;
               float var140Value = intVal5 * 0.32F;
               GuiRenderUtil.renderArcRaw(arg, var12Var142Value - var140Value, var13Var142Value - var140Value, var140Value * 2.0F, var140Value * 2.0F, 360.0F, 0.0F, new Color(20, 20, 26, 255).getRGB(), false);
               float var140Value2 = intVal5 * 0.12F;
               GuiRenderUtil.renderArcRaw(arg, var12Var142Value - var140Value2, var13Var142Value - var140Value2, var140Value2 * 2.0F, var140Value2 * 2.0F, 360.0F, 0.0F, clickGuiModuleValue2, false);
            }

            int var4ScalePx60Value = intVal + scalePx(60.0F);
            boolean falseSnapshot = false;

            try {
               falseSnapshot = spotifyBridgeServiceValue.active() && (Boolean)instance.showControlsSetting.getValue();
            } catch (Throwable error4) {
            }

            int var6ScalePx60Value = intVal3 - scalePx(60.0F) - (falseSnapshot ? scalePx(70.0F) : scalePx(8.0F));
            if (!spotifyBridgeServiceValue.active()) {
               String local = "Nothing playing";
               HudModule.drawTextAt(arg, local, intVal + (intVal3 - HudModule.measureTextWidth(local)) / 2, intVal2 + (intVal4 - 11) / 2, colorInst2);
               return;
            }

            String local2 = truncateToWidth(spotifyBridgeServiceValue.title(), var6ScalePx60Value);
            String local3 = truncateToWidth(spotifyBridgeServiceValue.artist(), var6ScalePx60Value);
            HudModule.drawTextAt(arg, local2, var4ScalePx60Value, intVal2 + scalePx(6.0F), hudModuleValue2);
            HudModule.drawTextAt(arg, local3, var4ScalePx60Value, intVal2 + scalePx(22.0F), colorInst2);
            if (falseSnapshot) {
               int intVal6 = scalePx(18.0F);
               int intVal7 = scalePx(4.0F);
               int intVal8 = scalePx(8.0F);
               int var5ScalePx6Value = intVal2 + scalePx(6.0F);
               int var4Var6Var22Var20Value = intVal + intVal3 - intVal8 - intVal6;
               int var4Var6Var223Var202Var21V = intVal + intVal3 - intVal8 - 3 * intVal6 - 2 * intVal7;
               int var4Var6Var222Var20Var21Va = intVal + intVal3 - intVal8 - 2 * intVal6 - intVal7;
               prevButtonRect = new int[]{var4Var6Var223Var202Var21V, var5ScalePx6Value, intVal6, intVal6};
               playPauseButtonRect = new int[]{var4Var6Var222Var20Var21Va, var5ScalePx6Value, intVal6, intVal6};
               nextButtonRect = new int[]{var4Var6Var22Var20Value, var5ScalePx6Value, intVal6, intVal6};
               GuiRenderUtil.drawTexture(arg, var4Var6Var223Var202Var21V, var5ScalePx6Value, intVal6, prevIcon, colorInst2, 1.0F, false);
               GuiRenderUtil.drawTexture(arg, var4Var6Var22Var20Value, var5ScalePx6Value, intVal6, nextIcon, colorInst2, 1.0F, false);
               if (spotifyBridgeServiceValue.playing()) {
                  GuiRenderUtil.drawTexture(arg, var4Var6Var222Var20Var21Va, var5ScalePx6Value, intVal6, pauseIcon, -1, 1.0F, false);
               } else {
                  GuiRenderUtil.strokeRoundedRect(arg, var4Var6Var222Var20Var21Va, var5ScalePx6Value, intVal6, intVal6, intVal6 / 2.0F, 1.4F, clickGuiModuleValue2, false);
                  GuiRenderUtil.drawTexture(arg, var4Var6Var222Var20Var21Va, var5ScalePx6Value, intVal6, playIcon, clickGuiModuleValue2, 1.0F, false);
               }
            } else {
               prevButtonRect = new int[]{0, 0, 0, 0};
               playPauseButtonRect = new int[]{0, 0, 0, 0};
               nextButtonRect = new int[]{0, 0, 0, 0};
            }

            long longVal = spotifyBridgeServiceValue.durMs();
            long longVal2 = spotifyBridgeServiceValue.livePosMs();
            float floatVal2 = longVal > 0L ? Math.max(0.0F, Math.min(1.0F, (float)longVal2 / (float)longVal)) : 0.0F;
            int var40Snapshot = var4ScalePx60Value;
            int var6ScalePx60Value2 = intVal3 - scalePx(60.0F) - scalePx(8.0F);
            int var5ScalePx44Value = intVal2 + scalePx(44.0F);
            int maxValue = Math.max(2, scalePx(3.0F));
            int colorInst3 = new Color(255, 255, 255, 60).getRGB();
            GuiRenderUtil.fillRoundedRect(arg, var40Snapshot, var5ScalePx44Value, var6ScalePx60Value2, maxValue, 1.5F, colorInst3, false);
            int maxValue2 = Math.max(maxValue, (int)(var6ScalePx60Value2 * floatVal2));
            GuiRenderUtil.fillRoundedRect(arg, var40Snapshot, var5ScalePx44Value, maxValue2, maxValue, 1.5F, clickGuiModuleValue2, false);
            float maxValue3 = Math.max(5.0F, scalePx(5.0F));
            float var48Var30Value = var40Snapshot + maxValue2;
            float var27Var282Value = var5ScalePx44Value + maxValue / 2.0F;
            GuiRenderUtil.drawTexture(arg, var48Var30Value - maxValue3 / 2.0F, var27Var282Value - maxValue3 / 2.0F, maxValue3, progressDotIcon, -1, 1.0F, false);
            progressBarRect = new int[]{var40Snapshot, var5ScalePx44Value - 3, var6ScalePx60Value2, maxValue + 6};
            isSeekBarActive = spotifyBridgeServiceValue.canSeek() && longVal > 0L;
         }
      }
   }

   private static boolean isInsideRect(double doubleVal, double doubleVal2, int[] int2) {

      return int2[2] > 0 && int2[3] > 0 && doubleVal >= int2[0] && doubleVal <= int2[0] + int2[2] && doubleVal2 >= int2[1] && doubleVal2 <= int2[1] + int2[3];
   }

   public static boolean handleMouseClick(double doubleVal, double doubleVal2) {
      try {
         if (instance != null && instance.isEnabled() && !HudLayoutManager.isLayoutEditing && !HudLayoutManager.INSTANCE5.isInteractionActive()) {
            SpotifyTrackState spotifyBridgeServiceValue = SpotifyBridgeService.getTrackState();
            if (!spotifyBridgeServiceValue.active()) {
               return false;
            }

            if (isInsideRect(doubleVal, doubleVal2, prevButtonRect)) {
               SpotifyBridgeService.previousTrack();
               return true;
            }

            if (isInsideRect(doubleVal, doubleVal2, playPauseButtonRect)) {
               SpotifyBridgeService.togglePlayPause();
               return true;
            }

            if (isInsideRect(doubleVal, doubleVal2, nextButtonRect)) {
               SpotifyBridgeService.nextTrack();
               return true;
            }

            if (isSeekBarActive && isInsideRect(doubleVal, doubleVal2, progressBarRect) && spotifyBridgeServiceValue.durMs() > 0L) {
               float floatVal = (float)((doubleVal - progressBarRect[0]) / progressBarRect[2]);
               floatVal = Math.max(0.0F, Math.min(1.0F, floatVal));
               SpotifyBridgeService.seekTo((long)(floatVal * (float)spotifyBridgeServiceValue.durMs()));
               return true;
            }
         }
      } catch (Throwable error) {
      }

      return false;
   }

}
