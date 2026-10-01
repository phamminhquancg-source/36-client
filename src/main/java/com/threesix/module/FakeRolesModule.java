package com.threesix.module;

import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.text.MutableText;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ModeSetting02;
import com.threesix.data.ModuleCategory;

public final class FakeRolesModule extends ModuleBase {
   public static FakeRolesModule instance;
   public static final String roleNone = "None";
   public static final String roleSrMod = "SRMod";
   public static final String roleMedia = "Media";
   public static final String roleSrAdmin = "SRAdmin";
   public static final int roleModeNone = 0;
   public static final int roleModeMod = 1;
   public static final int roleModeMedia = 2;
   public static final int roleModeAdmin = 3;
   public static final int roleModeCount = 4;
   public final ModeSetting02 roleSetting = new ModeSetting02("Role", "None", "None", "SRMod", "Media", "SRAdmin");

   public FakeRolesModule() {
      super("FakeRoles", ModuleCategory.DONUT);
      instance = this;
   }

   public static boolean isFakeRolesActive() {
      return false;
   }

   public static String getHeaderPrefix() {
      return "";
   }

   public static String getNameTag() {
      return "";
   }

   public static Text applyFakeRoles(Text arg) {
      return arg;
   }

   public static Text buildRoleText(String string) {
      return Text.literal(string);
   }

   public static String getTabName() {
      return "";
   }

   public static Style getStyleNone() {
      return Style.EMPTY;
   }

   public static Style getStyleMod() {
      return Style.EMPTY;
   }

   public static Style getStyleForRoleIndex(int intVal) {
      return Style.EMPTY;
   }

   public static Style getStyleAdmin() {
      return Style.EMPTY;
   }

   public static String getFooter() {
      return "";
   }

   public static Text makeText1(String string) {
      return Text.literal(string);
   }

   public static Text makeText2(String string) {
      return Text.literal(string);
   }

   public static Text makeText4(String string) {
      return Text.literal(string);
   }

   public static Text makeText5(String string) {
      return Text.literal(string);
   }

   public static void applyRoleToPlayerList(MutableText arg, String string, Style arg2) {
   }

   public static Style getStyleForLevel(int intVal) {
      return Style.EMPTY;
   }

}
