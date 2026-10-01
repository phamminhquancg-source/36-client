package com.threesix.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.text.Style;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.OrderedText;
import com.threesix.module.NameProtectModule;
import com.threesix.data.StyledTextRecord;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ServerAddressRedirector {

   public static String replaceAddress(String string) {
      if (string != null && isEnabled3()) {
         String local = getCurrentAddress();
         String local2 = getReplacementAddress();
         if (local != null && local2 != null && !local.isBlank() && !local.equals(local2)) {
            return string.contains(local) ? string.replace(local, local2) : string;
         } else {
            return string;
         }
      } else {
         return string;
      }
   }

   public static StringVisitable rewriteText(StringVisitable arg) {
      if (arg != null && isEnabled3()) {
         List local = applyReplacement(splitText(arg));
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

   public static OrderedText rewriteOrderedText(OrderedText arg) {
      if (arg != null && isEnabled3()) {
         List local = applyReplacement(splitOrderedText(arg));
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

   public static boolean isEnabled3() {
      int local = NameProtectModule.nameProtectModule != null && NameProtectModule.nameProtectModule.isEnabled() && getCurrentAddress() != null && !getCurrentAddress().isBlank() ? 1 : 0;
      return local != 0;
   }

   public static String getCurrentAddress() {
      MinecraftClient mc = MinecraftClient.getInstance();
      return mc != null && mc.getSession() != null ? mc.getSession().getUsername() : null;
   }

   public static String getReplacementAddress() {
      String nameProtectModuleValue = NameProtectModule.nameProtectModule == null ? null : NameProtectModule.nameProtectModule.getMaskedName();
      return nameProtectModuleValue;
   }

   public static List splitText(StringVisitable arg) {
      ArrayList arrayListInst = new ArrayList();
      arg.visit((local, local2) -> {
         appendCodepoints(arrayListInst, local2, local);
         return Optional.empty();
      }, Style.EMPTY);
      return arrayListInst;
   }

   public static List splitOrderedText(OrderedText arg) {
      ArrayList arrayListInst = new ArrayList();
      arg.accept((local, local2, local3) -> {
         arrayListInst.add(new StyledTextRecord(new String(Character.toChars(local3)), local2));
         return true;
      });
      return arrayListInst;
   }

   public static void appendCodepoints(List list, String string, Style arg) {
      if (string != null && !string.isEmpty()) {
         int local = 0;
         while (local < string.length()) {
            int intVal = string.codePointAt(local);
            list.add(new StyledTextRecord(new String(Character.toChars(intVal)), arg));
            local += Character.charCount(intVal);
         }
      }
   }

   public static List applyReplacement(List list) {
      String local = getCurrentAddress();
      String local2 = getReplacementAddress();
      if (local != null && local2 != null && !local.isBlank() && !local.equals(local2)) {
         StringBuilder stringBuilderInst = new StringBuilder();
         ArrayList arrayListInst = new ArrayList(list.size());

         for (StyledTextRecord styledTextRecord : (Iterable<StyledTextRecord>)list) {
            arrayListInst.add(stringBuilderInst.length());
            stringBuilderInst.append(styledTextRecord.text());
         }

         String local3 = stringBuilderInst.toString();
         if (!local3.contains(local)) {
            return null;
         }

         ArrayList arrayListInst2 = new ArrayList();
         int local6 = 0;
         int var11Snapshot = 0;
         int local5;
         while ((local5 = local3.indexOf(local, var11Snapshot)) >= 0) {
            while (local6 < list.size() && (Integer)arrayListInst.get(local6) < local5) {
               arrayListInst2.add((StyledTextRecord)list.get(local6++));
            }

            Style local4 = local6 < list.size() ? ((StyledTextRecord)list.get(local6)).style() : Style.EMPTY;
            arrayListInst2.add(new StyledTextRecord(local2, local4));
            int var9Var1Value = local5 + local.length();

            while (local6 < list.size() && (Integer)arrayListInst.get(local6) < var9Var1Value) {
               local6++;
            }

            var11Snapshot = var9Var1Value;
         }

         while (local6 < list.size()) {
            arrayListInst2.add((StyledTextRecord)list.get(local6++));
         }

         return mergeRuns(arrayListInst2);
      } else {
         return null;
      }
   }

   public static List mergeRuns(List list) {
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
