package com.threesix.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Formatting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityAttachmentType;
import org.joml.Matrix3x2fStack;
import com.threesix.module.NameProtectModule;
import com.threesix.module.FreecamModule;
import com.threesix.data.ItemStackEntry;
import com.threesix.util.XorBitUtils;
import com.threesix.util.WorldToScreenUtil;
import com.threesix.internal.ModuleBase;
import com.threesix.util.GuiRenderUtil;
import com.threesix.data.HealthBarLayout;
import com.threesix.data.LineSegmentBounds;
import com.threesix.manager.CustomFontManager;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.data.EntityHudEntry;
import com.threesix.data.ModuleCategory;
import com.threesix.gui.ClickGuiScreen;
import com.threesix.util.TextStyleUtil;

public final class NameTagsModule extends ModuleBase {
   public static final float maxRenderDistance = 128.0F;
   public static final boolean enabledByDefault = true;
   public static final int sentinelIndex = -1;
   public static final int shadowColor = -16777216;
   public static final float lineWidthScale = 0.025F;
   public static final int unitHalfBlock = 1;
   public static final int offEdgeValue = 0;
   public static final int healthRowSpacing = 10;
   public static final int defaultPanelWidth = 0;
   public static final int itemRowSpacing = 16;
   public static final int itemRowGap = 2;
   public static final int maxLabelWidth = 60;
   public static final int pingBucketCount = 3;
   public static final int nameColor = -1610612736;
   public static final int distanceColor = -13722113;
   public static final int healthBarBorder = 34;
   public static final int healthBarHeight = 16;
   public static final int healthBarOffset = 0;
   public static final double distanceScale = 0.62;
   public static final double pingScale = 1.25;
   public static final long cacheLifetimeMs = 125L;
   public static final int heartSpriteIndex = 9;
   public static final int heartOverlayIndex = 8;
   public static final Identifier heartContainerTexture = Identifier.ofVanilla("hud/heart/container");
   public static final Identifier heartFullTexture = Identifier.ofVanilla("hud/heart/full");
   public static final Identifier heartHalfTexture = Identifier.ofVanilla("hud/heart/half");
   public static final Identifier heartAbsorbingFullTexture = Identifier.ofVanilla("hud/heart/absorbing_full");
   public static final Identifier heartAbsorbingHalfTexture = Identifier.ofVanilla("hud/heart/absorbing_half");
   public static final Pattern colorCodePattern = Pattern.compile("§.");
   public static final float nameTextScale = 3.0F;
   public static final float statTextScale = 3.0F;
   public static final float pingTextScale = 2.0F;
   public static final float iconTextScale = 1.0F;
   public static final int panelAlpha = 150;
   public static final int accentAlpha = 90;
   public static NameTagsModule instance;
   public final ClientSetting selfSetting = new ClientSetting("Self", false);
   public final ClientSetting nameSetting = new ClientSetting("Name", true);
   public final ClientSetting pingSetting = new ClientSetting("Ping", true);
   public final ClientSetting healthSetting = new ClientSetting("Health", true);
   public final ClientSetting mainHandSetting = new ClientSetting("MainHand", true);
   public final ClientSetting offHandSetting = new ClientSetting("OffHand", true);
   public final ClientSetting armorSetting = new ClientSetting("Armor", true);
   public final ClientSetting panelSetting = new ClientSetting("Panel", true);
   public final ClientSetting droppedItemsSetting = new ClientSetting("Dropped Items", true);
   public final ClientSetting distanceSetting = new ClientSetting("Distance", true);
   public final ClientSetting itemAmountSetting = new ClientSetting("Item Amount", true);
   public final ClientSetting scaleSetting = new ClientSetting("Scale", 1.0F, 0.5F, 2.0F);
   public final ClientSetting rangeSetting = new ClientSetting("Range", 64.0F, 8.0F, 256.0F);
   public final ClientSetting opacitySetting = new ClientSetting("Opacity", 100, 10, 100);
   public final Map<UUID, EntityHudEntry> entryCache = new HashMap();
   public final LineSegmentBounds lineBounds = new LineSegmentBounds();
   public int lastSettingsMask = Integer.MIN_VALUE;

