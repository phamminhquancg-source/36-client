package com.threesix.module;

import java.awt.Color;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemDisplayContext;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class OutlineHandModule extends ModuleBase {
   public static OutlineHandModule instance;
   public final ClientSetting mainColorSetting = new ClientSetting("Main Color", new Color(85, 255, 255, 255));
   public final ClientSetting offColorSetting = new ClientSetting("Off Color", new Color(255, 85, 255, 255));

   public OutlineHandModule() {
      super("Outline Hand", ModuleCategory.RENDER);
      this.registerSetting(this.mainColorSetting);
      this.registerSetting(this.offColorSetting);
      instance = this;
   }

   public static boolean isModuleEnabled() {
      int local = instance != null && instance.isEnabled() ? 1 : 0;
      return local != 0;
   }

   public static boolean hasArmPose(ItemStack arg) {
      try {

         return arg != null && !arg.isEmpty();
      } catch (Throwable error) {
         return false;
      }
   }

   public static boolean isOutlineVisible(ItemStack arg) {
      return false;
   }

   public static int toArgb(Color color) {
      return color == null ? 0 : color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   public static int getHandOutlineColor(LivingEntity arg, ItemStack arg2, ItemDisplayContext arg3) {
      try {
         if (!isModuleEnabled()) {
            return 0;
         } else if (arg != null && hasArmPose(arg2)) {
            Color local = arg3 == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? (Color)instance.offColorSetting.getValue() : (Color)instance.mainColorSetting.getValue();
            int intVal = toArgb(local);
            return (intVal & 0xFF000000) == 0 ? 0 : intVal;
         } else {
            return 0;
         }
      } catch (Throwable error) {
         return 0;
      }
   }

}
