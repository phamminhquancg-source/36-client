package com.threesix.module;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.MinecraftClient;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;

public final class DiscordRpcModule extends ModuleBase {
   public static final String APPLICATION_ID = "1524597834450604032";
   public static DiscordRpcModule INSTANCE8;
   public ScheduledExecutorService scheduler;
   public volatile boolean isConnected = false;
   public volatile long startedAtSeconds = 0L;
   public Socket socket;
   public OutputStream outputStream;
   public InputStream inputStream;
   public static final int OPCODE_HANDSHAKE = 0;
   public static final int OPCODE_SET_ACTIVITY = 1;
   public static final int OPCODE_FRAME = 2;

   public DiscordRpcModule() {
      super("DiscordRPC", ModuleCategory.CLIENT);
      INSTANCE8 = this;
   }

   public static boolean isActive() {

      int local = INSTANCE8 != null && INSTANCE8.isEnabled() ? 1 : 0;
      return local != 0;
   }

   @Override
   public void onEnable() {
      this.startedAtSeconds = System.currentTimeMillis() / 1000L;
      this.scheduler = Executors.newSingleThreadScheduledExecutor(item -> {
         Thread local = new Thread(item, "threesix-drpc");
         local.setDaemon(true);
         return local;
      });
      this.scheduler.submit(this::connectAndHandshake);
      this.scheduler.scheduleAtFixedRate(this::updateActivity, 5L, 15L, TimeUnit.SECONDS);
   }

