package com.threesix.module;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.glfw.GLFW;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class ChatMacroModule extends ModuleBase {
   public final ClientSetting macro1TextSetting = new ClientSetting("Macro 1 Text", "");
   public final ClientSetting macro1KeySetting = new ClientSetting("Macro 1 Key", 0, 0, 348);
   public final ClientSetting macro2TextSetting = new ClientSetting("Macro 2 Text", "");
   public final ClientSetting macro2KeySetting = new ClientSetting("Macro 2 Key", 0, 0, 348);
   public final ClientSetting macro3TextSetting = new ClientSetting("Macro 3 Text", "");
   public final ClientSetting macro3KeySetting = new ClientSetting("Macro 3 Key", 0, 0, 348);
   public final ClientSetting macro4TextSetting = new ClientSetting("Macro 4 Text", "");
   public final ClientSetting macro4KeySetting = new ClientSetting("Macro 4 Key", 0, 0, 348);
   public final ClientSetting macro5TextSetting = new ClientSetting("Macro 5 Text", "");
   public final ClientSetting macro5KeySetting = new ClientSetting("Macro 5 Key", 0, 0, 348);
   public final Map<Integer, Boolean> keyPressedMap = new HashMap<>();

   public ChatMacroModule() {
      super("Chat Macro", ModuleCategory.MISC);
      this.registerSetting(this.macro1TextSetting);
      this.registerSetting(this.macro1KeySetting);
      this.registerSetting(this.macro2TextSetting);
      this.registerSetting(this.macro2KeySetting);
      this.registerSetting(this.macro3TextSetting);
      this.registerSetting(this.macro3KeySetting);
      this.registerSetting(this.macro4TextSetting);
      this.registerSetting(this.macro4KeySetting);
      this.registerSetting(this.macro5TextSetting);
      this.registerSetting(this.macro5KeySetting);
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.currentScreen == null) {
         this.checkMacroKey((Integer)this.macro1KeySetting.getValue(), (String)this.macro1TextSetting.getValue());
         this.checkMacroKey((Integer)this.macro2KeySetting.getValue(), (String)this.macro2TextSetting.getValue());
         this.checkMacroKey((Integer)this.macro3KeySetting.getValue(), (String)this.macro3TextSetting.getValue());
         this.checkMacroKey((Integer)this.macro4KeySetting.getValue(), (String)this.macro4TextSetting.getValue());
         this.checkMacroKey((Integer)this.macro5KeySetting.getValue(), (String)this.macro5TextSetting.getValue());
      }
   }

   public void checkMacroKey(int intVal, String string) {
      if (intVal > 0 && string != null && !string.isBlank()) {
         boolean flag = this.isKeyDown(intVal);
         boolean flag2 = this.keyPressedMap.getOrDefault(intVal, false);
         if (flag && !flag2) {
            this.sendMacroText(string.trim());
         }

         this.keyPressedMap.put(intVal, flag);
      }
   }

   public boolean isKeyDown(int intVal) {
      if (minecraftClient.getWindow() == null) {
         return false;
      }

      if (intVal <= 0 || intVal > 348) {
         return false;
      }

      try {
         return GLFW.glfwGetKey(minecraftClient.getWindow().getHandle(), intVal) == 1;
      } catch (Exception error) {
         return false;
      }
   }

   public void sendMacroText(String string) {
      minecraftClient.execute(() -> {

         if (minecraftClient.player != null && minecraftClient.getNetworkHandler() != null) {
            if (string.startsWith("/")) {
               minecraftClient.getNetworkHandler().sendChatCommand(string.substring(1));
            } else {
               minecraftClient.getNetworkHandler().sendChatMessage(string);
            }
         }
      });
   }

}
