package com.threesix.module;

import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class HitboxExpandModule extends ModuleBase {
   public static HitboxExpandModule instance;
   public final ClientSetting expandSetting = new ClientSetting("Expand", 1.0F, 0.5F, 2.0F);

   public HitboxExpandModule() {
      super("Hitbox", ModuleCategory.COMBAT);
      this.registerSetting(this.expandSetting);
      instance = this;
   }

}
