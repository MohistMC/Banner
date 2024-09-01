package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ComposterBlock;
import org.bukkit.craftbukkit.inventory.CraftBlockInventoryHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComposterBlock.EmptyContainer.class)
public class MixinComposterBlock_EmptyContainer extends SimpleContainer {

    @ShadowConstructor
    public void banner$constructor() {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(LevelAccessor world, BlockPos blockPos) {
        banner$constructor();
        this.setOwner(new CraftBlockInventoryHolder(world, blockPos, this));
    }
}
