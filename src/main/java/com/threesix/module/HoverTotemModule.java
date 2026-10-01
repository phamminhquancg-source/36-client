package com.threesix.module;

import com.threesix.mixin.HandledScreenAccessor;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Items;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.StringVaultDecoder;
import com.threesix.data.ModuleCategory;

public final class HoverTotemModule extends ModuleBase {
   public int lastSwappedSlot = -1;

   public HoverTotemModule() {
      super("Hover Totem", ModuleCategory.COMBAT);
   }

   @Override
   public void onDisable() {
      this.lastSwappedSlot = -1;
      super.onDisable();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.interactionManager != null) {
         if (minecraftClient.currentScreen instanceof HandledScreen local2) {
            Slot local = ((HandledScreenAccessor)local2).threesix$getFocusedSlot();
            if (local != null && !local.getStack().isEmpty()) {
               if (!local.getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                  this.lastSwappedSlot = -1;
               } else if (!minecraftClient.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) && local.id != this.lastSwappedSlot) {
                  int minecraftClientValue = minecraftClient.player.currentScreenHandler.syncId;
                  minecraftClient.interactionManager.clickSlot(minecraftClientValue, local.id, 40, SlotActionType.SWAP, minecraftClient.player);
                  this.lastSwappedSlot = local.id;
               }
            } else {
               this.lastSwappedSlot = -1;
            }
         } else {
            this.lastSwappedSlot = -1;
         }
      }
   }

}
