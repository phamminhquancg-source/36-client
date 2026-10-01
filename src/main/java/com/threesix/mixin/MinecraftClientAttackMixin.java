package com.threesix.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.SpearSwapModule;

@Mixin(MinecraftClient.class)
public class MinecraftClientAttackMixin {
   @Inject(method = "handleInputEvents", at = @At("HEAD"), require = 0)
   private void threesix$preInput(CallbackInfo callbackInfo) {
      SpearSwapModule spearSwapModuleValue = SpearSwapModule.instance;
      if (spearSwapModuleValue != null && spearSwapModuleValue.isEnabled()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.player != null && mc.options != null && mc.currentScreen == null) {
            if (mc.options.attackKey.isPressed()) {
               spearSwapModuleValue.trySwapToSpear();
            } else {
               spearSwapModuleValue.endSwap();
            }
         }
      }
   }
}
