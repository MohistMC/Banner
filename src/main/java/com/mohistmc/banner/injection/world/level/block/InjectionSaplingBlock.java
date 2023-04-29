package com.mohistmc.banner.injection.world.level.block;

import org.bukkit.TreeType;

public interface InjectionSaplingBlock {

    default TreeType bridge$getTreeType() {
        return null;
    }

    default void banner$setTreeType(TreeType treeType) {

    }
}
