package com.threesix.mixin;

import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.BoatFlyModule;

@Mixin(AbstractBoatEntity.class)
public class BoatNoclipMixin {
   @Shadow
   private boolean pressingLeft;
   @Shadow
   private boolean pressingRight;

   @Inject(method = "collidesWith", at = @At("HEAD"), cancellable = true, require = 0)
   private void threesix$boatNoCollide(Entity arg, CallbackInfoReturnable callbackInfoReturnable) {
      try {
         AbstractBoatEntity local = (AbstractBoatEntity)(Object)this;
         MinecraftClient mc = MinecraftClient.getInstance();
         if (BoatFlyModule.isBoatNoclipActive() && mc != null && local.getControllingPassenger() == mc.player) {
            callbackInfoReturnable.setReturnValue(false);
         }
      } catch (Throwable error) {
      }
   }

   @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/AbstractBoatEntity;pressingLeft:Z", opcode = 180), require = 0)
   private boolean threesix$noPaddleLeft(AbstractBoatEntity arg) {
      try {
         if (BoatFlyModule.isBoatNoclipActive()) {
            return false;
         }
      } catch (Throwable error) {
      }

      return ((BoatNoclipMixin)(Object)arg).pressingLeft;
   }

   @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/AbstractBoatEntity;pressingRight:Z", opcode = 180), require = 0)
   private boolean threesix$noPaddleRight(AbstractBoatEntity arg) {
      try {
         if (BoatFlyModule.isBoatNoclipActive()) {
            return false;
         }
      } catch (Throwable error) {
      }

      return ((BoatNoclipMixin)(Object)arg).pressingRight;
   }
}
