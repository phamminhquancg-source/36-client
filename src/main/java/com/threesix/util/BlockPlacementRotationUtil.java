package com.threesix.util;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.enums.Orientation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Direction.Axis;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.data.BlockRotationConstraint;

public final class BlockPlacementRotationUtil {

   public static BlockRotationConstraint getPlacementConstraint(BlockState arg, ClientPlayerEntity arg2) {

      Block var0Value = arg.getBlock();
      if (arg.contains(Properties.ORIENTATION)) {
         Orientation local = (Orientation)arg.get(Properties.ORIENTATION);
         Direction var10Value = local.getFacing();
         Direction var10Value2 = local.getRotation();
         if (var10Value.getAxis() == Axis.Y) {
            float floatVal = var10Value == Direction.UP ? 90.0F : -90.0F;
            BlockRotationConstraint local2 = constraintForFacing(var10Value2.getOpposite(), false);
            return new BlockRotationConstraint(local2.yaw, floatVal);
         } else {
            return constraintForFacing(var10Value.getOpposite(), true);
         }
      } else {
         if (arg.contains(Properties.AXIS)) {
            Axis local3 = (Axis)arg.get(Properties.AXIS);
            if (local3 == Axis.Y) {
               return new BlockRotationConstraint(null, 90.0F);
            }

            if (local3 == Axis.X) {
               if (arg2 != null) {
                  float class3532Value = MathHelper.wrapDegrees(arg2.getYaw());
                  float floatVal2 = class3532Value < 0.0F ? -90.0F : 90.0F;
                  return new BlockRotationConstraint(floatVal2, null);
               }

               return new BlockRotationConstraint(90.0F, null);
            }

            if (local3 == Axis.Z) {
               if (arg2 != null) {
                  float class3532Value2 = MathHelper.wrapDegrees(arg2.getYaw());
                  float absValue = Math.abs(class3532Value2) > 90.0F ? 180.0F : 0.0F;
                  return new BlockRotationConstraint(absValue, null);
               }

               return new BlockRotationConstraint(0.0F, null);
            }
         }

         if (arg.contains(Properties.HORIZONTAL_FACING)) {
            Direction local4 = (Direction)arg.get(Properties.HORIZONTAL_FACING);
            return !(var0Value instanceof StairsBlock)
                  && !(var0Value instanceof TrapdoorBlock)
                  && !(var0Value instanceof FenceGateBlock)
                  && !(var0Value instanceof AnvilBlock)
                  && !(var0Value instanceof DoorBlock)
                  && !(var0Value instanceof HopperBlock)
               ? constraintForFacing(local4.getOpposite(), false)
               : constraintForFacing(local4, false);
         }

         if (arg.contains(Properties.FACING)) {
            Direction local5 = (Direction)arg.get(Properties.FACING);
            if (var0Value instanceof ObserverBlock) {
               return constraintForFacing(local5, true);
            }

            if (!(var0Value instanceof HopperBlock)) {
               return constraintForFacing(local5.getOpposite(), true);
            }

            if (local5 != Direction.DOWN) {
               return constraintForFacing(local5, false);
            }
         }

         return null;
      }
   }

   private static BlockRotationConstraint constraintForFacing(Direction arg, boolean flag) {
      switch (arg) {
         case UP:
            return new BlockRotationConstraint(null, -90.0F);
         case DOWN:
            return new BlockRotationConstraint(null, 90.0F);
         case SOUTH:
            return new BlockRotationConstraint(0.0F, flag ? 0.0F : null);
         case WEST:
            return new BlockRotationConstraint(90.0F, flag ? 0.0F : null);
         case NORTH:
            return new BlockRotationConstraint(180.0F, flag ? 0.0F : null);
         case EAST:
            return new BlockRotationConstraint(-90.0F, flag ? 0.0F : null);
         default:
            return null;
      }
   }

   public static boolean canSatisfy(ClientPlayerEntity arg, BlockRotationConstraint blockRotationConstraint) {

      if (blockRotationConstraint == null) {
         return true;
      }

      if (blockRotationConstraint.yaw != null) {
         Direction local = yawToFacing(blockRotationConstraint.yaw);
         if (local != null && arg.getHorizontalFacing() != local) {
            return false;
         }
      }

      if (blockRotationConstraint.pitch == null) {
         return true;
      } else {
         float var0Value = arg.getPitch();
         if (blockRotationConstraint.pitch == -90.0F) {
            return !(var0Value > -45.0F);
         } else {
            return blockRotationConstraint.pitch == 90.0F ? !(var0Value < 45.0F) : !(var0Value < -45.0F) && !(var0Value > 45.0F);
         }
      }
   }

   private static Direction yawToFacing(float floatVal) {

      float class3532Value = MathHelper.wrapDegrees(floatVal);
      if (class3532Value >= -45.0F && class3532Value < 45.0F) {
         return Direction.SOUTH;
      } else if (class3532Value >= 45.0F && class3532Value < 135.0F) {
         return Direction.WEST;
      } else {
         return class3532Value >= -135.0F && class3532Value < -45.0F ? Direction.EAST : Direction.NORTH;
      }
   }

   public static float snapYaw(float floatVal, float floatVal2, MinecraftClient arg) {
      double doubleVal = (Double)arg.options.getMouseSensitivity().getValue();
      float floatVal3 = (float)(doubleVal * 0.6F + 0.2F);
      float var5Var5Var58Value = floatVal3 * floatVal3 * floatVal3 * 8.0F;
      float var60Value = var5Var5Var58Value * 0.15F;
      float var0Var1Value = floatVal - floatVal2;
      var0Var1Value = MathHelper.wrapDegrees(var0Var1Value);
      int roundValue = Math.round(var0Var1Value / var60Value);
      return floatVal2 + roundValue * var60Value;
   }

   public static float snapYawClamped(float floatVal, float floatVal2, MinecraftClient arg) {
      double doubleVal = (Double)arg.options.getMouseSensitivity().getValue();
      float floatVal3 = (float)(doubleVal * 0.6F + 0.2F);
      float var5Var5Var58Value = floatVal3 * floatVal3 * floatVal3 * 8.0F;
      float var60Value = var5Var5Var58Value * 0.15F;
      float var0Var1Value = floatVal - floatVal2;
      int roundValue = Math.round(var0Var1Value / var60Value);
      float var1Var9Var7Value = floatVal2 + roundValue * var60Value;
      return MathHelper.clamp(var1Var9Var7Value, -90.0F, 90.0F);
   }

}
