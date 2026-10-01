package com.threesix.mixin;

import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.FakeStatsModule;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
   @Shadow
   private Text footer;

   @Inject(method = "render", at = @At("HEAD"))
   private void threesix$fakeFooter(DrawContext arg, int intVal, Scoreboard arg2, ScoreboardObjective arg3, CallbackInfo callbackInfo) {
      FakeStatsModule fakeStatsModuleValue = FakeStatsModule.call1();
      if (fakeStatsModuleValue != null && fakeStatsModuleValue.isEnabled() && this.footer != null) {
         Text local = fakeStatsModuleValue.call2(this.footer);
         if (local != null) {
            this.footer = local;
         }
      }
   }
}
