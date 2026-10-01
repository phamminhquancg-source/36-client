package com.threesix.module;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.setting.ModeSetting02;
import com.threesix.manager.NotificationHudManager;

public final class WeatherNotifierModule extends ModuleBase {
   public final ModeSetting02 notificationModeSetting = new ModeSetting02("Notification Mode", "Both", "Chat", "Toast", "Both");
   public final ClientSetting notifyThunderSetting = new ClientSetting("Notify Thunder", true);
   public Boolean wasRaining = null;
   public Boolean wasThundering = null;

   public WeatherNotifierModule() {
      super("WeatherNotifier", ModuleCategory.MISC);
      this.registerSetting(this.notificationModeSetting);
      this.registerSetting(this.notifyThunderSetting);
   }

   @Override
   public void onEnable() {
      this.wasRaining = null;
      this.wasThundering = null;
   }

   @Override
   public void onDisable() {
      this.wasRaining = null;
      this.wasThundering = null;
   }

   @Override
   public void onTick() {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         boolean minecraftClientValue = minecraftClient.world.isRaining();
         boolean minecraftClientValue2 = minecraftClient.world.isThundering();
         if (this.wasRaining == null) {
            this.wasRaining = minecraftClientValue;
            this.wasThundering = minecraftClientValue2;
            if (minecraftClientValue) {
               this.notifyWeatherChange("The rain started.", "Rain Started", -10835482);
            }

            if (minecraftClientValue2 && (Boolean)this.notifyThunderSetting.getValue()) {
               this.notifyWeatherChange("A thunderstorm started.", "Thunder Started", -4879105);
            }
         } else {
            if (minecraftClientValue && !this.wasRaining) {
               this.notifyWeatherChange("The rain started.", "Rain Started", -10835482);
            } else if (!minecraftClientValue && this.wasRaining) {
               this.notifyWeatherChange("The rain stopped.", "Rain Stopped", -340971);
            }

            if ((Boolean)this.notifyThunderSetting.getValue()) {
               if (minecraftClientValue2 && !this.wasThundering) {
                  this.notifyWeatherChange("A thunderstorm started.", "Thunder Started", -4879105);
               } else if (!minecraftClientValue2 && this.wasThundering) {
                  this.notifyWeatherChange("The thunderstorm ended.", "Thunder Ended", -340971);
               }
            }

            this.wasRaining = minecraftClientValue;
            this.wasThundering = minecraftClientValue2;
         }
      }
   }

   public void notifyWeatherChange(String string, String string2, int intVal) {
      String local = (String)this.notificationModeSetting.getValue();
      int intVal2 = !"Chat".equalsIgnoreCase(local) && !"Both".equalsIgnoreCase(local) ? 0 : 1;
      boolean flag = "Toast".equalsIgnoreCase(local) || "Both".equalsIgnoreCase(local);
      if (intVal2 != 0) {
         try {
            minecraftClient.inGameHud.getChatHud().addMessage(Text.literal("[WeatherNotifier] " + string));
         } catch (Throwable error) {
         }
      }

      if (flag) {
         NotificationHudManager.INSTANCE6.push("WeatherNotifier", string2, ItemStack.EMPTY, intVal);
      }
   }

}
