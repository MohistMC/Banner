package com.mohistmc.banner.injection.world.level.block;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public interface InjectionAbstractTreeGrower {


    default void setTreeType(Holder<ConfiguredFeature<?, ?>> holder) {
    }
}
