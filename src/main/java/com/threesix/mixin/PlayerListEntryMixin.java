package com.threesix.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.threesix.module.SkinChangerModule;
import com.threesix.module.FakeRolesModule;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {
   @Shadow
   public abstract GameProfile getProfile();

   @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
   private void threesix$overrideListEntrySkin(CallbackInfoReturnable callbackInfoReturnable) {
      GameProfile thisValue = this.getProfile();
      if (thisValue != null && thisValue.id() != null) {
         SkinTextures skinChangerModuleValue = SkinChangerModule.getOverrideSkin(thisValue.id());
         if (skinChangerModuleValue != null) {
            callbackInfoReturnable.setReturnValue(skinChangerModuleValue);
         }
      }
   }

   @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
   private void threesix$fakeRoleDisplayName(CallbackInfoReturnable callbackInfoReturnable) {
      if (FakeRolesModule.isFakeRolesActive()) {
         GameProfile thisValue = this.getProfile();
         MinecraftClient mc = MinecraftClient.getInstance();
         if (thisValue != null && thisValue.id() != null && mc != null && mc.player != null && thisValue.id().equals(mc.player.getUuid())) {
            Text fakeRolesModuleValue = FakeRolesModule.buildRoleText(thisValue.name());
            if (fakeRolesModuleValue != null) {
               callbackInfoReturnable.setReturnValue(fakeRolesModuleValue);
            }
         }
      }
   }
}
