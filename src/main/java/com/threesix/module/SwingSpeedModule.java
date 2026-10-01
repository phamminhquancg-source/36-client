package com.threesix.module;

import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class SwingSpeedModule extends ModuleBase {
   public static SwingSpeedModule instance;
   public final ClientSetting swingSpeedSetting = new ClientSetting("Swing Speed", 1.0F, 0.1F, 10.0F);

   public SwingSpeedModule() {
      super("SwingSpeed", ModuleCategory.MISC);
      instance = this;
      this.registerSetting(this.swingSpeedSetting);
   }

   public float getSwingSpeedDivisor() {
      float floatVal = this.swingSpeedSetting.getValue() == null ? 1.0F : (Float)this.swingSpeedSetting.getValue();
      return floatVal < 0.1F ? 0.1F : Math.min(floatVal, 10.0F);
   }

}
