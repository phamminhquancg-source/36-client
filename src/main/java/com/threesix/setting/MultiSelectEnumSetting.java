package com.threesix.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.item.ItemStack;
import com.threesix.util.XorBitUtils;
import com.threesix.data.SelectOptionRecord;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class MultiSelectEnumSetting extends ClientSetting {
   public final List options;

   public MultiSelectEnumSetting(String string, SelectOptionRecord... local) {
      super(string, new LinkedHashSet());
      this.options = List.of(local);
   }

   public void setValues(Set set) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (set != null) {
         for (String string : (Iterable<String>)set) {
            if (string != null) {
               for (SelectOptionRecord selectOptionRecord : (Iterable<SelectOptionRecord>)this.options) {
                  if (selectOptionRecord.value().equalsIgnoreCase(string)) {
                     linkedHashSetInst.add(selectOptionRecord.value());
                     break;
                  }
               }
            }
         }
      }

      super.setValue(linkedHashSetInst);
   }

   public List getOptions() {
      return this.options;
   }

   public List searchOptions(String string) {
      String local = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
      if (local.isEmpty()) {
         return this.options;
      }

      ArrayList arrayListInst = new ArrayList();

      for (SelectOptionRecord selectOptionRecord : (Iterable<SelectOptionRecord>)this.options) {
         String local2 = selectOptionRecord.value() == null ? "" : selectOptionRecord.value().toLowerCase(Locale.ROOT);
         String local3 = selectOptionRecord.label() == null ? "" : selectOptionRecord.label().toLowerCase(Locale.ROOT);
         if (local2.contains(local) || local3.contains(local)) {
            arrayListInst.add(selectOptionRecord);
         }
      }

      return arrayListInst;
   }

   public void toggleValue(String string) {
      if (string != null) {
         LinkedHashSet linkedHashSetInst = new LinkedHashSet((Collection)this.getValue());
         String nullSnapshot = null;

         for (SelectOptionRecord selectOptionRecord : (Iterable<SelectOptionRecord>)this.options) {
            if (selectOptionRecord.value().equalsIgnoreCase(string)) {
               nullSnapshot = selectOptionRecord.value();
               break;
            }
         }

         if (nullSnapshot != null) {
            if (!linkedHashSetInst.add(nullSnapshot)) {
               linkedHashSetInst.remove(nullSnapshot);
            }

            this.setValues(linkedHashSetInst);
         }
      }
   }

   public void clearValues() {
      if (!((Set)this.getValue()).isEmpty()) {
         this.setValues(Collections.emptySet());
      }
   }

   public boolean isValueSelected(String string) {
      if (string == null) {
         return false;
      }

      for (String string2 : (Iterable<String>)(Set)this.getValue()) {
         if (string2.equalsIgnoreCase(string)) {
            return true;
         }
      }

      return false;
   }

   public boolean isOptionSelected(SelectOptionRecord selectOptionRecord) {
      return selectOptionRecord != null && this.isValueSelected(selectOptionRecord.value());
   }

   public int getSelectedCount4() {
      return ((Set)this.getValue()).size();
   }

   public List getSelectedOptions() {
      ArrayList arrayListInst = new ArrayList();

      for (SelectOptionRecord selectOptionRecord : (Iterable<SelectOptionRecord>)this.options) {
         if (this.isValueSelected(selectOptionRecord.value())) {
            arrayListInst.add(selectOptionRecord);
         }
      }

      return arrayListInst;
   }

   public String getSummaryLabel4() {
      List local = this.getSelectedOptions();
      if (local.isEmpty()) {
         return "Choose";
      }

      SelectOptionRecord local2 = (SelectOptionRecord)local.getFirst();
      int intVal = local.size() - 1;
      return intVal > 0 ? local2.label() + " +" + intVal : local2.label();
   }

   public ItemStack getPreviewStack() {
      List local = this.getSelectedOptions();
      return local.isEmpty() ? ItemStack.EMPTY : ((SelectOptionRecord)local.getFirst()).getPreviewStack();
   }

}
