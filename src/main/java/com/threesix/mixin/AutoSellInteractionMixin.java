package com.threesix.mixin;

import net.minecraft.util.PlayerInput;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.BellBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.block.ChiseledBookshelfBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.util.BlockPlacementRotationUtil;
import com.threesix.data.BlockRotationConstraint;
import com.threesix.data.BaseFinderProbeData;
import com.threesix.data.BaseFinderSearchState;
import com.threesix.util.LitematicaCompat;
import com.threesix.module.AutoPlaceModule;

@Mixin(ClientPlayerInteractionManager.class)
public class AutoSellInteractionMixin {
   private boolean spoofedSneak = false;
   private static long lastInvSwapTime = 0L;

   private boolean shouldSneakPlaceAgainst(ClientPlayerEntity arg, BlockHitResult arg2) {
      BlockState var1Value = arg.getEntityWorld().getBlockState(arg2.getBlockPos());
      Block var3Value = var1Value.getBlock();
      return var3Value instanceof BlockWithEntity
         || var3Value instanceof CraftingTableBlock
         || var3Value instanceof AnvilBlock
         || var3Value instanceof LoomBlock
         || var3Value instanceof CartographyTableBlock
         || var3Value instanceof GrindstoneBlock
         || var3Value instanceof StonecutterBlock
         || var3Value instanceof SmithingTableBlock
         || var3Value instanceof LecternBlock
         || var3Value instanceof EnchantingTableBlock
         || var3Value instanceof LeverBlock
         || var3Value instanceof ButtonBlock
         || var3Value instanceof FenceGateBlock
         || var3Value instanceof DoorBlock
         || var3Value instanceof TrapdoorBlock
         || var3Value instanceof NoteBlock
         || var3Value instanceof JukeboxBlock
         || var3Value instanceof RepeaterBlock
         || var3Value instanceof ComparatorBlock
         || var3Value instanceof BedBlock
         || var3Value instanceof BellBlock
         || var3Value instanceof ComposterBlock
         || var3Value instanceof ChiseledBookshelfBlock
         || var3Value instanceof FlowerPotBlock
         || var3Value instanceof CakeBlock;
   }

