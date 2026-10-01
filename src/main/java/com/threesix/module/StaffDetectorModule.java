package com.threesix.module;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.minecraft.world.GameMode;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TextColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.module.HudModule;
import com.threesix.util.GuiRenderUtil;
import com.threesix.data.ClientFontType;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.data.HudElementType;
import com.threesix.data.HudTextEntry;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.setting.ModeSetting02;
import com.threesix.data.NameCountPair;
import com.threesix.manager.CustomFontManager;

public final class StaffDetectorModule extends ModuleBase {
   public final ClientSetting chatAlertSetting = new ClientSetting("Chat Alert", true);
   public final ClientSetting hudSetting = new ClientSetting("HUD", true);
   public final ModeSetting02 detectModeSetting = new ModeSetting02("Detect Mode", "Star + Rank", "Star + Rank", "Star Only", "Rank Only", "Names Only");
   public final ClientSetting vanishedStaffSetting = new ClientSetting("Vanished Staff", true);
   public final ClientSetting fontIconsSetting = new ClientSetting("Font Icons", true);
   public final ClientSetting hudScaleSetting = new ClientSetting("HUD Scale", 1.0, 0.5, 2.0);
   public static final String starGlyphs = "★☆✦✧✪✩✫✬✭✮✯⭐✰❂⚝✴✵✶✷✸✹⍟";
   public static final List<String> rankKeywords = Arrays.asList(
      "coowner",
      "owner",
      "manager",
      "administrator",
      "admin",
      "developer",
      "dev",
      "srmod",
      "seniormod",
      "moderator",
      "mod",
      "srhelper",
      "seniorhelper",
      "helper",
      "trialmod",
      "trial",
      "builder",
      "support",
      "staff",
      "community",
      "junior",
      "head",
      "operator",
      "sentinel",
      "guard"
   );
   public static final Map rankLabels = new HashMap();
   public static final Set friendNames = new HashSet<>(Arrays.asList("donutsmp", "donut", "notsobot"));
   public final Map<String, String> detectedStaff = new LinkedHashMap();
   public final Map<String, Integer> staffRanks = new LinkedHashMap();
   public final Set<String> vanishedPlayers = new HashSet();
   public final Set<String> alertedPlayers = new HashSet();
   public final Map<String, Identifier> staffSkins = new LinkedHashMap();
   public int scanCounter = 0;
   public boolean hasAnyStaff = false;
   public static StaffDetectorModule instance;

   public StaffDetectorModule() {
      super("Staff Detector", ModuleCategory.DONUT);
      this.registerSetting(this.chatAlertSetting);
      this.registerSetting(this.hudSetting);
      this.registerSetting(this.detectModeSetting);
      this.registerSetting(this.vanishedStaffSetting);
      this.registerSetting(this.fontIconsSetting);
      this.registerSetting(this.hudScaleSetting);
      instance = this;
   }

   @Override
   public void onEnable() {
      this.detectedStaff.clear();
      this.staffRanks.clear();
      this.vanishedPlayers.clear();
      this.alertedPlayers.clear();
      this.staffSkins.clear();
      this.scanCounter = 0;
      this.hasAnyStaff = false;
   }

   @Override
   public void onDisable() {
      this.detectedStaff.clear();
      this.staffRanks.clear();
      this.vanishedPlayers.clear();
      this.alertedPlayers.clear();
      this.staffSkins.clear();
      this.hasAnyStaff = false;
   }

