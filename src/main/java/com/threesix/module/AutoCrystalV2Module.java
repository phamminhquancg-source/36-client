package com.threesix.module;

import java.util.Comparator;
import net.minecraft.util.Hand;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.ClientSetting;

public final class AutoCrystalV2Module extends KeybindModuleBase {
   public final ClientSetting breakRangeSetting = new ClientSetting("Break Range", 5.0, 1.0, 10.0);
   public final ClientSetting placeRangeSetting = new ClientSetting("Place Range", 5.0, 1.0, 10.0);
   public final ClientSetting breakDelaySetting = new ClientSetting("Break Delay", 0, 0, 20);
   public final ClientSetting placeDelaySetting = new ClientSetting("Place Delay", 2, 0, 20);
   public final ClientSetting autoObsidianSetting = new ClientSetting("Auto Obsidian", false);
   public final ClientSetting switchBackSetting = new ClientSetting("Switch Back", true);
   public final ClientSetting multiTargetSetting = new ClientSetting("Multi Target", false);
   public int breakCooldownTicks = 0;
   public int placeCooldownTicks = 0;
   public int savedHotbarSlot = -1;
   public boolean isAutoModeActive = false;
   public int lastBrokenEntityId = -1;

   public AutoCrystalV2Module() {
      super("Auto Crystal v2", ModuleCategory.COMBAT);
      this.registerSetting(this.breakRangeSetting);
      this.registerSetting(this.placeRangeSetting);
      this.registerSetting(this.breakDelaySetting);
      this.registerSetting(this.placeDelaySetting);
      this.registerSetting(this.autoObsidianSetting);
      this.registerSetting(this.switchBackSetting);
      this.registerSetting(this.multiTargetSetting);
   }

   @Override
   public void onEnable() {

      this.resetState();
      this.isAutoModeActive = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.restoreHotbarSlot();
      this.resetState();
      super.onDisable();
   }

   @Override
   public void onActivationKey() {
      if (this.isEnabled()) {
         this.isAutoModeActive = !this.isAutoModeActive;
         if (!this.isAutoModeActive) {
            this.restoreHotbarSlot();
         }
      }
   }

   @Override
   public void onTick() {

      if (this.isAutoModeActive && minecraftClient.player != null && minecraftClient.world != null && minecraftClient.currentScreen == null) {
         this.tickCooldowns();
         PlayerEntity local = this.findNearestTarget();
         if (local != null) {
            if (this.breakCooldownTicks <= 0) {
               for (EndCrystalEntity class1511 : minecraftClient.world
                  .getEntitiesByClass(
                     EndCrystalEntity.class,
                     new Box(
                           new Vec3d(minecraftClient.player.getX(), minecraftClient.player.getY(), minecraftClient.player.getZ()),
                           new Vec3d(minecraftClient.player.getX(), minecraftClient.player.getY(), minecraftClient.player.getZ())
                        )
                        .expand((Double)this.breakRangeSetting.getValue()),
                     item -> true
                  )) {
                  if (class1511.getId() != this.lastBrokenEntityId) {
                     double sqrtValue = Math.sqrt(
                        Math.pow(minecraftClient.player.getX() - class1511.getX(), 2.0)
                           + Math.pow(minecraftClient.player.getY() - class1511.getY(), 2.0)
                           + Math.pow(minecraftClient.player.getZ() - class1511.getZ(), 2.0)
                     );
                     if (!(sqrtValue > (Double)this.breakRangeSetting.getValue())) {
                        this.attackEntity(class1511);
                        this.lastBrokenEntityId = class1511.getId();
                        this.breakCooldownTicks = (Integer)this.breakDelaySetting.getValue();
                        break;
                     }
                  }
               }
            }

            if (this.placeCooldownTicks <= 0) {
               BlockPos local2 = this.findBestPlacePos(local);
               if (local2 != null) {
                  this.switchToCrystalSlot();
                  this.placeCrystal(local2);
                  this.placeCooldownTicks = (Integer)this.placeDelaySetting.getValue();
               }
            }
         }
      }
   }

   public void tickCooldowns() {
      if (this.breakCooldownTicks > 0) {
         this.breakCooldownTicks--;
      }

      if (this.placeCooldownTicks > 0) {
         this.placeCooldownTicks--;
      }
   }

   public void resetState() {
      this.breakCooldownTicks = 0;
      this.placeCooldownTicks = 0;
      this.lastBrokenEntityId = -1;
   }

