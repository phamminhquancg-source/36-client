package com.threesix.setting;

import java.util.List;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class ModeSetting02 extends ClientSetting {
   public final List modes;
   public final List aliases;

   public ModeSetting02(String string, String string2, String... local) {
      this(string, string2, new String[0], local);
   }

   public ModeSetting02(String string, String string2, String[] string3, String... local) {
      super(string, string2);
      if (local != null && local.length != 0) {
         this.modes = List.of(local);
         this.aliases = string3 == null ? List.of() : List.of(string3);
         this.selectMode(string2);
      } else {
         throw new IllegalArgumentException("ModeSetting requires at least one mode");
      }
   }

   public List getModes() {
      return this.modes;
   }

   public void nextMode() {
      this.selectMode(this.getSteppedMode(1));
   }

   public void previousMode() {
      this.selectMode(this.getSteppedMode(-1));
   }

   public boolean isModeSelected2(String string) {
      return this.normalize(string).equalsIgnoreCase((String)this.getValue());
   }

   public void selectMode(String string) {
      super.setValue(this.canonicalize(string));
   }

   @Override
   public boolean isNamed(String string) {
      if (super.isNamed(string)) {
         return true;
      }

      String local = this.normalize(string);

      for (String string2 : (Iterable<String>)this.aliases) {
         if (string2.equalsIgnoreCase(local)) {
            return true;
         }
      }

      return false;
   }

   public String getSteppedMode(int intVal) {
      int intVal2 = this.modes.size();
      if (intVal2 == 0) {
         return "";
      }

      String local = (String)this.getValue();

      for (int index = 0; index < intVal2; index++) {
         if (((String)this.modes.get(index)).equalsIgnoreCase(local)) {
            int floorModValue = Math.floorMod(index + intVal, intVal2);
            return (String)this.modes.get(floorModValue);
         }
      }

      return (String)this.modes.getFirst();
   }

   public String canonicalize(String string) {
      String local = this.normalize(string);

      for (String string2 : (Iterable<String>)this.modes) {
         if (string2.equalsIgnoreCase(local)) {
            return string2;
         }
      }

      return (String)this.modes.getFirst();
   }

   public String normalize(String string) {
      return string == null ? "" : string.trim();
   }

}
