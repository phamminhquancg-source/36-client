package com.threesix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.FreecamModule;
import com.threesix.module.FreeLookModule;

@Mixin(Camera.class)
public abstract class CameraMixin {
   @Shadow
   protected abstract void setPos(double arg1, double arg2, double arg3);

   @Shadow
   protected abstract void setRotation(float arg1, float arg4);

   @Shadow
   protected abstract float clipToSpace(float arg1);

   @Shadow
   protected abstract void moveBy(float arg1, float arg4, float arg2);

   @Inject(method = "update", at = @At("TAIL"))
   private void onUpdate(World arg, Entity arg2, boolean flag, boolean flag2, float floatVal, CallbackInfo callbackInfo) {
      if (FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled()) {
         double freecamModuleValue3 = FreecamModule.freecamInstance.getCameraX(floatVal);
         double freecamModuleValue4 = FreecamModule.freecamInstance.getCameraY(floatVal);
         double freecamModuleValue5 = FreecamModule.freecamInstance.getCameraZ(floatVal);
         float freecamModuleValue = FreecamModule.freecamInstance.getCameraPitch(floatVal);
         float freecamModuleValue2 = FreecamModule.freecamInstance.getCameraYaw(floatVal);
         this.setPos(freecamModuleValue3, freecamModuleValue4, freecamModuleValue5);
         this.setRotation(freecamModuleValue, freecamModuleValue2);
      } else {
         FreeLookModule freeLookModuleValue = FreeLookModule.freeLookInstance;
         if (freeLookModuleValue != null && freeLookModuleValue.isThirdPersonActive() && arg2 != null) {
            Vec3d var2Value = arg2.getCameraPosVec(floatVal);
            this.setRotation(freeLookModuleValue.getTargetPitch(), freeLookModuleValue.getTargetYaw());
            this.setPos(var2Value.x, var2Value.y, var2Value.z);
            float floatVal2 = freeLookModuleValue.getThirdPersonDistance();
            if (!freeLookModuleValue.isSmoothRotation()) {
               floatVal2 = this.clipToSpace(floatVal2);
            }

            this.moveBy(-floatVal2, 0.0F, 0.0F);
         }
      }
   }

   @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
   private void onIsThirdPerson(CallbackInfoReturnable callbackInfoReturnable) {
      if (FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled()) {
         callbackInfoReturnable.setReturnValue(true);
      } else if (FreeLookModule.freeLookInstance != null && FreeLookModule.freeLookInstance.isThirdPersonActive()) {
         callbackInfoReturnable.setReturnValue(true);
      }
   }
}