   public NameTagsModule() {
      super("NameTags", ModuleCategory.MISC);
      instance = this;
      this.registerSetting(this.selfSetting);
      this.registerSetting(this.nameSetting);
      this.registerSetting(this.pingSetting);
      this.registerSetting(this.healthSetting);
      this.registerSetting(this.mainHandSetting);
      this.registerSetting(this.offHandSetting);
      this.registerSetting(this.armorSetting);
      this.registerSetting(this.panelSetting);
      this.registerSetting(this.droppedItemsSetting);
      this.registerSetting(this.distanceSetting);
      this.registerSetting(this.itemAmountSetting);
      this.registerSetting(this.scaleSetting);
      this.registerSetting(this.rangeSetting);
      this.registerSetting(this.opacitySetting);
   }

   public static boolean isActive() {
      int local = instance != null && instance.isEnabled() && minecraftClient != null && minecraftClient.player != null ? 1 : 0;
      return local != 0;
   }

   public static void call1(DrawContext arg, float floatVal) {
      if (isActive() && minecraftClient.world != null && !minecraftClient.options.hudHidden && !isGuiOpen()) {
         NameTagsModule instanceSnapshot = instance;
         if (instanceSnapshot != null) {
            instanceSnapshot.refreshCache();
            instanceSnapshot.pruneCache();
            long systemValue = System.currentTimeMillis();
            Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
            if (textStyleUtilValue != null) {
               Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
               double textStyleUtilValue2PosY = textStyleUtilValue2.x;
               double textStyleUtilValue2PosX = textStyleUtilValue2.y;
               double textStyleUtilValue2PosZ = textStyleUtilValue2.z;
               float floatVal2 = 64.0F;

               try {
                  floatVal2 = (Float)instanceSnapshot.rangeSetting.getValue();
               } catch (Throwable error) {
               }

               double floatVal2Value = (double)floatVal2 * floatVal2;
               float floatVal3 = 1.0F;

               try {
                  floatVal3 = (Float)instanceSnapshot.scaleSetting.getValue();
               } catch (Throwable error2) {
               }

               float floatVal4 = 1.0F;

               try {
                  floatVal4 = Math.max(0.1F, Math.min(1.0F, ((Integer)instanceSnapshot.opacitySetting.getValue()).intValue() / 100.0F));
               } catch (Throwable error3) {
               }

               double minecraftClientValue = minecraftClient.getWindow().getScaledWidth() * 0.5 * Math.abs(WorldToScreenUtil.projectionMatrix2.m00()) * 0.025F;
               double minecraftClientValue2 = minecraftClient.getWindow().getScaledHeight() * 0.5 * Math.abs(WorldToScreenUtil.projectionMatrix2.m11()) * 0.025F;
               Matrix3x2fStack var0Value = arg.getMatrices();

               for (PlayerEntity class1657 : minecraftClient.world.getPlayers()) {
                  if (instanceSnapshot.isTrackablePlayer(class1657)) {
                     double class3532Value = MathHelper.lerp(floatVal, class1657.lastRenderX, class1657.getX());
                     double class3532Value2 = MathHelper.lerp(floatVal, class1657.lastRenderY, class1657.getY());
                     double class3532Value3 = MathHelper.lerp(floatVal, class1657.lastRenderZ, class1657.getZ());
                     double var25Var7Value = class3532Value - textStyleUtilValue2PosY;
                     double var27Var9Value = class3532Value2 - textStyleUtilValue2PosX;
                     double var29Var11Value = class3532Value3 - textStyleUtilValue2PosZ;
                     double var31Var31Var33Var33Var35V = var25Var7Value * var25Var7Value + var27Var9Value * var27Var9Value + var29Var11Value * var29Var11Value;
                     if (!(var31Var31Var33Var33Var35V > floatVal2Value)) {
                        EntityHudEntry local = instanceSnapshot.getOrCreateEntry(class1657, systemValue);
                        if (!local.pu()) {
                           double maxValue = Math.max(0.35, class1657.getWidth() * 0.5);
                           if (TextStyleUtil.isAabbVisible(class3532Value - maxValue, class3532Value2, class3532Value3 - maxValue, class3532Value + maxValue, class3532Value2 + class1657.getHeight() + 1.25, class3532Value3 + maxValue)) {
                              float floatVal5 = instanceSnapshot.projectToScreen(class1657, floatVal, class3532Value, class3532Value2, class3532Value3, minecraftClientValue, minecraftClientValue2);
                              if (!(floatVal5 <= 0.0F)) {
                                 floatVal5 *= 1.2F * floatVal3;
                                 double var25Var7Value2 = class3532Value - textStyleUtilValue2PosY;
                                 double var29Var11Value2 = class3532Value3 - textStyleUtilValue2PosZ;
                                 double sqrtValue = Math.sqrt(var25Var7Value2 * var25Var7Value2 + var29Var11Value2 * var29Var11Value2);
                                 if (sqrtValue >= 20.0) {
                                    int intVal = (int)((sqrtValue - 20.0) / 10.0) + 1;
                                    floatVal5 *= (float)Math.pow(2.0, intVal);
                                 }

                                 if (floatVal5 > 3.0F) {
                                    floatVal5 = 3.0F;
                                 }

                                 boolean flag = local.nameLabel() != null;
                                 boolean flag2 = local.healthData() != null;
                                 boolean flag3 = !local.items().isEmpty();
                                 int intVal2 = getPanelLeftOffset(flag);
                                 int intVal3 = getPanelWidth(flag, flag2);
                                 var0Value.pushMatrix();
                                 translateTo(var0Value, (float)instanceSnapshot.lineBounds.x, (float)instanceSnapshot.lineBounds.y, floatVal5);
                                 if ((Boolean)instanceSnapshot.panelSetting.getValue()) {
                                    renderPanel(arg, local, floatVal4);
                                 }

                                 renderNameLine(arg, local.nameLabel(), local.nameWidth(), local.itemRowWidth(), 1, false, floatVal4);
                                 renderHealthBar(arg, local.healthData(), intVal2);
                                 if (flag3) {
                                    int intVal4 = local.itemRowWidth();
                                    int intVal5 = -(intVal4 / 2);
                                    int intVal6 = -intVal3;

                                    for (int index = 0; index < local.items().size(); index++) {
                                       ItemStack local2 = ((ItemStackEntry)local.items().get(index)).stack();
                                       int var55Var5718Value = intVal5 + index * 18;
                                       arg.drawItem(local2, var55Var5718Value, intVal6);
                                       arg.drawStackOverlay(minecraftClient.textRenderer, local2, var55Var5718Value, intVal6, null);
                                    }
                                 }

                                 var0Value.popMatrix();
                              }
                           }
                        }
                     }
                  }
               }

               instanceSnapshot.renderDroppedItems(arg, floatVal, var0Value, minecraftClientValue, minecraftClientValue2, textStyleUtilValue2PosY, textStyleUtilValue2PosX, textStyleUtilValue2PosZ);
            }
         }
      }
   }

