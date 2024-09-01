package com.mohistmc.banner.mixin.world.entity.animal;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.GameRules;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityBreedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.world.entity.animal.Fox$FoxBreedGoal")
public abstract class MixinFox_BreedGoal extends BreedGoal{

    public MixinFox_BreedGoal(Animal animal, double speedIn) {
        super(animal, speedIn);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void breed() {
        ServerLevel serverworld = (ServerLevel) this.level;
        Fox foxentity = (Fox) this.animal.getBreedOffspring(serverworld, this.partner);
        if (foxentity != null) {
            ServerPlayer serverplayerentity = this.animal.getLoveCause();
            ServerPlayer serverplayerentity1 = this.partner.getLoveCause();
            ServerPlayer serverplayerentity2 = serverplayerentity;
            if (serverplayerentity != null) {
                foxentity.addTrustedUUID(serverplayerentity.getUUID());
            } else {
                serverplayerentity2 = serverplayerentity1;
            }

            if (serverplayerentity1 != null && serverplayerentity != serverplayerentity1) {
                foxentity.addTrustedUUID(serverplayerentity1.getUUID());
            }
            int experience = this.animal.getRandom().nextInt(7) + 1;
            final EntityBreedEvent entityBreedEvent = CraftEventFactory.callEntityBreedEvent(foxentity, this.animal, this.partner, serverplayerentity,  this.animal.getBreedItem(), experience);
            if (entityBreedEvent.isCancelled()) {
                return;
            }
            experience = entityBreedEvent.getExperience();
            if (serverplayerentity2 != null) {
                serverplayerentity2.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(serverplayerentity2, this.animal, this.partner, foxentity);
            }

            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            foxentity.setAge(-24000);
            foxentity.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
            serverworld.addFreshEntityWithPassengers(foxentity);
            this.level.broadcastEntityEvent(this.animal, (byte) 18);
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                if (experience > 0) {
                    this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), experience));
                }
            }

        }
    }
}
