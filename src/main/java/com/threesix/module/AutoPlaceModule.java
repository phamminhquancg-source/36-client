package com.threesix.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.hit.BlockHitResult;
import com.threesix.util.BlockPlacementRotationUtil;
import com.threesix.data.BlockRotationConstraint;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.BaseFinderProbeData;
import com.threesix.data.ModuleCategory;
import com.threesix.data.BaseFinderSearchState;
import com.threesix.util.LitematicaCompat;
import com.threesix.data.ClientStateFlags;
import com.threesix.setting.ItemSelectSetting;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AutoPlaceModule extends ModuleBase {
   public static final ClientSetting autoPickSetting = new ClientSetting("Auto Pick", true);
   public static final ClientSetting limitToSchematicSetting = new ClientSetting("Limit To Schematic", true);
   public static final ClientSetting preventGuiSetting = new ClientSetting("Prevent Gui", true);
   public static final ClientSetting adjustRedstoneSetting = new ClientSetting("Adjust Redstone", true);
   public static final ClientSetting pauseMovementSetting = new ClientSetting("Pause Movement", true);
   public static final ClientSetting lockLayerSetting = new ClientSetting("Lock Layer", false);
   public static final ClientSetting lockToSchematicSetting = new ClientSetting("Lock To Schematic", false);
   public static final ClientSetting placeDelaySetting = new ClientSetting("Place Delay Ms", 35, 0, 500);
   public static final ClientSetting safetyModeSetting = new ClientSetting("Safety Mode", true);
   public static final ClientSetting delayVarianceSetting = new ClientSetting("Delay Variance", 25, 0, 200);
   private final ClientSetting placeWhileMovingSetting = new ClientSetting("Place While Moving", false);
   private final ClientSetting allowManualPlaceSetting = new ClientSetting("Allow Manual Place", false);
   public final ItemSelectSetting manualWhitelist = new ItemSelectSetting("Manual Whitelist");
   private boolean isManualPlaceActive = false;
   private int lastSelectedSlot = -1;
   public static volatile boolean isManualPlaceConsumed = false;
   private long manualPlaceGraceUntil = 0L;
   private static final int scanSteps = 2;
   private static final int rotationSteps = 2;
   private final Random random = new Random();
   private static AutoPlaceModule instance;
   private long nextPlaceAt = 0L;
   private BlockPos lastPlacedPos = null;
   private long lastPlaceAt = 0L;

   public AutoPlaceModule() {
      super("AutoPlace", ModuleCategory.DONUT);
      this.manualWhitelist.withVisibility(() -> {
         return (Boolean)this.allowManualPlaceSetting.getValue();
      });
      this.registerSetting(autoPickSetting);
      this.registerSetting(limitToSchematicSetting);
      this.registerSetting(preventGuiSetting);
      this.registerSetting(adjustRedstoneSetting);
      this.registerSetting(pauseMovementSetting);
      this.registerSetting(lockLayerSetting);
      this.registerSetting(lockToSchematicSetting);
      this.registerSetting(placeDelaySetting);
      this.registerSetting(safetyModeSetting);
      this.registerSetting(delayVarianceSetting);
      this.registerSetting(this.placeWhileMovingSetting);
      this.registerSetting(this.allowManualPlaceSetting);
      this.registerSetting(this.manualWhitelist);
      instance = this;
   }

   public static AutoPlaceModule getInstance() {
      return instance;
   }

   public boolean isAutoPick() {
      return (Boolean)autoPickSetting.getValue();
   }

   public boolean isLimitToSchematic() {
      return (Boolean)limitToSchematicSetting.getValue();
   }

   public boolean isPreventGuiEnabled() {
      return (Boolean)preventGuiSetting.getValue();
   }

   public boolean isAdjustRedstoneEnabled() {

      return (Boolean)adjustRedstoneSetting.getValue();
   }

   public boolean isPauseMovementEnabled() {
      return (Boolean)pauseMovementSetting.getValue();
   }

   public boolean isLockLayerEnabled() {
      return (Boolean)lockLayerSetting.getValue();
   }

   public boolean isLockToSchematic() {
      return (Boolean)lockToSchematicSetting.getValue();
   }

   public int getPlaceDelayMs() {
      return (Integer)placeDelaySetting.getValue();
   }

   public boolean isItemAllowed(Item arg) {
      return true;
   }

   public boolean isSafetyModeEnabled() {

      return (Boolean)safetyModeSetting.getValue();
   }

   public int getDelayVariance() {

      return (Integer)delayVarianceSetting.getValue();
   }

   public int rollDelayVariance() {
      int intVal = this.getDelayVariance();
      return intVal <= 0 ? 0 : this.random.nextInt(intVal + 1) - intVal / 2;
   }

   public boolean isPlaceWhileMoving() {
      return (Boolean)this.placeWhileMovingSetting.getValue();
   }

   public boolean isAllowManualPlace() {
      return (Boolean)this.allowManualPlaceSetting.getValue();
   }

   public boolean hasManualPlaceActive() {
      return this.isManualPlaceActive;
   }

   public static void requestAutoPick() {
      isManualPlaceConsumed = true;
      if (instance != null) {
         instance.manualPlaceGraceUntil = System.currentTimeMillis() + 300L;
      }
   }

   private void pollManualPlace() {
      if (minecraftClient.player != null) {
         int minecraftClientValue = minecraftClient.player.getInventory().getSelectedSlot();
         if (minecraftClientValue != this.lastSelectedSlot) {
            if (System.currentTimeMillis() > this.manualPlaceGraceUntil) {
               isManualPlaceConsumed = false;
            }

            boolean isManualPlaceConsumedSnapshot = isManualPlaceConsumed;
            isManualPlaceConsumed = false;
            if (!isManualPlaceConsumedSnapshot && this.isAllowManualPlace() && this.manualWhitelist != null) {
               try {
                  ItemStack minecraftClientValue2 = minecraftClient.player.getInventory().getStack(minecraftClientValue);
                  Item local = minecraftClientValue2 != null ? minecraftClientValue2.getItem() : null;
                  if (local != null && !((Set)this.manualWhitelist.getValue()).isEmpty() && this.manualWhitelist.isSelected3(local)) {
                     this.isManualPlaceActive = true;
                  } else {
                     this.isManualPlaceActive = false;
                  }
               } catch (Throwable error) {
                  this.isManualPlaceActive = false;
               }
            } else if (!isManualPlaceConsumedSnapshot) {
               this.isManualPlaceActive = false;
            }

            this.lastSelectedSlot = minecraftClientValue;
         } else if (System.currentTimeMillis() > this.manualPlaceGraceUntil) {
            isManualPlaceConsumed = false;
         }
      }
   }

   @Override
   public void onEnable() {
      this.nextPlaceAt = 0L;
      this.lastPlacedPos = null;
      this.lastPlaceAt = 0L;
      this.isManualPlaceActive = false;
      this.lastSelectedSlot = minecraftClient.player != null ? minecraftClient.player.getInventory().getSelectedSlot() : -1;
      this.manualPlaceGraceUntil = 0L;
      isManualPlaceConsumed = false;
      if (LitematicaCompat.isLitematicaPresent()) {
         ClientStateFlags.setLitematicaPaused(true);
      }
   }

   @Override
   public void onDisable() {
      this.nextPlaceAt = 0L;
      this.lastPlacedPos = null;
      this.lastPlaceAt = 0L;
      this.isManualPlaceActive = false;
      this.lastSelectedSlot = -1;
      this.manualPlaceGraceUntil = 0L;
      isManualPlaceConsumed = false;
      ClientStateFlags.setLitematicaPaused(false);
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         if (LitematicaCompat.isLitematicaPresent()) {
            this.pollManualPlace();
            if (!this.isManualPlaceActive) {
               this.tickSchematicPipeline();
               if (this.isPlaceWhileMoving()) {
                  this.tickPlaceWhileMoving();
               }
            }
         }
      }
   }

   private void tickSchematicPipeline() {
      if (this.isAdjustRedstoneEnabled() && !BaseFinderSearchState.pendingProbes.isEmpty()) {
         long systemValue = System.currentTimeMillis();
         Iterator baseFinderSearchStateValue = BaseFinderSearchState.pendingProbes.iterator();
         byte byteVal = 0;

         while (baseFinderSearchStateValue.hasNext()) {
            BaseFinderProbeData local = (BaseFinderProbeData)baseFinderSearchStateValue.next();
            if (systemValue - local.createdAt > 2000L) {
               baseFinderSearchStateValue.remove();
            } else {
               World litematicaCompatValue = LitematicaCompat.getSchematicWorld();
               if (litematicaCompatValue == null) {
                  baseFinderSearchStateValue.remove();
               } else {
                  if (local.remainingRotations == -1 && systemValue - local.createdAt > 50L) {
                     BlockState minecraftClientValue = minecraftClient.world.getBlockState(local.probePos);
                     BlockState var6Value = litematicaCompatValue.getBlockState(local.probePos);
                     if (minecraftClientValue.getBlock() instanceof RepeaterBlock && var6Value.getBlock() instanceof RepeaterBlock) {
                        int intVal = (Integer)minecraftClientValue.get(Properties.DELAY);
                        int intVal2 = (Integer)var6Value.get(Properties.DELAY);
                        local.remainingRotations = (intVal2 - intVal + 4) % 4;
                     } else if (minecraftClientValue.getBlock() instanceof ComparatorBlock && var6Value.getBlock() instanceof ComparatorBlock) {
                        ComparatorMode local2 = (ComparatorMode)minecraftClientValue.get(Properties.COMPARATOR_MODE);
                        ComparatorMode local3 = (ComparatorMode)var6Value.get(Properties.COMPARATOR_MODE);
                        local.remainingRotations = local2 != local3 ? 1 : 0;
                     } else if (!minecraftClientValue.isAir()) {
                        baseFinderSearchStateValue.remove();
                        continue;
                     }
                  }

                  if (local.remainingRotations > 0) {
                     if (byteVal == 0 && minecraftClient.interactionManager != null) {
                        BlockHitResult local4 = new BlockHitResult(
                           new Vec3d(local.probePos.getX() + 0.5, local.probePos.getY() + 0.5, local.probePos.getZ() + 0.5),
                           Direction.UP,
                           local.probePos,
                           false
                        );
                        BaseFinderSearchState.isProbeInteracting = true;

                        try {
                           minecraftClient.interactionManager.interactBlock(minecraftClient.player, local.hand, local4);
                           local.remainingRotations--;
                        } finally {
                           BaseFinderSearchState.isProbeInteracting = false;
                        }

                     }

                     if (local.remainingRotations == 0) {
                        baseFinderSearchStateValue.remove();
                     }
                  } else if (local.remainingRotations == 0) {
                     baseFinderSearchStateValue.remove();
                  }
               }
            }
         }
      }

      if (BaseFinderSearchState.isSchematicPlacing) {
         BaseFinderSearchState.placementTicks++;
         if (BaseFinderSearchState.isRotationReady || BaseFinderSearchState.placementTicks >= 2) {
            BaseFinderSearchState.isRotationReady = false;
            BaseFinderSearchState.placementTicks = 0;
            float minecraftClientValue2 = minecraftClient.player.getYaw();
            float minecraftClientValue3 = minecraftClient.player.getPitch();
            minecraftClient.player.setYaw(BaseFinderSearchState.targetYaw);
            minecraftClient.player.setPitch(BaseFinderSearchState.targetPitch);
            BaseFinderSearchState.isRotating = true;
            if (BaseFinderSearchState.pendingHand != null && BaseFinderSearchState.pendingHitResult != null && minecraftClient.interactionManager != null) {
               boolean flag = this.isPreventGuiEnabled() && !minecraftClient.player.isSneaking();
               PlayerInput nullSnapshot = null;
               if (flag && minecraftClient.getNetworkHandler() != null && minecraftClient.player.input != null) {
                  PlayerInput minecraftClientValue4 = minecraftClient.player.input.playerInput;
                  if (minecraftClientValue4 != null) {
                     nullSnapshot = minecraftClientValue4;
                     PlayerInput local5 = new PlayerInput(
                        minecraftClientValue4.forward(), minecraftClientValue4.backward(), minecraftClientValue4.left(), minecraftClientValue4.right(), minecraftClientValue4.jump(), true, minecraftClientValue4.sprint()
                     );
                     minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local5));
                  }
               }

               minecraftClient.interactionManager.interactBlock(minecraftClient.player, BaseFinderSearchState.pendingHand, BaseFinderSearchState.pendingHitResult);
               BaseFinderSearchState.nextActionTimeMillis = System.currentTimeMillis() + this.rollDelayVariance();
               if (flag && nullSnapshot != null && minecraftClient.getNetworkHandler() != null) {
                  minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(nullSnapshot));
               }

               try {
                  if (BaseFinderSearchState.pendingPos != null && this.isAdjustRedstoneEnabled()) {
                     BaseFinderSearchState.pendingProbes.add(new BaseFinderProbeData(BaseFinderSearchState.pendingPos, BaseFinderSearchState.pendingHand, BaseFinderSearchState.pendingHitResult));
                  }
               } catch (Exception error) {
               }
            }

            BaseFinderSearchState.isRotating = false;
            minecraftClient.player.setYaw(minecraftClientValue2);
            minecraftClient.player.setPitch(minecraftClientValue3);
            BaseFinderSearchState.isSchematicPlacing = false;
            BaseFinderSearchState.hasPendingRotation = false;
         }
      }
   }

   private void tickPlaceWhileMoving() {
      if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.interactionManager != null) {
         if (!minecraftClient.options.useKey.isPressed()) {
            this.lastPlacedPos = null;
         } else if (this.canPlaceNow()) {
            if (!BaseFinderSearchState.isSchematicPlacing && !BaseFinderSearchState.isRotating && !BaseFinderSearchState.isProbeInteracting) {
               long systemValue = System.currentTimeMillis();
               if (systemValue >= this.nextPlaceAt) {
                  World litematicaCompatValue = LitematicaCompat.getSchematicWorld();
                  if (litematicaCompatValue != null) {
                     List<BlockPos> local = this.collectPlaceablePositions(litematicaCompatValue);
                     if (!local.isEmpty()) {
                        Collections.shuffle(local, this.random);

                        for (BlockPos class2338 : local) {
                           BlockHitResult local2 = this.createPlacementFor(class2338);
                           if (local2 != null) {
                              if (!BaseFinderSearchState.isSchematicPlacing && !BaseFinderSearchState.isRotating) {
                                 try {
                                    BlockState var3Value = litematicaCompatValue.getBlockState(class2338);
                                    BlockRotationConstraint blockPlacementRotationUtil = BlockPlacementRotationUtil.getPlacementConstraint(var3Value, minecraftClient.player);
                                    if (blockPlacementRotationUtil != null && !BlockPlacementRotationUtil.canSatisfy(minecraftClient.player, blockPlacementRotationUtil)) {
                                       float floatVal = blockPlacementRotationUtil.yaw != null ? blockPlacementRotationUtil.yaw : minecraftClient.player.getYaw();
                                       float floatVal2 = blockPlacementRotationUtil.pitch != null ? blockPlacementRotationUtil.pitch : minecraftClient.player.getPitch();
                                       float blockPlacementRotationUtil2 = BlockPlacementRotationUtil.snapYaw(floatVal, minecraftClient.player.getYaw(), MinecraftClient.getInstance());
                                       float blockPlacementRotationUtil3 = BlockPlacementRotationUtil.snapYawClamped(floatVal2, minecraftClient.player.getPitch(), MinecraftClient.getInstance());
                                       if (getInstance() == null || getInstance().isItemAllowed(minecraftClient.player.getMainHandStack().getItem())) {
                                          BaseFinderSearchState.isSchematicPlacing = true;
                                          BaseFinderSearchState.targetYaw = blockPlacementRotationUtil2;
                                          BaseFinderSearchState.targetPitch = blockPlacementRotationUtil3;
                                          BaseFinderSearchState.pendingHand = Hand.MAIN_HAND;
                                          BaseFinderSearchState.pendingHitResult = local2;
                                          BaseFinderSearchState.pendingPos = class2338;
                                          BaseFinderSearchState.sellSpoofYaw = blockPlacementRotationUtil2;
                                          BaseFinderSearchState.sellSpoofPitch = blockPlacementRotationUtil3;
                                          BaseFinderSearchState.hasPendingRotation = true;
                                          return;
                                       }
                                    }
                                 } catch (Throwable error) {
                                 }

                                 minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, local2);
                                 this.lastPlacedPos = class2338;
                                 int maxValue = Math.max(10, this.getPlaceDelayMs());
                                 int maxValue2 = Math.max(0, this.getDelayVariance());
                                 int var15ThisValue;
                                 if (maxValue2 > 0) {
                                    var15ThisValue = maxValue + this.random.nextInt(maxValue2 + 1);
                                 } else {
                                    var15ThisValue = maxValue;
                                 }

                                 this.nextPlaceAt = System.currentTimeMillis() + var15ThisValue;
                                 return;
                              }

                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private List<BlockPos> collectPlaceablePositions(World arg) {
      ArrayList<BlockPos> arrayListInst = new ArrayList<>();
      BlockPos minecraftClientValue = minecraftClient.player.getBlockPos();
      Direction local = this.getLockLayerFacing();
      if (local == null) {
         local = minecraftClient.player.getHorizontalFacing();
      }

      Direction var4Value = local.rotateYClockwise();

      for (int index = 1; index <= 2; index++) {
         for (int index2 = -2; index2 <= 2; index2++) {
            if (Math.abs(index2) <= 2) {
               int var3Value = minecraftClientValue.getX() + local.getOffsetX() * index + var4Value.getOffsetX() * index2;
               int var3Value2 = minecraftClientValue.getZ() + local.getOffsetZ() * index + var4Value.getOffsetZ() * index2;

               for (int index3 = -2; index3 <= 3; index3++) {
                  int var3Value3 = minecraftClientValue.getY() + index3;
                  Object litematicaCompatValue = LitematicaCompat.getRenderLayerRange();
                  if (litematicaCompatValue != null) {
                     BlockPos local2 = new BlockPos(var3Value, var3Value3, var3Value2);
                     if (!LitematicaCompat.isPositionInRange(litematicaCompatValue, local2)) {
                        continue;
                     }
                  } else if (var3Value3 != minecraftClientValue.getY()) {
                     continue;
                  }

                  BlockPos local3 = new BlockPos(var3Value, var3Value3, var3Value2);
                  BlockState var1Value = arg.getBlockState(local3);
                  if (var1Value != null && !var1Value.isAir()) {
                     BlockState minecraftClientValue2 = minecraftClient.world.getBlockState(local3);
                     if (minecraftClientValue2.isAir() || minecraftClientValue2.isReplaceable()) {
                        if (this.isLockLayerEnabled()) {
                           Object litematicaCompatValue2 = LitematicaCompat.getRenderLayerRange();
                           if (litematicaCompatValue2 != null && !LitematicaCompat.isPositionInRange(litematicaCompatValue2, local3)) {
                              continue;
                           }
                        }

                        arrayListInst.add(local3);
                     }
                  }
               }
            }
         }
      }

      return arrayListInst;
   }

   private BlockHitResult createPlacementFor(BlockPos arg) {
      if (minecraftClient.world == null) {
         return null;
      }

      Direction[] local = new Direction[]{
         Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
      };

      for (Direction class2350 : local) {
         BlockPos var1Value = arg.offset(class2350.getOpposite());
         BlockState minecraftClientValue = minecraftClient.world.getBlockState(var1Value);
         if (this.canPlaceAt(var1Value, minecraftClientValue)) {
            Vec3d class243Value = Vec3d.ofCenter(var1Value);
            Vec3d var9Value = class243Value.add(class2350.getOffsetX() * 0.5, class2350.getOffsetY() * 0.5, class2350.getOffsetZ() * 0.5);
            return new BlockHitResult(var9Value, class2350, var1Value, false);
         }
      }

      return null;
   }

   private boolean canPlaceAt(BlockPos arg, BlockState arg2) {
      if (arg2 == null) {
         return false;
      }

      if (arg2.isAir()) {
         return false;
      }

      if (arg2.isReplaceable()) {
         return false;
      }

      try {
         return !arg2.getCollisionShape(minecraftClient.world, arg, ShapeContext.absent()).isEmpty();
      } catch (Throwable error) {
         return true;
      }
   }

   private boolean canPlaceNow() {

      if (minecraftClient.player == null) {
         return false;
      }

      if (minecraftClient.player.input != null) {
         PlayerInput minecraftClientValue = minecraftClient.player.input.playerInput;
         if (minecraftClientValue != null && (minecraftClientValue.forward() || minecraftClientValue.backward() || minecraftClientValue.left() || minecraftClientValue.right())) {
            return true;
         }
      }

      Vec3d minecraftClientValue2 = minecraftClient.player.getVelocity();
      return minecraftClientValue2.horizontalLengthSquared() > 1.0E-4;
   }

   private Direction getLockLayerFacing() {
      if (minecraftClient.player != null && minecraftClient.player.input != null) {
         PlayerInput minecraftClientValue = minecraftClient.player.input.playerInput;
         if (minecraftClientValue == null) {
            return null;
         }

         boolean flag = minecraftClientValue.forward();
         boolean flag2 = minecraftClientValue.backward();
         boolean flag3 = minecraftClientValue.left();
         boolean flag4 = minecraftClientValue.right();
         if (!flag && !flag2 && !flag3 && !flag4) {
            return null;
         }

         Direction minecraftClientValue2 = minecraftClient.player.getHorizontalFacing();
         if (flag && !flag2) {
            if (flag3 && !flag4) {
               return minecraftClientValue2.rotateYCounterclockwise();
            } else {
               return flag4 && !flag3 ? minecraftClientValue2.rotateYClockwise() : minecraftClientValue2;
            }
         } else if (flag2 && !flag) {
            if (flag3 && !flag4) {
               return minecraftClientValue2.rotateYClockwise();
            } else {
               return flag4 && !flag3 ? minecraftClientValue2.rotateYCounterclockwise() : minecraftClientValue2.getOpposite();
            }
         } else if (flag3 && !flag4) {
            return minecraftClientValue2.rotateYCounterclockwise();
         } else {
            return flag4 && !flag3 ? minecraftClientValue2.rotateYClockwise() : minecraftClientValue2;
         }
      } else {
         return null;
      }
   }

}
