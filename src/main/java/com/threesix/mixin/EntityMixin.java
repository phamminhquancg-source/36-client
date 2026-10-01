package com.threesix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.NameProtectModule;
import com.threesix.module.FreecamModule;
import com.threesix.module.FreeLookModule;
import com.threesix.module.FakeRolesModule;

@Mixin(Entity.class)
public class EntityMixin {
   @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
   private void onChangeLookDirection(double doubleVal, double doubleVal2, CallbackInfo callbackInfo) {
      Entity local = (Entity)(Object)this;
      if (local == MinecraftClient.getInstance().player) {
         if (FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled()) {
            FreecamModule.freecamInstance.setFreecamRotation(doubleVal * 0.15 * FreecamModule.freecamInstance.getSensitivityScale(), doubleVal2 * 0.15 * FreecamModule.freecamInstance.getSensitivityScale());
            callbackInfo.cancel();
         } else if (FreeLookModule.freeLookInstance != null && FreeLookModule.freeLookInstance.isThirdPersonActive()) {
            FreeLookModule.freeLookInstance.applyFreeLookDelta(doubleVal, doubleVal2);
            callbackInfo.cancel();
         }
      }
   }

   @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
   private void onIsSneaking(CallbackInfoReturnable callbackInfoReturnable) {
      if (FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled() && ((Object)this) == MinecraftClient.getInstance().player) {
         callbackInfoReturnable.setReturnValue(false);
      }
   }

   @Inject(method = "shouldRender(D)Z", at = @At("HEAD"), cancellable = true)
   private void onShouldRender(double doubleVal, CallbackInfoReturnable callbackInfoReturnable) {
      if (FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled() && doubleVal < 25600.0) {
         callbackInfoReturnable.setReturnValue(true);
      }
   }

   @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
   private void onGetDisplayName(CallbackInfoReturnable callbackInfoReturnable) {
      if (FakeRolesModule.isFakeRolesActive()) {
         Text fakeRolesModuleValue = FakeRolesModule.applyFakeRoles((Text)callbackInfoReturnable.getReturnValue());
         if (fakeRolesModuleValue != callbackInfoReturnable.getReturnValue()) {
            callbackInfoReturnable.setReturnValue(fakeRolesModuleValue);
            return;
         }
      }

      if (NameProtectModule.nameProtectModule != null && NameProtectModule.nameProtectModule.isEnabled() && MinecraftClient.getInstance().getSession() != null) {
         String class310Value = MinecraftClient.getInstance().getSession().getUsername();
         if (class310Value != null) {
            Text local = (Text)callbackInfoReturnable.getReturnValue();
            if (local != null) {
               String local2 = local.getString();
               if (local2.contains(class310Value)) {
                  callbackInfoReturnable.setReturnValue(Text.literal(local2.replace(class310Value, NameProtectModule.nameProtectModule.getMaskedName())));
               }
            }
         }
      }
   }

   @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
   private void onGetName(CallbackInfoReturnable callbackInfoReturnable) {
      if (FakeRolesModule.isFakeRolesActive()) {
         Text fakeRolesModuleValue = FakeRolesModule.applyFakeRoles((Text)callbackInfoReturnable.getReturnValue());
         if (fakeRolesModuleValue != callbackInfoReturnable.getReturnValue()) {
            callbackInfoReturnable.setReturnValue(fakeRolesModuleValue);
            return;
         }
      }

      if (NameProtectModule.nameProtectModule != null && NameProtectModule.nameProtectModule.isEnabled() && MinecraftClient.getInstance().getSession() != null) {
         String class310Value = MinecraftClient.getInstance().getSession().getUsername();
         if (class310Value != null) {
            Text local = (Text)callbackInfoReturnable.getReturnValue();
            if (local != null) {
               String local2 = local.getString();
               if (local2.contains(class310Value)) {
                  callbackInfoReturnable.setReturnValue(Text.literal(local2.replace(class310Value, NameProtectModule.nameProtectModule.getMaskedName())));
               }
            }
         }
      }
   }
}
