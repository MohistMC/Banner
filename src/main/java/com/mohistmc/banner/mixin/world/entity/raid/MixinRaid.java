package com.mohistmc.banner.mixin.world.entity.raid;

import com.mohistmc.banner.injection.world.entity.raid.InjectionRaid;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Mixin(Raid.class)
public class MixinRaid implements InjectionRaid {

    @Shadow private Raid.RaidStatus status;

    @Shadow @Final private Map<Integer, Set<Raider>> groupRaiderMap;

    @Override
    public boolean isInProgress() {
        return this.status == Raid.RaidStatus.ONGOING;
    }

    @Override
    public Collection<Raider> getRaiders() {
        return this.groupRaiderMap.values().stream().flatMap(Set::stream).collect(java.util.stream.Collectors.toSet());
    }
}
