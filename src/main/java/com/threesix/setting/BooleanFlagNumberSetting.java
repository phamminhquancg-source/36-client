package com.threesix.setting;

import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class BooleanFlagNumberSetting extends ClientSetting {
   public boolean flagValue;

   public BooleanFlagNumberSetting(String string, boolean flag, int intVal, int intVal2, int intVal3) {
      super(string, intVal, intVal2, intVal3);
      this.flagValue = flag;
   }

   public boolean getFlag() {
      return this.flagValue;
   }

   public void setFlag(boolean flag) {
      if (this.flagValue != flag) {
         this.flagValue = flag;
         ConfigManager.INSTANCE.notifyLayoutChanged();
      }
   }

   public String serialize() {
      return this.flagValue + "|" + this.getValue();
   }

   public void deserialize(String string) {
      if (string != null && !string.isBlank()) {
         String[] local = string.split("\\|", 2);

         try {
            if (local.length == 2) {
               this.flagValue = Boolean.parseBoolean(local[0]);
               this.setValue(Integer.parseInt(local[1]));
               return;
            }

            this.setValue(Integer.parseInt(string));
         } catch (NumberFormatException numberFormatException) {
         }
      }
   }

}
