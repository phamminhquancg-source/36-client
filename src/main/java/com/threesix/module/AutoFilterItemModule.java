package com.threesix.module;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.ItemSelectSetting;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ModeSetting02;
import com.threesix.internal.ModuleBase;

public final class AutoFilterItemModule extends ModuleBase {
   public final ItemSelectSetting filterItemsSetting = new ItemSelectSetting("Filter Items");
   public final ModeSetting02 modeSetting = new ModeSetting02("Mode", "Inventory", "Inventory", "Sell");
   private long nextActionAt = 0L;

   public AutoFilterItemModule() {
      super("AutoFilterItem", ModuleCategory.MISC);
      this.registerSetting(this.filterItemsSetting);
      this.registerSetting(this.modeSetting);
   }

   @Override
   public void onTick() {

      if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.interactionManager != null) {
         if (minecraftClient.currentScreen instanceof HandledScreen local) {
            ScreenHandler var1Value = local.getScreenHandler();
            if (var1Value != null) {
               long systemValue = System.currentTimeMillis();
               if (systemValue >= this.nextActionAt) {
                  boolean flag = "Sell".equalsIgnoreCase((String)this.modeSetting.getValue());
                  int intVal = var1Value.slots.size() - 36;
                  if (intVal < 0) {
                  }

                  int local2 = 0;
                  if (!flag) {
                     for (int index = 0; index < intVal; index++) {
                        ItemStack var12Value = var1Value.getSlot(index).getStack();
                        if (!var12Value.isEmpty()) {
                           Item var9Value = var12Value.getItem();
                           if (this.filterItemsSetting.isSelected3(var9Value)) {
                              minecraftClient.interactionManager.clickSlot(var1Value.syncId, index, 0, SlotActionType.QUICK_MOVE, minecraftClient.player);
                              this.nextActionAt = systemValue + ThreadLocalRandom.current().nextInt(10, 37);
                              local2++;
                              break;
                           }
                        }
                     }

                     if (local2 == 0) {
                        boolean falseSnapshot = false;

                        for (int index2 = 0; index2 < intVal; index2++) {
                           ItemStack var12Value2 = var1Value.getSlot(index2).getStack();
                           if (!var12Value2.isEmpty() && this.filterItemsSetting.isSelected3(var12Value2.getItem())) {
                              falseSnapshot = true;
                              break;
                           }
                        }

                        if (!falseSnapshot) {
                        }
                     }
                  } else {
                     boolean falseSnapshot2 = false;

                     for (int index3 = intVal; index3 < var1Value.slots.size(); index3++) {
                        ItemStack var12Value3 = var1Value.getSlot(index3).getStack();
                        if (!var12Value3.isEmpty() && this.filterItemsSetting.isSelected3(var12Value3.getItem())) {
                           minecraftClient.interactionManager.clickSlot(var1Value.syncId, index3, 0, SlotActionType.QUICK_MOVE, minecraftClient.player);
                           this.nextActionAt = systemValue + ThreadLocalRandom.current().nextInt(10, 37);
                           falseSnapshot2 = true;
                           break;
                        }
                     }

                     if (!falseSnapshot2) {
                        boolean falseSnapshot3 = false;

                        for (int index4 = intVal; index4 < var1Value.slots.size(); index4++) {
                           ItemStack var12Value4 = var1Value.getSlot(index4).getStack();
                           if (!var12Value4.isEmpty() && this.filterItemsSetting.isSelected3(var12Value4.getItem())) {
                              falseSnapshot3 = true;
                              break;
                           }
                        }

                        if (!falseSnapshot3) {
                           minecraftClient.player.closeHandledScreen();
                        }
                     }
                  }
               }
            }
         }
      }
   }

}
