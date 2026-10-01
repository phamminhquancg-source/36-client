package com.threesix.data;

import java.util.Objects;
import net.minecraft.util.math.Box;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ResourceLoadEntry {
   public final Box id;
   public final int fileHash;
   public final boolean isTexture;
   public final long loadedAtMillis;

   public ResourceLoadEntry(Box arg, int intVal, boolean flag) {
      this.id = arg;
      this.fileHash = intVal;
      this.isTexture = flag;
      this.loadedAtMillis = System.currentTimeMillis();
   }

   public boolean isLoaded() {
      return true;
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof ResourceLoadEntry local ? Objects.equals(this.id, local.id) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }

}
