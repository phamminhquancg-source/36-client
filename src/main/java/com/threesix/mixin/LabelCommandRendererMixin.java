package com.threesix.mixin;

import net.minecraft.client.render.command.LabelCommandRenderer;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.threesix.manager.MutedSoundRegistry;

@Mixin(LabelCommandRenderer.class)
public class LabelCommandRendererMixin {
   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)V",
         ordinal = 1
      ),
      require = 0
   )
   private void threesix$drawHealthLabelsWithOutline(
      TextRenderer local, Text local2, float local3, float local4, int local5, boolean local6, Matrix4f local7, VertexConsumerProvider local8, TextLayerType local9, int local10, int local11
   ) {
      if (!MutedSoundRegistry.shouldMuteText(local2)) {
         local.draw(local2, local3, local4, local5, local6, local7, local8, local9, local10, local11);
      } else {
         local.drawWithOutline(local2.asOrderedText(), local3, local4, local5, -16777216, local7, local8, local11);
      }
   }
}
