package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.npc.Villager;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(VillagerMakeLove.class)
public class MixinVillagerMakeLove {

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private Optional<Villager> breed(ServerLevel level, Villager parent, Villager partner) {
        Villager villager = parent.getBreedOffspring(level, partner);
        if (villager == null) {
            return Optional.empty();
        } else {
            // CraftBukkit start - call EntityBreedEvent
            if (CraftEventFactory.callEntityBreedEvent(partner, villager, parent, null, null, 0).isCancelled()) {
                return Optional.empty();
            }
            villager.setAge(-24000);
            villager.moveTo(parent.getX(), parent.getY(), parent.getZ(), 0.0F, 0.0F);
            parent.setAge(6000);
            partner.setAge(6000);
            level.addFreshEntityWithPassengers(villager);
            level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.BREEDING);
            level.broadcastEntityEvent(villager, (byte)12);
            return Optional.of(villager);
        }
    }

}
