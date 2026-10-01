package com.threesix.manager;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import com.threesix.module.AutoFilterItemModule;
import com.threesix.module.DoubleAnchorModule;
import com.threesix.module.BlockNotifierModule;
import com.threesix.module.FreeLookModule;
import com.threesix.util.XorBitUtils;
import com.threesix.module.AutoSellModule;
import com.threesix.internal.ModuleBase;
import com.threesix.internal.KeybindModuleBase;
import com.threesix.module.HudModule;
import com.threesix.module.PlayerEspModule;
import com.threesix.module.AutoTpaModule;
import com.threesix.module.FakeStatsModule;
import com.threesix.module.SpawnerNotifierModule;
import com.threesix.module.AnchorMacroModule;
import com.threesix.module.BasicFeatureModule;
import com.threesix.module.HoleEspModule;
import com.threesix.util.TextStyleUtil;
import com.threesix.module.AutoMineModule;
import com.threesix.module.ClickGuiModule;
import com.threesix.module.NameTagsModule;
import com.threesix.data.ModuleCategory;
import com.threesix.module.HoverTotemModule;
import com.threesix.module.FullBrightModule;
import com.threesix.module.BoatFlyModule;
import com.threesix.module.SingleAnchorModule;
import com.threesix.module.GrowthFinderModule;
import com.threesix.module.AutoDoubleHandModule;
import com.threesix.setting.MultiSelectEnumSetting;
import com.threesix.module.SwingSpeedModule;
import com.threesix.module.NetheriteDebugModule;
import com.threesix.module.ConfigShareModule;
import com.threesix.module.CoordSnapperModule;
import com.threesix.data.HudElementType;
import com.threesix.setting.BlockListSetting;
import com.threesix.module.WeatherNotifierModule;
import com.threesix.module.SpawnerProtectModule;
import com.threesix.module.AutoTotemModule;
import com.threesix.module.TriggerbotModule;
import com.threesix.module.DiscordRpcModule;
import com.threesix.module.StaffDetectorModule;
import com.threesix.module.VoidEspModule;
import com.threesix.module.CrafterSetupModule;
import com.threesix.module.AutoTotemModule02;
import com.threesix.module.AntiTrapModule;
import com.threesix.module.NoRenderModule;
import com.threesix.module.SusChunkFinderModule;
import com.threesix.module.OrderSellModule;
import com.threesix.setting.ItemSelectSetting;
import com.threesix.module.SpearSwapModule;
import com.threesix.module.TabDetectorModule;
import com.threesix.module.ChatMacroModule;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.module.SigmaChunkFinderModule;
import com.threesix.module.AutoToolModule;
import com.threesix.module.HomeResetModule;
import com.threesix.module.JumpCirclesModule;
import com.threesix.module.ShieldBreakerModule;
import com.threesix.module.StorageEspModule;
import com.threesix.module.BlockEspModule;
import com.threesix.module.AutoCrystalModule;
import com.threesix.module.AutoEspBugModule;
import com.threesix.module.AutoPlaceModule;
import com.threesix.module.FastPlaceModule;
import com.threesix.module.FriendsModule;
import com.threesix.module.FreecamModule;
import com.threesix.module.HitboxExpandModule;
import com.threesix.module.NameProtectModule;
import com.threesix.module.RegionMapModule;
import com.threesix.module.SpawnerOrderBotModule;
import com.threesix.module.SpotifyHudModule;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.setting.AutoLogSetting;
import com.threesix.setting.EntityListSetting;

public class ConfigManager {
   public static final String CONFIG_HEADER = "THREESIX_CONFIG_V2";
   public static final String MODULE_TAG = "MODULE";
   public static final String SETTING_TAG = "SETTING";
   public static final String HUDPOS_TAG = "HUDPOS";
   public static final String BLOCKCOLOR_TAG = "BLOCKCOLOR";
   public static final String STORAGECOLOR_TAG = "STORAGECOLOR";
   private static final String ACTIVE_FILE_NAME = "threesix_active.txt";
   public String currentConfigName = "default";
   public static final ConfigManager INSTANCE = new ConfigManager();
   public final List<ModuleBase> modules = new ArrayList<>();
   public boolean initialized = false;
   public boolean loading = false;

