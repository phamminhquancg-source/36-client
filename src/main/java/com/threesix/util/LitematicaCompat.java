package com.threesix.util;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class LitematicaCompat {
   private static final String SCHEMATIC_WORLD_CLASS = "fi.dy.masa.litematica.world.SchematicWorldHandler";
   private static final String DATA_MANAGER_CLASS = "fi.dy.masa.litematica.data.DataManager";
   private static Boolean isLitematicaLoaded;

   private LitematicaCompat() {
   }

   public static boolean isLitematicaPresent() {
      if (isLitematicaLoaded == null) {
         try {
            Class.forName("fi.dy.masa.litematica.world.SchematicWorldHandler");
            isLitematicaLoaded = true;
         } catch (Throwable error) {
            isLitematicaLoaded = false;
         }
      }

      return isLitematicaLoaded;
   }

   public static World getSchematicWorld() {
      if (!isLitematicaPresent()) {
         return null;
      }

      try {
         Object classValue = Class.forName("fi.dy.masa.litematica.world.SchematicWorldHandler").getMethod("getSchematicWorld").invoke(null);
         return classValue instanceof World ? (World)classValue : null;
      } catch (Throwable error) {
         return null;
      }
   }

   public static Object getRenderLayerRange() {
      if (!isLitematicaPresent()) {
         return null;
      }

      try {
         return Class.forName("fi.dy.masa.litematica.data.DataManager").getMethod("getRenderLayerRange").invoke(null);
      } catch (Throwable error) {
         return null;
      }
   }

   public static boolean isPositionInRange(Object object, BlockPos arg) {
      if (object == null) {
         return true;
      }

      try {
         return Boolean.TRUE.equals(object.getClass().getMethod("isPositionWithinRange", BlockPos.class).invoke(object, arg));
      } catch (Throwable error) {
         return true;
      }
   }

}
