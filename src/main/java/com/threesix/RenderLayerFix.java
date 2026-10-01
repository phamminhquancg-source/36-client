package com.threesix;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

/**
 * Accessor for RenderLayer.of(String, RenderSetup).
 *
 * <p>In Minecraft 1.21.11 that factory method is package-private, so calling it directly from
 * this package fails at runtime with {@code IllegalAccessError}. The method name is also
 * remapped to {@code method_XXXXX} in the shipped jar, so it cannot be referenced by a string
 * literal either. We therefore look the method up by its (static, String, RenderSetup) signature
 * and force accessibility.
 */
public final class RenderLayerFix {
   private static volatile Method CACHED;

   private RenderLayerFix() {
   }

   public static RenderLayer of(String name, RenderSetup setup) {
      try {
         if (CACHED == null) {
            CACHED = lookup();
         }

         return (RenderLayer)CACHED.invoke(null, name, setup);
      } catch (Throwable error) {
         throw new RuntimeException("RenderLayerFix failed", error);
      }
   }

   private static Method lookup() throws NoSuchMethodException {
      for (Method method : RenderLayer.class.getDeclaredMethods()) {
         Class<?>[] params = method.getParameterTypes();
         if (params.length == 2 && Modifier.isStatic(method.getModifiers())
            && params[0] == String.class && params[1] == RenderSetup.class) {
            method.setAccessible(true);
            return method;
         }
      }

      throw new NoSuchMethodException("static RenderLayer.of(String, RenderSetup)");
   }
}
