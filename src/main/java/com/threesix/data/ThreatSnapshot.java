package com.threesix.data;

import net.minecraft.entity.player.PlayerEntity;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record ThreatSnapshot(boolean hasAnyEnemy, boolean hasCriticalThreat, PlayerEntity threat, double distance) {

   

}
