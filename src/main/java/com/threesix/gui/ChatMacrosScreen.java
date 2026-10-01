package com.threesix.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.GuiRenderUtil;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.module.ClickGuiModule;
import com.threesix.module.ChatMacroModule;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.manager.CustomFontManager;

public class ChatMacrosScreen extends Screen {
   public static final int W = 420;
   public static final int H = 380;
   public static final int PAD = 12;
   public static final int HEAD_H = 36;
   public static final int FOOT_H = 44;
   public static final int ROW_H = 52;
   public static final int ROW_GAP = 6;
   public static final int KEY_W = 72;
   public static final float R = 14.0F;
   public static final float R_SM = 8.0F;
   public static final float R_XS = 5.0F;
   public static final int C_BD_IN = -15327184;
   public static final int C_TEXT = -2234128;
   public static final int C_TEXT_DIM = -7824982;
   public static final int C_MUTED = -12298906;
   public static final int C_RED = -2539435;
   public static final int C_GREEN = -11751558;
   public static final int C_WHITE_10 = 285212671;
   public static final int C_GRID_BG = -16183270;
   public final Screen parent;
   public final ChatMacroModule module;
   public long openNs = 0L;
   public final List macros = new ArrayList();
   public int editingText = -1;
   public int listeningKey = -1;
   public int scroll = 0;
   public int px;
   public int py;

   public ChatMacrosScreen(Screen arg) {
      super(Text.literal(""));
      this.parent = arg;
      this.module = this.bH();
      this.bI();
   }

