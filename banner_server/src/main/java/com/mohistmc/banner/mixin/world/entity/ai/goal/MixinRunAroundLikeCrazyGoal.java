package com.mohistmc.banner.mixin.world.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RunAroundLikeCrazyGoal.class)
public class MixinRunAroundLikeCrazyGoal {

    @Shadow @Final private AbstractHorse horse;

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;tameWithName(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean banner$tameCheck(AbstractHorse instance, Player player) {
        if (!CraftEventFactory.callEntityTameEvent(this.horse, ((CraftHumanEntity) this.horse.getBukkitEntity().getPassenger()).getHandle()).isCancelled()) {
            return this.horse.tameWithName(player);
        }
        return false;
    }
}
