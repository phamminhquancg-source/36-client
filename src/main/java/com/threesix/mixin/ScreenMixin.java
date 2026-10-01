package com.threesix.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.render.ScreenBlurPipeline;

@Mixin(Screen.class)
public class ScreenMixin {
   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), require = 0)
   private void threesix$blurBehindScreen(DrawContext arg, int intVal, int intVal2, float floatVal, CallbackInfo callbackInfo) {
      try {
         Screen local = (Screen)(Object)this;
         if (local instanceof ClickGuiScreen) {
            return;
         }

         ScreenBlurPipeline.call1(arg, local);
      } catch (Throwable error) {
      }
   }
}
