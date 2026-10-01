package com.threesix.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.item.ItemStack;
import com.threesix.util.XorBitUtils;
import com.threesix.data.SelectOptionRecord;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class ModeSetting extends ClientSetting {
   public final String defaultMode;
   public final List options2;

   public ModeSetting(String string, String string2, SelectOptionRecord... local) {
      super(string, string2);
      this.defaultMode = string2;
      this.options2 = List.of(local);
   }

   public List getOptions2() {
      return this.options2;
   }

   public List searchOptions2(String string) {
      String local = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
      if (local.isEmpty()) {
         return this.options2;
      }

      ArrayList arrayListInst = new ArrayList();

      for (SelectOptionRecord selectOptionRecord : (Iterable<SelectOptionRecord>)this.options2) {
         String local2 = selectOptionRecord.value() == null ? "" : selectOptionRecord.value().toLowerCase(Locale.ROOT);
         String local3 = selectOptionRecord.label() == null ? "" : selectOptionRecord.label().toLowerCase(Locale.ROOT);
         if (local2.contains(local) || local3.contains(local)) {
            arrayListInst.add(selectOptionRecord);
         }
      }

      return arrayListInst;
   }

   public void setMode(String string) {

      if (string == null) {
         this.resetToDefault();
      } else {
         for (SelectOptionRecord selectOptionRecord : (Iterable<SelectOptionRecord>)this.options2) {
            if (selectOptionRecord.value().equalsIgnoreCase(string)) {
               this.setValue(selectOptionRecord.value());
               return;
            }
         }
      }
   }

   public void resetToDefault() {

      this.setValue(this.defaultMode);
   }

   public boolean isModeSelected(SelectOptionRecord selectOptionRecord) {
      return selectOptionRecord != null && this.getValue() != null && selectOptionRecord.value().equalsIgnoreCase((String)this.getValue());
   }

   public SelectOptionRecord getSelectedOption() {

      for (SelectOptionRecord selectOptionRecord : (Iterable<SelectOptionRecord>)this.options2) {
         if (this.isModeSelected(selectOptionRecord)) {
            return selectOptionRecord;
         }
      }

      return this.options2.isEmpty() ? null : (SelectOptionRecord)this.options2.getFirst();
   }

   public String getDisplayLabel() {

      SelectOptionRecord local = this.getSelectedOption();
      return local == null ? "Choose" : local.label();
   }

   public ItemStack getPreviewStack2() {
      SelectOptionRecord local = this.getSelectedOption();
      return local == null ? ItemStack.EMPTY : local.getPreviewStack();
   }

}
