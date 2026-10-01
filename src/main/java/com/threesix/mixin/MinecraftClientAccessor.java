package com.threesix.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
   @Accessor("itemUseCooldown")
   int threesix$getItemUseCooldown();

   @Accessor("itemUseCooldown")
   void threesix$setItemUseCooldown(int arg1);
}
