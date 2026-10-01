package com.threesix.module;

import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BambooBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.registry.Registries;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;

public final class AutoToolModule extends ModuleBase {

   public AutoToolModule() {
      super("Auto Tool", ModuleCategory.MISC);
   }

   @Override
   public void onTick() {

      if (minecraftClient.player != null
         && minecraftClient.world != null
         && minecraftClient.interactionManager != null
         && minecraftClient.options.attackKey.isPressed()
         && minecraftClient.crosshairTarget != null) {
         HitResult local = minecraftClient.crosshairTarget;
         if (local.getType() == Type.ENTITY && local instanceof EntityHitResult) {
            this.selectBestForCombat();
         } else if (local.getType() == Type.BLOCK && local instanceof BlockHitResult local2) {
            this.selectBestForBlock(local2.getBlockPos());
         }
      }
   }

   public void selectBestForBlock(BlockPos arg) {
      BlockState minecraftClientValue = minecraftClient.world.getBlockState(arg);
      ItemStack minecraftClientValue2 = minecraftClient.player.getMainHandStack();
      double var9Snapshot = -1.0;
      int var7Snapshot = -1;

      for (int index = 0; index < 9; index++) {
         ItemStack minecraftClientValue3 = minecraftClient.player.getInventory().getStack(index);
         double doubleVal = getItemScore(minecraftClientValue3, minecraftClientValue, item -> true);
         if (doubleVal > var9Snapshot) {
            var9Snapshot = doubleVal;
            var7Snapshot = index;
         }
      }

      if (var7Snapshot != -1) {
         double doubleVal2 = getItemScore(minecraftClientValue2, minecraftClientValue, item -> true);
         if (var9Snapshot > doubleVal2 || !isUsableTool(minecraftClientValue2)) {
            this.selectSlot(var7Snapshot);
         }
      }
   }

   public void selectBestForCombat() {
      double var6Snapshot = Double.NEGATIVE_INFINITY;
      int var4Snapshot = -1;

      for (int index = 0; index < 9; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (!minecraftClientValue.isEmpty()) {
            double doubleVal = this.getAttackDamage(minecraftClientValue);
            if (doubleVal > var6Snapshot) {
               var6Snapshot = doubleVal;
               var4Snapshot = index;
            }
         }
      }

      if (var4Snapshot != -1) {
         this.selectSlot(var4Snapshot);
      }
   }

   public double getAttackDamage(ItemStack arg) {
      AttributeModifiersComponent local = (AttributeModifiersComponent)arg.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
      double doubleVal = 0.0;
      if (local != null) {
         for (Entry class9287 : local.modifiers()) {
            if (class9287.attribute().toString().contains("attack_damage")) {
               doubleVal += class9287.modifier().value();
            }
         }
      }

      if (doubleVal > 0.0) {
         return doubleVal;
      } else {
         String local2 = Registries.ITEM.getId(arg.getItem()).getPath();
         if (local2.endsWith("_sword")) {
            return 10.0 + this.getMaterialBonus(local2);
         } else {
            return local2.endsWith("_axe") ? 5.0 + this.getMaterialBonus(local2) : 0.0;
         }
      }
   }

   public void selectSlot(int intVal) {
      if (intVal >= 0 && intVal <= 8 && minecraftClient.player.getInventory().getSelectedSlot() != intVal) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
      }
   }

   public static double getItemScore(ItemStack arg, BlockState arg2, Predicate predicate) {
      if (predicate.test(arg) && isUsableTool(arg)) {
         String local = Registries.ITEM.getId(arg.getItem()).getPath();
         boolean flag = local.endsWith("_sword");
         return !arg.isSuitableFor(arg2)
               && (!flag || !(arg2.getBlock() instanceof BambooBlock) && !(arg2.getBlock() instanceof BambooShootBlock))
               && (!(arg.getItem() instanceof ShearsItem) || !(arg2.getBlock() instanceof LeavesBlock))
               && !arg2.isIn(BlockTags.WOOL)
            ? -1.0
            : arg.getMiningSpeedMultiplier(arg2) * 1000.0F;
      } else {
         return -1.0;
      }
   }

   public static boolean isUsableTool(ItemStack arg) {
      return isToolItem(arg.getItem());
   }

   public static boolean isToolItem(Item arg) {
      if (arg instanceof ShearsItem) {
         return true;
      }

      String local = Registries.ITEM.getId(arg).getPath();
      return local.endsWith("_pickaxe") || local.endsWith("_axe") || local.endsWith("_shovel") || local.endsWith("_hoe") || local.endsWith("_sword");
   }

   public double getMaterialBonus(String string) {
      if (string.startsWith("netherite_")) {
         return 6.0;
      } else if (string.startsWith("diamond_")) {
         return 5.0;
      } else if (string.startsWith("iron_")) {
         return 4.0;
      } else if (string.startsWith("golden_")) {
         return 3.0;
      } else if (string.startsWith("stone_")) {
         return 2.0;
      } else {
         return string.startsWith("wooden_") ? 1.0 : 0.0;
      }
   }

}
