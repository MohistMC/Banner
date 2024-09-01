package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Fox.FoxEatBerriesGoal.class)
public abstract class MixinFox_EatBerriesGoal extends MoveToBlockGoal{

    @SuppressWarnings("target") @Shadow(aliases = {"field_17975"}, remap = false)
    private Fox outerThis;

    public MixinFox_EatBerriesGoal(PathfinderMob creature, double speedIn, int length) {
        super(creature, speedIn, length);
    }

    @Inject(method = "pickSweetBerries", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private void banner$eatBerry(BlockState state, CallbackInfo ci) {
        if (!CraftEventFactory.callEntityChangeBlockEvent(outerThis, this.blockPos, state.setValue(SweetBerryBushBlock.AGE, 1))) {
            ci.cancel();
        }
    }
}
