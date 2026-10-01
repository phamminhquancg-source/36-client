package com.threesix.module;

import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public class NameProtectModule extends ModuleBase {
   public final ClientSetting fakeNameSetting = new ClientSetting("FakeName", "Player");
   public static NameProtectModule nameProtectModule;

   public NameProtectModule() {
      super("NameProtect", ModuleCategory.MISC);
      nameProtectModule = this;
      this.registerSetting(this.fakeNameSetting);
   }

   public String getMaskedName() {
      return (String)this.fakeNameSetting.getValue();
   }

}
