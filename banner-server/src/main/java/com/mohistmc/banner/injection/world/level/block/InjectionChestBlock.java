package com.mohistmc.banner.injection.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface InjectionChestBlock {

    default MenuProvider getMenuProvider(BlockState iblockdata, Level world, BlockPos blockposition, boolean ignoreObstructions) {
        throw new IllegalStateException("Not implemented");
    }
}