   public void renderDroppedItems(DrawContext arg, float floatVal, Matrix3x2fStack matrix3x2fStack, double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5) {
      if ((Boolean)this.droppedItemsSetting.getValue()) {
         float floatVal2 = 64.0F;

         try {
            floatVal2 = (Float)this.rangeSetting.getValue();
         } catch (Throwable error) {
         }

         float floatVal3 = 1.0F;

         try {
            floatVal3 = (Float)this.scaleSetting.getValue();
         } catch (Throwable error2) {
         }

         float floatVal4 = 1.0F;

         try {
            floatVal4 = Math.max(0.1F, Math.min(1.0F, ((Integer)this.opacitySetting.getValue()).intValue() / 100.0F));
         } catch (Throwable error3) {
         }

         boolean trueSnapshot = true;

         try {
            trueSnapshot = (Boolean)this.itemAmountSetting.getValue();
         } catch (Throwable error4) {
         }

         int maxValue = Math.max(16, Math.round(floatVal2));

         for (ItemEntity class1542 : minecraftClient.world
            .getEntitiesByClass(ItemEntity.class, minecraftClient.player.getBoundingBox().expand(maxValue, maxValue, maxValue), item -> true)) {
            ItemStack var20Value = class1542.getStack();
            if (var20Value != null && !var20Value.isEmpty()) {
               double class3532Value = MathHelper.lerp(floatVal, class1542.lastRenderX, class1542.getX());
               double class3532Value2 = MathHelper.lerp(floatVal, class1542.lastRenderY, class1542.getY());
               double class3532Value3 = MathHelper.lerp(floatVal, class1542.lastRenderZ, class1542.getZ());
               double var22DoubleVal3Value = class3532Value - doubleVal3;
               double var24DoubleVal4Value = class3532Value2 - doubleVal4;
               double var26DoubleVal5Value = class3532Value3 - doubleVal5;
               if (!(var22DoubleVal3Value * var22DoubleVal3Value + var24DoubleVal4Value * var24DoubleVal4Value + var26DoubleVal5Value * var26DoubleVal5Value > (double)maxValue * maxValue)) {
                  LineSegmentBounds lineSegmentBoundsInst = new LineSegmentBounds();
                  if (WorldToScreenUtil.projectLineSegment(WorldToScreenUtil.modelViewMatrix, WorldToScreenUtil.projectionMatrix2, class3532Value, class3532Value2 + 0.4, class3532Value3, lineSegmentBoundsInst)
                     && lineSegmentBoundsInst.isVisible
                     && !(lineSegmentBoundsInst.lineHitX < 0.0)
                     && !(lineSegmentBoundsInst.lineHitX > 1.0)
                     && !(lineSegmentBoundsInst.healthBarWidth <= 0.0)) {
                     double doubleValLineSegmentBounds = doubleVal / lineSegmentBoundsInst.healthBarWidth;
                     double doubleVal2LineSegmentBound = doubleVal2 / lineSegmentBoundsInst.healthBarWidth;
                     float floatVal5 = (float)((doubleValLineSegmentBounds + doubleVal2LineSegmentBound) * 0.5);
                     if (Float.isFinite(floatVal5) && !(floatVal5 <= 0.0F)) {
                        double var22DoubleVal3Value2 = class3532Value - doubleVal3;
                        double var26DoubleVal5Value2 = class3532Value3 - doubleVal5;
                        double sqrtValue = Math.sqrt(var22DoubleVal3Value2 * var22DoubleVal3Value2 + var26DoubleVal5Value2 * var26DoubleVal5Value2);
                        if (sqrtValue >= 20.0) {
                           int intVal = (int)((sqrtValue - 20.0) / 10.0) + 1;
                           floatVal5 *= (float)Math.pow(2.0, intVal);
                        }

                        String var21Value = var20Value.getName().getString() + (trueSnapshot && var20Value.getCount() > 1 ? " x" + var20Value.getCount() : "");
                        int customFontManagerValue = CustomFontManager.INSTANCE7.getStringWidth(var21Value);
                        floatVal5 *= 1.2F * floatVal3;
                        if (floatVal5 > 3.0F) {
                           floatVal5 = 3.0F;
                        }

                        matrix3x2fStack.pushMatrix();
                        translateTo(matrix3x2fStack, (float)lineSegmentBoundsInst.x, (float)lineSegmentBoundsInst.y, floatVal5);
                        if ((Boolean)this.panelSetting.getValue()) {
                           GuiRenderUtil.fillRoundedRect(
                              arg, -(customFontManagerValue / 2.0F) - 3.0F, -2.0F, customFontManagerValue + 6.0F, 14.0F, 4.0F, setAlphaChannel(ClickGuiModule.getBackgroundColorArgb(), (int)(150.0F * floatVal4)), false
                           );
                           GuiRenderUtil.strokeRoundedRect(
                              arg,
                              -(customFontManagerValue / 2.0F) - 3.0F,
                              -2.0F,
                              customFontManagerValue + 6.0F,
                              14.0F,
                              4.0F,
                              1.0F,
                              setAlphaChannel(ClickGuiModule.getAccentColorArgb(), (int)(90.0F * floatVal4)),
                              false
                           );
                        }

                        CustomFontManager.INSTANCE7.drawText(arg, var21Value, -(customFontManagerValue / 2), -1.0F, scaleAlpha(-1, floatVal4));
                        matrix3x2fStack.popMatrix();
                     }
                  }
               }
            }
         }
      }
   }

