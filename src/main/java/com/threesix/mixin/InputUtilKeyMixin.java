package com.threesix.mixin;

import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.util.InputUtil.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Key.class)
public class InputUtilKeyMixin {
   @Inject(method = "getLocalizedText", at = @At("HEAD"), cancellable = true)
   private void threesix$guardUnknownKey(CallbackInfoReturnable callbackInfoReturnable) {
      Key local = (Key)(Object)this;
      if (local.getCategory() == Type.KEYSYM && local.getCode() == 0) {
         callbackInfoReturnable.setReturnValue(Text.translatable("key.keyboard.unknown"));
      }
   }
}
