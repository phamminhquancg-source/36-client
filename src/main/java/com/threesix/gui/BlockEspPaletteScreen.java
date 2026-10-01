package com.threesix.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.Registries;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;
import com.threesix.util.StringVaultDecoder;
import com.threesix.module.BlockEspModule;

public class BlockEspPaletteScreen extends Screen {
   public static final int W = 480;
   public static final int H = 360;
   public static final int SIDEBAR_W = 150;
   public static final int PAD = 10;
   public static final int HEADER_H = 36;
   public static final int FOOTER_H = 40;
   public static final int CELL = 24;
   public static final int CELL_GAP = 3;
   public static final int COLS = 10;
   public static final int SEARCH_H = 22;
   public static final int TAB_H = 24;
   public static final int CP_SV = 100;
   public static final int CP_HUE_W = 14;
   public static final int CP_GAP = 6;
   public static final int CP_W = 128;
   public static final int CP_H = 136;
   public static final int C_BG = -267908078;
   public static final int C_SIDEBAR = -871558889;
   public static final int C_GRID_BG = -872019441;
   public static final int C_HEADER = -301330922;
   public static final int C_CELL_BG = -15657440;
   public static final int C_CELL_HOV = -15065040;
   public static final int C_BORDER = 872415231;
   public static final int C_TEXT = -2235153;
   public static final int C_MUTED = -11905688;
   public static final int C_ACCENT = -10754352;
   public static final int C_ACCENT2 = -12860240;
   public static final int C_RED = -2076576;
   public static final int C_SEARCH_BG = -15920096;
   public final BlockEspModule module;
   public final Screen parent;
   public final Set selectedBlocks = new LinkedHashSet();
   public final Map blockColors = new LinkedHashMap();
   public final List allBlocks = new ArrayList();
   public List filteredBlocks = new ArrayList();
   public String searchQuery = "";
   public boolean showSelected = false;
   public int scrollOffset = 0;
   public Block cpBlock = null;
   public int cpX;
   public int cpY;
   public boolean cpDragSV = false;
   public boolean cpDragHue = false;
   public boolean cpDragAlpha = false;
   public float fadeIn = 0.0F;
   public long openNanos = 0L;
   public int hoverIdx = -1;

   public BlockEspPaletteScreen(BlockEspModule blockEspModule, Screen arg) {
      super(Text.literal(""));
      this.module = blockEspModule;
      this.parent = arg;
      this.selectedBlocks.addAll(blockEspModule.getSelectedBlocks());
      Registries.BLOCK.forEach(entry -> {

         if (entry != Blocks.AIR && !new ItemStack(entry).isEmpty()) {
            this.allBlocks.add(entry);
         }
      });
      this.allBlocks.sort(Comparator.comparing(item -> {
         try {
            return ((Block)item).getName().getString();
         } catch (Exception error) {
            return "";
         }
      }));
      Map local = blockEspModule.getBlockColors();

      for (Block class2248 : (Iterable<Block>)(Object)this.selectedBlocks) {
         this.blockColors.put(class2248, local.getOrDefault(class2248, this.n()));
      }

      this.o();
   }

   public void o() {

      this.p(false);
   }

   public void p(boolean flag) {

      List local = this.showSelected ? new ArrayList(this.selectedBlocks) : this.allBlocks;
      if (this.searchQuery.isBlank()) {
         this.filteredBlocks = new ArrayList(local);
      } else {
         String local2 = this.searchQuery.trim().toLowerCase();
         this.filteredBlocks = new ArrayList();

         for (Block class2248 : (Iterable<Block>)(Object)local) {
            try {
               if (class2248.getName().getString().toLowerCase().contains(local2)) {
                  this.filteredBlocks.add(class2248);
               }
            } catch (Exception error) {
            }
         }
      }

      if (!flag) {
         this.scrollOffset = 0;
      } else {
         this.scrollOffset = Math.max(0, Math.min(this.q(), this.scrollOffset));
      }
   }

   public int r() {
      return (this.width - 480) / 2;
   }

   public int s() {
      return (this.height - 360) / 2;
   }

   public int t() {
      return 10;
   }

   public int u() {

      return this.r() + 150 + 10;
   }

   public int v() {
      return this.s() + 36 + 24 + 22 + 8;
   }

   public int w() {

      return 310;
   }

   public int x() {
      return 222;
   }

