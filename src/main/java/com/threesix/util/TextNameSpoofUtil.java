package com.threesix.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.text.Style;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.OrderedText;
import com.threesix.data.StyledTextRecord;
import com.threesix.util.XorBitUtils;
import com.threesix.module.FakeRolesModule;
import com.threesix.util.StringVaultDecoder;

public final class TextNameSpoofUtil {

   public static String replaceAddress2(String string) {
      if (string != null && FakeRolesModule.isFakeRolesActive()) {
         String local = getCurrentAddress2();
         if (local == null || local.isBlank()) {
            return string;
         }

         if (!string.contains(local)) {
            return string;
         }

         String fakeRolesModuleValue = FakeRolesModule.getTabName();
         return fakeRolesModuleValue != null && !fakeRolesModuleValue.equals(local) ? string.replace(local, fakeRolesModuleValue) : string;
      } else {
         return string;
      }
   }

   public static OrderedText rewriteOrderedText2(OrderedText arg) {
      if (arg != null && FakeRolesModule.isFakeRolesActive()) {
         List local = applyRolePrefix(splitOrderedText2(arg));
         if (local == null) {
            return arg;
         }

         if (local.isEmpty()) {
            return OrderedText.empty();
         }

         ArrayList arrayListInst = new ArrayList(local.size());

         for (StyledTextRecord styledTextRecord : (Iterable<StyledTextRecord>)local) {
            arrayListInst.add(OrderedText.styledForwardsVisitedString(styledTextRecord.text(), styledTextRecord.style()));
         }

         return OrderedText.concat(arrayListInst);
      } else {
         return arg;
      }
   }

   public static StringVisitable rewriteText2(StringVisitable arg) {
      if (arg != null && FakeRolesModule.isFakeRolesActive()) {
         List local = applyRolePrefix(splitText2(arg));
         if (local == null) {
            return arg;
         }

         if (local.isEmpty()) {
            return StringVisitable.EMPTY;
         }

         ArrayList arrayListInst = new ArrayList(local.size());

         for (StyledTextRecord styledTextRecord : (Iterable<StyledTextRecord>)local) {
            arrayListInst.add(StringVisitable.styled(styledTextRecord.text(), styledTextRecord.style()));
         }

         return StringVisitable.concat(arrayListInst);
      } else {
         return arg;
      }
   }

   public static String getCurrentAddress2() {
      MinecraftClient mc = MinecraftClient.getInstance();
      return mc != null && mc.getSession() != null ? mc.getSession().getUsername() : null;
   }

   public static List splitOrderedText2(OrderedText arg) {
      ArrayList arrayListInst = new ArrayList();
      arg.accept((local, local2, local3) -> {
         arrayListInst.add(new StyledTextRecord(new String(Character.toChars(local3)), local2));
         return true;
      });
      return arrayListInst;
   }

   public static List splitText2(StringVisitable arg) {
      ArrayList arrayListInst = new ArrayList();
      arg.visit((local, local2) -> {
         int local3 = 0;
         while (local3 < local2.length()) {
            int local4 = local2.codePointAt(local3);
            arrayListInst.add(new StyledTextRecord(new String(Character.toChars(local4)), local));
            local3 += Character.charCount(local4);
         }

         return Optional.empty();
      }, Style.EMPTY);
      return arrayListInst;
   }

   public static List applyRolePrefix(List list) {
      String local = getCurrentAddress2();
      if (local != null && !local.isBlank()) {
         String fakeRolesModuleValue = FakeRolesModule.getFooter();
         if (fakeRolesModuleValue == null) {
            return null;
         }

         StringBuilder stringBuilderInst = new StringBuilder();
         ArrayList arrayListInst = new ArrayList(list.size());

         for (StyledTextRecord styledTextRecord : (Iterable<StyledTextRecord>)list) {
            arrayListInst.add(stringBuilderInst.length());
            stringBuilderInst.append(styledTextRecord.text());
         }

         String local2 = stringBuilderInst.toString();
         if (!local2.contains(local)) {
            return null;
         }

         Style fakeRolesModuleValue2 = FakeRolesModule.getStyleAdmin();
         ArrayList arrayListInst2 = new ArrayList();
         int local4 = 0;
         int var11Snapshot = 0;
         int local3;
         int var10Var1Value = 0;
         while ((local3 = local2.indexOf(local, var11Snapshot)) >= 0) {
            while (local4 < list.size() && (Integer)arrayListInst.get(local4) < local3) {
               arrayListInst2.add((StyledTextRecord)list.get(local4++));
            }

            while (var10Var1Value < fakeRolesModuleValue.length()) {
               int intVal = fakeRolesModuleValue.codePointAt(var10Var1Value);
               arrayListInst2.add(new StyledTextRecord(new String(Character.toChars(intVal)), FakeRolesModule.getStyleForRoleIndex(intVal)));
               var10Var1Value += Character.charCount(intVal);
            }

            while (var10Var1Value < local.length()) {
               int intVal2 = local.codePointAt(var10Var1Value);
               arrayListInst2.add(new StyledTextRecord(new String(Character.toChars(intVal2)), fakeRolesModuleValue2));
               var10Var1Value += Character.charCount(intVal2);
            }

            var10Var1Value = local3 + local.length();

            while (local4 < list.size() && (Integer)arrayListInst.get(local4) < var10Var1Value) {
               local4++;
            }

            var11Snapshot = var10Var1Value;
         }

         while (local4 < list.size()) {
            arrayListInst2.add((StyledTextRecord)list.get(local4++));
         }

         return mergeRuns2(arrayListInst2);
      } else {
         return null;
      }
   }

   public static List mergeRuns2(List list) {
      if (list.isEmpty()) {
         return list;
      }

      ArrayList arrayListInst = new ArrayList(list.size());
      StyledTextRecord local = (StyledTextRecord)list.getFirst();

      for (int index = 1; index < list.size(); index++) {
         StyledTextRecord local2 = (StyledTextRecord)list.get(index);
         if (Objects.equals(local.style(), local2.style())) {
            local = new StyledTextRecord(local.text() + local2.text(), local.style());
         } else {
            arrayListInst.add(local);
            local = local2;
         }
      }

      arrayListInst.add(local);
      return arrayListInst;
   }

}
