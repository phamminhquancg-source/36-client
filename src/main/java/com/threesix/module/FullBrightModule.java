package com.threesix.module;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.client.option.SimpleOption;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.StringVaultDecoder;
import com.threesix.data.ModuleCategory;

public final class FullBrightModule extends ModuleBase {
   public static final double fullBrightGamma = 10.0;
   public double savedGamma = 1.0;
   public boolean isApplied = false;
   public static Field gammaField;

   public FullBrightModule() {
      super("FullBright", ModuleCategory.RENDER);
   }

   @Override
   public void onEnable() {
      if (minecraftClient.options != null) {
         this.savedGamma = (Double)minecraftClient.options.getGamma().getValue();
         this.setGamma(10.0);
         this.isApplied = true;
      }
   }

   @Override
   public void onDisable() {
      if (minecraftClient.options != null) {
         this.setGamma(this.isApplied ? this.savedGamma : 1.0);
         this.isApplied = false;
      }
   }

   @Override
   public void onTick() {
      if (minecraftClient.options != null) {
         if (!this.isApplied) {
            this.savedGamma = (Double)minecraftClient.options.getGamma().getValue();
            if (Math.abs(this.savedGamma - 10.0) < 1.0E-4) {
               this.savedGamma = 1.0;
            }

            this.setGamma(10.0);
            this.isApplied = true;
         } else {
            try {
               double doubleVal = (Double)minecraftClient.options.getGamma().getValue();
               if (Math.abs(doubleVal - 10.0) > 1.0E-4) {
                  this.setGamma(10.0);
               }
            } catch (Exception error) {
            }
         }
      }
   }

   public void setGamma(double doubleVal) {
      SimpleOption minecraftClientValue = minecraftClient.options.getGamma();
      Field gammaFieldSnapshot = gammaField;
      if (gammaFieldSnapshot == null) {
         gammaFieldSnapshot = this.findGammaField(minecraftClientValue);
         gammaField = gammaFieldSnapshot;
      }

      if (gammaFieldSnapshot != null) {
         try {
            gammaFieldSnapshot.set(minecraftClientValue, doubleVal);
            Double local = (Double)minecraftClientValue.getValue();
            if (local != null && Math.abs(local - doubleVal) <= 1.0E-4) {
               return;
            }

            gammaField = null;
         } catch (Exception error) {
         }
      }

      try {
         minecraftClientValue.setValue(doubleVal);
      } catch (Exception error2) {
      }
   }

   public Field findGammaField(SimpleOption arg) {
      Object var1Value;
      try {
         var1Value = arg.getValue();
      } catch (Exception error) {
         var1Value = null;
      }

      Field nullSnapshot = null;

      for (Field field : SimpleOption.class.getDeclaredFields()) {
         if (!Modifier.isStatic(field.getModifiers())) {
            if ("value".equals(field.getName())) {
               nullSnapshot = field;
            }

            field.setAccessible(true);

            try {
               Object local = field.get(arg);
               if (var1Value == null ? local == null : var1Value.equals(local)) {
                  return field;
               }
            } catch (Exception error2) {
            }
         }
      }

      if (nullSnapshot != null) {
         nullSnapshot.setAccessible(true);
         return nullSnapshot;
      } else {
         return null;
      }
   }

}
