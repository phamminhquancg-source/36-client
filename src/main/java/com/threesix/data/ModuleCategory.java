package com.threesix.data;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public enum ModuleCategory {
   COMBAT("COMBAT"),
   RENDER("RENDER"),
   MISC("MISC"),
   DONUT("DONUT"),
   BASEFINDING("BASEFINDING"),
   CLIENT("CLIENT");

   public final String name;

   ModuleCategory(String string) {
      this.name = string;
   }

   public String getName() {
      return this.name;
   }

   public ItemStack getIcon() {
      return ItemStack.EMPTY;
   }

   public Identifier getIconTexture() {

      return Identifier.of("threesix", "textures/gui/category/" + this.name.toLowerCase() + ".png");
   }

}