   public static void renderPanel(DrawContext arg, EntityHudEntry entityHudEntry, float floatVal) {
      int intVal = entityHudEntry.nameLabel() != null ? 1 : 0;
      boolean flag = entityHudEntry.healthData() != null;
      boolean flag2 = !entityHudEntry.items().isEmpty();
      if (intVal != 0 || flag || flag2) {
         int intVal2 = getPanelLeftOffset(intVal != 0);
         int intVal3 = getPanelWidth(intVal != 0, flag);
         float floatVal2 = 9.0F;
         float floatValue = Float.MAX_VALUE;
         float floatVal3 = -Float.MAX_VALUE;
         float floatVal4 = 0.0F;
         if (intVal != 0) {
            floatValue = Math.min(floatValue, 0.0F);
            floatVal3 = Math.max(floatVal3, 0.0F + floatVal2);
            floatVal4 = Math.max(floatVal4, entityHudEntry.nameWidth());
         }

         if (flag) {
            floatValue = Math.min(floatValue, -intVal2);
            floatVal3 = Math.max(floatVal3, intVal != 0 ? 9 : -intVal2 + 3);
            floatVal4 = Math.max(floatVal4, 60.0F);
         }

         if (flag2) {
            floatValue = Math.min(floatValue, -intVal3);
            floatVal3 = Math.max(floatVal3, intVal == 0 && !flag ? -intVal3 + 16 : 9);
            floatVal4 = Math.max(floatVal4, entityHudEntry.itemRowWidth());
         }

         float var116Value = floatVal4 + 6.0F;
         float var10Var96Value = floatVal3 - floatValue + 6.0F;
         float floatVal5 = -(var116Value / 2.0F);
         float var93Value = floatValue - 3.0F;
         int intVal4 = setAlphaChannel(ClickGuiModule.getBackgroundColorArgb(), (int)(150.0F * floatVal));
         int intVal5 = setAlphaChannel(ClickGuiModule.getAccentColorArgb(), (int)(90.0F * floatVal));
         GuiRenderUtil.fillRoundedRect(arg, floatVal5, var93Value, var116Value, var10Var96Value, 4.0F, intVal4, false);
         GuiRenderUtil.strokeRoundedRect(arg, floatVal5, var93Value, var116Value, var10Var96Value, 4.0F, 1.0F, intVal5, false);
      }
   }

