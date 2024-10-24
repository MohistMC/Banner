package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScaffoldingBlock.class)
public class MixinScaffoldingBlock {

    @Redirect(method = "tick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    public Comparable<Integer> banner$blockFade(BlockState state, Property<Integer> property, BlockState blockState, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        Integer integer = state.getValue(property);
        if (integer == 7) {
            if (CraftEventFactory.callBlockFadeEvent(worldIn, pos, Blocks.AIR.defaultBlockState()).isCancelled()) {
                return 6;
            } else {
                return integer;
            }
        } else {
            return integer;
        }
    }
}
