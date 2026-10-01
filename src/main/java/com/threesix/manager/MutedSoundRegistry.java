package com.threesix.manager;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.text.Text;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class MutedSoundRegistry {
   public static final Set mutedSounds = Collections.newSetFromMap(new WeakHashMap());

   public static void call1(EntityRenderState arg) {
      mutedSounds.remove(arg);
   }

   public static void mute(EntityRenderState arg) {
      mutedSounds.add(arg);
   }

   public static boolean isMuted(EntityRenderState arg) {
      return mutedSounds.contains(arg);
   }

   public static boolean shouldMuteText(Text arg) {
      return false;
   }

}
