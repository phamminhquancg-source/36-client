package com.threesix.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.module.ConfigShareModule;
import com.threesix.manager.CustomFontManager;
import com.threesix.util.StringVaultDecoder;
import com.threesix.data.ConfigsScreenTab;

public class ConfigManagerScreen extends Screen {
   public static final int W = 340;
   public static final int H = 380;
   public static final int PAD = 14;
   public static final int ROW_H = 42;
   public static final int ROWS = 4;
   public static final int MAX_CFG = 5;
   public static int BG;
   public static int SURFACE;
   public static int SURFACE2;
   public static int BORDER;
   public static int ACCENT;
   public static int ACCENT2;
   public static final int TEXT = -1117441;
   public static final int TEXT2 = -7824982;
   public static final int TEXT3 = -12298906;
   public static final int GREEN = -14498466;
   public static final int GREEN_BG = -15914984;
   public static final int RED = -1096636;
   public static final int RED_BG = -13824498;
   public static final int AMBER = -680437;
   public static final int AMBER_BG = -13821184;
   public static int SEL_BG;
   public static int SEL_BD;
   public ConfigsScreenTab tab = ConfigsScreenTab.CONFIGS;
   public final List configs = new ArrayList();
   public int sel = 0;
   public int scroll = 0;
   public boolean newOpen = false;
   public boolean importOpen = false;
   public String newName = "";
   public String importCode = "";
   public String status = "";
   public int statusCol = -7824982;
   public long statusAt = 0L;
   public int px;
   public int py;

   public static void dT() {
      int clickGuiModuleValue = ClickGuiModule.getBackgroundColorArgb();
      int clickGuiModuleValue2 = ClickGuiModule.getAccentColorArgb();
      BG = dU(clickGuiModuleValue, 0.85F);
      SURFACE = dU(clickGuiModuleValue, 0.95F);
      SURFACE2 = clickGuiModuleValue;
      BORDER = dV(clickGuiModuleValue, clickGuiModuleValue2, 0.15F);
      ACCENT = clickGuiModuleValue2;
      ACCENT2 = dU(clickGuiModuleValue2, 0.75F);
      SEL_BG = dV(clickGuiModuleValue, clickGuiModuleValue2, 0.2F);
      SEL_BD = dV(clickGuiModuleValue, clickGuiModuleValue2, 0.55F);
   }

   public static int dU(int intVal, float floatVal) {

      int maxValue = Math.max(0, (int)((intVal >> 16 & 0xFF) * floatVal));
      int maxValue2 = Math.max(0, (int)((intVal >> 8 & 0xFF) * floatVal));
      int maxValue3 = Math.max(0, (int)((intVal & 0xFF) * floatVal));
      return intVal & 0xFF000000 | maxValue << 16 | maxValue2 << 8 | maxValue3;
   }

   public static int dV(int intVal, int intVal2, float floatVal) {
      int intVal3 = intVal >> 16 & 0xFF;
      int intVal4 = intVal >> 8 & 0xFF;
      int intVal5 = intVal & 0xFF;
      int intVal6 = intVal2 >> 16 & 0xFF;
      int intVal7 = intVal2 >> 8 & 0xFF;
      int intVal8 = intVal2 & 0xFF;
      return 0xFF000000 | (int)(intVal3 + (intVal6 - intVal3) * floatVal) << 16 | (int)(intVal4 + (intVal7 - intVal4) * floatVal) << 8 | (int)(intVal5 + (intVal8 - intVal5) * floatVal);
   }

   public static float dW() {
      float clickGuiModuleValue = ClickGuiModule.getBaseCornerRadius() + 6.0F;
      return clickGuiModuleValue;
   }

   public static float dX() {
      return Math.max(4.0F, ClickGuiModule.getBaseCornerRadius());
   }

   public void dY(DrawContext arg, String string, int intVal, int intVal2, int intVal3) {
      CustomFontManager.INSTANCE7.drawText(arg, string, intVal, intVal2, intVal3);
   }

