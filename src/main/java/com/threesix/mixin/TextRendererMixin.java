package com.threesix.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.threesix.util.TextNameSpoofUtil;
import com.threesix.util.ServerAddressRedirector;

@Mixin(TextRenderer.class)
public class TextRendererMixin {
   @ModifyVariable(method = "prepare(Ljava/lang/String;FFIZI)Lnet/minecraft/client/font/TextRenderer$GlyphDrawable;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private String threesix$replacePreparedString(String string) {
      return TextNameSpoofUtil.replaceAddress2(ServerAddressRedirector.replaceAddress(string));
   }

   @ModifyVariable(
      method = "prepare(Lnet/minecraft/text/OrderedText;FFIZZI)Lnet/minecraft/client/font/TextRenderer$GlyphDrawable;",
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private OrderedText threesix$replacePreparedOrderedText(OrderedText local) {
      return TextNameSpoofUtil.rewriteOrderedText2(ServerAddressRedirector.rewriteOrderedText(local));
   }

   @ModifyVariable(
      method = "drawWithOutline(Lnet/minecraft/text/OrderedText;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private OrderedText threesix$replaceOutlinedOrderedText(OrderedText local) {
      return TextNameSpoofUtil.rewriteOrderedText2(ServerAddressRedirector.rewriteOrderedText(local));
   }

   @ModifyVariable(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private String threesix$replaceWidthString(String string) {
      return TextNameSpoofUtil.replaceAddress2(ServerAddressRedirector.replaceAddress(string));
   }

   @ModifyVariable(method = "getWidth(Lnet/minecraft/text/StringVisitable;)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private StringVisitable threesix$replaceWidthVisitable(StringVisitable arg) {
      return TextNameSpoofUtil.rewriteText2(ServerAddressRedirector.rewriteText(arg));
   }

   @ModifyVariable(method = "getWidth(Lnet/minecraft/text/OrderedText;)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private OrderedText threesix$replaceWidthOrderedText(OrderedText arg) {
      return TextNameSpoofUtil.rewriteOrderedText2(ServerAddressRedirector.rewriteOrderedText(arg));
   }
}
