package com.threesix.setting;

import java.util.function.Supplier;
import com.threesix.util.XorBitUtils;

public class ClientSetting {
   public final String name2;
   public final Object defaultValue;
   public Object value;
   public Object minValue;
   public Object maxValue;
   public Supplier visibilitySupplier = () -> true;

   public ClientSetting(String string, Object object) {
      this.name2 = string;
      this.value = object;
      this.defaultValue = object;
   }

   public ClientSetting(String string, Object object, Object object2, Object object3) {
      this.name2 = string;
      this.value = object;
      this.defaultValue = object;
      this.minValue = object2;
      this.maxValue = object3;
   }

   public String getName() {
      return this.name2;
   }

   public Object getValue() {
      return this.value;
   }

   public Object getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isNamed(String string) {

      return this.name2.equalsIgnoreCase(string);
   }

   public ClientSetting withVisibility(Supplier supplier) {
      this.visibilitySupplier = supplier == null ? () -> true : supplier;
      return this;
   }

   public boolean isVisible() {
      try {

         return this.visibilitySupplier == null || (Boolean)this.visibilitySupplier.get();
      } catch (Exception error) {
         return true;
      }
   }

   public void setValue(Object object) {
      this.value = object;
   }

   public Object getMinValue() {
      return this.minValue;
   }

   public Object getMaxValue() {
      return this.maxValue;
   }

}
