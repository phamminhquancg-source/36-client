package com.threesix.module;

import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;

public final class AutoMineModule extends ModuleBase {
   public AutoMineModule() {
      super("AutoMine", ModuleCategory.MISC);
   }

   @Override
   public void onDisable() {
      if (minecraftClient.options != null) {
         minecraftClient.options.attackKey.setPressed(false);
      }
   }
}