   @Override
   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.shutdownNow();
      }

      this.clearActivity();
      this.disconnect();
   }

   public void connectAndHandshake() {

      for (int index = 0; index <= 9; index++) {
         try {
            String local = this.resolveIpcPath(index);
            if (local == null) {
               break;
            }

            this.connectToIpc(local);
            if (this.isConnected) {
               this.sendHandshake();
               this.updateActivity();
               return;
            }
         } catch (Exception error) {
         }
      }
   }

   public String resolveIpcPath(int intVal) {
      String systemValue = System.getProperty("os.name", "").toLowerCase();
      if (systemValue.contains("win")) {
         return "\\\\.\\pipe\\discord-ipc-" + intVal;
      }

      String[] local = new String[]{System.getenv("XDG_RUNTIME_DIR"), System.getenv("TMPDIR"), System.getenv("TMP"), System.getenv("TEMP"), "/temp"};

      for (String string : local) {
         if (string != null) {
            File fileInst = new File(string, "discord-ipc-" + intVal);
            if (fileInst.exists()) {
               return fileInst.getAbsolutePath();
            }
         }
      }

      return null;
   }

   public void connectToIpc(String string) {
      try {

         String systemValue = System.getProperty("os.name", "").toLowerCase();
         if (systemValue.contains("win")) {
            this.connectWindowsNamedPipe(string);
         } else {
            this.connectUnixSocket(string);
         }
      } catch (Exception error) {
         this.isConnected = false;
      }
   }

   public void connectWindowsNamedPipe(String string) throws Exception {
      final RandomAccessFile randomAccessFileInst = new RandomAccessFile(string, "rw");
      this.outputStream = new FileOutputStream(randomAccessFileInst.getFD());
      this.inputStream = new InputStream() {

         @Override
         public int read() throws IOException {
            return randomAccessFileInst.read();
         }

         @Override
         public int read(byte[] string, int local, int local2) throws IOException {
            return randomAccessFileInst.read(string, local, local2);
         }

      };
      this.isConnected = true;
   }

   public void connectUnixSocket(String string) throws Exception {
      try {

         Class classValue = Class.forName("java.net.UnixDomainSocketAddress");
         Object local = classValue.getMethod("of", String.class).invoke(null, string);
         Class classValue2 = Class.forName("java.nio.channels.SocketChannel");
         Object classValue2Value = classValue2.getMethod("open", Class.forName("java.net.ProtocolFamily"))
            .invoke(null, Enum.valueOf((Class)Class.forName("java.net.StandardProtocolFamily"), "UNIX"));
         classValue2.getMethod("connect", Class.forName("java.net.SocketAddress")).invoke(classValue2Value, local);
         this.socket = (Socket)classValue2.getMethod("socket").invoke(classValue2Value);
         this.outputStream = this.socket.getOutputStream();
         this.inputStream = this.socket.getInputStream();
         this.isConnected = true;
      } catch (Exception error) {
         this.isConnected = false;
      }
   }

   public void disconnect() {
      this.isConnected = false;

      try {
         if (this.outputStream != null) {
            this.outputStream.close();
         }
      } catch (Exception error) {
      }

      try {
         if (this.inputStream != null) {
            this.inputStream.close();
         }
      } catch (Exception error2) {
      }

      try {
         if (this.socket != null) {
            this.socket.close();
         }
      } catch (Exception error3) {
      }

      this.outputStream = null;
      this.inputStream = null;
      this.socket = null;
   }

   public void sendHandshake() throws Exception {

      String local = "{\"v\":1,\"client_id\":\"1524597834450604032\"}";
      this.sendFrame(0, local);
      this.readFrame();
   }

   public void sendFrame(int intVal, String string) throws Exception {

      byte[] local = string.getBytes(StandardCharsets.UTF_8);
      byte[] local2 = new byte[8];
      local2[0] = (byte)(intVal & 0xFF);
      local2[1] = (byte)(intVal >> 8 & 0xFF);
      local2[2] = (byte)(intVal >> 16 & 0xFF);
      local2[3] = (byte)(intVal >> 24 & 0xFF);
      int intVal2 = local.length;
      local2[4] = (byte)(intVal2 & 0xFF);
      local2[5] = (byte)(intVal2 >> 8 & 0xFF);
      local2[6] = (byte)(intVal2 >> 16 & 0xFF);
      local2[7] = (byte)(intVal2 >> 24 & 0xFF);
      this.outputStream.write(local2);
      this.outputStream.write(local);
      this.outputStream.flush();
   }

   public String readFrame() throws Exception {
      byte[] local = new byte[8];
      int local3 = 0;
      while (local3 < 8) {
         int intVal = this.inputStream.read(local, local3, 8 - local3);
         if (intVal < 0) {
            throw new EOFException();
         }

         local3 += intVal;
      }

      int intVal2 = local[4] & 255 | (local[5] & 255) << 8 | (local[6] & 255) << 16 | (local[7] & 255) << 24;
      byte[] local2 = new byte[intVal2];
      local3 = 0;
      while (local3 < intVal2) {
         int intVal3 = this.inputStream.read(local2, local3, intVal2 - local3);
         if (intVal3 < 0) {
            throw new EOFException();
         }

         local3 += intVal3;
      }

      return new String(local2, StandardCharsets.UTF_8);
   }

   public void updateActivity() {

      if (!this.isConnected) {
         this.connectAndHandshake();
      } else {
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            String local = "Playing on Threesix Client";
            String local2 = mc != null && mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
            String stringValue = String.valueOf(System.currentTimeMillis());
            String local3 = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":"
               + ProcessHandle.current().pid()
               + ",\"activity\":{\"details\":\""
               + escapeJson2(local)
               + "\",\"state\":\""
               + escapeJson2(local2)
               + "\",\"timestamps\":{\"start\":"
               + this.startedAtSeconds
               + "},\"assets\":{\"large_image\":\"content\",\"large_text\":\"Threesix Client\",\"small_image\":\"minecraft\",\"small_text\":\"Minecraft\"}}},\"nonce\":\""
               + stringValue
               + "\"}";
            this.sendFrame(1, local3);
            this.readFrame();
         } catch (Exception error) {
            this.disconnect();
         }
      }
   }

   public void clearActivity() {
      if (this.isConnected) {
         try {
            String stringValue = String.valueOf(System.currentTimeMillis());
            String local = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid() + ",\"activity\":null},\"nonce\":\"" + stringValue + "\"}";
            this.sendFrame(1, local);
            this.readFrame();
         } catch (Exception error) {
         }
      }
   }

   public static String escapeJson2(String string) {

      return string == null ? "" : string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
   }

   public static String getVaultKey4() {

      return "T";
   }

}
