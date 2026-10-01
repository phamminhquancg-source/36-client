package com.threesix.manager;

import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import com.threesix.module.SpotifyHudModule;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.module.HudModule;
import com.threesix.util.GuiRenderUtil;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.data.HudElementType;
import com.threesix.util.StringVaultDecoder;

public final class HudLayoutManager {
   public static final HudLayoutManager INSTANCE5 = new HudLayoutManager();
   public static final int GRID_SIZE = 12;
   public String draggingElementName = null;
   public int dragOffsetX;
   public int dragOffsetY;
   public boolean isDraggingElement = false;
   public String resizingElementName = null;
   public int resizeStartMouseX;
   public int resizeStartMouseY;
   public float resizeStartScale;
   public static volatile boolean isLayoutEditing = false;

   public static float getGlobalHudScale() {
      return HudModule.getHudScale();
   }

   public static float getElementScale(HudElementType hudElementType) {
      return hudElementType == HudElementType.SPOTIFY_HUD ? HudModule.call9(hudElementType) : HudModule.getScaledFactor(hudElementType);
   }

   public static int[] getScaledBounds(HudElementType hudElementType, int intVal, int intVal2) {

      float floatVal = hudElementType == HudElementType.SPOTIFY_HUD ? 1.0F : getElementScale(hudElementType);
      if (!(floatVal > 0.0F)) {
         floatVal = 1.0F;
      }

      if (hudElementType == HudElementType.MODULE_LIST) {
         int[] hudModuleValue = HudModule.call5();
         int maxValue = Math.max(1, hudModuleValue[2]);
         int maxValue2 = Math.max(1, hudModuleValue[3]);
         return new int[]{Math.round((intVal - maxValue) * floatVal), Math.round(intVal2 * floatVal), Math.round(maxValue * floatVal), Math.round(maxValue2 * floatVal)};
      }

      int maxValue3;
      int maxValue4;
      if (hudElementType == HudElementType.SPOTIFY_HUD) {
         maxValue3 = Math.max(1, SpotifyHudModule.getSpotifyPanelWidth());
         maxValue4 = Math.max(1, SpotifyHudModule.getSpotifyPanelHeight());
      } else {
         int[] hudModuleValue2 = HudModule.call8(hudElementType);
         maxValue3 = Math.max(1, hudModuleValue2[2]);
         maxValue4 = Math.max(1, hudModuleValue2[3]);
      }

      return new int[]{Math.round(intVal * floatVal), Math.round(intVal2 * floatVal), Math.round(maxValue3 * floatVal), Math.round(maxValue4 * floatVal)};
   }

   public static boolean isElementDormant(HudElementType hudElementType) {
      try {

         MinecraftClient mc = MinecraftClient.getInstance();
         if (hudElementType == HudElementType.MODULE_LIST) {
            for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
               if ((moduleBase.isEnabled() || (Float)(Float)(Float)HudModule.moduleListAnimations.getOrDefault(moduleBase, 0.0F) > 0.02F) && moduleBase.getCategory() != ModuleCategory.CLIENT) {
                  return false;
               }
            }

            return true;
         }

         if (hudElementType == HudElementType.POTION_EFFECTS) {
            if (mc != null && mc.player != null) {
               return mc.player.getStatusEffects().isEmpty();
            }

            return false;
         }

         if (hudElementType == HudElementType.KEYBINDS) {
            return HudModule.getBoundModules().isEmpty();
         }

         if (hudElementType == HudElementType.ARMOR) {
            if (mc != null && mc.player != null) {
               for (EquipmentSlot class1304 : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                  try {
                     ItemStack local = mc.player.getEquippedStack(class1304);
                     if (local != null && !local.isEmpty()) {
                        return false;
                     }
                  } catch (Throwable error) {
                  }
               }

               return true;
            }

            return false;
         }
      } catch (Throwable error2) {
      }

