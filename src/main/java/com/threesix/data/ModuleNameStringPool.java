package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ModuleNameStringPool {
   private static final int nameLengthSum = 28234;
   private static final int[][] encodedNames = new int[][]{
      encodeName("Auto Totem"),
      encodeName("Auto Totem"),
      encodeName("Storage ESP"),
      encodeName("Block ESP"),
      encodeName("Freecam"),
      encodeName("SpawnerProtect"),
      encodeName("AntiTrap"),
      encodeName("AutoDoubleHand"),
      encodeName("Auto Inv Totem"),
      encodeName("Threesix-CoordSnapper"),
      encodeName("Threesix-SkinChanger"),
      encodeName("Threesix +"),
      encodeName("Threesix Menu"),
      encodeName("Threesix Configs"),
      encodeName("AutoLog"),
      encodeName("AutoTPA"),
      encodeName("NameProtect"),
      encodeName("Sprint"),
      encodeName("FullBright"),
      encodeName("NoRender"),
      encodeName("Hole ESP"),
      encodeName("Player ESP"),
      encodeName("Mob ESP"),
      encodeName("Chat Macro"),
      encodeName("Chunk Toggle"),
      encodeName("ChunkFinder"),
      encodeName("Config Share"),
      encodeName("CoordSnapper"),
      encodeName("Double Anchor"),
      encodeName("FakeRoles"),
      encodeName("FakeStats"),
      encodeName("Fast Place"),
      encodeName("FreeLook"),
      encodeName("Friends"),
      encodeName("FutureDebug"),
      encodeName("Growth Finder"),
      encodeName("Hitbox"),
      encodeName("HomeSetter"),
      encodeName("Hud"),
      encodeName("JumpCircles"),
      encodeName("Light Debug"),
      encodeName("NameTags"),
      encodeName("RadiusDebug"),
      encodeName("Shield Breaker"),
      encodeName("SkinChanger"),
      encodeName("Spawner Notifier"),
      encodeName("SpearSwap"),
      encodeName("Spotify HUD"),
      encodeName("SwingSpeed"),
      encodeName("TabDetector"),
      encodeName("Triggerbot"),
      encodeName("WeatherNotifier"),
      encodeName("Hover Totem"),
      encodeName("Anchor Macro"),
      encodeName("Amethyst ESP"),
      encodeName("ActivityDebug"),
      encodeName("BoneDropper"),
      encodeName("AutoHitCrystal"),
      encodeName("Threesix"),
      encodeName("Initialisiere Threesix Client (Yarn Mappings)"),
      encodeName("CrackedByDexter"),
      encodeName("Auto Totem"),
      encodeName("AnchorMacro")
   };

   private ModuleNameStringPool() {
   }

   public static String getName(int intVal) {
      int[] local = encodedNames[intVal];
      char[] local2 = new char[local.length];

      for (int index = 0; index < local.length; index++) {
         local2[index] = (char)(local[index] ^ 28234);
      }

      return new String(local2);
   }

   private static int[] encodeName(String string) {
      int[] local = new int[string.length()];

      for (int index = 0; index < string.length(); index++) {
         local[index] = string.charAt(index) ^ 28234;
      }

      return local;
   }

}
