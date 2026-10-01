package com.threesix.internal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import com.threesix.data.ModuleCategory;
import com.threesix.setting.ClientSetting;
import com.threesix.module.HudModule;

public abstract class ModuleBase {
   public final String name;
   public final ModuleCategory category;
   public boolean enabled;
   public int keyCode = 0;
   public boolean expanded = false;
   public boolean keyPressed = false;
   public final List<ClientSetting> settings = new ArrayList<>();
   public static final MinecraftClient minecraftClient = MinecraftClient.getInstance();

   public static boolean isAuthorized() {
      try {
         return true;
      } catch (Exception error) {
         return true;
      }
   }

   public ModuleBase(String string, ModuleCategory moduleCategory) {
      this.name = string;
      this.category = moduleCategory;
      this.enabled = false;
   }

   public void registerSetting(ClientSetting clientSetting) {
      this.settings.add(clientSetting);
   }

   public List<ClientSetting> getSettings() {
      return this.settings;
   }

   public String getName2() {
      return this.name;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public void setEnabled(boolean flag) {
      boolean enabledSnapshot = this.enabled;
      if (flag && !isAuthorized()) {
         this.enabled = false;
      } else {
         this.enabled = flag;
         if (flag) {
            this.onEnable();
         } else {
            this.onDisable();
         }
      }

      if (enabledSnapshot != this.enabled) {
         try {
            HudModule.call18(this, this.enabled);
         } catch (Throwable error) {
         }
      }
   }

   public ItemStack getDisplayName() {
      Item local = getFormatForModule(this.name);
      return local != null && local != Items.AIR ? new ItemStack(local) : new ItemStack(this.getDefaultFormat());
   }

   public static Item getFormatForModule(String string) {
      String local = string.toLowerCase().replace(" ", "_");

      return switch (local) {
         case "fullbright" -> Items.GLOWSTONE;
         case "storage_esp" -> Items.CHEST;
         case "nametags" -> Items.NAME_TAG;
         case "sprint" -> Items.FEATHER;
         case "freecam" -> Items.ENDER_EYE;
         case "killaura" -> Items.DIAMOND_SWORD;
         case "auto_crystal" -> Items.END_CRYSTAL;
         case "triggerbot" -> Items.BOW;
         case "hitbox" -> Items.BARRIER;
         case "auto_totem" -> Items.TOTEM_OF_UNDYING;
         case "hover_totem" -> Items.TOTEM_OF_UNDYING;
         case "auto_inv_totem" -> Items.TOTEM_OF_UNDYING;
         case "elytra_swap" -> Items.ELYTRA;
         case "shield_breaker" -> Items.SHIELD;
         case "anchor_macro" -> Items.RESPAWN_ANCHOR;
         case "mace_swap" -> Items.MACE;
         case "double_anchor" -> Items.RESPAWN_ANCHOR;
         case "auto_double_hand" -> Items.SHIELD;
         case "speraswap" -> Items.MACE;
         case "freelook" -> Items.SPYGLASS;
         case "skinscraper" -> Items.LEATHER_CHESTPLATE;
         case "skin_changer" -> Items.LEATHER_CHESTPLATE;
         case "auto_tool" -> Items.DIAMOND_PICKAXE;
         case "fast_place" -> Items.PISTON;
         case "coordsnapper" -> Items.COMPASS;
         case "nameprotect" -> Items.BOOK;
         case "autolog" -> Items.PAPER;
         case "autotpa" -> Items.ENDER_PEARL;
         case "fakepos" -> Items.MAP;
         case "tab_detector" -> Items.PLAYER_HEAD;
         case "chat_macro" -> Items.WRITABLE_BOOK;
         case "weather_notifier" -> Items.LIGHTNING_ROD;
         case "spawner_notifier" -> Items.SPAWNER;
         case "block_esp" -> Items.GLASS;
         case "spawner_protect" -> Items.SPAWNER;
         case "homesetter" -> Items.RED_BED;
         case "swing_speed" -> Items.CLOCK;
         case "hud" -> Items.MAP;
         case "threesix_+" -> Items.WATER_BUCKET;
         case "friends" -> Items.PLAYER_HEAD;
         case "fakeroles" -> Items.PAPER;
         case "fakestats" -> Items.PAPER;
         case "antitrap" -> Items.TRIPWIRE_HOOK;
         case "activitydebug" -> Items.DEBUG_STICK;
         case "bonedropper" -> Items.BONE;
         case "auto_sell" -> Items.GOLD_INGOT;
         case "autosell" -> Items.GOLD_INGOT;
         case "auto_place" -> Items.DISPENSER;
         case "autoplace" -> Items.DISPENSER;
         case "auto_chunk_loader" -> Items.ENDER_CHEST;
         case "sus_chunk_finder" -> Items.SUSPICIOUS_SAND;
         case "grow_finder" -> Items.KELP;
         case "sigma_chunk_finder" -> Items.LIME_CONCRETE;
         case "chunk_finder" -> Items.LIME_CONCRETE;
         case "radiusdebug" -> Items.STICK;
         case "spotify_hud" -> Items.JUKEBOX;
         default -> null;
      };
   }

   public Item getDefaultFormat() {
      return switch (this.category) {
         case COMBAT -> Items.DIAMOND_SWORD;
         case RENDER -> Items.ENDER_EYE;
         case MISC -> Items.COMPASS;
         case CLIENT -> Items.WATER_BUCKET;
         default -> Items.PAPER;
      };
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void toggle() {
      this.setEnabled(!this.enabled);
   }

   public void toggleByKey() {
      this.toggle();
   }

   public int getKeyCode() {
      return this.keyCode;
   }

   public void setKeyCode(int intVal) {
      this.keyCode = intVal >= 0 && intVal <= 348 ? intVal : 0;
   }

   public void setKeyCodeFromConfig(int intVal) {
      this.keyCode = intVal >= 0 && intVal <= 348 ? intVal : 0;
   }

   public void setEnabledRaw(boolean flag) {
      this.enabled = flag;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public void setExpanded(boolean flag) {
      this.expanded = flag;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onTick() {
   }

   public void onRender(MatrixStack arg, float floatVal) {
   }

   public void onPacketSend(Packet arg) {
   }

   public boolean onPacketReceive(Packet arg) {
      return false;
   }

   public static String getSeparator() {
      return "_";
   }
}
