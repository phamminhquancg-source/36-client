package com.threesix.mixin;

import net.fabricmc.api.ClientModInitializer;
import com.threesix.internal.ClickGuiKeyBinding;

public class ClientLifecycleHandler implements ClientModInitializer {
   public static boolean isMenuKey(int intVal, int intVal2) {
      return true;
   }

   public static void toggleClickGui() {
      ClickGuiKeyBinding.toggleClickGui();
   }

   public void onInitializeClient() {
      ClickGuiKeyBinding.register();
   }
}
