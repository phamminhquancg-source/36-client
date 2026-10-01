package com.threesix.mixin;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.HitboxExpandModule;
import com.threesix.manager.MutedSoundRegistry;
import com.threesix.module.NameTagsModule;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
   @Inject(method = "updateRenderState", at = @At("TAIL"))
   private void threesix$updateNametagState(Entity arg, EntityRenderState arg2, float floatVal, CallbackInfo callbackInfo) {
      if (arg instanceof LivingEntity local && NameTagsModule.isActive()) {
         NameTagsModule nameTagsModuleValue = NameTagsModule.instance;
         if (nameTagsModuleValue != null && nameTagsModuleValue.call2(local, arg2.squaredDistanceToCamera)) {
            MutedSoundRegistry.mute(arg2);
         } else {
            MutedSoundRegistry.call1(arg2);
         }
      } else {
         MutedSoundRegistry.call1(arg2);
      }
   }

   @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
   private void threesix$renderCustomNametag(EntityRenderState arg, MatrixStack arg2, OrderedRenderCommandQueue arg3, CameraRenderState arg4, CallbackInfo callbackInfo) {
      if (MutedSoundRegistry.isMuted(arg)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "getShadowRadius", at = @At("RETURN"), cancellable = true)
   private void threesix$hitbox(EntityRenderState arg, CallbackInfoReturnable callbackInfoReturnable) {
      HitboxExpandModule hitboxExpandModuleValue = HitboxExpandModule.instance;
      if (hitboxExpandModuleValue != null && hitboxExpandModuleValue.isEnabled()) {
         callbackInfoReturnable.setReturnValue((Float)callbackInfoReturnable.getReturnValue() + (Float)hitboxExpandModuleValue.expandSetting.getValue());
      }
   }
}
