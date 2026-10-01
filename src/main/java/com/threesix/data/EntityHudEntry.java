package com.threesix.data;

import java.util.List;
import net.minecraft.text.Text;
import com.threesix.data.ItemStackEntry;
import com.threesix.util.XorBitUtils;
import com.threesix.data.HealthBarLayout;
import com.threesix.util.StringVaultDecoder;

public record EntityHudEntry(long expiresAtMs, Text nameLabel, int nameWidth, HealthBarLayout healthData, List<ItemStackEntry> items, int itemRowWidth) {

   public boolean pu() {
      return this.nameLabel == null && this.healthData == null && this.items.isEmpty();
   }

   

}
