package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.RootedDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RootedDirtBlock.class, priority = 1500)
public class MixinRootedDirtBlock {

    @Redirect(method = "performBonemeal", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean banner$blockSpread(ServerLevel instance, BlockPos pos, BlockState state) {
        return CraftEventFactory.handleBlockSpreadEvent(instance, pos.above(), pos, state);
    }
}
