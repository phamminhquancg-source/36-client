package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public enum AutoSellState {
   CHECK_HOTBAR,
   WAIT_ORDER_GUI,
   WAIT_TOTEM_SELECT,
   WAIT_CHEST_CONFIRM,
   WAIT_STASH,
   COLLECT_TO_HOTBAR;

}
