package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChangeOverTimeBlock.class)
public interface MixinChangeOverTimeBlock<T extends Enum<T>> {

    @Redirect(method = "method_54763", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean banner$callBlockFormEvent(ServerLevel instance, BlockPos blockPos, BlockState blockState) {
        return CraftEventFactory.handleBlockFormEvent(instance, blockPos, blockState);
    }
}
