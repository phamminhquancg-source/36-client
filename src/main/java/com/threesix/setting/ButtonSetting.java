package com.threesix.setting;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class ButtonSetting extends ClientSetting {
   public final Runnable action;

   public ButtonSetting(String string, String string2, Runnable runnable) {
      super(string, string2);
      this.action = runnable;
   }

   public void press() {
      if (this.action != null) {
         this.action.run();
      }
   }

}