   @Inject(
      method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onInteractBlockHead(ClientPlayerEntity local2, Hand local3, BlockHitResult local4, CallbackInfoReturnable local5) {
      if (local2 != null) {
         AutoPlaceModule autoPlaceModuleValue = AutoPlaceModule.getInstance();
         boolean flag = autoPlaceModuleValue != null && autoPlaceModuleValue.isEnabled();
         if (flag) {
            if (autoPlaceModuleValue.isAdjustRedstoneEnabled() && !BaseFinderSearchState.isRotating && !BaseFinderSearchState.isProbeInteracting) {
               Block class310Value = MinecraftClient.getInstance().world.getBlockState(local4.getBlockPos()).getBlock();
               if (class310Value instanceof RepeaterBlock || class310Value instanceof ComparatorBlock) {
                  local5.setReturnValue(ActionResult.FAIL);
                  return;
               }
            }

            if (autoPlaceModuleValue.isPreventGuiEnabled() && !local2.isSneaking() && this.shouldSneakPlaceAgainst(local2, local4)) {
               this.spoofedSneak = true;
               local2.networkHandler.sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, true, false)));
            }

            if (!BaseFinderSearchState.isRotating) {
               if (BaseFinderSearchState.isSchematicPlacing) {
                  local5.setReturnValue(ActionResult.FAIL);
               } else if (System.currentTimeMillis() - BaseFinderSearchState.nextActionTimeMillis < autoPlaceModuleValue.getPlaceDelayMs()) {
                  local5.setReturnValue(ActionResult.FAIL);
               } else {
                  World litematicaCompatValue = LitematicaCompat.getSchematicWorld();
                  if (litematicaCompatValue != null) {
                     ItemStack var1Value = local2.getStackInHand(local3);
                     BlockPos var19Value;
                     if (!(var1Value.getItem() instanceof BucketItem) && var1Value.getItem() != Items.POWDER_SNOW_BUCKET) {
                        ItemPlacementContext local = new ItemPlacementContext(local2, local3, var1Value, local4);
                        var19Value = local.getBlockPos();
                     } else {
                        BlockState class310Value2 = MinecraftClient.getInstance().world.getBlockState(local4.getBlockPos());
                        if (class310Value2.isReplaceable()) {
                           var19Value = local4.getBlockPos();
                        } else {
                           var19Value = local4.getBlockPos().offset(local4.getSide());
                        }
                     }

                     if (autoPlaceModuleValue.isLockLayerEnabled()) {
                        Object litematicaCompatValue2 = LitematicaCompat.getRenderLayerRange();
                        if (litematicaCompatValue2 != null && !LitematicaCompat.isPositionInRange(litematicaCompatValue2, var19Value)) {
                           local5.setReturnValue(ActionResult.FAIL);
                           return;
                        }
                     }

                     BlockState var18Value = litematicaCompatValue.getBlockState(var19Value);
                     if (!autoPlaceModuleValue.isLimitToSchematic() && !autoPlaceModuleValue.isLockToSchematic() || var18Value != null && !var18Value.isAir()) {
                        if (var18Value != null && !var18Value.isAir()) {
                           Item var21Value = var18Value.getBlock().asItem();
                           if (var18Value.getBlock() == Blocks.WATER) {
                              var21Value = Items.WATER_BUCKET;
                           } else if (var18Value.getBlock() == Blocks.LAVA) {
                              var21Value = Items.LAVA_BUCKET;
                           } else if (var18Value.getBlock() == Blocks.POWDER_SNOW) {
                              var21Value = Items.POWDER_SNOW_BUCKET;
                           }

                           if (autoPlaceModuleValue.isAutoPick() && var1Value.getItem() != var21Value && local3 == Hand.MAIN_HAND) {

                              int var13Snapshot = -1;

                              for (int index = 0; index < 36; index++) {
                                 if (local2.getInventory().getStack(index).getItem() == var21Value) {
                                    var13Snapshot = index;
                                    break;
                                 }
                              }

                              if (var13Snapshot != -1) {
                                 AutoPlaceModule.requestAutoPick();
                                 if (var13Snapshot >= 9) {
                                    int intVal = ((PlayerInventoryMixinAccessor)local2.getInventory()).getSelectedSlot();
                                    MinecraftClient.getInstance().interactionManager.clickSlot(local2.playerScreenHandler.syncId, var13Snapshot, intVal, SlotActionType.SWAP, local2);
                                    local5.setReturnValue(ActionResult.FAIL);
                                    return;
                                 }

                                 ((PlayerInventoryMixinAccessor)local2.getInventory()).setSelectedSlot(var13Snapshot);
                                 MinecraftClient.getInstance().getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(var13Snapshot));
                              }
                           }

                           ItemStack var1Value2 = local2.getStackInHand(local3);
                           if (!var1Value2.isEmpty()) {
                              if (var1Value2.getItem() instanceof BlockItem || var1Value2.getItem() instanceof BucketItem) {
                                 if (var1Value2.getItem() != var21Value) {
                                    local5.setReturnValue(ActionResult.FAIL);
                                 } else if (!autoPlaceModuleValue.isItemAllowed(var1Value2.getItem())) {
                                    this.queueAdjustmentIfRequired(var19Value, local3, local4, var18Value, autoPlaceModuleValue);
                                 } else {
                                    BlockRotationConstraint blockPlacementRotationUtil = BlockPlacementRotationUtil.getPlacementConstraint(var18Value, local2);
                                    if (blockPlacementRotationUtil != null) {
                                       if (BlockPlacementRotationUtil.canSatisfy(local2, blockPlacementRotationUtil)) {
                                          this.queueAdjustmentIfRequired(var19Value, local3, local4, var18Value, autoPlaceModuleValue);
                                          return;
                                       }

                                       float floatVal = blockPlacementRotationUtil.pitch != null ? blockPlacementRotationUtil.pitch : local2.getYaw();
                                       float floatVal2 = blockPlacementRotationUtil.yaw != null ? blockPlacementRotationUtil.yaw : local2.getPitch();
                                       float blockPlacementRotationUtil2 = BlockPlacementRotationUtil.snapYaw(floatVal, local2.getYaw(), MinecraftClient.getInstance());
                                       float blockPlacementRotationUtil3 = BlockPlacementRotationUtil.snapYawClamped(floatVal2, local2.getPitch(), MinecraftClient.getInstance());
                                       if (BaseFinderSearchState.isSchematicPlacing) {
                                          local5.setReturnValue(ActionResult.FAIL);
                                          return;
                                       }

                                       BaseFinderSearchState.isSchematicPlacing = true;
                                       BaseFinderSearchState.sellSpoofYaw = blockPlacementRotationUtil2;
                                       BaseFinderSearchState.sellSpoofPitch = blockPlacementRotationUtil3;
                                       BaseFinderSearchState.pendingHand = local3;
                                       BaseFinderSearchState.pendingHitResult = local4;
                                       BaseFinderSearchState.pendingPos = var19Value;
                                       BaseFinderSearchState.targetYaw = blockPlacementRotationUtil2;
                                       BaseFinderSearchState.targetPitch = blockPlacementRotationUtil3;
                                       BaseFinderSearchState.hasPendingRotation = true;
                                       local5.setReturnValue(ActionResult.FAIL);
                                    } else {
                                       this.queueAdjustmentIfRequired(var19Value, local3, local4, var18Value, autoPlaceModuleValue);
                                    }
                                 }
                              }
                           }
                        }
                     } else if (var1Value.getItem() instanceof BlockItem || var1Value.getItem() instanceof BucketItem) {
                        local5.setReturnValue(ActionResult.FAIL);
                     }
                  }
               }
            }
         }
      }
   }

   @Inject(method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"))
   private void onInteractBlockReturn(ClientPlayerEntity arg, Hand arg2, BlockHitResult arg3, CallbackInfoReturnable callbackInfoReturnable) {
      if (this.spoofedSneak) {
         this.spoofedSneak = false;
         if (arg != null && arg.networkHandler != null && !arg.isSneaking()) {
            arg.networkHandler.sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
         }
      }
   }

   private void queueAdjustmentIfRequired(BlockPos arg, Hand arg2, BlockHitResult arg3, BlockState arg4, AutoPlaceModule autoPlaceModule) {
      if (autoPlaceModule != null) {
         if (autoPlaceModule.isEnabled() && autoPlaceModule.isAdjustRedstoneEnabled() && arg4 != null && (arg4.getBlock() instanceof RepeaterBlock || arg4.getBlock() instanceof ComparatorBlock)
            )
          {
            BaseFinderSearchState.pendingProbes.add(new BaseFinderProbeData(arg, arg2, arg3));
         }
      }
   }
}
