package com.threesix.mixin.litematica;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.data.ClientStateFlags;

@Mixin(targets = "fi/dy/masa/litematica/util/WorldUtils", remap = false)
public class WorldUtilsMixin {
   @Inject(method = "placementRestrictionInEffect", at = @At("HEAD"), cancellable = true, remap = false)
   private static void onPlacementRestrictionInEffect(MinecraftClient arg, CallbackInfoReturnable callbackInfoReturnable) {
      if (ClientStateFlags.isLitematicaCompatActive) {
         callbackInfoReturnable.setReturnValue(Boolean.FALSE);
      }
   }
}
