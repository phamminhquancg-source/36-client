package com.threesix.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class BlockListSetting extends ClientSetting {
   public final List availableBlocks = Registries.BLOCK.stream().filter(local -> {
      return local != Blocks.AIR;
   }).sorted(Comparator.comparing(this::getBlockName, String.CASE_INSENSITIVE_ORDER)).toList();
   public long changeCount;

   public BlockListSetting(String string, Block... local) {
      super(string, toBlockSet(local));
   }

   public void setBlocks(Set set) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (set != null) {
         for (Block class2248 : (Iterable<Block>)set) {
            if (class2248 != null && class2248 != Blocks.AIR) {
               linkedHashSetInst.add(class2248);
            }
         }
      }

      super.setValue(linkedHashSetInst);
      this.changeCount++;
   }

   public boolean isSelected(Block arg) {
      return arg != null && ((Set)this.getValue()).contains(arg);
   }

   public void toggleBlock(Block arg) {
      if (arg != null && arg != Blocks.AIR) {
         LinkedHashSet linkedHashSetInst = new LinkedHashSet((Collection)this.getValue());
         if (!linkedHashSetInst.add(arg)) {
            linkedHashSetInst.remove(arg);
         }

         this.setBlocks(linkedHashSetInst);
      }
   }

   public void clearBlocks() {
      if (!((Set)this.getValue()).isEmpty()) {
         this.setBlocks(Collections.emptySet());
      }
   }

   public int getSelectedCount() {
      return ((Set)this.getValue()).size();
   }

   public long getChangeCount() {
      return this.changeCount;
   }

   public Set getSelectedBlocks() {
      return Collections.unmodifiableSet((Set)this.getValue());
   }

   public List getAllBlocks() {
      return this.availableBlocks;
   }

   public List searchBlocks(String string) {

      String local = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
      if (local.isEmpty()) {
         return this.availableBlocks;
      }

      ArrayList arrayListInst = new ArrayList();

      for (Block class2248 : (Iterable<Block>)this.availableBlocks) {
         String local2 = this.getBlockName(class2248).toLowerCase(Locale.ROOT);
         Identifier local3 = Registries.BLOCK.getId(class2248);
         String local4 = local3 == null ? "" : local3.toString().toLowerCase(Locale.ROOT);
         if (local2.contains(local) || local4.contains(local)) {
            arrayListInst.add(class2248);
         }
      }

      return arrayListInst;
   }

   public String getBlockName(Block arg) {
      try {
         return arg.getName().getString();
      } catch (Exception error) {
         Identifier local = Registries.BLOCK.getId(arg);
         return local == null ? "Block" : local.getPath();
      }
   }

   public String getSummaryLabel() {
      if (((Set)this.getValue()).isEmpty()) {
         return "None";
      }

      Block local = (Block)((Set)this.getValue()).iterator().next();
      String local2 = this.getBlockName(local);
      int intVal = ((Set)this.getValue()).size() - 1;
      return intVal > 0 ? local2 + " +" + intVal : local2;
   }

   public static Set toBlockSet(Block... local) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (local != null) {
         Collections.addAll(linkedHashSetInst, local);
         linkedHashSetInst.remove(null);
         linkedHashSetInst.remove(Blocks.AIR);
      }

      return linkedHashSetInst;
   }

}
