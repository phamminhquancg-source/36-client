package com.threesix.module;

import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.option.Perspective;
import org.joml.Vector3d;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public class FreecamModule extends ModuleBase {
   public static final float minSpeedValue = 0.1F;
   public static final float maxSpeedValue = 10.0F;
   public static final float speedStepValue = 0.2F;
   public final Vector3d cameraPos = new Vector3d();
   public final Vector3d previousCameraPos = new Vector3d();
   public final Vector3d movementDelta = new Vector3d();
   public float cameraYaw;
   public float cameraPitch;
   public float previousCameraYaw;
   public float previousCameraPitch;
   public final ClientSetting speedSetting = new ClientSetting("Speed", 1.0F, 0.1F, 10.0F);
   public final ClientSetting smoothSetting = new ClientSetting("Smooth", Boolean.TRUE);
   public final ClientSetting keepSneakSetting = new ClientSetting("Keep Sneak", Boolean.FALSE);
   public final ClientSetting stickToEyeSetting = new ClientSetting("Stick to Eye", Boolean.FALSE);
   public float eyeDistance = 0.5F;
   public float currentSpeed;
   public Perspective previousCameraEntity;
   public boolean wasNoClip;
   public long lastFrameMillis;
   public float playerYaw;
   public float playerPitch;
   public boolean wasSneaking;
   public static FreecamModule freecamInstance;

   public FreecamModule() {
      super("Freecam", ModuleCategory.RENDER);
      freecamInstance = this;
      this.registerSetting(this.speedSetting);
      this.registerSetting(this.smoothSetting);
      this.registerSetting(this.keepSneakSetting);
      this.registerSetting(this.stickToEyeSetting);
   }

   @Override
   public void onEnable() {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         this.previousCameraEntity = minecraftClient.options.getPerspective();
         this.wasNoClip = minecraftClient.chunkCullingEnabled;
         minecraftClient.chunkCullingEnabled = false;
         this.playerYaw = minecraftClient.player.getYaw();
         this.playerPitch = minecraftClient.player.getPitch();
         this.wasSneaking = minecraftClient.player.isSneaking();
         this.cameraYaw = minecraftClient.player.getYaw();
         this.cameraPitch = minecraftClient.player.getPitch();
         Vec3d minecraftClientValue = minecraftClient.player.getCameraPosVec(1.0F);
         this.cameraPos.set(minecraftClientValue.x, minecraftClientValue.y, minecraftClientValue.z);
         this.previousCameraPos.set(minecraftClientValue.x, minecraftClientValue.y, minecraftClientValue.z);
         this.previousCameraYaw = this.cameraYaw;
         this.previousCameraPitch = this.cameraPitch;
         this.lastFrameMillis = System.nanoTime() / 1000000L;
         this.movementDelta.set(0.0, 0.0, 0.0);
         this.currentSpeed = this.getSpeed();
      } else {
         this.toggle();
      }
   }

   @Override
   public void onDisable() {

      if (minecraftClient.player != null) {
         minecraftClient.player.setYaw(this.playerYaw);
         minecraftClient.player.setPitch(this.playerPitch);
         minecraftClient.player.setHeadYaw(this.playerYaw);
         minecraftClient.player.setBodyYaw(this.playerYaw);
         boolean minecraftClientValue = minecraftClient.options.sneakKey.isPressed();
         if ((Boolean)this.keepSneakSetting.getValue() && this.wasSneaking) {
         }

         minecraftClient.player.setSneaking(minecraftClientValue);
         if (minecraftClient.player.input != null && minecraftClient.getNetworkHandler() != null) {
            PlayerInput minecraftClientValue2 = minecraftClient.player.input.playerInput;
            if (minecraftClientValue2.sneak() != minecraftClientValue) {
               PlayerInput local = new PlayerInput(
                  minecraftClientValue2.forward(), minecraftClientValue2.backward(), minecraftClientValue2.left(), minecraftClientValue2.right(), minecraftClientValue2.jump(), minecraftClientValue, minecraftClientValue2.sprint()
               );
               minecraftClient.player.input.playerInput = local;
               minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local));
            }
         }
      }

      if (this.previousCameraEntity != null) {
         minecraftClient.options.setPerspective(this.previousCameraEntity);
      } else {
         minecraftClient.options.setPerspective(Perspective.FIRST_PERSON);
      }

      minecraftClient.chunkCullingEnabled = this.wasNoClip;
      this.movementDelta.set(0.0, 0.0, 0.0);
      this.currentSpeed = this.getSpeed();
   }

   @Override
   public void onTick() {
      if (minecraftClient.player != null && this.isEnabled()) {
         this.updateCameraMovement();
         if ((Boolean)this.keepSneakSetting.getValue() && this.wasSneaking) {
            minecraftClient.player.setSneaking(true);
            if (minecraftClient.player.input != null && minecraftClient.getNetworkHandler() != null) {
               PlayerInput minecraftClientValue = minecraftClient.player.input.playerInput;
               if (!minecraftClientValue.sneak()) {
                  PlayerInput local = new PlayerInput(
                     minecraftClientValue.forward(), minecraftClientValue.backward(), minecraftClientValue.left(), minecraftClientValue.right(), minecraftClientValue.jump(), true, minecraftClientValue.sprint()
                  );
                  minecraftClient.player.input.playerInput = local;
                  minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local));
               }
            }
         }
      } else if (minecraftClient.player != null && (Boolean)this.keepSneakSetting.getValue() && this.wasSneaking) {
         minecraftClient.player.setSneaking(true);
         if (minecraftClient.player.input != null && minecraftClient.getNetworkHandler() != null) {
            PlayerInput minecraftClientValue2 = minecraftClient.player.input.playerInput;
            if (!minecraftClientValue2.sneak()) {
               PlayerInput local2 = new PlayerInput(
                  minecraftClientValue2.forward(), minecraftClientValue2.backward(), minecraftClientValue2.left(), minecraftClientValue2.right(), minecraftClientValue2.jump(), true, minecraftClientValue2.sprint()
               );
               minecraftClient.player.input.playerInput = local2;
               minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local2));
            }
         }
      }
   }

   public void updateCameraMovement() {
      if (minecraftClient.player != null) {
         this.previousCameraPos.set(this.cameraPos);
         this.previousCameraYaw = this.cameraYaw;
         this.previousCameraPitch = this.cameraPitch;
         float floatVal = 0.05F;
         float floatVal2 = (float)Math.toRadians(this.cameraYaw);
         double doubleVal = -Math.sin(floatVal2);
         double cosValue = Math.cos(floatVal2);
         double doubleVal2 = -Math.cos(floatVal2);
         double doubleVal3 = -Math.sin(floatVal2);
         double doubleVal4 = 0.0;
         double doubleVal5 = 0.0;
         double doubleVal6 = 0.0;
         double doubleVal7 = this.currentSpeed * 2.0;
         if (minecraftClient.options != null && minecraftClient.options.sprintKey.isPressed()) {
            doubleVal7 *= 2.0;
         }

         if (minecraftClient.options.forwardKey.isPressed()) {
            doubleVal4 += doubleVal * doubleVal7;
            doubleVal6 += cosValue * doubleVal7;
         }

         if (minecraftClient.options.backKey.isPressed()) {
            doubleVal4 -= doubleVal * doubleVal7;
            doubleVal6 -= cosValue * doubleVal7;
         }

         if (minecraftClient.options.rightKey.isPressed()) {
            doubleVal4 += doubleVal2 * doubleVal7;
            doubleVal6 += doubleVal3 * doubleVal7;
         }

         if (minecraftClient.options.leftKey.isPressed()) {
            doubleVal4 -= doubleVal2 * doubleVal7;
            doubleVal6 -= doubleVal3 * doubleVal7;
         }

         if (minecraftClient.options.jumpKey.isPressed()) {
            doubleVal5 += doubleVal7;
         }

         boolean flag = (Boolean)this.keepSneakSetting.getValue() && this.wasSneaking;
         boolean minecraftClientValue = minecraftClient.options.sneakKey.isPressed();
         if (!minecraftClientValue) {
            if (!flag
               && minecraftClient.player != null
               && minecraftClient.player.isTouchingWater()
               && minecraftClient.getNetworkHandler() != null
               && minecraftClient.player.input != null) {
               PlayerInput minecraftClientValue2 = minecraftClient.player.input.playerInput;
               if (minecraftClientValue2.sneak()) {
                  PlayerInput local = new PlayerInput(
                     minecraftClientValue2.forward(), minecraftClientValue2.backward(), minecraftClientValue2.left(), minecraftClientValue2.right(), minecraftClientValue2.jump(), false, minecraftClientValue2.sprint()
                  );
                  minecraftClient.player.input.playerInput = local;
                  minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local));
               }
            }
         } else {
            boolean minecraftClientValue3 = minecraftClient.player != null && minecraftClient.player.isTouchingWater();
            double var17MinecraftClientValue3 = doubleVal7 * (minecraftClientValue3 ? 1.8 : 1.0);
            doubleVal5 -= var17MinecraftClientValue3;
            if (minecraftClientValue3 && minecraftClient.getNetworkHandler() != null && minecraftClient.player.input != null) {
               PlayerInput minecraftClientValue4 = minecraftClient.player.input.playerInput;
               if (!minecraftClientValue4.sneak()) {
                  PlayerInput local2 = new PlayerInput(
                     minecraftClientValue4.forward(), minecraftClientValue4.backward(), minecraftClientValue4.left(), minecraftClientValue4.right(), minecraftClientValue4.jump(), true, minecraftClientValue4.sprint()
                  );
                  minecraftClient.player.input.playerInput = local2;
                  minecraftClient.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(local2));
               }
            }
         }

         double doubleVal8 = 5.0;
         if ((Boolean)this.smoothSetting.getValue()) {
            double doubleVal9 = 1.0 - Math.pow(0.001, floatVal);
            this.movementDelta.x = MathHelper.lerp(doubleVal9, this.movementDelta.x, doubleVal4 * doubleVal8);
            this.movementDelta.y = MathHelper.lerp(doubleVal9, this.movementDelta.y, doubleVal5 * doubleVal8);
            this.movementDelta.z = MathHelper.lerp(doubleVal9, this.movementDelta.z, doubleVal6 * doubleVal8);
         } else {
            this.movementDelta.set(doubleVal4 * doubleVal8, doubleVal5 * doubleVal8, doubleVal6 * doubleVal8);
         }

         this.cameraPos.x = this.cameraPos.x + this.movementDelta.x * floatVal;
         this.cameraPos.y = this.cameraPos.y + this.movementDelta.y * floatVal;
         this.cameraPos.z = this.cameraPos.z + this.movementDelta.z * floatVal;
      }
   }

   public void addSpeed(double doubleVal) {

      float currentSpeedSnapshot = this.currentSpeed;
      float floatVal = 0.5F;
      float var3FloatVar1Var4Value = currentSpeedSnapshot + (float)doubleVal * floatVal;
      this.currentSpeed = MathHelper.clamp(var3FloatVar1Var4Value, 0.1F, 10.0F);
   }

   public void setFreecamRotation(double doubleVal, double doubleVal2) {

      this.cameraYaw += (float)doubleVal;
      this.cameraPitch += (float)doubleVal2;
      this.cameraYaw = MathHelper.wrapDegrees(this.cameraYaw);
      this.cameraPitch = MathHelper.clamp(this.cameraPitch, -90.0F, 90.0F);
   }

   public double getCameraX(float floatVal) {
      return MathHelper.lerp(floatVal, this.previousCameraPos.x, this.cameraPos.x);
   }

   public double getCameraY(float floatVal) {
      return MathHelper.lerp(floatVal, this.previousCameraPos.y, this.cameraPos.y);
   }

   public double getCameraZ(float floatVal) {
      return MathHelper.lerp(floatVal, this.previousCameraPos.z, this.cameraPos.z);
   }

   public float getCameraYaw(float floatVal) {
      return MathHelper.lerp(floatVal, this.previousCameraYaw, this.cameraYaw);
   }

   public float getCameraPitch(float floatVal) {

      return MathHelper.lerp(floatVal, this.previousCameraPitch, this.cameraPitch);
   }

   public float getSensitivityScale() {
      return this.eyeDistance;
   }

   public void setSpeedFromScroll(double doubleVal) {
      if (doubleVal != 0.0) {
         float floatVal = this.currentSpeed + (float)Math.signum(doubleVal) * 0.2F;
         this.currentSpeed = MathHelper.clamp(floatVal, 0.1F, 10.0F);
      }
   }

   public float getSpeed() {
      return MathHelper.clamp((Float)this.speedSetting.getValue(), 0.1F, 10.0F);
   }

   public boolean isStickToEye() {
      return (Boolean)this.stickToEyeSetting.getValue();
   }

   public static Vec3d getFreecamEyePos(Vec3d arg, float floatVal) {
      return freecamInstance != null && freecamInstance.isEnabled() && freecamInstance.isStickToEye() && minecraftClient.player != null
         ? minecraftClient.player.getCameraPosVec(floatVal)
         : arg;
   }

}
