package com.threesix.data;

import net.minecraft.item.ItemStack;
import com.threesix.util.StringVaultDecoder;

public final class NotificationEntry {
   public final String title;
   public final String message;
   public final ItemStack icon;
   public final int color;
   public final long createdAtMillis;

   public NotificationEntry(String string, String string2, ItemStack arg, int intVal, long longVal) {
      this.title = string;
      this.message = string2;
      this.icon = arg;
      this.color = intVal | 0xFF000000;
      this.createdAtMillis = longVal;
   }

   public boolean isExpired(long longVal) {
      return longVal - this.createdAtMillis >= 3500L;
   }

   public float getFadeProgress(long longVal) {

      long var1ThisValue = longVal - this.createdAtMillis;
      if (var1ThisValue <= 0L) {
         return 0.0F;
      }

      if (var1ThisValue < 220L) {
         return this.easeOutCubic((float)var1ThisValue / 220.0F);
      }

      long longVal2 = 3280L;
      return var1ThisValue > longVal2 ? this.easeOutCubic(Math.max(0.0F, 1.0F - (float)(var1ThisValue - longVal2) / 220.0F)) : 1.0F;
   }

   public float easeOutCubic(float floatVal) {

      float floatVal2 = 1.0F - Math.max(0.0F, Math.min(1.0F, floatVal));
      return 1.0F - floatVal2 * floatVal2 * floatVal2;
   }

}
