package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record WebhookAlertPayload(
   String webhook,
   String title,
   String description,
   int value,
   String playerName,
   String threatName,
   String distance,
   int spawnersInInventory,
   boolean allMined,
   String serverIp,
   String time,
   String skinRenderUrl,
   String disconnectReason
) {

   

}
