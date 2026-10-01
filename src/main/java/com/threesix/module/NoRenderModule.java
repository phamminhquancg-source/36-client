package com.threesix.module;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.biome.Biome.Precipitation;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class NoRenderModule extends ModuleBase {
   public static NoRenderModule instance;
   public final ClientSetting rainSetting = new ClientSetting("Rain", true);
   public final ClientSetting snowSetting = new ClientSetting("Snow", true);
   public final ClientSetting thunderSetting = new ClientSetting("Thunder", true);

   public NoRenderModule() {
      super("NoRender", ModuleCategory.RENDER);
      instance = this;
      this.registerSetting(this.rainSetting);
      this.registerSetting(this.snowSetting);
      this.registerSetting(this.thunderSetting);
   }

   public static boolean isActive() {
      return instance != null && instance.isEnabled() && minecraftClient != null && minecraftClient.world != null;
   }

   public static boolean isRainActive() {
      return isActive() && (Boolean)instance.rainSetting.getValue();
   }

   public static boolean isSnowActive() {
      int local = isActive() && (Boolean)instance.snowSetting.getValue() ? 1 : 0;
      return local != 0;
   }

   public static boolean isThunderActive() {
      int local = isActive() && (Boolean)instance.thunderSetting.getValue() ? 1 : 0;
      return local != 0;
   }

   public static boolean shouldHideParticles() {
      int local = isRainActive() && isSnowActive() ? 1 : 0;
      return local != 0;
   }

   public static Precipitation filterPrecipitation(Precipitation arg) {
      if (!isActive() || arg == null) {
         return arg;
      } else if (arg == Precipitation.RAIN && isRainActive()) {
         return Precipitation.NONE;
      } else {
         return arg == Precipitation.SNOW && isSnowActive() ? Precipitation.NONE : arg;
      }
   }

   public static boolean isRainHidden() {
      return shouldHideParticles();
   }

   public static boolean shouldCancelWeatherSound(SoundEvent arg) {
      if (isActive() && arg != null) {
         return !isRainActive() || arg != SoundEvents.WEATHER_RAIN && arg != SoundEvents.WEATHER_RAIN_ABOVE
            ? isThunderActive() && (arg == SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER || arg == SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT)
            : true;
      } else {
         return false;
      }
   }

}