   public void dZ(DrawContext arg, String string, int intVal, int intVal2, int intVal3) {
      int customFontManagerValue = CustomFontManager.INSTANCE7.getStringWidth(string);
      CustomFontManager.INSTANCE7.drawText(arg, string, intVal - customFontManagerValue / 2, intVal2, intVal3);
   }

   public int ea(String string) {
      return CustomFontManager.INSTANCE7.getStringWidth(string);
   }

   public ConfigManagerScreen() {
      super(Text.literal("Threesix Configs"));
   }

   public void init() {
      this.px = (this.width - 340) / 2;
      this.py = (this.height - 380) / 2;
      this.eb();
   }

   public void render(DrawContext arg, int intVal, int intVal2, float floatVal) {
      dT();
      ConfigManager.INSTANCE.getModules().stream().filter(item -> item instanceof ClickGuiModule).findFirst().ifPresent(item -> ((ClickGuiModule)item).onTick());
      CustomFontManager.clearTextYOffset();
      GuiRenderUtil.fillRoundedRect(arg, this.px, this.py, 340.0F, 380.0F, dW(), BG, false);
      GuiRenderUtil.strokeRoundedRect(arg, this.px, this.py, 340.0F, 380.0F, dW(), 1.0F, BORDER, false);
      this.ee(arg, intVal, intVal2);
      this.ef(arg, intVal, intVal2);
      this.eg(arg, intVal, intVal2);
      this.ei(arg, intVal, intVal2);
      if (!this.status.isEmpty() && System.currentTimeMillis() - this.statusAt < 3000L) {
         int intVal3 = this.ea(this.status) + 24;
         int intVal4 = this.px + 170 - intVal3 / 2;
         int intVal5 = this.py + 380 - 36;
         GuiRenderUtil.fillRoundedRect(arg, intVal4, intVal5, intVal3, 22.0F, 6.0F, -871756268, false);
         GuiRenderUtil.strokeRoundedRect(arg, intVal4, intVal5, intVal3, 22.0F, 6.0F, 1.0F, this.statusCol & 1442840575, false);
         this.dZ(arg, this.status, this.px + 170, intVal5 + 7, this.statusCol);
      }

      if (this.newOpen) {
         this.ej(arg, intVal, intVal2);
      }

      if (this.importOpen) {
         this.ek(arg, intVal, intVal2);
      }
   }

   public void ee(DrawContext arg, int intVal, int intVal2) {
      int intVal3 = this.py + 18;
      this.dY(arg, "THREESIX CONFIGS", this.px + 14, intVal3, -1117441);
      String local = this.configs.size() + " / 5";
      int intVal4 = this.ea(local) + 16;
      GuiRenderUtil.fillRoundedRect(arg, this.px + 340 - 14 - intVal4, intVal3 - 4, intVal4, 20.0F, 5.0F, SURFACE2, false);
      this.dZ(arg, local, this.px + 340 - 14 - intVal4 / 2, intVal3, -7824982);
      boolean flag = this.el(intVal, intVal2, this.px + 340 - 28, intVal3 - 4, 20, 20);
      this.dY(arg, "X", this.px + 340 - 22, intVal3, flag ? -1117441 : -7824982);
      arg.fill(this.px + 14, this.py + 46, this.px + 340 - 14, this.py + 47, BORDER);
   }

   public void ef(DrawContext arg, int intVal, int intVal2) {
      int intVal3 = this.py + 56;
      String local = "MY CONFIGS";
      int intVal4 = this.px + 14;
      GuiRenderUtil.fillRoundedRect(arg, intVal4 - 6, intVal3 - 4, this.ea(local) + 12, 24.0F, 5.0F, SURFACE2, false);
      this.dY(arg, local, intVal4, intVal3 + 3, -1117441);
      GuiRenderUtil.fillRoundedRect(arg, intVal4, intVal3 + 22, this.ea(local), 3.0F, 2.0F, ACCENT, false);
      arg.fill(this.px + 14, intVal3 + 28, this.px + 340 - 14, intVal3 + 29, BORDER);
   }