   private Path getActiveConfigFilePath() {
      try {
         return MinecraftClient.getInstance().runDirectory.toPath().resolve("threesix_active.txt");
      } catch (Throwable error) {
         return null;
      }
   }

   private void writeActiveConfigName() {
      Path local = this.getActiveConfigFilePath();
      if (local != null && this.currentConfigName != null) {
         try {
            Files.writeString(local, this.currentConfigName, StandardCharsets.UTF_8);
         } catch (IOException iOException) {
         }
      }
   }

   private void detectActiveConfigName() {
      Path local = this.getActiveConfigFilePath();
      if (local != null && Files.isRegularFile(local)) {
         try {
            String filesValue = Files.readString(local, StandardCharsets.UTF_8).trim();
            if (!filesValue.isBlank()) {
               Path local2 = this.getConfigPath(filesValue);
               if (Files.isRegularFile(local2)) {
                  this.currentConfigName = filesValue;
                  return;
               }
            }
         } catch (IOException iOException) {
         }
      }

      Path local3 = this.findMostRecentConfigFile();
      if (local3 != null) {
         this.currentConfigName = this.extractConfigName(local3);
      } else {
         this.currentConfigName = "default";
      }
   }

   private Path getConfigPath(String string) {
      String local = string != null && !string.isBlank() ? string.trim() : "default";
      String local2 = local.equals("default") ? "threesix_config.txt" : "threesix_config_" + local + ".txt";

      try {
         return MinecraftClient.getInstance().runDirectory.toPath().resolve(local2);
      } catch (Throwable error) {
         return null;
      }
   }

   private String extractConfigName(Path path) {
      String local = path.getFileName().toString();
      if (local.equals("threesix_config.txt")) {
         return "default";
      } else if (local.startsWith("threesix_config_") && local.endsWith(".txt")) {
         return local.substring("threesix_config_".length(), local.length() - 4);
      } else {
         return local.endsWith(".txt") ? local.substring(0, local.length() - 4) : "default";
      }
   }

