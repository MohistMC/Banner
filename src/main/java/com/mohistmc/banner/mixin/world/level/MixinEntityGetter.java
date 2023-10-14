package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionEntityGetter;
import com.mohistmc.banner.paper.PaperExtraConstants;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityGetter.class)
public interface MixinEntityGetter extends InjectionEntityGetter {

    @Shadow @Nullable Player getNearestPlayer(double d, double e, double f, double g, @Nullable Predicate<Entity> predicate);

    @Shadow List<? extends Player> players();

    @Override
    default @Nullable Player findNearbyPlayer(Entity entity, double maxDistance, @Nullable Predicate<Entity> predicate) {
        return this.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), maxDistance, predicate);
    }

    @Override
    default boolean hasNearbyAlivePlayerThatAffectsSpawning(double x, double y, double z, double range) {
        for (Player player : this.players()) {
            if (PaperExtraConstants.PLAYER_AFFECTS_SPAWNING.test(player)) { // combines NO_SPECTATORS and LIVING_ENTITY_STILL_ALIVE with an "affects spawning" check
                double distanceSqr = player.distanceToSqr(x, y, z);
                if (range < 0.0D || distanceSqr < range * range) {
                    return true;
                }
            }
        }
        return false;
    }
}
