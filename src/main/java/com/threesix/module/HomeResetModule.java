package com.threesix.module;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.ClientSetting;

public final class HomeResetModule extends ModuleBase {
   public static final long ackTimeoutMs = 1000L;
   public static final long resendIntervalMs = 1500L;
   public static final int maxRetries = 5;
   public final ClientSetting homeSlotSetting = new ClientSetting("Home Slot", 1, 1, 5);
   public volatile boolean isRunning = false;
   public int stage;
   public String pendingCommand;
   public long lastSendTime;
   public boolean isCommandConfirmed;
   public int retryCount;
   public long ackDeadline;

   public HomeResetModule() {
      super("HomeReset", ModuleCategory.DONUT);
      this.registerSetting(this.homeSlotSetting);
   }

   @Override
   public void onEnable() {
      if (minecraftClient != null && minecraftClient.player != null && minecraftClient.world != null && !this.isRunning) {
         this.isRunning = true;
         this.stage = 0;
         this.pendingCommand = null;
         this.retryCount = 0;
         this.ackDeadline = 0L;
      } else if (!this.isRunning) {
         this.toggle();
      }
   }

   @Override
   public void onDisable() {
      this.isRunning = false;
      this.pendingCommand = null;
   }

   @Override
   public void onTick() {
      if (this.isRunning
         && minecraftClient.player != null
         && minecraftClient.world != null
         && minecraftClient.getNetworkHandler() != null
         && minecraftClient.currentScreen == null) {
         long systemValue = System.currentTimeMillis();
         String stringValue = String.valueOf(this.homeSlotSetting.getValue());
         if (this.stage == 0) {
            this.advanceStage(systemValue, "delhome " + stringValue, 1);
         } else if (this.stage == 1) {
            this.advanceStage(systemValue, "sethome " + stringValue, 2);
         } else {
            this.finishReset();
         }
      }
   }

   public void advanceStage(long longVal, String string, int intVal) {
      if (this.pendingCommand == null) {
         this.retryCount = 0;
         this.sendCommand(string, longVal);
      } else if (this.isCommandConfirmed) {
         if (longVal >= this.ackDeadline) {
            this.pendingCommand = null;
            this.stage = intVal;
            if (intVal > 1) {
               this.finishReset();
            }
         }
      } else {
         if (longVal - this.lastSendTime >= 1500L) {
            if (this.retryCount >= 5) {
               this.finishReset();
            } else {
               this.retryCount++;
               this.sendCommand(string, longVal);
            }
         }
      }
   }

   public void sendCommand(String string, long longVal) {
      this.pendingCommand = string;
      this.isCommandConfirmed = false;
      this.lastSendTime = longVal;
      this.ackDeadline = longVal + 1000L;
      this.sendChatCommand("/" + string);
   }

   public void finishReset() {
      this.isRunning = false;
      this.pendingCommand = null;
      if (this.isEnabled()) {
         this.toggle();
      }
   }

   @Override
   public boolean onPacketReceive(Packet arg) {
      try {
         if (this.isRunning && this.pendingCommand != null && !this.isCommandConfirmed && arg instanceof ChatCommandSignedC2SPacket local) {
            String nullSnapshot = null;

            try {
               nullSnapshot = local.command();
            } catch (Throwable error) {
            }

            if (nullSnapshot != null && nullSnapshot.equalsIgnoreCase(this.pendingCommand)) {
               this.isCommandConfirmed = true;
            }
         }
      } catch (Throwable error2) {
      }

      return false;
   }

   @Override
   public void onPacketSend(Packet arg) {
      try {
         if (!this.isRunning || this.pendingCommand == null || this.isCommandConfirmed) {
            return;
         }

         String local = extractPacketText(arg);
         if (local == null) {
            return;
         }

         String local2 = local.toLowerCase();
         String nullSnapshot = null;
         if (this.pendingCommand.startsWith("delhome ")) {
            nullSnapshot = "home deleted";
         } else if (this.pendingCommand.startsWith("sethome ")) {
            nullSnapshot = "home set";
         }

         if (nullSnapshot != null && local2.contains(nullSnapshot)) {
            this.isCommandConfirmed = true;
         }
      } catch (Throwable error) {
      }
   }

   public static String extractPacketText(Object object) {
      try {

         for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.getParameterCount() == 0) {
               try {
                  method.setAccessible(true);
                  Object local = method.invoke(object);
                  if (local instanceof Text local5) {
                     String local2 = local5.getString();
                     if (local2 != null && !local2.isEmpty()) {
                        return local2;
                     }
                  } else if (local instanceof String local6 && !local6.isEmpty()) {
                     return local6;
                  }
               } catch (Throwable error) {
               }
            }
         }

         for (Field field : object.getClass().getDeclaredFields()) {
            try {
               field.setAccessible(true);
               Object local3 = field.get(object);
               if (local3 instanceof Text local7) {
                  String local4 = local7.getString();
                  if (local4 != null && !local4.isEmpty()) {
                     return local4;
                  }
               } else if (local3 instanceof String local8 && !local8.isEmpty()) {
                  return local8;
               }
            } catch (Throwable error2) {
            }
         }
      } catch (Throwable error3) {
      }

      return null;
   }

   public void sendChatCommand(String string) {
      if (minecraftClient != null) {
         ClientPlayNetworkHandler nullSnapshot = null;

         try {
            if (minecraftClient.player != null) {
               nullSnapshot = minecraftClient.player.networkHandler;
            }
         } catch (Throwable error) {
         }

         if (nullSnapshot == null) {
            try {
               nullSnapshot = minecraftClient.getNetworkHandler();
            } catch (Throwable error2) {
            }
         }

         if (nullSnapshot != null) {
            String local = string.startsWith("/") ? string.substring(1) : string;

            try {
               nullSnapshot.sendChatCommand(local);
            } catch (Throwable error3) {
            }
         }
      }
   }

}
