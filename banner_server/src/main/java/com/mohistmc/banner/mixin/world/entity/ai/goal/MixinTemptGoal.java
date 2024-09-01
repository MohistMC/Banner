package com.mohistmc.banner.mixin.world.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public class MixinTemptGoal {

    @Shadow @Nullable protected Player player;

    @Shadow @Final protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("TAIL"), cancellable = true)
    private void banner$targetEvent(CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        if (this.player != null) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, this.player, EntityTargetEvent.TargetReason.TEMPT);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
            this.player = (event.getTarget() == null) ? null : ((CraftPlayer) event.getTarget()).getHandle();
        }
        // CraftBukkit end
    }
}