   public PlayerEntity findNearestTarget() {

      double maxValue = Math.max((Double)this.breakRangeSetting.getValue(), (Double)this.placeRangeSetting.getValue());
      return minecraftClient.world
         .getPlayers()
         .stream()
         .filter(item -> {
            return item != minecraftClient.player && item.isAlive() && !item.isInvisibleTo(minecraftClient.player);
         })
         .filter(
            item2 -> {
               return Math.sqrt(
                     Math.pow(minecraftClient.player.getX() - item2.getX(), 2.0)
                        + Math.pow(minecraftClient.player.getY() - item2.getY(), 2.0)
                        + Math.pow(minecraftClient.player.getZ() - item2.getZ(), 2.0)
                  )
                  <= maxValue;
            }
         )
         .min(
            Comparator.comparingDouble(
               item -> {
                  return Math.sqrt(
                     Math.pow(minecraftClient.player.getX() - item.getX(), 2.0)
                        + Math.pow(minecraftClient.player.getY() - item.getY(), 2.0)
                        + Math.pow(minecraftClient.player.getZ() - item.getZ(), 2.0)
                  );
               }
            )
         )
         .orElse(null);
   }

   public BlockPos findBestPlacePos(PlayerEntity arg) {
      Vec3d local = new Vec3d(arg.getX(), arg.getY(), arg.getZ());
      double doubleVal = (Double)this.placeRangeSetting.getValue();
      BlockPos nullSnapshot = null;
      double var17Snapshot = Double.MAX_VALUE;

      for (int index = (int)(minecraftClient.player.getX() - doubleVal); index <= minecraftClient.player.getX() + doubleVal; index++) {
         for (int index2 = (int)(minecraftClient.player.getY() - 2.0); index2 <= minecraftClient.player.getY() + 2.0; index2++) {
            for (int index3 = (int)(minecraftClient.player.getZ() - doubleVal); index3 <= minecraftClient.player.getZ() + doubleVal; index3++) {
               BlockPos local2 = new BlockPos(index, index2, index3);
               if (this.isReplaceableBlock(local2)) {
                  BlockPos var11Value = local2.up();
                  if (minecraftClient.world.getBlockState(var11Value).isAir() && minecraftClient.world.getOtherEntities(null, new Box(var11Value)).isEmpty()) {
                     double doubleVal2 = new Vec3d(
                           minecraftClient.player.getX(), minecraftClient.player.getY(), minecraftClient.player.getZ()
                        )
                        .distanceTo(Vec3d.ofCenter(var11Value));
                     if (!(doubleVal2 > doubleVal)) {
                        double localValue = local.distanceTo(Vec3d.ofCenter(var11Value));
                        double var15Var130Value = localValue - doubleVal2 * 0.3;
                        if (var15Var130Value < var17Snapshot) {
                           var17Snapshot = var15Var130Value;
                           nullSnapshot = local2;
                        }
                     }
                  }
               }
            }
         }
      }

      return nullSnapshot;
   }

   public boolean isReplaceableBlock(BlockPos arg) {

      BlockState minecraftClientValue = minecraftClient.world.getBlockState(arg);
      return minecraftClientValue.isOf(Blocks.OBSIDIAN) || minecraftClientValue.isOf(Blocks.BEDROCK);
   }

   public void attackEntity(EndCrystalEntity arg) {
      minecraftClient.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(arg, minecraftClient.player.isSneaking()));
      minecraftClient.player.swingHand(Hand.MAIN_HAND);
   }

   public void placeCrystal(BlockPos arg) {
      BlockPos var1Value = arg.up();
      Vec3d class243Value = Vec3d.ofCenter(var1Value);
      BlockHitResult local = new BlockHitResult(class243Value, Direction.UP, var1Value, false);
      minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, local);
      minecraftClient.player.swingHand(Hand.MAIN_HAND);
   }

   public void switchToCrystalSlot() {
      if (!minecraftClient.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
         int var2Snapshot = -1;
         for (int index = 0; index < 9; index++) {
            if (minecraftClient.player.getInventory().getStack(index).isOf(Items.END_CRYSTAL)) {
               var2Snapshot = index;
               break;
            }
         }

         if (var2Snapshot != -1) {
            if ((Boolean)this.switchBackSetting.getValue() && this.savedHotbarSlot == -1) {
               this.savedHotbarSlot = minecraftClient.player.getInventory().getSelectedSlot();
            }

            minecraftClient.player.getInventory().setSelectedSlot(var2Snapshot);
         }
      }
   }

   public void restoreHotbarSlot() {
      if ((Boolean)this.switchBackSetting.getValue() && this.savedHotbarSlot != -1 && minecraftClient.player != null) {
         minecraftClient.player.getInventory().setSelectedSlot(this.savedHotbarSlot);
      }

      this.savedHotbarSlot = -1;
   }

}
