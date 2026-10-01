package com.threesix.module;

import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;

public final class SprintModule extends ModuleBase {
   public boolean wasSprintToggled = false;

   public SprintModule() {
      super("Sprint", ModuleCategory.MISC);
   }

   @Override
   public void onEnable() {
      if (minecraftClient != null && minecraftClient.options != null) {
         this.wasSprintToggled = this.isSprintToggled();
         this.setSprintToggled(false);
      }
   }

   @Override
   public void onDisable() {
      if (minecraftClient != null && minecraftClient.options != null) {
         this.setSprintToggled(this.wasSprintToggled);

         try {
            minecraftClient.options.sprintKey.setPressed(false);
         } catch (Throwable error) {
         }
      }
   }

   @Override
   public void onTick() {
      if (minecraftClient != null && minecraftClient.player != null && minecraftClient.options != null) {
         this.setSprintToggled(false);

         try {
            minecraftClient.options.sprintKey.setPressed(true);
         } catch (Throwable error) {
         }
      }
   }

   public boolean isSprintToggled() {
      try {
         Object minecraftClientValue = minecraftClient.options.getClass().getMethod("getSprintToggled").invoke(minecraftClient.options);
         return minecraftClientValue == null ? false : minecraftClientValue.getClass().getMethod("getValue").invoke(minecraftClientValue) instanceof Boolean local && local;
      } catch (Throwable error) {
         return false;
      }
   }

   public void setSprintToggled(boolean flag) {
      try {
         Object minecraftClientValue = minecraftClient.options.getClass().getMethod("getSprintToggled").invoke(minecraftClient.options);
         if (minecraftClientValue == null) {
            return;
         }

         minecraftClientValue.getClass().getMethod("setValue", Object.class).invoke(minecraftClientValue, flag);
      } catch (Throwable error) {
      }
   }

}
