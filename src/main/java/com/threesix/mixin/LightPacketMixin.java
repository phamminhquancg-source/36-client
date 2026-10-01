package com.threesix.mixin;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.SusChunkFinderModule;

@Mixin(ClientPlayNetworkHandler.class)
public class LightPacketMixin {
   @Inject(method = "onLightUpdate", at = @At("TAIL"))
   private void threesix$captureLightUpdate(LightUpdateS2CPacket arg, CallbackInfo callbackInfo) {
      SusChunkFinderModule.field1.onLightUpdate(arg.getChunkX(), arg.getChunkZ(), arg.getData(), MinecraftClient.getInstance().world);
   }

   @Inject(method = "onChunkData", at = @At("TAIL"))
   private void threesix$captureChunkLight(ChunkDataS2CPacket arg, CallbackInfo callbackInfo) {
      SusChunkFinderModule.field1.onLightUpdate(arg.getChunkX(), arg.getChunkZ(), arg.getLightData(), MinecraftClient.getInstance().world);
   }
}
