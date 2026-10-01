package com.threesix.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import com.threesix.util.XorBitUtils;
import com.threesix.data.SpotifyTrackState;
import com.threesix.util.StringVaultDecoder;

public final class SpotifyBridgeService {
   private static volatile SpotifyTrackState trackState = SpotifyTrackState.INACTIVE;
   private static Process bridgeProcess;
   private static BufferedWriter commandWriter;
   private static volatile boolean shouldStop = true;
   private static Thread bridgeThread;
   private static int reconnectAttempts;

   private SpotifyBridgeService() {
   }

   public static SpotifyTrackState getTrackState() {
      return trackState;
   }

   public static synchronized void start() {
      if (bridgeThread == null || !bridgeThread.isAlive()) {
         if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            shouldStop = false;
            bridgeThread = new Thread(SpotifyBridgeService::bridgeLoop, "threesix-spotify-bridge");
            bridgeThread.setDaemon(true);
            bridgeThread.start();
         }
      }
   }

   public static synchronized void stop() {

      shouldStop = true;

      try {
         if (bridgeProcess != null) {
            bridgeProcess.destroy();
         }
      } catch (Throwable error) {
      }

      bridgeProcess = null;
      commandWriter = null;
      bridgeThread = null;
   }

   public static Path getArtFilePath() {
      try {

         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.runDirectory != null) {
            return mc.runDirectory.toPath().resolve("threesix_spotify_art.img");
         }
      } catch (Throwable error) {
      }

      return null;
   }

   private static void bridgeLoop() {

      for (reconnectAttempts = 0; !shouldStop && reconnectAttempts < 4; reconnectAttempts++) {
         try {
            Path local = extractBridgeScript();
            Path local2 = getArtFilePath();
            if (local2 == null) {
               return;
            }

            ProcessBuilder processBuilderInst = new ProcessBuilder(
               "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", local.toString(), local2.toString()
            );
            processBuilderInst.redirectErrorStream(false);
            synchronized (SpotifyBridgeService.class) {
               if (shouldStop) {
                  return;
               }

               bridgeProcess = processBuilderInst.start();
               commandWriter = new BufferedWriter(new OutputStreamWriter(bridgeProcess.getOutputStream(), StandardCharsets.UTF_8));
            }

            String local3;
            try (BufferedReader local4 = new BufferedReader(new InputStreamReader(bridgeProcess.getInputStream(), StandardCharsets.UTF_8))) {
               while ((local3 = local4.readLine()) != null) {
                  handleBridgeLine(local3.trim());
               }
            }

            bridgeProcess.waitFor();
         } catch (Throwable error) {
         }

         trackState = SpotifyTrackState.INACTIVE;
      }

   }

   private static Path extractBridgeScript() throws Exception {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.runDirectory != null) {
         Path local = mc.runDirectory.toPath().resolve("config");
         Files.createDirectories(local);
         Path local2 = local.resolve("threesix_smtc_bridge.ps1");

         try (InputStream local3 = SpotifyBridgeService.class.getClassLoader().getResourceAsStream("assets/threesix/spotify/smtc_bridge.ps1")) {
            if (local3 == null) {
               throw new IllegalStateException("bridge script missing from mod resources");
            }

            Files.write(local2, local3.readAllBytes());
         }

         return local2;
      } else {
         throw new IllegalStateException("no run dir");
      }
   }

   private static void handleBridgeLine(String string) {
      if (!string.isEmpty() && string.charAt(0) == '{') {
         try {
            if (!readJsonBoolean(string, "active")) {
               trackState = SpotifyTrackState.INACTIVE;
               return;
            }

            trackState = new SpotifyTrackState(
               true,
               readJsonValue(string, "title"),
               readJsonValue(string, "artist"),
               readJsonLong(string, "posMs"),
               readJsonLong(string, "durMs"),
               readJsonBoolean(string, "playing"),
               readJsonBoolean(string, "canSeek"),
               readJsonInt(string, "artV"),
               readJsonInt(string, "vol"),
               System.nanoTime()
            );
         } catch (Throwable error) {
         }
      }
   }

   private static String readJsonValue(String string, String string2) {
      String local = "\"" + string2 + "\"";
      int intVal = string.indexOf(local);
      if (intVal < 0) {
         return "";
      }

      intVal = string.indexOf(58, intVal + local.length());
      if (intVal < 0) {
         return "";
      }

      intVal++;

      while (intVal < string.length() && string.charAt(intVal) == ' ') {
         intVal++;
      }

      if (intVal < string.length() && string.charAt(intVal) == '"') {
         StringBuilder stringBuilderInst = new StringBuilder();
         intVal++;

         while (intVal < string.length()) {
            char charVal = string.charAt(intVal);
            if (charVal == '\\' && intVal + 1 < string.length()) {
               char charVal2 = string.charAt(intVal + 1);
               if (charVal2 == '"' || charVal2 == '\\' || charVal2 == '/') {
                  stringBuilderInst.append(charVal2);
                  intVal += 2;
                  continue;
               }

               if (charVal2 == 'n') {
                  stringBuilderInst.append('\n');
                  intVal += 2;
                  continue;
               }

               if (charVal2 == 't') {
                  stringBuilderInst.append('\t');
                  intVal += 2;
                  continue;
               }

               if (charVal2 == 'u' && intVal + 5 < string.length()) {
                  try {
                     stringBuilderInst.append((char)Integer.parseInt(string.substring(intVal + 2, intVal + 6), 16));
                  } catch (Throwable error) {
                  }

                  intVal += 6;
                  continue;
               }
            }

            if (charVal == '"') {
               break;
            }

            stringBuilderInst.append(charVal);
            intVal++;
         }

         return stringBuilderInst.toString();
      } else {
         int var3Snapshot = intVal;

         while (var3Snapshot < string.length() && string.charAt(var3Snapshot) != ',' && string.charAt(var3Snapshot) != '}') {
            var3Snapshot++;
         }

         return string.substring(intVal, var3Snapshot).trim();
      }
   }

   private static long readJsonLong(String string, String string2) {
      try {

         return Long.parseLong(readJsonValue(string, string2));
      } catch (Throwable error) {
         return 0L;
      }
   }

   private static int readJsonInt(String string, String string2) {
      try {

         return Integer.parseInt(readJsonValue(string, string2));
      } catch (Throwable error) {
         return 0;
      }
   }

   private static boolean readJsonBoolean(String string, String string2) {
      return "true".equalsIgnoreCase(readJsonValue(string, string2));
   }

   private static void sendCommand(String string) {
      try {
         BufferedWriter commandWriterSnapshot;
         synchronized (SpotifyBridgeService.class) {
            commandWriterSnapshot = commandWriter;
         }

         if (commandWriterSnapshot != null) {
            commandWriterSnapshot.write(string);
            commandWriterSnapshot.newLine();
            commandWriterSnapshot.flush();
         }
      } catch (Throwable error) {
      }
   }

   public static void nextTrack() {
      sendCommand("NEXT");
   }

   public static void previousTrack() {
      sendCommand("PREV");
   }

   public static void togglePlayPause() {
      sendCommand("PLAYPAUSE");
   }

   public static void seekTo(long longVal) {

      sendCommand("SEEK " + Math.max(0L, longVal));
   }

   public static void setVolume(int intVal) {

      sendCommand("VOLUME " + Math.max(0, Math.min(100, intVal)));
   }

}
