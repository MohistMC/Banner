package com.mohistmc.banner.injection.world.level.block;

public interface InjectionFireBlock {

    default boolean bridge$canBurn(net.minecraft.world.level.block.Block block) {
        throw new IllegalStateException("Not implemented");
    }
}
