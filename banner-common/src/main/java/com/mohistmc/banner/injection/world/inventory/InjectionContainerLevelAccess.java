package com.mohistmc.banner.injection.world.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.bukkit.Location;

public interface InjectionContainerLevelAccess {

    default Level getWorld() {
        return null;
    }

    default BlockPos getPosition() {
        return null;
    }

    default Location getLocation() {
        return null;
    }
}
