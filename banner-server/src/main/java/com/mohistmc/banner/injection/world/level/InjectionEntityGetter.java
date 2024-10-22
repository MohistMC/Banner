package com.mohistmc.banner.injection.world.level;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface InjectionEntityGetter {

    default @Nullable Player findNearbyPlayer(Entity entity, double maxDistance, @Nullable Predicate<Entity> predicate) {
        throw new IllegalStateException("Not implemented");
    }
}