   private Path findMostRecentConfigFile() {
      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.runDirectory != null) {
            Path local = mc.runDirectory.toPath();
            Path nullSnapshot = null;
            long longVal = -1L;

            try (Stream<Path> local4 = Files.list(local)) {
               for (Path path : local4.toList()) {
                  String local2 = path.getFileName().toString();
                  if ((local2.equals("threesix_config.txt") || local2.startsWith("threesix_config_") && local2.endsWith(".txt")) && Files.isRegularFile(path)) {
                     long filesValue = Files.getLastModifiedTime(path).toMillis();
                     if (filesValue > longVal) {
                        longVal = filesValue;
                        nullSnapshot = path;
                     }
                  }
               }
            } catch (IOException iOException) {
            }

            Path local3 = local.resolve("threesix_configs");
            if (Files.isDirectory(local3)) {
               try (Stream<Path> local5 = Files.list(local3)) {
                  for (Path path2 : local5.toList()) {
                     if (path2.getFileName().toString().endsWith(".txt") && Files.isRegularFile(path2)) {
                        long filesValue2 = Files.getLastModifiedTime(path2).toMillis();
                        if (filesValue2 > longVal) {
                           longVal = filesValue2;
                           nullSnapshot = path2;
                        }
                     }
                  }
               } catch (IOException iOException2) {
               }
            }

            return nullSnapshot;
         } else {
            return null;
         }
      } catch (Throwable error) {
         return null;
      }
   }

   public void init() {
      if (!this.initialized) {
         this.modules.add(new ClickGuiModule());
         this.modules.add(new HudModule());
         this.modules.add(new SpotifyHudModule());
         this.modules.add(new FriendsModule());
         this.modules.add(new ConfigShareModule());
         this.modules.add(new BasicFeatureModule("Elytra Swap", ModuleCategory.COMBAT));
         this.modules.add(new AutoTotemModule02());
         this.modules.add(new ShieldBreakerModule());
         this.modules.add(new AnchorMacroModule());
         this.modules.add(new BasicFeatureModule("Mace Swap", ModuleCategory.COMBAT));
         this.modules.add(new TriggerbotModule());
         this.modules.add(new HoverTotemModule());
         this.modules.add(new DoubleAnchorModule());
         this.modules.add(new SingleAnchorModule());
         this.modules.add(new AutoDoubleHandModule());
         this.modules.add(new AutoTotemModule());
         this.modules.add(new AutoCrystalModule());
         this.modules.add(new HitboxExpandModule());
         this.modules.add(new SpearSwapModule());
         this.modules.add(new DiscordRpcModule());
         this.modules.add(new StorageEspModule());
         this.modules.add(new FullBrightModule());
         this.modules.add(new PlayerEspModule());
         this.modules.add(new BoatFlyModule());
         this.modules.add(new NoRenderModule());
         this.modules.add(new JumpCirclesModule());
         this.modules.add(new BlockEspModule());
         this.modules.add(new FreecamModule());
         this.modules.add(new SpawnerNotifierModule());
         this.modules.add(new HoleEspModule());
         this.modules.add(new VoidEspModule());
         this.modules.add(new BlockNotifierModule());
         this.modules.add(new SusChunkFinderModule());
         this.modules.add(new GrowthFinderModule());
         this.modules.add(new AutoMineModule());
         this.modules.add(new NameTagsModule());
         this.modules.add(new RegionMapModule());
         this.modules.add(new AutoToolModule());
         this.modules.add(new NameProtectModule());
         this.modules.add(new FreeLookModule());
         this.modules.add(new WeatherNotifierModule());
         this.modules.add(new CoordSnapperModule());
         this.modules.add(new FastPlaceModule());
         this.modules.add(new TabDetectorModule());
         this.modules.add(new AutoLogSetting());
         this.modules.add(new CrafterSetupModule());
         this.modules.add(new AutoTpaModule());
         this.modules.add(new SwingSpeedModule());
         this.modules.add(new ChatMacroModule());
         this.modules.add(new SpawnerProtectModule());
         this.modules.add(new FakeStatsModule());
         this.modules.add(new SpawnerOrderBotModule());
         this.modules.add(new AntiTrapModule());
         this.modules.add(new StaffDetectorModule());
         this.modules.add(new SigmaChunkFinderModule());
         this.modules.add(new HomeResetModule());
         this.modules.add(new AutoSellModule());
         this.modules.add(new AutoPlaceModule());
         this.modules.add(new NetheriteDebugModule());
         this.modules.add(new AutoFilterItemModule());
         this.modules.add(new OrderSellModule());
         this.modules.add(new AutoEspBugModule());
         this.initialized = true;
         this.detectActiveConfigName();
         this.load();
         this.applyEnabledStates();
      }
   }

   public void applyEnabledStates() {

      for (ModuleBase moduleBase : this.modules) {
         if (moduleBase != null && moduleBase.isEnabled()) {
            try {
               moduleBase.onEnable();
            } catch (Throwable error) {
            }
         }
      }
   }

   public void notifyLayoutChanged() {
   }

   public void save() {
      if (this.initialized && !this.loading) {
         Path local = this.getConfigFile();

         try {
            Files.createDirectories(local.getParent());

            try (BufferedWriter local9 = Files.newBufferedWriter(local, StandardCharsets.UTF_8)) {
               local9.write("THREESIX_CONFIG_V2");
               local9.newLine();

               for (ModuleBase moduleBase : this.modules) {
                  local9.write("MODULE");
                  local9.write(9);
                  local9.write(this.encodeBase64(moduleBase.getName2()));
                  local9.write(9);
                  local9.write(Integer.toString(moduleBase.getKeyCode()));
                  local9.write(9);
                  int intVal = moduleBase instanceof KeybindModuleBase local10 ? local10.getBindKeyCode() : 0;
                  local9.write(Integer.toString(intVal));
                  local9.write(9);
                  local9.write(Boolean.toString(moduleBase.isEnabled()));
                  local9.newLine();

                  for (ClientSetting clientSetting : moduleBase.getSettings()) {
                     String local2 = this.serializeSettingValue(clientSetting);
                     if (local2 != null) {
                        local9.write("SETTING");
                        local9.write(9);
                        local9.write(this.encodeBase64(moduleBase.getName2()));
                        local9.write(9);
                        local9.write(this.encodeBase64(clientSetting.getName()));
                        local9.write(9);
                        local9.write(this.encodeBase64(local2));
                        local9.newLine();
                     }
                  }
               }

               for (HudElementType hudElementType : HudElementType.values()) {
                  int[] hudModuleValue = HudModule.getSavedElementPos(hudElementType);
                  local9.write("HUDPOS");
                  local9.write(9);
                  local9.write(hudElementType.name());
                  local9.write(9);
                  local9.write(Integer.toString(hudModuleValue[0]));
                  local9.write(9);
                  local9.write(Integer.toString(hudModuleValue[1]));
                  local9.newLine();
               }

               for (ModuleBase moduleBase2 : this.modules) {
                  if (moduleBase2 instanceof BlockEspModule local11) {
                     Map local3 = local11.getBlockColors();

                     for (Entry entry : (Iterable<Entry>)local3.entrySet()) {
                        Identifier local4 = Registries.BLOCK.getId((Block)entry.getKey());
                        if (local4 != null) {
                           Color local5 = (Color)entry.getValue();
                           local9.write("BLOCKCOLOR");
                           local9.write(9);
                           local9.write(this.encodeBase64(local4.toString()));
                           local9.write(9);
                           local9.write(local5.getRed() + "," + local5.getGreen() + "," + local5.getBlue() + "," + local5.getAlpha());
                           local9.newLine();
                        }
                     }
                  }
               }

               for (ModuleBase moduleBase3 : this.modules) {
                  if (moduleBase3 instanceof StorageEspModule local12) {
                     Map local6 = local12.getBlockColorMap();

                     for (Entry entry2 : (Iterable<Entry>)local6.entrySet()) {
                        Identifier local7 = Registries.BLOCK.getId((Block)entry2.getKey());
                        if (local7 != null) {
                           Color local8 = (Color)entry2.getValue();
                           local9.write("STORAGECOLOR");
                           local9.write(9);
                           local9.write(this.encodeBase64(local7.toString()));
                           local9.write(9);
                           local9.write(local8.getRed() + "," + local8.getGreen() + "," + local8.getBlue() + "," + local8.getAlpha());
                           local9.newLine();
                        }
                     }
                  }
               }
            }
         } catch (IOException iOException) {
         }
      }
   }

   public void load() {
      Path local = this.getConfigFile();
      if (!Files.exists(local)) {
         Path local2 = this.findMostRecentConfigFile();
         if (local2 != null && !local2.equals(local)) {
            String local3 = this.extractConfigName(local2);
            if (local3 != null && !local3.equals(this.currentConfigName)) {
               this.currentConfigName = local3;
               this.writeActiveConfigName();
               local = this.getConfigFile();
            }

            if (!Files.exists(local) && local2.getParent() != null && local2.getParent().getFileName().toString().equals("threesix_configs")) {
               try {
                  Files.createDirectories(local.getParent());
                  Files.copy(local2, local, StandardCopyOption.REPLACE_EXISTING);
               } catch (IOException iOException) {
               }
            }
         }
      }

      if (!Files.exists(local)) {
         this.currentConfigName = "default";
         this.writeActiveConfigName();
      } else {
         if (Files.exists(local)) {
            this.loading = true;

            try {
               for (String string : Files.readAllLines(local, StandardCharsets.UTF_8)) {
                  if (string != null && !string.isBlank() && !"THREESIX_CONFIG_V2".equals(string)) {
                     if (string.startsWith("MODULE")) {
                        this.parseModuleLine(string);
                     } else if (string.startsWith("SETTING")) {
                        this.parseSettingLine(string);
                     } else if (string.startsWith("HUDPOS")) {
                        this.parseHudPosLine(string);
                     } else if (string.startsWith("BLOCKCOLOR")) {
                        this.parseBlockColorLine(string);
                     } else if (string.startsWith("STORAGECOLOR")) {
                        this.parseStorageColorLine(string);
                     } else {
                        this.parseLegacyLine(string);
                     }
                  }
               }
            } catch (IOException iOException2) {
            } finally {
               this.loading = false;
            }
         }
      }
   }

   public void parseHudPosLine(String string) {
      try {
         String[] local = string.split("\t");
         if (local.length < 4) {
            return;
         }

         HudElementType hudElementTypeValue = HudElementType.valueOf(local[1]);
         int integerValue = Integer.parseInt(local[2]);
         int integerValue2 = Integer.parseInt(local[3]);
         HudModule.call4(hudElementTypeValue, integerValue, integerValue2);
      } catch (Exception error) {
      }
   }

   public void parseBlockColorLine(String string) {
      try {
         String[] local = string.split("\t");
         if (local.length < 3) {
            return;
         }

         String local2 = this.decodeBase64(local[1]);
         String[] local3 = local[2].split(",");
         if (local3.length < 4) {
            return;
         }

         int integerValue = Integer.parseInt(local3[0].trim());
         int integerValue2 = Integer.parseInt(local3[1].trim());
         int integerValue3 = Integer.parseInt(local3[2].trim());
         int integerValue4 = Integer.parseInt(local3[3].trim());
         Identifier class2960Value = Identifier.tryParse(local2);
         if (class2960Value == null) {
            return;
         }

         Block local4 = (Block)Registries.BLOCK.get(class2960Value);
         if (local4 == null || local4 == Blocks.AIR) {
            return;
         }

         for (ModuleBase moduleBase : this.modules) {
            if (moduleBase instanceof BlockEspModule local6) {
               Map local5 = local6.getBlockColors();
               local5.put(local4, new Color(integerValue, integerValue2, integerValue3, integerValue4));
               local6.setBlockColors(local5);
            }
         }
      } catch (Exception error) {
      }
   }

   public void parseStorageColorLine(String string) {
      try {
         String[] local = string.split("\t");
         if (local.length < 3) {
            return;
         }

         String local2 = this.decodeBase64(local[1]);
         String[] local3 = local[2].split(",");
         if (local3.length < 4) {
            return;
         }

         int integerValue = Integer.parseInt(local3[0].trim());
         int integerValue2 = Integer.parseInt(local3[1].trim());
         int integerValue3 = Integer.parseInt(local3[2].trim());
         int integerValue4 = Integer.parseInt(local3[3].trim());
         Identifier class2960Value = Identifier.tryParse(local2);
         if (class2960Value == null) {
            return;
         }

         Block local4 = (Block)Registries.BLOCK.get(class2960Value);
         if (local4 == null || local4 == Blocks.AIR) {
            return;
         }

         for (ModuleBase moduleBase : this.modules) {
            if (moduleBase instanceof StorageEspModule local5) {
               local5.setBlockColor(local4, new Color(integerValue, integerValue2, integerValue3, integerValue4));
            }
         }
      } catch (Exception error) {
      }
   }

   public String getCurrentConfigName() {
      return this.currentConfigName;
   }

   public void setConfigName(String string) {
      String currentConfigNameSnapshot = this.currentConfigName;
      this.currentConfigName = string != null && !string.isBlank() ? string.trim() : "default";
      if (!this.currentConfigName.equals(currentConfigNameSnapshot)) {
         this.writeActiveConfigName();
      } else {
         this.writeActiveConfigName();
      }
   }

   public boolean isLoading() {
      return this.loading;
   }

   public List<ModuleBase> getModules() {
      return this.modules;
   }

   public List<ModuleBase> getModulesByCategory(ModuleCategory moduleCategory) {
      ArrayList<ModuleBase> arrayListInst = new ArrayList<>();

      for (ModuleBase moduleBase : this.modules) {
         if (moduleBase.getCategory() == moduleCategory) {
            String local = moduleBase.getName2();
            if (!"DiscordRPC".equals(local)) {
               arrayListInst.add(moduleBase);
            }
         }
      }

      return arrayListInst;
   }

   public ModuleBase getModuleByName(String string) {

      for (ModuleBase moduleBase : this.modules) {
         if (moduleBase.getName2().equalsIgnoreCase(string)) {
            return moduleBase;
         }
      }

      return null;
   }

   public void tick() {

      for (ModuleBase moduleBase : this.modules) {
         if (moduleBase.isEnabled()) {
            moduleBase.onTick();
         }
      }
   }

   public void render(MatrixStack arg, float floatVal) {

      for (ModuleBase moduleBase : this.modules) {
         if (moduleBase.isEnabled()) {
            moduleBase.onRender(arg, floatVal);
         }
      }

      try {
         WorldShapeRenderer local = (WorldShapeRenderer)TextStyleUtil.rendererHolder.get();
         if (local != null) {
            local.markShapesPending();
         }
      } catch (Throwable error) {
      }
   }

   public void onPacketSend2(Packet arg) {

      for (ModuleBase moduleBase : this.modules) {
         if (moduleBase.isEnabled()) {
            moduleBase.onPacketSend(arg);
         }
      }
   }

   public boolean onPacketReceive2(Packet arg) {
      byte byteVal = 0;

      for (ModuleBase moduleBase : this.modules) {
         if (moduleBase.isEnabled()) {
            try {
               if (moduleBase.onPacketReceive(arg)) {
                  byteVal = 1;
               }
            } catch (Exception error) {
            }
         }
      }

      return byteVal != 0;
   }

   public Path getConfigFile() {
      String local = this.currentConfigName != null && !this.currentConfigName.isBlank() ? this.currentConfigName : "default";
      String local2 = local.equals("default") ? "threesix_config.txt" : "threesix_config_" + local + ".txt";
      return MinecraftClient.getInstance().runDirectory.toPath().resolve(local2);
   }

   public void parseModuleLine(String string) {
      String[] local = string.split("\t", 5);
      if (local.length >= 5) {
         ModuleBase local2 = this.getModuleByName(this.decodeBase64(local[1]));
         if (local2 != null) {
            try {
               local2.setKeyCodeFromConfig(Integer.parseInt(local[2]));
               if (local2 instanceof KeybindModuleBase local3) {
                  local3.setBindKeyCodeFromConfig(Integer.parseInt(local[3]));
               }

               local2.setEnabledRaw(Boolean.parseBoolean(local[4]));
            } catch (Exception error) {
            }
         }
      }
   }

   public void parseSettingLine(String string) {
      String[] local = string.split("\t", 4);
      if (local.length >= 4) {
         ModuleBase local2 = this.getModuleByName(this.decodeBase64(local[1]));
         if (local2 != null) {
            ClientSetting local3 = this.findSetting(local2, this.decodeBase64(local[2]));
            if (local3 != null) {
               this.deserializeSettingValue(local3, this.decodeBase64(local[3]));
            }
         }
      }
   }

   public void parseLegacyLine(String string) {
      String[] local = string.split(":", 4);
      if (local.length >= 2) {
         ModuleBase local2 = this.getModuleByName(local[0]);
         if (local2 != null) {
            try {
               if (local.length >= 2) {
                  local2.setKeyCodeFromConfig(Integer.parseInt(local[1]));
               }

               if (local.length >= 3 && local2 instanceof KeybindModuleBase local3) {
                  local3.setBindKeyCodeFromConfig(Integer.parseInt(local[2]));
               }

               if (local.length >= 4) {
                  local2.setEnabledRaw(Boolean.parseBoolean(local[3]));
               }
            } catch (Exception error) {
            }
         }
      }
   }

   public ClientSetting findSetting(ModuleBase moduleBase, String string) {

      for (ClientSetting clientSetting : moduleBase.getSettings()) {
         if (clientSetting.isNamed(string)) {
            return clientSetting;
         }
      }

      return null;
   }

   public String serializeSettingValue(ClientSetting clientSetting) {
      Object local = clientSetting.getValue();
      if (clientSetting instanceof ItemSelectSetting local2) {
         return this.formatItemList(local2);
      } else if (clientSetting instanceof BlockListSetting local3) {
         return this.formatBlockList(local3);
      } else if (clientSetting instanceof EntityListSetting local4) {
         return this.formatEntityList(local4);
      } else if (clientSetting instanceof MultiSelectEnumSetting local5) {
         return this.joinSet((Set)local5.getValue());
      } else if (local instanceof Boolean local6) {
         return Boolean.toString(local6);
      } else if (local instanceof Float local7) {
         return Float.toString(local7);
      } else if (local instanceof Integer local8) {
         return Integer.toString(local8);
      } else if (local instanceof Double local9) {
         return Double.toString(local9);
      } else if (local instanceof String) {
         return (String)local;
      } else {
         return local instanceof Color local10 ? local10.getRed() + "," + local10.getGreen() + "," + local10.getBlue() + "," + local10.getAlpha() : null;
      }
   }

   public void deserializeSettingValue(ClientSetting clientSetting, String string) {
      Object local = clientSetting.getValue();

      try {
         if (clientSetting instanceof ItemSelectSetting local3) {
            local3.setItems(this.parseItemList(string));
            return;
         }

         if (clientSetting instanceof BlockListSetting local4) {
            local4.setBlocks(this.parseBlockList(string));
            return;
         }

         if (clientSetting instanceof EntityListSetting local5) {
            local5.setEntities(this.parseEntityList(string));
            return;
         }

         if (clientSetting instanceof MultiSelectEnumSetting local6) {
            local6.setValues(this.splitToSet(string));
            return;
         }

         if (local instanceof Boolean) {
            clientSetting.setValue(Boolean.parseBoolean(string));
            return;
         }

         if (local instanceof Float) {
            clientSetting.setValue(Float.parseFloat(string));
            return;
         }

         if (local instanceof Integer) {
            clientSetting.setValue(Math.round(Float.parseFloat(string)));
            return;
         }

         if (local instanceof Double) {
            clientSetting.setValue(Double.parseDouble(string));
            return;
         }

         if (local instanceof String) {
            clientSetting.setValue(string);
            return;
         }

         if (local instanceof Color) {
            String[] local2 = string.split(",", 4);
            if (local2.length == 4) {
               clientSetting.setValue(new Color(Integer.parseInt(local2[0]), Integer.parseInt(local2[1]), Integer.parseInt(local2[2]), Integer.parseInt(local2[3])));
            }
         }
      } catch (Exception error) {
      }
   }

   public String formatBlockList(BlockListSetting blockListSetting) {
      StringBuilder stringBuilderInst = new StringBuilder();

      for (Block class2248 : (Iterable<Block>)blockListSetting.getSelectedBlocks()) {
         Identifier local = Registries.BLOCK.getId(class2248);
         if (local != null) {
            if (!stringBuilderInst.isEmpty()) {
               stringBuilderInst.append(',');
            }

            stringBuilderInst.append(local);
         }
      }

      return stringBuilderInst.toString();
   }

   public Set parseBlockList(String string) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (string != null && !string.isBlank()) {
         for (String string2 : string.split(",")) {
            String local = string2.trim();
            if (!local.isEmpty()) {
               Identifier class2960Value = Identifier.tryParse(local);
               if (class2960Value != null) {
                  Block local2 = (Block)Registries.BLOCK.get(class2960Value);
                  if (local2 != null) {
                     linkedHashSetInst.add(local2);
                  }
               }
            }
         }

         return linkedHashSetInst;
      } else {
         return linkedHashSetInst;
      }
   }

   public String formatEntityList(EntityListSetting entityListSetting) {
      StringBuilder stringBuilderInst = new StringBuilder();

      for (EntityType class1299 : (Iterable<EntityType>)entityListSetting.getSelectedEntities()) {
         Identifier local = Registries.ENTITY_TYPE.getId(class1299);
         if (local != null) {
            if (!stringBuilderInst.isEmpty()) {
               stringBuilderInst.append(',');
            }

            stringBuilderInst.append(local);
         }
      }

      return stringBuilderInst.toString();
   }

   public Set parseEntityList(String string) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (string != null && !string.isBlank()) {
         for (String string2 : string.split(",")) {
            String local = string2.trim();
            if (!local.isEmpty()) {
               Identifier class2960Value = Identifier.tryParse(local);
               if (class2960Value != null && Registries.ENTITY_TYPE.containsId(class2960Value)) {
                  linkedHashSetInst.add((EntityType)Registries.ENTITY_TYPE.get(class2960Value));
               }
            }
         }

         return linkedHashSetInst;
      } else {
         return linkedHashSetInst;
      }
   }

   public String joinSet(Set set) {
      StringBuilder stringBuilderInst = new StringBuilder();

      for (String string : (Iterable<String>)set) {
         if (string != null && !string.isEmpty()) {
            if (!stringBuilderInst.isEmpty()) {
               stringBuilderInst.append(',');
            }

            stringBuilderInst.append(string);
         }
      }

      return stringBuilderInst.toString();
   }

   public Set splitToSet(String string) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (string != null && !string.isBlank()) {
         for (String string2 : string.split(",")) {
            String local = string2.trim();
            if (!local.isEmpty()) {
               linkedHashSetInst.add(local);
            }
         }

         return linkedHashSetInst;
      } else {
         return linkedHashSetInst;
      }
   }

   public String encodeBase64(String string) {
      return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
   }

   public String decodeBase64(String string) {
      if (string != null && !string.isEmpty()) {
         try {
            return new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8);
         } catch (IllegalArgumentException illegalArgumentException) {
            return string;
         }
      } else {
         return "";
      }
   }

   public String formatItemList(ItemSelectSetting itemSelectSetting) {
      StringBuilder stringBuilderInst = new StringBuilder();

      for (Item class1792 : (Iterable<Item>)(Set)itemSelectSetting.getValue()) {
         Identifier local = Registries.ITEM.getId(class1792);
         if (local != null) {
            if (!stringBuilderInst.isEmpty()) {
               stringBuilderInst.append(',');
            }

            stringBuilderInst.append(local.toString());
         }
      }

      return stringBuilderInst.toString();
   }

   public Set parseItemList(String string) {
      LinkedHashSet linkedHashSetInst = new LinkedHashSet();
      if (string != null && !string.isBlank()) {
         for (String string2 : string.split(",")) {
            String local = string2.trim();
            if (!local.isEmpty()) {
               Identifier class2960Value = Identifier.tryParse(local);
               if (class2960Value != null && Registries.ITEM.containsId(class2960Value)) {
                  linkedHashSetInst.add((Item)Registries.ITEM.get(class2960Value));
               }
            }
         }
      }

      return linkedHashSetInst;
   }

   public static String getDefaultKey() {
      return "L";
   }

}
