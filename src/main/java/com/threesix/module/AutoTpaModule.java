package com.threesix.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.setting.ModeSetting02;

public final class AutoTpaModule extends ModuleBase {
   public final ClientSetting playerSetting = new ClientSetting("Player", "Player");
   public final ModeSetting02 modeSetting = new ModeSetting02("Mode", "tpahere", "tpa", "tpahere");
   public final ClientSetting minDelaySetting = new ClientSetting("Min Delay", 10.0F, 1.0F, 100.0F);
   public final ClientSetting maxDelaySetting = new ClientSetting("Max Delay", 30.0F, 1.0F, 100.0F);
   public int cooldownTicks = 0;
   public int triggerCooldown = 0;
   public boolean isRequestPending = false;
   public int lastWorldId = -1;
   public int lastServerId = -1;
   public static final int triggerCooldownTicks = 400;

   public AutoTpaModule() {
      super("AutoTPA", ModuleCategory.MISC);
      this.registerSetting(this.playerSetting);
      this.registerSetting(this.modeSetting);
      this.registerSetting(this.minDelaySetting);
      this.registerSetting(this.maxDelaySetting);
   }

   @Override
   public void onEnable() {
      this.cooldownTicks = 0;
      this.triggerCooldown = 0;
      this.isRequestPending = false;
      this.lastWorldId = minecraftClient.player != null ? minecraftClient.player.getLastAttackedTime() : -1;
      this.lastServerId = minecraftClient.player != null ? minecraftClient.player.getLastAttackTime() : -1;
   }

   @Override
   public void onDisable() {
      this.cooldownTicks = 0;
      this.triggerCooldown = 0;
      this.isRequestPending = false;
      this.lastWorldId = -1;
      this.lastServerId = -1;
   }

   @Override
   public void onPacketSend(Packet arg) {
      if (minecraftClient.player != null && arg instanceof EntityVelocityUpdateS2CPacket local && local.getEntityId() == minecraftClient.player.getId()) {
         this.triggerRequest();
      }
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.getNetworkHandler() != null) {
         int minecraftClientValue = minecraftClient.player.getLastAttackedTime();
         if (minecraftClientValue > 0 && minecraftClientValue != this.lastWorldId) {
            this.lastWorldId = minecraftClientValue;
            if (minecraftClient.player.getLastAttacker() instanceof PlayerEntity local && local != minecraftClient.player && !local.isSpectator()) {
               this.triggerRequest();
            }
         }

         int minecraftClientValue2 = minecraftClient.player.getLastAttackTime();
         if (minecraftClientValue2 > 0 && minecraftClientValue2 != this.lastServerId) {
            this.lastServerId = minecraftClientValue2;
            this.triggerRequest();
         }

         if (this.isRequestPending) {
            if (this.triggerCooldown > 0) {
               this.triggerCooldown--;
               return;
            }

            this.isRequestPending = false;
            this.cooldownTicks = 0;
         }

         if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
         } else {
            minecraftClient.getNetworkHandler().sendChatCommand((String)this.modeSetting.getValue() + " " + ((String)this.playerSetting.getValue()).trim());
            int intVal = ((Float)this.minDelaySetting.getValue()).intValue();
            int maxValue = Math.max(intVal, ((Float)this.maxDelaySetting.getValue()).intValue());
            this.cooldownTicks = intVal + (int)(Math.random() * (maxValue - intVal + 1));
         }
      }
   }

   public void triggerRequest() {
      this.isRequestPending = true;
      this.triggerCooldown = 400;
      this.cooldownTicks = 0;
   }

}
