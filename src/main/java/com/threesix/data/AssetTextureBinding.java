package com.threesix.data;

import net.minecraft.util.AssetInfo.TextureAsset;
import com.threesix.util.XorBitUtils;
import com.threesix.data.PlayerSkinInfo;
import com.threesix.util.StringVaultDecoder;

public record AssetTextureBinding(PlayerSkinInfo lookup, TextureAsset textureAsset) {

   

}
