package com.threesix.mixin;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.world.biome.Biome.Precipitation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.threesix.module.NoRenderModule;

@Mixin(WeatherRendering.class)
public abstract class WeatherRenderingMixin {
   @Invoker("getPrecipitationAt")
   protected abstract Precipitation threesix$getPrecipitationAt(World arg1, BlockPos arg2);

   @Redirect(
      method = "buildPrecipitationPieces",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/WeatherRendering;getPrecipitationAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;"
      )
   )
   private Precipitation threesix$filterRenderedPrecipitation(WeatherRendering local, World local2, BlockPos local3) {
      return NoRenderModule.filterPrecipitation(this.threesix$getPrecipitationAt(local2, local3));
   }

   @Redirect(
      method = "addParticlesAndSound",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/WeatherRendering;getPrecipitationAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;"
      )
   )
   private Precipitation threesix$filterWeatherParticlesAndSounds(WeatherRendering local, World local2, BlockPos local3) {
      return NoRenderModule.filterPrecipitation(this.threesix$getPrecipitationAt(local2, local3));
   }
}
