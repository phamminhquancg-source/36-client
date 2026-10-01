package com.threesix.module;

import net.minecraft.util.math.MathHelper;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;
import com.threesix.module.ClickGuiModule;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;
import com.threesix.setting.ModeSetting02;
import com.threesix.manager.NotificationHudManager;
import com.threesix.internal.ModuleBase;

public final class FreeLookModule extends ModuleBase {
   public static final float minDistance = 1.0F;
   public static final float maxDistance = 15.0F;
   public static final float minSensitivity = 0.1F;
   public static final float maxSensitivity = 3.0F;
   public static FreeLookModule field1;
   public final ModeSetting02 modeSetting = new ModeSetting02("Mode", "Hold", new String[]{"Activation Mode"}, "Hold", "Toggle");
   public final ClientSetting distanceSetting = new ClientSetting("Distance", 4.0F, 1.0F, 15.0F);
   public final ClientSetting wallClipSetting = new ClientSetting("Wall Clip", true);
   public final ClientSetting sensitivitySetting = new ClientSetting("Sensitivity", 1.0F, 0.1F, 3.0F);
   public final ClientSetting invertYSetting = new ClientSetting("Invert Y", false);
   public boolean isFreeLookActive;
   public Perspective previousPerspective;
   public boolean previousThirdPerson = true;
   public float freeLookYaw;
   public float freeLookPitch;

   public static FreeLookModule freeLookInstance;

   public FreeLookModule() {
      super("FreeLook", ModuleCategory.MISC);
      field1 = this;
      this.registerSetting(this.modeSetting);
      this.registerSetting(this.distanceSetting);
      this.registerSetting(this.wallClipSetting);
      this.registerSetting(this.sensitivitySetting);
      this.registerSetting(this.invertYSetting);
   }

   @Override
   public void onEnable() {
      this.isFreeLookActive = false;
      this.previousPerspective = null;
      this.previousThirdPerson = true;
   }

   @Override
   public void onDisable() {
      this.stopFreeLook();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player == null || minecraftClient.options == null || minecraftClient.getWindow() == null) {
         this.stopFreeLook();
      } else if (this.isHoldMode()) {
         int intVal = this.getKeyCode();
         boolean flag = intVal > 0 && intVal < 349 && GLFW.glfwGetKey(minecraftClient.getWindow().getHandle(), intVal) == 1;
         if (flag) {
            this.startFreeLook();
         } else {
            this.stopFreeLook();
         }
      } else if (this.isFreeLookActive) {
         this.releaseFreeLook();
      }
   }

   @Override
   public void toggleByKey() {

      if (!this.isHoldMode()) {
         if (!this.isEnabled()) {
            this.toggle();
         } else if (this.isFreeLookActive) {
            this.stopFreeLook();
         } else {
            this.startFreeLook();
         }
      }
   }

   public boolean isThirdPersonActive() {

      return this.isEnabled() && this.isFreeLookActive && minecraftClient.player != null;
   }

   public void applyFreeLookDelta(double doubleVal, double doubleVal2) {
      if (this.isThirdPersonActive()) {
         double doubleVal3 = (Boolean)this.invertYSetting.getValue() ? -1.0 : 1.0;
         double doubleVal4 = 0.15 * this.getSensitivity();
         this.freeLookYaw = MathHelper.wrapDegrees(this.freeLookYaw + (float)(doubleVal * doubleVal4));
         this.freeLookPitch = MathHelper.clamp(this.freeLookPitch + (float)(doubleVal2 * doubleVal4 * doubleVal3), -90.0F, 90.0F);
      }
   }

   public float getTargetPitch() {
      return this.freeLookPitch;
   }

   public float getTargetYaw() {
      return this.freeLookYaw;
   }

   public float getThirdPersonDistance() {

      return MathHelper.clamp((Float)this.distanceSetting.getValue(), 1.0F, 15.0F);
   }

   public boolean isSmoothRotation() {
      return (Boolean)this.wallClipSetting.getValue();
   }

   public float getSensitivity() {
      return MathHelper.clamp((Float)this.sensitivitySetting.getValue(), 0.1F, 3.0F);
   }

   public boolean isHoldMode() {
      return this.modeSetting.isModeSelected2("Hold");
   }

   public void startFreeLook() {
      if (!this.isFreeLookActive && minecraftClient.player != null && minecraftClient.options != null) {
         this.previousPerspective = minecraftClient.options.getPerspective();
         this.previousThirdPerson = minecraftClient.chunkCullingEnabled;
         minecraftClient.options.setPerspective(Perspective.THIRD_PERSON_BACK);
         minecraftClient.chunkCullingEnabled = false;
         this.freeLookYaw = minecraftClient.player.getYaw();
         this.freeLookPitch = minecraftClient.player.getPitch();
         this.isFreeLookActive = true;
         this.notifyStateChange(true);
      } else {
         this.releaseFreeLook();
      }
   }

   public void releaseFreeLook() {
      if (this.isFreeLookActive
         && minecraftClient.options != null
         && (minecraftClient.options.getPerspective().isFirstPerson() || minecraftClient.options.getPerspective().isFrontView())) {
         minecraftClient.options.setPerspective(Perspective.THIRD_PERSON_BACK);
         minecraftClient.chunkCullingEnabled = false;
      }
   }

   public void stopFreeLook() {

      if (this.isFreeLookActive) {
         this.isFreeLookActive = false;
         if (minecraftClient.options != null) {
            minecraftClient.options.setPerspective(this.previousPerspective == null ? Perspective.FIRST_PERSON : this.previousPerspective);
         }

         minecraftClient.chunkCullingEnabled = this.previousThirdPerson;
         this.notifyStateChange(false);
      }
   }

   public void notifyStateChange(boolean flag) {
      try {

         if (ClickGuiModule.isStateChangeNotifyEnabled()) {
            NotificationHudManager.INSTANCE6.notifyWithIcon(this.getName2(), flag, this.getDisplayName());
         }
      } catch (Exception error) {
      }
   }

}
