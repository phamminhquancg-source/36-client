package com.threesix.mixin;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.OutlineHandModule;

@Mixin(HeldItemRenderer.class)
public class HeldItemOutlineMixin {
   @Unique
   private LivingEntity threesix$outlineEntity;
   @Unique
   private ItemStack threesix$outlineStack;
   @Unique
   private ItemDisplayContext threesix$outlineMode;

   @Inject(
      method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V",
      at = @At("HEAD"),
      require = 0
   )
   private void threesix$captureHandItem(LivingEntity local, ItemStack local2, ItemDisplayContext local3, MatrixStack local4, OrderedRenderCommandQueue local5, int local6, CallbackInfo local7) {
      this.threesix$outlineEntity = local;
      this.threesix$outlineStack = local2;
      this.threesix$outlineMode = local3;
   }

   @ModifyArg(
      method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderState;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;III)V"),
      index = 4,
      require = 0
   )
   private int threesix$handOutlineColor(int local4) {
      try {
         LivingEntity local = this.threesix$outlineEntity;
         ItemStack local2 = this.threesix$outlineStack;
         ItemDisplayContext local3 = this.threesix$outlineMode;
         this.threesix$outlineEntity = null;
         this.threesix$outlineStack = null;
         this.threesix$outlineMode = null;
         if (local == null || local2 == null) {
            return local4;
         }

         try {
            if (local != MinecraftClient.getInstance().player) {
               return local4;
            }
         } catch (Throwable error) {
         }

         int outlineHandModuleValue = OutlineHandModule.getHandOutlineColor(local, local2, local3);
         if (outlineHandModuleValue != 0) {
            return outlineHandModuleValue;
         }
      } catch (Throwable error2) {
      }

      return local4;
   }
}
