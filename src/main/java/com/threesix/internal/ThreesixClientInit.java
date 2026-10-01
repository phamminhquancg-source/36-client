package com.threesix.internal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.util.Identifier;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil.Type;
import org.lwjgl.glfw.GLFW;
import com.threesix.module.RegionMapModule;
import com.threesix.module.SpotifyHudModule;
import com.threesix.service.AuthBootstrapService;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.module.HudModule;
import com.threesix.module.SpawnerNotifierModule;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.module.NameTagsModule;
import com.threesix.service.RemoteUpdateService;
import com.threesix.module.PearlEspModule;
import com.threesix.util.StringVaultDecoder;

public class ThreesixClientInit implements PreLaunchEntrypoint, ModInitializer, ClientModInitializer {
   private static KeyBinding rightShiftKey;
   private static volatile long AUTH_SESSION;

   public static boolean checkAuth() {
      return true;
   }

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

   public static void renderHudStack(DrawContext arg, float floatVal) {
      try {
         NameTagsModule.call1(arg, floatVal);
      } catch (Throwable error) {
      }

      try {
         PearlEspModule.call1(arg, floatVal);
      } catch (Throwable error2) {
      }

      try {
         SpawnerNotifierModule.call3(arg, floatVal);
      } catch (Throwable error3) {
      }

      try {
         HudModule.call17(arg);
      } catch (Throwable error4) {
      }

      try {
         if (RegionMapModule.field1 != null && RegionMapModule.field1.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null
               && mc.player != null
               && mc.world != null
               && !mc.getDebugHud().shouldShowDebugHud()
               && !(mc.currentScreen instanceof ClickGuiScreen)) {
               RegionMapModule.field1.call1(arg, mc);
            }
         }
      } catch (Throwable error5) {
      }

      try {
         SpotifyHudModule.renderSpotifyHud(arg);
      } catch (Throwable error6) {
      }
   }

   public void onPreLaunch() {
      RemoteUpdateService.bootstrap();
      AUTH_SESSION = RemoteUpdateService.getSessionId();
      if (!checkAuth()) {
         Runtime.getRuntime().halt(7);
      }
   }

   public void onInitialize() {
      if (!checkAuth()) {
         Runtime.getRuntime().halt(7);
      }
   }

   public void onInitializeClient() {
      ConfigManager.INSTANCE.init();
      rightShiftKey = KeyBindingHelper.registerKeyBinding(
         new KeyBinding("key.threesix.toggle_menu", Type.KEYSYM, 344, new Category(Identifier.of("threesix", "general")))
      );
      HudRenderCallback.EVENT.register((HudRenderCallback)(item, local2) -> {
         renderHudStack(item, local2.getTickProgress(false));
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)item -> {
         ConfigManager.INSTANCE.tick();
         if (item.currentScreen == null && item.getWindow() != null) {
            for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
               int intVal = moduleBase.getKeyCode();
               KeybindModuleBase local = moduleBase instanceof KeybindModuleBase local3 ? local3 : null;
               int intVal2 = local != null ? local.getBindKeyCode() : 0;
               if (intVal >= 32 && intVal <= 348) {
                  try {
                     boolean gLFWValue = GLFW.glfwGetKey(item.getWindow().getHandle(), intVal) == 1;
                     if (gLFWValue && !moduleBase.keyPressed && intVal2 != intVal) {
                        moduleBase.toggleByKey();
                     }

                     moduleBase.keyPressed = gLFWValue;
                  } catch (Exception error) {
                  }
               }

               if (local != null && intVal2 >= 32 && intVal2 <= 348) {
                  try {
                     boolean gLFWValue2 = GLFW.glfwGetKey(item.getWindow().getHandle(), intVal2) == 1;
                     if (gLFWValue2 && !local.bindKeyPressed) {
                        local.onActivationKey();
                     }

                     local.bindKeyPressed = gLFWValue2;
                  } catch (Exception error2) {
                  }
               }
            }
         }
      });
   }

   static {
      try {
         Class<AuthBootstrapService> authBootstrapServiceValue = AuthBootstrapService.class;
      } catch (Exception error) {
      }

      AUTH_SESSION = 0L;
   }

}
