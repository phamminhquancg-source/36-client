package com.threesix.data;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public class HudTextEntry {
   public NativeImageBackedTexture nativeImage;
   public Identifier textureId;
   public int halfWidth;
   public int halfHeight;

   public HudTextEntry(NativeImageBackedTexture arg, Identifier arg2, int intVal, int intVal2) {
      this.nativeImage = arg;
      this.textureId = arg2;
      this.halfWidth = intVal;
      this.halfHeight = intVal2;
   }

}
