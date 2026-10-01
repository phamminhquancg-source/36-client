package com.threesix.manager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import com.threesix.util.XorBitUtils;
import com.threesix.data.NotificationEntry;
import com.threesix.module.HudModule;
import com.threesix.util.GuiRenderUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.util.StringVaultDecoder;

public final class NotificationHudManager {
   public static final NotificationHudManager INSTANCE6 = new NotificationHudManager();
   public static final long NOTIFICATION_LIFETIME_MS = 3500L;
   public static final long NOTIFICATION_SLIDE_MS = 220L;
   public static final int MAX_NOTIFICATIONS = 5;
   public static final float NOTIFICATION_WIDTH = 170.0F;
   public static final float NOTIFICATION_HEIGHT = 36.0F;
   public static final float CORNER_RADIUS = 5.0F;
   public static final float ICON_SIZE = 16.0F;
   public static final float ICON_PADDING = 7.0F;
   public static final float SHADOW_OFFSET = 3.0F;
   public final List notifications = new CopyOnWriteArrayList();

   public void notify(String string, boolean flag) {
      this.notifyWithIcon(string, flag, ItemStack.EMPTY);
   }

   public void notifyWithIcon(String string, boolean flag, ItemStack arg) {
      this.push(string, flag ? "Enabled" : "Disabled", arg, flag ? -12799933 : -1096636);
   }

   public void render3(DrawContext arg) {
      this.renderNotifications(arg);
   }

   public void push(String string, String string2, ItemStack arg, int intVal) {
      this.notifications
         .add(
            0,
            new NotificationEntry(
               string == null ? "" : string, string2 == null ? "" : string2, arg == null ? ItemStack.EMPTY : arg.copy(), intVal, Util.getMeasuringTimeMs()
            )
         );

      while (this.notifications.size() > 5) {
         this.notifications.remove(this.notifications.size() - 1);
      }

      try {
         HudModule.pushToast(string, string2, intVal, null);
      } catch (Throwable error) {
      }
   }

   public void renderNotifications(DrawContext arg) {
      if (!this.notifications.isEmpty()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         long class156Value = Util.getMeasuringTimeMs();
         this.notifications.removeIf(item -> {
            return ((NotificationEntry)item).isExpired(class156Value);
         });
         if (!this.notifications.isEmpty()) {
            arg.createNewRootLayer();
            float var2Value = mc.getWindow().getScaledWidth() - 170.0F - 8.0F;
            float var2Value2 = mc.getWindow().getScaledHeight() - 36.0F - 8.0F;
            float clickGuiModuleValue = ClickGuiModule.getBaseCornerRadius();

            for (int index = 0; index < this.notifications.size(); index++) {
               NotificationEntry local = (NotificationEntry)this.notifications.get(index);
               float floatVal = local.getFadeProgress(class156Value);
               if (!(floatVal <= 0.0F)) {
                  float var51Value = var2Value + (1.0F - floatVal) * 184.0F;
                  float var6Var841Value = var2Value2 - index * 41.0F;
                  int intVal = applyAlpha(ClickGuiModule.getBackgroundColorArgb(), floatVal * 0.97F * HudModule.getGlobalAlphaScale());
                  int intVal2 = applyAlpha(-16777216, floatVal * 0.45F * HudModule.getGlobalAlphaScale());
                  GuiRenderUtil.fillRoundedRect(arg, var51Value + 2.0F, var6Var841Value + 2.0F, 170.0F, 36.0F, clickGuiModuleValue, intVal2, false);
                  GuiRenderUtil.fillRoundedRect(arg, var51Value, var6Var841Value, 170.0F, 36.0F, clickGuiModuleValue, intVal, false);
                  float var117Value = var51Value + 7.0F + 3.0F;
                  float var1210Value = var6Var841Value + 10.0F;
                  int intVal3 = applyAlpha(-15724528, floatVal * 0.7F);
                  GuiRenderUtil.fillRoundedRect(arg, var117Value - 2.0F, var1210Value - 2.0F, 20.0F, 20.0F, clickGuiModuleValue * 0.5F, intVal3, false);
                  if (!local.icon.isEmpty()) {
                     arg.drawItem(local.icon, (int)var117Value, (int)var1210Value);
                  }

                  HudModule hudModuleValue = HudModule.instance;
                  int intVal4 = applyAlpha(-1, floatVal);
                  int intVal5 = applyAlpha(local.color | 0xFF000000, floatVal);
                  float var1616Value = var117Value + 16.0F + 6.0F;
                  hudModuleValue.call1(arg, local.title, var1616Value, var6Var841Value + 8.0F, intVal4);
                  hudModuleValue.call1(arg, local.message, var1616Value, var6Var841Value + 20.0F, intVal5);
               }
            }
         }
      }
   }

   public static int applyAlpha(int intVal, float floatVal) {

      int maxValue = Math.max(0, Math.min(255, Math.round((intVal >>> 24 & 0xFF) * floatVal)));
      return intVal & 16777215 | maxValue << 24;
   }

   public static int lerpColorRgb(int intVal, int intVal2, float floatVal) {

      int intVal3 = intVal >> 16 & 0xFF;
      int intVal4 = intVal >> 8 & 0xFF;
      int intVal5 = intVal & 0xFF;
      int intVal6 = intVal2 >> 16 & 0xFF;
      int intVal7 = intVal2 >> 8 & 0xFF;
      int intVal8 = intVal2 & 0xFF;
      int intVal9 = (int)(intVal3 + (intVal6 - intVal3) * floatVal);
      int intVal10 = (int)(intVal4 + (intVal7 - intVal4) * floatVal);
      int intVal11 = (int)(intVal5 + (intVal8 - intVal5) * floatVal);
      return 0xFF000000 | intVal9 << 16 | intVal10 << 8 | intVal11;
   }

   public static String getVaultKey3() {

      return "R";
   }

}
