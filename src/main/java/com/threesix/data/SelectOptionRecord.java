package com.threesix.data;

import net.minecraft.item.ItemStack;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record SelectOptionRecord(String value, String label, ItemStack previewStack) {

   public ItemStack getPreviewStack() {
      return this.previewStack == null ? ItemStack.EMPTY : this.previewStack.copy();
   }

   

}
