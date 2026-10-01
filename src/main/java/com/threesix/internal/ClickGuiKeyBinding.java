package com.threesix.internal;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.util.Identifier;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil.Type;
import org.lwjgl.glfw.GLFW;
import com.threesix.module.SpotifyHudModule;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.module.HudModule;
import com.threesix.module.SpawnerNotifierModule;
import com.threesix.util.StringVaultDecoder;
import com.threesix.manager.NotificationHudManager;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.module.NameTagsModule;

public final class ClickGuiKeyBinding {
   public static KeyBinding toggleMenuKey;

   public static boolean isMenuKey(int intVal, int intVal2) {
      return true;
   }

   public static void toggleClickGui() {

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null) {
         mc.execute(() -> {

            if (mc.currentScreen instanceof ClickGuiScreen) {
               mc.setScreen(null);
            } else {
               ClickGuiScreen.d();
            }
         });
      }
   }

   public static void register() {
      ConfigManager.INSTANCE.init();
      toggleMenuKey = KeyBindingHelper.registerKeyBinding(
         new KeyBinding("key.threesix.toggle_menu", Type.KEYSYM, 344, new Category(Identifier.of("threesix", "general")))
      );
      HudRenderCallback.EVENT.register((HudRenderCallback)(item, local) -> {
         NameTagsModule.call1(item, local.getTickProgress(false));
         HudModule.call17(item);
         SpotifyHudModule.renderSpotifyHud(item);
         SpawnerNotifierModule.call3(item, local.getTickProgress(false));
         NotificationHudManager.INSTANCE6.renderNotifications(item);
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)item -> {
         ConfigManager.INSTANCE.tick();
         if (item.currentScreen == null && item.getWindow() != null) {
            for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
               int local3 = moduleBase.getKeyCode();
               KeybindModuleBase local2 = moduleBase instanceof KeybindModuleBase keybindModuleBase ? keybindModuleBase : null;
               int intVal = local2 != null ? local2.getBindKeyCode() : 0;
               if (local3 > 0 && local3 < 349) {
                  try {
                     boolean gLFWValue = GLFW.glfwGetKey(item.getWindow().getHandle(), local3) == 1;
                     if (gLFWValue && !moduleBase.keyPressed && intVal != local3) {
                        moduleBase.toggleByKey();
                     }

                     moduleBase.keyPressed = gLFWValue;
                  } catch (Exception error) {
                  }
               }

               if (local2 != null && intVal > 0 && intVal < 349) {
                  try {
                     boolean gLFWValue2 = GLFW.glfwGetKey(item.getWindow().getHandle(), intVal) == 1;
                     if (gLFWValue2 && !local2.bindKeyPressed) {
                        local2.onActivationKey();
                     }

                     local2.bindKeyPressed = gLFWValue2;
                  } catch (Exception error2) {
                  }
               }
            }
         }
      });
   }

}
