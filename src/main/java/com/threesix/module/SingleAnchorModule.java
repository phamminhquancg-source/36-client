package com.threesix.module;

import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class SingleAnchorModule extends KeybindModuleBase {
   public final ClientSetting delaySetting = new ClientSetting("Delay", 0.0F, 0.0F, 20.0F);
   public final ClientSetting totemSlotSetting = new ClientSetting("Totem Slot", 1.0F, 1.0F, 9.0F);
   public final ClientSetting switchBackSetting = new ClientSetting("Switch Back", false);
   public int delayTicks = 0;
   public int stepIndex = 0;
   public boolean isSequenceActive = false;
   public boolean shouldSwitchBack = false;

   public SingleAnchorModule() {
      super("Single Anchor", ModuleCategory.COMBAT);
      this.registerSetting(this.delaySetting);
      this.registerSetting(this.totemSlotSetting);
      this.registerSetting(this.switchBackSetting);
   }

   @Override
   public void onEnable() {
      this.resetSequence();
      this.isSequenceActive = false;
      this.shouldSwitchBack = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetSequence();
      this.isSequenceActive = false;
      this.shouldSwitchBack = false;
      super.onDisable();
   }

   @Override
   public void onActivationKey() {
      if (this.isEnabled()) {
         this.resetSequence();
         this.isSequenceActive = true;
         this.shouldSwitchBack = false;
      }
   }

   @Override
   public void onPacketSend(Packet arg) {
      if ((Boolean)this.switchBackSetting.getValue() && this.shouldSwitchBack && arg instanceof HealthUpdateS2CPacket) {
         this.shouldSwitchBack = false;
         if (minecraftClient.player != null) {
            int intVal = this.findHotbarSlot(Items.RESPAWN_ANCHOR);
            if (intVal != -1) {
               minecraftClient.player.getInventory().setSelectedSlot(intVal);
            }
         }
      }
   }

   @Override
   public void onTick() {
      if (this.isSequenceActive && minecraftClient.currentScreen == null && minecraftClient.player != null && minecraftClient.world != null) {
         if (!this.hasAnchorItems()) {
            this.isSequenceActive = false;
            this.resetSequence();
         } else if (minecraftClient.crosshairTarget instanceof BlockHitResult local) {
            if (minecraftClient.world.getBlockState(local.getBlockPos()).isOf(Blocks.AIR)) {
               this.isSequenceActive = false;
               this.resetSequence();
            } else {
               int maxValue = Math.max(0, ((Float)this.delaySetting.getValue()).intValue());
               if (this.delayTicks < maxValue) {
                  this.delayTicks++;
               } else {
                  if (this.stepIndex == 0) {
                     this.selectItem(Items.RESPAWN_ANCHOR);
                  } else if (this.stepIndex == 1) {
                     this.interactWithBlock(local);
                  } else if (this.stepIndex == 2) {
                     this.selectItem(Items.GLOWSTONE);
                  } else if (this.stepIndex == 3) {
                     this.interactWithBlock(local);
                  } else if (this.stepIndex == 4) {
                     int intVal = ((Float)this.totemSlotSetting.getValue()).intValue() - 1;
                     this.selectSlot(intVal);
                  } else if (this.stepIndex == 5) {
                     this.interactWithBlock(local);
                     if ((Boolean)this.switchBackSetting.getValue()) {
                        this.shouldSwitchBack = true;
                     }
                  } else if (this.stepIndex == 6) {
                     this.isSequenceActive = false;
                     this.resetSequence();
                     return;
                  }

                  this.stepIndex++;
               }
            }
         } else {
            this.isSequenceActive = false;
            this.resetSequence();
         }
      }
   }

   public void resetSequence() {
      this.delayTicks = 0;
      this.stepIndex = 0;
   }

   public boolean hasAnchorItems() {
      byte byteVal = 0;
      boolean falseSnapshot = false;

      for (int index = 0; index < 9; index++) {
         ItemStack minecraftClientValue = minecraftClient.player.getInventory().getStack(index);
         if (minecraftClientValue.isOf(Items.RESPAWN_ANCHOR)) {
         }

         if (minecraftClientValue.isOf(Items.GLOWSTONE)) {
            falseSnapshot = true;
         }
      }

      return byteVal != 0 && falseSnapshot;
   }

   public int findHotbarSlot(Item arg) {

      for (int index = 0; index < 9; index++) {
         if (minecraftClient.player.getInventory().getStack(index).isOf(arg)) {
            return index;
         }
      }

      return -1;
   }

   public void selectItem(Item arg) {
      int intVal = this.findHotbarSlot(arg);
      if (intVal != -1) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
      }
   }

   public void selectSlot(int intVal) {
      if (intVal >= 0 && intVal <= 8) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
      }
   }

   public void interactWithBlock(BlockHitResult arg) {
      minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, arg);
      minecraftClient.player.swingHand(Hand.MAIN_HAND);
   }

}
