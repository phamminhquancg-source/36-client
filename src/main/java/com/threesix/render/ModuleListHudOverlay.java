package com.threesix.render;

import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.GuiRenderUtil;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.util.StringVaultDecoder;

public final class ModuleListHudOverlay {
   public static final ModuleListHudOverlay INSTANCE4 = new ModuleListHudOverlay();
   public static final int ROW_BACKGROUND_COLOR = -652993496;
   public static final int ROW_BORDER_COLOR = -869253035;
   public static final int TEXT_COLOR = -854277;
   public static final int ACCENT_BAR_COLOR = -9710683;
   public static final float ROW_CORNER_RADIUS = 6.0F;
   public static final int RIGHT_MARGIN = 72;

   public void render2(DrawContext arg) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.options != null && !mc.getDebugHud().shouldShowDebugHud() && !(mc.currentScreen instanceof ClickGuiScreen)) {
         TextRenderer local = mc.textRenderer;
         ArrayList arrayListInst = new ArrayList();

         for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
            if (moduleBase.isEnabled()) {
               arrayListInst.add(moduleBase);
            }
         }

         if (!arrayListInst.isEmpty()) {
            arrayListInst.sort(Comparator.<ModuleBase>comparingInt(item -> {
               return local.getWidth(item.getName2());
            }).reversed());
            int var2Value = mc.getWindow().getScaledWidth() - 10;
            int local3 = 72;

            for (ModuleBase moduleBase2 : (Iterable<ModuleBase>)arrayListInst) {
               String local2 = moduleBase2.getName2();
               int var3Value = local.getWidth(local2);
               int var1014Value = var3Value + 14;
               byte byteVal = 14;
               int var14Var11Value = var2Value - var1014Value;
               GuiRenderUtil.fillRoundedRect(arg, var14Var11Value, local3, var1014Value, byteVal, 6.0F, -652993496, false);
               GuiRenderUtil.strokeRoundedRect(arg, var14Var11Value, local3, var1014Value, byteVal, 6.0F, 1.0F, -869253035, false);
               GuiRenderUtil.fillRoundedRect(arg, var14Var11Value, local3, 2.0F, byteVal, 2.0F, -9710683, false);
               arg.drawText(local, local2, var14Var11Value + 6, local3 + 3, -854277, false);
               local3 += byteVal + 4;
            }
         }
      }
   }

   public static String getVaultKey() {
      return "E";
   }

}
