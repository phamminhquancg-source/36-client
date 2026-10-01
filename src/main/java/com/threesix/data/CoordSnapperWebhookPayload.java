package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record CoordSnapperWebhookPayload(String webhook, String playerName, int x, int y, int z, String serverIp, String time, String skinRenderUrl) {

   public String oQ() {
      return "X: " + this.x + " Y: " + this.y + " Z: " + this.z;
   }

   public String oR() {
      return this.x + ", " + this.y + ", " + this.z;
   }

   

}
