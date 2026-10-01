package com.threesix.data;

import com.threesix.util.StringVaultDecoder;

public record SpotifyTrackState(
   boolean active, String title, String artist, long posMs, long durMs, boolean playing, boolean canSeek, int artVersion, int volume, long stampNs
) {
   public static final SpotifyTrackState INACTIVE = new SpotifyTrackState(false, "", "", 0L, 0L, false, false, 0, -1, 0L);

   public long livePosMs() {
      if (!this.active) {
         return 0L;
      }

      long posMsSnapshot = this.posMs;
      if (this.playing) {
         posMsSnapshot += (System.nanoTime() - this.stampNs) / 1000000L;
      }

      if (this.durMs > 0L && posMsSnapshot > this.durMs) {
         posMsSnapshot = this.durMs;
      }

      return Math.max(0L, posMsSnapshot);
   }

   

}
