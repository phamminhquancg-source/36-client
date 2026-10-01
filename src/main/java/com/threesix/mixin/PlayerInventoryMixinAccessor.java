package com.threesix.mixin;

import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInventory.class)
public interface PlayerInventoryMixinAccessor {
   @Accessor("selectedSlot")
   void setSelectedSlot(int arg1);

   @Accessor("selectedSlot")
   int getSelectedSlot();
}
