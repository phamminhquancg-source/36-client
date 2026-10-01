package com.threesix.module;

import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.hit.HitResult.Type;
import org.lwjgl.glfw.GLFW;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AnchorMacroModule extends KeybindModuleBase {
   public final ClientSetting switchDelaySetting = new ClientSetting("Switch Delay", 0.0F, 0.0F, 20.0F);
   public final ClientSetting glowstoneDelaySetting = new ClientSetting("Glowstone Delay", 0.0F, 0.0F, 20.0F);
   public final ClientSetting explodeDelaySetting = new ClientSetting("Explode Delay", 0.0F, 0.0F, 20.0F);
   public final ClientSetting totemSlotSetting = new ClientSetting("Totem Slot", 1.0F, 1.0F, 9.0F);
   public final ClientSetting switchBackSetting = new ClientSetting("Switch Back", false);
   public int switchDelayTicks;
   public int glowstoneDelayTicks;
   public int explodeDelayTicks;
   public boolean isPendingSwitchBack = false;

   public AnchorMacroModule() {
      super("Anchor Macro", ModuleCategory.COMBAT);
      this.registerSetting(this.switchDelaySetting);
      this.registerSetting(this.glowstoneDelaySetting);
      this.registerSetting(this.explodeDelaySetting);
      this.registerSetting(this.totemSlotSetting);
      this.registerSetting(this.switchBackSetting);
   }

   @Override
   public void onEnable() {

      this.resetDelays();
      this.isPendingSwitchBack = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetDelays();
      this.isPendingSwitchBack = false;
      super.onDisable();
   }

   @Override
   public void onPacketSend(Packet arg) {
      if ((Boolean)this.switchBackSetting.getValue() && this.isPendingSwitchBack && arg instanceof HealthUpdateS2CPacket) {
         this.isPendingSwitchBack = false;
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
      if (minecraftClient.player != null
         && minecraftClient.world != null
         && minecraftClient.interactionManager != null
         && minecraftClient.currentScreen == null
         && !this.isAnchorInHand()) {
         if (!this.isRightMouseHeld()) {
            this.resetDelays();
         } else {
            this.onAnchorUse();
         }
      }
   }

   public boolean isAnchorInHand() {
      int intVal = !minecraftClient.player.getMainHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD)
            && !minecraftClient.player.getOffHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD)
         ? 0
         : 1;
      boolean minecraftClientValue = minecraftClient.player.getMainHandStack().getItem() instanceof ShieldItem
         || minecraftClient.player.getOffHandStack().getItem() instanceof ShieldItem;
      boolean flag = this.isRightMouseHeld();
      return (intVal != 0 || minecraftClientValue) && flag;
   }

   public boolean isRightMouseHeld() {

      return minecraftClient.getWindow() != null && GLFW.glfwGetMouseButton(minecraftClient.getWindow().getHandle(), 1) == 1;
   }

   public void onAnchorUse() {
      if (minecraftClient.crosshairTarget instanceof BlockHitResult local && local.getType() == Type.BLOCK) {
         BlockPos var1Value = local.getBlockPos();
         BlockState minecraftClientValue = minecraftClient.world.getBlockState(var1Value);
         if (minecraftClientValue.isOf(Blocks.RESPAWN_ANCHOR)) {
            minecraftClient.options.useKey.setPressed(false);
            int intVal = (Integer)minecraftClientValue.get(RespawnAnchorBlock.CHARGES);
            if (intVal == 0) {
               this.handleGlowstone(local);
            } else {
               this.handleExplode(local);
            }
         }
      }
   }

   public void handleGlowstone(BlockHitResult arg) {

      if (!minecraftClient.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
         if (this.switchDelayTicks < ((Float)this.switchDelaySetting.getValue()).intValue()) {
            this.switchDelayTicks++;
            return;
         }

         this.switchDelayTicks = 0;
         if (!this.switchToItem(Items.GLOWSTONE)) {
            return;
         }
      }

      if (minecraftClient.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
         if (this.glowstoneDelayTicks < ((Float)this.glowstoneDelaySetting.getValue()).intValue()) {
            this.glowstoneDelayTicks++;
            return;
         }

         this.glowstoneDelayTicks = 0;
         this.useGlowstone(arg);
      }
   }

   public void handleExplode(BlockHitResult arg) {
      int maxValue = Math.max(0, Math.min(8, ((Float)this.totemSlotSetting.getValue()).intValue() - 1));
      if (minecraftClient.player.getInventory().getSelectedSlot() != maxValue) {
         if (this.switchDelayTicks < ((Float)this.switchDelaySetting.getValue()).intValue()) {
            this.switchDelayTicks++;
            return;
         }

         this.switchDelayTicks = 0;
         minecraftClient.player.getInventory().setSelectedSlot(maxValue);
      }

      if (minecraftClient.player.getInventory().getSelectedSlot() == maxValue) {
         if (this.explodeDelayTicks < ((Float)this.explodeDelaySetting.getValue()).intValue()) {
            this.explodeDelayTicks++;
            return;
         }

         this.explodeDelayTicks = 0;
         this.useGlowstone(arg);
         if ((Boolean)this.switchBackSetting.getValue()) {
            this.isPendingSwitchBack = true;
         }
      }
   }

   public int findHotbarSlot(Item arg) {

      for (int index = 0; index < 9; index++) {
         if (minecraftClient.player.getInventory().getStack(index).isOf(arg)) {
            return index;
         }
      }

      return -1;
   }

   public boolean switchToItem(Item arg) {
      int intVal = this.findHotbarSlot(arg);
      if (intVal != -1) {
         minecraftClient.player.getInventory().setSelectedSlot(intVal);
         return true;
      } else {
         return false;
      }
   }

   public void useGlowstone(BlockHitResult arg) {
      minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, arg);
      minecraftClient.player.swingHand(Hand.MAIN_HAND);
   }

   public void resetDelays() {

      this.switchDelayTicks = 0;
      this.glowstoneDelayTicks = 0;
      this.explodeDelayTicks = 0;
   }

}
