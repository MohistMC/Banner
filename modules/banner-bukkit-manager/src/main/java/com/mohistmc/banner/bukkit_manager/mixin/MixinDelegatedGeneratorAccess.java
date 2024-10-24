package com.mohistmc.banner.bukkit_manager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import org.bukkit.craftbukkit.util.DelegatedGeneratorAccess;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DelegatedGeneratorAccess.class)
public abstract class MixinDelegatedGeneratorAccess implements WorldGenLevel {

    @Shadow public abstract WorldGenLevel getHandle();

    @Override
    public boolean addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (getHandle() != (Object) this) {
            return getHandle().addEntity(entity, reason);
        } else {
            this.pushAddEntityReason(reason);
            return getHandle().addFreshEntity(entity);
        }
    }

    @Override
    public void pushAddEntityReason(CreatureSpawnEvent.SpawnReason reason) {
        if (getHandle() != (Object) this) {
            getHandle().pushAddEntityReason(reason);
        }
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getAddEntityReason() {
        if (getHandle() != (Object) this) {
            return  getHandle().getAddEntityReason();
        }
        return null;
    }
}
