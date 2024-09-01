package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionServerLevelAccessor;
import java.util.Iterator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLevelAccessor.class)
public interface MixinServerLevelAccessor extends LevelAccessor, InjectionServerLevelAccessor {

    @Shadow ServerLevel getLevel();

    @Override
    default ServerLevel getMinecraftWorld() {
        return getLevel();
    }


    /**
     * @author wdog5
     * @reason functionally replaced
     * TODO change in other ways
     */
    @Overwrite
    default void addFreshEntityWithPassengers(Entity entity) {
        CreatureSpawnEvent.SpawnReason spawnReason = getAddEntityReason();
        Iterator<Entity> iterator = entity.getSelfAndPassengers().iterator();
        while (iterator.hasNext()) {
            Entity next = iterator.next();
            pushAddEntityReason(spawnReason);
            this.addFreshEntity(next);
        }
    }

    @Override
    default boolean addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        Iterator<Entity> iterator = entity.getSelfAndPassengers().iterator();
        while (iterator.hasNext()) {
            Entity next = iterator.next();
            pushAddEntityReason(reason);
            this.addFreshEntity(next);
        }
        return !entity.isRemoved();
    }

    @Override
    default boolean addAllEntities(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return this.addFreshEntityWithPassengers(entity, reason);
    }
}