   public static int setAlphaChannel(int intVal, int intVal2) {
      return (intVal2 & 0xFF) << 24 | intVal & 16777215;
   }

   public static int getPanelLeftOffset(boolean flag) {
      return flag ? 7 : 0;
   }

   public static int getPanelWidth(boolean flag, boolean flag2) {
      if (flag2) {
         return flag ? 27 : 22;
      } else {
         return flag ? 21 : 0;
      }
   }

   public boolean isTrackablePlayer(LivingEntity arg) {
      if (!arg.isAlive() || arg.isSpectator() || arg instanceof ArmorStandEntity || !(arg instanceof PlayerEntity)) {
         return false;
      } else if (arg != minecraftClient.player && arg.isInvisibleTo(minecraftClient.player)) {
         return true;
      } else {
         return arg != minecraftClient.player
            ? true
            : (Boolean)this.selfSetting.getValue()
               && (!minecraftClient.options.getPerspective().isFirstPerson() || FreecamModule.freecamInstance != null && FreecamModule.freecamInstance.isEnabled());
      }
   }

   public boolean call2(LivingEntity arg, double doubleVal) {
      float floatVal = 64.0F;

      try {
         floatVal = (Float)this.rangeSetting.getValue();
      } catch (Throwable error) {
      }

      return this.isTrackablePlayer(arg) && !isGuiOpen() ? doubleVal <= (double)floatVal * floatVal : false;
   }

   @Override
   public void onEnable() {
      this.entryCache.clear();
      this.lastSettingsMask = Integer.MIN_VALUE;
   }

   @Override
   public void onDisable() {
      this.entryCache.clear();
   }

   @Override
   public void onTick() {
   }

   public Text buildNameLabel(LivingEntity arg) {
      MutableText class2561Value = Text.empty();
      byte byteVal = 0;
      if ((Boolean)this.nameSetting.getValue()) {
         class2561Value.append(Text.literal("| ").formatted(Formatting.DARK_AQUA));
         MutableText var1Value = arg.getDisplayName().copy();
         if (NameProtectModule.nameProtectModule != null && NameProtectModule.nameProtectModule.isEnabled()) {
            String minecraftClientValue = minecraftClient.getSession() != null ? minecraftClient.getSession().getUsername() : null;
            if (minecraftClientValue != null && !minecraftClientValue.isEmpty()) {
               var1Value = Text.literal(var1Value.getString().replace(minecraftClientValue, NameProtectModule.nameProtectModule.getMaskedName()));
            }
         }

         class2561Value.append(var1Value.formatted(Formatting.WHITE));
      }

      if ((Boolean)this.pingSetting.getValue() && arg instanceof PlayerEntity local) {
         int intVal = this.getPing(local);
         if (intVal >= 0) {
            if (byteVal != 0) {
               class2561Value.append(Text.literal(" ").formatted(Formatting.GRAY));
            }

            class2561Value.append(Text.literal("[").formatted(Formatting.DARK_GRAY));
            class2561Value.append(Text.literal(intVal + " ms").formatted(this.getPingColor(intVal)));
            class2561Value.append(Text.literal("]").formatted(Formatting.DARK_GRAY));
         }
      }

      if ((Boolean)this.healthSetting.getValue()) {
         float maxValue = Math.max(0.0F, arg.getAbsorptionAmount());
         if (maxValue > 0.0F) {
            int maxValue2 = Math.max(1, MathHelper.ceil(maxValue));
            if (byteVal != 0) {
               class2561Value.append(Text.literal(" ").formatted(Formatting.GRAY));
            }

            class2561Value.append(Text.literal("+" + maxValue2).formatted(Formatting.GOLD));
         }
      }

      if ((Boolean)this.distanceSetting.getValue() && arg != minecraftClient.player && minecraftClient.player != null) {
         try {
            int intVal2 = (int)Math.sqrt(minecraftClient.player.squaredDistanceTo(arg));
            if (byteVal != 0) {
               class2561Value.append(Text.literal(" ").formatted(Formatting.GRAY));
            }

            class2561Value.append(Text.literal(intVal2 + "m").formatted(Formatting.GRAY));
         } catch (Throwable error) {
         }
      }

      return byteVal != 0 ? class2561Value : null;
   }

