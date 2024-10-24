package com.mohistmc.banner.mixin.world.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EatBlockGoal.class)
public abstract class MixinEatBlockGoal extends Goal {

    @Shadow @Final private Mob mob;

    @Shadow @Final private Level level;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean banner$eatGrassTick(GameRules instance, GameRules.Key<GameRules.BooleanValue> key) {
        return CraftEventFactory.callEntityChangeBlockEvent(this.mob, this.mob.blockPosition(), Blocks.AIR.defaultBlockState(), !this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING));
    }
}
