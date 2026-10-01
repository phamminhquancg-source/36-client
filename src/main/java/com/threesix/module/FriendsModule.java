package com.threesix.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class FriendsModule extends ModuleBase {
   public static FriendsModule instance;
   public final ClientSetting namesSetting = new ClientSetting("Names", "");
   public final ClientSetting antiTriggerbotSetting = new ClientSetting("Anti Triggerbot", true);
   public final ClientSetting espColorSetting = new ClientSetting("ESP Color", true);
   public final ClientSetting autoLogSetting = new ClientSetting("Auto Log", true);
   public final ClientSetting spawnerProtectSetting = new ClientSetting("Spawner Protect", true);
   public final ClientSetting friendColorSetting = new ClientSetting("Friend Color", new Color(0, 200, 255));

   public FriendsModule() {
      super("Friends", ModuleCategory.CLIENT);
      this.registerSetting(this.namesSetting);
      this.registerSetting(this.antiTriggerbotSetting);
      this.registerSetting(this.espColorSetting);
      this.registerSetting(this.autoLogSetting);
      this.registerSetting(this.spawnerProtectSetting);
      this.registerSetting(this.friendColorSetting);
      instance = this;
   }

   public static boolean isFriend(String string) {
      if (instance != null && instance.isEnabled() && string != null && !string.isEmpty()) {
         String local = string.trim().toLowerCase(Locale.ROOT);

         for (String string2 : parseNames((String)instance.namesSetting.getValue())) {
            if (string2.equalsIgnoreCase(local)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isAntiTriggerbot() {
      int local = instance != null && (Boolean)instance.antiTriggerbotSetting.getValue() ? 1 : 0;
      return local != 0;
   }

   public static boolean isEspColorEnabled() {
      return instance != null && (Boolean)instance.espColorSetting.getValue();
   }

   public static boolean isAutoLogEnabled() {
      int local = instance != null && (Boolean)instance.autoLogSetting.getValue() ? 1 : 0;
      return local != 0;
   }

   public static boolean isSpawnerProtect() {
      int local = instance != null && (Boolean)instance.spawnerProtectSetting.getValue() ? 1 : 0;
      return local != 0;
   }

   public static Color getFriendColor() {
      if (instance == null) {
         return new Color(0, 200, 255);
      } else {
         Color local = (Color)instance.friendColorSetting.getValue();
         if (local == null) {
            return new Color(0, 200, 255);
         } else {
            return local.getAlpha() == 0 ? new Color(local.getRed(), local.getGreen(), local.getBlue(), 255) : local;
         }
      }
   }

   public static void onClientInit() {
   }

   public static List<String> getFriendNames() {
      List<String> local = instance == null ? List.of() : parseNames((String)instance.namesSetting.getValue());
      return local;
   }

   public static void setFriendNames(List<String> list) {
      if (instance != null) {
         instance.namesSetting.setValue(formatNames(list));
      }
   }

   public static List<String> parseNames(String string) {
      if (string != null && !string.isBlank()) {
         String local = string.replace('\n', ',').replace('\r', ',');
         LinkedHashSet<String> linkedHashSetInst = new LinkedHashSet<>();

         for (String string2 : local.split(",")) {
            String local2 = string2 == null ? "" : string2.trim();
            if (!local2.isEmpty()) {
               linkedHashSetInst.add(local2.toLowerCase(Locale.ROOT));
            }
         }

         return new ArrayList<>(linkedHashSetInst);
      } else {
         return List.of();
      }
   }

   public static String formatNames(List<String> list) {
      if (list != null && !list.isEmpty()) {
         StringBuilder stringBuilderInst = new StringBuilder();

         for (String string : list) {
            String local = string == null ? "" : string.trim();
            if (!local.isEmpty()) {
               if (!stringBuilderInst.isEmpty()) {
                  stringBuilderInst.append(", ");
               }

               stringBuilderInst.append(local);
            }
         }

         return stringBuilderInst.toString();
      } else {
         return "";
      }
   }

   public static String getNamePrefix() {
      return "N";
   }

}
