package com.threesix.module;

import net.minecraft.util.Hand;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.client.gui.screen.ingame.CrafterScreen;
import net.minecraft.util.hit.HitResult.Type;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.MultiSelectSetting;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class CrafterSetupModule extends ModuleBase {
   public final MultiSelectSetting patternSetting = new MultiSelectSetting("Pattern", new Boolean[]{true, true, true, true, true, true, true, true, true});
   public final ClientSetting delaySetting = new ClientSetting("Delay", 2, 1, 20);
   public final ClientSetting autoTriggerSetting = new ClientSetting("Auto Trigger", true);
   private int cooldownTicks = 0;
   private long lastActionAt = 0L;

   public CrafterSetupModule() {
      super("Crafter Setup", ModuleCategory.MISC);
      this.registerSetting(this.patternSetting);
      this.registerSetting(this.delaySetting);
      this.registerSetting(this.autoTriggerSetting);
   }

   private boolean[] getPatternMask() {
      Boolean[] local = (Boolean[])this.patternSetting.getValue();
      if (local != null && local.length == 9) {
         boolean[] local2 = new boolean[9];

         for (int index = 0; index < 9; index++) {
            local2[index] = local[index] != null && local[index];
         }

         return local2;
      } else {
         return new boolean[]{true, true, true, true, true, true, true, true, true};
      }
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
         } else if (System.currentTimeMillis() - this.lastActionAt >= 500L) {
            if (minecraftClient.currentScreen instanceof CrafterScreen) {
               CrafterScreenHandler local = (CrafterScreenHandler)((CrafterScreen)minecraftClient.currentScreen).getScreenHandler();
               this.applyScreenPattern(local);
            } else if ((Boolean)this.autoTriggerSetting.getValue()) {
               HitResult local2 = minecraftClient.crosshairTarget;
               if (local2 != null && local2.getType() == Type.BLOCK) {
                  BlockHitResult local3 = (BlockHitResult)local2;
                  BlockPos var2Value = local3.getBlockPos();
                  if (minecraftClient.world.getBlockState(var2Value).isOf(Blocks.CRAFTER)) {
                     if (!(minecraftClient.player.squaredDistanceTo(Vec3d.ofCenter(var2Value)) > 25.0)) {
                        boolean[] local4 = this.getPatternMask();
                        if (minecraftClient.world.getBlockEntity(var2Value) instanceof CrafterBlockEntity local5) {
                           boolean falseSnapshot = false;

                           for (int index = 0; index < 9; index++) {
                              boolean var5Value = local5.isSlotDisabled(index);
                              boolean flag = !var5Value;
                              if (flag != local4[index]) {
                                 falseSnapshot = true;
                                 break;
                              }
                           }

                           if (!falseSnapshot) {
                              return;
                           }
                        }

                        if (minecraftClient.interactionManager != null) {
                           minecraftClient.interactionManager.interactBlock(minecraftClient.player, Hand.MAIN_HAND, local3);
                           this.cooldownTicks = (Integer)this.delaySetting.getValue();
                           this.lastActionAt = System.currentTimeMillis();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void applyScreenPattern(CrafterScreenHandler arg) {
      boolean[] local = this.getPatternMask();
      byte byteVal = 0;

      for (int index = 0; index < 9; index++) {
         Slot local2 = (Slot)arg.slots.get(index);
         boolean flag = !local2.isEnabled();
         boolean flag2 = !flag;
         boolean flag3 = local[index];
         if (flag2 != flag3) {
            minecraftClient.interactionManager.clickSlot(arg.syncId, index, 0, SlotActionType.PICKUP, minecraftClient.player);
            this.cooldownTicks = (Integer)this.delaySetting.getValue();
            break;
         }
      }

      if (byteVal == 0) {
         minecraftClient.player.closeHandledScreen();
      }
   }

   private boolean[] parsePatternString(String string) {
      if (string != null && string.length() == 9) {
         boolean[] local = new boolean[9];

         for (int index = 0; index < 9; index++) {
            char charVal = string.charAt(index);
            if (charVal == '1') {
               local[index] = true;
            } else {
               if (charVal != '0') {
                  return null;
               }

               local[index] = false;
            }
         }

         return local;
      } else {
         return null;
      }
   }

   public static boolean[] parsePatternMask(String string) {
      if (string != null && string.length() == 9) {
         boolean[] local = new boolean[9];

         for (int index = 0; index < 9; index++) {
            local[index] = string.charAt(index) == '1';
         }

         return local;
      } else {
         return new boolean[]{true, true, true, true, true, true, true, true, true};
      }
   }

}
