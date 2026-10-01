package com.threesix.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.GuiRenderUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.setting.BlockListSetting;
import com.threesix.manager.CustomFontManager;
import com.threesix.util.StringVaultDecoder;
import com.threesix.module.StorageEspModule;
import com.threesix.manager.ConfigManager;

public final class BlockColorPickerScreen extends Screen {
   public static final int W = 320;
   public static final int H = 300;
   public static final int PAD = 10;
   public static final int HEAD_H = 34;
   public static final int FOOT_H = 36;
   public static final int TAB_H = 24;
   public static final int SRCH_H = 22;
   public static final int CELL = 22;
   public static final int GAP = 3;
   public static final int COLS = 9;
   public static final int CP_W = 140;
   public static final int CP_H = 130;
   public static final int CP_SV = 90;
   public static final int CP_HUE = 12;
   public static final int CP_GAP = 6;
   public static final int C_BD_IN = -15327184;
   public static final int C_TEXT = -2234128;
   public static final int C_TEXT_DIM = -7824982;
   public static final int C_MUTED = -12298906;
   public static final int C_RED = -2539435;
   public static final int C_GRID_BG = -16183270;
   public static final int C_CELL_BG = -15656928;
   public static final int C_CELL_HOV = -15063498;
   public static final int C_CELL_SEL = -15785936;
   public static final int C_WHITE_10 = 285212671;
   public final Screen parent;
   public final ModuleBase module;
   public final BlockListSetting setting;
   public final Map colorMap;
   public final Consumer colorSaveCallback;
   public final Map builtinColors = new LinkedHashMap();
   public final Set builtinBlocks = new LinkedHashSet();
   public final Set sel = new LinkedHashSet();
   public final List all = new ArrayList();
   public List filtered = new ArrayList();
   public String search = "";
   public boolean showSel = false;
   public int scroll = 0;
   public long openNs = 0L;
   public Block cpBlock = null;
   public boolean cpBuiltin = false;
   public int cpX;
   public int cpY;
   public boolean cpDragSV = false;
   public boolean cpDragHue = false;

   public BlockColorPickerScreen(Screen arg, ModuleBase moduleBase, BlockListSetting blockListSetting) {
      this(arg, moduleBase, blockListSetting, new LinkedHashMap(), null);
   }

   public BlockColorPickerScreen(Screen arg, ModuleBase moduleBase, BlockListSetting blockListSetting, Map map, Consumer consumer) {
      super(Text.literal(""));
      this.parent = arg;
      this.module = moduleBase;
      this.setting = blockListSetting;
      this.colorMap = new LinkedHashMap(map);
      this.colorSaveCallback = consumer;
      if (this.module instanceof StorageEspModule local) {
         this.builtinColors.putAll(local.getBuiltinColors());
         this.builtinBlocks.addAll(this.builtinColors.keySet());
      }

      this.sel.addAll(blockListSetting.getSelectedBlocks());
      this.all.addAll(blockListSetting.getAllBlocks());
      this.all.removeIf(item -> {
         return item == null || item == Blocks.AIR || new ItemStack((Block)item).isEmpty();
      });
      this.all.sort(Comparator.comparing(blockListSetting::getBlockName, String.CASE_INSENSITIVE_ORDER));
      this.W();
   }

   public int X() {
      return (this.width - 320) / 2;
   }

   public int Y() {
      return (this.height - 300) / 2;
   }

   public int Z() {
      return this.X() + 10;
   }

   public int scanAmethystChunks() {
      return this.Y() + 34 + 24 + 22 + 6;
   }

   public int rescanBlocks() {
      return 300;
   }

   public int clearBlockCache() {
      return 172;
   }

   public int bd() {
      return Math.max(1, this.clearBlockCache() / 25);
   }

   public int rescanHoles() {
      return (int)Math.ceil(this.pruneHoleCache().size() / 9.0);
   }

   public int rebuildHoleMesh() {
      return Math.max(0, this.rescanHoles() - this.bd());
   }

   public List pruneHoleCache() {
      return this.showSel ? this.clearHoleMesh() : this.filtered;
   }

   public List clearHoleMesh() {
      ArrayList arrayListInst = new ArrayList(this.builtinBlocks);

      for (Block class2248 : (Iterable<Block>)this.sel) {
         if (!this.builtinBlocks.contains(class2248)) {
            arrayListInst.add(class2248);
         }
      }

      return arrayListInst;
   }

