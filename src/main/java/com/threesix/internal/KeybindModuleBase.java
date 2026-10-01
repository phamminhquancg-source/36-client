package com.threesix.internal;

import com.threesix.manager.ConfigManager;
import com.threesix.data.ModuleCategory;

public abstract class KeybindModuleBase extends ModuleBase {
   public int bindKeyCode = 0;
   public boolean bindKeyPressed = false;

   public KeybindModuleBase(String string, ModuleCategory moduleCategory) {
      super(string, moduleCategory);
   }

   public void onActivationKey() {
      this.toggle();
   }

   public int getBindKeyCode() {
      return this.bindKeyCode;
   }

   public void setBindKeyCode(int intVal) {
      this.bindKeyCode = intVal >= 0 && intVal <= 348 ? intVal : 0;
      ConfigManager.INSTANCE.save();
   }

   public void setBindKeyCodeFromConfig(int intVal) {
      this.bindKeyCode = intVal >= 0 && intVal <= 348 ? intVal : 0;
   }

   public static String getDefaultBindKey() {
      return "C";
   }
}
