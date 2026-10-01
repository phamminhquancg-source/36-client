package com.threesix.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import com.threesix.gui.BlockColorPickerScreen;
import com.threesix.setting.EntityListSetting;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.data.BlockPickerGuiEntry;
import com.threesix.internal.ModuleBase;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ItemPickerGuiEntry;
import com.threesix.util.GuiRenderUtil;
import com.threesix.gui.ConfigManagerScreen;
import com.threesix.data.ColorDragMode;
import com.threesix.render.ScreenBlurPipeline;
import com.threesix.module.ConfigShareModule;
import com.threesix.setting.BlockListSetting;
import com.threesix.manager.CustomFontManager;
import com.threesix.gui.ChatMacrosScreen;
import com.threesix.setting.ItemSelectSetting;
import com.threesix.setting.MultiSelectSetting;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.SettingLayoutPair;
import com.threesix.data.GuiSettingRect;
import com.threesix.module.StorageEspModule;
import com.threesix.module.BlockEspModule;
import com.threesix.setting.ModeSetting02;
import com.threesix.data.Matrix4fBuffer;
import com.threesix.data.ModuleCategory;
import com.threesix.module.ClickGuiModule;

public class ClickGuiScreen extends Screen {
   public static final ModuleCategory[] CACHED_CATEGORIES = ModuleCategory.values();
   public static final EnumMap categoryCache = new EnumMap<>(ModuleCategory.class);
   public static final int SLIDER_TRACK_COLOR_ARGB = -15196373;
   public static final int PANEL_W = 155;
   public static final int PANEL_PAD = 10;
   public static final int PANEL_HEADER_H = 22;
   public static final int PANEL_GAP = 12;
   public static final int PANEL_HEADER_SPACING = 6;
   public static final int ROW_H = 17;
   public static final int ROW_STEP = 19;
   public static final int SEARCH_H = 20;
   public static final int COLOR_PICKER_SV_SIZE = 80;
   public static final int COLOR_PICKER_HUE_W = 16;
   public static final int COLOR_PICKER_GAP = 6;
   public static final int COLOR_PICKER_PREVIEW_H = 14;
   public static final int COLOR_PICKER_BOTTOM_PAD = 6;
   public static final int COLOR_PICKER_FIELD_HEIGHT = 80;
   public static final int COLOR_PICKER_ALPHA_HEIGHT = 10;
   public static final int COLOR_PICKER_EXTRA_HEIGHT = 112;
   public static final int BLOCK_PICKER_SEARCH_H = 16;
   public static final int BLOCK_PICKER_ROW_H = 18;
   public static final int BLOCK_PICKER_VISIBLE_ROWS = 5;
   public static final int BLOCK_PICKER_GAP = 6;
   public static final int BLOCK_PICKER_CLEAR_W = 30;
   public static final int BLOCK_PICKER_BOTTOM_PAD = 6;
   public static final float BLOCK_PICKER_SCROLLBAR_W = 4.0F;
   public static final float BLOCK_PICKER_INDICATOR_SIZE = 6.0F;
   public static final float BLOCK_PICKER_TEXT_SCALE = 0.9F;
   public static final int COLOR_SCREEN_BG = -16052460;
   public static int COLOR_PANEL_BG = 1185831;
   public static final int COLOR_PANEL_OUTLINE = 0;
   public static final int COLOR_HEADER_BG = 0;
   public static final int COLOR_ROW_BG = 0;
   public static final int COLOR_ROW_HOVER = 872415231;
   public static final int COLOR_ROW_ACTIVE = 857419306;
   public static final int COLOR_TEXT = -1511950;
   public static final int COLOR_TEXT_MUTED = -6642510;
   public static int COLOR_ACCENT = -9710683;
   public static int COLOR_ACCENT_DIM = -11620474;
   public static final int COLOR_DIVIDER = 0;
   public static final int COLOR_SEARCH_OUTLINE = -14274495;
   public static final int COLOR_ROW_OUTLINE = 0;
   public static final int COLOR_KEY_BG = -15195855;
   public static final int SCROLL_STEP = 24;
   public ModuleBase listeningBind = null;
   public KeybindModuleBase listeningActivationBind = null;
   public ClientSetting listeningString = null;
   public boolean listeningGuiKey = false;
   public ClientSetting expandedStringListSetting = null;
   public boolean stringListAddActive = false;
   public String stringListAddBuffer = "";
   public ClientSetting expandedColorSetting = null;
   public ClientSetting activeColorSetting = null;
   public BlockListSetting expandedBlocksSetting = null;
   public EntityListSetting expandedMobsSetting = null;
   public ItemSelectSetting expandedItemsSetting = null;
   public MultiSelectSetting expandedCrafterGridSetting = null;
   public ColorDragMode colorDragMode = ColorDragMode.NONE;
   public boolean searchActive = false;
   public boolean blockSearchActive = false;
   public boolean mobSearchActive = false;
   public boolean itemSearchActive = false;
   public String searchQuery = "";
   public String blockSearchQuery = "";
   public String mobSearchQuery = "";
   public String itemSearchQuery = "";
   public int mobPickerScroll = 0;
   public int itemPickerScroll = 0;
   public int verticalScroll = 0;
   public int blockPickerScroll = 0;
   public float uiScale = 1.0F;
   public ClientSetting draggingNumericSetting = null;
   public ModuleBase draggingNumericModule = null;
   public int draggingNumericCatX = 0;
   public final EnumMap categoryOffsets = new EnumMap<>(ModuleCategory.class);
   public ModuleCategory draggingCategory = null;
   public int dragGrabOffsetX = 0;
   public int dragGrabOffsetY = 0;
   public final HashMap animValues = new HashMap();
   public long lastAnimNanos = 0L;
   public float frameDt = 0.016666668F;
   public final HashMap moduleOpenTime = new HashMap();
   public static final long MODULE_STAGGER_MS = 35L;
   public static final long MODULE_SLIDE_DURATION_MS = 220L;
   public static ClickGuiScreen INSTANCE;

   public static List cachedCategory(ModuleCategory moduleCategory) {
      List local = (List)categoryCache.get(moduleCategory);
      if (local == null) {
         local = ConfigManager.INSTANCE.getModulesByCategory(moduleCategory);
         categoryCache.put(moduleCategory, local);
      }

      return local;
   }

   public void ca(DrawContext arg, String string, int intVal, float floatVal, int intVal2, boolean flag) {
      CustomFontManager.INSTANCE7.drawText(arg, string, intVal, floatVal, intVal2);
   }

   public void caC(DrawContext arg, String string, int intVal, float floatVal, float floatVal2, int intVal2, boolean flag) {
      int customFontManagerValue = 11;
      try {
         customFontManagerValue = CustomFontManager.INSTANCE7.getLineHeight();
      } catch (Throwable error) {
      }

      this.ca(arg, string, intVal, floatVal + Math.max(0.0F, (floatVal2 - customFontManagerValue) / 2.0F), intVal2, flag);
   }

   public int cb(String string) {
      return CustomFontManager.INSTANCE7.getStringWidth(string);
   }

   public String cc(String string, int intVal) {
      if (this.cb(string) <= intVal) {
         return string;
      }

      String local = "...";
      int intVal2 = this.cb(local);

      while (string.length() > 0 && this.cb(string) + intVal2 > intVal) {
         string = string.substring(0, string.length() - 1);
      }

      return string + local;
   }

   public void cd() {
      long systemValue = System.nanoTime();
      if (this.lastAnimNanos != 0L) {
         this.frameDt = Math.min(0.1F, (float)(systemValue - this.lastAnimNanos) / 1.0E9F);
      }

      this.lastAnimNanos = systemValue;
   }

   public float ce(String string, float floatVal, float floatVal2) {
      if (!ClickGuiModule.isAnimationEnabled()) {
         this.animValues.put(string, floatVal);
         return floatVal;
      } else {
         float floatVal3 = (float)(Float)this.animValues.getOrDefault(string, floatVal);
         float floatVal4 = 1.0F - (float)Math.exp(-floatVal2 * ClickGuiModule.getAnimationSpeedScale() * this.frameDt);
         float var4Var2Var4Var5Value = floatVal3 + (floatVal - floatVal3) * floatVal4;
         this.animValues.put(string, var4Var2Var4Var5Value);
         return var4Var2Var4Var5Value;
      }
   }

   public int ch(ModuleBase moduleBase) {
      int local = 19;
      if (moduleBase instanceof KeybindModuleBase) {
         local += 19;
      }

      if ("Config Share".equals(moduleBase.getName2())) {
         local += 19;
      }

      for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase.getSettings()) {
         local += 19;
         if (clientSetting instanceof BlockListSetting local2 && this.expandedBlocksSetting == local2) {
            local += this.ci(local2);
         }

         if (clientSetting instanceof EntityListSetting local3 && this.expandedMobsSetting == local3) {
            local += this.cj(local3);
         }

         if (clientSetting instanceof ItemSelectSetting local4 && this.expandedItemsSetting == local4) {
            local += this.itemHeight(local4);
         }

         if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
            local += 112;
         }

