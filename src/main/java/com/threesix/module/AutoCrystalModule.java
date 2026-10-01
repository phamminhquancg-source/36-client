package com.threesix.module;

import java.util.Random;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.HitResult.Type;
import org.lwjgl.glfw.GLFW;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AutoCrystalModule extends KeybindModuleBase {
   public final ClientSetting placeDelaySetting = new ClientSetting("Place Delay", 2.0F, 0.0F, 20.0F);
   public final ClientSetting breakDelaySetting = new ClientSetting("Break Delay", 2.0F, 0.0F, 20.0F);
   public final ClientSetting placeChanceSetting = new ClientSetting("Place Chance", 100.0F, 0.0F, 100.0F);
   public final ClientSetting breakChanceSetting = new ClientSetting("Break Chance", 100.0F, 0.0F, 100.0F);
   public final ClientSetting fakePunchSetting = new ClientSetting("Fake Punch", false);
   public final ClientSetting antiWeaknessSetting = new ClientSetting("Anti-Weakness", false);
   public final Random random = new Random();
   public boolean isArmed = false;
   public int placeCooldown = 0;
   public int breakCooldown = 0;
   public int tickCounter = 0;

   public AutoCrystalModule() {
      super("Auto Crystal", ModuleCategory.COMBAT);
      this.registerSetting(this.placeDelaySetting);
      this.registerSetting(this.breakDelaySetting);
      this.registerSetting(this.placeChanceSetting);
      this.registerSetting(this.breakChanceSetting);
      this.registerSetting(this.fakePunchSetting);
      this.registerSetting(this.antiWeaknessSetting);
   }

   @Override
   public void onEnable() {
      this.placeCooldown = 0;
      this.breakCooldown = 0;
      this.isArmed = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {

      this.placeCooldown = 0;
      this.breakCooldown = 0;
      this.isArmed = false;
   }

   @Override
   public void onActivationKey() {
   }

   public boolean isBindHeld() {

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

      if (++this.tickCounter % 2 == 0 && minecraftClient.player != null && minecraftClient.world != null && minecraftClient.currentScreen == null) {
         boolean flag = this.placeCooldown != 0;
         boolean flag2 = this.breakCooldown != 0;
         if (flag) {
            this.placeCooldown--;
         }

         if (flag2) {
            this.breakCooldown--;
         }

         if (!minecraftClient.player.isDead()) {
            if (!this.isBindHeld()) {
               this.placeCooldown = 0;
               this.breakCooldown = 0;
               this.isArmed = false;
            } else {
               this.isArmed = true;
               if (minecraftClient.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
                  HitResult local = minecraftClient.crosshairTarget;
                  int intVal = this.random.nextInt(100) + 1;
                  if (local instanceof BlockHitResult local3 && local3.getType() == Type.BLOCK) {
                     BlockPos var5Value = local3.getBlockPos();
                     boolean minecraftClientValue = minecraftClient.world.getBlockState(var5Value).isOf(Blocks.OBSIDIAN);
                     boolean minecraftClientValue2 = minecraftClient.world.getBlockState(var5Value).isOf(Blocks.BEDROCK);
                     if (minecraftClientValue || minecraftClientValue2) {
                        boolean flag3 = this.canPlaceCrystal(var5Value);
                        if (!flag && intVal <= (Float)this.placeChanceSetting.getValue()) {
                           if (flag3) {
                              minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, local3);
                              minecraftClient.player.swingHand(Hand.MAIN_HAND);
                              this.placeCooldown = ((Float)this.placeDelaySetting.getValue()).intValue();
                           }

                           if ((Boolean)this.fakePunchSetting.getValue() && !flag2 && intVal <= (Float)this.breakChanceSetting.getValue()) {
                              minecraftClient.interactionManager.attackBlock(var5Value, local3.getSide());
                              minecraftClient.player.swingHand(Hand.MAIN_HAND);
                              this.breakCooldown = ((Float)this.breakDelaySetting.getValue()).intValue();
                           }
                        }
                     }
                  }

                  intVal = this.random.nextInt(100) + 1;
                  if (local instanceof EntityHitResult local4) {
                     Entity var12Value = local4.getEntity();
                     if (var12Value instanceof EndCrystalEntity && !flag2 && intVal <= (Float)this.breakChanceSetting.getValue()) {
                        int minecraftClientValue3 = minecraftClient.player.getInventory().getSelectedSlot();
                        if ((Boolean)this.antiWeaknessSetting.getValue() && this.shouldSwapToSword()) {
                           for (int index = 0; index < 9; index++) {
                              ItemStack minecraftClientValue4 = minecraftClient.player.getInventory().getStack(index);
                              String local2 = Registries.ITEM.getId(minecraftClientValue4.getItem()).getPath();
                              if (local2.endsWith("_sword")) {
                                 minecraftClient.player.getInventory().setSelectedSlot(index);
                                 break;
                              }
                           }
                        }

                        minecraftClient.interactionManager.attackEntity(minecraftClient.player, var12Value);
                        minecraftClient.player.swingHand(Hand.MAIN_HAND);
                        this.breakCooldown = ((Float)this.breakDelaySetting.getValue()).intValue();
                        if ((Boolean)this.antiWeaknessSetting.getValue()) {
                           minecraftClient.player.getInventory().setSelectedSlot(minecraftClientValue3);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean canPlaceCrystal(BlockPos arg) {
      BlockPos var1Value = arg.up();
      if (!minecraftClient.world.isAir(var1Value)) {
         return false;
      }

      Box local = new Box(
         var1Value.getX(), var1Value.getY(), var1Value.getZ(), var1Value.getX() + 1.0, var1Value.getY() + 2.0, var1Value.getZ() + 1.0
      );
      return minecraftClient.world.getOtherEntities(null, local).isEmpty();
   }

   public boolean shouldSwapToSword() {
      if (minecraftClient.player == null) {
         return false;
      }

      boolean minecraftClientValue = minecraftClient.player.hasStatusEffect(StatusEffects.WEAKNESS);
      if (!minecraftClientValue) {
         return false;
      }

      boolean minecraftClientValue2 = minecraftClient.player.hasStatusEffect(StatusEffects.STRENGTH);
      int minecraftClientValue3 = minecraftClient.player.getStatusEffect(StatusEffects.WEAKNESS).getAmplifier();
      int intVal = minecraftClientValue2 ? minecraftClient.player.getStatusEffect(StatusEffects.STRENGTH).getAmplifier() : -1;
      return intVal <= minecraftClientValue3;
   }

   public int findHotbarSlot(Item arg) {

      for (int index = 0; index < 9; index++) {
         if (minecraftClient.player.getInventory().getStack(index).isOf(arg)) {
            return index;
         }
      }

      return -1;
   }

   public static String getItemSeparator() {
      return "_";
   }

}
