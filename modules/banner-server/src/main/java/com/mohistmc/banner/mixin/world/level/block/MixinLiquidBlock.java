package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlock.class)
public class MixinLiquidBlock {

    private transient boolean banner$fizz = true;

    @Redirect(method = "shouldSpreadLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    public boolean banner$blockForm(Level world, BlockPos pos, BlockState state) {
        return banner$fizz = CraftEventFactory.handleBlockFormEvent(world, pos, state);
    }

    @Inject(method = "fizz", cancellable = true, at = @At("HEAD"))
    public void banner$fizz(LevelAccessor worldIn, BlockPos pos, CallbackInfo ci) {
        if (!banner$fizz) {
            ci.cancel();
        }
    }
}