   public void eg(DrawContext arg, int intVal, int intVal2) {
      int intVal3 = this.py + 100;
      short shortVal = 168;
      if (this.configs.isEmpty()) {
         this.dZ(arg, "No configs yet — create one below", this.px + 170, intVal3 + shortVal / 2 - 6, -12298906);
      } else {
         for (int index = 0; index < 4; index++) {
            int var6ThisValue = index + this.scroll;
            if (var6ThisValue >= this.configs.size()) {
               if (var6ThisValue == this.configs.size() && this.configs.size() < 5) {
                  int var4Var6424Value = intVal3 + index * 42 + 4;
                  GuiRenderUtil.fillRoundedRect(arg, this.px + 14, var4Var6424Value, 312.0F, 34.0F, dX(), 587202559, false);
                  GuiRenderUtil.strokeRoundedRect(arg, this.px + 14, var4Var6424Value, 312.0F, 34.0F, dX(), 1.0F, 587202559, false);
                  this.dZ(arg, "+ new config slot", this.px + 170, var4Var6424Value + 17 - 5, -12298906);
               }
            } else {
               int var4Var6424Value2 = intVal3 + index * 42 + 4;
               boolean flag = var6ThisValue == this.sel;
               boolean flag2 = this.el(intVal, intVal2, this.px + 14, var4Var6424Value2, 312, 34);
               int intVal4 = flag ? SEL_BG : (flag2 ? SURFACE2 : SURFACE);
               int intVal5 = flag ? SEL_BD : BORDER;
               GuiRenderUtil.fillRoundedRect(arg, this.px + 14, var4Var6424Value2, 312.0F, 34.0F, dX(), intVal4, false);
               GuiRenderUtil.strokeRoundedRect(arg, this.px + 14, var4Var6424Value2, 312.0F, 34.0F, dX(), 1.0F, intVal5, false);
               if (flag) {
                  GuiRenderUtil.fillRoundedRect(arg, this.px + 14, var4Var6424Value2 + 8, 3.0F, 18.0F, 2.0F, ACCENT, false);
               }

               this.dY(arg, (String)this.configs.get(var6ThisValue), this.px + 14 + (flag ? 14 : 10), var4Var6424Value2 + 17 - 5, flag ? -1117441 : -7824982);
               int var221710Value = var4Var6424Value2 + 17 - 10;
               int intVal6 = this.px + 340 - 14 - 60;
               int var1465Value = intVal6 - 65;
               int var1565Value = var1465Value - 65;
               int var1665Value = var1565Value - 65;
               boolean flag3 = this.el(intVal, intVal2, var1465Value, var221710Value, 60, 20);
               GuiRenderUtil.fillRoundedRect(arg, var1465Value, var221710Value, 60.0F, 20.0F, 5.0F, flag3 ? -15914984 : SURFACE, false);
               GuiRenderUtil.strokeRoundedRect(arg, var1465Value, var221710Value, 60.0F, 20.0F, 5.0F, 1.0F, flag3 ? -14498466 : 872415231, false);
               this.dZ(arg, "Reload", var1465Value + 30, var221710Value + 5, -14498466);
               boolean flag4 = this.el(intVal, intVal2, intVal6, var221710Value, 60, 20);
               GuiRenderUtil.fillRoundedRect(arg, intVal6, var221710Value, 60.0F, 20.0F, 5.0F, flag4 ? -13824498 : SURFACE, false);
               GuiRenderUtil.strokeRoundedRect(arg, intVal6, var221710Value, 60.0F, 20.0F, 5.0F, 1.0F, flag4 ? -1096636 : 872415231, false);
               this.dZ(arg, "Delete", intVal6 + 30, var221710Value + 5, -1096636);
               boolean flag5 = this.el(intVal, intVal2, var1565Value, var221710Value, 60, 20);
               GuiRenderUtil.fillRoundedRect(arg, var1565Value, var221710Value, 60.0F, 20.0F, 5.0F, flag5 ? -15914984 : SURFACE, false);
               GuiRenderUtil.strokeRoundedRect(arg, var1565Value, var221710Value, 60.0F, 20.0F, 5.0F, 1.0F, flag5 ? -14498466 : 872415231, false);
               this.dZ(arg, "Save", var1565Value + 30, var221710Value + 5, -14498466);
               boolean flag6 = this.el(intVal, intVal2, var1665Value, var221710Value, 60, 20);
               GuiRenderUtil.fillRoundedRect(arg, var1665Value, var221710Value, 60.0F, 20.0F, 5.0F, flag6 ? -680437 : SURFACE, false);
               GuiRenderUtil.strokeRoundedRect(arg, var1665Value, var221710Value, 60.0F, 20.0F, 5.0F, 1.0F, flag6 ? -680437 : 872415231, false);
               this.dZ(arg, "Share", var1665Value + 30, var221710Value + 5, -680437);
            }
         }
      }
   }