      return false;
   }

   public static void setElementPosition(HudElementType hudElementType, int intVal, int intVal2) {
      try {
         int[] hudModuleValue = HudModule.call8(hudElementType);
         int intVal3 = hudModuleValue[0];
         int intVal4 = hudModuleValue[1];
         int[] hudModuleValue2 = HudModule.call4(hudElementType, intVal, intVal2);
         if (!isPositionFree(hudElementType, hudModuleValue2[0], hudModuleValue2[1])) {
            HudModule.saveHudElementPos(hudElementType, hudModuleValue2[0], hudModuleValue2[1]);
            return;
         }

         int[] hudModuleValue3 = HudModule.call4(hudElementType, hudModuleValue2[0], intVal4);
         if (!isPositionFree(hudElementType, hudModuleValue3[0], hudModuleValue3[1])) {
            HudModule.saveHudElementPos(hudElementType, hudModuleValue3[0], hudModuleValue3[1]);
            return;
         }

         int[] hudModuleValue4 = HudModule.call4(hudElementType, intVal3, hudModuleValue2[1]);
         if (!isPositionFree(hudElementType, hudModuleValue4[0], hudModuleValue4[1])) {
            HudModule.saveHudElementPos(hudElementType, hudModuleValue4[0], hudModuleValue4[1]);
            return;
         }

         if (isPositionFree(hudElementType, intVal3, intVal4)) {
            HudModule.saveHudElementPos(hudElementType, hudModuleValue2[0], hudModuleValue2[1]);
         }
      } catch (Throwable error) {
      }
   }

   public static boolean isPositionFree(HudElementType hudElementType, int intVal, int intVal2) {
      try {

         if (isElementDormant(hudElementType)) {
            return false;
         }

         int[] local = getScaledBounds(hudElementType, intVal, intVal2);

         for (HudElementType hudElementType2 : HudElementType.values()) {
            if (hudElementType2 != hudElementType && HudModule.call14(hudElementType2) && !isElementDormant(hudElementType2)) {
               int[] local2;
               if (hudElementType2 == HudElementType.MODULE_LIST) {
                  int[] hudModuleValue = HudModule.call5();
                  local2 = getScaledBounds(hudElementType2, hudModuleValue[0], hudModuleValue[1]);
               } else if (hudElementType2 == HudElementType.SPOTIFY_HUD) {
                  int[] hudModuleValue2 = HudModule.call8(hudElementType2);
                  local2 = getScaledBounds(hudElementType2, hudModuleValue2[0], hudModuleValue2[1]);
               } else {
                  int[] hudModuleValue3 = HudModule.call8(hudElementType2);
                  float floatVal = getElementScale(hudElementType2);
                  if (!(floatVal > 0.0F)) {
                     floatVal = 1.0F;
                  }

                  local2 = new int[]{Math.round(hudModuleValue3[0] * floatVal), Math.round(hudModuleValue3[1] * floatVal), Math.round(hudModuleValue3[2] * floatVal), Math.round(hudModuleValue3[3] * floatVal)};
               }

               if (local[0] < local2[0] + local2[2] && local2[0] < local[0] + local[2] && local[1] < local2[1] + local2[3] && local2[1] < local[1] + local[3]) {
                  return true;
               }
            }
         }
      } catch (Throwable error) {
      }

      return false;
   }

   public boolean onMouseButton(double doubleVal, double doubleVal2, int intVal) {
      if (intVal != 0 && intVal != 1) {
         return false;
      }

      for (HudElementType hudElementType : HudElementType.values()) {
         if (HudModule.call14(hudElementType) && hasVisibleContent(hudElementType)) {
            int[] local = getElementPosition(hudElementType);
            int intVal2 = getElementWidth(hudElementType);
            int intVal3 = getElementHeight(hudElementType);
            if (intVal == 0 && isOverResizeHandle(doubleVal, doubleVal2, local, intVal2, intVal3)) {
               this.resizingElementName = hudElementType.name();
               this.resizeStartMouseX = (int)doubleVal;
               this.resizeStartMouseY = (int)doubleVal2;
               this.resizeStartScale = getRawElementScale(hudElementType);
               this.isDraggingElement = false;
               this.draggingElementName = null;
               isLayoutEditing = true;
               return true;
            }

            if (doubleVal >= local[0] && doubleVal <= local[0] + intVal2 && doubleVal2 >= local[1] && doubleVal2 <= local[1] + intVal3 && intVal == 0) {
               this.draggingElementName = hudElementType.name();
               this.dragOffsetX = (int)(doubleVal - local[0]);
               this.dragOffsetY = (int)(doubleVal2 - local[1]);
               this.isDraggingElement = true;
               this.resizingElementName = null;
               isLayoutEditing = true;
               return true;
            }
         }
      }

      if (intVal == 0) {
         this.draggingElementName = null;
         this.isDraggingElement = false;
         this.resizingElementName = null;
      }

      return false;
   }

   public boolean isInteractionActive() {
      return this.draggingElementName != null && this.isDraggingElement || this.resizingElementName != null;
   }

   public boolean isResizing() {

      return this.resizingElementName != null;
   }

   public void onMouseDrag(double doubleVal, double doubleVal2) {

      MinecraftClient mc = MinecraftClient.getInstance();
      boolean flag = mc != null && mc.getWindow() != null && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 0) == 1;
      if (!flag) {
         this.onMouseRelease();
      } else if (this.resizingElementName != null) {
         try {
            HudElementType hudElementTypeValue = HudElementType.valueOf(this.resizingElementName);
            double doubleValThisValue = doubleVal - this.resizeStartMouseX;
            double doubleVal2ThisValue = doubleVal2 - this.resizeStartMouseY;
            double var8Var10Value = doubleValThisValue + doubleVal2ThisValue;
            float maxValue = Math.max(0.5F, Math.min(3.0F, this.resizeStartScale + (float)(var8Var10Value * 0.01F)));
            HudModule.applyHudElementScale(hudElementTypeValue, maxValue);
         } catch (Exception error) {
         }
      } else if (this.draggingElementName != null && this.isDraggingElement) {
         try {
            HudElementType hudElementTypeValue2 = HudElementType.valueOf(this.draggingElementName);
            float floatVal = getElementScale(hudElementTypeValue2);
            if (hudElementTypeValue2 == HudElementType.SPOTIFY_HUD) {
               int intVal = (int)Math.round(doubleVal - this.dragOffsetX);
               int intVal2 = (int)Math.round(doubleVal2 - this.dragOffsetY);
               setElementPosition(hudElementTypeValue2, intVal, intVal2);
            } else {
               int intVal3 = (int)Math.round((doubleVal - this.dragOffsetX) / floatVal);
               int intVal4 = (int)Math.round((doubleVal2 - this.dragOffsetY) / floatVal);
               if (hudElementTypeValue2 == HudElementType.MODULE_LIST) {
                  int[] hudModuleValue = HudModule.call5();
                  int maxValue2 = Math.max(1, hudModuleValue[2]);
                  setElementPosition(hudElementTypeValue2, intVal3 + maxValue2, intVal4);
               } else {
                  setElementPosition(hudElementTypeValue2, intVal3, intVal4);
               }
            }
         } catch (Exception error2) {
         }
      }
   }

   public void onMouseRelease() {
      this.draggingElementName = null;
      this.isDraggingElement = false;
      this.resizingElementName = null;
      isLayoutEditing = false;
   }

   public boolean onScroll(double doubleVal, double doubleVal2, double doubleVal3) {

      if (doubleVal3 == 0.0) {
         return false;
      } else if (!HudModule.call14(HudElementType.SPOTIFY_HUD)) {
         return false;
      } else {
         int[] hudModuleValue = HudModule.call8(HudElementType.SPOTIFY_HUD);
         int spotifyHudModuleValue = SpotifyHudModule.getSpotifyPanelWidth();
         int spotifyHudModuleValue2 = SpotifyHudModule.getSpotifyPanelHeight();
         if (!(doubleVal < hudModuleValue[0]) && !(doubleVal > hudModuleValue[0] + spotifyHudModuleValue) && !(doubleVal2 < hudModuleValue[1]) && !(doubleVal2 > hudModuleValue[1] + spotifyHudModuleValue2)) {
            SpotifyHudModule.setSpotifyPanelScale(SpotifyHudModule.getScale() + (float)(doubleVal3 * 0.08));
            return true;
         } else {
            return false;
         }
      }
   }

   public void renderOutlines(DrawContext arg, int intVal, int intVal2) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null) {
         int clickGuiModuleValue = ClickGuiModule.getAccentColorArgb();
         float clickGuiModuleValue2 = ClickGuiModule.getBaseCornerRadius();

         for (HudElementType hudElementType : HudElementType.values()) {
            if (HudModule.call14(hudElementType) && (hasVisibleContent(hudElementType) || hudElementType == HudElementType.SPOTIFY_HUD)) {
               int[] local = getElementPosition(hudElementType);
               int intVal3 = getElementWidth(hudElementType);
               int intVal4 = getElementHeight(hudElementType);
               if (hudElementType == HudElementType.MODULE_LIST) {
                  TextRenderer local2 = mc.textRenderer;
                  ArrayList arrayListInst = new ArrayList();

                  for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
                     if (moduleBase.isEnabled() && moduleBase.getCategory() != ModuleCategory.CLIENT) {
                        arrayListInst.add(moduleBase);
                     }
                  }

                  if (arrayListInst.isEmpty()) {
                     continue;
                  }

                  arrayListInst.sort(Comparator.<ModuleBase>comparingInt(item -> {
                     return local2.getWidth(item.getName2().toUpperCase());
                  }).reversed().thenComparing(ModuleBase::getName2));
                  int[] hudModuleValue = HudModule.call8(HudElementType.MODULE_LIST);
                  int intVal5 = hudModuleValue[1];

                  for (ModuleBase moduleBase2 : (Iterable<ModuleBase>)arrayListInst) {
                     int var14Value = local2.getWidth(moduleBase2.getName2()) + 12;
                     byte byteVal = 12;
                     int intVal6 = hudModuleValue[0] - var14Value;
                     GuiRenderUtil.strokeRoundedRect(arg, intVal6, intVal5, var14Value, byteVal, 3.0F, 1.0F, clickGuiModuleValue, false);
                     intVal5 += byteVal;
                  }
               } else if (hudElementType == HudElementType.POTION_EFFECTS) {
                  if (mc.player == null) {
                     continue;
                  }

                  TextRenderer local3 = mc.textRenderer;
                  ArrayList arrayListInst2 = new ArrayList(mc.player.getStatusEffects());
                  arrayListInst2.sort(Comparator.comparingInt(item -> {
                     return local3.getWidth(HudModule.formatStatusEffectName((StatusEffectInstance)item));
                  }));
                  int[] hudModuleValue2 = HudModule.call8(HudElementType.POTION_EFFECTS);
                  int intVal7 = hudModuleValue2[1];

                  for (StatusEffectInstance class1293 : (Iterable<StatusEffectInstance>)arrayListInst2) {
                     String hudModuleValue3 = HudModule.formatStatusEffectName(class1293);
                     int var23Value = local3.getWidth(hudModuleValue3) + 14;
                     byte byteVal2 = 14;
                     GuiRenderUtil.strokeRoundedRect(arg, hudModuleValue2[0], intVal7, var23Value, byteVal2, 5.0F, 1.0F, clickGuiModuleValue, false);
                     intVal7 += byteVal2 + 3;
                  }
               } else {
                  GuiRenderUtil.strokeRoundedRect(arg, local[0], local[1], intVal3, intVal4, clickGuiModuleValue2, 1.0F, clickGuiModuleValue, false);
               }

               if (hudElementType != HudElementType.MODULE_LIST && hudElementType != HudElementType.POTION_EFFECTS) {
                  boolean flag = isOverResizeHandle(intVal, intVal2, local, intVal3, intVal4);
                  boolean flag2 = this.resizingElementName != null && this.resizingElementName.equals(hudElementType.name());
                  int intVal8 = flag2 ? clickGuiModuleValue : (flag ? blendColor(clickGuiModuleValue, -1, 0.3F) : clickGuiModuleValue & 16777215 | -1728053248);
                  float floatVal = local[0] + intVal3 - 12;
                  float floatVal2 = local[1] + intVal4 - 12;
                  GuiRenderUtil.fillRoundedRect(arg, floatVal, floatVal2, 12.0F, 12.0F, Math.min(clickGuiModuleValue2, 4.0F), intVal8, false);
                  int local4 = intVal8;
                  arg.fill((int)(floatVal + 2.0F), (int)(floatVal2 + 12.0F - 3.0F), (int)(floatVal + 12.0F - 1.0F), (int)(floatVal2 + 12.0F - 2.0F), local4);
                  arg.fill((int)(floatVal + 2.0F), (int)(floatVal2 + 12.0F - 5.0F), (int)(floatVal + 12.0F - 3.0F), (int)(floatVal2 + 12.0F - 4.0F), local4);
               }
            }
         }
      }
   }

   public static boolean isOverResizeHandle(double doubleVal, double doubleVal2, int[] int2, int intVal, int intVal2) {
      return doubleVal >= int2[0] + intVal - 12 && doubleVal <= int2[0] + intVal && doubleVal2 >= int2[1] + intVal2 - 12 && doubleVal2 <= int2[1] + intVal2;
   }

   public static float getRawElementScale(HudElementType hudElementType) {
      return HudModule.call9(hudElementType);
   }

   public static int blendColor(int intVal, int intVal2, float floatVal) {

      int intVal3 = intVal >> 16 & 0xFF;
      int intVal4 = intVal >> 8 & 0xFF;
      int intVal5 = intVal & 0xFF;
      int intVal6 = intVal >> 24 & 0xFF;
      int intVal7 = intVal2 >> 16 & 0xFF;
      int intVal8 = intVal2 >> 8 & 0xFF;
      int intVal9 = intVal2 & 0xFF;
      int intVal10 = intVal2 >> 24 & 0xFF;
      return (int)(intVal6 + (intVal10 - intVal6) * floatVal) << 24
         | (int)(intVal3 + (intVal7 - intVal3) * floatVal) << 16
         | (int)(intVal4 + (intVal8 - intVal4) * floatVal) << 8
         | (int)(intVal5 + (intVal9 - intVal5) * floatVal);
   }

   public static boolean hasVisibleContent(HudElementType hudElementType) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null) {
         return false;
      }

      return switch (hudElementType) {
         case ARMOR -> {
            if (mc.player == null) {
               yield false;
            } else {
               EquipmentSlot[] local = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

               for (EquipmentSlot class1304 : local) {
                  ItemStack local2 = mc.player.getEquippedStack(class1304);
                  if (local2 != null && !local2.isEmpty()) {
                     yield true;
                  }
               }

               yield false;
            }
         }
         case POTION_EFFECTS -> mc.player == null ? false : !mc.player.getStatusEffects().isEmpty();
         case MODULE_LIST -> {
            for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
               if (moduleBase.isEnabled() && moduleBase.getCategory() != ModuleCategory.CLIENT) {
                  yield true;
               }
            }

            yield false;
         }
         case KEYBINDS -> {
            for (ModuleBase moduleBase2 : ConfigManager.INSTANCE.getModules()) {
               if (moduleBase2.getKeyCode() != 0) {
                  yield true;
               }
            }

            yield false;
         }
         default -> true;
      };
   }

   public static int[] getElementPosition(HudElementType hudElementType) {
      float floatVal = getElementScale(hudElementType);
      if (hudElementType == HudElementType.MODULE_LIST) {
         int[] hudModuleValue = HudModule.call5();
         return new int[]{Math.round(hudModuleValue[0] * floatVal), Math.round(hudModuleValue[1] * floatVal)};
      } else {
         int[] hudModuleValue2 = HudModule.call8(hudElementType);
         return new int[]{Math.round(hudModuleValue2[0] * floatVal), Math.round(hudModuleValue2[1] * floatVal)};
      }
   }

   public static int getElementWidth(HudElementType hudElementType) {
      float floatVal = getElementScale(hudElementType);
      if (hudElementType == HudElementType.MODULE_LIST) {
         return Math.round(HudModule.call5()[2] * floatVal);
      } else {
         return hudElementType == HudElementType.SPOTIFY_HUD ? SpotifyHudModule.getSpotifyPanelWidth() : Math.round(HudModule.call8(hudElementType)[2] * floatVal);
      }
   }

   public static int getElementHeight(HudElementType hudElementType) {
      float floatVal = getElementScale(hudElementType);
      if (hudElementType == HudElementType.MODULE_LIST) {
         return Math.round(HudModule.call5()[3] * floatVal);
      } else {
         return hudElementType == HudElementType.SPOTIFY_HUD ? SpotifyHudModule.getSpotifyPanelHeight() : Math.round(HudModule.call8(hudElementType)[3] * floatVal);
      }
   }

   public static String getVaultKey2() {

      return "A";
   }

}
