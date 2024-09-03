package com.mohistmc.banner.mixin.world.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BreakDoorGoal.class)
public abstract class MixinBreakDoorGoal extends DoorInteractGoal {

    public MixinBreakDoorGoal(Mob mob) {
        super(mob);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void banner$breakDoorTick(CallbackInfo ci) {
        // CraftBukkit start
        if (CraftEventFactory.callEntityBreakDoorEvent(this.mob, this.doorPos).isCancelled()) {
            this.start();
            ci.cancel();
        }
        // CraftBukkit end
    }
}
