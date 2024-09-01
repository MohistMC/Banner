package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SculkCatalystBlock.class)
public abstract class MixinSculkCatalystBlock extends Block {

    @Shadow @Final private IntProvider xpRange;

    public MixinSculkCatalystBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getExpDrop(BlockState blockState, ServerLevel world, BlockPos blockPos, ItemStack itemStack, boolean flag) {
        if (flag) {
            return this.banner$tryDropExperience(world, blockPos, itemStack, this.xpRange);
        }
        return 0;
    }
}
