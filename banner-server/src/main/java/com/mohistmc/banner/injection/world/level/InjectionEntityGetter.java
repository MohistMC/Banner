package com.mohistmc.banner.injection.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface InjectionEntityGetter {

    default @Nullable Player findNearbyPlayer(Entity entity, double maxDistance, @Nullable Predicate<Entity> predicate) {
        throw new IllegalStateException("Not implemented");
    }
}
