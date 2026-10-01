package com.threesix.module;

import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.RemovalReason;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class AntiTrapModule extends ModuleBase {
   public final ClientSetting armorStandSetting = new ClientSetting("Armor Stands", true);
   public final ClientSetting minecartSetting = new ClientSetting("Minecarts", true);
   public final ClientSetting chestMinecartSetting = new ClientSetting("Chest Minecarts", true);
   public final ClientSetting hopperMinecartSetting = new ClientSetting("Hopper Minecarts", true);

   public AntiTrapModule() {
      super("AntiTrap", ModuleCategory.DONUT);
      this.registerSetting(this.armorStandSetting);
      this.registerSetting(this.minecartSetting);
      this.registerSetting(this.chestMinecartSetting);
      this.registerSetting(this.hopperMinecartSetting);
   }

   @Override
   public void onEnable() {
      this.breakTraps();
   }

   @Override
   public void onTick() {
      this.breakTraps();
   }

   public void breakTraps() {

      if (minecraftClient.world != null) {
         ArrayList<Entity> arrayListInst = new ArrayList<>();
         minecraftClient.world.getEntities().forEach(entry -> {
            if (entry != null && this.isTrapEntityType(entry.getType())) {
               arrayListInst.add(entry);
            }
         });
         arrayListInst.forEach(item -> {
            if (!item.isRemoved()) {
               item.remove(RemovalReason.DISCARDED);
            }
         });
      }
   }

   public boolean isTrapEntityType(EntityType arg) {
      if (arg == null) {
         return false;
      } else if ((Boolean)this.armorStandSetting.getValue() && arg.equals(EntityType.ARMOR_STAND)) {
         return true;
      } else if ((Boolean)this.minecartSetting.getValue() && arg.equals(EntityType.MINECART)) {
         return true;
      } else {
         return (Boolean)this.chestMinecartSetting.getValue() && arg.equals(EntityType.CHEST_MINECART)
            ? true
            : (Boolean)this.hopperMinecartSetting.getValue() && arg.equals(EntityType.HOPPER_MINECART);
      }
   }

}
