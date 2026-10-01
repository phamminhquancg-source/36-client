package com.threesix.mixin;

import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.FreecamModule;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
   @Inject(method = "tick()V", at = @At("RETURN"))
   private void onTickReturn(CallbackInfo callbackInfo) {
      if (FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled()) {
         InputAccessor local = (InputAccessor)(Object)this;
         local.threesix$setPlayerInput(new PlayerInput(false, false, false, false, false, false, false));
         local.threesix$setMovementVector(Vec2f.ZERO);
      }
   }
}
