package com.threesix.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.manager.ConfigManager;
import com.threesix.module.AutoMineModule;

@Mixin(MinecraftClient.class)
public class AutoMineMixin {
   private BlockPos autoMineLastPos = null;
   private boolean autoMineActive = false;

   @Inject(method = "tick", at = @At("HEAD"))
   private void onTick(CallbackInfo callbackInfo) {
      MinecraftClient local = (MinecraftClient)(Object)this;
      AutoMineModule local2 = (AutoMineModule)ConfigManager.INSTANCE.getModuleByName("AutoMine");
      if (local2 != null && local2.isEnabled()) {
         if (local.player != null && local.world != null && local.interactionManager != null && local.currentScreen == null) {
            boolean gLFWValue = GLFW.glfwGetMouseButton(local.getWindow().getHandle(), 0) == 1;
            if (gLFWValue) {
               this.autoMineActive = false;
               this.autoMineLastPos = null;
            } else {
               HitResult local3 = local.crosshairTarget;
               if (local3 != null && local3.getType() == Type.BLOCK) {
                  BlockHitResult local4 = (BlockHitResult)local3;
                  BlockPos var6Value = local4.getBlockPos();
                  BlockState local5 = local.world.getBlockState(var6Value);
                  if (local5.isAir() || local5.getHardness(local.world, var6Value) < 0.0F) {
                     this.autoMineLastPos = null;
                     this.autoMineActive = false;
                  } else if (local5.getHardness(local.world, var6Value) > 0.0F) {
                     local.options.attackKey.setPressed(true);
                     this.autoMineActive = true;
                     this.autoMineLastPos = var6Value;
                  } else {
                     local.options.attackKey.setPressed(false);
                     this.autoMineActive = false;
                  }
               } else {
                  this.autoMineLastPos = null;
                  this.autoMineActive = false;
               }
            }
         }
      } else if (this.autoMineActive) {
         this.autoMineActive = false;
         this.autoMineLastPos = null;
      }
   }
}
