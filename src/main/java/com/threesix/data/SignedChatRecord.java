package com.threesix.data;

import java.util.List;
import net.minecraft.text.Text;
import com.threesix.data.ScoreboardEntryRecord;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record SignedChatRecord(Text title, List<ScoreboardEntryRecord> lines, String signature) {

   

}
