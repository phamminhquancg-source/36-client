package com.threesix.setting;

import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import org.lwjgl.glfw.GLFW;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AutoHitCrystalToggle extends KeybindModuleBase {
   public final ClientSetting delayTicksSetting = new ClientSetting("Delay (ticks)", 1, 0, 10);
   public int cooldownTicks = 0;

   public AutoHitCrystalToggle() {
      super("AutoHitCrystal", ModuleCategory.COMBAT);
      this.registerSetting(this.delayTicksSetting);
   }

   @Override
   public void onEnable() {
      this.cooldownTicks = 0;
   }

   @Override
   public void onActivationKey() {
   }

   public boolean isActivationKeyDown() {
      int intVal = this.getBindKeyCode();
      if (intVal == 0) {
         return true;
      }

      if (minecraftClient.getWindow() == null) {
         return false;
      }

      if (intVal <= 0 || intVal > 348) {
         return false;
      }

      try {
         return GLFW.glfwGetKey(minecraftClient.getWindow().getHandle(), intVal) == 1;
      } catch (Exception error) {
         return false;
      }
   }

   @Override
   public void onTick() {
      if (this.cooldownTicks > 0) {
         this.cooldownTicks--;
      } else if (minecraftClient.player != null
         && minecraftClient.world != null
         && minecraftClient.interactionManager != null
         && minecraftClient.currentScreen == null
         && this.isActivationKeyDown()
         && minecraftClient.crosshairTarget != null
         && minecraftClient.crosshairTarget.getType() == Type.BLOCK) {
         BlockHitResult local = (BlockHitResult)minecraftClient.crosshairTarget;
         BlockPos var1Value = local.getBlockPos();
         BlockPos var2Value = var1Value.up();
         BlockPos var3Value = var2Value.up();
         EndCrystalEntity local2 = this.findCrystalAbove(var3Value);
         if (local2 != null) {
            minecraftClient.interactionManager.attackEntity(minecraftClient.player, local2);
            minecraftClient.player.swingHand(Hand.MAIN_HAND);
            this.cooldownTicks = (Integer)this.delayTicksSetting.getValue();
         } else if (!this.hasBedBlockBelow(var2Value)) {
            int intVal = this.findHotbarSlot(Items.OBSIDIAN);
            if (intVal >= 0) {
               if (minecraftClient.player.getInventory().getSelectedSlot() != intVal) {
                  minecraftClient.player.getInventory().setSelectedSlot(intVal);
               }

               BlockHitResult local3 = new BlockHitResult(Vec3d.ofCenter(var1Value).add(0.0, 0.5, 0.0), Direction.UP, var1Value, false);
               minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, local3);
               minecraftClient.player.swingHand(Hand.MAIN_HAND);
               this.cooldownTicks = (Integer)this.delayTicksSetting.getValue();
            }
         } else if (this.isEmptySpawnBox(var3Value)) {
            int intVal2 = this.findHotbarSlot(Items.END_CRYSTAL);
            if (intVal2 >= 0) {
               if (minecraftClient.player.getInventory().getSelectedSlot() != intVal2) {
                  minecraftClient.player.getInventory().setSelectedSlot(intVal2);
               }

               BlockHitResult local4 = new BlockHitResult(Vec3d.ofCenter(var2Value).add(0.0, 0.5, 0.0), Direction.UP, var2Value, false);
               minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, local4);
               minecraftClient.player.swingHand(Hand.MAIN_HAND);
               this.cooldownTicks = (Integer)this.delayTicksSetting.getValue();
            }
         }
      }
   }

   public boolean hasBedBlockBelow(BlockPos arg) {

      BlockState minecraftClientValue = minecraftClient.world.getBlockState(arg);
      return minecraftClientValue.isOf(Blocks.OBSIDIAN) || minecraftClientValue.isOf(Blocks.BEDROCK);
   }

   public boolean isEmptySpawnBox(BlockPos arg) {
      if (!minecraftClient.world.isAir(arg)) {
         return false;
      }

      Box local = new Box(
         arg.getX(), arg.getY(), arg.getZ(), arg.getX() + 1.0, arg.getY() + 2.0, arg.getZ() + 1.0
      );
      return minecraftClient.world.getOtherEntities(null, local).isEmpty();
   }

   public EndCrystalEntity findCrystalAbove(BlockPos arg) {
      Box local = new Box(
         arg.getX(), arg.getY(), arg.getZ(), arg.getX() + 1.0, arg.getY() + 2.0, arg.getZ() + 1.0
      );

      for (Entity class1297 : minecraftClient.world.getOtherEntities(null, local)) {
         if (class1297 instanceof EndCrystalEntity local2 && local2.isAlive()) {
            return local2;
         }
      }

      return null;
   }

   public int findHotbarSlot(Item arg) {

      for (int index = 0; index < 9; index++) {
         if (minecraftClient.player.getInventory().getStack(index).isOf(arg)) {
            return index;
         }
      }

      return -1;
   }

}
