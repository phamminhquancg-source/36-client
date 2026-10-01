package com.threesix.module;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class SpearSwapModule extends ModuleBase {
   public static SpearSwapModule instance;
   public final ClientSetting lungeSetting = new ClientSetting("Lunge", true);
   public final ClientSetting sharpnessSetting = new ClientSetting("Sharpness", false);
   public final ClientSetting onlySwordSetting = new ClientSetting("Only Sword", false);
   public final ClientSetting onlyAxeSetting = new ClientSetting("Only Axe", false);
   public final ClientSetting switchBackSetting = new ClientSetting("Switch Back", true);
   public final ClientSetting switchDelaySetting = new ClientSetting("Switch Delay", 1.0F, 1.0F, 20.0F);
   public int previousSlot = -1;
   public int switchDelayTicks = 0;
   public boolean isSwapping = false;

   public SpearSwapModule() {
      super("SpearSwap", ModuleCategory.COMBAT);
      this.registerSetting(this.lungeSetting);
      this.registerSetting(this.sharpnessSetting);
      this.registerSetting(this.onlySwordSetting);
      this.registerSetting(this.onlyAxeSetting);
      this.registerSetting(this.switchBackSetting);
      this.registerSetting(this.switchDelaySetting);
      instance = this;
   }

   public void trySwapToSpear() {

      if (minecraftClient.player != null) {
         boolean isSwappingSnapshot = this.isSwapping;
         this.isSwapping = true;
         if (!isSwappingSnapshot && this.switchDelayTicks <= 0) {
            PlayerInventory minecraftClientValue = minecraftClient.player.getInventory();
            int intVal = this.findBestWeaponSlot();
            if (intVal >= 0 && minecraftClientValue.getSelectedSlot() != intVal) {
               this.previousSlot = minecraftClientValue.getSelectedSlot();
               minecraftClientValue.setSelectedSlot(intVal);
               this.switchDelayTicks = Math.max(1, ((Float)this.switchDelaySetting.getValue()).intValue());
            }
         }
      }
   }

   public void endSwap() {
      this.isSwapping = false;
   }

   @Override
   public void onEnable() {
      this.previousSlot = -1;
      this.switchDelayTicks = 0;
   }

   @Override
   public void onDisable() {

      this.previousSlot = -1;
      this.switchDelayTicks = 0;
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.options != null) {
         PlayerInventory minecraftClientValue = minecraftClient.player.getInventory();
         if (this.switchDelayTicks > 0) {
            this.switchDelayTicks--;
            if (this.switchDelayTicks == 0) {
               if ((Boolean)this.switchBackSetting.getValue() && this.previousSlot >= 0 && this.previousSlot < 9 && minecraftClientValue.getSelectedSlot() != this.previousSlot) {
                  minecraftClientValue.setSelectedSlot(this.previousSlot);
               }

               this.previousSlot = -1;
            }
         }

         if (minecraftClient.options.attackKey.isPressed()) {
            this.trySwapToSpear();
         } else {
            this.endSwap();
         }
      }
   }

   public int findBestWeaponSlot() {
      PlayerInventory minecraftClientValue = minecraftClient.player.getInventory();
      boolean flag = (Boolean)this.onlySwordSetting.getValue();
      boolean flag2 = (Boolean)this.onlyAxeSetting.getValue();
      boolean flag3 = (Boolean)this.lungeSetting.getValue();
      boolean flag4 = (Boolean)this.sharpnessSetting.getValue();
      int integerValue = Integer.MIN_VALUE;
      int var8Snapshot = -1;

      for (int index = 0; index < 9; index++) {
         ItemStack var1Value = minecraftClientValue.getStack(index);
         if (!var1Value.isEmpty()) {
            String local = Registries.ITEM.getId(var1Value.getItem()).getPath();
            String local2 = "";

            try {
               local2 = var1Value.getName().getString().toLowerCase();
            } catch (Throwable error) {
            }

            boolean flag5 = local.endsWith("_sword");
            boolean var9Value = var1Value.getItem() instanceof AxeItem;
            boolean var1ValueValue = var1Value.getItem() == Items.TRIDENT
               || var1Value.getItem() == Items.MACE
               || local.contains("spear")
               || local2.contains("spear");
            boolean flag6 = this.hasLungeEnchant(var1Value);
            boolean flag7 = flag3 && flag6;
            if ((!flag || flag5) && (!flag2 || var9Value) && (flag || flag2 || flag5 || var9Value || var1ValueValue || flag7)) {
               int local3 = 0;
               if (flag7) {
                  local3 += 500;
               }

               if (var1ValueValue) {
                  local3 += 300;
               } else if (flag5) {
                  local3 += 200;
               } else if (var9Value) {
                  local3 += 100;
               }

               if (flag4) {
                  local3 += this.getSharpnessLevel(var1Value) * 60;
               }

               if (local3 > integerValue) {
                  integerValue = local3;
                  var8Snapshot = index;
               }
            }
         }
      }

      return var8Snapshot;
   }

   public boolean hasLungeEnchant(ItemStack arg) {
      try {

         ItemEnchantmentsComponent local = (ItemEnchantmentsComponent)arg.get(DataComponentTypes.ENCHANTMENTS);
         if (local == null) {
            return false;
         }

         for (RegistryEntry class6880 : local.getEnchantments()) {
            RegistryKey local2 = (RegistryKey)class6880.getKey().orElse(null);
            if (local2 != null) {
               if (local2.equals(Enchantments.WIND_BURST) || local2.equals(Enchantments.DENSITY) || local2.equals(Enchantments.RIPTIDE)) {
                  return true;
               }

               String var5Value = local2.getValue().toString().toLowerCase();
               if (var5Value.contains("lunge")) {
                  return true;
               }
            }
         }
      } catch (Throwable error) {
      }

      return false;
   }

   public int getSharpnessLevel(ItemStack arg) {
      try {
         ItemEnchantmentsComponent local = (ItemEnchantmentsComponent)arg.get(DataComponentTypes.ENCHANTMENTS);
         if (local == null) {
            return 0;
         }

         for (RegistryEntry class6880 : local.getEnchantments()) {
            RegistryKey local2 = (RegistryKey)class6880.getKey().orElse(null);
            if (local2 != null && local2.equals(Enchantments.SHARPNESS)) {
               return local.getLevel(class6880);
            }
         }
      } catch (Throwable error) {
      }

      return 0;
   }

}
