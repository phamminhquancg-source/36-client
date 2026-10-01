package com.threesix.module;

import com.threesix.mixin.MinecraftClientAccessor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.component.DataComponentTypes;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class FastPlaceModule extends ModuleBase {
   public final ClientSetting onlyXpSetting = new ClientSetting("Only XP", false);
   public final ClientSetting blocksSetting = new ClientSetting("Blocks", true);
   public final ClientSetting itemsSetting = new ClientSetting("Items", true);
   public final ClientSetting delaySetting = new ClientSetting("Delay", 0.0F, 0.0F, 10.0F);

   public FastPlaceModule() {
      super("Fast Place", ModuleCategory.MISC);
      this.registerSetting(this.onlyXpSetting);
      this.registerSetting(this.blocksSetting);
      this.registerSetting(this.itemsSetting);
      this.registerSetting(this.delaySetting);
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.currentScreen == null && minecraftClient.options.useKey.isPressed()) {
         ItemStack minecraftClientValue = minecraftClient.player.getMainHandStack();
         ItemStack minecraftClientValue2 = minecraftClient.player.getOffHandStack();
         if (this.shouldFastPlace(minecraftClientValue, minecraftClientValue2)) {
            MinecraftClientAccessor local = (MinecraftClientAccessor)minecraftClient;
            int maxValue = Math.max(0, ((Float)this.delaySetting.getValue()).intValue());
            if (local.threesix$getItemUseCooldown() != maxValue) {
               local.threesix$setItemUseCooldown(maxValue);
            }
         }
      }
   }

   public boolean shouldFastPlace(ItemStack arg, ItemStack arg2) {
      boolean var1Value = arg.isOf(Items.EXPERIENCE_BOTTLE);
      boolean var2Value = arg2.isOf(Items.EXPERIENCE_BOTTLE);
      if ((Boolean)this.onlyXpSetting.getValue()) {
         return var1Value || var2Value;
      } else {
         Item var1Value2 = arg.getItem();
         Item var2Value2 = arg2.getItem();
         if (this.isXpOrb(arg) || this.isXpOrb(arg2)) {
            return false;
         } else if (arg.isOf(Items.RESPAWN_ANCHOR)
            || arg.isOf(Items.GLOWSTONE)
            || arg2.isOf(Items.RESPAWN_ANCHOR)
            || arg2.isOf(Items.GLOWSTONE)) {
            return false;
         } else if (!(var1Value2 instanceof RangedWeaponItem) && !(var2Value2 instanceof RangedWeaponItem)) {
            boolean flag = var1Value2 instanceof BlockItem || var2Value2 instanceof BlockItem;
            return flag ? (Boolean)this.blocksSetting.getValue() : (Boolean)this.itemsSetting.getValue();
         } else {
            return false;
         }
      }
   }

   public boolean isXpOrb(ItemStack arg) {
      return arg.getComponents().contains(DataComponentTypes.FOOD);
   }

}