   @Override
   public void onTick() {
      if (minecraftClient.world != null && minecraftClient.player != null && minecraftClient.getNetworkHandler() != null && ++this.scanCounter % 20 == 0) {
         HashSet hashSetInst = new HashSet();
         HashSet hashSetInst2 = new HashSet();

         try {
            for (PlayerListEntry class640 : minecraftClient.getNetworkHandler().getListedPlayerListEntries()) {
               hashSetInst2.add(class640.getProfile().id());
            }
         } catch (Throwable error) {
         }

         String minecraftClientValue = minecraftClient.player.getName().getString();
         Iterator minecraftClientValue2 = minecraftClient.getNetworkHandler().getPlayerList().iterator();

         while (true) {
            PlayerListEntry local;
            String var5Value;
            UUID var5Value2;
            while (true) {
               if (!minecraftClientValue2.hasNext()) {
                  this.hasAnyStaff = true;
                  this.alertedPlayers.retainAll(hashSetInst);
                  this.detectedStaff.keySet().retainAll(hashSetInst);
                  this.staffRanks.keySet().retainAll(hashSetInst);
                  this.vanishedPlayers.retainAll(hashSetInst);
                  this.staffSkins.keySet().retainAll(hashSetInst);
                  return;
               }

               local = (PlayerListEntry)minecraftClientValue2.next();

               try {
                  var5Value = local.getProfile().name();
                  var5Value2 = local.getProfile().id();
                  break;
               } catch (Throwable error2) {
               }
            }

            if (var5Value != null && !var5Value.isEmpty() && !var5Value.equalsIgnoreCase(minecraftClientValue)) {
               boolean var5Value3;
               try {
                  var5Value3 = local.getGameMode() == GameMode.SPECTATOR || !hashSetInst2.contains(var5Value2);
               } catch (Throwable error3) {
                  var5Value3 = false;
               }

               if (!var5Value3 || (Boolean)this.vanishedStaffSetting.getValue()) {
                  Text nullSnapshot = null;
                  Text nullSnapshot2 = null;
                  Text nullSnapshot3 = null;
                  String nullSnapshot4 = null;

                  try {
                     nullSnapshot = local.getDisplayName();
                     if (local.getScoreboardTeam() != null) {
                        nullSnapshot2 = local.getScoreboardTeam().getPrefix();
                        nullSnapshot3 = local.getScoreboardTeam().getSuffix();
                        nullSnapshot4 = local.getScoreboardTeam().getName();
                     }
                  } catch (Throwable error4) {
                  }

                  NameCountPair local2 = this.detectStaffMember(var5Value, nullSnapshot, nullSnapshot2, nullSnapshot3, nullSnapshot4);
                  if (local2 != null) {
                     hashSetInst.add(var5Value);
                     this.detectedStaff.put(var5Value, local2.name);
                     this.staffRanks.put(var5Value, local2.count);
                     if (var5Value3) {
                        this.vanishedPlayers.add(var5Value);
                     } else {
                        this.vanishedPlayers.remove(var5Value);
                     }

                     if (!this.staffSkins.containsKey(var5Value)) {
                        for (AbstractClientPlayerEntity class742 : minecraftClient.world.getPlayers()) {
                           if (class742.getName().getString().equalsIgnoreCase(var5Value) && class742 instanceof AbstractClientPlayerEntity) {
                              Identifier local3 = extractSkinTexture(class742);
                              if (local3 != null) {
                                 this.staffSkins.put(var5Value, local3);
                              }
                              break;
                           }
                        }
                     }

                     if (this.hasAnyStaff && !this.alertedPlayers.contains(var5Value)) {
                        this.alertedPlayers.add(var5Value);
                        if ((Boolean)this.chatAlertSetting.getValue() && minecraftClient.player != null) {
                           minecraftClient.player
                              .sendMessage(Text.literal("§8[§cStaff Detector§8] §c⚠ §f" + var5Value + " §7(" + local2.name + ")"), false);
                        }

                        try {
                           HudModule.pushToast("Staff Detector", var5Value, HudModule.toastDisabledColor, null);
                        } catch (Throwable error5) {
                        }

                        minecraftClient.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F);
                     }
                  }
               }
            }
         }
      }
   }

   public NameCountPair detectStaffMember(String string, Text arg, Text arg2, Text arg3, String string2) {
      if (string != null && !string.isEmpty()) {
         boolean friendNamesValue = friendNames.contains(string.toLowerCase(Locale.ROOT));
         Integer local = this.findStarColor(arg);
         if (local == null) {
            local = this.findStarColor(arg2);
         }

         if (local == null) {
            local = this.findStarColor(arg3);
         }

         int intVal = local != null ? 1 : 0;
         String local2 = safeText(arg) + " " + safeText(arg2) + " " + safeText(arg3) + " " + (string2 == null ? "" : string2);
         String local3 = findRankTag(stripPlayerName(local2, string));
         int intVal2 = local3 != null ? 1 : 0;
         String local4 = "Star + Rank";

         try {
            local4 = (String)this.detectModeSetting.getValue();
         } catch (Throwable error) {
         }

         int var8Snapshot;
         if ("Star Only".equals(local4)) {
            var8Snapshot = intVal;
         } else if ("Rank Only".equals(local4)) {
            var8Snapshot = intVal2;
         } else if ("Names Only".equals(local4)) {
            var8Snapshot = 0;
         } else {
            var8Snapshot = intVal == 0 && intVal2 == 0 ? 0 : 1;
         }

         if (!friendNamesValue && var8Snapshot == 0) {
            return null;
         }

         String local5 = intVal2 != 0 ? formatRankLabel(local3) : "STAFF";
         return new NameCountPair(local5, intVal != 0 ? local : 0);
      } else {
         return null;
      }
   }

   public Integer findStarColor(Text arg) {
      if (arg == null) {
         return null;
      }

      try {
         return (Integer)arg.visit((local, local2) -> {
            int local3 = 0;

            while (local3 < local2.length()) {
               int local4 = local2.codePointAt(local3);
               local3 += Character.charCount(local4);
               if (isStarGlyph(local4, (Boolean)this.fontIconsSetting.getValue())) {
                  TextColor local5 = local.getColor();
                  return Optional.of(local5 != null ? 0xFF000000 | local5.getRgb() : 0);
               }
            }

            return Optional.empty();
         }, Style.EMPTY).orElse(null);
      } catch (Throwable error) {
         return null;
      }
   }

   public static boolean isStarGlyph(int intVal, boolean flag) {

      return "★☆✦✧✪✩✫✬✭✮✯⭐✰❂⚝✴✵✶✷✸✹⍟".indexOf(intVal) >= 0
         ? true
         : flag && (intVal >= 57344 && intVal <= 63743 || intVal >= 983040 && intVal <= 1048573 || intVal >= 1048576 && intVal <= 1114109);
   }

   public static String findRankTag(String string) {
      if (string == null) {
         return null;
      }

      String local = stripNonAscii(string.toLowerCase(Locale.ROOT));
      if (local.isEmpty()) {
         return null;
      }

      for (String string2 : rankKeywords) {
         if (!string2.isEmpty() && local.contains(string2)) {
            return string2;
         }
      }

      return null;
   }

   public static String formatRankLabel(String string) {
      if (string == null) {
         return "STAFF";
      } else {
         String local = (String)rankLabels.get(string);
         if (local != null) {
            return local;
         } else {
            return string.isEmpty() ? "STAFF" : Character.toUpperCase(string.charAt(0)) + string.substring(1);
         }
      }
   }

   public static String safeText(Text arg) {
      try {

         return arg == null ? "" : arg.getString();
      } catch (Throwable error) {
         return "";
      }
   }

   public static String stripNonAscii(String string) {
      StringBuilder stringBuilderInst = new StringBuilder(string.length());

      for (int index = 0; index < string.length(); index++) {
         char charVal = string.charAt(index);
         if (charVal >= 'a' && charVal <= 'z') {
            stringBuilderInst.append(charVal);
         }
      }

      return stringBuilderInst.toString();
   }

   public static String stripPlayerName(String string, String string2) {
      try {

         return string2 != null && !string2.isEmpty() ? string.replaceAll("(?i)" + Pattern.quote(string2), " ") : string;
      } catch (Throwable error) {
         return string;
      }
   }

   public static Identifier extractSkinTexture(AbstractClientPlayerEntity arg) {
      try {
         SkinTextures var0Value = arg.getSkin();

         for (Method method : var0Value.getClass().getMethods()) {
            if (method.getParameterCount() == 0) {
               method.setAccessible(true);
               Object nullSnapshot = null;

               try {
                  nullSnapshot = method.invoke(var0Value);
               } catch (Exception error) {
                  continue;
               }

               if (nullSnapshot != null) {
                  if (nullSnapshot instanceof Identifier) {
                     return (Identifier)nullSnapshot;
                  }

                  try {
                     for (Method method2 : nullSnapshot.getClass().getMethods()) {
                        if (method2.getParameterCount() == 0 && method2.getReturnType() == Identifier.class) {
                           method2.setAccessible(true);
                           Identifier local = (Identifier)method2.invoke(nullSnapshot);
                           if (local != null) {
                              return local;
                           }
                        }
                     }
                  } catch (Exception error2) {
                  }
               }
            }
         }
      } catch (Exception error3) {
      }

      return null;
   }

   public static int getIconLineHeight() {
      try {
         if (CustomFontManager.getFontType() != ClientFontType.VANILLA) {
            HudTextEntry customFontManagerValue = CustomFontManager.INSTANCE7.getTextTexture("Ag");
            if (customFontManagerValue != null && customFontManagerValue.halfHeight > 0) {
               return customFontManagerValue.halfHeight;
            }
         } else {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.textRenderer != null) {
               return 9;
            }
         }
      } catch (Throwable error) {
      }

      return 8;
   }

   public static float centerVertically(int intVal, int intVal2, int intVal3) {
      return intVal + Math.max(0.0F, ((float)intVal2 - intVal3) / 2.0F);
   }

   public static void call2(DrawContext arg) {
      if (instance != null && instance.isEnabled() && (Boolean)instance.hudSetting.getValue()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc == null || mc.player == null || HudModule.instance == null) {
            return;
         }

         Map<String, String> instanceValue = instance.detectedStaff;
         int var2Count = instanceValue.size();
         byte byteVal = 12;
         byte byteVal2 = 10;
         byte byteVal3 = 12;
         String stringValue = String.valueOf(var2Count);
         int hudModuleValue = HudModule.instance.call2(stringValue) + 8;
         float var4HudModuleValue = byteVal + HudModule.instance.call2("STAFF") + 10 + hudModuleValue + 3.5F;
         float var9Snapshot = var4HudModuleValue;

         for (Entry entry : instanceValue.entrySet()) {
            float floatVal4 = byteVal
                  + byteVal2
                  + 4
                  + HudModule.instance.call2(((String)entry.getKey()).toUpperCase())
                  + 16
                  + HudModule.instance.call2((String)entry.getValue())
               + 3.5F;
            if (floatVal4 > var9Snapshot) {
               var9Snapshot = floatVal4;
            }
         }

         int intVal = var2Count == 0 ? 20 : 23 + var2Count * byteVal3 + 6;
         int[] hudModuleValue2 = HudModule.getSavedElementPos(HudElementType.STAFF_LIST);
         int intVal2 = hudModuleValue2[0];
         int intVal3 = hudModuleValue2[1];
         float hudModuleValue3 = HudModule.getScaledFactor(HudElementType.STAFF_LIST);
         arg.getMatrices().pushMatrix();
         arg.getMatrices().scale(hudModuleValue3, hudModuleValue3);
         int clickGuiModuleValue = ClickGuiModule.getAccentColorArgb();
         int clickGuiModuleValue2 = ClickGuiModule.getBackgroundColorArgb();
         boolean falseSnapshot = false;
         float floatVal = 2.0F;

         try {
            falseSnapshot = HudModule.instance != null && (Boolean)HudModule.instance.rainbowSetting.getValue();
         } catch (Throwable error) {
         }

         try {
            floatVal = (Float)HudModule.instance.rainbowSpeedSetting.getValue();
         } catch (Throwable error2) {
         }

         int hudModuleValue4 = HudModule.rainbowColorAt(floatVal, 0).getRGB();
         int intVal4 = falseSnapshot ? hudModuleValue4 : ClickGuiModule.getAccentColor().getRGB();
         int intVal5 = falseSnapshot ? hudModuleValue4 : clickGuiModuleValue;
         byte byteVal4 = -1;
         int intVal6 = falseSnapshot ? hudModuleValue4 : clickGuiModuleValue;
         int intVal7 = falseSnapshot ? hudModuleValue4 : clickGuiModuleValue;
         int intVal8 = falseSnapshot ? hudModuleValue4 & 16777215 | 1073741824 : clickGuiModuleValue & 16777215 | 1073741824;
         GuiRenderUtil.fillRoundedRect(arg, intVal2, intVal3, var9Snapshot, intVal, 3.5F, clickGuiModuleValue2, false);
         GuiRenderUtil.strokeRoundedRect(arg, intVal2, intVal3, var9Snapshot, intVal, 3.5F, 1.0F, intVal7, false);
         int intVal9 = getIconLineHeight();
         int var146Value = intVal3 + 6;
         float var48Var10Var4Var8Value = intVal2 + var9Snapshot - byteVal - hudModuleValue;
         int var281Value = var146Value - 1;
         float floatVal2 = centerVertically(var281Value, 10, intVal9);
         HudModule.instance.call1(arg, "STAFF", intVal2 + byteVal, floatVal2, intVal4);
         GuiRenderUtil.fillRoundedRect(arg, var48Var10Var4Var8Value, var281Value, hudModuleValue, 10.0F, 3.5F, intVal5, false);
         HudModule.instance.call1(arg, stringValue, var48Var10Var4Var8Value + hudModuleValue / 2 - HudModule.instance.call2(stringValue) / 2, floatVal2, byteVal4);
         if (var2Count > 0) {
            int var2884Value = var146Value + 8 + 4;
            GuiRenderUtil.fillRoundedRect(arg, intVal2 + byteVal, var2884Value, var9Snapshot - byteVal - 3.5F, 1.0F, 0.5F, intVal8, false);
            int var3214Value = var2884Value + 1 + 4;

            for (Entry entry2 : instanceValue.entrySet()) {
               String local = (String)entry2.getKey();
               String local2 = (String)entry2.getValue();
               Identifier local3 = (Identifier)instance.staffSkins.get(local);
               if (local3 == null && mc.world != null) {
                  for (AbstractClientPlayerEntity class742 : mc.world.getPlayers()) {
                     if (class742.getName().getString().equalsIgnoreCase(local) && class742 instanceof AbstractClientPlayerEntity) {
                        local3 = extractSkinTexture(class742);
                        if (local3 != null) {
                           instance.staffSkins.put(local, local3);
                        }
                        break;
                     }
                  }
               }

               int var48Var4Value = intVal2 + byteVal;
               int var331Value = var3214Value + 1;
               Identifier hudModuleValue5 = HudModule.resolveSkinHeadTexture(local3);
               if (hudModuleValue5 != null) {
                  GuiRenderUtil.drawTexture(arg, var48Var4Value, var331Value, byteVal2, hudModuleValue5, -1, 0.0F, false);
               } else {
                  Identifier class2960Id = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
                  Identifier hudModuleValue6 = HudModule.resolveSkinHeadTexture(class2960Id);
                  if (hudModuleValue6 != null) {
                     GuiRenderUtil.drawTexture(arg, var48Var4Value, var331Value, byteVal2, hudModuleValue6, -1, 0.0F, false);
                  }
               }

               float floatVal3 = centerVertically(var3214Value, byteVal3, intVal9);
               HudModule.instance.call1(arg, local.toUpperCase(), var48Var4Value + byteVal2 + 4, floatVal3, -1);
               int instanceValue2 = instance.vanishedPlayers.contains(local) ? HudModule.toastDisabledColor : intVal6;
               HudModule.instance.call1(arg, local2, intVal2 + var9Snapshot - byteVal - HudModule.instance.call2(local2), floatVal3, instanceValue2);
               var3214Value += byteVal3;
            }
         }

         arg.getMatrices().popMatrix();
      }
   }

   public Map call8() {
      return Collections.unmodifiableMap(this.detectedStaff);
   }

   public boolean hasDetectedStaff() {

      return !this.detectedStaff.isEmpty();
   }

   static {
      rankLabels.put("coowner", "Co-Owner");
      rankLabels.put("owner", "Owner");
      rankLabels.put("manager", "Manager");
      rankLabels.put("administrator", "Admin");
      rankLabels.put("admin", "Admin");
      rankLabels.put("developer", "Dev");
      rankLabels.put("dev", "Dev");
      rankLabels.put("srmod", "Sr.Mod");
      rankLabels.put("seniormod", "Sr.Mod");
      rankLabels.put("moderator", "Mod");
      rankLabels.put("mod", "Mod");
      rankLabels.put("srhelper", "Sr.Helper");
      rankLabels.put("seniorhelper", "Sr.Helper");
      rankLabels.put("helper", "Helper");
      rankLabels.put("trialmod", "Trial");
      rankLabels.put("trial", "Trial");
      rankLabels.put("builder", "Builder");
      rankLabels.put("support", "Support");
      rankLabels.put("staff", "Staff");
      rankLabels.put("community", "COMMUNITY MGR");
      rankLabels.put("junior", "Junior");
      rankLabels.put("head", "Head");
      rankLabels.put("operator", "Operator");
      rankLabels.put("sentinel", "Sentinel");
      rankLabels.put("guard", "Guard");
   }

}
