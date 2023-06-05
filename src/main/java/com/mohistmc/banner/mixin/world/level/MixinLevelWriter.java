package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionLevelWriter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelWriter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelWriter.class)
public interface MixinLevelWriter extends InjectionLevelWriter {

    // CraftBukkit start
    @Override
    default boolean addFreshEntity(Entity entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }
    // CraftBukkit end
}
