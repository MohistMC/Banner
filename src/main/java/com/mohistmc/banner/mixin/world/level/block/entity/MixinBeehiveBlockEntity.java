package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionBeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeehiveBlockEntity.class)
public class MixinBeehiveBlockEntity implements InjectionBeehiveBlockEntity {

    public int maxBees = 3; // CraftBukkit - allow setting max amount of bees a hive can hold

    @Override
    public int bridge$maxBees() {
        return maxBees;
    }

    @Override
    public void banner$setMaxBees(int maxBees) {
        this.maxBees = maxBees;
    }
}
