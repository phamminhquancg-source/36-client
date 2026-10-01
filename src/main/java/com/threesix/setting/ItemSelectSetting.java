package com.threesix.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class ItemSelectSetting extends ClientSetting {
   public final List availableItems = Registries.ITEM.stream().filter(local -> {
      return local != Items.AIR;
   }).sorted(Comparator.comparing(this::getItemName, String.CASE_INSENSITIVE_ORDER)).toList();
   public long changeCount3;

   public ItemSelectSetting(String string, Item... local) {
      super(string, toItemSet(local));
   }

   public void setItems(Set set) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (set != null) {
         for (Item class1792 : (Iterable<Item>)set) {
            if (class1792 != null) {
               linkedHashSetInst.add(class1792);
            }
         }
      }

      super.setValue(linkedHashSetInst);
      this.changeCount3++;
   }

   public boolean isSelected3(Item arg) {
      return arg != null && ((Set)this.getValue()).contains(arg);
   }

   public void toggleItem(Item arg) {
      if (arg != null) {
         LinkedHashSet linkedHashSetInst = new LinkedHashSet((Collection)this.getValue());
         if (!linkedHashSetInst.add(arg)) {
            linkedHashSetInst.remove(arg);
         }

         this.setItems(linkedHashSetInst);
      }
   }

   public void clearItems() {
      if (!((Set)this.getValue()).isEmpty()) {
         this.setItems(Collections.emptySet());
      }
   }

   public int getSelectedCount3() {
      return ((Set)this.getValue()).size();
   }

   public long getChangeCount3() {
      return this.changeCount3;
   }

   public Set getSelectedItems() {
      return Collections.unmodifiableSet((Set)this.getValue());
   }

   public List getAllItems() {
      return this.availableItems;
   }

   public List searchItems(String string) {
      String local = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
      if (local.isEmpty()) {
         return this.availableItems;
      }

      ArrayList arrayListInst = new ArrayList();

      for (Item class1792 : (Iterable<Item>)this.availableItems) {
         String local2 = this.getItemName(class1792).toLowerCase(Locale.ROOT);
         Identifier local3 = Registries.ITEM.getId(class1792);
         String local4 = local3 == null ? "" : local3.toString().toLowerCase(Locale.ROOT);
         if (local2.contains(local) || local4.contains(local)) {
            arrayListInst.add(class1792);
         }
      }

      return arrayListInst;
   }

   public String getItemName(Item arg) {
      try {
         return arg.getName().getString();
      } catch (Exception error) {
         Identifier local = Registries.ITEM.getId(arg);
         return local == null ? "Item" : local.getPath();
      }
   }

   public ItemStack toItemStack(Item arg) {
      return arg == null ? ItemStack.EMPTY : new ItemStack(arg);
   }

   public String getSummaryLabel3() {
      if (((Set)this.getValue()).isEmpty()) {
         return "None";
      }

      Item local = (Item)((Set)this.getValue()).iterator().next();
      String local2 = this.getItemName(local);
      int intVal = ((Set)this.getValue()).size() - 1;
      return intVal > 0 ? local2 + " +" + intVal : local2;
   }

   public static Set toItemSet(Item... local) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (local != null) {
         Collections.addAll(linkedHashSetInst, local);
         linkedHashSetInst.remove(null);
         linkedHashSetInst.remove(Items.AIR);
      }

      return linkedHashSetInst;
   }

}
