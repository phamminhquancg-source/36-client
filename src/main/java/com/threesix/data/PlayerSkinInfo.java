package com.threesix.data;

import java.util.UUID;
import net.minecraft.entity.player.PlayerSkinType;
import com.threesix.util.StringVaultDecoder;

public record PlayerSkinInfo(String playerName, UUID uuid, String textureUrl, PlayerSkinType skinType) {

   

}