   public HealthBarLayout buildHealthBarLayout(LivingEntity arg) {
      if (!(Boolean)this.healthSetting.getValue()) {
         return null;
      }

      float maxValue = Math.max(1.0F, arg.getMaxHealth());
      float class3532Value = MathHelper.clamp(arg.getHealth(), 0.0F, maxValue);
      float maxValue2 = Math.max(0.0F, arg.getAbsorptionAmount());
      int maxValue3 = Math.max(1, MathHelper.ceil(maxValue / 2.0F));
      if (maxValue3 > 10) {
         float floatVal = 10.0F / maxValue3;
         class3532Value *= floatVal;
         maxValue2 *= floatVal;
      }

      int class3532Value2 = MathHelper.clamp(Math.round(class3532Value), 0, maxValue3 * 2);
      int var152Value = class3532Value2 / 2;
      boolean flag = (class3532Value2 & 1) != 0;
      int maxValue4 = Math.max(0, maxValue3 - var152Value - (flag ? 1 : 0));
      int maxValue5 = Math.max(0, Math.round(maxValue2));
      int var102Value = maxValue5 / 2;
      boolean flag2 = (maxValue5 & 1) != 0;
      int var5Var11Var1210Value = maxValue3 + var102Value + (flag2 ? 1 : 0);
      if (var152Value <= 0 && !flag && var102Value <= 0 && !flag2 && maxValue4 <= 0) {
         return null;
      }

      int intVal = (var5Var11Var1210Value - 1) * 8 + 9;
      return new HealthBarLayout(maxValue3, var152Value, flag, maxValue4, var102Value, flag2, intVal);
   }

   public List buildItemRows(LivingEntity arg) {
      ArrayList arrayListInst = new ArrayList(6);
      if ((Boolean)this.offHandSetting.getValue()) {
         this.addRowIfPresent(arrayListInst, arg.getOffHandStack());
      }

      if ((Boolean)this.armorSetting.getValue()) {
         this.addRowIfPresent(arrayListInst, arg.getEquippedStack(EquipmentSlot.FEET));
         this.addRowIfPresent(arrayListInst, arg.getEquippedStack(EquipmentSlot.LEGS));
         this.addRowIfPresent(arrayListInst, arg.getEquippedStack(EquipmentSlot.CHEST));
         this.addRowIfPresent(arrayListInst, arg.getEquippedStack(EquipmentSlot.HEAD));
      }

      if ((Boolean)this.mainHandSetting.getValue()) {
         this.addRowIfPresent(arrayListInst, arg.getMainHandStack());
      }

      return arrayListInst;
   }

   public void addRowIfPresent(List list, ItemStack arg) {
      if (arg != null && !arg.isEmpty()) {
         list.add(new ItemStackEntry(arg.copy()));
      }
   }

   public static void drawItemRow(DrawContext arg, List list, int intVal, boolean flag, boolean flag2) {
      if (!list.isEmpty()) {
         int intVal2 = -(intVal / 2);
         int intVal3 = flag2 ? 34 : (flag ? 16 : 0);
         int intVal4 = -intVal3;

         for (int index = 0; index < list.size(); index++) {
            ItemStack local = ((ItemStackEntry)list.get(index)).stack();
            int var5Var818Value = intVal2 + index * 18;
            arg.drawItem(local, var5Var818Value, intVal4);
            arg.drawStackOverlay(minecraftClient.textRenderer, local, var5Var818Value, intVal4, null);
         }
      }
   }

