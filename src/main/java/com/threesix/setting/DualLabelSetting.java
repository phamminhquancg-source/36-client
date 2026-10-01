package com.threesix.setting;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class DualLabelSetting extends ClientSetting {
   public final String leftLabel;
   public final String rightLabel;

   public DualLabelSetting(String string, boolean flag, String string2, String string3) {
      super(string, flag);
      this.leftLabel = string2;
      this.rightLabel = string3;
   }

   public String getLeftLabel() {
      return this.leftLabel;
   }

   public String getRightLabel() {
      return this.rightLabel;
   }

}
