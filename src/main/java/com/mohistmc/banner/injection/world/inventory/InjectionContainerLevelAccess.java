package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.bukkit.Location;

public interface InjectionContainerLevelAccess {

    default Level getWorld() {
        throw new IllegalStateException("Not implemented");
    }

    default BlockPos getPosition() {
        throw new IllegalStateException("Not implemented");
    }

    default Location getLocation() {
        throw new IllegalStateException("Not implemented");
    }
}
