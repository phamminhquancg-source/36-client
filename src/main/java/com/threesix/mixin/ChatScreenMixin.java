package com.threesix.mixin;

import net.minecraft.client.gui.Click;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.SpotifyHudModule;
import com.threesix.internal.ThreesixClientInit;
import com.threesix.manager.HudLayoutManager;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void threesix$mouseClicked(Click arg, boolean flag, CallbackInfoReturnable callbackInfoReturnable) {
      double argValue = arg.x();
      double argValue2 = arg.y();
      int var1Value = arg.button();
      if (var1Value == 0) {
         try {
            if (SpotifyHudModule.handleMouseClick(argValue, argValue2)) {
               callbackInfoReturnable.setReturnValue(true);
               return;
            }
         } catch (Throwable error) {
         }

         if (HudLayoutManager.INSTANCE5.onMouseButton(argValue, argValue2, var1Value)) {
            HudLayoutManager.isLayoutEditing = true;
            callbackInfoReturnable.setReturnValue(true);
         }
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void threesix$render(DrawContext arg, int intVal, int intVal2, float floatVal, CallbackInfo callbackInfo) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null) {
         boolean gLFWValue = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 0) == 1;
         if (!gLFWValue) {
            HudLayoutManager.INSTANCE5.onMouseRelease();
            HudLayoutManager.isLayoutEditing = false;
         }
      }

      ThreesixClientInit.renderHudStack(arg, 0.0F);
      HudLayoutManager.INSTANCE5.renderOutlines(arg, intVal, intVal2);
   }
}
