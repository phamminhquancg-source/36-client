package com.threesix.data;

import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public record TabStatCache(String money, String shards, String kills, String deaths, String playtime) {

   public String lQ() {
      return this.money + "|" + this.shards + "|" + this.kills + "|" + this.deaths + "|" + this.playtime;
   }

   

}
