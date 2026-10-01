package com.threesix.module;

import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import com.threesix.manager.ConfigManager;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.gui.ConfigManagerScreen;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class ConfigShareModule extends ModuleBase {
   public static ConfigShareModule INSTANCE3;

   public ConfigShareModule() {
      super("Config Share", ModuleCategory.CLIENT);
      INSTANCE3 = this;
   }

   public static ConfigShareModule getInstance() {
      return INSTANCE3;
   }

   @Override
   public void onEnable() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null) {
         mc.execute(() -> {
            mc.setScreen(new ConfigManagerScreen());
            this.setEnabled(false);
         });
      }
   }

   public String buildShareText() {
      StringBuilder stringBuilderInst = new StringBuilder();
      String configManagerValue = ConfigManager.INSTANCE.getCurrentConfigName();
      stringBuilderInst.append("#").append(configManagerValue != null ? configManagerValue.trim() : "default").append("\n");

      for (Object moduleObj : ConfigManager.INSTANCE.getModules()) {
         ModuleBase moduleBase = (ModuleBase)moduleObj;
         if (moduleBase.getCategory() != ModuleCategory.CLIENT) {
            stringBuilderInst.append(moduleBase.getName2()).append(":").append(moduleBase.isEnabled() ? "1" : "0").append(":").append(moduleBase.getKeyCode()).append(":");

            for (Object settingObj : moduleBase.getSettings()) {
               ClientSetting clientSetting = (ClientSetting)settingObj;
               String local = this.formatSettingValue(clientSetting);
               stringBuilderInst.append(local != null ? local : "").append(",");
            }

            stringBuilderInst.append("\n");
         }
      }

      return stringBuilderInst.toString();
   }

   public static String parseConfigName(String string) {

      if (string == null) {
         return null;
      }

      for (String string2 : string.split("\\n")) {
         String local = string2.trim();
         if (!local.isEmpty()) {
            if (local.startsWith("#")) {
               String local2 = local.substring(1).trim().replaceAll("[\\\\/:*?\"<>|]", "_");
               if (local2.length() > 32) {
                  local2 = local2.substring(0, 32).trim();
               }

               return local2.isEmpty() ? null : local2;
            }
            break;
         }
      }

      return null;
   }

   public void importShareText(String string) {
      if (string != null) {
         for (String string2 : string.replace("\r\n", "\n").replace("\r", "\n").split("\\n")) {
            if (!string2.trim().startsWith("#")) {
               String[] local = string2.split(":", 4);
               if (local.length >= 4) {
                  ModuleBase configManagerValue = ConfigManager.INSTANCE.getModuleByName(local[0]);
                  if (configManagerValue != null) {
                     boolean flag = local[1].equals("1");
                     if (flag != configManagerValue.isEnabled()) {
                        configManagerValue.toggle();
                     }

                     try {
                        configManagerValue.setKeyCode(Integer.parseInt(local[2]));
                     } catch (Exception error) {
                     }

                     String[] local2 = local[3].split(",", -1);
                     int local3 = 0;
                     for (Object settingObj : configManagerValue.getSettings()) {
                        ClientSetting clientSetting = (ClientSetting)settingObj;
                        if (local3 >= local2.length) {
                           break;
                        }

                        if (!local2[local3].isEmpty()) {
                           this.parseSettingValue(clientSetting, local2[local3]);
                        }

                        local3++;
                     }
                  }
               }
            }
         }
      }
   }

   public String formatSettingValue(ClientSetting clientSetting) {

      Object local = clientSetting.getValue();
      if (local instanceof Boolean) {
         return (Boolean)local ? "1" : "0";
      } else if (local instanceof Float) {
         return String.valueOf((Float)local);
      } else if (local instanceof Double) {
         return String.valueOf((Double)local);
      } else if (local instanceof Integer) {
         return String.valueOf((Integer)local);
      } else if (local instanceof String) {
         return ((String)local).replace(",", "").replace("\n", "");
      } else {
         return local instanceof Color local2 ? String.format("%02x%02x%02x%02x", local2.getRed(), local2.getGreen(), local2.getBlue(), local2.getAlpha()) : "";
      }
   }

   public void parseSettingValue(ClientSetting clientSetting, String string) {
      try {

         Object local = clientSetting.getValue();
         if (local instanceof Boolean) {
            clientSetting.setValue(string.equals("1"));
         } else if (local instanceof Float) {
            clientSetting.setValue(Float.parseFloat(string));
         } else if (local instanceof Double) {
            clientSetting.setValue(Double.parseDouble(string));
         } else if (local instanceof Integer) {
            clientSetting.setValue(Integer.parseInt(string));
         } else if (local instanceof String) {
            clientSetting.setValue(string);
         } else if (local instanceof Color && string.length() == 8) {
            clientSetting.setValue(
               new Color(
                  Integer.parseInt(string.substring(0, 2), 16),
                  Integer.parseInt(string.substring(2, 4), 16),
                  Integer.parseInt(string.substring(4, 6), 16),
                  Integer.parseInt(string.substring(6, 8), 16)
               )
            );
         }
      } catch (Exception error) {
      }
   }

}