   public void W() {
      this.scanNearbyStorages(false);
   }

   public void scanNearbyStorages(boolean flag) {
      if (this.search.isBlank()) {
         this.filtered = new ArrayList(this.all);
      } else {
         String local = this.search.trim().toLowerCase();
         this.filtered = new ArrayList();

         for (Block class2248 : (Iterable<Block>)this.all) {
            if (this.setting.getBlockName(class2248).toLowerCase().contains(local)) {
               this.filtered.add(class2248);
            }
         }
      }

      if (!flag) {
         this.scroll = 0;
      } else {
         this.scroll = Math.max(0, Math.min(this.rebuildHoleMesh(), this.scroll));
      }
   }

   public Color bj(Block arg) {
      return this.builtinColors.containsKey(arg) ? (Color)this.builtinColors.get(arg) : (Color)this.colorMap.computeIfAbsent(arg, item -> this.bm());
   }

   public void bl(Block arg, Color color) {
      if (this.builtinColors.containsKey(arg)) {
         this.builtinColors.put(arg, color);
      } else {
         this.colorMap.put(arg, color);
      }
   }

   public Color bm() {
      int colorValue = Color.HSBtoRGB((float)Math.random(), 0.75F, 1.0F);
      return new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, 200);
   }

   public boolean bn(Block arg) {
      return this.builtinBlocks.contains(arg) || this.sel.contains(arg);
   }

   public void render(DrawContext arg, int intVal, int intVal2, float floatVal) {
      if (this.openNs == 0L) {
         this.openNs = System.nanoTime();
      }

      float floatVal2 = this.bo(Math.min(1.0F, (float)(System.nanoTime() - this.openNs) / 1.6E8F));
      int intVal3 = this.X();
      int intVal4 = this.Y();
      int clickGuiModuleValue = ClickGuiModule.getAccentColorArgb();
      int clickGuiModuleValue2 = ClickGuiModule.getBackgroundColorArgb();
      float clickGuiModuleValue3 = ClickGuiModule.getBaseCornerRadius();
      float maxValue = Math.max(5.0F, clickGuiModuleValue3 * 0.6F);
      float maxValue2 = Math.max(4.0F, clickGuiModuleValue3 * 0.4F);
      float clickGuiModuleValue4 = ClickGuiModule.getGlassIntensityFactor();
      arg.fill(0, 0, this.width, this.height, this.bt(-2013265920, floatVal2));
      GuiRenderUtil.fillRoundedRect(arg, intVal3 - 4, intVal4 - 4, 328.0F, 308.0F, clickGuiModuleValue3 + 3.0F, this.bt(bu(clickGuiModuleValue & 16777215, (int)(28.0F * floatVal2)), 1.0F), false);
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, 320.0F, 300.0F, clickGuiModuleValue3, this.bt(clickGuiModuleValue2, floatVal2), false);
      if (clickGuiModuleValue4 > 0.01F) {
         GuiRenderUtil.fillRoundedRect(arg, intVal3 + 1, intVal4 + 1, 318.0F, 36.0F, clickGuiModuleValue3, this.bt(bu(16777215, (int)(16.0F * clickGuiModuleValue4)), floatVal2), false);
         GuiRenderUtil.strokeRoundedRect(arg, intVal3, intVal4, 320.0F, 300.0F, clickGuiModuleValue3, 1.0F, this.bt(bu(16777215, (int)(55.0F * clickGuiModuleValue4)), floatVal2), false);
      } else {
         GuiRenderUtil.strokeRoundedRect(arg, intVal3, intVal4, 320.0F, 300.0F, clickGuiModuleValue3, 1.0F, this.bt(clickGuiModuleValue & 16777215 | 1426063360, floatVal2), false);
      }

      GuiRenderUtil.fillPerCornerGradient(arg, intVal3, intVal4, 320.0F, 34.0F, clickGuiModuleValue3, clickGuiModuleValue3, 0.0F, 0.0F, false, this.bt(bu(0, 55), floatVal2));
      arg.fill(intVal3, intVal4 + 34, intVal3 + 320, intVal4 + 34 + 1, this.bt(-15327184, floatVal2));
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4 + 8, 3.0F, 18.0F, 1.5F, this.bt(clickGuiModuleValue, floatVal2), false);
      String local = (this.setting.getName() + " — " + (this.module == null ? "" : this.module.getName2())).toUpperCase();
      this.bv(arg, local, intVal3 + 10 + 8, intVal4 + 10, this.bt(-2234128, floatVal2));
      int intVal5 = this.builtinBlocks.size() + this.sel.size();
      this.bv(arg, intVal5 + " selected", intVal3 + 10 + 8, intVal4 + 22, this.bt(-7824982, floatVal2));
      int var7344Value = intVal4 + 34 + 4;
      short shortVal = 145;
      int var610Value = intVal3 + 10;
      int var18Var1710Value = var610Value + shortVal + 10;
      this.bx(arg, var610Value, var7344Value, shortVal, 18, "ALL", !this.showSel, this.bw(intVal, intVal2, var610Value, var7344Value, shortVal, 18), floatVal2, clickGuiModuleValue);
      this.bx(
         arg,
         var18Var1710Value,
         var7344Value,
         shortVal,
         18,
         "SELECTED (" + (this.builtinBlocks.size() + this.sel.size()) + ")",
         this.showSel,
         this.bw(intVal, intVal2, var18Var1710Value, var7344Value, shortVal, 18),
         floatVal2,
         clickGuiModuleValue
      );
      if (!this.showSel) {
         int var734242Value = intVal4 + 34 + 24 + 2;
         boolean flag = !this.search.isEmpty();
         GuiRenderUtil.fillRoundedRect(arg, intVal3 + 10, var734242Value, 300.0F, 20.0F, maxValue, this.bt(-16183270, floatVal2), false);
         GuiRenderUtil.strokeRoundedRect(arg, intVal3 + 10, var734242Value, 300.0F, 20.0F, maxValue, 1.0F, this.bt(flag ? clickGuiModuleValue & 16777215 | 1711276032 : -15327184, floatVal2), false);
         this.bv(arg, flag ? "⌕  " + this.search + "_" : "⌕  threesix+", intVal3 + 10 + 8, var734242Value + 11 - 5, this.bt(flag ? -2234128 : -12298906, floatVal2));
      } else {
         int var734242Value2 = intVal4 + 34 + 24 + 2;
         this.bv(arg, "Right-click a block to change its color", intVal3 + 10 + 4, var734242Value2 + 11 - 5, this.bt(-12298906, floatVal2));
      }

      List local2 = this.pruneHoleCache();
      int intVal6 = this.Z();
      int intVal7 = this.scanAmethystChunks();
      int intVal8 = this.rescanBlocks();
      int intVal9 = this.clearBlockCache();
      GuiRenderUtil.fillRoundedRect(arg, intVal6 - 3, intVal7 - 3, intVal8 + 6, intVal9 + 6, maxValue, this.bt(-16183270, floatVal2), false);
      String nullSnapshot = null;
      int var33Snapshot = 0;
      int var34Snapshot = 0;
      int intVal10 = this.bd();

      for (int index = 0; index < intVal10; index++) {
         for (int index2 = 0; index2 < 9; index2++) {
            int intVal11 = (index + this.scroll) * 9 + index2;
            if (intVal11 >= local2.size()) {
               break;
            }

            Block local3 = (Block)local2.get(intVal11);
            int var43Var3025Value = intVal6 + index2 * 25;
            int var22Var2925Value = intVal7 + index * 25;
            boolean flag2 = this.bn(local3);
            boolean flag3 = this.builtinBlocks.contains(local3);
            boolean flag4 = this.bw(intVal, intVal2, var43Var3025Value, var22Var2925Value, 22, 22);
            GuiRenderUtil.fillRoundedRect(arg, var43Var3025Value, var22Var2925Value, 22.0F, 22.0F, maxValue2, this.bt(flag2 ? -15785936 : (flag4 ? -15063498 : -15656928), floatVal2), false);
            if (flag2) {
               Color local4 = this.bj(local3);
               int intVal12 = (int)(50.0F * floatVal2) << 24 | local4.getRed() << 16 | local4.getGreen() << 8 | local4.getBlue();
               GuiRenderUtil.fillRoundedRect(arg, var43Var3025Value, var22Var2925Value, 22.0F, 22.0F, maxValue2, intVal12, false);
               int intVal13 = (int)(255.0F * floatVal2) << 24 | local4.getRed() << 16 | local4.getGreen() << 8 | local4.getBlue();
               GuiRenderUtil.strokeRoundedRect(arg, var43Var3025Value, var22Var2925Value, 22.0F, 22.0F, maxValue2, local3 == this.cpBlock ? 2.0F : 1.5F, intVal13, false);
               GuiRenderUtil.fillRoundedRect(arg, var43Var3025Value + 22 - 7, var22Var2925Value + 22 - 7, 6.0F, 6.0F, 3.0F, this.bt(-16777216, floatVal2), false);
               GuiRenderUtil.fillRoundedRect(
                  arg,
                  var43Var3025Value + 22 - 6,
                  var22Var2925Value + 22 - 6,
                  4.0F,
                  4.0F,
                  2.0F,
                  this.bt(0xFF000000 | local4.getRed() << 16 | local4.getGreen() << 8 | local4.getBlue(), floatVal2),
                  false
               );
               if (flag3) {
                  GuiRenderUtil.fillRoundedRect(arg, var43Var3025Value + 1, var22Var2925Value + 1, 5.0F, 5.0F, 2.0F, this.bt(clickGuiModuleValue & 16777215 | -1442840576, floatVal2), false);
               }
            } else if (flag4) {
               GuiRenderUtil.strokeRoundedRect(arg, var43Var3025Value, var22Var2925Value, 22.0F, 22.0F, maxValue2, 1.0F, this.bt(clickGuiModuleValue & 16777215 | 1140850688, floatVal2), false);
            }

            ItemStack local5 = new ItemStack(local3);
            if (!local5.isEmpty()) {
               arg.drawItem(local5, var43Var3025Value + 3, var22Var2925Value + 3);
            }

            if (flag4) {
               nullSnapshot = this.setting.getBlockName(local3) + (flag3 ? " [built-in]" : "");
               var33Snapshot = var43Var3025Value;
               var34Snapshot = var22Var2925Value;
            }
         }
      }

      if (nullSnapshot != null) {
         String var2514Value = nullSnapshot + "  [RMB: color]";
         int intVal14 = this.by(var2514Value) + 10;
         int minValue = Math.min(var33Snapshot, intVal6 + intVal8 - intVal14);
         int var2715Value = var34Snapshot - 15;
         if (var2715Value < intVal7) {
            var2715Value = var34Snapshot + 22 + 2;
         }

         GuiRenderUtil.fillRoundedRect(arg, minValue, var2715Value, intVal14, 13.0F, maxValue2, this.bt(-16117736, floatVal2), false);
         GuiRenderUtil.strokeRoundedRect(arg, minValue, var2715Value, intVal14, 13.0F, maxValue2, 1.0F, this.bt(-15327184, floatVal2), false);
         this.bv(arg, var2514Value, minValue + 5, var2715Value + 2, this.bt(-2234128, floatVal2));
      }

      if (local2.isEmpty()) {
         this.bv(arg, "No blocks", intVal6 + intVal8 / 2 - this.by("No blocks") / 2, intVal7 + intVal9 / 2 - 5, this.bt(-12298906, floatVal2));
      }

      if (this.rescanHoles() > this.bd()) {
         int var43Var232Value = intVal6 + intVal8 + 2;
         float maxValue3 = Math.max(16.0F, (float)(intVal9 * this.bd()) / this.rescanHoles());
         float var22Var24Var48FloatThisVa = intVal7 + (intVal9 - maxValue3) * ((float)this.scroll / Math.max(1, this.rebuildHoleMesh()));
         GuiRenderUtil.fillRoundedRect(arg, var43Var232Value, intVal7, 3.0F, intVal9, 1.5F, this.bt(-15327184, floatVal2), false);
         GuiRenderUtil.fillRoundedRect(arg, var43Var232Value, (int)var22Var24Var48FloatThisVa, 3.0F, (int)maxValue3, 1.5F, this.bt(clickGuiModuleValue & 16777215 | -1442840576, floatVal2), false);
      }

      int var730036Value = intVal4 + 300 - 36;
      arg.fill(intVal3, var730036Value, intVal3 + 320, var730036Value + 1, this.bt(-15327184, floatVal2));
      GuiRenderUtil.fillPerCornerGradient(arg, intVal3, var730036Value, 320.0F, 36.0F, 0.0F, 0.0F, clickGuiModuleValue3, clickGuiModuleValue3, false, this.bt(bu(0, 50), floatVal2));
      int var467Value = var730036Value + 7;
      int var610Value2 = intVal3 + 10;
      boolean flag5 = this.bw(intVal, intVal2, var610Value2, var467Value, 76, 22);
      GuiRenderUtil.fillRoundedRect(arg, var610Value2, var467Value, 76.0F, 22.0F, 11.0F, this.bt(flag5 ? 869875797 : 349782101, floatVal2), false);
      GuiRenderUtil.strokeRoundedRect(arg, var610Value2, var467Value, 76.0F, 22.0F, 11.0F, 1.0F, this.bt(-2539435, floatVal2 * (flag5 ? 0.9F : 0.4F)), false);
      this.bv(arg, "CLEAR ALL", var610Value2 + (76 - this.by("CLEAR ALL")) / 2, var467Value + 7, this.bt(-2539435, floatVal2));
      int var632010661066Value = intVal3 + 320 - 10 - 66 - 10 - 66;
      boolean flag6 = this.bw(intVal, intVal2, var632010661066Value, var467Value, 66, 22);
      GuiRenderUtil.fillRoundedRect(arg, var632010661066Value, var467Value, 66.0F, 22.0F, 11.0F, this.bt(flag6 ? 419430399 : 150994943, floatVal2), false);
      GuiRenderUtil.strokeRoundedRect(arg, var632010661066Value, var467Value, 66.0F, 22.0F, 11.0F, 1.0F, this.bt(-15327184, floatVal2), false);
      this.bv(arg, "CANCEL", var632010661066Value + (66 - this.by("CANCEL")) / 2, var467Value + 7, this.bt(-7824982, floatVal2));
      int var63201066Value = intVal3 + 320 - 10 - 66;
      boolean flag7 = this.bw(intVal, intVal2, var63201066Value, var467Value, 66, 22);
      GuiRenderUtil.fillRoundedRect(arg, var63201066Value, var467Value, 66.0F, 22.0F, 11.0F, this.bt(flag7 ? clickGuiModuleValue : clickGuiModuleValue & 16777215 | 570425344, floatVal2), false);
      GuiRenderUtil.strokeRoundedRect(arg, var63201066Value, var467Value, 66.0F, 22.0F, 11.0F, 1.5F, this.bt(clickGuiModuleValue, floatVal2 * (flag7 ? 1.0F : 0.5F)), false);
      this.bv(arg, "SAVE", var63201066Value + (66 - this.by("SAVE")) / 2, var467Value + 7, this.bt(flag7 ? -16777216 : clickGuiModuleValue, floatVal2));
      if (this.cpBlock != null) {
         this.bz(arg, intVal, intVal2, floatVal2);
      }
   }

   public void bz(DrawContext arg, int intVal, int intVal2, float floatVal) {
      int minValue = Math.min(this.cpX, this.X() + 320 - 140 - 6);
      if (minValue < this.X() + 4) {
         minValue = this.X() + 4;
      }

      int minValue2 = Math.min(this.cpY, this.Y() + 300 - 130 - 6);
      if (minValue2 < this.Y() + 34 + 4) {
         minValue2 = this.Y() + 34 + 4;
      }

      Color local = this.bj(this.cpBlock);
      float floatVal2 = this.bA(local)[0];
      float floatVal3 = this.bA(local)[1];
      float floatVal4 = this.bA(local)[2];
      float clickGuiModuleValue = ClickGuiModule.getBaseCornerRadius();
      int clickGuiModuleValue2 = ClickGuiModule.getAccentColorArgb();
      GuiRenderUtil.fillRoundedRect(arg, minValue - 2, minValue2 - 2, 144.0F, 134.0F, clickGuiModuleValue, this.bt(bu(0, 80), floatVal), false);
      GuiRenderUtil.fillRoundedRect(arg, minValue, minValue2, 140.0F, 130.0F, clickGuiModuleValue, this.bt(ClickGuiModule.getBackgroundColorArgb(), floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, minValue, minValue2, 140.0F, 130.0F, clickGuiModuleValue, 1.0F, this.bt(clickGuiModuleValue2 & 16777215 | -2013265920, floatVal), false);
      int var56Value = minValue + 6;
      int var66Value = minValue2 + 6;
      int var13906Value = var56Value + 90 + 6;
      byte byteVal = 12;
      float floatVal5 = 90.0F / byteVal;
      float floatVal6 = 90.0F / byteVal;

      for (int index = 0; index < byteVal; index++) {
         float floatVal7 = 1.0F - (float)index / byteVal;

         for (int index2 = 0; index2 < byteVal; index2++) {
            float floatVal8 = (float)index2 / byteVal;
            arg.fill(
               (int)(var56Value + index2 * floatVal5),
               (int)(var66Value + index * floatVal6),
               (int)(var56Value + (index2 + 1) * floatVal5),
               (int)(var66Value + (index + 1) * floatVal6),
               0xFF000000 | Color.HSBtoRGB(floatVal2, floatVal8, floatVal7) & 16777215
            );
         }
      }

      GuiRenderUtil.strokeRoundedRect(arg, var56Value, var66Value, 90.0F, 90.0F, 3.0F, 1.0F, this.bt(1157627903, floatVal), false);
      int var13IntVar990Value = var56Value + (int)(floatVal3 * 90.0F);
      int var14Int1Value = var66Value + (int)((1.0F - floatVal4) * 90.0F);
      GuiRenderUtil.fillRoundedRect(arg, var13IntVar990Value - 4, var14Int1Value - 4, 8.0F, 8.0F, 4.0F, this.bt(-2013265920, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, var13IntVar990Value - 4, var14Int1Value - 4, 8.0F, 8.0F, 4.0F, 2.0F, this.bt(-1, floatVal), false);

      for (int index3 = 0; index3 < 90; index3++) {
         float var2790Value = index3 / 90.0F;
         arg.fill(var13906Value, var66Value + index3, var13906Value + 12, var66Value + index3 + 1, 0xFF000000 | Color.HSBtoRGB(var2790Value, 1.0F, 1.0F) & 16777215);
      }

      GuiRenderUtil.strokeRoundedRect(arg, var13906Value, var66Value, 12.0F, 90.0F, 3.0F, 1.0F, this.bt(1157627903, floatVal), false);
      int var14IntVar890Value = var66Value + (int)(floatVal2 * 90.0F);
      GuiRenderUtil.fillRoundedRect(arg, var13906Value - 2, var14IntVar890Value - 1, 16.0F, 3.0F, 1.5F, this.bt(-1, floatVal), false);
      int var14906Value = var66Value + 90 + 6;
      byte byteVal2 = 52;
      int intVal3 = 0xFF000000 | local.getRed() << 16 | local.getGreen() << 8 | local.getBlue();
      GuiRenderUtil.fillRoundedRect(arg, var56Value, var14906Value, byteVal2, 10.0F, 3.0F, this.bt(intVal3, floatVal), false);
      GuiRenderUtil.fillRoundedRect(arg, var56Value + byteVal2 + 4, var14906Value, byteVal2, 10.0F, 3.0F, this.bt(intVal3, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, var56Value, var14906Value, byteVal2, 10.0F, 3.0F, 1.0F, this.bt(1157627903, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, var56Value + byteVal2 + 4, var14906Value, byteVal2, 10.0F, 3.0F, 1.0F, this.bt(1157627903, floatVal), false);
      this.bv(arg, this.setting.getBlockName(this.cpBlock), minValue + 6, minValue2 + 130 - 14, this.bt(-7824982, floatVal));
   }

   public float[] bA(Color color) {
      return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
   }

   public int bB() {
      int minValue = Math.min(this.cpX, this.X() + 320 - 140 - 6);
      if (minValue < this.X() + 4) {
         minValue = this.X() + 4;
      }

      return minValue + 6;
   }

   public int bC() {
      int minValue = Math.min(this.cpY, this.Y() + 300 - 130 - 6);
      if (minValue < this.Y() + 34 + 4) {
         minValue = this.Y() + 34 + 4;
      }

      return minValue + 6;
   }

   public int bD() {
      return this.bB() + 90 + 6;
   }

   public void bE(int intVal, int intVal2) {
      if (this.cpBlock != null) {
         Color local = this.bj(this.cpBlock);
         float maxValue = Math.max(0.0F, Math.min(1.0F, (intVal - this.bB()) / 90.0F));
         float floatVal = 1.0F - Math.max(0.0F, Math.min(1.0F, (intVal2 - this.bC()) / 90.0F));
         int colorValue = Color.HSBtoRGB(this.bA(local)[0], maxValue, floatVal);
         this.bl(this.cpBlock, new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, local.getAlpha()));
      }
   }

   public void bF(int intVal) {
      if (this.cpBlock != null) {
         Color local = this.bj(this.cpBlock);
         float maxValue = Math.max(0.0F, Math.min(1.0F, (intVal - this.bC()) / 90.0F));
         int colorValue = Color.HSBtoRGB(maxValue, this.bA(local)[1], this.bA(local)[2]);
         this.bl(this.cpBlock, new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, local.getAlpha()));
      }
   }

   public boolean mouseClicked(Click arg, boolean flag) {
      int intVal = (int)arg.x();
      int intVal2 = (int)arg.y();
      int var1Value = arg.button();
      int intVal3 = this.X();
      int intVal4 = this.Y();
      if (this.cpBlock != null) {
         int intVal5 = this.bB();
         int intVal6 = this.bC();
         int intVal7 = this.bD();
         if (var1Value == 0) {
            if (this.bw(intVal, intVal2, intVal5, intVal6, 90, 90)) {
               this.bE(intVal, intVal2);
               this.cpDragSV = true;
               return true;
            }

            if (this.bw(intVal, intVal2, intVal7, intVal6, 12, 90)) {
               this.bF(intVal2);
               this.cpDragHue = true;
               return true;
            }
         }

         int minValue = Math.min(this.cpX, intVal3 + 320 - 140 - 6);
         if (minValue < intVal3 + 4) {
            minValue = intVal3 + 4;
         }

         int minValue2 = Math.min(this.cpY, intVal4 + 300 - 130 - 6);
         if (minValue2 < intVal4 + 34 + 4) {
            minValue2 = intVal4 + 34 + 4;
         }

         if (!this.bw(intVal, intVal2, minValue - 2, minValue2 - 2, 144, 134)) {
            this.cpBlock = null;
            this.cpDragSV = false;
            this.cpDragHue = false;
         }

         return true;
      } else {
         int var7344Value = intVal4 + 34 + 4;
         short shortVal = 145;
         int var610Value = intVal3 + 10;
         int var10Var910Value = var610Value + shortVal + 10;
         if (this.bw(intVal, intVal2, var610Value, var7344Value, shortVal, 18)) {
            this.showSel = false;
            this.scroll = 0;
            this.W();
            return true;
         }

         if (this.bw(intVal, intVal2, var10Var910Value, var7344Value, shortVal, 18)) {
            this.showSel = true;
            this.scroll = 0;
            this.W();
            return true;
         }

         if (intVal >= this.Z()
            && intVal < this.Z() + this.rescanBlocks()
            && intVal2 >= this.scanAmethystChunks()
            && intVal2 < this.scanAmethystChunks() + this.clearBlockCache()) {
            List local = this.pruneHoleCache();
            int intVal8 = (intVal - this.Z()) / 25;
            int intVal9 = (intVal2 - this.scanAmethystChunks()) / 25 + this.scroll;
            int var149Var26Value = intVal9 * 9 + intVal8;
            if (intVal8 < 9 && var149Var26Value >= 0 && var149Var26Value < local.size()) {
               Block local2 = (Block)local.get(var149Var26Value);
               boolean flag2 = this.builtinBlocks.contains(local2);
               if (var1Value == 0 && !flag2) {
                  if (this.sel.contains(local2)) {
                     this.sel.remove(local2);
                  } else {
                     this.sel.add(local2);
                     this.bj(local2);
                  }

                  this.scanNearbyStorages(true);
               } else if (var1Value == 1 && this.bn(local2)) {
                  int intVal10 = this.Z() + intVal8 * 25;
                  int intVal11 = this.scanAmethystChunks() + (intVal9 - this.scroll) * 25;
                  this.cpX = intVal10 + 22 + 4;
                  this.cpY = intVal11 - 4;
                  this.cpBlock = local2;
                  this.cpBuiltin = flag2;
                  this.cpDragSV = false;
                  this.cpDragHue = false;
               }
            }

            return true;
         } else {
            int var730036Value = intVal4 + 300 - 36;
            int var127Value = var730036Value + 7;
            if (this.bw(intVal, intVal2, intVal3 + 10, var127Value, 76, 22)) {
               this.sel.clear();
               this.colorMap.clear();
               this.W();
               return true;
            } else if (this.bw(intVal, intVal2, intVal3 + 320 - 10 - 66 - 10 - 66, var127Value, 66, 22)) {
               this.client.setScreen(this.parent);
               return true;
            } else if (this.bw(intVal, intVal2, intVal3 + 320 - 10 - 66, var127Value, 66, 22)) {
               this.bG();
               return true;
            } else {
               return super.mouseClicked(arg, flag);
            }
         }
      }
   }

   public boolean mouseDragged(Click arg, double doubleVal, double doubleVal2) {
      int intVal = (int)arg.x();
      int intVal2 = (int)arg.y();
      if (this.cpDragSV) {
         this.bE(intVal, intVal2);
         return true;
      } else if (this.cpDragHue) {
         this.bF(intVal2);
         return true;
      } else {
         return super.mouseDragged(arg, doubleVal, doubleVal2);
      }
   }

   public boolean mouseReleased(Click arg) {
      this.cpDragSV = false;
      this.cpDragHue = false;
      return super.mouseReleased(arg);
   }

   public boolean mouseScrolled(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4) {
      if (doubleVal >= this.Z()
         && doubleVal < this.Z() + this.rescanBlocks()
         && doubleVal2 >= this.scanAmethystChunks()
         && doubleVal2 < this.scanAmethystChunks() + this.clearBlockCache()) {
         this.scroll = Math.max(0, Math.min(this.rebuildHoleMesh(), this.scroll + (doubleVal4 > 0.0 ? -1 : 1)));
         return true;
      } else {
         return super.mouseScrolled(doubleVal, doubleVal2, doubleVal3, doubleVal4);
      }
   }

   public boolean charTyped(CharInput arg) {
      if (this.cpBlock != null) {
         return true;
      } else {
         String var1Value = arg.asString();
         if (var1Value != null && !var1Value.isEmpty() && !this.showSel) {
            this.search = this.search + var1Value;
            this.W();
            return true;
         } else {
            return super.charTyped(arg);
         }
      }
   }

   public boolean keyPressed(KeyInput arg) {
      if (this.cpBlock != null) {
         if (arg.isEscape()) {
            this.cpBlock = null;
         }

         return true;
      } else if (arg.getKeycode() == 259 && !this.search.isEmpty()) {
         this.search = this.search.substring(0, this.search.length() - 1);
         this.W();
         return true;
      } else if (arg.isEscape()) {
         this.bG();
         return true;
      } else {
         return super.keyPressed(arg);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   public void renderBackground(DrawContext arg, int intVal, int intVal2, float floatVal) {
   }

   public void bx(DrawContext arg, int intVal, int intVal2, int intVal3, int intVal4, String string, boolean flag, boolean flag2, float floatVal, int intVal5) {
      int intVal6 = flag ? this.bt(intVal5 & 16777215 | 436207616, floatVal) : (flag2 ? this.bt(285212671, floatVal) : this.bt(150994943, floatVal));
      int intVal7 = flag ? this.bt(intVal5 & 16777215 | 1426063360, floatVal) : this.bt(-15327184, floatVal);
      int intVal8 = flag ? this.bt(intVal5, floatVal) : this.bt(-7824982, floatVal);
      GuiRenderUtil.fillRoundedRect(arg, intVal, intVal2, intVal3, intVal4, intVal4 / 2.0F, intVal6, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal, intVal2, intVal3, intVal4, intVal4 / 2.0F, 1.0F, intVal7, false);
      this.bv(arg, string, intVal + (intVal3 - this.by(string)) / 2, intVal2 + intVal4 / 2 - 5, intVal8);
   }

   public void bv(DrawContext arg, String string, int intVal, int intVal2, int intVal3) {
      CustomFontManager.INSTANCE7.drawText(arg, string, intVal, intVal2, intVal3);
   }

   public int by(String string) {
      return CustomFontManager.INSTANCE7.getStringWidth(string);
   }

   public int bt(int intVal, float floatVal) {
      return Math.max(0, Math.min(255, (int)((intVal >> 24 & 0xFF) * floatVal))) << 24 | intVal & 16777215;
   }

   public static int bu(int intVal, int intVal2) {
      return Math.max(0, Math.min(255, intVal2)) << 24 | intVal & 16777215;
   }

   public float bo(float floatVal) {
      return 1.0F - (float)Math.pow(1.0F - Math.min(1.0F, floatVal), 3.0);
   }

   public boolean bw(int intVal, int intVal2, int intVal3, int intVal4, int intVal5, int intVal6) {
      return intVal >= intVal3 && intVal <= intVal3 + intVal5 && intVal2 >= intVal4 && intVal2 <= intVal4 + intVal6;
   }

   public void bG() {
      this.setting.setBlocks(new LinkedHashSet(this.sel));
      if (this.colorSaveCallback != null) {
         this.colorSaveCallback.accept(new LinkedHashMap(this.colorMap));
      }

      if (this.module instanceof StorageEspModule local) {
         for (Entry entry : (Iterable<Entry>)this.builtinColors.entrySet()) {
            local.setBlockColor((Block)entry.getKey(), (Color)entry.getValue());
         }
      }

      ConfigManager.INSTANCE.save();
      this.client.setScreen(this.parent);
   }

}