   public static void renderNameLine(DrawContext arg, Text arg2, int intVal, int intVal2, int intVal3, boolean flag, float floatVal) {
      if (arg2 != null) {
         int maxValue = Math.max(intVal, intVal2);
         int intVal4 = -(maxValue / 2);
         int intVal5 = -intVal3;
         List<Object[]> local = splitStyledParts(arg2);
         if (flag) {
            for (int index = -1; index <= 1; index++) {
               for (int index2 = -1; index2 <= 1; index2++) {
                  if (index != 0 || index2 != 0) {
                     float var8Snapshot = intVal4;

                     for (Object[] object : local) {
                        CustomFontManager.INSTANCE7.drawText(arg, (String)object[0], var8Snapshot + index, (float)intVal5 + index2, scaleAlpha(-16777216, floatVal));
                        var8Snapshot += CustomFontManager.INSTANCE7.getStringWidth((String)object[0]);
                     }
                  }
               }
            }
         }

         float var8Snapshot2 = intVal4;

         for (Object[] object2 : local) {
            CustomFontManager.INSTANCE7.drawText(arg, (String)object2[0], var8Snapshot2, intVal5, scaleAlpha((Integer)object2[1], floatVal));
            var8Snapshot2 += CustomFontManager.INSTANCE7.getStringWidth((String)object2[0]);
         }
      }
   }

   public static int scaleAlpha(int intVal, float floatVal) {
      int maxValue = Math.max(0, Math.min(255, (int)((intVal >>> 24 & 0xFF) * floatVal)));
      return maxValue << 24 | intVal & 16777215;
   }

   public static List<Object[]> splitStyledParts(Text arg) {
      ArrayList<Object[]> arrayListInst = new ArrayList();
      arg.visit((local, local2) -> {
         if (!local2.isEmpty()) {
            TextColor local3 = local.getColor();
            arrayListInst.add(new Object[]{local2, local3 != null ? 0xFF000000 | local3.getRgb() : -1});
         }

         return Optional.empty();
      }, Style.EMPTY);
      return arrayListInst;
   }

   public static void renderHealthBar(DrawContext arg, HealthBarLayout healthBarLayout, int intVal) {
      if (healthBarLayout != null) {
         byte byteVal = 60;
         int intVal2 = -(byteVal / 2);
         int intVal3 = -intVal;
         int maxValue = Math.max(1, healthBarLayout.baseHeartCount() * 2);
         int maxValue2 = Math.max(0, Math.min(maxValue, healthBarLayout.fullHearts() * 2 + (healthBarLayout.halfHeart() ? 1 : 0)));
         float floatVal = (float)maxValue2 / maxValue;
         arg.fill(intVal2, intVal3, intVal2 + byteVal, intVal3 + 3, -1610612736);
         int roundValue = Math.round(byteVal * floatVal);
         if (roundValue > 0) {
            arg.fill(intVal2 + byteVal - roundValue, intVal3, intVal2 + byteVal, intVal3 + 3, -13722113);
         }
      }
   }

   public static void drawHeart(DrawContext arg, Identifier arg2, int intVal, int intVal2) {
      arg.drawGuiTexture(RenderPipelines.GUI_TEXTURED, arg2, intVal, intVal2, 9, 9);
   }

   public void refreshCache() {
      int intVal = this.getSettingsMask();
      if (intVal != this.lastSettingsMask) {
         this.lastSettingsMask = intVal;
         this.entryCache.clear();
      }
   }

   public void pruneCache() {
      if (minecraftClient.world != null && this.entryCache.size() > minecraftClient.world.getPlayers().size() + 8) {
         this.entryCache.keySet().removeIf(item -> {
            return minecraftClient.world.getPlayerByUuid(item) == null;
         });
      }
   }

   public int getSettingsMask() {
      short shortVal = 0;
      if ((Boolean)this.selfSetting.getValue()) {
         shortVal = (short)(shortVal | 1);
      }

      if ((Boolean)this.nameSetting.getValue()) {
         shortVal = (short)(shortVal | 2);
      }

      if ((Boolean)this.pingSetting.getValue()) {
         shortVal = (short)(shortVal | 4);
      }

      if ((Boolean)this.healthSetting.getValue()) {
         shortVal = (short)(shortVal | 8);
      }

      if ((Boolean)this.mainHandSetting.getValue()) {
         shortVal = (short)(shortVal | 16);
      }

      if ((Boolean)this.offHandSetting.getValue()) {
         shortVal = (short)(shortVal | 32);
      }

      if ((Boolean)this.armorSetting.getValue()) {
         shortVal = (short)(shortVal | 64);
      }

      if ((Boolean)this.panelSetting.getValue()) {
         shortVal = (short)(shortVal | 128);
      }

      if ((Boolean)this.distanceSetting.getValue()) {
         shortVal = (short)(shortVal | 256);
      }

      if ((Boolean)this.itemAmountSetting.getValue()) {
         shortVal = (short)(shortVal | 512);
      }

      return shortVal;
   }

