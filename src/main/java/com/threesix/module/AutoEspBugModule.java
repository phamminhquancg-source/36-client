package com.threesix.module;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.lwjgl.glfw.GLFW;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.setting.ModeSetting02;

public final class AutoEspBugModule extends KeybindModuleBase {
   public static final long setHomeWaitMs = 1500L;
   public static final long homeWaitMs = 10000L;
   public static final long travelTimeoutMs = 20000L;
   public static final double travelDistance = 150.0;
   public static final double jitterDistance = 25.0;
   public static final String[] rtpWorlds = new String[]{"asia", "eu west", "eu central", "east"};
   public final ClientSetting homeSetting = new ClientSetting("Home", 1, 1, 10);
   public final ModeSetting02 methodSetting = new ModeSetting02("Method", "RTP Method", "RTP Method", "Relog Fast");
   public boolean isRunning;
   public boolean wasKeyHeld;
   public int stage;
   public long stageStartTime;
   public long nextActionTime;
   public String rtpWorld;
   public String lastHomeStatus;
   public long homeStatusTime;
   public double startX;
   public double startZ;
   public boolean isTeleportDone;
   public long travelStartTime;
   public boolean isHomeDeleted;
   public double lastX;
   public double lastZ;
   public boolean hasLastPos;
   public String lastServerName;
   public ServerInfo lastServerInfo;
   public boolean isReconnecting;
   public long relogStartTime;
   public long reconnectAtTime;

   public AutoEspBugModule() {
      super("AutoEspBug", ModuleCategory.DONUT);
      this.registerSetting(this.methodSetting);
      this.registerSetting(this.homeSetting);
      this.homeSetting.withVisibility(() -> {
         return this.methodSetting.isModeSelected2("RTP Method");
      });
   }

   @Override
   public void onEnable() {
      this.resetState();
   }

   @Override
   public void onDisable() {

      this.resetState();
   }

   @Override
   public void onActivationKey() {
   }

