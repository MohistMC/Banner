package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionContainerOpenersCounter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// TODO fix inject methods
@Mixin(ContainerOpenersCounter.class)
public abstract class MixinContainerOpenersCounter implements InjectionContainerOpenersCounter {

    @Shadow protected abstract void onOpen(Level level, BlockPos pos, BlockState state);

    @Shadow protected abstract void onClose(Level level, BlockPos pos, BlockState state);

    @Shadow protected abstract void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount);

    public boolean opened; // CraftBukkit

    @Override
    public boolean bridge$opened() {
        return opened;
    }

    @Override
    public void banner$setOpened(boolean opened) {
        this.opened = opened;
    }

    @Override
    public void onAPIOpen(Level world, BlockPos blockposition, BlockState iblockdata) {
        onOpen(world, blockposition, iblockdata);
    }

    @Override
    public void onAPIClose(Level world, BlockPos blockposition, BlockState iblockdata) {
        onClose(world, blockposition, iblockdata);
    }

    @Override
    public void openerAPICountChanged(Level world, BlockPos blockposition, BlockState iblockdata, int i, int j) {
        openerCountChanged(world, blockposition, iblockdata, i, j);
    }
}
