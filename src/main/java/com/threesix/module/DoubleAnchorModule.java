package com.threesix.module;

import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.internal.KeybindModuleBase;

public final class DoubleAnchorModule extends KeybindModuleBase {
   public final ClientSetting delaySetting = new ClientSetting("Delay", 0.0F, 0.0F, 20.0F);
   public final ClientSetting totemSlotSetting = new ClientSetting("Totem Slot", 1.0F, 1.0F, 9.0F);
   public final ClientSetting switchBackSetting = new ClientSetting("Switch Back", false);
   public int delayTicks = 0;
   public int stepIndex = 0;
   public boolean isAnchoring = false;
   public BlockPos savedPos = null;
   public boolean shouldSwitchBack = false;

   public DoubleAnchorModule() {
      super("Double Anchor", ModuleCategory.COMBAT);
      this.registerSetting(this.delaySetting);
      this.registerSetting(this.totemSlotSetting);
      this.registerSetting(this.switchBackSetting);
   }

   @Override
   public void onEnable() {
      this.resetState();
      this.isAnchoring = false;
      this.shouldSwitchBack = false;
      this.savedPos = null;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetState();
      this.isAnchoring = false;
      this.shouldSwitchBack = false;
      this.savedPos = null;
      super.onDisable();
   }

   @Override
   public void toggleByKey() {
      super.toggleByKey();
   }

   @Override
   public void onActivationKey() {
      if (this.isEnabled()) {
         this.resetState();
         this.isAnchoring = true;
         this.shouldSwitchBack = false;
         this.savedPos = null;
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
      if (this.isAnchoring && minecraftClient.currentScreen == null && minecraftClient.player != null && minecraftClient.world != null) {
         if (!this.hasAnchors()) {
            this.isAnchoring = false;
            this.resetState();
         } else if (minecraftClient.crosshairTarget instanceof BlockHitResult local) {
            if (minecraftClient.world.getBlockState(local.getBlockPos()).isOf(Blocks.AIR)) {
               this.isAnchoring = false;
               this.resetState();
            } else {
               int maxValue = Math.max(0, ((Float)this.delaySetting.getValue()).intValue());
               if (this.delayTicks < maxValue) {
                  this.delayTicks++;
               } else {
                  if (this.stepIndex == 0) {
                     this.swapToSlot(Items.RESPAWN_ANCHOR);
                  } else if (this.stepIndex == 1) {
                     this.useAnchor(local);
                  } else if (this.stepIndex == 2) {
                     this.swapToSlot(Items.GLOWSTONE);
                  } else if (this.stepIndex == 3) {
                     this.useAnchor(local);
                  } else if (this.stepIndex == 4) {
                     this.swapToSlot(Items.RESPAWN_ANCHOR);
                  } else if (this.stepIndex == 5) {
                     this.useAnchor(local);
                     this.useAnchor(local);
                  } else if (this.stepIndex == 6) {
                     this.swapToSlot(Items.GLOWSTONE);
                  } else if (this.stepIndex == 7) {
                     this.useAnchor(local);
                  } else if (this.stepIndex == 8) {
                     int intVal = ((Float)this.totemSlotSetting.getValue()).intValue() - 1;
                     this.selectHotbarSlot(intVal);
                     this.savedPos = local.getBlockPos();
                  } else if (this.stepIndex == 9) {
                     this.useAnchor(local);
                     if ((Boolean)this.switchBackSetting.getValue()) {
                        this.shouldSwitchBack = true;
                     }
                  } else if (this.stepIndex == 10) {
                     this.isAnchoring = false;
                     this.resetState();
                     return;
                  }

                  this.stepIndex++;
               }
            }
         } else {
            this.isAnchoring = false;
            this.resetState();
         }
      }
   }

   public void resetState() {
      this.delayTicks = 0;
      this.stepIndex = 0;
   }

   public boolean hasAnchors() {
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

   public void swapToSlot(Item arg) {
      int intVal = this.findHotbarSlot(arg);
      if (intVal != -1) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
      }
   }

   public void selectHotbarSlot(int intVal) {
      if (intVal >= 0 && intVal <= 8) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
      }
   }

   public void useAnchor(BlockHitResult arg) {
      minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, arg);
      minecraftClient.player.swingHand(Hand.MAIN_HAND);
   }

}
