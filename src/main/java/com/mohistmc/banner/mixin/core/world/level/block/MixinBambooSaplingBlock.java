package com.mohistmc.banner.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(BambooSaplingBlock.class)
public class MixinBambooSaplingBlock {

    @Redirect(method = "growBamboo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean banner$blockSpread(Level instance, BlockPos pos, BlockState newState, int flags) {
        return CraftEventFactory.handleBlockSpreadEvent(instance, pos.below(), pos, newState, flags);
    }

    private AtomicReference<ServerLevel> banner$level = new AtomicReference<>();

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void banner$setLevel(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        banner$level.set(level);
    }

    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
    private int banner$corpRate(int constant) {
        return banner$level.get().bridge$spigotConfig().bambooModifier / 100;
    }
}