         if (this.ck(moduleBase, clientSetting) && this.expandedStringListSetting == clientSetting) {
            local += this.cl(clientSetting);
         }
      }

      return local;
   }

   public boolean ck(ModuleBase moduleBase, ClientSetting clientSetting) {
      if (moduleBase != null && clientSetting != null && clientSetting.getValue() instanceof String) {
         return "Friends".equalsIgnoreCase(moduleBase.getName2()) && clientSetting.isNamed("Names")
            ? true
            : "TabDetector".equalsIgnoreCase(moduleBase.getName2()) && clientSetting.isNamed("Target Players");
      } else {
         return false;
      }
   }

   public int cl(ClientSetting clientSetting) {
      return (Math.min(6, this.cm(clientSetting).size()) + 1) * 19;
   }

   public List cm(ClientSetting clientSetting) {
      if (clientSetting != null && clientSetting.getValue() instanceof String) {
         String local = (String)clientSetting.getValue();
         if (local != null && !local.isBlank()) {
            String local2 = local.replace('\n', ',').replace('\r', ',');
            ArrayList arrayListInst = new ArrayList();

            for (String string : local2.split(",")) {
               String local3 = string == null ? "" : string.trim();
               if (!local3.isEmpty()) {
                  arrayListInst.add(local3);
               }
            }

            LinkedHashSet linkedHashSetInst = new LinkedHashSet();

            for (String string2 : (Iterable<String>)(Iterable)arrayListInst) {
               linkedHashSetInst.add(string2.toLowerCase(Locale.ROOT));
            }

            return new ArrayList(linkedHashSetInst);
         } else {
            return new ArrayList();
         }
      } else {
         return new ArrayList();
      }
   }

   public void cn(ClientSetting clientSetting, List list) {
      if (clientSetting != null) {
         StringBuilder stringBuilderInst = new StringBuilder();

         for (String string : (Iterable<String>)(Iterable)list) {
            String local = string == null ? "" : string.trim();
            if (!local.isEmpty()) {
               if (!stringBuilderInst.isEmpty()) {
                  stringBuilderInst.append(", ");
               }

               stringBuilderInst.append(local);
            }
         }

         clientSetting.setValue(stringBuilderInst.toString());
      }
   }

   public float co() {
      int clickGuiModuleValue = ClickGuiModule.getMenuSize();
      clickGuiModuleValue = Math.max(1, Math.min(10, clickGuiModuleValue));
      return 0.5F + (clickGuiModuleValue - 1) * 0.05F;
   }

   public double cq(double doubleVal) {
      return doubleVal / Math.max(1.0E-4F, this.uiScale);
   }

   public double cr(double doubleVal) {
      return doubleVal / Math.max(1.0E-4F, this.uiScale);
   }

   public int cs() {
      return Math.round(this.width / Math.max(1.0E-4F, this.uiScale));
   }

   public int ct() {
      return Math.round(this.height / Math.max(1.0E-4F, this.uiScale));
   }

   public float cu(ModuleBase moduleBase, String string) {
      return this.ce(string + "/expand", moduleBase.isExpanded() ? 1.0F : 0.0F, 20.0F);
   }

   public static int cv(int intVal, int intVal2, float floatVal) {
      if (floatVal <= 0.0F) {
         return intVal;
      }

      if (floatVal >= 1.0F) {
         return intVal2;
      }

      int intVal3 = intVal >>> 24 & 0xFF;
      int intVal4 = intVal >>> 16 & 0xFF;
      int intVal5 = intVal >>> 8 & 0xFF;
      int intVal6 = intVal & 0xFF;
      int intVal7 = intVal2 >>> 24 & 0xFF;
      int intVal8 = intVal2 >>> 16 & 0xFF;
      int intVal9 = intVal2 >>> 8 & 0xFF;
      int intVal10 = intVal2 & 0xFF;
      return (int)(intVal3 + (intVal7 - intVal3) * floatVal) << 24
         | (int)(intVal4 + (intVal8 - intVal4) * floatVal) << 16
         | (int)(intVal5 + (intVal9 - intVal5) * floatVal) << 8
         | (int)(intVal6 + (intVal10 - intVal6) * floatVal);
   }

   public int[] cw(ModuleCategory moduleCategory) {
      return (int[])this.categoryOffsets.computeIfAbsent(moduleCategory, item -> {
         return new int[2];
      });
   }

   public int cy(ModuleCategory moduleCategory, int intVal) {
      int cACHEDCATEGORIESValue = CACHED_CATEGORIES.length * 155 + (CACHED_CATEGORIES.length - 1) * 12;
      int maxValue = Math.max(10, (this.cs() - cACHEDCATEGORIESValue) / 2);
      return maxValue + intVal * 167 + this.cw(moduleCategory)[0];
   }

   public int cz(ModuleCategory moduleCategory) {
      return this.cA() + this.verticalScroll + this.cw(moduleCategory)[1];
   }

   public static void d() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null) {
         mc.setScreen(new ClickGuiScreen());
      }
   }

   public ClickGuiScreen() {
      super(Text.literal("36 Client"));
      INSTANCE = this;
   }

   public void init() {
      super.init();
      this.moduleOpenTime.clear();
   }

   public static int cB(int intVal, int intVal2) {
      return Math.max(0, Math.min(255, intVal2)) << 24 | intVal & 16777215;
   }

   public void render(DrawContext arg, int intVal, int intVal2, float floatVal) {
      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null) {
            ScreenBlurPipeline.call1(arg, mc.currentScreen);
         }
      } catch (Throwable error) {
      }

      COLOR_ACCENT = ClickGuiModule.getAccentColorArgb();
      COLOR_PANEL_BG = ClickGuiModule.getBackgroundColorArgb();
      Color clickGuiModuleValue = ClickGuiModule.getAccentColor();
      COLOR_ACCENT_DIM = 0xFF000000 | Math.max(0, clickGuiModuleValue.getRed() - 30) << 16 | Math.max(0, clickGuiModuleValue.getGreen() - 35) << 8 | Math.max(0, clickGuiModuleValue.getBlue() - 20);
      this.cd();
      this.uiScale = this.co();
      int roundValue = Math.round(intVal / this.uiScale);
      int roundValue2 = Math.round(intVal2 / this.uiScale);
      arg.getMatrices().pushMatrix();
      arg.getMatrices().scale(this.uiScale, this.uiScale);
      this.verticalScroll = this.cD(this.verticalScroll);
      int minValue = Math.min(260, this.cs() - 60);
      int intVal3 = (this.cs() - minValue) / 2;
      int intVal4 = this.ct() - 20 - 60;
      int intVal5 = this.searchActive ? COLOR_ACCENT : -14274495;
      GuiRenderUtil.fillRoundedRect(arg, intVal3, intVal4, minValue, 20.0F, 10.0F, COLOR_PANEL_BG, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal3, intVal4, minValue, 20.0F, 10.0F, 1.0F, intVal5, false);
      String local = this.searchQuery.isEmpty() ? "36 client" : this.searchQuery;
      int intVal6 = this.searchQuery.isEmpty() && !this.searchActive ? -6642510 : -1511950;
      boolean systemValue = System.currentTimeMillis() / 500L % 2L == 0L;
      if (this.searchActive && systemValue) {
         local = local + "_";
      }

      this.cE(arg, intVal3, intVal4, minValue, 20.0F, local, intVal3 + 8, intVal4 + 0.5F, intVal6);
      String local2 = "Configs";
      byte byteVal = 14;
      int intVal7 = this.cb(local2) + 18;
      int intVal8 = (this.cs() - intVal7) / 2;
      int var10Var166Value = intVal4 - byteVal - 6;
      boolean flag = roundValue >= intVal8 && roundValue <= intVal8 + intVal7 && roundValue2 >= var10Var166Value && roundValue2 <= var10Var166Value + byteVal;
      GuiRenderUtil.fillRoundedRect(arg, intVal8, var10Var166Value, intVal7, byteVal, 7.0F, COLOR_PANEL_BG, false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal8, var10Var166Value, intVal7, byteVal, 7.0F, 1.0F, flag ? COLOR_ACCENT : -14274495, false);
      this.caC(arg, local2, intVal8 + (intVal7 - this.cb(local2)) / 2, var10Var166Value, byteVal, flag ? -1511950 : -6642510, false);
      ModuleCategory[] cACHEDCATEGORIESSnapshot = CACHED_CATEGORIES;

      for (int index = 0; index < cACHEDCATEGORIESSnapshot.length; index++) {
         ModuleCategory local3 = cACHEDCATEGORIESSnapshot[index];
         int intVal9 = this.cy(local3, index);
         int intVal10 = this.cz(local3);
         int intVal11 = this.cF(local3);
         GuiRenderUtil.fillRoundedRect(arg, intVal9, intVal10, 155.0F, intVal11, 10.0F, COLOR_PANEL_BG, false);
         float floatVal2 = 0.75F;
         int var2410Value = intVal9 + 10;
         int var2522Int16Value = intVal10 + (22 - (int)(16.0F * floatVal2)) / 2;

         try {
            Identifier local4 = local3.getIconTexture();
            GuiRenderUtil.drawTexture(arg, var2410Value, var2522Int16Value, 12.0F, local4, -1, 1.0F, false);
         } catch (Exception error2) {
            ItemStack local5 = local3.getIcon();
            arg.getMatrices().pushMatrix();
            arg.getMatrices().translate(var2410Value, var2522Int16Value);
            arg.getMatrices().scale(floatVal2, floatVal2);
            arg.drawItem(local5, 0, 0);
            arg.getMatrices().popMatrix();
         }

         int intVal12 = (int)(16.0F * floatVal2) + 4;
         this.caC(arg, local3.getName(), intVal9 + 10 + intVal12, intVal10, 22.0F, -1511950, false);
         GuiRenderUtil.fillRoundedRect(arg, intVal9 + 10, intVal10 + 22 - 2, 135.0F, 1.0F, 0.5F, cB(16777215, 90), false);
         int var25Var26Value = intVal10 + intVal11;
         CustomFontManager.setTextYOffset(var25Var26Value);
         int var25226Value = intVal10 + 22 + 6;
         List local6 = cachedCategory(local3);
         int local20 = 0;
         int local21 = 0;
         long systemValue2 = System.currentTimeMillis();

         for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)local6) {
            if (this.cK(moduleBase)) {
               String local7 = local3.name() + "/" + moduleBase.getName2() + "/openTime";
               if (!this.moduleOpenTime.containsKey(local7)) {
                  this.moduleOpenTime.put(local7, systemValue2 + local20 * 35L);
               }

               local20++;
            }
         }

         for (ModuleBase moduleBase2 : (Iterable<ModuleBase>)(Iterable)local6) {
            if (this.cK(moduleBase2)) {
               String local8 = local3.name() + "/" + moduleBase2.getName2() + "/openTime";
               Long local9 = (Long)this.moduleOpenTime.get(local8);
               float floatVal3 = 1.0F;
               if (local9 != null) {
                  long systemValue3 = System.currentTimeMillis() - local9;
                  if (systemValue3 < 0L) {
                     floatVal3 = 0.0F;
                  } else {
                     float minValue2 = Math.min(1.0F, (float)systemValue3 / 220.0F);
                     floatVal3 = 1.0F - (float)Math.pow(1.0F - minValue2, 3.0);
                  }
               }

               int intVal13 = (int)((1.0F - floatVal3) * -14.0F);
               local21++;
               boolean flag2 = roundValue >= intVal9 + 4 && roundValue <= intVal9 + 155 - 4 && roundValue2 >= var25226Value && roundValue2 <= var25226Value + 17;
               String local10 = local3.name() + "/" + moduleBase2.getName2();
               float floatVal4 = this.ce(local10 + "/hover", flag2 ? 1.0F : 0.0F, 14.0F);
               float floatVal5 = this.ce(local10 + "/enabled", moduleBase2.isEnabled() ? 1.0F : 0.0F, 12.0F);
               int intVal14 = cv(-6642510, -1511950, floatVal4);
               int intVal15 = cv(intVal14, -1, floatVal5);
               arg.getMatrices().pushMatrix();
               arg.getMatrices().translate(0.0F, intVal13);
               float maxValue = Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F);
               GuiRenderUtil.fillRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, maxValue, this.cM(0, floatVal3), false);
               GuiRenderUtil.strokeRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, maxValue, 1.0F, this.cM(0, 0.45F * floatVal3), false);
               if (floatVal5 > 0.01F) {
                  GuiRenderUtil.fillRoundedRect(arg, intVal9 + 4, var25226Value, 2.5F, 17.0F, maxValue, this.cM(COLOR_ACCENT, floatVal5 * floatVal3), false);
               }

               float floatVal6 = 22.0F;
               float floatVal7 = 10.0F;
               float var241554Var58Value = intVal9 + 155 - 4 - floatVal6;
               float var353Value = var25226Value + 3.5F;
               int local22 = -13619152;
               int cOLORACCENTSnapshot = COLOR_ACCENT;
               int intVal16 = cv(local22, cOLORACCENTSnapshot, floatVal5);
               GuiRenderUtil.fillRoundedRect(arg, var241554Var58Value, var353Value, floatVal6, floatVal7, 5.0F, this.cM(intVal16, floatVal3), false);
               float var601Value = var241554Var58Value + 1.5F + floatVal5 * (floatVal6 - 3.0F - 7.0F);
               int intVal17 = cv(-1, -1, 1.0F);
               GuiRenderUtil.fillRoundedRect(arg, var601Value, var353Value + 1.5F, 7.0F, 7.0F, 3.5F, this.cM(intVal17, floatVal3), false);
               this.caC(arg, moduleBase2.getName2(), intVal9 + 10 + 4, var25226Value, 17.0F, this.cM(intVal15, floatVal3), false);
               arg.getMatrices().popMatrix();
               var25226Value += 19;
               maxValue = this.cu(moduleBase2, local10);
               int intVal18 = this.ch(moduleBase2);
               int roundValue3 = Math.round(maxValue * intVal18);
               if (maxValue > 0.001F) {
                  int var35Snapshot = var25226Value;
                  float floatVal8 = this.cO(maxValue);
                  float floatVal9 = -(1.0F - floatVal8) * 4.0F;
                  boolean intVal19 = this.listeningBind == moduleBase2;
                  float floatVal10 = this.cP((roundValue3 - (var25226Value - var25226Value)) / 17.0F);
                  float var70Var73Value = floatVal8 * floatVal10;
                  if (!this.animValues.containsKey(local10 + "/stagger/bind")) {
                     this.animValues.put(local10 + "/stagger/bind", -10.0F);
                  }

                  float floatVal11 = this.ce(local10 + "/stagger/bind", moduleBase2.isExpanded() ? 0.0F : -10.0F, 20.0F);
                  arg.getMatrices().pushMatrix();
                  arg.getMatrices().translate(0.0F, floatVal9 + floatVal11);
                  GuiRenderUtil.fillRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), this.cM(0, var70Var73Value), false);
                  GuiRenderUtil.strokeRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), 1.0F, this.cM(0, var70Var73Value), false);
                  this.caC(arg, "Bind", intVal9 + 10, var25226Value, 17.0F, this.cM(-6642510, var70Var73Value), false);
                  String local11 = intVal19 ? "..." : (moduleBase2.getKeyCode() == 0 ? "None" : this.cQ(moduleBase2.getKeyCode()));
                  int intVal20 = this.cb(local11) + 10;
                  int var2415510Var77Value = intVal9 + 155 - 10 - intVal20;
                  int var353Value2 = var25226Value + 3;
                  int intVal21 = intVal19 ? this.cM(COLOR_ACCENT & 16777215 | -2013265920, var70Var73Value) : this.cM(COLOR_ACCENT & 16777215 | 855638016, var70Var73Value);
                  GuiRenderUtil.fillRoundedRect(arg, var2415510Var77Value, var353Value2, intVal20, 10.0F, Math.max(2.0F, ClickGuiModule.getPanelCornerRadius() * 0.5F), intVal21, false);
                  GuiRenderUtil.strokeRoundedRect(
                     arg,
                     var2415510Var77Value,
                     var353Value2,
                     intVal20,
                     10.0F,
                     Math.max(2.0F, ClickGuiModule.getPanelCornerRadius() * 0.5F),
                     1.0F,
                     this.cM(COLOR_ACCENT & 16777215 | 1711276032, var70Var73Value),
                     false
                  );
                  this.ca(arg, local11, var2415510Var77Value + 5, var353Value2 + 0.5F, this.cM(COLOR_ACCENT, var70Var73Value), false);
                  arg.getMatrices().popMatrix();
                  var25226Value += 19;
                  if (moduleBase2 instanceof KeybindModuleBase local23) {
                     boolean flag3 = this.listeningActivationBind == local23;
                     var70Var73Value = this.cP((roundValue3 - (var25226Value - var25226Value)) / 17.0F);
                     floatVal11 = floatVal8 * var70Var73Value;
                     if (!this.animValues.containsKey(local10 + "/stagger/act")) {
                        this.animValues.put(local10 + "/stagger/act", -10.0F);
                     }

                     float floatVal12 = this.ce(local10 + "/stagger/act", moduleBase2.isExpanded() ? 0.0F : -10.0F, 18.0F);
                     arg.getMatrices().pushMatrix();
                     arg.getMatrices().translate(0.0F, floatVal9 + floatVal12);
                     GuiRenderUtil.fillRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), this.cM(0, floatVal11), false);
                     GuiRenderUtil.strokeRoundedRect(
                        arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), 1.0F, this.cM(0, floatVal11), false
                     );
                     this.caC(arg, "Activation", intVal9 + 10, var25226Value, 17.0F, this.cM(-6642510, floatVal11), false);
                     String local12 = flag3 ? "..." : (local23.getBindKeyCode() == 0 ? "None" : this.cQ(local23.getBindKeyCode()));
                     var2415510Var77Value = this.cb(local12) + 10;
                     var353Value2 = intVal9 + 155 - 10 - var2415510Var77Value;
                     intVal21 = var25226Value + 3;
                     int intVal22 = flag3 ? this.cM(COLOR_ACCENT & 16777215 | -2013265920, floatVal11) : this.cM(COLOR_ACCENT & 16777215 | 855638016, floatVal11);
                     GuiRenderUtil.fillRoundedRect(arg, var353Value2, intVal21, var2415510Var77Value, 10.0F, Math.max(2.0F, ClickGuiModule.getPanelCornerRadius() * 0.5F), intVal22, false);
                     GuiRenderUtil.strokeRoundedRect(
                        arg,
                        var353Value2,
                        intVal21,
                        var2415510Var77Value,
                        10.0F,
                        Math.max(2.0F, ClickGuiModule.getPanelCornerRadius() * 0.5F),
                        1.0F,
                        this.cM(COLOR_ACCENT & 16777215 | 1711276032, floatVal11),
                        false
                     );
                     this.ca(arg, local12, var353Value2 + 5, intVal21 + 0.5F, this.cM(COLOR_ACCENT, floatVal11), false);
                     arg.getMatrices().popMatrix();
                     var25226Value += 19;
                  }

                  if ("Config Share".equals(moduleBase2.getName2())) {
                     float floatVal13 = this.cP((roundValue3 - (var25226Value - var25226Value)) / 17.0F);
                     floatVal10 = floatVal8 * floatVal13;
                     boolean flag4 = this.cR(roundValue, roundValue2, intVal9 + 4, var25226Value, 147.0F, 17.0F);
                     int intVal23 = flag4 ? this.cM(COLOR_ACCENT, floatVal10) : this.cM(-15722464, floatVal10);
                     arg.getMatrices().pushMatrix();
                     arg.getMatrices().translate(0.0F, floatVal9);
                     GuiRenderUtil.fillRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), intVal23, false);
                     GuiRenderUtil.strokeRoundedRect(
                        arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), 1.0F, this.cM(COLOR_ACCENT, floatVal10 * 0.6F), false
                     );
                     this.caC(arg, "Open Config Manager", intVal9 + 10, var25226Value, 17.0F, this.cM(-1, floatVal10), false);
                     arg.getMatrices().popMatrix();
                     var25226Value += 19;
                  }

                  int staggerIndex = 0;
                  for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase2.getSettings()) {
                     floatVal11 = this.cP((roundValue3 - (var25226Value - var35Snapshot)) / 17.0F);
                     float var70Var75Value = floatVal8 * floatVal11;
                     String var11645Var72Value = local10 + "/stagger/" + staggerIndex;
                     float floatVal14 = moduleBase2.isExpanded() ? 0.0F : -8.0F;
                     if (!this.animValues.containsKey(var11645Var72Value)) {
                        this.animValues.put(var11645Var72Value, -8.0F);
                     }

                     float floatVal15 = this.ce(var11645Var72Value, floatVal14, Math.max(8.0F, 18.0F - staggerIndex * 1.5F));
                     staggerIndex++;
                     arg.getMatrices().pushMatrix();
                     arg.getMatrices().translate(0.0F, floatVal9 + floatVal15);
                     GuiRenderUtil.fillRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), this.cM(0, var70Var75Value), false);
                     GuiRenderUtil.strokeRoundedRect(
                        arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), 1.0F, this.cM(0, var70Var75Value), false
                     );
                     Object local13 = clientSetting.getValue();
                     if (clientSetting instanceof ModeSetting02 local24) {
                        this.cS(arg, local24, intVal9 + 4, 147, var25226Value, var70Var75Value);
                     } else if (local13 instanceof Boolean) {
                        boolean flag5 = (Boolean)local13;
                        int var241551020Value = intVal9 + 155 - 10 - 20;
                        int var354Value = var25226Value + 4;
                        String systemValue4 = System.identityHashCode(clientSetting) + "/tog";
                        float floatVal16 = this.ce(systemValue4, flag5 ? 1.0F : 0.0F, 16.0F);
                        GuiRenderUtil.fillRoundedRect(arg, var241551020Value, var354Value, 20.0F, 8.0F, 4.0F, this.cM(cv(-13682875, COLOR_ACCENT_DIM, floatVal16), var70Var75Value), false);
                        GuiRenderUtil.fillRoundedRect(
                           arg, var241551020Value + 2 + Math.round(10.0F * floatVal16), var354Value + 1, 6.0F, 6.0F, 3.0F, this.cM(cv(-1511950, COLOR_ACCENT, floatVal16), var70Var75Value), false
                        );
                        this.caC(arg, clientSetting.getName(), intVal9 + 10, var25226Value, 17.0F, this.cM(cv(-6642510, COLOR_ACCENT, floatVal16), var70Var75Value), false);
                     } else if (!(local13 instanceof Float) && !(local13 instanceof Double) && !(local13 instanceof Integer)) {
                        if (local13 instanceof String) {
                           if ("GUI Key".equals(clientSetting.getName())) {
                              String local14 = this.listeningGuiKey ? "GUI Key: ..." : "GUI Key: " + ClickGuiModule.getMenuKeyName();
                              float class310Value = MinecraftClient.getInstance().textRenderer.getWidth(local14) + 10;
                              float var241554Var157Value = intVal9 + 155 - 4 - class310Value;
                              float var353Value3 = var25226Value + 3.5F;
                              GuiRenderUtil.fillRoundedRect(arg, var241554Var157Value, var353Value3, class310Value, 10.0F, 4.0F, COLOR_ACCENT & 16777215 | 1140850688, false);
                              this.caC(arg, "GUI Key", intVal9 + 10, var25226Value, 17.0F, this.cM(-1511950, var70Var75Value), false);
                              this.ca(
                                 arg,
                                 this.listeningGuiKey ? "..." : ClickGuiModule.getMenuKeyName(),
                                 (int)(var241554Var157Value + 5.0F),
                                 (int)(var353Value3 + 1.0F),
                                 this.cM(COLOR_ACCENT, var70Var75Value),
                                 false
                              );
                           } else {
                              String local15;
                              if (this.ck(moduleBase2, clientSetting)) {
                                 int intVal24 = this.cm(clientSetting).size();
                                 local15 = clientSetting.getName() + ": " + intVal24 + " entries";
                                 if (this.expandedStringListSetting == clientSetting) {
                                    local15 = local15 + " (edit)";
                                 }
                              } else {
                                 String local16 = this.cV(moduleBase2, clientSetting, (String)local13);
                                 local15 = clientSetting.getName() + ": " + local16;
                                 if (this.listeningString == clientSetting) {
                                    local15 = local15 + "_";
                                 }
                              }

                              this.cE(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, local15, intVal9 + 10, var25226Value + 0.5F, this.cM(-1511950, var70Var75Value));
                           }
                        } else if (clientSetting instanceof BlockListSetting local25) {
                           this.cW(arg, local25, intVal9 + 4, 147.0F, var25226Value, 17, var70Var75Value);
                        } else if (clientSetting instanceof EntityListSetting local26) {
                           this.cX(arg, local26, intVal9 + 4, 147.0F, var25226Value, 17, var70Var75Value);
                        } else if (clientSetting instanceof ItemSelectSetting local27) {
                           this.itemRenderRow(arg, local27, intVal9 + 4, 147.0F, var25226Value, 17, var70Var75Value);
                        } else if (clientSetting instanceof MultiSelectSetting local28) {
                           this.crafterGridRenderRow(arg, local28, intVal9 + 4, 147.0F, var25226Value, 17, var70Var75Value);
                        } else if (local13 instanceof Color local29) {
                           boolean flag6 = roundValue >= intVal9 + 4 && roundValue <= intVal9 + 155 - 4 && roundValue2 >= var25226Value && roundValue2 <= var25226Value + 17;
                           this.caC(arg, clientSetting.getName(), intVal9 + 10, var25226Value, 17.0F, this.cM(-1511950, var70Var75Value), false);
                           this.cY(arg, clientSetting, intVal9 + 4, 147.0F, var25226Value, 17, flag6 ? 1.0F : 0.0F, this.expandedColorSetting == clientSetting ? 1.0F : 0.0F, var70Var75Value);
                        }
                     } else {
                        float local17;
                        float local19;
                        float local18;
                        String integerValue;
                        if (local13 instanceof Integer local30 && clientSetting.getMinValue() instanceof Integer && clientSetting.getMaxValue() instanceof Integer) {
                           local17 = local30.intValue();
                           local18 = ((Integer)clientSetting.getMinValue()).intValue();
                           local19 = ((Integer)clientSetting.getMaxValue()).intValue();
                           integerValue = Integer.toString(local30);
                        } else {
                           local17 = local13 instanceof Float ? (Float)local13 : (float)((Double)local13).doubleValue();
                           local19 = clientSetting.getMaxValue() instanceof Float ? (Float)clientSetting.getMaxValue() : (float)((Double)clientSetting.getMaxValue()).doubleValue();
                           local18 = clientSetting.getMinValue() instanceof Float ? (Float)clientSetting.getMinValue() : (float)((Double)clientSetting.getMinValue()).doubleValue();
                           if (this.cT(moduleBase2)) {
                              integerValue = String.format("%.1f", local17);
                           } else {
                              local17 = Math.round(local17);
                              integerValue = Integer.toString(Math.round(local17));
                           }
                        }

                        float floatVal17 = (local17 - local18) / (local19 - local18);
                        int var2410Value2 = intVal9 + 10;
                        int var3511Value = var25226Value + 11;
                        short shortVal = 135;
                        int intVal25 = (int)(shortVal * Math.max(0.0F, Math.min(1.0F, floatVal17)));
                        GuiRenderUtil.fillRoundedRect(arg, var2410Value2, var3511Value, shortVal, 3.0F, 1.5F, this.cM(-14208703, var70Var75Value), false);
                        GuiRenderUtil.fillRoundedRect(arg, var2410Value2, var3511Value, intVal25, 3.0F, 1.5F, this.cM(COLOR_ACCENT_DIM, var70Var75Value), false);
                        float var94Var973Value = var2410Value2 + intVal25 - 3.0F;
                        float var951Value = var3511Value + 1.5F - 3.0F;
                        GuiRenderUtil.fillRoundedRect(arg, var94Var973Value, var951Value, 6.0F, 6.0F, 3.0F, this.cM(COLOR_ACCENT, var70Var75Value), false);
                        GuiRenderUtil.strokeRoundedRect(arg, var94Var973Value, var951Value, 6.0F, 6.0F, 3.0F, 1.0F, this.cM(-1711276033, var70Var75Value), false);
                        this.caC(arg, clientSetting.getName() + ": " + integerValue, intVal9 + 10, var25226Value, 17.0F, this.cM(-1511950, var70Var75Value), false);
                     }

                     arg.getMatrices().popMatrix();
                     var25226Value += 19;
                     if (this.ck(moduleBase2, clientSetting) && this.expandedStringListSetting == clientSetting) {
                        float floatVal18 = this.cP((roundValue3 - (var25226Value - var35Snapshot)) / 17.0F);
                        if (floatVal8 * floatVal18 > 0.01F) {
                           arg.getMatrices().pushMatrix();
                           arg.getMatrices().translate(0.0F, floatVal9);
                           this.cZ(arg, clientSetting, intVal9 + 4, var25226Value, 147, floatVal8 * floatVal18);
                           arg.getMatrices().popMatrix();
                        }

                        var25226Value += this.cl(clientSetting);
                     }

                     if (clientSetting instanceof BlockListSetting local31 && this.expandedBlocksSetting == local31) {
                        float var70ThisValue = floatVal8 * this.cP((roundValue3 - (var25226Value - var35Snapshot)) / 17.0F);
                        arg.getMatrices().pushMatrix();
                        arg.getMatrices().translate(0.0F, floatVal9);
                        if (var70ThisValue > 0.01F) {
                           this.da(arg, local31, intVal9 + 4, 147.0F, var25226Value, intVal, intVal2);
                        }

                        arg.getMatrices().popMatrix();
                        var25226Value += this.ci(local31);
                     }

                     if (clientSetting instanceof EntityListSetting local32 && this.expandedMobsSetting == local32) {
                        float var70ThisValue2 = floatVal8 * this.cP((roundValue3 - (var25226Value - var35Snapshot)) / 17.0F);
                        arg.getMatrices().pushMatrix();
                        arg.getMatrices().translate(0.0F, floatVal9);
                        if (var70ThisValue2 > 0.01F) {
                           this.db(arg, local32, intVal9 + 4, 147.0F, var25226Value, intVal, intVal2);
                        }

                        arg.getMatrices().popMatrix();
                        var25226Value += this.cj(local32);
                     }

                     if (clientSetting instanceof ItemSelectSetting local33 && this.expandedItemsSetting == local33) {
                        float var70ThisValue3 = floatVal8 * this.cP((roundValue3 - (var25226Value - var35Snapshot)) / 17.0F);
                        arg.getMatrices().pushMatrix();
                        arg.getMatrices().translate(0.0F, floatVal9);
                        if (var70ThisValue3 > 0.01F) {
                           this.itemRenderExpanded(arg, local33, intVal9 + 4, 147.0F, var25226Value, intVal, intVal2);
                        }

                        arg.getMatrices().popMatrix();
                        var25226Value += this.itemHeight(local33);
                     }

                     if (clientSetting instanceof MultiSelectSetting local34 && this.expandedCrafterGridSetting == local34) {
                        float var70ThisValue4 = floatVal8 * this.cP((roundValue3 - (var25226Value - var35Snapshot)) / 17.0F);
                        arg.getMatrices().pushMatrix();
                        arg.getMatrices().translate(0.0F, floatVal9);
                        if (var70ThisValue4 > 0.01F) {
                           this.crafterGridRenderExpanded(arg, local34, intVal9 + 4, 147.0F, var25226Value, intVal, intVal2);
                        }

                        arg.getMatrices().popMatrix();
                        var25226Value += this.crafterGridExpandedHeight(local34);
                     }

                     if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                        var25226Value += 112;
                     }
                  }

                  var25226Value = var35Snapshot + roundValue3;
               }
            }
         }

         if (local21 == 0) {
            GuiRenderUtil.fillRoundedRect(arg, intVal9 + 4, var25226Value, 147.0F, 17.0F, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), 0, false);
            this.caC(arg, "No results", intVal9 + 10, var25226Value, 17.0F, -6642510, false);
         }

         CustomFontManager.clearTextYOffset();
      }

      arg.getMatrices().popMatrix();

      try {
         if (this.listeningBind != null) {
            for (int index2 = 32; index2 <= 348; index2++) {
               if (index2 != 256 && index2 != 259 && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), index2) == 1) {
                  this.listeningBind.setKeyCode(index2);
                  this.listeningBind = null;
                  break;
               }
            }

            if (this.listeningBind != null && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 259) == 1) {
               this.listeningBind.setKeyCode(0);
               this.listeningBind = null;
            }

            if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 256) == 1) {
               this.listeningBind = null;
            }
         }
      } catch (Throwable error3) {
         this.listeningBind = null;
      }

      try {
         if (this.listeningActivationBind != null) {
            for (int index3 = 32; index3 <= 348; index3++) {
               if (index3 != 256 && index3 != 259 && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), index3) == 1) {
                  this.listeningActivationBind.setBindKeyCode(index3);
                  this.listeningActivationBind = null;
                  break;
               }
            }

            if (this.listeningActivationBind != null && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 259) == 1) {
               this.listeningActivationBind.setBindKeyCode(0);
               this.listeningActivationBind = null;
            }

            if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 256) == 1) {
               this.listeningActivationBind = null;
            }
         }
      } catch (Throwable error4) {
         this.listeningActivationBind = null;
      }

      try {
         if (this.listeningGuiKey) {
            for (int index4 = 32; index4 <= 348; index4++) {
               if (index4 != 256 && index4 != 259 && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), index4) == 1) {
                  ClickGuiModule.setMenuKey(index4, bY(index4));
                  this.listeningGuiKey = false;
                  break;
               }
            }

            if (this.listeningGuiKey && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 256) == 1) {
               this.listeningGuiKey = false;
            }
         }
      } catch (Throwable error5) {
         this.listeningGuiKey = false;
      }
   }

   public void cW(DrawContext arg, BlockListSetting blockListSetting, float floatVal, float floatVal2, float floatVal3, int intVal, float floatVal4) {
      String local = this.expandedBlocksSetting == blockListSetting ? "v" : ">";
      int intVal2 = this.cb(local);
      int roundValue = Math.round(floatVal + floatVal2 - 6.0F - intVal2);
      int maxValue = Math.max(30, roundValue - (Math.round(floatVal) + 10 + this.cb(blockListSetting.getName()) + 14));
      int intVal3 = this.cM(this.expandedBlocksSetting != blockListSetting && blockListSetting.getSelectedCount() <= 0 ? -6642510 : -1511950, floatVal4);
      String local2 = blockListSetting.getSelectedCount() == 0 ? "Choose" : this.de(blockListSetting);
      String local3 = this.df(local2, Math.round((maxValue - 18) / 0.9F));
      int maxValue2 = Math.max(34, Math.min(maxValue, this.cb(local3) + 22));
      int var10Var156Value = roundValue - maxValue2 - 6;
      int intVal4 = this.cM(blockListSetting.getSelectedCount() > 0 ? COLOR_ACCENT_DIM : -15195855, floatVal4);
      ItemStack local4 = this.dg(blockListSetting);
      this.caC(arg, blockListSetting.getName(), Math.round(floatVal) + 10, Math.round(floatVal3), 17.0F, intVal3, false);
      GuiRenderUtil.fillRoundedRect(arg, var10Var156Value, floatVal3 + 2.0F, maxValue2, 12.0F, Math.max(2.0F, ClickGuiModule.getPanelCornerRadius() * 0.5F), intVal4, false);
      GuiRenderUtil.strokeRoundedRect(arg, var10Var156Value, floatVal3 + 2.0F, maxValue2, 12.0F, Math.max(2.0F, ClickGuiModule.getPanelCornerRadius() * 0.5F), 1.0F, this.cM(0, floatVal4), false);
      if (!local4.isEmpty()) {
         arg.drawItem(local4, var10Var156Value + 2, Math.round(floatVal3) + 1);
      }

      this.dh(arg, local3, var10Var156Value + (local4.isEmpty() ? 6 : 16), floatVal3 + 4.0F, 0.9F, this.cM(-1511950, floatVal4));
      this.caC(arg, local, roundValue, Math.round(floatVal3), 17.0F, this.cM(-6642510, floatVal4), false);
   }

   public void da(DrawContext arg, BlockListSetting blockListSetting, float floatVal, float floatVal2, float floatVal3, int intVal, int intVal2) {
      Matrix4fBuffer local = this.di(floatVal, floatVal2, floatVal3, blockListSetting);
      List local2 = this.dj(blockListSetting);
      this.blockPickerScroll = this.dk(local2.size(), this.blockPickerScroll);
      float clickGuiModuleValue = ClickGuiModule.getPanelCornerRadius();
      float maxValue = Math.max(4.0F, clickGuiModuleValue * 0.5F);
      float clickGuiModuleValue2 = ClickGuiModule.getGlassIntensityFactor();
      int clickGuiModuleValue3 = ClickGuiModule.getBackgroundColorArgb();
      GuiRenderUtil.fillRoundedRect(arg, local.rectX, local.rectY, local.rectWidth, local.rectHeight, maxValue, clickGuiModuleValue3, false);
      if (clickGuiModuleValue2 > 0.01F) {
         GuiRenderUtil.strokeRoundedRect(arg, local.rectX, local.rectY, local.rectWidth, local.rectHeight, maxValue, 1.0F, cB(16777215, (int)(50.0F * clickGuiModuleValue2)), false);
         GuiRenderUtil.fillRoundedRect(
            arg, local.rectX + 1.0F, local.rectY + 1.0F, local.rectWidth - 2.0F, 6.0F, maxValue, cB(16777215, (int)(15.0F * clickGuiModuleValue2)), false
         );
      } else {
         GuiRenderUtil.strokeRoundedRect(
            arg, local.rectX, local.rectY, local.rectWidth, local.rectHeight, maxValue, 1.0F, COLOR_ACCENT & 16777215 | 855638016, false
         );
      }

      boolean flag = this.blockSearchActive && this.expandedBlocksSetting == blockListSetting;
      int intVal3 = flag ? -15919840 : -16117736;
      int intVal4 = flag ? COLOR_ACCENT : -14799552;
      GuiRenderUtil.fillRoundedRect(arg, local.searchX, local.searchY, local.searchWidth, local.searchHeight, maxValue, intVal3, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.searchX, local.searchY, local.searchWidth, local.searchHeight, maxValue, 1.0F, intVal4, false);
      String local3 = this.blockSearchQuery.isEmpty() ? "⌕  Search..." : "⌕  " + this.blockSearchQuery;
      if (flag && System.currentTimeMillis() / 500L % 2L == 0L) {
         local3 = local3 + "_";
      }

      this.cE(
         arg,
         local.searchX,
         local.searchY,
         Math.max(0.0F, local.searchWidth),
         Math.max(0.0F, local.searchHeight),
         local3,
         Math.round(local.searchX) + 6,
         Math.round(local.searchY) + 4,
         this.blockSearchQuery.isEmpty() && !flag ? -6642510 : -1511950
      );
      GuiRenderUtil.fillRoundedRect(arg, local.clearButtonX, local.clearButtonY, local.clearButtonWidth, local.clearButtonHeight, maxValue, 585125984, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.clearButtonX, local.clearButtonY, local.clearButtonWidth, local.clearButtonHeight, maxValue, 1.0F, 1725976672, false);
      this.caC(arg, "✕", Math.round(local.clearButtonX) + (int)(local.clearButtonWidth / 2.0F) - 3, Math.round(local.clearButtonY), local.clearButtonHeight, -2076576, false);
      if (local2.isEmpty()) {
         this.caC(arg, "No blocks found", Math.round(local.listX) + 6, Math.round(local.listY), 18.0F, -6642510, false);
      } else {
         int minValue = Math.min(5, local2.size());
         boolean flag2 = local2.size() > minValue;

         for (int index = 0; index < minValue; index++) {
            int intVal5 = this.blockPickerScroll + index;
            if (intVal5 >= local2.size()) {
               break;
            }

            Block local4 = (Block)local2.get(intVal5);
            float floatVal4 = local.listY + index * 18;
            boolean flag3 = blockListSetting.isSelected(local4);
            boolean flag4 = intVal >= local.listX && intVal <= local.listX + local.listWidth && intVal2 >= floatVal4 && intVal2 <= floatVal4 + 18.0F - 2.0F;
            int intVal6 = flag3 ? COLOR_ACCENT & 16777215 | 570425344 : (flag4 ? 419430399 : 0);
            if (intVal6 != 0) {
               GuiRenderUtil.fillRoundedRect(arg, local.listX, floatVal4, local.listWidth, 16.0F, maxValue, intVal6, false);
            }

            if (flag3) {
               GuiRenderUtil.fillRoundedRect(arg, local.listX, floatVal4 + 2.0F, 2.0F, 12.0F, 1.0F, COLOR_ACCENT, false);
            }

            ItemStack local5 = new ItemStack(local4);
            if (!local5.isEmpty()) {
               arg.getMatrices().pushMatrix();
               arg.getMatrices().translate(local.listX + 4.0F, floatVal4 + 1.0F);
               arg.getMatrices().scale(0.75F, 0.75F);
               arg.drawItem(local5, 0, 0);
               arg.getMatrices().popMatrix();
            }

            float floatVal5 = local.listX + local.listWidth - 10.0F;
            int roundValue = Math.round(local.listX) + 16;
            String local6 = this.df(blockListSetting.getBlockName(local4), Math.round((floatVal5 - roundValue - 4.0F) / 0.9F));
            this.dh(arg, local6, roundValue, floatVal4 + 4.0F, 0.9F, flag3 ? COLOR_ACCENT : (flag4 ? -1511950 : -6642510));
            int intVal7 = flag3 ? COLOR_ACCENT : -14799552;
            int intVal8 = flag3 ? COLOR_ACCENT & 16777215 | 1140850688 : 0;
            if (intVal8 != 0) {
               GuiRenderUtil.fillRoundedRect(arg, floatVal5, floatVal4 + 5.0F, 6.0F, 6.0F, 3.0F, intVal8, false);
            }

            GuiRenderUtil.strokeRoundedRect(arg, floatVal5, floatVal4 + 5.0F, 6.0F, 6.0F, 3.0F, 1.0F, intVal7, false);
         }

         if (flag2) {
            int maxValue2 = Math.max(1, local2.size() - minValue);
            float floatVal6 = local.listX + local.listWidth - 4.0F;
            float floatVal7 = local.listY + 1.0F;
            float floatVal8 = local.listHeight - 2.0F;
            float maxValue3 = Math.max(12.0F, floatVal8 * ((float)minValue / local2.size()));
            float floatVal9 = (floatVal8 - maxValue3) * ((float)this.blockPickerScroll / maxValue2);
            GuiRenderUtil.fillRoundedRect(arg, floatVal6, floatVal7, 4.0F, floatVal8, 2.0F, -16117736, false);
            GuiRenderUtil.fillRoundedRect(arg, floatVal6, floatVal7 + floatVal9, 4.0F, maxValue3, 2.0F, COLOR_ACCENT & 16777215 | -1442840576, false);
         }
      }
   }

   public void cY(DrawContext arg, ClientSetting clientSetting, float floatVal, float floatVal2, float floatVal3, int intVal, float floatVal4, float floatVal5, float floatVal6) {
      Color local = (Color)clientSetting.getValue();
      float clickGuiModuleValue = ClickGuiModule.getPanelCornerRadius();
      float maxValue = Math.max(2.0F, clickGuiModuleValue * 0.5F);
      float floatVal7 = 12.0F;
      float var3Var45Value = floatVal + floatVal2 - 5.0F - floatVal7;
      float var5Var64Value = floatVal3 + (intVal - 4.0F - floatVal7) / 2.0F;
      GuiRenderUtil.fillRoundedRect(arg, var3Var45Value, var5Var64Value + 2.0F, floatVal7, floatVal7, maxValue, this.dl((Color)clientSetting.getValue(), floatVal6), false);
      GuiRenderUtil.strokeRoundedRect(arg, var3Var45Value, var5Var64Value + 2.0F, floatVal7, floatVal7, maxValue, 1.0F, this.cM(-1427114245, floatVal6), false);
      if (!(floatVal5 <= 0.01F)) {
         float floatVal8 = this.cO(floatVal5) * floatVal6;
         float var34Value = floatVal + 4.0F;
         float var5Var66Value = floatVal3 + intVal + 6.0F;
         float maxValue2 = Math.max(1.0F, 80.0F * floatVal8);
         float maxValue3 = Math.max(1.0F, 16.0F * floatVal8);
         float var17Var196Value = var34Value + maxValue2 + 6.0F;
         float floatVal9 = this.getHue(local);
         float floatVal10 = this.getSaturation(local);
         float floatVal11 = this.getBrightness(local);
         byte byteVal = 12;
         byte byteVal2 = 12;
         float var19Var25Value = maxValue2 / byteVal;
         float var19Var26Value = maxValue2 / byteVal2;

         for (int index = 0; index < byteVal2; index++) {
            float floatVal12 = 1.0F - (float)index / byteVal2;

            for (int index2 = 0; index2 < byteVal; index2++) {
               float floatVal13 = (float)index2 / byteVal;
               arg.fill(
                  (int)(var34Value + index2 * var19Var25Value),
                  (int)(var5Var66Value + index * var19Var26Value),
                  (int)(var34Value + (index2 + 1) * var19Var25Value),
                  (int)(var5Var66Value + (index + 1) * var19Var26Value),
                  this.dm(Color.HSBtoRGB(floatVal9, floatVal13, floatVal12), floatVal8)
               );
            }
         }

         GuiRenderUtil.strokeRoundedRect(arg, var34Value, var5Var66Value, maxValue2, maxValue2, maxValue, 1.0F, this.dm(1728053247, floatVal8), false);
         float var17Var23Var19Value = var34Value + floatVal10 * maxValue2;
         float var181Value = var5Var66Value + (1.0F - floatVal11) * maxValue2;
         GuiRenderUtil.fillRoundedRect(arg, var17Var23Var19Value - 4.0F, var181Value - 4.0F, 8.0F, 8.0F, 4.0F, this.dm(-2013265920, floatVal8), false);
         GuiRenderUtil.strokeRoundedRect(arg, var17Var23Var19Value - 4.0F, var181Value - 4.0F, 8.0F, 8.0F, 4.0F, 2.0F, this.dm(-1, floatVal8), false);

         for (int index3 = 0; index3 < (int)maxValue2; index3++) {
            float floatVal14 = (float)index3 / (int)maxValue2;
            arg.fill(
               (int)var17Var196Value,
               (int)(var5Var66Value + index3),
               (int)(var17Var196Value + maxValue3),
               (int)(var5Var66Value + index3 + 1.0F),
               this.dm(0xFF000000 | Color.HSBtoRGB(floatVal14, 1.0F, 1.0F) & 16777215, floatVal8)
            );
         }

         GuiRenderUtil.strokeRoundedRect(arg, var17Var196Value, var5Var66Value, maxValue3, maxValue2, maxValue, 1.0F, this.dm(1728053247, floatVal8), false);
         GuiRenderUtil.fillRoundedRect(arg, var17Var196Value - 2.0F, var5Var66Value + floatVal9 * maxValue2 - 1.0F, maxValue3 + 4.0F, 3.0F, 1.5F, this.dm(-1, floatVal8), false);
         float var18Var196Value = var5Var66Value + maxValue2 + 6.0F;
         float floatVal15 = (maxValue2 + 6.0F + maxValue3) / 2.0F - 2.0F;
         float maxValue4 = Math.max(1.0F, 14.0F * floatVal8);
         int intVal2 = this.dl(local, floatVal8);
         GuiRenderUtil.fillRoundedRect(arg, var34Value, var18Var196Value, floatVal15, maxValue4, maxValue, intVal2, false);
         GuiRenderUtil.fillRoundedRect(arg, var34Value + floatVal15 + 4.0F, var18Var196Value, floatVal15, maxValue4, maxValue, intVal2, false);
         GuiRenderUtil.strokeRoundedRect(arg, var34Value, var18Var196Value, floatVal15, maxValue4, maxValue, 1.0F, this.dm(1442840575, floatVal8), false);
         GuiRenderUtil.strokeRoundedRect(arg, var34Value + floatVal15 + 4.0F, var18Var196Value, floatVal15, maxValue4, maxValue, 1.0F, this.dm(1442840575, floatVal8), false);
         this.ca(arg, "ORIGINAL", (int)(var34Value + 2.0F), (int)(var18Var196Value + maxValue4 + 0.5F), this.dm(-6642510, floatVal8), false);
         this.ca(arg, "NEW", (int)(var34Value + floatVal15 + 6.0F), (int)(var18Var196Value + maxValue4 + 0.5F), this.dm(-6642510, floatVal8), false);
      }
   }

   public float getHue(Color color) {
      return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
   }

   public float getSaturation(Color color) {
      return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[1];
   }

   public float getBrightness(Color color) {
      return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2];
   }

   public float getAlphaFloat(Color color) {
      return color.getAlpha() / 255.0F;
   }

   public void dn(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, int intVal, IntFunction intFunction) {
      if (!(floatVal4 <= 0.0F)) {
         float maxValue = Math.max(1.0F, floatVal3 / intVal);

         for (int index = 0; index < intVal; index++) {
            float var2Var9Var10Value = floatVal + maxValue * index;
            float floatVal6 = index == intVal - 1 ? floatVal + floatVal3 - var2Var9Var10Value : maxValue + 1.0F;
            float floatVal7 = index == 0 ? floatVal5 : 0.0F;
            float floatVal8 = index == intVal - 1 ? floatVal5 : 0.0F;
            GuiRenderUtil.fillPerCornerGradient(arg, var2Var9Var10Value, floatVal2, floatVal6, floatVal4, floatVal7, floatVal8, floatVal8, floatVal7, false, (Integer)intFunction.apply(index));
         }
      }
   }

   public boolean mouseClicked(Click arg, boolean flag) {
      this.uiScale = this.co();
      double doubleVal = this.cq(arg.x());
      double doubleVal2 = this.cr(arg.y());
      int var1Value = arg.button();
      this.activeColorSetting = null;
      this.colorDragMode = ColorDragMode.NONE;
      this.draggingNumericSetting = null;
      this.draggingNumericModule = null;
      int minValue = Math.min(260, this.cs() - 60);
      int intVal = (this.cs() - minValue) / 2;
      int intVal2 = this.ct() - 20 - 60;
      String local = "Configs";
      byte byteVal = 14;
      int intVal3 = this.cb(local) + 18;
      int intVal4 = (this.cs() - intVal3) / 2;
      int var10Var126Value = intVal2 - byteVal - 6;
      if (doubleVal >= intVal4 && doubleVal <= intVal4 + intVal3 && doubleVal2 >= var10Var126Value && doubleVal2 <= var10Var126Value + byteVal) {
         this.searchActive = false;
         MinecraftClient.getInstance().setScreen(new ConfigManagerScreen());
         return true;
      }

      if (doubleVal >= intVal && doubleVal <= intVal + minValue && doubleVal2 >= intVal2 && doubleVal2 <= intVal2 + 20) {
         this.searchActive = true;
         this.blockSearchActive = false;
         this.listeningBind = null;
         this.listeningActivationBind = null;
         this.listeningString = null;
         return true;
      }

      this.searchActive = false;
      if (var1Value == 0) {
         ModuleCategory[] cACHEDCATEGORIESSnapshot = CACHED_CATEGORIES;

         for (int index = 0; index < cACHEDCATEGORIESSnapshot.length; index++) {
            int intVal5 = this.cy(cACHEDCATEGORIESSnapshot[index], index);
            int intVal6 = this.cz(cACHEDCATEGORIESSnapshot[index]);
            if (doubleVal >= intVal5 && doubleVal <= intVal5 + 155 && doubleVal2 >= intVal6 && doubleVal2 <= intVal6 + 22) {
               this.draggingCategory = cACHEDCATEGORIESSnapshot[index];
               this.dragGrabOffsetX = (int)(doubleVal - intVal5);
               this.dragGrabOffsetY = (int)(doubleVal2 - intVal6);
               return true;
            }
         }
      }

      ModuleCategory[] cACHEDCATEGORIESSnapshot2 = CACHED_CATEGORIES;

      for (int index2 = 0; index2 < cACHEDCATEGORIESSnapshot2.length; index2++) {
         ModuleCategory local2 = cACHEDCATEGORIESSnapshot2[index2];
         int intVal7 = this.cy(local2, index2);
         int intVal8 = this.cz(local2);
         int var20226Value = intVal8 + 22 + 6;

         for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)cachedCategory(local2)) {
            if (this.cK(moduleBase)) {
               if (doubleVal >= intVal7 + 4 && doubleVal <= intVal7 + 155 - 4 && doubleVal2 >= var20226Value && doubleVal2 <= var20226Value + 17) {
                  if (var1Value == 1 && moduleBase.getName2().equals("Chat Macro")) {
                     MinecraftClient.getInstance().setScreen(new ChatMacrosScreen(this));
                     return true;
                  }

                  if (var1Value == 0) {
                     moduleBase.toggle();
                  } else if (var1Value == 1) {
                     boolean flag2 = !moduleBase.isExpanded();
                     String local3 = local2.name() + "/" + moduleBase.getName2();
                     if (!flag2) {
                        this.animValues.put(local3 + "/expand", 0.0F);
                     } else {
                        this.animValues.put(local3 + "/expand", 0.0F);

                        for (int index3 = 0; index3 < moduleBase.getSettings().size() + 2; index3++) {
                           this.animValues.put(local3 + "/stagger/" + index3, -8.0F);
                        }

                        this.animValues.put(local3 + "/stagger/bind", -10.0F);
                        this.animValues.put(local3 + "/stagger/act", -10.0F);
                     }

                     moduleBase.setExpanded(flag2);
                  }

                  return true;
               }

               var20226Value += 19;
               if (moduleBase.isExpanded()) {
                  if (doubleVal >= intVal7 + 4 && doubleVal <= intVal7 + 155 - 4 && doubleVal2 >= var20226Value && doubleVal2 <= var20226Value + 17) {
                     if (var1Value == 1) {
                        moduleBase.setKeyCode(0);
                        this.listeningBind = null;
                        this.listeningActivationBind = null;
                     } else if (var1Value == 0) {
                        this.listeningBind = moduleBase;
                        this.listeningActivationBind = null;
                     }

                     return true;
                  }

                  var20226Value += 19;
                  if ("Config Share".equals(moduleBase.getName2())) {
                     if (doubleVal >= intVal7 + 4 && doubleVal <= intVal7 + 155 - 4 && doubleVal2 >= var20226Value && doubleVal2 <= var20226Value + 17) {
                        if (var1Value == 0) {
                           MinecraftClient.getInstance().setScreen(new ConfigManagerScreen());
                        }

                        return true;
                     }

                     var20226Value += 19;
                  }

                  if (moduleBase instanceof KeybindModuleBase local16) {
                     if (doubleVal >= intVal7 + 4 && doubleVal <= intVal7 + 155 - 4 && doubleVal2 >= var20226Value && doubleVal2 <= var20226Value + 17) {
                        if (var1Value == 1) {
                           local16.setBindKeyCode(0);
                           this.listeningActivationBind = null;
                           this.listeningBind = null;
                        } else if (var1Value == 0) {
                           this.listeningActivationBind = local16;
                           this.listeningBind = null;
                        }

                        return true;
                     }

                     var20226Value += 19;
                  }

                  for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase.getSettings()) {
                     if (doubleVal >= intVal7 + 4 && doubleVal <= intVal7 + 155 - 4 && doubleVal2 >= var20226Value && doubleVal2 <= var20226Value + 17) {
                        if (clientSetting instanceof ModeSetting02 local17) {
                           if (var1Value == 1) {
                              local17.previousMode();
                           } else {
                              local17.nextMode();
                           }
                        } else if (clientSetting.getValue() instanceof Boolean) {
                           clientSetting.setValue(!(Boolean)clientSetting.getValue());
                        } else if (clientSetting.getValue() instanceof String) {
                           if ("GUI Key".equals(clientSetting.getName())) {
                              if (var1Value == 0) {
                                 this.listeningGuiKey = !this.listeningGuiKey;
                                 this.listeningBind = null;
                                 this.listeningActivationBind = null;
                                 this.listeningString = null;
                              } else if (var1Value == 1) {
                                 this.listeningGuiKey = false;
                              }
                           } else if (this.ck(moduleBase, clientSetting)) {
                              if (var1Value == 0) {
                                 this.expandedStringListSetting = this.expandedStringListSetting == clientSetting ? null : clientSetting;
                                 this.stringListAddActive = this.expandedStringListSetting == clientSetting;
                                 this.stringListAddBuffer = "";
                                 this.listeningString = null;
                              } else if (var1Value == 1) {
                                 this.expandedStringListSetting = null;
                                 this.stringListAddActive = false;
                                 this.stringListAddBuffer = "";
                              }
                           } else {
                              this.expandedStringListSetting = null;
                              this.stringListAddActive = false;
                              this.stringListAddBuffer = "";
                              this.listeningString = clientSetting;
                           }
                        } else if (!(clientSetting.getValue() instanceof Float)
                           && !(clientSetting.getValue() instanceof Double)
                           && !(clientSetting.getValue() instanceof Integer)) {
                           if (clientSetting instanceof BlockListSetting local18) {
                              if (var1Value == 0 || var1Value == 1) {
                                 LinkedHashMap linkedHashMapInst = new LinkedHashMap();
                                 Consumer nullSnapshot = null;
                                 if (moduleBase instanceof StorageEspModule local19) {
                                    linkedHashMapInst.putAll(local19.getBlockColorMap());
                                    nullSnapshot = item -> local19.replaceBlockColors((Map)item);
                                 } else if (moduleBase instanceof BlockEspModule local20) {
                                    linkedHashMapInst.putAll(local20.getBlockColors());
                                    nullSnapshot = item -> local20.setBlockColors((Map)item);
                                 }

                                 MinecraftClient.getInstance().setScreen(new BlockColorPickerScreen(this, moduleBase, local18, linkedHashMapInst, nullSnapshot));
                                 return true;
                              }

                              if (var1Value == 2) {
                                 local18.clearBlocks();
                              }
                           } else if (clientSetting instanceof EntityListSetting local21) {
                              if (var1Value == 0) {
                                 if (this.expandedMobsSetting != local21) {
                                    this.mobSearchQuery = "";
                                    this.mobPickerScroll = 0;
                                 }

                                 this.expandedMobsSetting = this.expandedMobsSetting == local21 ? null : local21;
                                 this.mobSearchActive = this.expandedMobsSetting == local21;
                              } else if (var1Value == 1) {
                                 local21.clearEntities();
                                 this.mobPickerScroll = 0;
                              }
                           } else if (clientSetting instanceof MultiSelectSetting local22) {
                              if (var1Value == 0) {
                                 this.expandedCrafterGridSetting = this.expandedCrafterGridSetting == local22 ? null : local22;
                              }
                           } else if (clientSetting instanceof ItemSelectSetting local23) {
                              if (var1Value == 0) {
                                 if (this.expandedItemsSetting != local23) {
                                    this.itemSearchQuery = "";
                                    this.itemPickerScroll = 0;
                                 }

                                 this.expandedItemsSetting = this.expandedItemsSetting == local23 ? null : local23;
                                 this.itemSearchActive = this.expandedItemsSetting == local23;
                              } else if (var1Value == 1) {
                                 local23.clearItems();
                                 this.itemPickerScroll = 0;
                              }
                           } else if (clientSetting.getValue() instanceof Color) {
                              if (var1Value == 0) {
                                 this.expandedColorSetting = this.expandedColorSetting == clientSetting ? null : clientSetting;
                              } else if (var1Value == 1) {
                                 this.expandedColorSetting = null;
                              }
                           }
                        } else if (var1Value == 0) {
                           this.draggingNumericSetting = clientSetting;
                           this.draggingNumericModule = moduleBase;
                           this.draggingNumericCatX = intVal7;
                           this.handleNumericDrag(moduleBase, clientSetting, doubleVal, intVal7);
                        }

                        return true;
                     }

                     if (this.ck(moduleBase, clientSetting) && this.expandedStringListSetting == clientSetting) {
                        int var2119Value = var20226Value + 19;
                        int intVal9 = this.cl(clientSetting);
                        int var434Value = intVal7 + 4;
                        short shortVal = 147;
                        if (this.cR(doubleVal, doubleVal2, var434Value, var2119Value, shortVal, intVal9)) {
                           List local4 = this.cm(clientSetting);
                           int minValue2 = Math.min(6, local4.size());

                           for (int index4 = 0; index4 < minValue2; index4++) {
                              int var26Var8519Value = var2119Value + index4 * 19;
                              int var28Var291012Value = var434Value + shortVal - 10 - 12;
                              int var882Value = var26Var8519Value + 2;
                              if (this.cR(doubleVal, doubleVal2, var28Var291012Value, var882Value, 12.0F, 12.0F) && var1Value == 0) {
                                 String local5 = (String)local4.get(index4);
                                 local4.removeIf(item -> {
                                    return ((String)item).equalsIgnoreCase(local5);
                                 });
                                 this.cn(clientSetting, local4);
                                 return true;
                              }
                           }

                           int var26Var8019Value = var2119Value + minValue2 * 19;
                           int var28Var291012Value2 = var434Value + shortVal - 10 - 12;
                           int var862Value = var26Var8019Value + 2;
                           if (var1Value == 0 && this.cR(doubleVal, doubleVal2, var28Var291012Value2, var862Value, 12.0F, 12.0F)) {
                              String local6 = this.stringListAddBuffer == null ? "" : this.stringListAddBuffer.trim();
                              if (!local6.isEmpty()) {
                                 String local7 = local6.toLowerCase(Locale.ROOT);
                                 boolean falseSnapshot = false;

                                 for (String string : (Iterable<String>)(Iterable)local4) {
                                    if (string.equalsIgnoreCase(local7)) {
                                       falseSnapshot = true;
                                       break;
                                    }
                                 }

                                 if (!falseSnapshot) {
                                    local4.add(local7);
                                    this.cn(clientSetting, local4);
                                 }
                              }

                              this.stringListAddBuffer = "";
                              this.stringListAddActive = true;
                              this.listeningString = null;
                              return true;
                           }

                           if (var1Value == 0 && this.cR(doubleVal, doubleVal2, var434Value, var26Var8019Value, shortVal, 17.0F)) {
                              this.stringListAddActive = true;
                              this.listeningString = null;
                              return true;
                           }

                           return true;
                        }
                     }

                     if (clientSetting instanceof BlockListSetting local24 && this.expandedBlocksSetting == local24) {
                        Matrix4fBuffer local8 = this.di(intVal7 + 4, 147.0F, var20226Value + 19, local24);
                        if (this.cR(doubleVal, doubleVal2, local8.clearButtonX, local8.clearButtonY, local8.clearButtonWidth, local8.clearButtonHeight)) {
                           local24.clearBlocks();
                           this.blockPickerScroll = 0;
                           this.blockSearchQuery = "";
                           return true;
                        }

                        if (this.cR(doubleVal, doubleVal2, local8.searchX, local8.searchY, local8.searchWidth, local8.searchHeight)) {
                           this.blockSearchActive = true;
                           this.searchActive = false;
                           this.listeningString = null;
                           return true;
                        }

                        if (this.cR(doubleVal, doubleVal2, local8.rectX, local8.rectY, local8.rectWidth, local8.rectHeight)) {
                           this.blockSearchActive = false;
                           List local9 = this.dj(local24);
                           int minValue3 = Math.min(5, Math.max(1, local9.size()));

                           for (int index5 = 0; index5 < minValue3; index5++) {
                              int intVal10 = this.dk(local9.size(), this.blockPickerScroll) + index5;
                              if (intVal10 >= local9.size()) {
                                 break;
                              }

                              float floatVal = local8.listY + index5 * 18;
                              if (this.cR(doubleVal, doubleVal2, local8.listX, floatVal, local8.listWidth, 16.0F)) {
                                 local24.toggleBlock((Block)local9.get(intVal10));
                                 return true;
                              }
                           }

                           return true;
                        }
                     }

                     if (clientSetting instanceof EntityListSetting local25 && this.expandedMobsSetting == local25) {
                        Matrix4fBuffer local10 = this.dq(intVal7 + 4, 147.0F, var20226Value + 19, local25);
                        if (this.cR(doubleVal, doubleVal2, local10.clearButtonX, local10.clearButtonY, local10.clearButtonWidth, local10.clearButtonHeight)) {
                           local25.clearEntities();
                           this.mobPickerScroll = 0;
                           this.mobSearchQuery = "";
                           return true;
                        }

                        if (this.cR(doubleVal, doubleVal2, local10.searchX, local10.searchY, local10.searchWidth, local10.searchHeight)) {
                           this.mobSearchActive = true;
                           this.searchActive = false;
                           this.listeningString = null;
                           return true;
                        }

                        if (this.cR(doubleVal, doubleVal2, local10.rectX, local10.rectY, local10.rectWidth, local10.rectHeight)) {
                           this.mobSearchActive = false;
                           List local11 = this.dr(local25);
                           int minValue4 = Math.min(5, Math.max(1, local11.size()));

                           for (int index6 = 0; index6 < minValue4; index6++) {
                              int intVal11 = this.ds(local11.size(), this.mobPickerScroll) + index6;
                              if (intVal11 >= local11.size()) {
                                 break;
                              }

                              float floatVal2 = local10.listY + index6 * 18;
                              if (this.cR(doubleVal, doubleVal2, local10.listX, floatVal2, local10.listWidth, 16.0F)) {
                                 local25.toggleEntity((EntityType)local11.get(intVal11));
                                 return true;
                              }
                           }

                           return true;
                        }
                     }

                     if (clientSetting instanceof MultiSelectSetting local26 && this.expandedCrafterGridSetting == local26) {
                        int var43410Value = intVal7 + 4 + 10;
                        int var21194Value = var20226Value + 19 + 4;
                        byte byteVal2 = 24;
                        byte byteVal3 = 2;

                        for (int index7 = 0; index7 < 3; index7++) {
                           for (int index8 = 0; index8 < 3; index8++) {
                              int var773Var82Value = index7 * 3 + index8;
                              int var60Var82Var68Var72Value = var43410Value + index8 * (byteVal2 + byteVal3);
                              int var63Var77Var68Var72Value = var21194Value + index7 * (byteVal2 + byteVal3);
                              if (doubleVal >= var60Var82Var68Var72Value && doubleVal <= var60Var82Var68Var72Value + byteVal2 && doubleVal2 >= var63Var77Var68Var72Value && doubleVal2 <= var63Var77Var68Var72Value + byteVal2) {
                                 Boolean[] local12 = (Boolean[])local26.getValue();
                                 if (local12 != null && local12.length == 9) {
                                    local12[var773Var82Value] = local12[var773Var82Value] == null ? true : !local12[var773Var82Value];
                                    local26.setValue(local12);
                                 }

                                 return true;
                              }
                           }
                        }

                        return true;
                     }

                     if (clientSetting instanceof ItemSelectSetting local27 && this.expandedItemsSetting == local27) {
                        Matrix4fBuffer local13 = this.itemLayout(intVal7 + 4, 147.0F, var20226Value + 19, local27);
                        if (this.cR(doubleVal, doubleVal2, local13.clearButtonX, local13.clearButtonY, local13.clearButtonWidth, local13.clearButtonHeight)) {
                           local27.clearItems();
                           this.itemPickerScroll = 0;
                           this.itemSearchQuery = "";
                           return true;
                        }

                        if (this.cR(doubleVal, doubleVal2, local13.searchX, local13.searchY, local13.searchWidth, local13.searchHeight)) {
                           this.itemSearchActive = true;
                           this.searchActive = false;
                           this.listeningString = null;
                           return true;
                        }

                        if (this.cR(doubleVal, doubleVal2, local13.rectX, local13.rectY, local13.rectWidth, local13.rectHeight)) {
                           this.itemSearchActive = false;
                           List local14 = this.itemList(local27);
                           int minValue5 = Math.min(5, Math.max(1, local14.size()));

                           for (int index9 = 0; index9 < minValue5; index9++) {
                              int intVal12 = this.itemScrollClamp(local14.size(), this.itemPickerScroll) + index9;
                              if (intVal12 >= local14.size()) {
                                 break;
                              }

                              float floatVal3 = local13.listY + index9 * 18;
                              if (this.cR(doubleVal, doubleVal2, local13.listX, floatVal3, local13.listWidth, 16.0F)) {
                                 local27.toggleItem((Item)local14.get(intVal12));
                                 return true;
                              }
                           }

                           return true;
                        }
                     }

                     if (var1Value == 0 && clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                        GuiSettingRect local15 = this.dt(intVal7 + 4, 147.0F, var20226Value, 17);
                        if (this.cR(doubleVal, doubleVal2, local15.x, local15.colorRectY, local15.width, local15.colorRectHeight)) {
                           this.du(clientSetting, local15, doubleVal, doubleVal2);
                           this.activeColorSetting = clientSetting;
                           this.colorDragMode = ColorDragMode.FIELD;
                           return true;
                        }

                        if (this.cR(doubleVal, doubleVal2, local15.textX - 6.0F, local15.colorRectY - 4.0F, local15.textWidth + 12.0F, local15.colorRectHeight + 8.0F)) {
                           this.dv(clientSetting, local15, doubleVal2);
                           this.activeColorSetting = clientSetting;
                           this.colorDragMode = ColorDragMode.ALPHA;
                           return true;
                        }
                     }

                     var20226Value += 19;
                     if (this.ck(moduleBase, clientSetting) && this.expandedStringListSetting == clientSetting) {
                        var20226Value += this.cl(clientSetting);
                     }

                     if (clientSetting instanceof BlockListSetting local28 && this.expandedBlocksSetting == local28) {
                        var20226Value += this.ci(local28);
                     }

                     if (clientSetting instanceof EntityListSetting local29 && this.expandedMobsSetting == local29) {
                        var20226Value += this.cj(local29);
                     }

                     if (clientSetting instanceof ItemSelectSetting local30 && this.expandedItemsSetting == local30) {
                        var20226Value += this.itemHeight(local30);
                     }

                     if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                        var20226Value += 112;
                     }
                  }
               }
            }
         }
      }

      this.listeningBind = null;
      this.listeningActivationBind = null;
      this.listeningString = null;
      this.stringListAddActive = false;
      this.blockSearchActive = false;
      return super.mouseClicked(arg, flag);
   }

   public boolean mouseDragged(Click arg, double doubleVal, double doubleVal2) {
      this.uiScale = this.co();
      double doubleVal3 = this.cq(arg.x());
      double doubleVal4 = this.cr(arg.y());
      int var1Value = arg.button();
      if (var1Value != 0) {
         return super.mouseDragged(arg, doubleVal, doubleVal2);
      }

      if (this.colorDragMode != ColorDragMode.NONE && this.activeColorSetting != null && this.dw(doubleVal3, doubleVal4)) {
         return true;
      }

      if (this.draggingNumericSetting != null && this.draggingNumericModule != null) {
         this.handleNumericDrag(this.draggingNumericModule, this.draggingNumericSetting, doubleVal3, this.draggingNumericCatX);
         return true;
      }

      if (this.draggingCategory != null) {
         ModuleCategory[] cACHEDCATEGORIESSnapshot = CACHED_CATEGORIES;
         int var24Snapshot = 0;

         for (int index = 0; index < cACHEDCATEGORIESSnapshot.length; index++) {
            if (cACHEDCATEGORIESSnapshot[index] == this.draggingCategory) {
               var24Snapshot = index;
               break;
            }
         }

         int cACHEDCATEGORIESValue = CACHED_CATEGORIES.length * 155 + (CACHED_CATEGORIES.length - 1) * 12;
         int maxValue = Math.max(10, (this.cs() - cACHEDCATEGORIESValue) / 2);
         int var26Var23167Value = maxValue + var24Snapshot * 167;
         int intVal = this.cA() + this.verticalScroll;
         int[] local = this.cw(this.draggingCategory);
         local[0] = (int)(doubleVal3 - this.dragGrabOffsetX) - var26Var23167Value;
         local[1] = (int)(doubleVal4 - this.dragGrabOffsetY) - intVal;
         return true;
      } else {
         ModuleCategory[] cACHEDCATEGORIESSnapshot2 = CACHED_CATEGORIES;

         for (int index2 = 0; index2 < cACHEDCATEGORIESSnapshot2.length; index2++) {
            ModuleCategory local2 = cACHEDCATEGORIESSnapshot2[index2];
            int intVal2 = this.cy(local2, index2);
            int intVal3 = this.cz(local2);
            int var15226Value = intVal3 + 22 + 6;

            for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)cachedCategory(local2)) {
               if (this.cK(moduleBase)) {
                  var15226Value += 19;
                  if (moduleBase.isExpanded()) {
                     var15226Value += 19;
                     var15226Value += 19;

                     for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase.getSettings()) {
                        if (doubleVal3 >= intVal2 + 4
                           && doubleVal3 <= intVal2 + 155 - 4
                           && doubleVal4 >= var15226Value
                           && doubleVal4 <= var15226Value + 17
                           && (clientSetting.getValue() instanceof Float || clientSetting.getValue() instanceof Double || clientSetting.getValue() instanceof Integer)) {
                           this.handleNumericDrag(moduleBase, clientSetting, doubleVal3, intVal2);
                        }

                        var15226Value += 19;
                        if (clientSetting instanceof BlockListSetting local3 && this.expandedBlocksSetting == local3) {
                           var15226Value += this.ci(local3);
                        }

                        if (clientSetting instanceof EntityListSetting local4 && this.expandedMobsSetting == local4) {
                           var15226Value += this.cj(local4);
                        }

                        if (clientSetting instanceof ItemSelectSetting local5 && this.expandedItemsSetting == local5) {
                           var15226Value += this.itemHeight(local5);
                        }

                        if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                           var15226Value += 112;
                        }
                     }
                  }
               }
            }
         }

         return super.mouseDragged(arg, doubleVal, doubleVal2);
      }
   }

   public boolean mouseReleased(Click arg) {
      this.activeColorSetting = null;
      this.colorDragMode = ColorDragMode.NONE;
      this.draggingCategory = null;
      this.draggingNumericSetting = null;
      this.draggingNumericModule = null;
      return super.mouseReleased(arg);
   }

   public boolean mouseScrolled(double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4) {
      this.uiScale = this.co();
      doubleVal = this.cq(doubleVal);
      doubleVal2 = this.cr(doubleVal2);
      double doubleVal5 = doubleVal4 != 0.0 ? doubleVal4 : doubleVal3;
      if (doubleVal5 == 0.0) {
         return super.mouseScrolled(doubleVal, doubleVal2, doubleVal3, doubleVal4);
      } else {
         BlockPickerGuiEntry local = this.dx();
         if (local != null && this.cR(doubleVal, doubleVal2, local.layout().rectX, local.layout().rectY, local.layout().rectWidth, local.layout().rectHeight)) {
            this.blockPickerScroll = this.dk(this.dj(local.setting()).size(), this.blockPickerScroll + (doubleVal5 > 0.0 ? -1 : 1));
            return true;
         } else {
            SettingLayoutPair local2 = this.dy();
            if (local2 != null
               && this.cR(doubleVal, doubleVal2, local2.layout().rectX, local2.layout().rectY, local2.layout().rectWidth, local2.layout().rectHeight)) {
               this.mobPickerScroll = this.ds(this.dr(local2.setting()).size(), this.mobPickerScroll + (doubleVal5 > 0.0 ? -1 : 1));
               return true;
            } else {
               ItemPickerGuiEntry local3 = this.itemDZ();
               if (local3 != null
                  && this.cR(doubleVal, doubleVal2, local3.layout().rectX, local3.layout().rectY, local3.layout().rectWidth, local3.layout().rectHeight)) {
                  this.itemPickerScroll = this.itemScrollClamp(this.itemList(local3.setting()).size(), this.itemPickerScroll + (doubleVal5 > 0.0 ? -1 : 1));
                  return true;
               } else {
                  this.verticalScroll = this.cD(this.verticalScroll + (int)Math.round(doubleVal5 * 24.0));
                  return true;
               }
            }
         }
      }
   }

   public boolean charTyped(CharInput arg) {
      String local = this.dz(arg.asString());
      if (local.isEmpty()) {
         return super.charTyped(arg);
      } else if (this.blockSearchActive && this.expandedBlocksSetting != null) {
         this.blockSearchQuery = this.blockSearchQuery + local;
         this.blockPickerScroll = 0;
         return true;
      } else if (this.mobSearchActive && this.expandedMobsSetting != null) {
         this.mobSearchQuery = this.mobSearchQuery + local;
         this.mobPickerScroll = 0;
         return true;
      } else if (this.itemSearchActive && this.expandedItemsSetting != null) {
         this.itemSearchQuery = this.itemSearchQuery + local;
         this.itemPickerScroll = 0;
         return true;
      } else if (this.searchActive && this.listeningString == null) {
         this.searchQuery = this.searchQuery + local;
         return true;
      } else if (this.stringListAddActive && this.expandedStringListSetting != null) {
         this.stringListAddBuffer = (this.stringListAddBuffer == null ? "" : this.stringListAddBuffer) + local;
         return true;
      } else if (this.listeningString != null) {
         this.listeningString.setValue((String)this.listeningString.getValue() + local);
         return true;
      } else {
         return super.charTyped(arg);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   public boolean keyPressed(KeyInput arg) {
      int var1Value = arg.getKeycode();
      if (this.listeningBind != null) {
         if (arg.isEscape() || var1Value == 256) {
            this.listeningBind = null;
         } else if (var1Value == 259) {
            this.listeningBind.setKeyCode(0);
            this.listeningBind = null;
         } else if (var1Value >= 32 && var1Value <= 348) {
            this.listeningBind.setKeyCode(var1Value);
            this.listeningBind = null;
         }

         return true;
      } else if (this.listeningActivationBind != null) {
         if (arg.isEscape() || var1Value == 256) {
            this.listeningActivationBind = null;
         } else if (var1Value == 259) {
            this.listeningActivationBind.setBindKeyCode(0);
            this.listeningActivationBind = null;
         } else if (var1Value >= 32 && var1Value <= 348) {
            this.listeningActivationBind.setBindKeyCode(var1Value);
            this.listeningActivationBind = null;
         }

         return true;
      } else if (this.listeningGuiKey) {
         if (arg.isEscape() || var1Value == 256) {
            this.listeningGuiKey = false;
         } else if (var1Value >= 32 && var1Value <= 348) {
            ClickGuiModule.setMenuKey(var1Value, bY(var1Value));
            this.listeningGuiKey = false;
         }

         return true;
      } else {
         if (this.blockSearchActive && this.expandedBlocksSetting != null && this.dA(arg)) {
            return true;
         }

         if (this.mobSearchActive && this.expandedMobsSetting != null && this.dB(arg)) {
            return true;
         }

         if (this.itemSearchActive && this.expandedItemsSetting != null && this.itemKey(arg)) {
            return true;
         }

         if (this.searchActive) {
            if (this.dC(arg)) {
               this.searchActive = false;
               return true;
            } else if (arg.getKeycode() == 259) {
               this.searchQuery = this.dD(this.searchQuery);
               return true;
            } else if (arg.isPaste()) {
               this.searchQuery = this.searchQuery + this.dE();
               return true;
            } else {
               return true;
            }
         } else if (this.listeningString != null && this.dF(arg)) {
            return true;
         } else {
            return this.stringListAddActive && this.expandedStringListSetting != null && this.dG(arg) ? true : super.keyPressed(arg);
         }
      }
   }

   public boolean dG(KeyInput arg) {
      if (arg.getKeycode() == 259) {
         this.stringListAddBuffer = this.dD(this.stringListAddBuffer == null ? "" : this.stringListAddBuffer);
         return true;
      }

      if (arg.isPaste()) {
         this.stringListAddBuffer = (this.stringListAddBuffer == null ? "" : this.stringListAddBuffer) + this.dE();
         return true;
      }

      if (arg.isEscape()) {
         this.stringListAddActive = false;
         this.stringListAddBuffer = "";
         return true;
      }

      if (!arg.isEnter()) {
         return true;
      }

      if (this.expandedStringListSetting != null) {
         List local = this.cm(this.expandedStringListSetting);
         String local2 = this.stringListAddBuffer == null ? "" : this.stringListAddBuffer.trim();
         if (!local2.isEmpty()) {
            String local3 = local2.toLowerCase(Locale.ROOT);
            boolean falseSnapshot = false;

            for (String string : (Iterable<String>)(Iterable)local) {
               if (string.equalsIgnoreCase(local3)) {
                  falseSnapshot = true;
                  break;
               }
            }

            if (!falseSnapshot) {
               local.add(local3);
               this.cn(this.expandedStringListSetting, local);
            }
         }
      }

      this.stringListAddBuffer = "";
      return true;
   }

   public boolean cK(ModuleBase moduleBase) {
      if (moduleBase instanceof ConfigShareModule) {
         return false;
      } else {
         return this.searchQuery.isBlank() ? true : moduleBase.getName2().toLowerCase().contains(this.searchQuery.trim().toLowerCase());
      }
   }

   public String cN(ModuleBase moduleBase) {
      String local = this.cQ(moduleBase.getKeyCode());
      return "None".equals(local) ? "" : local;
   }

   public String cQ(int intVal) {
      return bY(intVal);
   }

   public static String bY(int intVal) {
      if (intVal <= 0 || intVal > 348) {
         return "None";
      }

      String gLFWValue = GLFW.glfwGetKeyName(intVal, 0);
      if (gLFWValue != null && !gLFWValue.isBlank()) {
         return dH(gLFWValue);
      }

      return switch (intVal) {
         case 32 -> "Space";
         case 256 -> "Esc";
         case 257 -> "Enter";
         case 258 -> "Tab";
         case 259 -> "Backspace";
         case 260 -> "Insert";
         case 261 -> "Delete";
         case 262 -> "Right";
         case 263 -> "Left";
         case 264 -> "Down";
         case 265 -> "Up";
         case 266 -> "Page Up";
         case 267 -> "Page Down";
         case 268 -> "Home";
         case 269 -> "End";
         case 280 -> "Caps";
         case 340 -> "LShift";
         case 341 -> "LCtrl";
         case 342 -> "LAlt";
         case 344 -> "RShift";
         case 345 -> "RCtrl";
         case 346 -> "RAlt";
         default -> "Key " + intVal;
      };
   }

   public static String dH(String string) {
      if (string == null) {
         return "";
      }

      String local = string.trim();
      if (local.isEmpty()) {
         return "";
      }

      String local2 = local.toLowerCase();

      return switch (local2) {
         case "right shift" -> "RShift";
         case "left shift" -> "LShift";
         case "right control", "right ctrl" -> "RCtrl";
         case "left control", "left ctrl" -> "LCtrl";
         case "right alt" -> "RAlt";
         case "left alt" -> "LAlt";
         case "escape" -> "Esc";
         case "caps lock" -> "Caps";
         case "page up" -> "Page Up";
         case "page down" -> "Page Down";
         default -> local.length() == 1 ? local.toUpperCase() : local;
      };
   }

   public void cS(DrawContext arg, ModeSetting02 modeSetting02, int intVal, int intVal2, int intVal3, float floatVal) {
      String local = modeSetting02.getName();
      String local2 = (String)modeSetting02.getValue();
      int intVal4 = this.cb(local2) + 12;
      int var3Var410Var9Value = intVal + intVal2 - 10 - intVal4;
      int var54Value = intVal3 + 4;
      int var310Value = intVal + 10;
      int maxValue = Math.max(0, var3Var410Var9Value - 4 - var310Value);
      String var7Snapshot = local;
      if (this.cb(local) > maxValue) {
         String local3 = "...";
         int intVal5 = this.cb(local3);

         while (var7Snapshot.length() > 0 && this.cb(var7Snapshot) + intVal5 > maxValue) {
            var7Snapshot = var7Snapshot.substring(0, var7Snapshot.length() - 1);
         }

         var7Snapshot = var7Snapshot + local3;
      }

      this.ca(arg, var7Snapshot, var310Value, var54Value, this.cM(-1511950, floatVal), false);
      GuiRenderUtil.fillRoundedRect(arg, var3Var410Var9Value, intVal3 + 2, intVal4, 12.0F, 5.0F, this.cM(-15195855, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, var3Var410Var9Value, intVal3 + 2, intVal4, 12.0F, 5.0F, 1.0F, this.cM(0, floatVal), false);
      this.ca(arg, local2, var3Var410Var9Value + 6, var54Value, this.cM(COLOR_ACCENT, floatVal), false);
   }

   public void cZ(DrawContext arg, ClientSetting clientSetting, int intVal, int intVal2, int intVal3, float floatVal) {
      List local = this.cm(clientSetting);
      int minValue = Math.min(6, local.size());
      int intVal4 = this.cl(clientSetting);
      GuiRenderUtil.fillRoundedRect(arg, intVal, intVal2, intVal3, intVal4, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), this.cM(0, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, intVal, intVal2, intVal3, intVal4, Math.max(0.0F, ClickGuiModule.getPanelCornerRadius() - 3.0F), 1.0F, this.cM(0, floatVal), false);
      int var4Snapshot = intVal2;

      for (int index = 0; index < minValue; index++) {
         String local2 = (String)local.get(index);
         this.caC(arg, local2, intVal + 10, var4Snapshot, 17.0F, this.cM(-1511950, floatVal), false);
         int var3Var51012Value = intVal + intVal3 - 10 - 12;
         int var102Value = var4Snapshot + 2;
         GuiRenderUtil.fillRoundedRect(arg, var3Var51012Value, var102Value, 12.0F, 12.0F, 4.0F, this.cM(-14011323, floatVal), false);
         GuiRenderUtil.strokeRoundedRect(arg, var3Var51012Value, var102Value, 12.0F, 12.0F, 4.0F, 1.0F, this.cM(0, floatVal), false);
         this.caC(arg, "x", var3Var51012Value + 4, var4Snapshot, 17.0F, this.cM(-1938838, floatVal), false);
         var4Snapshot += 19;
      }

      String local3 = "Add: " + (this.stringListAddBuffer == null ? "" : this.stringListAddBuffer);
      if (this.stringListAddActive && this.expandedStringListSetting == clientSetting) {
         local3 = local3 + "_";
      }

      this.cE(arg, intVal, var4Snapshot, intVal3, 17.0F, local3, intVal + 10, var4Snapshot + 4, this.cM(-6642510, floatVal));
      int var3Var51012Value2 = intVal + intVal3 - 10 - 12;
      int var102Value2 = var4Snapshot + 2;
      GuiRenderUtil.fillRoundedRect(arg, var3Var51012Value2, var102Value2, 12.0F, 12.0F, 4.0F, this.cM(-15195855, floatVal), false);
      GuiRenderUtil.strokeRoundedRect(arg, var3Var51012Value2, var102Value2, 12.0F, 12.0F, 4.0F, 1.0F, this.cM(0, floatVal), false);
      this.caC(arg, "+", var3Var51012Value2 + 4, var4Snapshot, 17.0F, this.cM(COLOR_ACCENT, floatVal), false);
   }

   public int cF(ModuleCategory moduleCategory) {
      int local3 = 38;
      int local2 = 0;

      for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)ConfigManager.INSTANCE.getModulesByCategory(moduleCategory)) {
         if (this.cK(moduleBase)) {
            local2++;
            local3 += 19;
            String local = moduleCategory.name() + "/" + moduleBase.getName2();
            float floatVal = (float)(Float)this.animValues.getOrDefault(local + "/expand", moduleBase.isExpanded() ? 1.0F : 0.0F);
            if (floatVal > 0.001F) {
               local3 += Math.round(this.ch(moduleBase) * floatVal);
            }
         }
      }

      if (local2 == 0) {
         local3 += 19;
      }

      return local3;
   }

   public Matrix4fBuffer di(float floatVal, float floatVal2, float floatVal3, BlockListSetting blockListSetting) {
      int intVal = this.dj(blockListSetting).size();
      int minValue = Math.min(5, Math.max(1, intVal));
      float var16Value = floatVal + 6.0F;
      float var36Value = floatVal3 + 6.0F;
      float var1Var230Value = floatVal + floatVal2 - 30.0F - 6.0F;
      float maxValue = Math.max(24.0F, var1Var230Value - var16Value - 4.0F);
      float var16Value2 = floatVal + 6.0F;
      float var816Value = var36Value + 16.0F + 6.0F;
      float var212Value = floatVal2 - 12.0F;
      float var618Value = minValue * 18;
      float floatVal4 = 28.0F + var618Value + 6.0F;
      return new Matrix4fBuffer(floatVal, floatVal3, floatVal2, floatVal4, var16Value, var36Value, maxValue, 16.0F, var1Var230Value, var36Value, 30.0F, 16.0F, var16Value2, var816Value, var212Value, var618Value);
   }

   public List dj(BlockListSetting blockListSetting) {
      ArrayList arrayListInst = new ArrayList(blockListSetting.searchBlocks(this.blockSearchQuery));
      arrayListInst.sort(Comparator.<Block, Boolean>comparing(item -> {
         return !blockListSetting.isSelected(item);
      }).thenComparing(blockListSetting::getBlockName, String.CASE_INSENSITIVE_ORDER));
      return arrayListInst;
   }

   public String de(BlockListSetting blockListSetting) {
      return "Choose";
   }

   public ItemStack dg(BlockListSetting blockListSetting) {
      Block local = (Block)blockListSetting.getSelectedBlocks().stream().findFirst().orElse(null);
      if (local == null) {
         return ItemStack.EMPTY;
      }

      ItemStack local2 = new ItemStack(local);
      return local2.isEmpty() ? ItemStack.EMPTY : local2;
   }

   public String df(String string, int intVal) {
      if (string != null && !string.isEmpty() && intVal > 0) {
         if (this.cb(string) <= intVal) {
            return string;
         }

         int intVal2 = this.cb("...");
         return intVal2 >= intVal ? this.cc(string, intVal) : this.cc(string, intVal - intVal2) + "...";
      } else {
         return "";
      }
   }

   public String cV(ModuleBase moduleBase, ClientSetting clientSetting, String string) {
      if (string != null && !string.isEmpty()) {
         return this.dJ(moduleBase, clientSetting) ? this.dK(string, 10) : string;
      } else {
         return "";
      }
   }

   public boolean dJ(ModuleBase moduleBase, ClientSetting clientSetting) {
      return moduleBase != null && clientSetting != null && "CoordSnapper".equalsIgnoreCase(moduleBase.getName2()) && clientSetting.isNamed("Webhook");
   }

   public String dK(String string, int intVal) {
      String local = string == null ? "" : string.trim();
      if (local.isEmpty()) {
         return "";
      }

      int maxValue = Math.max(local.lastIndexOf(47), local.lastIndexOf(92));
      String local2 = maxValue >= 0 && maxValue < local.length() - 1 ? local.substring(maxValue + 1) : local;
      return local2.length() <= intVal ? "..." + local2 : "..." + local2.substring(local2.length() - intVal);
   }

   public void dh(DrawContext arg, String string, float floatVal, float floatVal2, float floatVal3, int intVal) {
      if (string != null && !string.isEmpty()) {
         Matrix3x2fStack var1Value = arg.getMatrices();
         var1Value.pushMatrix();
         var1Value.translate(floatVal, floatVal2);
         var1Value.scale(floatVal3, floatVal3);
         this.ca(arg, string, 0, 0.0F, intVal, false);
         var1Value.popMatrix();
      }
   }

   public void cE(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, String string, int intVal, float floatVal5, int intVal2) {
      if (string == null) {
         string = "";
      }

      int intVal3 = (int)Math.max(0.0F, floatVal3) - (intVal - (int)floatVal) - 4;
      String local = intVal3 > 0 ? this.cc(string, intVal3) : string;
      this.caC(arg, local, intVal, floatVal2, floatVal4, intVal2, false);
   }

   public int ci(BlockListSetting blockListSetting) {
      return Math.round(this.di(0.0F, 147.0F, 0.0F, blockListSetting).rectHeight);
   }

   public int dk(int intVal, int intVal2) {
      return Math.max(0, Math.min(Math.max(0, intVal - 5), intVal2));
   }

   public Matrix4fBuffer dq(float floatVal, float floatVal2, float floatVal3, EntityListSetting entityListSetting) {
      int intVal = this.dr(entityListSetting).size();
      int minValue = Math.min(5, Math.max(1, intVal));
      float var16Value = floatVal + 6.0F;
      float var36Value = floatVal3 + 6.0F;
      float var1Var230Value = floatVal + floatVal2 - 30.0F - 6.0F;
      float maxValue = Math.max(24.0F, var1Var230Value - var16Value - 4.0F);
      float var16Value2 = floatVal + 6.0F;
      float var816Value = var36Value + 16.0F + 6.0F;
      float var212Value = floatVal2 - 12.0F;
      float var618Value = minValue * 18;
      float floatVal4 = 28.0F + var618Value + 6.0F;
      return new Matrix4fBuffer(floatVal, floatVal3, floatVal2, floatVal4, var16Value, var36Value, maxValue, 16.0F, var1Var230Value, var36Value, 30.0F, 16.0F, var16Value2, var816Value, var212Value, var618Value);
   }

   public List dr(EntityListSetting entityListSetting) {
      ArrayList arrayListInst = new ArrayList(entityListSetting.searchEntities(this.mobSearchQuery));
      arrayListInst.sort(Comparator.<EntityType, Boolean>comparing(item -> {
         return !entityListSetting.isSelected2(item);
      }).thenComparing(entityListSetting::getEntityName, String.CASE_INSENSITIVE_ORDER));
      return arrayListInst;
   }

   public int cj(EntityListSetting entityListSetting) {
      return Math.round(this.dq(0.0F, 147.0F, 0.0F, entityListSetting).rectHeight);
   }

   public int ds(int intVal, int intVal2) {
      return Math.max(0, Math.min(Math.max(0, intVal - 5), intVal2));
   }

   public String dM(EntityListSetting entityListSetting) {
      EntityType local = (EntityType)entityListSetting.getSelectedEntities().stream().findFirst().orElse(null);
      if (local == null) {
         return "Choose";
      }

      int intVal = entityListSetting.getSelectedCount2() - 1;
      return intVal > 0 ? entityListSetting.getEntityName(local) + " +" + intVal : entityListSetting.getEntityName(local);
   }

   public ItemStack dN(EntityListSetting entityListSetting) {
      EntityType local = (EntityType)entityListSetting.getSelectedEntities().stream().findFirst().orElse(null);
      return local == null ? ItemStack.EMPTY : this.dO(local);
   }

   public ItemStack dO(EntityType arg) {
      try {
         SpawnEggItem class1826Value = SpawnEggItem.forEntity(arg);
         if (class1826Value != null) {
            return new ItemStack(class1826Value);
         }
      } catch (Throwable error) {
      }

      return new ItemStack(Items.EGG);
   }

   public void cX(DrawContext arg, EntityListSetting entityListSetting, float floatVal, float floatVal2, float floatVal3, int intVal, float floatVal4) {
      String local = this.expandedMobsSetting == entityListSetting ? "v" : ">";
      int intVal2 = this.cb(local);
      int roundValue = Math.round(floatVal + floatVal2 - 6.0F - intVal2);
      int maxValue = Math.max(30, roundValue - (Math.round(floatVal) + 10 + this.cb(entityListSetting.getName()) + 14));
      int intVal3 = this.cM(this.expandedMobsSetting != entityListSetting && entityListSetting.getSelectedCount2() <= 0 ? -6642510 : -1511950, floatVal4);
      String local2 = entityListSetting.getSelectedCount2() == 0 ? "Choose" : this.dM(entityListSetting);
      String local3 = this.df(local2, Math.round((maxValue - 18) / 0.9F));
      int maxValue2 = Math.max(34, Math.min(maxValue, this.cb(local3) + 22));
      int var10Var156Value = roundValue - maxValue2 - 6;
      int intVal4 = this.cM(entityListSetting.getSelectedCount2() > 0 ? COLOR_ACCENT_DIM : -15195855, floatVal4);
      ItemStack local4 = this.dN(entityListSetting);
      this.caC(arg, entityListSetting.getName(), Math.round(floatVal) + 10, Math.round(floatVal3), 17.0F, intVal3, false);
      GuiRenderUtil.fillRoundedRect(arg, var10Var156Value, floatVal3 + 2.0F, maxValue2, 12.0F, 5.0F, intVal4, false);
      GuiRenderUtil.strokeRoundedRect(arg, var10Var156Value, floatVal3 + 2.0F, maxValue2, 12.0F, 5.0F, 1.0F, this.cM(0, floatVal4), false);
      if (!local4.isEmpty()) {
         arg.drawItem(local4, var10Var156Value + 2, Math.round(floatVal3) + 1);
      }

      this.dh(arg, local3, var10Var156Value + (local4.isEmpty() ? 6 : 16), floatVal3 + 4.0F, 0.9F, this.cM(-1511950, floatVal4));
      this.caC(arg, local, roundValue, Math.round(floatVal3), 17.0F, this.cM(-6642510, floatVal4), false);
   }

   public void db(DrawContext arg, EntityListSetting entityListSetting, float floatVal, float floatVal2, float floatVal3, int intVal, int intVal2) {
      Matrix4fBuffer local = this.dq(floatVal, floatVal2, floatVal3, entityListSetting);
      List local2 = this.dr(entityListSetting);
      this.mobPickerScroll = this.ds(local2.size(), this.mobPickerScroll);
      GuiRenderUtil.fillRoundedRect(arg, local.rectX, local.rectY, local.rectWidth, local.rectHeight, 6.0F, -15195855, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.rectX, local.rectY, local.rectWidth, local.rectHeight, 6.0F, 1.0F, 0, false);
      int intVal3 = this.mobSearchActive && this.expandedMobsSetting == entityListSetting ? COLOR_ACCENT : -14274495;
      GuiRenderUtil.fillRoundedRect(arg, local.searchX, local.searchY, local.searchWidth, local.searchHeight, 5.0F, COLOR_PANEL_BG, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.searchX, local.searchY, local.searchWidth, local.searchHeight, 5.0F, 1.0F, intVal3, false);
      GuiRenderUtil.fillRoundedRect(arg, local.clearButtonX, local.clearButtonY, local.clearButtonWidth, local.clearButtonHeight, 5.0F, 0, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.clearButtonX, local.clearButtonY, local.clearButtonWidth, local.clearButtonHeight, 5.0F, 1.0F, 0, false);
      String local3 = this.mobSearchQuery.isEmpty() ? "Search mobs..." : this.mobSearchQuery;
      if (this.mobSearchActive && this.expandedMobsSetting == entityListSetting && System.currentTimeMillis() / 500L % 2L == 0L) {
         local3 = local3 + "_";
      }

      this.cE(
         arg,
         local.searchX,
         local.searchY,
         Math.max(0.0F, local.searchWidth),
         Math.max(0.0F, local.searchHeight),
         local3,
         Math.round(local.searchX) + 6,
         Math.round(local.searchY) + 4,
         this.mobSearchQuery.isEmpty() && !this.mobSearchActive ? -6642510 : -1511950
      );
      this.caC(arg, "Clear", Math.round(local.clearButtonX) + 4, Math.round(local.clearButtonY), local.clearButtonHeight, -6642510, false);
      if (local2.isEmpty()) {
         this.caC(arg, "No mobs found", Math.round(local.listX) + 6, Math.round(local.listY), 18.0F, -6642510, false);
      } else {
         int minValue = Math.min(5, local2.size());
         boolean flag = local2.size() > minValue;

         for (int index = 0; index < minValue; index++) {
            int intVal4 = this.mobPickerScroll + index;
            if (intVal4 >= local2.size()) {
               break;
            }

            EntityType local4 = (EntityType)local2.get(intVal4);
            float floatVal4 = local.listY + index * 18;
            boolean flag2 = intVal >= local.listX && intVal <= local.listX + local.listWidth && intVal2 >= floatVal4 && intVal2 <= floatVal4 + 18.0F - 2.0F;
            boolean flag3 = entityListSetting.isSelected2(local4);
            GuiRenderUtil.fillRoundedRect(arg, local.listX, floatVal4, local.listWidth, 16.0F, 5.0F, flag3 ? 857419306 : (flag2 ? 872415231 : 0), false);
            GuiRenderUtil.strokeRoundedRect(arg, local.listX, floatVal4, local.listWidth, 16.0F, 5.0F, 1.0F, 0, false);
            ItemStack local5 = this.dO(local4);
            int roundValue = Math.round(local.listX) + 5;
            if (!local5.isEmpty()) {
               arg.drawItem(local5, Math.round(local.listX) + 2, Math.round(floatVal4) + 1);
               roundValue += 16;
            }

            float floatVal5 = local.listX + local.listWidth - 10.0F;
            this.dh(
               arg, this.df(entityListSetting.getEntityName(local4), Math.round((floatVal5 - roundValue - 4.0F) / 0.9F)), roundValue, floatVal4 + 4.0F, 0.9F, flag3 ? COLOR_ACCENT : -1511950
            );
            GuiRenderUtil.fillRoundedRect(arg, floatVal5, floatVal4 + 5.0F, 6.0F, 6.0F, 2.5F, flag3 ? COLOR_ACCENT : -15195855, false);
            GuiRenderUtil.strokeRoundedRect(arg, floatVal5, floatVal4 + 5.0F, 6.0F, 6.0F, 2.5F, 1.0F, flag3 ? COLOR_ACCENT : -14274495, false);
         }

         if (flag) {
            int maxValue = Math.max(1, local2.size() - minValue);
            float floatVal6 = local.listX + local.listWidth - 4.0F;
            float floatVal7 = local.listY + 1.0F;
            float floatVal8 = local.listHeight - 2.0F;
            float maxValue2 = Math.max(12.0F, floatVal8 * ((float)minValue / local2.size()));
            float floatVal9 = (floatVal8 - maxValue2) * ((float)this.mobPickerScroll / maxValue);
            GuiRenderUtil.fillRoundedRect(arg, floatVal6, floatVal7, 4.0F, floatVal8, 2.0F, COLOR_PANEL_BG, false);
            GuiRenderUtil.fillRoundedRect(arg, floatVal6, floatVal7 + floatVal9, 4.0F, maxValue2, 2.0F, COLOR_ACCENT_DIM, false);
         }
      }
   }

   public Matrix4fBuffer itemLayout(float floatVal, float floatVal2, float floatVal3, ItemSelectSetting itemSelectSetting) {
      int intVal = this.itemList(itemSelectSetting).size();
      int minValue = Math.min(5, Math.max(1, intVal));
      float var16Value = floatVal + 6.0F;
      float var36Value = floatVal3 + 6.0F;
      float var1Var230Value = floatVal + floatVal2 - 30.0F - 6.0F;
      float maxValue = Math.max(24.0F, var1Var230Value - var16Value - 4.0F);
      float var16Value2 = floatVal + 6.0F;
      float var816Value = var36Value + 16.0F + 6.0F;
      float var212Value = floatVal2 - 12.0F;
      float var618Value = minValue * 18;
      float floatVal4 = 28.0F + var618Value + 6.0F;
      return new Matrix4fBuffer(floatVal, floatVal3, floatVal2, floatVal4, var16Value, var36Value, maxValue, 16.0F, var1Var230Value, var36Value, 30.0F, 16.0F, var16Value2, var816Value, var212Value, var618Value);
   }

   public List itemList(ItemSelectSetting itemSelectSetting) {
      ArrayList arrayListInst = new ArrayList(itemSelectSetting.searchItems(this.itemSearchQuery));
      arrayListInst.sort(Comparator.<Item, Boolean>comparing(item -> {
         return !itemSelectSetting.isSelected3(item);
      }).thenComparing(itemSelectSetting::getItemName, String.CASE_INSENSITIVE_ORDER));
      return arrayListInst;
   }

   public int itemHeight(ItemSelectSetting itemSelectSetting) {
      return Math.round(this.itemLayout(0.0F, 147.0F, 0.0F, itemSelectSetting).rectHeight);
   }

   public int itemScrollClamp(int intVal, int intVal2) {
      return Math.max(0, Math.min(Math.max(0, intVal - 5), intVal2));
   }

   public String itemSummary(ItemSelectSetting itemSelectSetting) {
      Item local = (Item)itemSelectSetting.getSelectedItems().stream().findFirst().orElse(null);
      if (local == null) {
         return "Choose";
      }

      int intVal = itemSelectSetting.getSelectedCount3() - 1;
      return intVal > 0 ? itemSelectSetting.getItemName(local) + " +" + intVal : itemSelectSetting.getItemName(local);
   }

   public ItemStack itemPreview(ItemSelectSetting itemSelectSetting) {
      Item local = (Item)itemSelectSetting.getSelectedItems().stream().findFirst().orElse(null);
      return local == null ? ItemStack.EMPTY : new ItemStack(local);
   }

   public void crafterGridRenderRow(DrawContext arg, MultiSelectSetting multiSelectSetting, float floatVal, float floatVal2, float floatVal3, int intVal, float floatVal4) {
      String local = this.expandedCrafterGridSetting == multiSelectSetting ? "v" : ">";
      int intVal2 = this.cb(local);
      int roundValue = Math.round(floatVal + floatVal2 - 6.0F - intVal2);
      this.caC(arg, multiSelectSetting.getName(), Math.round(floatVal) + 10, Math.round(floatVal3), 17.0F, this.cM(-1511950, floatVal4), false);
      this.caC(arg, local, roundValue, Math.round(floatVal3), 17.0F, this.cM(-6642510, floatVal4), false);
   }

   public void crafterGridRenderExpanded(DrawContext arg, MultiSelectSetting multiSelectSetting, float floatVal, float floatVal2, float floatVal3, int intVal, int intVal2) {
      Boolean[] local = (Boolean[])multiSelectSetting.getValue();
      if (local != null && local.length == 9) {
         int roundValue = Math.round(floatVal) + 10;
         int roundValue2 = Math.round(floatVal3) + 4;
         byte byteVal = 24;
         byte byteVal2 = 2;

         for (int index = 0; index < 9; index++) {
            int intVal3 = index % 3;
            int var133Value = index / 3;
            int var9Var14Var11Var12Value = roundValue + intVal3 * (byteVal + byteVal2);
            int var10Var15Var11Var12Value = roundValue2 + var133Value * (byteVal + byteVal2);
            boolean flag = local[index] != null && local[index];
            int intVal4 = flag ? -929431552 : -928339286;
            int local2 = -11184811;
            GuiRenderUtil.fillRoundedRect(arg, var9Var14Var11Var12Value, var10Var15Var11Var12Value, byteVal, byteVal, 3.0F, intVal4, false);
            GuiRenderUtil.strokeRoundedRect(arg, var9Var14Var11Var12Value, var10Var15Var11Var12Value, byteVal, byteVal, 3.0F, 1.0F, local2, false);
         }
      }
   }

   public int crafterGridExpandedHeight(MultiSelectSetting multiSelectSetting) {
      return 84;
   }

   public void itemRenderRow(DrawContext arg, ItemSelectSetting itemSelectSetting, float floatVal, float floatVal2, float floatVal3, int intVal, float floatVal4) {
      String local = this.expandedItemsSetting == itemSelectSetting ? "v" : ">";
      int intVal2 = this.cb(local);
      int roundValue = Math.round(floatVal + floatVal2 - 6.0F - intVal2);
      int maxValue = Math.max(30, roundValue - (Math.round(floatVal) + 10 + this.cb(itemSelectSetting.getName()) + 14));
      int intVal3 = this.cM(this.expandedItemsSetting != itemSelectSetting && itemSelectSetting.getSelectedCount3() <= 0 ? -6642510 : -1511950, floatVal4);
      String local2 = itemSelectSetting.getSelectedCount3() == 0 ? "Choose" : this.itemSummary(itemSelectSetting);
      String local3 = this.df(local2, Math.round((maxValue - 18) / 0.9F));
      int maxValue2 = Math.max(34, Math.min(maxValue, this.cb(local3) + 22));
      int var10Var156Value = roundValue - maxValue2 - 6;
      int intVal4 = this.cM(itemSelectSetting.getSelectedCount3() > 0 ? COLOR_ACCENT_DIM : -15195855, floatVal4);
      ItemStack local4 = this.itemPreview(itemSelectSetting);
      this.caC(arg, itemSelectSetting.getName(), Math.round(floatVal) + 10, Math.round(floatVal3), 17.0F, intVal3, false);
      GuiRenderUtil.fillRoundedRect(arg, var10Var156Value, floatVal3 + 2.0F, maxValue2, 12.0F, 5.0F, intVal4, false);
      GuiRenderUtil.strokeRoundedRect(arg, var10Var156Value, floatVal3 + 2.0F, maxValue2, 12.0F, 5.0F, 1.0F, this.cM(0, floatVal4), false);
      if (!local4.isEmpty()) {
         arg.drawItem(local4, var10Var156Value + 2, Math.round(floatVal3) + 1);
      }

      this.dh(arg, local3, var10Var156Value + (local4.isEmpty() ? 6 : 16), floatVal3 + 4.0F, 0.9F, this.cM(-1511950, floatVal4));
      this.caC(arg, local, roundValue, Math.round(floatVal3), 17.0F, this.cM(-6642510, floatVal4), false);
   }

   public void itemRenderExpanded(DrawContext arg, ItemSelectSetting itemSelectSetting, float floatVal, float floatVal2, float floatVal3, int intVal, int intVal2) {
      Matrix4fBuffer local = this.itemLayout(floatVal, floatVal2, floatVal3, itemSelectSetting);
      List local2 = this.itemList(itemSelectSetting);
      this.itemPickerScroll = this.itemScrollClamp(local2.size(), this.itemPickerScroll);
      GuiRenderUtil.fillRoundedRect(arg, local.rectX, local.rectY, local.rectWidth, local.rectHeight, 6.0F, -15195855, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.rectX, local.rectY, local.rectWidth, local.rectHeight, 6.0F, 1.0F, 0, false);
      int intVal3 = this.itemSearchActive && this.expandedItemsSetting == itemSelectSetting ? COLOR_ACCENT : -14274495;
      GuiRenderUtil.fillRoundedRect(arg, local.searchX, local.searchY, local.searchWidth, local.searchHeight, 5.0F, COLOR_PANEL_BG, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.searchX, local.searchY, local.searchWidth, local.searchHeight, 5.0F, 1.0F, intVal3, false);
      GuiRenderUtil.fillRoundedRect(arg, local.clearButtonX, local.clearButtonY, local.clearButtonWidth, local.clearButtonHeight, 5.0F, 0, false);
      GuiRenderUtil.strokeRoundedRect(arg, local.clearButtonX, local.clearButtonY, local.clearButtonWidth, local.clearButtonHeight, 5.0F, 1.0F, 0, false);
      String local3 = this.itemSearchQuery.isEmpty() ? "Search items..." : this.itemSearchQuery;
      if (this.itemSearchActive && this.expandedItemsSetting == itemSelectSetting && System.currentTimeMillis() / 500L % 2L == 0L) {
         local3 = local3 + "_";
      }

      this.cE(
         arg,
         local.searchX,
         local.searchY,
         Math.max(0.0F, local.searchWidth),
         Math.max(0.0F, local.searchHeight),
         local3,
         Math.round(local.searchX) + 6,
         Math.round(local.searchY) + 4,
         this.itemSearchQuery.isEmpty() && !this.itemSearchActive ? -6642510 : -1511950
      );
      this.caC(arg, "Clear", Math.round(local.clearButtonX) + 4, Math.round(local.clearButtonY), local.clearButtonHeight, -6642510, false);
      if (local2.isEmpty()) {
         this.caC(arg, "No items found", Math.round(local.listX) + 6, Math.round(local.listY), 18.0F, -6642510, false);
      } else {
         int minValue = Math.min(5, local2.size());
         boolean flag = local2.size() > minValue;

         for (int index = 0; index < minValue; index++) {
            int intVal4 = this.itemPickerScroll + index;
            if (intVal4 >= local2.size()) {
               break;
            }

            Item local4 = (Item)local2.get(intVal4);
            float floatVal4 = local.listY + index * 18;
            boolean flag2 = intVal >= local.listX && intVal <= local.listX + local.listWidth && intVal2 >= floatVal4 && intVal2 <= floatVal4 + 18.0F - 2.0F;
            boolean flag3 = itemSelectSetting.isSelected3(local4);
            GuiRenderUtil.fillRoundedRect(arg, local.listX, floatVal4, local.listWidth, 16.0F, 5.0F, flag3 ? 857419306 : (flag2 ? 872415231 : 0), false);
            GuiRenderUtil.strokeRoundedRect(arg, local.listX, floatVal4, local.listWidth, 16.0F, 5.0F, 1.0F, 0, false);
            ItemStack local5 = new ItemStack(local4);
            int roundValue = Math.round(local.listX) + 5;
            if (!local5.isEmpty()) {
               arg.drawItem(local5, Math.round(local.listX) + 2, Math.round(floatVal4) + 1);
               roundValue += 16;
            }

            float floatVal5 = local.listX + local.listWidth - 10.0F;
            this.dh(
               arg, this.df(itemSelectSetting.getItemName(local4), Math.round((floatVal5 - roundValue - 4.0F) / 0.9F)), roundValue, floatVal4 + 4.0F, 0.9F, flag3 ? COLOR_ACCENT : -1511950
            );
            GuiRenderUtil.fillRoundedRect(arg, floatVal5, floatVal4 + 5.0F, 6.0F, 6.0F, 2.5F, flag3 ? COLOR_ACCENT : -15195855, false);
            GuiRenderUtil.strokeRoundedRect(arg, floatVal5, floatVal4 + 5.0F, 6.0F, 6.0F, 2.5F, 1.0F, flag3 ? COLOR_ACCENT : -14274495, false);
         }

         if (flag) {
            int maxValue = Math.max(1, local2.size() - minValue);
            float floatVal6 = local.listX + local.listWidth - 4.0F;
            float floatVal7 = local.listY + 1.0F;
            float floatVal8 = local.listHeight - 2.0F;
            float maxValue2 = Math.max(12.0F, floatVal8 * ((float)minValue / local2.size()));
            float floatVal9 = (floatVal8 - maxValue2) * ((float)this.itemPickerScroll / maxValue);
            GuiRenderUtil.fillRoundedRect(arg, floatVal6, floatVal7, 4.0F, floatVal8, 2.0F, COLOR_PANEL_BG, false);
            GuiRenderUtil.fillRoundedRect(arg, floatVal6, floatVal7 + floatVal9, 4.0F, maxValue2, 2.0F, COLOR_ACCENT_DIM, false);
         }
      }
   }

   public GuiSettingRect dt(float floatVal, float floatVal2, float floatVal3, int intVal) {
      float var14Value = floatVal + 4.0F;
      float var3Var46Value = floatVal3 + intVal + 6.0F;
      float floatVal4 = 80.0F;
      float floatVal5 = 80.0F;
      float var5Var76Value = var14Value + floatVal4 + 6.0F;
      float floatVal6 = 24.0F;
      return new GuiSettingRect(var14Value, var3Var46Value, floatVal4, floatVal5, var5Var76Value, floatVal6);
   }

   public boolean dw(double doubleVal, double doubleVal2) {
      int intVal = this.cA() + this.verticalScroll;

      for (int index = 0; index < CACHED_CATEGORIES.length; index++) {
         ModuleCategory local = CACHED_CATEGORIES[index];
         int intVal2 = this.cy(local, index);
         int var5226Value = intVal + 22 + 6;

         for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)cachedCategory(local)) {
            if (this.cK(moduleBase)) {
               var5226Value += 19;
               if (moduleBase.isExpanded()) {
                  var5226Value += 19;
                  var5226Value += 19;

                  for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase.getSettings()) {
                     if (clientSetting == this.activeColorSetting && clientSetting.getValue() instanceof Color) {
                        GuiSettingRect local2 = this.dt(intVal2 + 4, 147.0F, var5226Value, 17);
                        if (this.colorDragMode == ColorDragMode.FIELD) {
                           this.du(clientSetting, local2, doubleVal, doubleVal2);
                        } else if (this.colorDragMode == ColorDragMode.ALPHA) {
                           this.dv(clientSetting, local2, doubleVal2);
                        }

                        return true;
                     }

                     var5226Value += 19;
                     if (clientSetting instanceof BlockListSetting local3 && this.expandedBlocksSetting == local3) {
                        var5226Value += this.ci(local3);
                     }

                     if (clientSetting instanceof EntityListSetting local4 && this.expandedMobsSetting == local4) {
                        var5226Value += this.cj(local4);
                     }

                     if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                        var5226Value += 112;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public SettingLayoutPair dy() {
      if (this.expandedMobsSetting == null) {
         return null;
      }

      int intVal = this.cA() + this.verticalScroll;

      for (int index = 0; index < CACHED_CATEGORIES.length; index++) {
         ModuleCategory local = CACHED_CATEGORIES[index];
         int intVal2 = this.cy(local, index);
         int var1226Value = intVal + 22 + 6;

         for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)cachedCategory(local)) {
            if (this.cK(moduleBase)) {
               var1226Value += 19;
               if (moduleBase.isExpanded()) {
                  var1226Value += 19;
                  var1226Value += 19;

                  for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase.getSettings()) {
                     if (clientSetting == this.expandedMobsSetting) {
                        return new SettingLayoutPair(this.expandedMobsSetting, this.dq(intVal2 + 4, 147.0F, var1226Value + 19, this.expandedMobsSetting));
                     }

                     var1226Value += 19;
                     if (clientSetting instanceof BlockListSetting local2 && this.expandedBlocksSetting == local2) {
                        var1226Value += this.ci(local2);
                     }

                     if (clientSetting instanceof EntityListSetting local3 && this.expandedMobsSetting == local3) {
                        var1226Value += this.cj(local3);
                     }

                     if (clientSetting instanceof ItemSelectSetting local4 && this.expandedItemsSetting == local4) {
                        var1226Value += this.itemHeight(local4);
                     }

                     if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                        var1226Value += 112;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   public ItemPickerGuiEntry itemDZ() {
      if (this.expandedItemsSetting == null) {
         return null;
      }

      int intVal = this.cA() + this.verticalScroll;

      for (int index = 0; index < CACHED_CATEGORIES.length; index++) {
         ModuleCategory local = CACHED_CATEGORIES[index];
         int intVal2 = this.cy(local, index);
         int var1226Value = intVal + 22 + 6;

         for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)cachedCategory(local)) {
            if (this.cK(moduleBase)) {
               var1226Value += 19;
               if (moduleBase.isExpanded()) {
                  var1226Value += 19;
                  var1226Value += 19;

                  for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase.getSettings()) {
                     if (clientSetting == this.expandedItemsSetting) {
                        return new ItemPickerGuiEntry(this.expandedItemsSetting, this.itemLayout(intVal2 + 4, 147.0F, var1226Value + 19, this.expandedItemsSetting));
                     }

                     var1226Value += 19;
                     if (clientSetting instanceof BlockListSetting local2 && this.expandedBlocksSetting == local2) {
                        var1226Value += this.ci(local2);
                     }

                     if (clientSetting instanceof EntityListSetting local3 && this.expandedMobsSetting == local3) {
                        var1226Value += this.cj(local3);
                     }

                     if (clientSetting instanceof ItemSelectSetting local4 && this.expandedItemsSetting == local4) {
                        var1226Value += this.itemHeight(local4);
                     }

                     if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                        var1226Value += 112;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   public BlockPickerGuiEntry dx() {
      if (this.expandedBlocksSetting == null) {
         return null;
      }

      int intVal = this.cA() + this.verticalScroll;

      for (int index = 0; index < CACHED_CATEGORIES.length; index++) {
         ModuleCategory local = CACHED_CATEGORIES[index];
         int intVal2 = this.cy(local, index);
         int var1226Value = intVal + 22 + 6;

         for (ModuleBase moduleBase : (Iterable<ModuleBase>)(Iterable)cachedCategory(local)) {
            if (this.cK(moduleBase)) {
               var1226Value += 19;
               if (moduleBase.isExpanded()) {
                  var1226Value += 19;
                  var1226Value += 19;

                  for (ClientSetting clientSetting : (Iterable<ClientSetting>)(Iterable)moduleBase.getSettings()) {
                     if (clientSetting == this.expandedBlocksSetting) {
                        return new BlockPickerGuiEntry(this.expandedBlocksSetting, this.di(intVal2 + 4, 147.0F, var1226Value + 19, this.expandedBlocksSetting));
                     }

                     var1226Value += 19;
                     if (clientSetting instanceof BlockListSetting local2 && this.expandedBlocksSetting == local2) {
                        var1226Value += this.ci(local2);
                     }

                     if (clientSetting instanceof EntityListSetting local3 && this.expandedMobsSetting == local3) {
                        var1226Value += this.cj(local3);
                     }

                     if (clientSetting instanceof ItemSelectSetting local4 && this.expandedItemsSetting == local4) {
                        var1226Value += this.itemHeight(local4);
                     }

                     if (clientSetting.getValue() instanceof Color && this.expandedColorSetting == clientSetting) {
                        var1226Value += 112;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   public boolean dC(KeyInput arg) {
      return arg.isEscape() || arg.isEnter();
   }

   public boolean dA(KeyInput arg) {
      if (arg.getKeycode() == 259) {
         this.blockSearchQuery = this.dD(this.blockSearchQuery);
         this.blockPickerScroll = 0;
         return true;
      }

      if (arg.isPaste()) {
         this.blockSearchQuery = this.blockSearchQuery + this.dE();
         this.blockPickerScroll = 0;
         return true;
      }

      if (!arg.isEscape() && !arg.isEnter()) {
         return true;
      }

      this.blockSearchActive = false;
      return true;
   }

   public boolean dB(KeyInput arg) {
      if (arg.getKeycode() == 259) {
         this.mobSearchQuery = this.dD(this.mobSearchQuery);
         this.mobPickerScroll = 0;
         return true;
      }

      if (arg.isPaste()) {
         this.mobSearchQuery = this.mobSearchQuery + this.dE();
         this.mobPickerScroll = 0;
         return true;
      }

      if (!arg.isEscape() && !arg.isEnter()) {
         return true;
      }

      this.mobSearchActive = false;
      return true;
   }

   public boolean itemKey(KeyInput arg) {
      if (arg.getKeycode() == 259) {
         this.itemSearchQuery = this.dD(this.itemSearchQuery);
         this.itemPickerScroll = 0;
         return true;
      }

      if (arg.isPaste()) {
         this.itemSearchQuery = this.itemSearchQuery + this.dE();
         this.itemPickerScroll = 0;
         return true;
      }

      if (!arg.isEscape() && !arg.isEnter()) {
         return true;
      }

      this.itemSearchActive = false;
      return true;
   }

   public boolean dF(KeyInput arg) {
      if (arg.getKeycode() == 259) {
         this.listeningString.setValue(this.dD((String)this.listeningString.getValue()));
         return true;
      }

      if (arg.isPaste()) {
         this.listeningString.setValue((String)this.listeningString.getValue() + this.dE());
         return true;
      }

      if (!arg.isEscape() && !arg.isEnter()) {
         return true;
      }

      this.listeningString = null;
      return true;
   }

   public int cA() {
      return 16;
   }

   public int dP() {
      int maxValue = 0;

      for (ModuleCategory moduleCategory : CACHED_CATEGORIES) {
         maxValue = Math.max(maxValue, this.cF(moduleCategory));
      }

      return maxValue;
   }

   public int cD(int intVal) {
      int maxValue = Math.max(0, this.ct() - this.cA() - 16);
      int minValue = Math.min(0, maxValue - this.dP());
      return Math.max(minValue, Math.min(0, intVal));
   }

   public String dE() {
      return this.dz(MinecraftClient.getInstance().keyboard.getClipboard());
   }

   public String dz(String string) {
      if (string != null && !string.isEmpty()) {
         StringBuilder stringBuilderInst = new StringBuilder(string.length());
         string.codePoints().filter(item -> {
            return !Character.isISOControl(item);
         }).forEach(stringBuilderInst::appendCodePoint);
         return stringBuilderInst.toString();
      } else {
         return "";
      }
   }

   public String dD(String string) {
      return string != null && !string.isEmpty() ? string.substring(0, string.offsetByCodePoints(string.length(), -1)) : "";
   }

   public void du(ClientSetting clientSetting, GuiSettingRect guiSettingRect, double doubleVal, double doubleVal2) {
      Color local = (Color)clientSetting.getValue();
      float floatVal = this.getHue(local);
      float floatVal2 = this.cP((float)((doubleVal - guiSettingRect.colorRectY) / guiSettingRect.colorRectHeight));
      float floatVal3 = 1.0F - this.cP((float)((doubleVal2 - guiSettingRect.colorRectY) / guiSettingRect.colorRectHeight));
      int colorValue = Color.HSBtoRGB(floatVal, floatVal2, floatVal3);
      clientSetting.setValue(new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, local.getAlpha()));
   }

   public void dv(ClientSetting clientSetting, GuiSettingRect guiSettingRect, double doubleVal) {
      float floatVal = this.cP((float)((doubleVal - guiSettingRect.colorRectY) / guiSettingRect.colorRectHeight));
      Color local = (Color)clientSetting.getValue();
      int colorValue = Color.HSBtoRGB(floatVal, this.getSaturation(local), this.getBrightness(local));
      clientSetting.setValue(new Color(colorValue >> 16 & 0xFF, colorValue >> 8 & 0xFF, colorValue & 0xFF, local.getAlpha()));
   }

   public boolean cR(double doubleVal, double doubleVal2, float floatVal, float floatVal2, float floatVal3, float floatVal4) {
      return doubleVal >= floatVal && doubleVal <= floatVal + floatVal3 && doubleVal2 >= floatVal2 && doubleVal2 <= floatVal2 + floatVal4;
   }

   public boolean cT(ModuleBase moduleBase) {
      if (moduleBase == null) {
         return false;
      }

      String local = moduleBase.getName2() == null ? "" : moduleBase.getName2().toLowerCase().replace(" ", "");
      return local.equals("swingspeed")
         || local.equals("freelook")
         || local.equals("fastplace")
         || local.equals("playeresp")
         || local.equals("storageesp")
         || local.equals("freecam")
         || local.equals("holeesp")
         || local.equals("jumpcircles")
         || local.equals("autototem")
         || local.equals("autoinvtotem")
         || local.equals("hitbox")
         || local.equals("anchormacro")
         || local.equals("autocrystal")
         || local.equals("doubleanchor")
         || local.equals("triggerbot")
         || local.equals("shieldbreaker")
         || local.equals("spotifyhud")
         || local.equals("threesix+")
         || local.equals("hud")
         || local.equals("spawnernotifier")
         || local.equals("nametags");
   }

   public void handleNumericDrag(ModuleBase moduleBase, ClientSetting clientSetting, double doubleVal, int intVal) {
      double maxValue = Math.max(0.0, Math.min(1.0, (doubleVal - (intVal + 10)) / 135.0));
      boolean flag = this.cT(moduleBase);
      if (clientSetting.getValue() instanceof Float && clientSetting.getMinValue() instanceof Float && clientSetting.getMaxValue() instanceof Float) {
         float floatVal = (Float)clientSetting.getMinValue();
         float floatVal2 = (Float)clientSetting.getMaxValue();
         float floatVal3 = (float)(floatVal + (floatVal2 - floatVal) * maxValue);
         if (!flag) {
            floatVal3 = Math.round(floatVal3);
         }

         clientSetting.setValue(floatVal3);
      } else if (clientSetting.getValue() instanceof Integer && clientSetting.getMinValue() instanceof Integer && clientSetting.getMaxValue() instanceof Integer) {
         int intVal2 = (Integer)clientSetting.getMinValue();
         int intVal3 = (Integer)clientSetting.getMaxValue();
         int intVal4 = (int)Math.round(intVal2 + (intVal3 - intVal2) * maxValue);
         clientSetting.setValue(Math.max(intVal2, Math.min(intVal3, intVal4)));
      } else if (clientSetting.getValue() instanceof Double && clientSetting.getMinValue() instanceof Double && clientSetting.getMaxValue() instanceof Double) {
         double doubleVal2 = (Double)clientSetting.getMinValue();
         double doubleVal3 = (Double)clientSetting.getMaxValue();
         double roundValue = doubleVal2 + (doubleVal3 - doubleVal2) * maxValue;
         if (!flag) {
            roundValue = Math.round(roundValue);
         }

         clientSetting.setValue(roundValue);
      }
   }

   public float cP(float floatVal) {
      return Math.max(0.0F, Math.min(1.0F, floatVal));
   }

   public int dm(int intVal, float floatVal) {
      int maxValue = Math.max(0, Math.min(255, Math.round(floatVal * 255.0F)));
      return intVal & 16777215 | maxValue << 24;
   }

   public int cM(int intVal, float floatVal) {
      int intVal2 = intVal >> 24 & 0xFF;
      int maxValue = Math.max(0, Math.min(255, Math.round(intVal2 * floatVal)));
      return intVal & 16777215 | maxValue << 24;
   }

   public int dl(Color color, float floatVal) {
      int maxValue = Math.max(0, Math.min(255, Math.round(color.getAlpha() * floatVal)));
      return maxValue << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   public float cO(float floatVal) {
      float floatVal2 = this.cP(floatVal);
      return 1.0F - (float)Math.pow(1.0F - floatVal2, 3.0);
   }

   public void renderBackground(DrawContext arg, int intVal, int intVal2, float floatVal) {
   }

   public static String dR() {
      return "W";
   }

}
