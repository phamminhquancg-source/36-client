package com.threesix.mixin;

import net.minecraft.util.PlayerInput;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.data.BaseFinderSearchState;
import com.threesix.module.AutoPlaceModule;

@Mixin(ClientPlayerEntity.class)
public class AutoSellSpoofMixin {
   private static boolean didSpoofRot = false;
   private static float savedYaw = 0.0F;
   private static float savedPitch = 0.0F;

   @Inject(method = "sendMovementPackets", at = @At("HEAD"))
   private void onSendMovementPacketsHead(CallbackInfo callbackInfo) {
      ClientPlayerEntity local = (ClientPlayerEntity)(Object)this;
      if (BaseFinderSearchState.hasPendingRotation) {
         BaseFinderSearchState.spoofedYaw = local.getYaw();
         BaseFinderSearchState.spoofedPitch = local.getPitch();
         local.setYaw(BaseFinderSearchState.sellSpoofYaw);
         local.setPitch(BaseFinderSearchState.sellSpoofPitch);
         didSpoofRot = true;
      }
   }

   @Inject(method = "sendMovementPackets", at = @At("TAIL"))
   private void onSendMovementPacketsTail(CallbackInfo callbackInfo) {
      ClientPlayerEntity local = (ClientPlayerEntity)(Object)this;
      if (didSpoofRot) {
         didSpoofRot = false;
         local.setYaw(BaseFinderSearchState.spoofedYaw);
         local.setPitch(BaseFinderSearchState.spoofedPitch);
         BaseFinderSearchState.isRotationReady = true;
      }
   }

   @Inject(method = "tickMovement", at = @At("HEAD"))
   private void onTickMovementHead(CallbackInfo callbackInfo) {
      AutoPlaceModule autoPlaceModuleValue = AutoPlaceModule.getInstance();
      if (autoPlaceModuleValue != null && autoPlaceModuleValue.isEnabled() && autoPlaceModuleValue.isPauseMovementEnabled()) {
         long systemValue = System.currentTimeMillis() - BaseFinderSearchState.nextActionTimeMillis;
         if (systemValue < autoPlaceModuleValue.getPlaceDelayMs()) {
            ClientPlayerEntity local = (ClientPlayerEntity)(Object)this;
            if (local.input != null && local.input.playerInput != null) {
               PlayerInput local2 = local.input.playerInput;
               local.input.playerInput = new PlayerInput(false, false, false, false, local2.jump(), local2.sneak(), local2.sprint());
            }
         }
      }
   }
}