   public int y() {

      return (int)Math.floor(this.x() / 27.0);
   }

   public int z() {
      return (int)Math.ceil(this.filteredBlocks.size() / 10.0);
   }

   public int q() {
      return Math.max(0, this.z() - this.y());
   }

   public Color A(Block arg) {
      return (Color)this.blockColors.computeIfAbsent(arg, item -> {
         Map local = this.module.getBlockColors();
         return local.getOrDefault(item, this.n());
      });
   }

   public void render(DrawContext arg, int intVal, int intVal2, float floatVal) {

      if (this.openNanos == 0L) {
         this.openNanos = System.nanoTime();
      }

      float floatVal2 = (float)(System.nanoTime() - this.openNanos) / 1.0E9F;
      this.fadeIn = Math.min(1.0F, floatVal2 / 0.18F);
      float floatVal3 = this.C(this.fadeIn);
      int intVal3 = this.r();
      int intVal4 = this.s();
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, 480.0F, 360.0F, 12.0F, this.D(-267908078, floatVal3), false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal3, intVal4, 480.0F, 360.0F, 12.0F, 1.0F, this.D(872415231, floatVal3), false);
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, 150.0F, 360.0F, 12.0F, this.D(-871558889, floatVal3), false);
      GuiRenderUtil.fillRoundedRect(arg, intVal3 + 150 - 1, intVal4, 1.0F, 360.0F, 0.0F, this.D(587202559, floatVal3), false);
      GuiRenderUtil.fillPerCornerGradient(arg, intVal3, intVal4, 480.0F, 36.0F, 12.0F, 12.0F, 0.0F, 0.0F, false, this.D(-301330922, floatVal3));
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, 3.0F, 36.0F, 1.5F, this.D(-10754352, floatVal3), false);
      arg.drawText(this.textRenderer, Text.literal("BLOCK ESP"), intVal3 + 10 + 8, intVal4 + 10, this.D(-2235153, floatVal3), false);
      String local = this.selectedBlocks.size() + " selected";
      arg.drawText(this.textRenderer, local, intVal3 + 10 + 8, intVal4 + 22, this.D(-11905688, floatVal3), false);
      this.H(arg, intVal3, intVal4, intVal, intVal2, floatVal3);
      int var7150Value = intVal3 + 150;
      short shortVal = 330;
      int var836Value = intVal4 + 36;
      int intVal5 = (shortVal - 30) / 2;
      int var1010Value = var7150Value + 10;
      int var14Var1310Value = var1010Value + intVal5 + 10;
      this.I(arg, var1010Value, var836Value + 4, intVal5, 16, "ALL BLOCKS", !this.showSelected, floatVal3);
      this.I(arg, var14Var1310Value, var836Value + 4, intVal5, 16, "SELECTED (" + this.selectedBlocks.size() + ")", this.showSelected, floatVal3);
      int var83624Value = intVal4 + 36 + 24;
      int var1010Value2 = var7150Value + 10;
      int var1120Value = shortVal - 20;
      GuiRenderUtil.fillRoundedRect(arg, var1010Value2, var83624Value + 2, var1120Value, 18.0F, 6.0F, this.D(-15920096, floatVal3), false);
      GuiRenderUtil.strokeRoundedRect(arg, var1010Value2, var83624Value + 2, var1120Value, 18.0F, 6.0F, 1.0F, this.D(872415231, floatVal3), false);
      String local2 = this.searchQuery.isEmpty() ? "⌕ search" : "⌕ " + this.searchQuery + "_";
      int intVal6 = this.searchQuery.isEmpty() ? this.D(-11905688, floatVal3) : this.D(-2235153, floatVal3);
      arg.drawText(this.textRenderer, local2, var1010Value2 + 8, var83624Value + 9 - 3, intVal6, false);
      GuiRenderUtil.fillRoundedRect(arg, this.u() - 4, this.v() - 4, this.w() + 8, this.x() + 8, 8.0F, this.D(-872019441, floatVal3), false);
      this.J(arg, intVal, intVal2, floatVal3);
      this.K(arg, floatVal3);
      this.L(arg, intVal3, intVal4, intVal, intVal2, floatVal3);
      if (this.cpBlock != null) {
         this.M(arg, intVal, intVal2, floatVal3);
      }
   }

   public void H(DrawContext arg, int intVal, int intVal2, int intVal3, int intVal4, float floatVal) {
      int var210Value = intVal + 10;
      int var33612Value = intVal2 + 36 + 12;
      arg.drawText(this.textRenderer, Text.literal("SETTINGS"), var210Value, var33612Value, this.D(-10754352, floatVal), false);
      var33612Value += 18;
      arg.fill(var210Value, var33612Value, intVal + 150 - 10, var33612Value + 1, this.D(587202559, floatVal));
      var33612Value += 8;
      this.N(arg, var210Value, var33612Value, 130, "Tracers", this.module.isTracersEnabled(), floatVal);
      var33612Value += 26;
      this.N(arg, var210Value, var33612Value, 130, "Notify", this.module.isNotifyEnabled(), floatVal);
   }

   public void N(DrawContext arg, int intVal, int intVal2, int intVal3, String string, boolean flag, float floatVal) {
      int var2Var422Value = intVal + intVal3 - 22;
      int intVal4 = flag ? this.D(-10754352, floatVal * 0.8F) : this.D(-14011323, floatVal);
      int intVal5 = flag ? var2Var422Value + 12 : var2Var422Value + 2;
      GuiRenderUtil.fillRoundedRect(arg, var2Var422Value, intVal2 + 2, 20.0F, 8.0F, 4.0F, intVal4, false);
      GuiRenderUtil.fillRoundedRect(arg, intVal5, intVal2 + 3, 6.0F, 6.0F, 3.0F, flag ? this.D(-2235153, floatVal) : this.D(-11905688, floatVal), false);
      arg.drawText(this.textRenderer, string, intVal, intVal2 + 2, this.D(flag ? -2235153 : -11905688, floatVal), false);
   }

   public void I(DrawContext arg, int intVal, int intVal2, int intVal3, int intVal4, String string, boolean flag, float floatVal) {
      int intVal5 = flag ? this.D(-10754352, floatVal * 0.18F) : this.D(301989887, floatVal);
      int intVal6 = flag ? this.D(-10754352, floatVal * 0.7F) : this.D(872415231, floatVal);
      int intVal7 = flag ? this.D(-10754352, floatVal) : this.D(-11905688, floatVal);
      GuiRenderUtil.fillRoundedRect(arg, intVal, intVal2, intVal3, intVal4, 5.0F, intVal5, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal, intVal2, intVal3, intVal4, 5.0F, 1.0F, intVal6, false);
      arg.drawText(this.textRenderer, string, intVal + (intVal3 - this.textRenderer.getWidth(string)) / 2, intVal2 + intVal4 / 2 - 4, intVal7, false);
   }

   public void J(DrawContext arg, int intVal, int intVal2, float floatVal) {
      int intVal3 = this.u();
      int intVal4 = this.v();
      int scrollOffsetSnapshot = this.scrollOffset;
      int minValue = Math.min(scrollOffsetSnapshot + this.y() + 1, this.z());
      this.hoverIdx = -1;

      for (int index = scrollOffsetSnapshot; index < minValue; index++) {
         for (int index2 = 0; index2 < 10; index2++) {
            int var910Var10Value = index * 10 + index2;
            if (var910Var10Value >= this.filteredBlocks.size()) {
               break;
            }

            Block local = (Block)this.filteredBlocks.get(var910Var10Value);
            int var5Var1027Value = intVal3 + index2 * 27;
            int var6Var9Var727Value = intVal4 + (index - scrollOffsetSnapshot) * 27;
            if (var6Var9Var727Value + 24 >= intVal4 && var6Var9Var727Value <= intVal4 + this.x()) {
               boolean flag = this.selectedBlocks.contains(local);
               boolean flag2 = intVal >= var5Var1027Value && intVal < var5Var1027Value + 24 && intVal2 >= var6Var9Var727Value && intVal2 < var6Var9Var727Value + 24;
               if (flag2) {
                  this.hoverIdx = var910Var10Value;
               }

               int intVal5 = flag ? this.D(-15065040, floatVal) : (flag2 ? this.D(-15065040, floatVal) : this.D(-15657440, floatVal));
               GuiRenderUtil.fillRoundedRect(arg, var5Var1027Value, var6Var9Var727Value, 24.0F, 24.0F, 5.0F, intVal5, false);
               if (flag) {
                  Color local2 = this.A(local);
                  int intVal6 = 838860800 | local2.getRed() << 16 | local2.getGreen() << 8 | local2.getBlue();
                  GuiRenderUtil.fillRoundedRect(arg, var5Var1027Value, var6Var9Var727Value, 24.0F, 24.0F, 5.0F, intVal6, false);
                  int intVal7 = 0xFF000000 | local2.getRed() << 16 | local2.getGreen() << 8 | local2.getBlue();
                  GuiRenderUtil.strokeRoundedRect(arg, var5Var1027Value, var6Var9Var727Value, 24.0F, 24.0F, 5.0F, local == this.cpBlock ? 2.5F : 1.5F, intVal7, false);
               } else if (flag2) {
                  GuiRenderUtil.strokeRoundedRect(arg, var5Var1027Value, var6Var9Var727Value, 24.0F, 24.0F, 5.0F, 1.0F, this.D(1728053247, floatVal), false);
               }

               ItemStack local3 = new ItemStack(local);
               if (!local3.isEmpty()) {
                  float floatVal2 = 1.0F;
                  int intVal8 = (24 - (int)(16.0F * floatVal2)) / 2;
                  arg.getMatrices().pushMatrix();
                  arg.getMatrices().translate(var5Var1027Value + intVal8, var6Var9Var727Value + intVal8);
                  arg.getMatrices().scale(floatVal2, floatVal2);
                  arg.drawItem(local3, 0, 0);
                  arg.getMatrices().popMatrix();
               }

               if (flag) {
                  Color local4 = this.A(local);
                  int intVal9 = 0xFF000000 | local4.getRed() << 16 | local4.getGreen() << 8 | local4.getBlue();
                  GuiRenderUtil.fillRoundedRect(arg, var5Var1027Value + 24 - 8, var6Var9Var727Value + 24 - 8, 6.0F, 6.0F, 3.0F, -16777216, false);
                  GuiRenderUtil.fillRoundedRect(arg, var5Var1027Value + 24 - 7, var6Var9Var727Value + 24 - 7, 4.0F, 4.0F, 2.0F, intVal9, false);
               }
            }
         }
      }
   }

   public void K(DrawContext arg, float floatVal) {

      if (this.z() > this.y()) {
         int intVal = this.u() + this.w() + 6;
         int intVal2 = this.v();
         int intVal3 = this.x();
         float maxValue = Math.max(20.0F, intVal3 * ((float)this.y() / this.z()));
         float var4Var5Var6FloatThisValue = intVal2 + (intVal3 - maxValue) * ((float)this.scrollOffset / Math.max(1, this.q()));
         GuiRenderUtil.fillRoundedRect(arg, intVal, intVal2, 3.0F, intVal3, 1.5F, this.D(587202559, floatVal), false);
         GuiRenderUtil.fillRoundedRect(arg, intVal, (int)var4Var5Var6FloatThisValue, 3.0F, (int)maxValue, 1.5F, this.D(-10754352, floatVal), false);
      }
   }

   public void L(DrawContext arg, int intVal, int intVal2, int intVal3, int intVal4, float floatVal) {
      int var336040Value = intVal2 + 360 - 40;
      GuiRenderUtil.fillPerCornerGradient(arg, intVal, var336040Value, 480.0F, 40.0F, 0.0F, 0.0F, 12.0F, 12.0F, false, this.D(-301330922, floatVal));
      arg.fill(intVal, var336040Value, intVal + 480, var336040Value + 1, this.D(587202559, floatVal));
      int var77Value = var336040Value + 7;
      int var210Value = intVal + 10;
      boolean flag = this.O(intVal3, intVal4, var210Value, var77Value, 90, 26);
      GuiRenderUtil.fillRoundedRect(arg, var210Value, var77Value, 90.0F, 26.0F, 5.0F, flag ? this.D(-2076576, floatVal) : this.D(870338656, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, var210Value, var77Value, 90.0F, 26.0F, 5.0F, 1.0F, this.D(-2076576, floatVal * 0.6F), false);
      this.P(arg, "CLEAR ALL", var210Value, var77Value, 90, 26, this.D(-2235153, floatVal));
      int var248010801080Value = intVal + 480 - 10 - 80 - 10 - 80;
      boolean flag2 = this.O(intVal3, intVal4, var248010801080Value, var77Value, 80, 26);
      GuiRenderUtil.fillRoundedRect(arg, var248010801080Value, var77Value, 80.0F, 26.0F, 5.0F, flag2 ? this.D(872415231, floatVal) : this.D(301989887, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, var248010801080Value, var77Value, 80.0F, 26.0F, 5.0F, 1.0F, this.D(872415231, floatVal), false);
      this.P(arg, "CANCEL", var248010801080Value, var77Value, 80, 26, this.D(-2235153, floatVal));
      int var24801080Value = intVal + 480 - 10 - 80;
      boolean flag3 = this.O(intVal3, intVal4, var24801080Value, var77Value, 80, 26);
      GuiRenderUtil.fillRoundedRect(arg, var24801080Value, var77Value, 80.0F, 26.0F, 5.0F, flag3 ? this.D(-10754352, floatVal) : this.D(872415231, floatVal * 0.8F), false);
      GuiRenderUtil.strokeRoundedRect(arg, var24801080Value, var77Value, 80.0F, 26.0F, 5.0F, 1.0F, this.D(-10754352, floatVal * 0.8F), false);
      this.P(arg, "SAVE", var24801080Value, var77Value, 80, 26, this.D(flag3 ? -16777216 : -10754352, floatVal));
   }

   public void M(DrawContext arg, int intVal, int intVal2, float floatVal) {
      int cpXSnapshot = this.cpX;
      int cpYSnapshot = this.cpY;
      if (cpXSnapshot + 128 + 8 > this.r() + 480) {
         cpXSnapshot = this.r() + 480 - 128 - 8;
      }

      if (cpYSnapshot + 136 + 8 > this.s() + 360) {
         cpYSnapshot = this.s() + 360 - 136 - 8;
      }

      Color local = this.A(this.cpBlock);
      float floatVal2 = this.Q(local)[0];
      float floatVal3 = this.Q(local)[1];
      float floatVal4 = this.Q(local)[2];
      GuiRenderUtil.fillRoundedRect(arg, cpXSnapshot - 4, cpYSnapshot - 4, 144.0F, 152.0F, 8.0F, this.D(-16381424, floatVal), false);
      GuiRenderUtil.fillRoundedRect(arg, cpXSnapshot, cpYSnapshot, 136.0F, 144.0F, 6.0F, this.D(-15919840, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, cpXSnapshot, cpYSnapshot, 136.0F, 144.0F, 6.0F, 1.0F, this.D(-10754352, floatVal * 0.5F), false);
      int var54Value = cpXSnapshot + 4;
      int var64Value = cpYSnapshot + 4;
      int var111006Value = var54Value + 100 + 6;
      byte byteVal = 14;
      float floatVal5 = 100.0F / byteVal;
      float floatVal6 = 100.0F / byteVal;

      for (int index = 0; index < byteVal; index++) {
         float floatVal7 = 1.0F - (float)index / byteVal;

         for (int index2 = 0; index2 < byteVal; index2++) {
            float floatVal8 = (float)index2 / byteVal;
            arg.fill(
               (int)(var54Value + index2 * floatVal5),
               (int)(var64Value + index * floatVal6),
               (int)(var54Value + (index2 + 1) * floatVal5),
               (int)(var64Value + (index + 1) * floatVal6),
               0xFF000000 | Color.HSBtoRGB(floatVal2, floatVal8, floatVal7) & 16777215
            );
         }
      }

      GuiRenderUtil.strokeRoundedRect(arg, var54Value, var64Value, 100.0F, 100.0F, 2.0F, 1.0F, 1157627903, false);
      int var11IntVar9100Value = var54Value + (int)(floatVal3 * 100.0F);
      int var12Int1Value = var64Value + (int)((1.0F - floatVal4) * 100.0F);
      GuiRenderUtil.fillRoundedRect(arg, var11IntVar9100Value - 4, var12Int1Value - 4, 8.0F, 8.0F, 4.0F, -2013265920, false);
      GuiRenderUtil.strokeRoundedRect(arg, var11IntVar9100Value - 4, var12Int1Value - 4, 8.0F, 8.0F, 4.0F, 2.0F, -1, false);

      for (int index3 = 0; index3 < 100; index3++) {
         float var27100Value = index3 / 100.0F;
         arg.fill(var111006Value, var64Value + index3, var111006Value + 14, var64Value + index3 + 1, 0xFF000000 | Color.HSBtoRGB(var27100Value, 1.0F, 1.0F) & 16777215);
      }

      GuiRenderUtil.strokeRoundedRect(arg, var111006Value, var64Value, 14.0F, 100.0F, 2.0F, 1.0F, 1157627903, false);
      int var12IntVar8100Value = var64Value + (int)(floatVal2 * 100.0F);
      GuiRenderUtil.fillRoundedRect(arg, var111006Value - 2, var12IntVar8100Value - 1, 18.0F, 3.0F, 1.5F, -1, false);
      int var121006Value = var64Value + 100 + 6;
      byte byteVal2 = 120;

      for (int index4 = 0; index4 < byteVal2; index4++) {
         float floatVal9 = (float)index4 / byteVal2;
         int colorValue = Color.HSBtoRGB(floatVal2, floatVal3, floatVal4) & 16777215;
         arg.fill(var54Value + index4, var121006Value, var54Value + index4 + 1, var121006Value + 8, (int)(floatVal9 * 255.0F) << 24 | colorValue);
      }

      GuiRenderUtil.strokeRoundedRect(arg, var54Value, var121006Value, byteVal2, 8.0F, 2.0F, 1.0F, 1157627903, false);
      int var11IntVar7Value = var54Value + (int)(local.getAlpha() / 255.0F * byteVal2);
      GuiRenderUtil.fillRoundedRect(arg, var11IntVar7Value - 2, var121006Value - 2, 4.0F, 12.0F, 2.0F, -1, false);
      int var3012Value = var121006Value + 12;
      int intVal3 = 0xFF000000 | local.getRed() << 16 | local.getGreen() << 8 | local.getBlue();
      GuiRenderUtil.fillRoundedRect(arg, var54Value, var3012Value, byteVal2, 6.0F, 2.0F, intVal3, false);
      GuiRenderUtil.strokeRoundedRect(arg, var54Value, var3012Value, byteVal2, 6.0F, 2.0F, 1.0F, 872415231, false);
   }

   public boolean mouseClicked(Click arg, boolean flag) {
      int intVal = (int)arg.x();
      int intVal2 = (int)arg.y();
      int var1Value = arg.button();
      int intVal3 = this.r();
      int intVal4 = this.s();
      if (this.cpBlock != null) {
         int cpXSnapshot = this.cpX;
         int cpYSnapshot = this.cpY;
         if (cpXSnapshot + 128 + 8 > intVal3 + 480) {
            cpXSnapshot = intVal3 + 480 - 128 - 8;
         }

         if (cpYSnapshot + 136 + 8 > intVal4 + 360) {
            cpYSnapshot = intVal4 + 360 - 136 - 8;
         }

         int var264Value = cpXSnapshot + 4;
         int var274Value = cpYSnapshot + 4;
         int var281006Value = var264Value + 100 + 6;
         int var291006Value = var274Value + 100 + 6;
         byte byteVal = 120;
         if (intVal >= var264Value && intVal < var264Value + 100 && intVal2 >= var274Value && intVal2 < var274Value + 100) {
            this.R(intVal, intVal2, var264Value, var274Value);
            this.cpDragSV = true;
            return true;
         }

         if (intVal >= var281006Value && intVal < var281006Value + 14 && intVal2 >= var274Value && intVal2 < var274Value + 100) {
            this.S(intVal2, var274Value);
            this.cpDragHue = true;
            return true;
         }

         if (intVal >= var264Value && intVal < var264Value + byteVal && intVal2 >= var291006Value && intVal2 < var291006Value + 8) {
            this.T(intVal, var264Value, byteVal);
            this.cpDragAlpha = true;
            return true;
         }

         if (intVal < cpXSnapshot - 4 || intVal > cpXSnapshot + 128 + 12 || intVal2 < cpYSnapshot - 4 || intVal2 > cpYSnapshot + 136 + 12) {
            this.cpBlock = null;
         }

         return true;
      } else {
         int var610Value = intVal3 + 10;
         short shortVal = 130;
         int var73638Value = intVal4 + 36 + 38;
         int var73664Value = intVal4 + 36 + 64;
         if (intVal2 >= var73638Value && intVal2 <= var73638Value + 14 && intVal >= var610Value && intVal <= var610Value + shortVal) {
            this.module.setTracersEnabled(!this.module.isTracersEnabled());
            return true;
         }

         if (intVal2 >= var73664Value && intVal2 <= var73664Value + 14 && intVal >= var610Value && intVal <= var610Value + shortVal) {
            this.module.setNotifyEnabled(!this.module.isNotifyEnabled());
            return true;
         }

         int var6150Value = intVal3 + 150;
         short shortVal2 = 330;
         int var736Value = intVal4 + 36;
         int intVal5 = (shortVal2 - 30) / 2;
         int var1210Value = var6150Value + 10;
         int var16Var1510Value = var1210Value + intVal5 + 10;
         if (intVal2 >= var736Value + 4 && intVal2 <= var736Value + 24 - 4) {
            if (intVal >= var1210Value && intVal <= var1210Value + intVal5) {
               this.showSelected = false;
               this.o();
               return true;
            }

            if (intVal >= var16Var1510Value && intVal <= var16Var1510Value + intVal5) {
               this.showSelected = true;
               this.o();
               return true;
            }
         }

         int intVal6 = this.u();
         int intVal7 = this.v();
         if (intVal >= intVal6 && intVal < intVal6 + this.w() && intVal2 >= intVal7 && intVal2 < intVal7 + this.x()) {
            int intVal8 = (intVal - intVal6) / 27;
            int intVal9 = (intVal2 - intVal7) / 27 + this.scrollOffset;
            int var2110Var20Value = intVal9 * 10 + intVal8;
            if (intVal8 < 10 && var2110Var20Value >= 0 && var2110Var20Value < this.filteredBlocks.size()) {
               Block local = (Block)this.filteredBlocks.get(var2110Var20Value);
               if (var1Value == 0) {
                  if (this.selectedBlocks.contains(local)) {
                     this.selectedBlocks.remove(local);
                  } else {
                     this.selectedBlocks.add(local);
                     this.blockColors.putIfAbsent(local, this.n());
                  }

                  this.p(true);
               } else if (var1Value == 1) {
                  if (!this.selectedBlocks.contains(local)) {
                     this.selectedBlocks.add(local);
                     this.blockColors.putIfAbsent(local, this.n());
                     this.p(true);
                  }

                  int var18Var2027Value = intVal6 + intVal8 * 27;
                  int var19Var21ThisValue = intVal7 + (intVal9 - this.scrollOffset) * 27;
                  this.cpX = var18Var2027Value + 24 + 6;
                  this.cpY = var19Var21ThisValue - 4;
                  this.cpBlock = local;
               }

               return true;
            }
         }

         int var736040Value = intVal4 + 360 - 40;
         int var337Value = var736040Value + 7;
         if (intVal2 >= var337Value && intVal2 <= var337Value + 26) {
            int var610Value2 = intVal3 + 10;
            int var648010801080Value = intVal3 + 480 - 10 - 80 - 10 - 80;
            int var64801080Value = intVal3 + 480 - 10 - 80;
            if (this.O(intVal, intVal2, var610Value2, var337Value, 90, 26)) {
               this.selectedBlocks.clear();
               this.blockColors.clear();
               this.o();
               return true;
            }

            if (this.O(intVal, intVal2, var648010801080Value, var337Value, 80, 26)) {
               this.client.setScreen(this.parent);
               return true;
            }

            if (this.O(intVal, intVal2, var64801080Value, var337Value, 80, 26)) {
               this.U();
               return true;
            }
         }

         return super.mouseClicked(arg, flag);
      }
   }

   public boolean mouseDragged(Click arg, double doubleVal, double doubleVal2) {

      int intVal = (int)arg.x();
      int intVal2 = (int)arg.y();
      if (this.cpBlock != null) {
         int cpXSnapshot = this.cpX;
         int cpYSnapshot = this.cpY;
         if (cpXSnapshot + 128 + 8 > this.r() + 480) {
            cpXSnapshot = this.r() + 480 - 128 - 8;
         }

         if (cpYSnapshot + 136 + 8 > this.s() + 360) {
            cpYSnapshot = this.s() + 360 - 136 - 8;
         }

         int var84Value = cpXSnapshot + 4;
         int var94Value = cpYSnapshot + 4;
         byte byteVal = 120;
         if (this.cpDragSV) {
            this.R(intVal, intVal2, var84Value, var94Value);
            return true;
         }

         if (this.cpDragHue) {
            this.S(intVal2, var94Value);
            return true;
         }

         if (this.cpDragAlpha) {
            this.T(intVal, var84Value, byteVal);
            return true;
         }
      }

      return super.mouseDragged(arg, doubleVal, doubleVal2);
   }

   public boolean mouseReleased(Click arg) {

      this.cpDragSV = false;
      this.cpDragHue = false;
      this.cpDragAlpha = false;
      return super.mouseReleased(arg);
   }

   public boolean mouseScrolled(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4) {
      int intVal = this.u();
      int intVal2 = this.v();
      if (doubleVal >= intVal && doubleVal < intVal + this.w() && doubleVal2 >= intVal2 && doubleVal2 < intVal2 + this.x()) {
         this.scrollOffset = Math.max(0, Math.min(this.q(), this.scrollOffset + (doubleVal4 > 0.0 ? -1 : 1)));
         return true;
      } else {
         return super.mouseScrolled(doubleVal, doubleVal2, doubleVal3, doubleVal4);
      }
   }

   public boolean charTyped(CharInput arg) {
      String var1Value = arg.asString();
      if (var1Value != null && !var1Value.isEmpty()) {
         this.searchQuery = this.searchQuery + var1Value;
         this.o();
         return true;
      } else {
         return super.charTyped(arg);
      }
   }

   public boolean keyPressed(KeyInput arg) {
      if (arg.getKeycode() == 259 && !this.searchQuery.isEmpty()) {
         this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
         this.o();
         return true;
      }

      if (arg.isEscape()) {
         if (this.cpBlock != null) {
            this.cpBlock = null;
            return true;
         } else {
            this.U();
            return true;
         }
      } else {
         return super.keyPressed(arg);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   public void renderBackground(DrawContext arg, int intVal, int intVal2, float floatVal) {
   }

   public void R(int intVal, int intVal2, int intVal3, int intVal4) {

      if (this.cpBlock != null) {
         Color local = this.A(this.cpBlock);
         float maxValue = Math.max(0.0F, Math.min(1.0F, (intVal - intVal3) / 100.0F));
         float floatVal = 1.0F - Math.max(0.0F, Math.min(1.0F, (intVal2 - intVal4) / 100.0F));
         int colorValue = Color.HSBtoRGB(this.Q(local)[0], maxValue, floatVal);
         this.blockColors.put(this.cpBlock, new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, local.getAlpha()));
      }
   }

   public void S(int intVal, int intVal2) {
      if (this.cpBlock != null) {
         Color local = this.A(this.cpBlock);
         float maxValue = Math.max(0.0F, Math.min(1.0F, (intVal - intVal2) / 100.0F));
         int colorValue = Color.HSBtoRGB(maxValue, this.Q(local)[1], this.Q(local)[2]);
         this.blockColors.put(this.cpBlock, new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, local.getAlpha()));
      }
   }

   public void T(int intVal, int intVal2, int intVal3) {
      if (this.cpBlock != null) {
         Color local = this.A(this.cpBlock);
         int intVal4 = (int)(Math.max(0.0F, Math.min(1.0F, (float)(intVal - intVal2) / intVal3)) * 255.0F);
         this.blockColors.put(this.cpBlock, new Color(local.getRed(), local.getGreen(), local.getBlue(), intVal4));
      }
   }

   public float[] Q(Color color) {
      return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
   }

   public Color n() {
      int colorValue = Color.HSBtoRGB((float)Math.random(), 0.8F, 1.0F);
      return new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, 180);
   }

   public int D(int intVal, float floatVal) {

      int maxValue = Math.max(0, Math.min(255, (int)((intVal >> 24 & 0xFF) * floatVal)));
      return maxValue << 24 | intVal & 16777215;
   }

   public float C(float floatVal) {

      return 1.0F - (float)Math.pow(1.0F - Math.min(1.0F, floatVal), 3.0);
   }

   public boolean O(int intVal, int intVal2, int intVal3, int intVal4, int intVal5, int intVal6) {
      return intVal >= intVal3 && intVal <= intVal3 + intVal5 && intVal2 >= intVal4 && intVal2 <= intVal4 + intVal6;
   }

   public void P(DrawContext arg, String string, int intVal, int intVal2, int intVal3, int intVal4, int intVal5) {
      arg.drawText(this.textRenderer, string, intVal + (intVal3 - this.textRenderer.getWidth(string)) / 2, intVal2 + (intVal4 - 8) / 2, intVal5, false);
   }

   public void U() {

      this.module.setSelectedBlocks(this.selectedBlocks);
      this.module.setBlockColors(this.blockColors);
      ConfigManager.INSTANCE.save();
      this.client.setScreen(this.parent);
   }

}
