package com.threesix.data;

import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record LabelRenderEntry(Object obj, Vec3d labelPos, Text nameLabel, Text healthLabel, List<Module> items) {

   

}
