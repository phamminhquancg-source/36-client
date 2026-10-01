package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record HealthBarLayout(
   int baseHeartCount, int fullHearts, boolean halfHeart, int emptyHearts, int absorptionFullHearts, boolean absorptionHalfHeart, int totalWidth
) {

   

}
