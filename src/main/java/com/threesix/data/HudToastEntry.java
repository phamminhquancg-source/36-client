package com.threesix.data;

import net.minecraft.util.Identifier;
import com.threesix.util.StringVaultDecoder;

public final class HudToastEntry {
   public final String title;
   public final String subtitle;
   public final int durationMs;
   public final Identifier toastIcon;
   public final long createdAt;

   public HudToastEntry(String string, String string2, int intVal, Identifier arg) {
      this.title = string;
      this.subtitle = string2;
      this.durationMs = intVal;
      this.toastIcon = arg;
      this.createdAt = System.currentTimeMillis();
   }

}
