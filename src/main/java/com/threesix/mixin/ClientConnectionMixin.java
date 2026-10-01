package com.threesix.mixin;

import io.netty.channel.ChannelFutureListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.manager.ConfigManager;
import com.threesix.internal.ModuleBase;
import com.threesix.module.SusChunkFinderModule;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
   @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
   private static void onHandlePacket(Packet arg, PacketListener arg2, CallbackInfo callbackInfo) {
      try {
         try {
            if (arg instanceof ChunkDataS2CPacket local) {
               SusChunkFinderModule.field1.onLightUpdate(local.getChunkX(), local.getChunkZ(), local.getLightData(), MinecraftClient.getInstance().world);
            } else if (arg instanceof LightUpdateS2CPacket local2) {
               SusChunkFinderModule.field1.onLightUpdate(local2.getChunkX(), local2.getChunkZ(), local2.getData(), MinecraftClient.getInstance().world);
            }
         } catch (Throwable error) {
         }

         ModuleBase configManagerValue = ConfigManager.INSTANCE.getModuleByName("RTP Home Reset");
         if (configManagerValue != null && configManagerValue.isEnabled() && containsBlockedText(arg)) {
            callbackInfo.cancel();
            return;
         }

         ConfigManager.INSTANCE.onPacketSend2(arg);
      } catch (Exception error2) {
      }
   }

   private static boolean containsBlockedText(Object object) {
      if (object == null) {
         return false;
      }

      for (Method method : object.getClass().getDeclaredMethods()) {
         if (method.getParameterCount() == 0) {
            try {
               method.setAccessible(true);
               Object local = method.invoke(object);
               if (local instanceof Text local3) {
                  if (isBlocked(local3.getString())) {
                     return true;
                  }

                  if (isBlocked(local3.getString())) {
                     return true;
                  }
               }

               if (local instanceof String local4 && isBlocked(local4)) {
                  return true;
               }
            } catch (Throwable error) {
            }
         }
      }

      for (Class index = object.getClass(); index != null; index = index.getSuperclass()) {
         for (Field field : index.getDeclaredFields()) {
            try {
               field.setAccessible(true);
               Object local2 = field.get(object);
               if (local2 instanceof Text local5 && isBlocked(local5.getString())) {
                  return true;
               }

               if (local2 instanceof String local6 && isBlocked(local6)) {
                  return true;
               }
            } catch (Throwable error2) {
            }
         }
      }

      return false;
   }

   private static boolean isBlocked(String string) {
      if (string == null) {
         return false;
      }

      String local = string.toLowerCase();
      return local.contains("home deleted")
         || local.contains("home set")
         || local.contains("teleported to a random location")
         || local.contains("teleported to your home");
   }

   @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
   private void onSend(Packet arg, ChannelFutureListener channelFutureListener, boolean flag, CallbackInfo callbackInfo) {
      try {
         if (ConfigManager.INSTANCE.onPacketReceive2(arg)) {
            callbackInfo.cancel();
         }
      } catch (Exception error) {
      }
   }
}