   public void ei(DrawContext arg, int intVal, int intVal2) {
      int intVal3 = this.py + 380 - 54;
      arg.fill(this.px + 14, intVal3, this.px + 340 - 14, intVal3 + 1, BORDER);
      intVal3 += 14;
      boolean flag = this.configs.size() >= 5;
      boolean flag2 = !flag && this.el(intVal, intVal2, this.px + 14, intVal3, 110, 18);
      this.dY(arg, "+ New config", this.px + 14, intVal3 + 2, flag2 ? ACCENT : (flag ? -12298906 : -7824982));
      byte byteVal = 90;
      int intVal4 = this.px + 340 - 14 - byteVal;
      boolean flag3 = this.el(intVal, intVal2, intVal4, intVal3 - 4, byteVal, 26);
      GuiRenderUtil.fillRoundedRect(arg, intVal4, intVal3 - 4, byteVal, 26.0F, 6.0F, flag3 ? -13821184 : SURFACE, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal4, intVal3 - 4, byteVal, 26.0F, 6.0F, 1.0F, flag3 ? -680437 : BORDER, false);
      this.dZ(arg, "Reset all", intVal4 + byteVal / 2, intVal3 + 2, -680437);
      int var88Var7Value = intVal4 - 8 - byteVal;
      boolean flag4 = this.el(intVal, intVal2, var88Var7Value, intVal3 - 4, byteVal, 26);
      GuiRenderUtil.fillRoundedRect(arg, var88Var7Value, intVal3 - 4, byteVal, 26.0F, 6.0F, flag4 ? -15914984 : SURFACE, false);
      GuiRenderUtil.strokeRoundedRect(arg, var88Var7Value, intVal3 - 4, byteVal, 26.0F, 6.0F, 1.0F, flag4 ? -14498466 : BORDER, false);
      this.dZ(arg, "Import", var88Var7Value + byteVal / 2, intVal3 + 2, -14498466);
   }

