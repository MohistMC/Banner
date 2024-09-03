package com.mohistmc.banner.mixin.stats;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatsCounter.class)
public abstract class MixinStatsCounter {

    @Shadow public abstract int getValue(Stat<?> stat);

    @Inject(method = "increment", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatsCounter;setValue(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/stats/Stat;I)V"))
    public void banner$statsIncl(Player player, Stat<?> stat, int amount, CallbackInfo ci, @Local(ordinal = 1) int i) {
        Cancellable cancellable = CraftEventFactory.handleStatisticsIncrease(player, stat, this.getValue(stat), i);
        if (cancellable != null && cancellable.isCancelled()) {
            ci.cancel();
        }
    }
}
