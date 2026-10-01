package com.threesix.module;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.client.network.PlayerListEntry;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.setting.ModeSetting02;
import com.threesix.manager.NotificationHudManager;

public final class TabDetectorModule extends ModuleBase {
   public final ModeSetting02 detectSetting = new ModeSetting02("Detect", "List", "Any", "List");
   public final ClientSetting targetPlayersSetting = new ClientSetting("Target Players", "");
   public final ModeSetting02 notificationModeSetting = new ModeSetting02("Notification Mode", "Both", "Chat", "Toast", "Both");
   public final ClientSetting logOfflineSetting = new ClientSetting("Log Offline", true);
   public final Set currentTargets = new HashSet();
   public final Set previousTargets = new HashSet();

   public TabDetectorModule() {
      super("TabDetector", ModuleCategory.MISC);
      this.targetPlayersSetting.withVisibility(() -> {
         return this.detectSetting.isModeSelected2("List");
      });
      this.registerSetting(this.detectSetting);
      this.registerSetting(this.targetPlayersSetting);
      this.registerSetting(this.notificationModeSetting);
      this.registerSetting(this.logOfflineSetting);
   }

   @Override
   public void onEnable() {
      this.currentTargets.clear();
      this.previousTargets.clear();
      this.refreshPreviousTargets(this.previousTargets);
   }

   @Override
   public void onDisable() {
      this.currentTargets.clear();
      this.previousTargets.clear();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && minecraftClient.world != null && minecraftClient.getNetworkHandler() != null) {
         boolean flag = this.detectSetting.isModeSelected2("Any");
         Set local = flag ? Set.of() : this.parseTargetNames((String)this.targetPlayersSetting.getValue());
         if (!flag && local.isEmpty()) {
            this.currentTargets.clear();
            this.previousTargets.clear();
         } else {
            this.currentTargets.clear();

            for (PlayerListEntry class640 : minecraftClient.getNetworkHandler().getPlayerList()) {
               String local2 = this.getEntryPlayerName(class640);
               if (!local2.isEmpty()
                  && (minecraftClient.player == null || !local2.equalsIgnoreCase(minecraftClient.player.getName().getString()))
                  && (flag || this.matchesTargetName(local, local2))) {
                  this.currentTargets.add(local2);
               }
            }

            HashSet hashSetInst = new HashSet(this.currentTargets);
            hashSetInst.removeAll(this.previousTargets);
            if (!hashSetInst.isEmpty()) {
               this.notifyPlayersJoined(hashSetInst);
            }

            if ((Boolean)this.logOfflineSetting.getValue()) {
               HashSet hashSetInst2 = new HashSet(this.previousTargets);
               hashSetInst2.removeAll(this.currentTargets);
               if (!hashSetInst2.isEmpty()) {
                  this.notifyPlayersLeft(hashSetInst2);
               }
            }

            this.previousTargets.clear();
            this.previousTargets.addAll(this.currentTargets);
         }
      }
   }

   public void notifyPlayersJoined(Set set) {

      String stringValue = String.join(", ", set);
      String local = set.size() == 1 ? "Target player joined: " + stringValue : "Target players joined: " + stringValue;
      this.notifyPresence(local, set.size() == 1 ? "Target Player Joined!" : "Target Players Joined!", -1938838);
   }

   public void notifyPlayersLeft(Set set) {
      String stringValue = String.join(", ", set);
      String local = set.size() == 1 ? "Target player left: " + stringValue : "Target players left: " + stringValue;
      this.notifyPresence(local, set.size() == 1 ? "Target Player Left!" : "Target Players Left!", -11152222);
   }

   public void notifyPresence(String string, String string2, int intVal) {
      String local = (String)this.notificationModeSetting.getValue();
      int intVal2 = !"Chat".equalsIgnoreCase(local) && !"Both".equalsIgnoreCase(local) ? 0 : 1;
      boolean flag = "Toast".equalsIgnoreCase(local) || "Both".equalsIgnoreCase(local);
      if (intVal2 != 0) {
         try {
            minecraftClient.inGameHud.getChatHud().addMessage(Text.literal("[TabDetector] " + string));
         } catch (Throwable error) {
         }
      }

      if (flag) {
         NotificationHudManager.INSTANCE6.push("TabDetector", string2, ItemStack.EMPTY, intVal);
      }
   }

   public void refreshPreviousTargets(Set set) {

      set.clear();
      if (minecraftClient.getNetworkHandler() != null) {
         boolean flag = this.detectSetting.isModeSelected2("Any");
         Set local = flag ? Set.of() : this.parseTargetNames((String)this.targetPlayersSetting.getValue());
         if (flag || !local.isEmpty()) {
            for (PlayerListEntry class640 : minecraftClient.getNetworkHandler().getPlayerList()) {
               String local2 = this.getEntryPlayerName(class640);
               if (!local2.isEmpty()
                  && (minecraftClient.player == null || !local2.equalsIgnoreCase(minecraftClient.player.getName().getString()))
                  && (flag || this.matchesTargetName(local, local2))) {
                  set.add(local2);
               }
            }
         }
      }
   }

   public String getEntryPlayerName(PlayerListEntry arg) {
      try {
         if (arg != null && arg.getProfile() != null) {
            try {
               String var1Value = arg.getProfile().name();
               return var1Value == null ? "" : var1Value;
            } catch (Throwable error) {
               return "";
            }
         } else {
            return "";
         }
      } catch (Throwable error2) {
         return "";
      }
   }

   public boolean matchesTargetName(Set set, String string) {
      return set.contains(string.toLowerCase(Locale.ROOT));
   }

   public Set parseTargetNames(String string) {
      if (string != null && !string.isBlank()) {
         String local = string.replace('\n', ',').replace('\r', ',');
         LinkedHashSet linkedHashSetInst = new LinkedHashSet();

         for (String string2 : local.split(",")) {
            String local2 = string2 == null ? "" : string2.trim();
            if (!local2.isEmpty()) {
               linkedHashSetInst.add(local2.toLowerCase(Locale.ROOT));
            }
         }

         return linkedHashSetInst;
      } else {
         return Set.of();
      }
   }

}