   public void ej(DrawContext arg, int intVal, int intVal2) {

      short shortVal = 340;
      short shortVal2 = 130;
      int intVal3 = this.px + 170 - shortVal / 2;
      int intVal4 = this.py + 190 - shortVal2 / 2;
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, shortVal, shortVal2, 12.0F, BG, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal3, intVal4, shortVal, shortVal2, 12.0F, 1.0F, BORDER, false);
      this.dZ(arg, "New Config Name", intVal3 + shortVal / 2, intVal4 + 14, -1117441);
      GuiRenderUtil.fillRoundedRect(arg, intVal3 + 16, intVal4 + 40, shortVal - 32, 30.0F, 6.0F, SURFACE2, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal3 + 16, intVal4 + 40, shortVal - 32, 30.0F, 6.0F, 1.0F, this.newName.isEmpty() ? BORDER : ACCENT, false);
      String local = this.newName.isEmpty() ? "Enter name..." : this.newName;
      this.dY(arg, local, intVal3 + 26, intVal4 + 50, this.newName.isEmpty() ? -12298906 : -1117441);
      int var6Var4255Value = intVal3 + shortVal / 2 - 55;
      byte byteVal = 110;
      boolean flag = this.el(intVal, intVal2, var6Var4255Value, intVal4 + 88, byteVal, 28);
      GuiRenderUtil.fillRoundedRect(arg, var6Var4255Value, intVal4 + 88, byteVal, 28.0F, 7.0F, flag ? ACCENT2 : SURFACE2, false);
      GuiRenderUtil.strokeRoundedRect(arg, var6Var4255Value, intVal4 + 88, byteVal, 28.0F, 7.0F, 1.0F, flag ? ACCENT : BORDER, false);
      this.dZ(arg, "Create", var6Var4255Value + byteVal / 2, intVal4 + 97, -1117441);
   }

   public void ek(DrawContext arg, int intVal, int intVal2) {

      short shortVal = 380;
      short shortVal2 = 130;
      int intVal3 = this.px + 170 - shortVal / 2;
      int intVal4 = this.py + 190 - shortVal2 / 2;
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, shortVal, shortVal2, 12.0F, BG, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal3, intVal4, shortVal, shortVal2, 12.0F, 1.0F, BORDER, false);
      this.dZ(arg, "Paste Config Content", intVal3 + shortVal / 2, intVal4 + 14, -1117441);
      GuiRenderUtil.fillRoundedRect(arg, intVal3 + 16, intVal4 + 40, shortVal - 32, 30.0F, 6.0F, SURFACE2, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal3 + 16, intVal4 + 40, shortVal - 32, 30.0F, 6.0F, 1.0F, this.importCode.isEmpty() ? BORDER : ACCENT, false);
      String local;
      if (this.importCode.isEmpty()) {
         local = "Paste content here (Ctrl+V)...";
      } else if (this.importCode.length() > 44) {
         local = this.importCode.substring(0, 44) + "... (" + this.importCode.length() + " chars)";
      } else {
         local = this.importCode;
      }

      this.dY(arg, local, intVal3 + 26, intVal4 + 50, this.importCode.isEmpty() ? -12298906 : -1117441);
      int var6Var4255Value = intVal3 + shortVal / 2 - 55;
      byte byteVal = 110;
      boolean flag = this.el(intVal, intVal2, var6Var4255Value, intVal4 + 88, byteVal, 28);
      GuiRenderUtil.fillRoundedRect(arg, var6Var4255Value, intVal4 + 88, byteVal, 28.0F, 7.0F, flag ? ACCENT2 : SURFACE2, false);
      GuiRenderUtil.strokeRoundedRect(arg, var6Var4255Value, intVal4 + 88, byteVal, 28.0F, 7.0F, 1.0F, flag ? ACCENT : BORDER, false);
      this.dZ(arg, "Import", var6Var4255Value + byteVal / 2, intVal4 + 97, -1117441);
   }

   public boolean mouseClicked(Click arg, boolean flag) {
      int intVal = (int)arg.x();
      int intVal2 = (int)arg.y();
      int pxSnapshot = this.px;
      int pySnapshot = this.py;
      if (this.newOpen) {
         short shortVal = 340;
         short shortVal2 = 130;
         int var5170Var172Value = pxSnapshot + 170 - shortVal / 2;
         int var6190Var202Value = pySnapshot + 190 - shortVal2 / 2;
         int var23Var17255Value = var5170Var172Value + shortVal / 2 - 55;
         if (this.el(intVal, intVal2, var23Var17255Value, var6190Var202Value + 88, 110, 28)) {
            this.en();
            return true;
         } else if (!this.el(intVal, intVal2, var5170Var172Value, var6190Var202Value, shortVal, shortVal2)) {
            this.newOpen = false;
            return true;
         } else {
            return true;
         }
      } else if (this.importOpen) {
         short shortVal3 = 380;
         short shortVal4 = 130;
         int var5170Var162Value = pxSnapshot + 170 - shortVal3 / 2;
         int var6190Var192Value = pySnapshot + 190 - shortVal4 / 2;
         int var22Var16255Value = var5170Var162Value + shortVal3 / 2 - 55;
         if (this.el(intVal, intVal2, var22Var16255Value, var6190Var192Value + 88, 110, 28)) {
            this.eo();
            return true;
         } else if (!this.el(intVal, intVal2, var5170Var162Value, var6190Var192Value, shortVal3, shortVal4)) {
            this.importOpen = false;
            return true;
         } else {
            return true;
         }
      } else {
         if (this.el(intVal, intVal2, pxSnapshot + 340 - 28, pySnapshot + 14, 20, 20)) {
            this.close();
            return true;
         }

         int var6100Value = pySnapshot + 100;

         for (int index = 0; index < 4; index++) {
            int var8ThisValue = index + this.scroll;
            if (var8ThisValue >= this.configs.size()) {
               break;
            }

            int var7Var8424Value = var6100Value + index * 42 + 4;
            int var101710Value = var7Var8424Value + 17 - 10;
            int var53401460Value = pxSnapshot + 340 - 14 - 60;
            int var1265Value = var53401460Value - 65;
            int var1365Value = var1265Value - 65;
            int var1465Value = var1365Value - 65;
            if (this.el(intVal, intVal2, var1465Value, var101710Value, 60, 20)) {
               this.sel = var8ThisValue;
               this.euShare((String)this.configs.get(var8ThisValue));
               return true;
            }

            if (this.el(intVal, intVal2, var1365Value, var101710Value, 60, 20)) {
               this.sel = var8ThisValue;
               ConfigManager.INSTANCE.setConfigName((String)this.configs.get(var8ThisValue));
               ConfigManager.INSTANCE.save();
               this.er("Saved: " + (String)this.configs.get(var8ThisValue), -14498466);
               return true;
            }

            if (this.el(intVal, intVal2, var1265Value, var101710Value, 60, 20)) {
               this.sel = var8ThisValue;
               this.ep();
               return true;
            }

            if (this.el(intVal, intVal2, var53401460Value, var101710Value, 60, 20)) {
               this.sel = var8ThisValue;
               this.eq();
               return true;
            }

            if (this.el(intVal, intVal2, pxSnapshot + 14, var7Var8424Value, 312, 34)) {
               this.sel = var8ThisValue;
               return true;
            }
         }

         int var63805414Value = pySnapshot + 380 - 54 + 14;
         boolean flag2 = this.configs.size() >= 5;
         if (!flag2 && this.el(intVal, intVal2, pxSnapshot + 14, var63805414Value, 110, 18)) {
            this.newOpen = true;
            this.newName = "";
            return true;
         } else if (flag2 && this.el(intVal, intVal2, pxSnapshot + 14, var63805414Value, 110, 18)) {
            this.er("Max 5 configs!", -1096636);
            return true;
         } else {
            byte byteVal = 90;
            int var534014Var24Value = pxSnapshot + 340 - 14 - byteVal;
            if (this.el(intVal, intVal2, var534014Var24Value, var63805414Value - 4, byteVal, 26)) {
               this.es();
               return true;
            } else {
               int var27890Value = var534014Var24Value - 8 - 90;
               if (this.el(intVal, intVal2, var27890Value, var63805414Value - 4, 90, 26)) {
                  this.importOpen = true;
                  this.importCode = "";
                  return true;
               } else {
                  return super.mouseClicked(arg, flag);
               }
            }
         }
      }
   }

   public boolean mouseScrolled(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4) {

      int maxValue = Math.max(0, this.configs.size() - 4);
      this.scroll = (int)Math.max(0.0, Math.min(maxValue, this.scroll - doubleVal4));
      return true;
   }

   public boolean keyPressed(KeyInput arg) {
      int var1Value = arg.getKeycode();
      if (this.newOpen) {
         if (var1Value == 256) {
            this.newOpen = false;
            return true;
         } else if (var1Value == 257) {
            this.en();
            return true;
         } else if (var1Value == 259 && !this.newName.isEmpty()) {
            this.newName = this.newName.substring(0, this.newName.length() - 1);
            return true;
         } else {
            return true;
         }
      } else if (this.importOpen) {
         if (var1Value == 256) {
            this.importOpen = false;
            return true;
         }

         if (var1Value == 257) {
            this.eo();
            return true;
         }

         if (var1Value == 259 && !this.importCode.isEmpty()) {
            this.importCode = this.importCode.substring(0, this.importCode.length() - 1);
            return true;
         }

         if (arg.isPaste() || var1Value == 86 && arg.hasCtrlOrCmd()) {
            String nullSnapshot = null;

            try {
               Clipboard toolkitValue = Toolkit.getDefaultToolkit().getSystemClipboard();
               if (toolkitValue.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                  nullSnapshot = (String)toolkitValue.getData(DataFlavor.stringFlavor);
               }
            } catch (Throwable error) {
            }

            if (nullSnapshot == null) {
               try {
                  nullSnapshot = MinecraftClient.getInstance().keyboard.getClipboard();
               } catch (Throwable error2) {
               }
            }

            if (nullSnapshot != null) {
               this.importCode = nullSnapshot.replace("\r\n", "\n").replace("\r", "\n").trim();
            }

            return true;
         } else {
            return true;
         }
      } else if (var1Value == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(arg);
      }
   }

   public boolean charTyped(CharInput arg) {
      String var1Value = arg.asString();
      if (this.newOpen && this.newName.length() < 24) {
         this.newName = this.newName + var1Value;
         return true;
      } else if (this.importOpen && this.importCode.length() < 65536) {
         this.importCode = this.importCode + var1Value;
         return true;
      } else {
         return super.charTyped(arg);
      }
   }

   public void ep() {
      if (!this.configs.isEmpty() && this.sel < this.configs.size()) {
         ConfigManager.INSTANCE.setConfigName((String)this.configs.get(this.sel));
      }

      ConfigManager.INSTANCE.load();
      this.er("Config loaded: " + ConfigManager.INSTANCE.getCurrentConfigName(), -14498466);
   }

   public void eq() {
      if (!this.configs.isEmpty()) {
         String local = (String)this.configs.get(this.sel);
         boolean flag = local.equals(ConfigManager.INSTANCE.getCurrentConfigName());

         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               File fileInst = new File(mc.runDirectory, "threesix_config_" + local + ".txt");
               if (fileInst.exists()) {
                  fileInst.delete();
               }

               File fileInst2 = new File(new File(mc.runDirectory, "threesix_configs"), local + ".txt");
               if (fileInst2.exists()) {
                  fileInst2.delete();
               }
            }
         } catch (Exception error) {
         }

         this.configs.remove(this.sel);
         if (this.sel >= this.configs.size()) {
            this.sel = Math.max(0, this.configs.size() - 1);
         }

         if (flag) {
            String local2 = this.configs.isEmpty() ? "default" : (String)this.configs.get(this.sel);

            try {
               MinecraftClient mc2 = MinecraftClient.getInstance();
               File[] mc2Value = mc2.runDirectory.listFiles(item -> {
                  return item.isFile() && item.getName().startsWith("threesix_config_") && item.getName().endsWith(".txt");
               });
               if (mc2Value != null && mc2Value.length > 0) {
                  File local3 = mc2Value[0];

                  for (File file : mc2Value) {
                     if (file.lastModified() > local3.lastModified()) {
                        local3 = file;
                     }
                  }

                  local2 = local3.getName().replace("threesix_config_", "").replace(".txt", "");
               } else if (!this.configs.isEmpty()) {
                  local2 = (String)this.configs.get(0);
               } else {
                  local2 = "default";
               }
            } catch (Exception error2) {
            }

            ConfigManager.INSTANCE.setConfigName(local2);
            ConfigManager.INSTANCE.load();
         }

         this.er("Config deleted.", -1096636);
      }
   }

   public void es() {
      ConfigManager.INSTANCE.getModules().forEach(item -> {
         if (((ClickGuiModule)item).isEnabled()) {
            ((ClickGuiModule)item).toggle();
         }
      });
      this.er("All modules disabled!", -680437);
   }

   public void euShare(String string) {

      ConfigShareModule configShareModuleValue = ConfigShareModule.getInstance();
      if (configShareModuleValue == null) {
         this.er("Config system not ready!", -1096636);
      } else {
         try {
            ConfigManager.INSTANCE.setConfigName(string);
            ConfigManager.INSTANCE.save();
            String local = configShareModuleValue.buildShareText();
            byte byteVal = 0;

            try {
               Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(local), null);
            } catch (Throwable error) {
            }

            if (byteVal == 0) {
               MinecraftClient mc = MinecraftClient.getInstance();
               if (mc != null) {
                  mc.keyboard.setClipboard(local);
               }
            }

            this.er("Content copied: " + string + " (" + local.length() + " chars)", -14498466);
         } catch (Exception error2) {
            this.er("Share failed!", -1096636);
         }
      }
   }

   public void eo() {
      String local = this.importCode == null ? "" : this.importCode.replace("\r\n", "\n").replace("\r", "\n").trim();
      if (local.isEmpty()) {
         this.er("Paste config content first!", -1096636);
      } else if (!local.contains(":")) {
         this.er("Invalid config content!", -1096636);
      } else {
         try {
            ConfigShareModule configShareModuleValue = ConfigShareModule.getInstance();
            if (configShareModuleValue == null) {
               this.er("Config system not ready!", -1096636);
               return;
            }

            String configShareModuleValue2 = ConfigShareModule.parseConfigName(local);
            if (configShareModuleValue2 != null && !configShareModuleValue2.equals(ConfigManager.INSTANCE.getCurrentConfigName())) {
               if (this.configs.size() >= 5 && !this.configs.contains(configShareModuleValue2)) {
                  this.er("Max 5 configs!", -1096636);
                  return;
               }

               ConfigManager.INSTANCE.setConfigName(configShareModuleValue2);
               if (!this.configs.contains(configShareModuleValue2)) {
                  this.configs.add(configShareModuleValue2);
               }
            }

            configShareModuleValue.importShareText(local);
            ConfigManager.INSTANCE.save();
            this.eb();
            this.importOpen = false;
            this.importCode = "";
            this.er("Imported: " + ConfigManager.INSTANCE.getCurrentConfigName(), -14498466);
         } catch (Exception error) {
            this.er("Invalid config content!", -1096636);
         }
      }
   }

   public void en() {
      if (this.configs.size() >= 5) {
         this.er("Max 5 configs!", -1096636);
      } else if (this.newName.trim().isEmpty()) {
         this.er("Enter a name first!", -1096636);
      } else {
         String local = this.newName.trim();
         ConfigManager.INSTANCE.setConfigName(local);
         ConfigManager.INSTANCE.save();
         if (!this.configs.contains(local)) {
            this.configs.add(local);
         }

         this.newOpen = false;
         this.newName = "";
         this.sel = this.configs.indexOf(local);
         this.er("\"" + local + "\" saved!", -14498466);
      }
   }

   public void eb() {
      this.configs.clear();

      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null) {
            File[] mcValue = mc.runDirectory.listFiles(item -> {
               return item.isFile() && item.getName().startsWith("threesix_config_") && item.getName().endsWith(".txt");
            });
            if (mcValue != null) {
               Arrays.sort(mcValue, (item, local) -> {
                  return Long.compare(local.lastModified(), item.lastModified());
               });

               for (File file : mcValue) {
                  this.configs.add(file.getName().replace("threesix_config_", "").replace(".txt", ""));
               }
            }

            String configManagerValue = ConfigManager.INSTANCE.getCurrentConfigName();
            int intVal = this.configs.indexOf(configManagerValue);
            this.sel = intVal >= 0 ? intVal : 0;
            if (this.configs.isEmpty()) {
               ConfigManager.INSTANCE.setConfigName("default");
            }
         }
      } catch (Exception error) {
         this.sel = 0;
      }
   }

   public void er(String string, int intVal) {
      this.status = string;
      this.statusCol = intVal;
      this.statusAt = System.currentTimeMillis();
   }

   public boolean el(int intVal, int intVal2, int intVal3, int intVal4, int intVal5, int intVal6) {
      return intVal >= intVal3 && intVal <= intVal3 + intVal5 && intVal2 >= intVal4 && intVal2 <= intVal4 + intVal6;
   }

   public boolean shouldPause() {
      return false;
   }

   public void renderBackground(DrawContext arg, int intVal, int intVal2, float floatVal) {
   }

}
