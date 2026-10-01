package com.threesix.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.StringVaultDecoder;

public final class ConfigFileManager {
   public static final String TXT_EXTENSION = ".txt";
   public static final Pattern invalidCharsPattern = Pattern.compile("[^A-Za-z0-9_\\- ]");

   public static Path getConfigsDirectory() {

      Path class310Value = MinecraftClient.getInstance().runDirectory.toPath().resolve("threesix_configs");

      try {
         Files.createDirectories(class310Value);
      } catch (IOException iOException) {
      }

      return class310Value;
   }

   public static Path getCurrentConfigFile() {
      Path class310Value = MinecraftClient.getInstance().runDirectory.toPath().resolve("threesix_config.txt");
      return class310Value;
   }

   public static String sanitizeName(String string) {
      return string == null ? "" : invalidCharsPattern.matcher(string.trim()).replaceAll("_");
   }

   public static List listConfigNames() {
      ArrayList arrayListInst = new ArrayList();
      Path local = getConfigsDirectory();
      if (!Files.isDirectory(local)) {
         return arrayListInst;
      }

      try {
         Files.list(local).forEach(entry -> {
            String local2 = entry.getFileName().toString();
            if (local2.endsWith(".txt")) {
               arrayListInst.add(local2.substring(0, local2.length() - ".txt".length()));
            }
         });
      } catch (IOException iOException) {
      }

      Collections.sort(arrayListInst, String.CASE_INSENSITIVE_ORDER);
      return arrayListInst;
   }

   public static boolean saveConfig(String string) {
      String local = sanitizeName(string);
      if (local.isEmpty()) {
         return false;
      }

      ConfigManager.INSTANCE.save();
      Path local2 = getCurrentConfigFile();
      Path local3 = getConfigsDirectory().resolve(local + ".txt");

      try {
         Files.copy(local2, local3, StandardCopyOption.REPLACE_EXISTING);
         return true;
      } catch (IOException iOException) {
         return false;
      }
   }

   public static boolean isConfigSaved(String string) {
      Path local = getConfigsDirectory().resolve(sanitizeName(string) + ".txt");
      return !Files.isRegularFile(local) ? false : applyConfigFile(local);
   }

   public static boolean deleteConfig(String string) {
      try {
         return Files.deleteIfExists(getConfigsDirectory().resolve(sanitizeName(string) + ".txt"));
      } catch (IOException iOException) {
         return false;
      }
   }

   public static String exportConfigCode() {
      try {
         ConfigManager.INSTANCE.save();
         byte[] filesValue = Files.readAllBytes(getCurrentConfigFile());
         return "WCFG-" + Base64.getUrlEncoder().withoutPadding().encodeToString(filesValue);
      } catch (IOException iOException) {
         return null;
      }
   }

   public static boolean importConfigCode(String string) {
      if (string == null) {
         return false;
      }

      String local = string.trim();
      if (local.startsWith("WCFG-")) {
         local = local.substring(5);
      }

      try {
         byte[] base64Value = Base64.getUrlDecoder().decode(local);
         Path local2 = getConfigsDirectory().resolve(".__shared_tmp.txt");
         Files.write(local2, base64Value);
         boolean flag = applyConfigFile(local2);

         try {
            Files.deleteIfExists(local2);
         } catch (IOException iOException) {
         }

         return flag;
      } catch (IOException | IllegalArgumentException local3) {
         return false;
      }
   }

   public static boolean applyConfigFile(Path path) {
      Map<String, Boolean> local = getModuleStates();

      try {
         Files.copy(path, getCurrentConfigFile(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException iOException) {
         return false;
      }

      ConfigManager.INSTANCE.load();
      Map<String, Boolean> local2 = getModuleStates();

      for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
         boolean flag = local.getOrDefault(moduleBase.getName2(), false);
         boolean flag2 = local2.getOrDefault(moduleBase.getName2(), false);
         if (flag != flag2) {
            try {
               if (flag2) {
                  moduleBase.onEnable();
               } else {
                  moduleBase.onDisable();
               }
            } catch (Throwable error) {
            }
         }
      }

      ConfigManager.INSTANCE.save();
      return true;
   }

   public static Map<String, Boolean> getModuleStates() {
      HashMap<String, Boolean> hashMapInst = new HashMap<>();

      for (ModuleBase moduleBase : ConfigManager.INSTANCE.getModules()) {
         hashMapInst.put(moduleBase.getName2(), moduleBase.isEnabled());
      }

      return hashMapInst;
   }

   public static String getClipboardText() {
      try {
         return MinecraftClient.getInstance().keyboard.getClipboard();
      } catch (Throwable error) {
         return "";
      }
   }

   public static void setClipboardText(String string) {
      try {

         MinecraftClient.getInstance().keyboard.setClipboard(string == null ? "" : string);
      } catch (Throwable error) {
      }
   }

   public static byte[] toUtf8Bytes(String string) {
      return string.getBytes(StandardCharsets.UTF_8);
   }

}
