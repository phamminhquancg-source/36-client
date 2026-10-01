package com.threesix.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class ThreesixMixinPlugin implements IMixinConfigPlugin {
   private static boolean litematicaChecked = false;
   private static boolean litematicaPresent = false;

   public void onLoad(String string) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String string, String string2) {
      return string2.startsWith("天阳光明媚心情愉快努.力学习天天进步生") ? isLitematicaPresent() : true;
   }

   public void acceptTargets(Set set, Set set2) {
   }

   public List getMixins() {
      return null;
   }

   public void preApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
   }

   public void postApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
   }

   private static boolean isLitematicaPresent() {
      if (!litematicaChecked) {
         litematicaChecked = true;

         try {
            Class.forName("fi.dy.masa.litematica.Litematica");
            litematicaPresent = true;
         } catch (Throwable error) {
            litematicaPresent = false;
         }
      }

      return litematicaPresent;
   }
}
