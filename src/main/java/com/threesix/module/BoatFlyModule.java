package com.threesix.module;

import java.util.List;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos.Mutable;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class BoatFlyModule extends ModuleBase {
   public static BoatFlyModule instance;
   public final ClientSetting speedSetting = new ClientSetting("Speed", true);
   public final ClientSetting horizontalSpeedSetting = new ClientSetting("Horizontal Speed", 10.0, 0.0, 50.0);
   public final ClientSetting insideBlockSpeedSetting = new ClientSetting("Horizontal Speed Inside Blocks", 5.0, 0.0, 50.0);
   public final ClientSetting verticalSpeedSetting = new ClientSetting("Vertical Speed", 10.0, 0.0, 20.0);
   public final ClientSetting fallSpeedSetting = new ClientSetting("Fall Speed", 0.1, 0.0, 20.0);
   public final ClientSetting antiFlyKickSetting = new ClientSetting("Anti Fly Kick", true);
   public final ClientSetting delaySetting = new ClientSetting("Delay", 40, 1, 80);
   public int kickDelayTicks;
   public double lastY = Double.MAX_VALUE;
   public boolean didAntiFlyKick;
   public boolean isInsideBlock;

   public BoatFlyModule() {
      super("Boat Fly", ModuleCategory.MISC);
      this.registerSetting(this.speedSetting);
      this.registerSetting(this.horizontalSpeedSetting.withVisibility(() -> {
         return (Boolean)this.speedSetting.getValue();
      }));
      this.registerSetting(this.insideBlockSpeedSetting.withVisibility(() -> {
         return (Boolean)this.speedSetting.getValue();
      }));
      this.registerSetting(this.verticalSpeedSetting);
      this.registerSetting(this.fallSpeedSetting);
      this.registerSetting(this.antiFlyKickSetting);
      this.registerSetting(this.delaySetting.withVisibility(() -> {
         return (Boolean)this.antiFlyKickSetting.getValue();
      }));
      instance = this;
   }

   public static boolean isBoatNoclipActive() {
      return instance != null && instance.isEnabled();
   }

   public AbstractBoatEntity getControllingBoat() {
      try {
         if (minecraftClient.player == null) {
            return null;
         } else if (minecraftClient.player.getVehicle() instanceof AbstractBoatEntity local) {
            return local.getControllingPassenger() != minecraftClient.player ? null : local;
         } else {
            return null;
         }
      } catch (Throwable error) {
         return null;
      }
   }

   @Override
   public void onEnable() {
      this.kickDelayTicks = (Integer)this.delaySetting.getValue();
      this.didAntiFlyKick = false;
      this.lastY = Double.MAX_VALUE;
   }

   @Override
   public void onDisable() {
      try {
         AbstractBoatEntity local = this.getControllingBoat();
         if (local == null && minecraftClient.player != null && minecraftClient.player.getVehicle() instanceof AbstractBoatEntity local2) {
            local = local2;
         }

         if (local != null) {
            local.noClip = false;
         }
      } catch (Throwable error) {
      }
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.getNetworkHandler() != null) {
         AbstractBoatEntity local = this.getControllingBoat();
         if (local != null) {
            this.isInsideBlock = this.checkInsideBlock(local);
            local.noClip = true;
            double posY = local.getVelocity().x;
            double doubleVal = 0.0;
            double posZ = local.getVelocity().z;
            if ((Boolean)this.speedSetting.getValue()) {
               double doubleVal2 = this.isInsideBlock ? (Double)this.insideBlockSpeedSetting.getValue() : (Double)this.horizontalSpeedSetting.getValue();
               Vec3d local2 = this.getDirectionVector(doubleVal2);
               posY = local2.x;
               posZ = local2.z;
            }

            if (minecraftClient.options.jumpKey.isPressed()) {
               doubleVal += (Double)this.verticalSpeedSetting.getValue() / 20.0;
            }

            if (minecraftClient.options.sprintKey.isPressed()) {
               doubleVal -= (Double)this.verticalSpeedSetting.getValue() / 20.0;
            } else {
               doubleVal -= (Double)this.fallSpeedSetting.getValue() / 20.0;
            }

            local.setYaw(minecraftClient.player.getYaw());
            local.setVelocity(new Vec3d(posY, doubleVal, posZ));
            if (this.didAntiFlyKick) {
               this.setBoatVelocity(local, local.getX(), this.lastY, local.getZ());
               this.didAntiFlyKick = false;
            }
         }

         this.kickDelayTicks--;
      }
   }

   public Vec3d getDirectionVector(double doubleVal) {
      try {
         float floatVal = (minecraftClient.options.forwardKey.isPressed() ? 1.0F : 0.0F) - (minecraftClient.options.backKey.isPressed() ? 1.0F : 0.0F);
         float floatVal2 = (minecraftClient.options.rightKey.isPressed() ? 1.0F : 0.0F) - (minecraftClient.options.leftKey.isPressed() ? 1.0F : 0.0F);
         Vec3d minecraftClientValue = minecraftClient.player.getRotationVector();
         Vec3d local = new Vec3d(minecraftClientValue.x, 0.0, minecraftClientValue.z);
         if (local.lengthSquared() < 1.0E-6) {
            local = new Vec3d(0.0, 0.0, 1.0);
         } else {
            local = local.normalize();
         }

         Vec3d local2 = new Vec3d(-local.z, 0.0, local.x);
         Vec3d var6Value = local.multiply(floatVal).add(local2.multiply(floatVal2));
         if (var6Value.lengthSquared() > 1.0) {
            var6Value = var6Value.normalize();
         }

         return var6Value.multiply(doubleVal);
      } catch (Throwable error) {
         return Vec3d.ZERO;
      }
   }

   @Override
   public boolean onPacketReceive(Packet arg) {
      try {
         if (!(arg instanceof VehicleMoveC2SPacket local2)) {
            return false;
         }

         if (!(Boolean)this.antiFlyKickSetting.getValue()) {
            return false;
         }

         AbstractBoatEntity local = this.getControllingBoat();
         if (local == null) {
            return false;
         }

         double doubleVal = local2.position().y;
         if (this.kickDelayTicks <= 0 && !this.didAntiFlyKick && this.isNearGroundY(doubleVal) && !local.isOnGround()) {
            this.setBoatVelocity(local, local.getX(), this.lastY - 0.0313, local.getZ());
            this.didAntiFlyKick = true;
            this.kickDelayTicks = (Integer)this.delaySetting.getValue();
            return true;
         }

         this.lastY = doubleVal;
      } catch (Throwable error) {
      }

      return false;
   }

   public void setBoatVelocity(AbstractBoatEntity arg, double doubleVal, double doubleVal2, double doubleVal3) {
      try {
         ClientPlayNetworkHandler minecraftClientValue = minecraftClient.getNetworkHandler();
         if (minecraftClientValue == null) {
            return;
         }

         minecraftClientValue.sendPacket(new VehicleMoveC2SPacket(new Vec3d(doubleVal, doubleVal2, doubleVal3), arg.getYaw(), arg.getPitch(), arg.isOnGround()));
      } catch (Throwable error) {
      }
   }

   public boolean isNearGroundY(double doubleVal) {
      return doubleVal >= this.lastY ? true : this.lastY - doubleVal < 0.0313;
   }

   public boolean checkInsideBlock(Entity arg) {
      try {
         if (arg == null || minecraftClient.world == null) {
            return false;
         }

         ClientWorld minecraftClientWorld = minecraftClient.world;
         Box var1Value = arg.getBoundingBox().expand(0.0, 0.05, 0.0).stretch(0.5, 0.0, 0.5);
         int class3532Value = MathHelper.floor(var1Value.minX);
         int class3532Value2 = MathHelper.floor(var1Value.minY);
         int class3532Value3 = MathHelper.floor(var1Value.minZ);
         int class3532Value4 = MathHelper.floor(var1Value.maxX);
         int class3532Value5 = MathHelper.floor(var1Value.maxY);
         int class3532Value6 = MathHelper.floor(var1Value.maxZ);
         Mutable local = new Mutable();

         for (int index = class3532Value; index <= class3532Value4; index++) {
            for (int index2 = class3532Value2; index2 <= class3532Value5; index2++) {
               for (int index3 = class3532Value3; index3 <= class3532Value6; index3++) {
                  local.set(index, index2, index3);
                  BlockState var2Value = minecraftClientWorld.getBlockState(local);
                  if (!var2Value.isAir()) {
                     VoxelShape var14Value = var2Value.getCollisionShape(minecraftClientWorld, local, ShapeContext.of(arg));
                     if (!var14Value.isEmpty()
                        && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(var1Value), var14Value.offset(index, index2, index3), BooleanBiFunction.AND)) {
                        return true;
                     }
                  }
               }
            }
         }

         List<Entity> var2Value2 = minecraftClientWorld.getOtherEntities(arg, var1Value);
         if (var2Value2 != null) {
            for (Entity class1297 : var2Value2) {
               if (class1297 != minecraftClient.player
                  && class1297 != arg.getVehicle()
                  && !arg.isConnectedThroughVehicle(class1297)
                  && !class1297.isConnectedThroughVehicle(arg)
                  && !class1297.isSpectator()
                  && class1297.isAlive()
                  && class1297.getBoundingBox().intersects(var1Value)) {
                  return true;
               }
            }
         }
      } catch (Throwable error) {
      }

      return false;
   }

}