   public EntityHudEntry getOrCreateEntry(PlayerEntity arg, long longVal) {
      EntityHudEntry local = (EntityHudEntry)this.entryCache.get(arg.getUuid());
      if (local != null && local.expiresAtMs() > longVal) {
         return local;
      }

      Text local2 = this.buildNameLabel(arg);
      List local3 = this.buildItemRows(arg);
      int local4 = 0;
      if (local2 != null) {
         for (Object[] object : splitStyledParts(local2)) {
            local4 += CustomFontManager.INSTANCE7.getStringWidth((String)object[0]);
         }
      }

      EntityHudEntry entityHudEntryInst = new EntityHudEntry(longVal + 125L, local2, local4, this.buildHealthBarLayout(arg), local3, local3.isEmpty() ? 0 : local3.size() * 16 + (local3.size() - 1) * 2);
      this.entryCache.put(arg.getUuid(), entityHudEntryInst);
      return entityHudEntryInst;
   }

   public static void translateTo(Matrix3x2fStack matrix3x2fStack, float floatVal, float floatVal2, float floatVal3) {
      matrix3x2fStack.translate(floatVal, floatVal2);
      matrix3x2fStack.scale(floatVal3, floatVal3);
   }

   public float projectToScreen(PlayerEntity arg, float floatVal, double doubleVal, double doubleVal2, double doubleVal3, double doubleVal4, double doubleVal5) {
      Vec3d var1Value = arg.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, arg.getLerpedYaw(floatVal));
      double var3Snapshot;
      double var5Var1Value;
      double var7Snapshot;
      if (var1Value == null) {
         var3Snapshot = doubleVal;
         var5Var1Value = doubleVal2 + arg.getHeight() + 0.5 + 0.62;
         var7Snapshot = doubleVal3;
      } else {
         var3Snapshot = doubleVal + var1Value.x;
         var5Var1Value = doubleVal2 + var1Value.y + 0.62;
         var7Snapshot = doubleVal3 + var1Value.z;
      }

      if (!WorldToScreenUtil.projectLineSegment(WorldToScreenUtil.modelViewMatrix, WorldToScreenUtil.projectionMatrix2, var3Snapshot, var5Var1Value, var7Snapshot, this.lineBounds)) {
         return 0.0F;
      } else if (this.lineBounds.isVisible
         && !(this.lineBounds.lineHitX < 0.0)
         && !(this.lineBounds.lineHitX > 1.0)
         && !(this.lineBounds.healthBarWidth <= 0.0)) {
         double doubleVal4ThisValue = doubleVal4 / this.lineBounds.healthBarWidth;
         double doubleVal5ThisValue = doubleVal5 / this.lineBounds.healthBarWidth;
         float floatVal2 = (float)((doubleVal4ThisValue + doubleVal5ThisValue) * 0.5);
         return Float.isFinite(floatVal2) && floatVal2 > 0.0F ? floatVal2 : 0.0F;
      } else {
         return 0.0F;
      }
   }

   public static boolean isGuiOpen() {
      return minecraftClient.currentScreen instanceof ClickGuiScreen;
   }

   public int getPing(PlayerEntity arg) {
      if (minecraftClient.getNetworkHandler() == null) {
         return -1;
      }

      PlayerListEntry minecraftClientValue = minecraftClient.getNetworkHandler().getPlayerListEntry(arg.getUuid());
      return minecraftClientValue != null ? minecraftClientValue.getLatency() : -1;
   }

   public Formatting getPingColor(int intVal) {
      if (intVal < 75) {
         return Formatting.GREEN;
      } else {
         return intVal < 150 ? Formatting.YELLOW : Formatting.RED;
      }
   }

   public String stripColorCodes(String string) {
      return string != null && !string.isEmpty() ? colorCodePattern.matcher(string).replaceAll("").trim() : "";
   }

   public boolean isProbablyPlayerName(String string) {

      for (int index = 0; index < string.length(); index++) {
         if (Character.isLetter(string.charAt(index))) {
            return true;
         }
      }

      return false;
   }

   public String sanitizeText(String string) {
      return string == null ? "" : string.replace('\n', ' ').replace('\r', ' ');
   }

   public String normalizeSearchText(String string) {
      return string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
   }

}
