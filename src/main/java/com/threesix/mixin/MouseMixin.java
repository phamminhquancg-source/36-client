package com.threesix.mixin;

import net.minecraft.client.input.MouseInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.FreecamModule;
import com.threesix.module.SpotifyHudModule;
import com.threesix.manager.HudLayoutManager;

@Mixin(Mouse.class)
public class MouseMixin {
   private static double toScaledX(MinecraftClient arg, double doubleVal) {
      if (arg != null && arg.getWindow() != null) {
         double argValue = arg.getWindow().getWidth();
         return argValue <= 0.0 ? doubleVal : doubleVal * (arg.getWindow().getScaledWidth() / argValue);
      } else {
         return doubleVal;
      }
   }

   private static double toScaledY(MinecraftClient arg, double doubleVal) {
      if (arg != null && arg.getWindow() != null) {
         double argValue = arg.getWindow().getHeight();
         return argValue <= 0.0 ? doubleVal : doubleVal * (arg.getWindow().getScaledHeight() / argValue);
      } else {
         return doubleVal;
      }
   }

   @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true, require = 0)
   private void threesix$hudEditorMouseButtonClick(long longVal, MouseInput arg, int intVal, CallbackInfo callbackInfo) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.currentScreen instanceof ChatScreen) {
         double doubleVal = toScaledX(mc, mc.mouse.getX());
         double doubleVal2 = toScaledY(mc, mc.mouse.getY());
         int intVal2 = arg.button();
         if (intVal == 1) {
            try {
               if (SpotifyHudModule.handleMouseClick(doubleVal, doubleVal2)) {
                  callbackInfo.cancel();
                  return;
               }
            } catch (Throwable error) {
            }

            if (HudLayoutManager.INSTANCE5.onMouseButton(doubleVal, doubleVal2, intVal2)) {
               callbackInfo.cancel();
            }
         } else if (intVal == 0) {
            HudLayoutManager.INSTANCE5.onMouseRelease();
         }
      }
   }

   @Inject(method = "onCursorPos", at = @At("HEAD"), require = 0)
   private void threesix$hudEditorCursorPos(long longVal, double doubleVal, double doubleVal2, CallbackInfo callbackInfo) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.player != null && mc.currentScreen instanceof ChatScreen) {
         boolean hudLayoutManagerValue = HudLayoutManager.INSTANCE5.isInteractionActive();
         if (hudLayoutManagerValue) {
            if (GLFW.glfwGetMouseButton(longVal, 0) != 1) {
               HudLayoutManager.INSTANCE5.onMouseRelease();
            } else {
               HudLayoutManager.INSTANCE5.onMouseDrag(toScaledX(mc, doubleVal), toScaledY(mc, doubleVal2));
            }
         }
      }
   }

   @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
   private void threesix$useScrollForFreecamSpeed(long longVal, double doubleVal, double doubleVal2, CallbackInfo callbackInfo) {
      if (FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled() && MinecraftClient.getInstance().currentScreen == null) {
         double doubleVal3 = doubleVal2 != 0.0 ? doubleVal2 : doubleVal;
         if (doubleVal3 != 0.0) {
            FreecamModule.freecamInstance.setSpeedFromScroll(doubleVal3);
            callbackInfo.cancel();
         }
      }
   }

   @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
   private void threesix$hudEditorMouseScroll(long longVal, double doubleVal, double doubleVal2, CallbackInfo callbackInfo) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.currentScreen instanceof ChatScreen) {
         double doubleVal3 = doubleVal2 != 0.0 ? doubleVal2 : doubleVal;
         if (doubleVal3 != 0.0) {
            double doubleVal4 = toScaledX(mc, mc.mouse.getX());
            double doubleVal5 = toScaledY(mc, mc.mouse.getY());
            if (HudLayoutManager.INSTANCE5.onScroll(doubleVal4, doubleVal5, doubleVal3)) {
               callbackInfo.cancel();
            }
         }
      }
   }
}
