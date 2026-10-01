package com.threesix.mixin.litematica;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.data.ClientStateFlags;

@Mixin(targets = "fi/dy/masa/litematica/util/EasyPlaceUtils", remap = false)
public class EasyPlaceUtilsMixin {
   @Inject(method = "placementRestrictionInEffect", at = @At("HEAD"), cancellable = true, remap = false)
   private static void onPlacementRestrictionInEffect(CallbackInfoReturnable callbackInfoReturnable) {
      if (ClientStateFlags.isLitematicaCompatActive) {
         callbackInfoReturnable.setReturnValue(Boolean.FALSE);
      }
   }
}