   public ChatMacroModule bH() {

      for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)ConfigManager.INSTANCE.getModules()) {
         if (moduleBase instanceof ChatMacroModule) {
            return (ChatMacroModule)moduleBase;
         }
      }

      return null;
   }

   public void bI() {
      this.macros.clear();
      if (this.module == null) {
         this.macros.add(new String[]{"", "0"});
      } else {
         List local = this.module.getSettings();

         for (byte index = 0; index + 1 < local.size(); index = (byte)(index + 2)) {
            String local2 = (String)((ClientSetting)local.get(index)).getValue();
            String stringValue = String.valueOf(((ClientSetting)local.get(index + 1)).getValue());
            this.macros.add(new String[]{local2, stringValue});
         }

         if (this.macros.isEmpty()) {
            this.macros.add(new String[]{"", "0"});
         }
      }
   }

   public void bJ() {
      if (this.module != null) {
         List local = this.module.getSettings();

         for (int index = 0; index < this.macros.size() && index * 2 + 1 < local.size(); index++) {
            ((ClientSetting)local.get(index * 2)).setValue(((String[])this.macros.get(index))[0]);
            ((ClientSetting)local.get(index * 2 + 1)).setValue(this.bK(((String[])this.macros.get(index))[1]));
         }
      }
   }

   public int bK(String string) {
      try {

         return Integer.parseInt(string);
      } catch (Exception error) {
         return 0;
      }
   }

   public int bL() {
      return (this.width - 420) / 2;
   }

   public int bM() {

      return (this.height - 380) / 2;
   }

   public int bN() {

      return this.bM() + 36 + 12;
   }

   public int bO() {

      return 276;
   }

   public int bP() {
      return Math.max(1, this.bO() / 58);
   }

   public int bQ() {

      return Math.max(0, this.macros.size() - this.bP());
   }

   public void init() {

      this.px = this.bL();
      this.py = this.bM();
   }

   public void renderBackground(DrawContext arg, int intVal, int intVal2, float floatVal) {
   }

   public void render(DrawContext arg, int intVal, int intVal2, float floatVal) {

      if (this.openNs == 0L) {
         this.openNs = System.nanoTime();
      }

      float floatVal2 = this.bR(Math.min(1.0F, (float)(System.nanoTime() - this.openNs) / 1.6E8F));
      this.px = this.bL();
      this.py = this.bM();
      int clickGuiModuleValue = ClickGuiModule.getAccentColorArgb();
      int clickGuiModuleValue2 = ClickGuiModule.getBackgroundColorArgb();
      float clickGuiModuleValue3 = ClickGuiModule.getBaseCornerRadius();
      float maxValue = Math.max(5.0F, clickGuiModuleValue3 * 0.6F);
      float maxValue2 = Math.max(4.0F, clickGuiModuleValue3 * 0.4F);
      float clickGuiModuleValue4 = ClickGuiModule.getGlassIntensityFactor();
      GuiRenderUtil.fillRoundedRect(arg, this.px - 4, this.py - 4, 428.0F, 388.0F, clickGuiModuleValue3 + 3.0F, this.bT(bS(clickGuiModuleValue & 16777215, (int)(25.0F * floatVal2)), 1.0F), false);
      GuiRenderUtil.fillRoundedRect(arg, this.px, this.py, 420.0F, 380.0F, clickGuiModuleValue3, this.bT(clickGuiModuleValue2, floatVal2), false);
      if (clickGuiModuleValue4 > 0.01F) {
         GuiRenderUtil.fillRoundedRect(arg, this.px + 1, this.py + 1, 418.0F, 38.0F, clickGuiModuleValue3, this.bT(bS(16777215, (int)(15.0F * clickGuiModuleValue4)), floatVal2), false);
         GuiRenderUtil.strokeRoundedRect(arg, this.px, this.py, 420.0F, 380.0F, clickGuiModuleValue3, 1.0F, this.bT(bS(16777215, (int)(50.0F * clickGuiModuleValue4)), floatVal2), false);
      } else {
         GuiRenderUtil.strokeRoundedRect(arg, this.px, this.py, 420.0F, 380.0F, clickGuiModuleValue3, 1.0F, this.bT(clickGuiModuleValue & 16777215 | 1426063360, floatVal2), false);
      }

      GuiRenderUtil.fillPerCornerGradient(arg, this.px, this.py, 420.0F, 36.0F, clickGuiModuleValue3, clickGuiModuleValue3, 0.0F, 0.0F, false, this.bT(bS(0, 55), floatVal2));
      arg.fill(this.px, this.py + 36, this.px + 420, this.py + 36 + 1, this.bT(-15327184, floatVal2));
      GuiRenderUtil.fillRoundedRect(arg, this.px, this.py + 8, 3.0F, 20.0F, 1.5F, this.bT(clickGuiModuleValue, floatVal2), false);
      this.bU(arg, "CHAT MACROS", this.px + 12 + 8, this.py + 11, this.bT(-2234128, floatVal2));
      this.bU(arg, this.macros.size() + " macros", this.px + 12 + 8, this.py + 23, this.bT(-7824982, floatVal2));
      int intVal3 = this.px + 420 - 12 - 60;
      int intVal4 = this.py + 9;
      boolean flag = this.bV(intVal, intVal2, intVal3, intVal4, 60, 18);
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, 60.0F, 18.0F, 9.0F, this.bT(flag ? clickGuiModuleValue & 16777215 | 855638016 : clickGuiModuleValue & 16777215 | 402653184, floatVal2), false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal3, intVal4, 60.0F, 18.0F, 9.0F, 1.0F, this.bT(clickGuiModuleValue & 16777215 | 1711276032, floatVal2), false);
      this.bU(arg, "+ ADD", intVal3 + (60 - this.bW("+ ADD")) / 2, intVal4 + 4, this.bT(clickGuiModuleValue, floatVal2));
      int intVal5 = this.px + 12;
      short shortVal = 396;
      int intVal6 = this.bN();
      int intVal7 = this.bP();
      this.scroll = Math.max(0, Math.min(this.bQ(), this.scroll));

      for (int index = 0; index < intVal7; index++) {
         int var19ThisValue = index + this.scroll;
         if (var19ThisValue >= this.macros.size()) {
            break;
         }

         String[] local = (String[])this.macros.get(var19ThisValue);
         String local2 = local[0];
         int intVal8 = this.bK(local[1]);
         int var17Var1958Value = intVal6 + index * 58;
         boolean flag2 = this.editingText == var19ThisValue;
         boolean flag3 = this.listeningKey == var19ThisValue;
         boolean flag4 = flag2 || flag3;
         int intVal9 = flag4 ? clickGuiModuleValue & 16777215 | 369098752 : this.bT(419430399, floatVal2);
         int intVal10 = flag4 ? clickGuiModuleValue & 16777215 | 1426063360 : this.bT(587202559, floatVal2);
         GuiRenderUtil.fillRoundedRect(arg, intVal5, var17Var1958Value, shortVal, 52.0F, maxValue, intVal9, false);
         GuiRenderUtil.strokeRoundedRect(arg, intVal5, var17Var1958Value, shortVal, 52.0F, maxValue, 1.0F, intVal10, false);
         this.bU(arg, "#" + (var19ThisValue + 1), intVal5 + 8, var17Var1958Value + 6, this.bT(flag4 ? clickGuiModuleValue : -12298906, floatVal2));
         int var158Value = intVal5 + 8;
         int var167248Value = shortVal - 72 - 48;
         boolean flag5 = flag2 && System.currentTimeMillis() / 500L % 2L == 0L;
         String var22Var321011Value = local2 + (flag5 ? "|" : "");
         if (var22Var321011Value.isEmpty()) {
            var22Var321011Value = flag2 ? "|" : "";
         }

         int intVal11 = flag2 ? this.bT(clickGuiModuleValue & 16777215 | 570425344, floatVal2) : this.bT(570425344, floatVal2);
         int intVal12 = flag2 ? this.bT(clickGuiModuleValue & 16777215 | -2013265920, floatVal2) : this.bT(872415231, floatVal2);
         GuiRenderUtil.fillRoundedRect(arg, var158Value - 2, var17Var1958Value + 22, var167248Value + 4, 20.0F, maxValue2, intVal11, false);
         GuiRenderUtil.strokeRoundedRect(arg, var158Value - 2, var17Var1958Value + 22, var167248Value + 4, 20.0F, maxValue2, 1.0F, intVal12, false);
         String local3 = "Type message or /command...";
         int intVal13 = local2.isEmpty() && !flag2 ? this.bT(-12298906, floatVal2) : this.bT(-2234128, floatVal2);
         String local4 = local2.isEmpty() && !flag2 ? local3 : var22Var321011Value;
         int var314Value = var167248Value - 4;
         if (this.bW(local4) > var314Value) {
            while (local4.length() > 1 && this.bW(local4) > var314Value) {
               local4 = local4.substring(1);
            }
         }

         this.bU(arg, local4, var158Value + 2, var17Var1958Value + 28, intVal13);
         int var15Var167234Value = intVal5 + shortVal - 72 - 34;
         int var2422Value = var17Var1958Value + 22;
         String local5 = flag3 ? "PRESS..." : (intVal8 <= 0 ? "NO KEY" : this.bX(intVal8));
         int intVal14 = flag3 ? this.bT(clickGuiModuleValue & 16777215 | 1140850688, floatVal2) : (intVal8 > 0 ? this.bT(clickGuiModuleValue & 16777215 | 671088640, floatVal2) : this.bT(570425344, floatVal2));
         int intVal15 = flag3 ? this.bT(clickGuiModuleValue, floatVal2) : (intVal8 > 0 ? this.bT(clickGuiModuleValue & 16777215 | 1996488704, floatVal2) : this.bT(872415231, floatVal2));
         GuiRenderUtil.fillRoundedRect(arg, var15Var167234Value, var2422Value, 72.0F, 20.0F, maxValue2, intVal14, false);
         GuiRenderUtil.strokeRoundedRect(arg, var15Var167234Value, var2422Value, 72.0F, 20.0F, maxValue2, 1.0F, intVal15, false);
         this.bU(arg, local5, var15Var167234Value + (72 - this.bW(local5)) / 2, var2422Value + 5, this.bT(flag3 ? clickGuiModuleValue : (intVal8 > 0 ? -2234128 : -12298906), floatVal2));
         int var15Var1626Value = intVal5 + shortVal - 26;
         int var2422Value2 = var17Var1958Value + 22;
         boolean flag6 = this.bV(intVal, intVal2, var15Var1626Value, var2422Value2, 20, 20);
         GuiRenderUtil.fillRoundedRect(arg, var15Var1626Value, var2422Value2, 20.0F, 20.0F, maxValue2, this.bT(flag6 ? 1155088469 : 584663125, floatVal2), false);
         GuiRenderUtil.strokeRoundedRect(arg, var15Var1626Value, var2422Value2, 20.0F, 20.0F, maxValue2, 1.0F, this.bT(-2539435, floatVal2 * (flag6 ? 0.9F : 0.4F)), false);
         this.bU(arg, "✕", var15Var1626Value + (20 - this.bW("✕")) / 2, var2422Value2 + 5, this.bT(-2539435, floatVal2));
      }

      if (this.macros.isEmpty()) {
         this.bU(arg, "No macros yet — click + ADD", this.px + 210 - this.bW("No macros yet — click + ADD") / 2, this.py + 190 - 5, this.bT(-12298906, floatVal2));
      }

      if (this.macros.size() > this.bP()) {
         int intVal16 = this.px + 420 - 8;
         int intVal17 = this.bN();
         int intVal18 = this.bO();
         float floatVal3 = (float)this.bP() / this.macros.size();
         float maxValue3 = Math.max(20.0F, intVal18 * floatVal3);
         float var51Var53Var57FloatThisVa = intVal17 + (intVal18 - maxValue3) * ((float)this.scroll / Math.max(1, this.bQ()));
         GuiRenderUtil.fillRoundedRect(arg, intVal16, intVal17, 4.0F, intVal18, 2.0F, this.bT(-15327184, floatVal2), false);
         GuiRenderUtil.fillRoundedRect(arg, intVal16, (int)var51Var53Var57FloatThisVa, 4.0F, (int)maxValue3, 2.0F, this.bT(clickGuiModuleValue & 16777215 | -1442840576, floatVal2), false);
      }

      int intVal19 = this.py + 380 - 44;
      arg.fill(this.px, intVal19, this.px + 420, intVal19 + 1, this.bT(-15327184, floatVal2));
      GuiRenderUtil.fillPerCornerGradient(arg, this.px, intVal19, 420.0F, 44.0F, 0.0F, 0.0F, clickGuiModuleValue3, clickGuiModuleValue3, false, this.bT(bS(0, 50), floatVal2));
      int var5011Value = intVal19 + 11;
      int intVal20 = this.px + 12;
      boolean flag7 = this.bV(intVal, intVal2, intVal20, var5011Value, 80, 22);
      GuiRenderUtil.fillRoundedRect(arg, intVal20, var5011Value, 80.0F, 22.0F, 11.0F, this.bT(flag7 ? 419430399 : 150994943, floatVal2), false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal20, var5011Value, 80.0F, 22.0F, 11.0F, 1.0F, this.bT(-15327184, floatVal2), false);
      this.bU(arg, "CANCEL", intVal20 + (80 - this.bW("CANCEL")) / 2, var5011Value + 7, this.bT(-7824982, floatVal2));
      int intVal21 = this.px + 420 - 12 - 80;
      boolean flag8 = this.bV(intVal, intVal2, intVal21, var5011Value, 80, 22);
      GuiRenderUtil.fillRoundedRect(arg, intVal21, var5011Value, 80.0F, 22.0F, 11.0F, this.bT(flag8 ? clickGuiModuleValue : clickGuiModuleValue & 16777215 | 570425344, floatVal2), false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal21, var5011Value, 80.0F, 22.0F, 11.0F, 1.5F, this.bT(clickGuiModuleValue, floatVal2 * (flag8 ? 1.0F : 0.5F)), false);
      this.bU(arg, "SAVE", intVal21 + (80 - this.bW("SAVE")) / 2, var5011Value + 7, this.bT(flag8 ? -16777216 : clickGuiModuleValue, floatVal2));
      super.render(arg, intVal, intVal2, floatVal);
   }

   public boolean mouseClicked(Click arg, boolean flag) {
      int intVal = (int)arg.x();
      int intVal2 = (int)arg.y();
      this.px = this.bL();
      this.py = this.bM();
      int intVal3 = this.py + 380 - 44;
      int var711Value = intVal3 + 11;
      if (this.bV(intVal, intVal2, this.px + 12, var711Value, 80, 22)) {
         MinecraftClient.getInstance().setScreen(this.parent);
         return true;
      }

      if (this.bV(intVal, intVal2, this.px + 420 - 12 - 80, var711Value, 80, 22)) {
         this.bJ();
         MinecraftClient.getInstance().setScreen(this.parent);
         return true;
      }

      int intVal4 = this.px + 420 - 12 - 60;
      int intVal5 = this.py + 9;
      if (this.bV(intVal, intVal2, intVal4, intVal5, 60, 18)) {
         this.macros.add(new String[]{"", "0"});
         this.scroll = this.bQ();
         this.editingText = this.macros.size() - 1;
         this.listeningKey = -1;
         return true;
      }

      this.editingText = -1;
      this.listeningKey = -1;
      int intVal6 = this.px + 12;
      short shortVal = 396;
      int intVal7 = this.bN();
      int intVal8 = this.bP();

      for (int index = 0; index < intVal8; index++) {
         int var15ThisValue = index + this.scroll;
         if (var15ThisValue >= this.macros.size()) {
            break;
         }

         int var13Var1558Value = intVal7 + index * 58;
         int var118Value = intVal6 + 8;
         int var127248Value = shortVal - 72 - 48;
         if (this.bV(intVal, intVal2, var118Value - 2, var13Var1558Value + 22, var127248Value + 4, 20)) {
            this.editingText = var15ThisValue;
            return true;
         }

         int var11Var127234Value = intVal6 + shortVal - 72 - 34;
         int var1722Value = var13Var1558Value + 22;
         if (this.bV(intVal, intVal2, var11Var127234Value, var1722Value, 72, 20)) {
            this.listeningKey = var15ThisValue;
            return true;
         }

         int var11Var1226Value = intVal6 + shortVal - 26;
         int var1722Value2 = var13Var1558Value + 22;
         if (this.bV(intVal, intVal2, var11Var1226Value, var1722Value2, 20, 20)) {
            this.macros.remove(var15ThisValue);
            this.scroll = Math.max(0, Math.min(this.bQ(), this.scroll));
            this.editingText = -1;
            this.listeningKey = -1;
            return true;
         }
      }

      return super.mouseClicked(arg, flag);
   }

   public boolean mouseScrolled(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4) {
      this.scroll = Math.max(0, Math.min(this.bQ(), this.scroll + (doubleVal4 > 0.0 ? -1 : 1)));
      return true;
   }

   public boolean keyPressed(KeyInput arg) {

      int var1Value = arg.getKeycode();
      if (this.listeningKey < 0) {
         if (this.editingText >= 0) {
            if (var1Value != 256 && var1Value != 257) {
               if (var1Value == 259 && !((String[])this.macros.get(this.editingText))[0].isEmpty()) {
                  String local = ((String[])this.macros.get(this.editingText))[0];
                  ((String[])this.macros.get(this.editingText))[0] = local.substring(0, local.length() - 1);
                  return true;
               } else if (arg.isPaste()) {
                  String[] local2 = (String[])this.macros.get(this.editingText);
                  local2[0] = local2[0] + MinecraftClient.getInstance().keyboard.getClipboard().trim();
                  return true;
               } else {
                  return true;
               }
            } else {
               this.editingText = -1;
               return true;
            }
         } else if (var1Value == 256) {
            MinecraftClient.getInstance().setScreen(this.parent);
            return true;
         } else {
            return super.keyPressed(arg);
         }
      } else {
         if (var1Value != 256 && var1Value != 259) {
            ((String[])this.macros.get(this.listeningKey))[1] = String.valueOf(var1Value);
         } else {
            ((String[])this.macros.get(this.listeningKey))[1] = "0";
         }

         this.listeningKey = -1;
         return true;
      }
   }

   public boolean charTyped(CharInput arg) {

      if (this.editingText >= 0) {
         String[] local = (String[])this.macros.get(this.editingText);
         local[0] = local[0] + arg.asString();
         return true;
      } else {
         return super.charTyped(arg);
      }
   }

   public void bU(DrawContext arg, String string, int intVal, int intVal2, int intVal3) {
      CustomFontManager.INSTANCE7.drawText(arg, string, intVal, intVal2, intVal3);
   }

   public int bW(String string) {
      return CustomFontManager.INSTANCE7.getStringWidth(string);
   }

   public int bT(int intVal, float floatVal) {
      return Math.max(0, Math.min(255, (int)((intVal >> 24 & 0xFF) * floatVal))) << 24 | intVal & 16777215;
   }

   public static int bS(int intVal, int intVal2) {
      return Math.max(0, Math.min(255, intVal2)) << 24 | intVal & 16777215;
   }

   public float bR(float floatVal) {
      return 1.0F - (float)Math.pow(1.0F - Math.min(1.0F, floatVal), 3.0);
   }

   public boolean bV(int intVal, int intVal2, int intVal3, int intVal4, int intVal5, int intVal6) {

      return intVal >= intVal3 && intVal <= intVal3 + intVal5 && intVal2 >= intVal4 && intVal2 <= intVal4 + intVal6;
   }

   public String bX(int intVal) {
      if (intVal <= 0) {
         return "None";
      }

      String gLFWValue = GLFW.glfwGetKeyName(intVal, 0);
      return gLFWValue != null && !gLFWValue.isBlank() ? gLFWValue.toUpperCase() : ClickGuiScreen.bY(intVal);
   }

   public boolean shouldPause() {
      return false;
   }

}
