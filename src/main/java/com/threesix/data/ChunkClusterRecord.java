package com.threesix.data;

import java.util.Set;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record ChunkClusterRecord(Set<Long> members, double centroidX, double centroidZ, double totalScore, double maxScore, int radius) {

   

}
