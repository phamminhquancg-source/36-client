package com.threesix.module;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.text.MutableText;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import com.threesix.data.ScoreboardEntryRecord;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.StyledTextPart;
import com.threesix.data.ModuleCategory;
import com.threesix.data.SignedChatRecord;
import com.threesix.data.TabStatCache;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class FakeStatsModule extends ModuleBase {
   public static FakeStatsModule instance;
   public static final String fakePlayerName = "threesix_fake_stats";
   public static final int maxRows = 15;
   public final ClientSetting moneySetting = new ClientSetting("Money", "0");
   public final ClientSetting shardsSetting = new ClientSetting("Shards", "0");
   public final ClientSetting killsSetting = new ClientSetting("Kills", "0");
   public final ClientSetting deathsSetting = new ClientSetting("Deaths", "0");
   public final ClientSetting playtimeSetting = new ClientSetting("Playtime", "0m");
   public final Random random = new Random();
   public ScoreboardObjective fakePlayer;
   public String profileName;
   public ScoreboardObjective scoreboardHolder;
   public TabStatCache tabCache;
   public Object lastNetworkHandler;
   public String lastSignature = "";
   public boolean isDirty;
   public long lastUpdateMillis = 0L;
   public static final long updateIntervalMs = 500L;

   public static FakeStatsModule call1() {
      return instance;
   }

   public FakeStatsModule() {
      super("FakeStats", ModuleCategory.DONUT);
      instance = this;
      this.registerSetting(this.moneySetting);
      this.registerSetting(this.shardsSetting);
      this.registerSetting(this.killsSetting);
      this.registerSetting(this.deathsSetting);
      this.registerSetting(this.playtimeSetting);
   }

   @Override
   public void onEnable() {

      this.lastNetworkHandler = minecraftClient.world;
      this.fakePlayer = null;
      this.profileName = null;
      this.scoreboardHolder = null;
      this.lastSignature = "";
      this.randomizeStats();
      this.rebuildTabCache();
      this.isDirty = true;
   }

   @Override
   public void onDisable() {

      this.lastNetworkHandler = null;
      this.lastSignature = "";
      this.isDirty = false;
      ScoreboardObjective fakePlayerSnapshot = this.fakePlayer;
      ScoreboardObjective scoreboardHolderSnapshot = this.scoreboardHolder;
      this.fakePlayer = null;
      this.profileName = null;
      this.scoreboardHolder = null;
      if (minecraftClient.world != null) {
         Scoreboard minecraftClientValue = minecraftClient.world.getScoreboard();

         try {
            if (scoreboardHolderSnapshot != null) {
               minecraftClientValue.removeObjective(scoreboardHolderSnapshot);
            }

            if (fakePlayerSnapshot != null && minecraftClientValue.getObjectives().contains(fakePlayerSnapshot)) {
               minecraftClientValue.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, fakePlayerSnapshot);
            }
         } catch (Exception error) {
         }
      }
   }

   @Override
   public void onTick() {

      if (minecraftClient.world == null) {
         this.fakePlayer = null;
         this.profileName = null;
         this.scoreboardHolder = null;
         this.lastNetworkHandler = null;
         this.lastSignature = "";
      } else {
         if (minecraftClient.world != this.lastNetworkHandler) {
            this.fakePlayer = null;
            this.profileName = null;
            this.scoreboardHolder = null;
            this.lastSignature = "";
            this.lastNetworkHandler = minecraftClient.world;
            this.isDirty = true;
         }

         this.findFakePlayer();
         if (this.fakePlayer != null) {
            Scoreboard minecraftClientValue = minecraftClient.world.getScoreboard();
            if (!minecraftClientValue.getObjectives().contains(this.fakePlayer)) {
               this.fakePlayer = null;
               this.findFakePlayer();
               if (this.fakePlayer == null) {
                  return;
               }
            }

            SignedChatRecord local = this.buildSignedRecord(minecraftClientValue, this.fakePlayer);
            if (local != null) {
               this.syncTabCache();
               if (!local.signature().equals(this.lastSignature) || this.scoreboardHolder == null) {
                  this.isDirty = true;
               }

               this.applyRecord(minecraftClientValue, local);
               if (this.scoreboardHolder != null && minecraftClientValue.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) != this.scoreboardHolder) {
                  minecraftClientValue.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.scoreboardHolder);
               }
            }
         }
      }
   }

   public void randomizeStats() {
      this.moneySetting.setValue(this.formatNumber(this.randomBetween(10000L, 5000000000L)));
      this.shardsSetting.setValue(this.formatNumber(this.randomBetween(0L, 2500000L)));
      this.killsSetting.setValue(String.valueOf(this.randomBetween(0L, 2000L)));
      this.deathsSetting.setValue(String.valueOf(this.randomBetween(0L, 1000L)));
      this.playtimeSetting.setValue(this.formatPlaytime(this.randomBetween(0L, 15552000L)));
   }

   public void rebuildTabCache() {

      this.tabCache = new TabStatCache(
         this.orDefault((String)this.moneySetting.getValue(), "0"),
         this.orDefault((String)this.shardsSetting.getValue(), "0"),
         this.orDefault((String)this.killsSetting.getValue(), "0"),
         this.orDefault((String)this.deathsSetting.getValue(), "0"),
         this.orDefault((String)this.playtimeSetting.getValue(), "0m")
      );
      this.isDirty = true;
   }

   public void syncTabCache() {

      TabStatCache tabStatCacheInst = new TabStatCache(
         this.orDefault((String)this.moneySetting.getValue(), "0"),
         this.orDefault((String)this.shardsSetting.getValue(), "0"),
         this.orDefault((String)this.killsSetting.getValue(), "0"),
         this.orDefault((String)this.deathsSetting.getValue(), "0"),
         this.orDefault((String)this.playtimeSetting.getValue(), "0m")
      );
      if (this.tabCache == null || !this.tabCache.lQ().equals(tabStatCacheInst.lQ())) {
         this.tabCache = tabStatCacheInst;
         this.isDirty = true;
      }
   }

   public void findFakePlayer() {
      if (minecraftClient.world != null) {
         Scoreboard minecraftClientValue = minecraftClient.world.getScoreboard();
         ScoreboardObjective var1Value = minecraftClientValue.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
         if (var1Value != null && !"threesix_fake_stats".equals(var1Value.getName())) {
            this.fakePlayer = var1Value;
            this.profileName = var1Value.getName();
         } else if (this.fakePlayer == null || !minecraftClientValue.getObjectives().contains(this.fakePlayer)) {
            if (this.profileName != null) {
               ScoreboardObjective var1Value2 = minecraftClientValue.getNullableObjective(this.profileName);
               if (var1Value2 != null && !"threesix_fake_stats".equals(var1Value2.getName())) {
                  this.fakePlayer = var1Value2;
                  return;
               }
            }

            for (ScoreboardObjective class266 : minecraftClientValue.getObjectives()) {
               if (!"threesix_fake_stats".equals(class266.getName())) {
                  this.fakePlayer = class266;
                  this.profileName = class266.getName();
                  return;
               }
            }
         }
      }
   }

   public SignedChatRecord buildSignedRecord(Scoreboard arg, ScoreboardObjective arg2) {

      ArrayList<ScoreboardEntryRecord> arrayListInst = new ArrayList<>();
      ArrayList<ScoreboardEntry> arrayListInst2 = new ArrayList<>(arg.getScoreboardEntries(arg2));
      arrayListInst2.removeIf(ScoreboardEntry::hidden);
      arrayListInst2.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed());
      if (arrayListInst2.size() > 15) {
         arrayListInst2 = new ArrayList<>(arrayListInst2.subList(0, 15));
      }

      for (ScoreboardEntry class9011 : arrayListInst2) {
         arrayListInst.add(new ScoreboardEntryRecord(class9011.value(), this.buildLine(arg, class9011)));
      }

      MutableText var2Value = arg2.getDisplayName() != null ? arg2.getDisplayName().copy() : Text.literal("Donut SMP");
      StringBuilder stringBuilderInst = new StringBuilder(var2Value.getString());

      for (ScoreboardEntryRecord scoreboardEntryRecord : arrayListInst) {
         stringBuilderInst.append('\n').append(scoreboardEntryRecord.value()).append(':').append(scoreboardEntryRecord.text().getString());
      }

      stringBuilderInst.append('\n').append(this.tabCache != null ? this.tabCache.lQ() : "");
      return new SignedChatRecord(var2Value, arrayListInst, stringBuilderInst.toString());
   }

   public Text buildLine(Scoreboard arg, ScoreboardEntry arg2) {
      if (arg2.display() != null) {
         return arg2.display().copy();
      }

      MutableText var2Value = arg2.name() != null ? arg2.name().copy() : Text.literal(arg2.owner());
      Team var1Value = arg.getScoreHolderTeam(arg2.owner());
      return Team.decorateName(var1Value, var2Value).copy();
   }

   public void applyRecord(Scoreboard arg, SignedChatRecord signedChatRecord) {
      if (this.isDirty && this.tabCache != null) {
         long systemValue = System.currentTimeMillis();
         if (systemValue - this.lastUpdateMillis >= 500L) {
            this.lastUpdateMillis = systemValue;
            ScoreboardObjective var1Value = arg.getNullableObjective("threesix_fake_stats");
            if (var1Value != null) {
               arg.removeObjective(var1Value);
            }

            this.scoreboardHolder = arg.addObjective(
               "threesix_fake_stats", ScoreboardCriterion.DUMMY, signedChatRecord.title().copy(), RenderType.INTEGER, true, BlankNumberFormat.INSTANCE
            );
            arg.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.scoreboardHolder);
            List local = signedChatRecord.lines();
            int local4 = 0;
            for (int index = 0; index < local.size(); index++) {
               ScoreboardEntryRecord local2 = (ScoreboardEntryRecord)local.get(index);
               ScoreHolder class9015Value = ScoreHolder.fromName("fake_stats_line_" + index);
               ScoreAccess var1Value2 = arg.getOrCreateScore(class9015Value, this.scoreboardHolder);
               var1Value2.setScore(local.size() - index);
               String local3 = local2.text().getString().trim();
               boolean flag = local3.matches(".*\\d.*");
               if (flag) {
                  var1Value2.setDisplayText(this.rewriteLine(local2.text(), local4));
                  local4++;
               } else {
                  var1Value2.setDisplayText(local2.text().copy());
               }

               var1Value2.setNumberFormat(BlankNumberFormat.INSTANCE);
            }

            this.lastSignature = signedChatRecord.signature();
            this.isDirty = false;
         }
      }
   }

   public Text rewriteLine(Text arg, int intVal) {
      String[] local4 = new String[]{
         this.tabCache.money(), this.tabCache.shards(), this.tabCache.kills(), this.tabCache.deaths(), this.tabCache.playtime()
      };
      if (intVal >= local4.length) {
         return arg.copy();
      }

      String local = local4[intVal];
      List<StyledTextPart> local2 = this.getMatchedParts(arg);
      String local3 = arg.getString();
      int var8Snapshot = -1;
      for (int index = 0; index < local3.length(); index++) {
         char charVal = local3.charAt(index);
         if (Character.isDigit(charVal) || charVal == '-' && index + 1 < local3.length() && Character.isDigit(local3.charAt(index + 1))) {
            var8Snapshot = index;
            break;
         }
      }

      if (var8Snapshot >= 0) {
         MutableText class2561Value = Text.empty();
         this.styleParts(class2561Value, local2, var8Snapshot);
         class2561Value.append(Text.literal(local).setStyle(this.getPartStyle(local2, var8Snapshot)));
         return class2561Value;
      }

      MutableText class2561Value2 = Text.empty();

      for (StyledTextPart styledTextPart : local2) {
         class2561Value2.append(Text.literal(styledTextPart.str()).setStyle(styledTextPart.style()));
      }

      return class2561Value2;
   }

   public List<StyledTextPart> getMatchedParts(Text arg) {
      ArrayList<StyledTextPart> arrayListInst = new ArrayList<>();
      arg.visit((local, local2) -> {
         if (!local2.isEmpty()) {
            arrayListInst.add(new StyledTextPart(local2, local));
         }

         return Optional.empty();
      }, Style.EMPTY);
      return arrayListInst;
   }

   public void styleParts(MutableText arg, List<StyledTextPart> list, int intVal) {
      int maxValue = Math.max(0, intVal);

      for (StyledTextPart styledTextPart : list) {
         if (maxValue <= 0) {
            return;
         }

         String local = styledTextPart.str();
         int minValue = Math.min(local.length(), maxValue);
         arg.append(Text.literal(local.substring(0, minValue)).setStyle(styledTextPart.style()));
         maxValue -= minValue;
      }
   }

   public Style getPartStyle(List<StyledTextPart> list, int intVal) {
      int maxValue = Math.max(0, intVal);
      Style local = Style.EMPTY;

      for (StyledTextPart styledTextPart : list) {
         if (!styledTextPart.str().isEmpty()) {
            local = styledTextPart.style();
         }

         if (maxValue < styledTextPart.str().length()) {
            return styledTextPart.style();
         }

         maxValue -= styledTextPart.str().length();
      }

      return local;
   }

   public void removeFakeScoreboard() {
      if (minecraftClient.world != null) {
         Scoreboard minecraftClientValue = minecraftClient.world.getScoreboard();
         ScoreboardObjective var1Value = minecraftClientValue.getNullableObjective("threesix_fake_stats");
         if (var1Value != null) {
            minecraftClientValue.removeObjective(var1Value);
         }

         if (this.fakePlayer != null && minecraftClientValue.getObjectives().contains(this.fakePlayer)) {
            minecraftClientValue.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.fakePlayer);
         }
      }
   }

   public long randomBetween(long longVal, long longVal2) {
      return longVal >= longVal2 ? longVal : longVal + (long)Math.floor(this.random.nextDouble() * (longVal2 - longVal + 1L));
   }

   public String formatNumber(long longVal) {
      long absValue = Math.abs(longVal);
      if (absValue < 1000L) {
         return Long.toString(longVal);
      } else if (absValue < 1000000L) {
         return this.formatScaled(longVal / 1000.0, "K");
      } else {
         return absValue < 1000000000L ? this.formatScaled(longVal / 1000000.0, "M") : this.formatScaled(longVal / 1.0E9, "B");
      }
   }

   public String formatScaled(double doubleVal, String string) {
      String local = doubleVal >= 100.0 ? "%.0f%s" : (doubleVal >= 10.0 ? "%.1f%s" : "%.2f%s");
      return String.format(Locale.US, local, doubleVal, string);
   }

   public String formatPlaytime(long longVal) {

      long var13600LValue = longVal / 3600L;
      long var324LValue = var13600LValue / 24L;
      long longVal2 = var13600LValue % 24L;
      long longVal3 = longVal % 3600L / 60L;
      if (var324LValue > 0L) {
         return String.format(Locale.US, "%dd %dh", var324LValue, longVal2);
      } else {
         return var13600LValue > 0L ? String.format(Locale.US, "%dh %dm", var13600LValue, longVal3) : String.format(Locale.US, "%dm", longVal3);
      }
   }

   public String orDefault(String string, String string2) {
      if (string == null) {
         return string2;
      }

      String local = string.trim();
      return local.isEmpty() ? string2 : local;
   }

   public Text call2(Text arg) {

      if (this.tabCache == null) {
         return arg;
      }

      String local = arg.getString();
      Pattern patternValue = Pattern.compile("(\\$\\s*)([0-9][0-9.,]*[KkMmBbTt]?)");
      Matcher local2 = patternValue.matcher(local);
      if (!local2.find()) {
         return arg;
      }

      String local3 = local.substring(0, local2.start(2)) + this.tabCache.money() + local.substring(local2.end(2));
      List local4 = this.getMatchedParts(arg);
      Style local5 = local4.isEmpty() ? Style.EMPTY : ((StyledTextPart)local4.get(0)).style();
      return Text.literal(local3).setStyle(local5);
   }

}
