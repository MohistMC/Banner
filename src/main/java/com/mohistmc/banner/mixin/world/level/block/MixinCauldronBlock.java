package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.bukkit.BukkitCauldronHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CauldronBlock.class)
public class MixinCauldronBlock {

    @Redirect(method = "receiveStalactiteDrip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean banner$drip(Level level, BlockPos pos, BlockState state, BlockState old) {
        return BukkitCauldronHooks.changeLevel(level, pos, state, null, CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL);
    }
}
