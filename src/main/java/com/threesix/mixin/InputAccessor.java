package com.threesix.mixin;

import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Input.class)
public interface InputAccessor {
   @Accessor("playerInput")
   void threesix$setPlayerInput(PlayerInput arg1);

   @Accessor("movementVector")
   void threesix$setMovementVector(Vec2f arg1);
}
