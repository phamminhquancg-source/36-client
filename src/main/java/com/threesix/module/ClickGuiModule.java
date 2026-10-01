package com.threesix.module;

import java.awt.Color;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ClientFontType;
import com.threesix.manager.CustomFontManager;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.setting.ModeSetting02;
import com.threesix.data.ModuleCategory;
import com.threesix.gui.ClickGuiScreen;

public final class ClickGuiModule extends ModuleBase {
   public static ClickGuiModule INSTANCE2;
   public final ClientSetting colorSetting = new ClientSetting("Color", new Color(255, 200, 0, 255));
   public final ClientSetting backgroundColorSetting = new ClientSetting("Background", new Color(12, 12, 16, 255));
   public final ClientSetting fontColorSetting = new ClientSetting("Font Color", new Color(255, 255, 255, 255));
   public final ClientSetting animationsSetting = new ClientSetting("Animations", true);
   public final ClientSetting blurSetting = new ClientSetting("Blur", true);
   public final ClientSetting tracerWidthSetting = new ClientSetting("Tracer Width", 2.0, 1.0, 6.0);
   public final ClientSetting glassIntensitySetting = new ClientSetting("Glass Intensity", 60, 0, 100);
   public final ModeSetting02 rowStyleSetting = new ModeSetting02("Row Style", "Filled", "Filled", "Minimal", "Outlined");
   public final ModeSetting02 panelShapeSetting = new ModeSetting02("Panel Shape", "Rounded", "Rounded", "Sharp", "Pill");
   public final ModeSetting02 animSpeedSetting = new ModeSetting02("Anim Speed", "Normal", "Slow", "Normal", "Fast", "Off");
   public final ClientSetting guiKeySetting = new ClientSetting("GUI Key", "RShift");
   public final ModeSetting02 menuSizeSetting = new ModeSetting02("Menu Size", "3", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
   public static volatile int menuKeyCode = 344;
   public static volatile boolean pendingKeyRebind = false;

   public ClickGuiModule() {
      super("36Client", ModuleCategory.CLIENT);
      this.registerSetting(this.colorSetting);
      this.registerSetting(this.backgroundColorSetting);
      this.registerSetting(this.fontColorSetting);
      this.registerSetting(this.glassIntensitySetting);
      this.registerSetting(this.rowStyleSetting);
      this.registerSetting(this.panelShapeSetting);
      this.registerSetting(this.animSpeedSetting);
      this.registerSetting(this.animationsSetting);
      this.registerSetting(this.blurSetting);
      this.registerSetting(this.guiKeySetting);
      INSTANCE2 = this;

      try {
         CustomFontManager.setFontType(ClientFontType.XUONG);
      } catch (Throwable error) {
      }
   }

   @Override
   public void onTick() {
      if (INSTANCE2 != null) {
         ClientFontType clientFontTypeValue = ClientFontType.XUONG;
         if (CustomFontManager.getFontType() != clientFontTypeValue) {
            CustomFontManager.setFontType(clientFontTypeValue);
         }

         menuKeyCode = keyNameToCode((String)this.guiKeySetting.getValue());
      }
   }

   public static void setMenuKey(int intVal, String string) {
      if (INSTANCE2 != null) {
         INSTANCE2.guiKeySetting.setValue(string);
         menuKeyCode = intVal;
         pendingKeyRebind = false;
      }
   }

   public static String getMenuKeyName() {
      if (INSTANCE2 == null) {
         return "RShift";
      }

      String local = (String)INSTANCE2.guiKeySetting.getValue();
      return local != null && !local.isBlank() ? local : "RShift";
   }

   public static String getRowStyle() {

      String local = INSTANCE2 == null ? "Filled" : (String)INSTANCE2.rowStyleSetting.getValue();
      return local;
   }

   public static float getPanelCornerRadius() {
      if (INSTANCE2 == null) {
         return 3.0F;
      }

      String local = (String)INSTANCE2.panelShapeSetting.getValue();

      return switch (local) {
         case "Sharp" -> 0.0F;
         case "Pill" -> 20.0F;
         default -> getBaseCornerRadius();
      };
   }

   public static float getAnimationSpeedScale() {
      if (INSTANCE2 == null) {
         return 1.0F;
      }

      String local = (String)INSTANCE2.animSpeedSetting.getValue();

      return switch (local) {
         case "Slow" -> 0.4F;
         case "Fast" -> 2.5F;
         case "Off" -> 999.0F;
         default -> 1.0F;
      };
   }

   public static boolean isAnimationEnabled() {
      return !isAnimationsSettingOn() ? false : !"Off".equals(INSTANCE2 == null ? "Normal" : INSTANCE2.animSpeedSetting.getValue());
   }

   public static int getMenuKeyCode() {

      return menuKeyCode > 0 ? menuKeyCode : 344;
   }

   public static boolean isStateChangeNotifyEnabled() {
      return false;
   }

   public static float getBaseCornerRadius() {
      return 3.0F;
   }

   public static float getGlassIntensityFactor() {

      if (INSTANCE2 == null) {
         return 0.6F;
      }

      int intVal = (Integer)INSTANCE2.glassIntensitySetting.getValue();
      return Math.max(0, Math.min(100, intVal)) / 100.0F;
   }

   public static int getAccentColorArgb() {
      Color local = getAccentColor();
      return local.getAlpha() << 24 | local.getRed() << 16 | local.getGreen() << 8 | local.getBlue();
   }

   public static Color getAccentColor() {
      Color local = INSTANCE2 == null ? new Color(255, 0, 200, 255) : (Color)INSTANCE2.colorSetting.getValue();
      return local;
   }

   public static int getBackgroundColorArgb() {
      Color local = getBackgroundColor();
      float floatVal = getGlassIntensityFactor();
      int intVal = local.getAlpha();
      int intVal2 = (int)(intVal * (1.0F - floatVal * 0.82F));
      intVal2 = Math.max(15, Math.min(255, intVal2));
      return intVal2 << 24 | local.getRed() << 16 | local.getGreen() << 8 | local.getBlue();
   }

   public static Color getBackgroundColor() {
      if (INSTANCE2 == null) {
         return new Color(30, 15, 40, 255);
      } else {
         Color local = (Color)INSTANCE2.backgroundColorSetting.getValue();
         if (local == null) {
            return new Color(30, 15, 40, 255);
         } else {
            return local.getAlpha() < 200 ? new Color(local.getRed(), local.getGreen(), local.getBlue(), 255) : local;
         }
      }
   }

   public static boolean isAnimationsSettingOn() {
      return INSTANCE2 == null ? true : (Boolean)INSTANCE2.animationsSetting.getValue();
   }

   public static int getMenuSize() {
      if (INSTANCE2 == null) {
         return 5;
      }

      try {
         return Integer.parseInt((String)INSTANCE2.menuSizeSetting.getValue());
      } catch (Exception error) {
         return 5;
      }
   }

   public static float getTracerWidth() {

      if (INSTANCE2 == null) {
         return 2.0F;
      }

      double local = 2.0;

      try {
         local = (Double)INSTANCE2.tracerWidthSetting.getValue();
      } catch (Exception error) {
      }

      if (Double.isNaN(local) || Double.isInfinite(local)) {
         local = 1.0;
      }

      local = Math.max(1.0, Math.min(6.0, local));
      return (float)local;
   }

   public static int keyNameToCode(String string) {

      if (string != null && !string.isBlank()) {
         for (int index = 0; index <= 348; index++) {
            if (ClickGuiScreen.bY(index).equalsIgnoreCase(string)) {
               return index;
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   public static String getDefaultKey2() {

      return "I";
   }

}
