package com.threesix.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class EntityListSetting extends ClientSetting {
   public final List availableEntities = Registries.ENTITY_TYPE
      .stream()
      .filter(EntityListSetting::isSupportedEntityType)
      .sorted(Comparator.comparing(this::getEntityName, String.CASE_INSENSITIVE_ORDER))
      .toList();
   public long changeCount2;

   public EntityListSetting(String string, EntityType... local) {
      super(string, toEntitySet(local));
   }

   public static boolean isSupportedEntityType(EntityType arg) {
      SpawnGroup var0Value = arg.getSpawnGroup();
      return var0Value == SpawnGroup.MONSTER
         || var0Value == SpawnGroup.CREATURE
         || var0Value == SpawnGroup.AMBIENT
         || var0Value == SpawnGroup.AXOLOTLS
         || var0Value == SpawnGroup.UNDERGROUND_WATER_CREATURE
         || var0Value == SpawnGroup.WATER_CREATURE
         || var0Value == SpawnGroup.WATER_AMBIENT;
   }

   public void setEntities(Set set) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (set != null) {
         for (EntityType class1299 : (Iterable<EntityType>)set) {
            if (class1299 != null) {
               linkedHashSetInst.add(class1299);
            }
         }
      }

      super.setValue(linkedHashSetInst);
      this.changeCount2++;
   }

   public boolean isSelected2(EntityType arg) {
      return arg != null && ((Set)this.getValue()).contains(arg);
   }

   public void toggleEntity(EntityType arg) {

      if (arg != null) {
         LinkedHashSet linkedHashSetInst = new LinkedHashSet((Collection)this.getValue());
         if (!linkedHashSetInst.add(arg)) {
            linkedHashSetInst.remove(arg);
         }

         this.setEntities(linkedHashSetInst);
      }
   }

   public void clearEntities() {
      if (!((Set)this.getValue()).isEmpty()) {
         this.setEntities(Collections.emptySet());
      }
   }

   public int getSelectedCount2() {
      return ((Set)this.getValue()).size();
   }

   public long getChangeCount2() {
      return this.changeCount2;
   }

   public Set getSelectedEntities() {
      return Collections.unmodifiableSet((Set)this.getValue());
   }

   public List getAllEntities() {
      return this.availableEntities;
   }

   public List searchEntities(String string) {

      String local = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
      if (local.isEmpty()) {
         return this.availableEntities;
      }

      ArrayList arrayListInst = new ArrayList();

      for (EntityType class1299 : (Iterable<EntityType>)this.availableEntities) {
         String local2 = this.getEntityName(class1299).toLowerCase(Locale.ROOT);
         Identifier local3 = Registries.ENTITY_TYPE.getId(class1299);
         String local4 = local3 == null ? "" : local3.toString().toLowerCase(Locale.ROOT);
         if (local2.contains(local) || local4.contains(local)) {
            arrayListInst.add(class1299);
         }
      }

      return arrayListInst;
   }

   public String getEntityName(EntityType arg) {
      try {
         return arg.getName().getString();
      } catch (Exception error) {
         Identifier local = Registries.ENTITY_TYPE.getId(arg);
         return local == null ? "Mob" : local.getPath();
      }
   }

   public String getSummaryLabel2() {

      if (((Set)this.getValue()).isEmpty()) {
         return "None";
      }

      EntityType local = (EntityType)((Set)this.getValue()).iterator().next();
      String local2 = this.getEntityName(local);
      int intVal = ((Set)this.getValue()).size() - 1;
      return intVal > 0 ? local2 + " +" + intVal : local2;
   }

   public static Set toEntitySet(EntityType... local) {

      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (local != null) {
         Collections.addAll(linkedHashSetInst, local);
         linkedHashSetInst.remove(null);
      }

      return linkedHashSetInst;
   }

}
