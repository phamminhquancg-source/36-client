package com.threesix.mixin;

import net.minecraft.client.input.KeyInput;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.ClickGuiModule;
import com.threesix.internal.ThreesixClientInit;

@Mixin(Keyboard.class)
public class ThreesixMenuKeyboardMixin {
   @Unique
   private boolean threesix$menuKeyDown = false;

   @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
   private void threesix$openClickGuiFromAnyScreen(long longVal, int intVal, KeyInput arg, CallbackInfo callbackInfo) {
      int clickGuiModuleValue = ClickGuiModule.getMenuKeyCode();
      boolean gLFWValue = GLFW.glfwGetKey(longVal, clickGuiModuleValue) == 1;
      if (gLFWValue && !this.threesix$menuKeyDown) {
         ThreesixClientInit.toggleClickGui();
         callbackInfo.cancel();
      }

      this.threesix$menuKeyDown = gLFWValue;
   }
}
