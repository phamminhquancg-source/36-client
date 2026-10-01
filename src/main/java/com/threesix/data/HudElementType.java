package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public enum HudElementType {
   WATERMARK("vetrix+"),
   COORDINATES("Coordinates"),
   INFO("Info"),
   FPS("FPS"),
   PING("Ping"),
   TIME("Time"),
   MODULE_LIST("Module List"),
   POTION_EFFECTS("Potion Effects"),
   ARMOR("Armor"),
   KEYBINDS("Keybinds"),
   SPOTIFY_HUD("Spotify HUD"),
   RADAR("Radar"),
   STAFF_LIST("Staff List"),
   REGION_MAP("Region Map");

   public final String label;

   HudElementType(String string) {
      this.label = string;
   }

}
