package com.threesix.setting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.client.network.ServerInfo.ServerType;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.module.FriendsModule;

public final class AutoLogSetting extends KeybindModuleBase {
   public static final long RECENT_COMBAT_WINDOW_MS = 20000L;
   public static final int RECENT_HIT_TICKS = 400;
   public static final double THREAT_SCAN_RADIUS_BLOCKS = 8.0;
   public static final long COMBAT_TEXT_COOLDOWN_MS = 1500L;
   public static final int NEARBY_PLAYER_SCAN_LIMIT = 4;
   public long lastHostileEventMillis;
   public long lastHostilePlayerMillis;
   public long lastCombatTextMillis;
   public float lastHealth = -1.0F;
   public int lastAttackerId = -1;
   public int lastAttackTargetId = -1;
   public String savedServerAddress;
   public int savedServerPort;
   public String savedServerBrand;
   public boolean isLeavingLogged = false;
   public long rejoinAtMillis = 0L;
   public static final long REJOIN_DELAY_MS = 3000L;

   public AutoLogSetting() {
      super("AutoLog", ModuleCategory.MISC);
   }

   @Override
   public void onActivationKey() {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         this.captureServerInfo();
         this.sendManualLeaveMessage();
         this.isLeavingLogged = true;
         this.rejoinAtMillis = System.currentTimeMillis() + 3000L;
      }
   }

   @Override
   public void onEnable() {

      this.lastHostileEventMillis = 0L;
      this.lastHostilePlayerMillis = 0L;
      this.lastCombatTextMillis = 0L;
      this.lastHealth = this.getCurrentHealth();
      this.lastAttackerId = this.getAttackerId();
      this.lastAttackTargetId = this.getAttackTargetId();
      if (minecraftClient.player != null && minecraftClient.world != null && (this.hasNearbyThreat() || this.isHostileAttackerPresent() || this.hasCombatTextIndicators())) {
         this.markAllHostileActivity(System.currentTimeMillis());
      }
   }

   @Override
   public void onTick() {
      if (this.isLeavingLogged && minecraftClient.player == null && minecraftClient.world == null) {
         if (System.currentTimeMillis() >= this.rejoinAtMillis && this.savedServerAddress != null) {
            this.isLeavingLogged = false;
            this.rejoinSavedServer();
         }
      } else if (minecraftClient.player != null && minecraftClient.world != null) {
         long systemValue = System.currentTimeMillis();
         float floatVal = this.getCurrentHealth();
         boolean flag = this.lastHealth >= 0.0F && floatVal + 0.001F < this.lastHealth;
         this.lastHealth = floatVal;
         if (minecraftClient.player.hurtTime > 0 || flag || this.hasNewAttacker()) {
            this.lastHostileEventMillis = systemValue;
         }

         boolean flag2 = this.hasCombatTextIndicators();
         if (flag2) {
            this.lastCombatTextMillis = systemValue;
         }

         if (minecraftClient.options.attackKey.isPressed()
            && minecraftClient.crosshairTarget != null
            && minecraftClient.crosshairTarget.getType() == Type.ENTITY
            && minecraftClient.crosshairTarget instanceof EntityHitResult local
            && local.getEntity() instanceof PlayerEntity local2
            && local2 != minecraftClient.player) {
            this.lastHostilePlayerMillis = systemValue;
         }

         if (this.hasNewAttackTarget()) {
            this.lastHostilePlayerMillis = systemValue;
         }

         if (!this.isSuppressedByRecentCombat(systemValue, flag2)) {
            for (PlayerEntity class1657 : minecraftClient.world.getPlayers()) {
               if (class1657 != minecraftClient.player
                  && !class1657.isSpectator()
                  && (!FriendsModule.isAutoLogEnabled() || !FriendsModule.isFriend(class1657.getName().getString()))) {
                  if (minecraftClient.getNetworkHandler() != null && minecraftClient.getNetworkHandler().getConnection() != null) {
                     this.captureServerInfo();
                     minecraftClient.getNetworkHandler()
                        .getConnection()
                        .disconnect(Text.literal("[AutoLog] Player detected: " + class1657.getName().getString()));
                     this.toggle();
                  }

                  return;
               }
            }
         }
      }
   }

   public void captureServerInfo() {
      if (minecraftClient.getCurrentServerEntry() != null) {
         ServerInfo minecraftClientValue = minecraftClient.getCurrentServerEntry();
         ServerAddress class639Value = ServerAddress.parse(minecraftClientValue.address);
         this.savedServerAddress = class639Value.getAddress();
         this.savedServerPort = class639Value.getPort();
         this.savedServerBrand = minecraftClientValue.name;
      }
   }

   public void sendManualLeaveMessage() {
      if (minecraftClient.getNetworkHandler() != null && minecraftClient.getNetworkHandler().getConnection() != null) {
         minecraftClient.getNetworkHandler().getConnection().disconnect(Text.literal("[AutoLog] Manual leave"));
      }
   }

   public void rejoinSavedServer() {

      if (this.savedServerAddress != null) {
         ServerInfo local = new ServerInfo(
            this.savedServerBrand != null ? this.savedServerBrand : this.savedServerAddress, this.savedServerAddress + ":" + this.savedServerPort, ServerType.OTHER
         );
         ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), minecraftClient, ServerAddress.parse(local.address), local, false, null);
      }
   }

   public boolean isSuppressedByRecentCombat(long longVal, boolean flag) {
      if (flag || this.isHostileAttackerPresent() || longVal - this.lastCombatTextMillis < 1500L) {
         return true;
      }

      if (this.lastHostileEventMillis <= 0L && this.lastHostilePlayerMillis <= 0L) {
         return false;
      }

      long maxValue = Math.max(this.lastHostileEventMillis, this.lastHostilePlayerMillis);
      return longVal - maxValue < 20000L;
   }

   public boolean hasNearbyThreat() {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         if (minecraftClient.player.hurtTime <= 0 && !this.isHostileAttackerPresent()) {
            if (minecraftClient.crosshairTarget != null
               && minecraftClient.crosshairTarget.getType() == Type.ENTITY
               && minecraftClient.crosshairTarget instanceof EntityHitResult local
               && local.getEntity() instanceof PlayerEntity local2
               && local2 != minecraftClient.player
               && !local2.isSpectator()) {
               return true;
            }

            double doubleVal = 64.0;

            for (PlayerEntity class1657 : minecraftClient.world.getPlayers()) {
               if (class1657 != minecraftClient.player && !class1657.isSpectator() && minecraftClient.player.squaredDistanceTo(class1657) <= doubleVal) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public void markAllHostileActivity(long longVal) {

      this.lastHostileEventMillis = longVal;
      this.lastHostilePlayerMillis = longVal;
      this.lastCombatTextMillis = longVal;
   }

   public boolean isHostileAttackerPresent() {
      return this.isHostileAttacker() || this.isHostileTarget();
   }

   public boolean hasNewAttacker() {

      int intVal = this.getAttackerId();
      if (intVal > 0 && intVal != this.lastAttackerId) {
         this.lastAttackerId = intVal;
         return this.isHostileAttacker();
      } else {
         return false;
      }
   }

   public boolean hasNewAttackTarget() {

      int intVal = this.getAttackTargetId();
      if (intVal > 0 && intVal != this.lastAttackTargetId) {
         this.lastAttackTargetId = intVal;
         return this.isHostileTarget();
      } else {
         return false;
      }
   }

   public boolean isHostileAttacker() {

      if (minecraftClient.player == null) {
         return false;
      } else {
         return minecraftClient.player.getLastAttacker() instanceof PlayerEntity local && local != minecraftClient.player && !local.isSpectator()
            ? this.isRecentHit(minecraftClient.player.getLastAttackedTime())
            : false;
      }
   }

   public boolean isHostileTarget() {
      if (minecraftClient.player == null) {
         return false;
      } else {
         return minecraftClient.player.getAttacking() instanceof PlayerEntity local && local != minecraftClient.player && !local.isSpectator()
            ? this.isRecentHit(minecraftClient.player.getLastAttackTime())
            : false;
      }
   }

   public boolean isRecentHit(int intVal) {
      if (minecraftClient.player != null && intVal > 0) {
         int minecraftClientValue = minecraftClient.player.age - intVal;
         return minecraftClientValue >= 0 && minecraftClientValue < 400;
      } else {
         return false;
      }
   }

   public int getAttackerId() {
      return minecraftClient.player != null ? minecraftClient.player.getLastAttackedTime() : -1;
   }

   public int getAttackTargetId() {
      return minecraftClient.player != null ? minecraftClient.player.getLastAttackTime() : -1;
   }

   public boolean hasCombatTextIndicators() {
      if (minecraftClient.world != null && this.packetContainsCombatText(minecraftClient.world.getScoreboard())) {
         return true;
      }

      Set collectionsValue = Collections.newSetFromMap(new IdentityHashMap());
      return minecraftClient.inGameHud != null && this.deepContainsCombatText(minecraftClient.inGameHud, 0, collectionsValue);
   }

   public boolean packetContainsCombatText(Scoreboard arg) {
      if (arg == null) {
         return false;
      }

      for (ScoreboardObjective class266 : arg.getObjectives()) {
         if (this.containsCombatKeyword(class266.getName()) || this.textContainsCombat(class266.getDisplayName())) {
            return true;
         }
      }

      for (ScoreboardDisplaySlot class8646 : ScoreboardDisplaySlot.values()) {
         ScoreboardObjective var1Value = arg.getObjectiveForSlot(class8646);
         if (var1Value != null) {
            if (this.containsCombatKeyword(var1Value.getName()) || this.textContainsCombat(var1Value.getDisplayName())) {
               return true;
            }

            for (ScoreboardEntry class9011 : arg.getScoreboardEntries(var1Value)) {
               if (this.containsCombatKeyword(class9011.owner()) || this.textContainsCombat(class9011.name()) || this.textContainsCombat(class9011.display())) {
                  return true;
               }

               Team var1Value2 = arg.getScoreHolderTeam(class9011.owner());
               if (this.isCombatTextComponent(var1Value2)) {
                  return true;
               }
            }
         }
      }

      for (Team class268 : arg.getTeams()) {
         if (this.isCombatTextComponent(class268)) {
            return true;
         }
      }

      return false;
   }

   public boolean isCombatTextComponent(Team arg) {
      return arg != null
         && (
            this.containsCombatKeyword(arg.getName())
               || this.textContainsCombat(arg.getDisplayName())
               || this.textContainsCombat(arg.getPrefix())
               || this.textContainsCombat(arg.getSuffix())
         );
   }

   public boolean deepContainsCombatText(Object object, int intVal, Set set) {

      if (object == null || intVal > 4) {
         return false;
      } else if (object instanceof Text local2) {
         return this.containsCombatKeyword(local2.getString());
      } else if (object instanceof String local3) {
         return this.containsCombatKeyword(local3);
      } else {
         if (!set.add(object)) {
            return false;
         }

         if (object instanceof Map local4) {
            for (Entry entry : (Iterable<Entry>)local4.entrySet()) {
               if (this.deepContainsCombatText(entry.getKey(), intVal + 1, set) || this.deepContainsCombatText(entry.getValue(), intVal + 1, set)) {
                  return true;
               }
            }

            return false;
         } else if (object instanceof Collection) {
            for (Object object2 : (Collection)object) {
               if (this.deepContainsCombatText(object2, intVal + 1, set)) {
                  return true;
               }
            }

            return false;
         } else {
            Class local = object.getClass();
            if (!this.isScannableClass(local)) {
               return false;
            }

            for (Class index = local; index != null && index != Object.class; index = index.getSuperclass()) {
               for (Field field : index.getDeclaredFields()) {
                  if (!Modifier.isStatic(field.getModifiers())
                     && !field.getType().isPrimitive()
                     && !field.getDeclaringClass().getName().startsWith("java.lang")) {
                     try {
                        field.setAccessible(true);
                        if (this.deepContainsCombatText(field.get(object), intVal + 1, set)) {
                           return true;
                        }
                     } catch (Exception error) {
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   public boolean isScannableClass(Class class2) {
      String local = class2.getName();
      return local.startsWith("net.minecraft.scoreboard.")
         || local.startsWith("net.minecraft.text.")
         || local.startsWith("net.minecraft.client.gui.hud.")
         || local.startsWith("net.minecraft.client.network.")
         || local.startsWith("java.util.");
   }

   public boolean containsCombatKeyword(String string) {
      return string != null && string.toLowerCase().contains("combat");
   }

   public boolean textContainsCombat(Text arg) {
      return arg != null && this.containsCombatKeyword(arg.getString());
   }

   public float getCurrentHealth() {
      return minecraftClient.player != null ? minecraftClient.player.getHealth() + minecraftClient.player.getAbsorptionAmount() : -1.0F;
   }

}
