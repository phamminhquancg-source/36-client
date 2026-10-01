package com.threesix.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.HudModule;

@Mixin(InGameHud.class)
public class InGameHudMixin {
   @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true, require = 0)
   private void threesix$hideVanillaEffects(DrawContext arg, RenderTickCounter arg2, CallbackInfo callbackInfo) {
      try {
         if (HudModule.isHudActive() && HudModule.instance != null && (Boolean)HudModule.instance.potionEffectsSetting.getValue()) {
            callbackInfo.cancel();
         }
      } catch (Throwable error) {
      }
   }
}
