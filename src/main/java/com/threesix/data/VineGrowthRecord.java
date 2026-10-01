package com.threesix.data;

import net.minecraft.util.math.ChunkPos;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record VineGrowthRecord(ChunkPos chunkPos, int suspicionLevel, ChunkPos baseChunk, boolean extreme, boolean source, int maxVineLength) {

   

}