   @Override
   public void onTick() {
      long systemValue = System.currentTimeMillis();
      if (this.methodSetting.isModeSelected2("Relog Fast")) {
         this.runRelogFast(systemValue);
      } else {
         if (this.isReconnecting) {
            this.isReconnecting = false;
         }

         if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.getNetworkHandler() != null && minecraftClient.currentScreen == null) {
            boolean flag = this.isBindKeyHeld();
            boolean flag2 = flag && !this.wasKeyHeld;
            this.wasKeyHeld = flag;
            if (flag2) {
               this.isRunning = true;
               this.rtpWorld = rtpWorlds[ThreadLocalRandom.current().nextInt(rtpWorlds.length)];
               this.isTeleportDone = false;
               this.travelStartTime = 0L;
               this.isHomeDeleted = false;
               this.hasLastPos = false;
               this.lastHomeStatus = null;
               this.homeStatusTime = 0L;
               this.stage = 0;
               this.nextActionTime = 0L;
               this.stageStartTime = 0L;
            }

            if (this.isRunning) {
               long systemValue2 = System.currentTimeMillis();
               String stringValue = String.valueOf(this.homeSetting.getValue());
               switch (this.stage) {
                  case 0:
                     this.startX = minecraftClient.player.getX();
                     this.startZ = minecraftClient.player.getZ();
                     this.isTeleportDone = true;
                     this.sendCommand("/sethome " + stringValue);
                     this.stageStartTime = systemValue2;
                     this.stage = 10;
                     break;
                  case 1:
                     if (systemValue2 < this.nextActionTime) {
                        return;
                     }

                     this.sendCommand("/rtp " + this.rtpWorld);
                     this.stageStartTime = systemValue2;
                     this.stage = 2;
                     break;
                  case 2:
                     if (!this.isTeleportDone) {
                        this.resetState();
                        return;
                     }

                     double minecraftClientValue = minecraftClient.player.getX() - this.startX;
                     double minecraftClientValue2 = minecraftClient.player.getZ() - this.startZ;
                     if (minecraftClientValue * minecraftClientValue + minecraftClientValue2 * minecraftClientValue2 >= 22500.0) {
                        this.sendCommand("/home " + stringValue);
                        this.travelStartTime = systemValue2;
                        this.isHomeDeleted = false;
                        this.hasLastPos = false;
                        this.stage = 3;
                     } else if (systemValue2 - this.stageStartTime >= 20000L) {
                        this.resetState();
                     }
                     break;
                  case 3:
                     double minecraftClientValue3 = minecraftClient.player.getX();
                     double minecraftClientValue4 = minecraftClient.player.getZ();
                     if (this.hasLastPos) {
                        double var18ThisValue = minecraftClientValue3 - this.lastX;
                        double var10ThisValue = minecraftClientValue4 - this.lastZ;
                        if (var18ThisValue * var18ThisValue + var10ThisValue * var10ThisValue >= 625.0) {
                           this.isHomeDeleted = true;
                        }
                     }

                     this.lastX = minecraftClientValue3;
                     this.lastZ = minecraftClientValue4;
                     this.hasLastPos = true;
                     if (this.isHomeDeleted) {
                        this.sendCommand("/delhome " + stringValue);
                        this.resetState();
                     } else if (systemValue2 - this.travelStartTime >= 20000L) {
                        this.resetState();
                     }
                     break;
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                  default:
                     this.resetState();
                     break;
                  case 10:
                     if (systemValue2 - this.stageStartTime < 1500L) {
                        return;
                     }

                     String local = this.getHomeStatus(this.stageStartTime);
                     if (local != null) {
                        if (local.equals("LIMITS")) {
                           this.sendCommand("/delhome " + stringValue);
                           this.stageStartTime = systemValue2;
                           this.stage = 11;
                        } else if (local.equals("SET")) {
                           this.stage = 1;
                           this.nextActionTime = systemValue2 + 1000L;
                        }
                     } else if (systemValue2 - this.stageStartTime >= 10000L) {
                        this.resetState();
                     }
                     break;
                  case 11:
                     String local2 = this.getHomeStatus(this.stageStartTime);
                     if (local2 == null || !local2.equals("DELETED")) {
                        if (systemValue2 - this.stageStartTime >= 10000L) {
                           this.nextActionTime = 0L;
                           this.resetState();
                        }
                     } else if (this.nextActionTime == 0L) {
                        this.nextActionTime = systemValue2 + 1000L;
                     } else if (systemValue2 >= this.nextActionTime) {
                        this.startX = minecraftClient.player.getX();
                        this.startZ = minecraftClient.player.getZ();
                        this.isTeleportDone = true;
                        this.sendCommand("/sethome " + stringValue);
                        this.stageStartTime = systemValue2;
                        this.nextActionTime = 0L;
                        this.stage = 12;
                     }
                     break;
                  case 12:
                     String local3 = this.getHomeStatus(this.stageStartTime);
                     if (local3 != null && local3.equals("SET")) {
                        this.stage = 1;
                        this.nextActionTime = systemValue2 + 1000L;
                     } else if (systemValue2 - this.stageStartTime >= 10000L) {
                        this.resetState();
                     }
               }
            }
         }
      }
   }

   public String getHomeStatus(long longVal) {

      return this.lastHomeStatus != null && this.homeStatusTime > longVal ? this.lastHomeStatus : null;
   }

   @Override
   public void onPacketSend(Packet arg) {
      try {
         if (!this.isRunning) {
            return;
         }

         String local = extractMessageText(arg);
         if (local == null) {
            return;
         }

         String local2 = local.toLowerCase();
         if (local2.contains("your home")) {
            if (this.stage == 3) {
               this.isHomeDeleted = true;
            }

            return;
         }

         String nullSnapshot = null;
         if (local2.contains("home limit")) {
            nullSnapshot = "LIMITS";
         } else if (local2.contains("home delet")) {
            nullSnapshot = "DELETED";
         } else if (local2.contains("home set")) {
            nullSnapshot = "SET";
         }

         if (nullSnapshot != null) {
            this.lastHomeStatus = nullSnapshot;
            this.homeStatusTime = System.currentTimeMillis();
         }
      } catch (Throwable error) {
      }
   }

   public static String extractMessageText(Object object) {
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

   public void runRelogFast(long longVal) {

      boolean flag = this.isBindKeyHeld();
      int intVal = flag && !this.wasKeyHeld ? 1 : 0;
      this.wasKeyHeld = flag;
      if (this.isReconnecting) {
         if (longVal - this.relogStartTime >= 15000L) {
            this.isReconnecting = false;
         } else {
            if (minecraftClient.world == null && minecraftClient.currentScreen instanceof DisconnectedScreen && longVal >= this.reconnectAtTime) {
               try {
                  ServerAddress class639Value = ServerAddress.parse(this.lastServerName);
                  ConnectScreen.connect(minecraftClient.currentScreen, minecraftClient, class639Value, this.lastServerInfo, false, null);
               } catch (Throwable error) {
               }

               this.isReconnecting = false;
            }
         }
      } else {
         if (intVal != 0) {
            try {
               if (minecraftClient.getNetworkHandler() == null) {
                  return;
               }

               ServerInfo minecraftClientValue = minecraftClient.getCurrentServerEntry();
               if (minecraftClientValue == null || minecraftClientValue.address == null || minecraftClientValue.address.isBlank()) {
                  return;
               }

               this.lastServerName = minecraftClientValue.address;
               this.lastServerInfo = minecraftClientValue;
               minecraftClient.getNetworkHandler().getConnection().disconnect(Text.literal("Reconnecting..."));
               this.isReconnecting = true;
               this.relogStartTime = longVal;
               this.reconnectAtTime = longVal + 1500L;
            } catch (Throwable error2) {
            }
         }
      }
   }

   public boolean isBindKeyHeld() {
      int intVal = this.getBindKeyCode();
      return intVal <= 0 || intVal > 348 ? false : GLFW.glfwGetKey(minecraftClient.getWindow().getHandle(), intVal) == 1;
   }

   public void resetState() {
      this.isRunning = false;
      this.wasKeyHeld = false;
      this.stage = 0;
      this.nextActionTime = 0L;
      this.stageStartTime = 0L;
      this.rtpWorld = null;
      this.lastHomeStatus = null;
      this.homeStatusTime = 0L;
      this.isTeleportDone = false;
      this.travelStartTime = 0L;
      this.isHomeDeleted = false;
      this.hasLastPos = false;
   }

   public void sendCommand(String string) {
      if (minecraftClient.getNetworkHandler() != null) {
         String local = string.startsWith("/") ? string.substring(1) : string;
         minecraftClient.getNetworkHandler().sendChatCommand(local);
      }
   }

}
