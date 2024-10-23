package org.spigotmc;

import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.SpigotTimings;

public class ActivationRange
{

    public enum ActivationType
    {
        MONSTER,
        ANIMAL,
        RAIDER,
        MISC;

        AABB boundingBox = new AABB( 0, 0, 0, 0, 0, 0 );
    }

    static AABB maxBB = new AABB( 0, 0, 0, 0, 0, 0 );

    /**
     * Initializes an entities type on construction to specify what group this
     * entity is in for activation ranges.
     *
     * @param entity
     * @return group id
     */
    public static ActivationType initializeEntityActivationType(Entity entity)
    {
        if ( entity instanceof Raider )
        {
            return ActivationType.RAIDER;
        } else if ( entity instanceof Monster || entity instanceof Slime )
        {
            return ActivationType.MONSTER;
        } else if ( entity instanceof PathfinderMob || entity instanceof AmbientCreature )
        {
            return ActivationType.ANIMAL;
        } else
        {
            return ActivationType.MISC;
        }
    }

    /**
     * These entities are excluded from Activation range checks.
     *
     * @param entity
     * @param config
     * @return boolean If it should always tick.
     */
    public static boolean initializeEntityActivationState(Entity entity, SpigotWorldConfig config)
    {
        if ( ( entity.bridge$activationType() == ActivationType.MISC && config.miscActivationRange == 0 )
                || ( entity.bridge$activationType() == ActivationType.RAIDER && config.raiderActivationRange == 0 )
                || ( entity.bridge$activationType() == ActivationType.ANIMAL && config.animalActivationRange == 0 )
                || ( entity.bridge$activationType() == ActivationType.MONSTER && config.monsterActivationRange == 0 )
                || entity instanceof Player
                || entity instanceof ThrowableProjectile
                || entity instanceof EnderDragon
                || entity instanceof EnderDragonPart
                || entity instanceof WitherBoss
                || entity instanceof AbstractHurtingProjectile
                || entity instanceof LightningBolt
                || entity instanceof PrimedTnt
                || entity instanceof EndCrystal
                || entity instanceof FireworkRocketEntity
                || entity instanceof ThrownTrident )
        {
            return true;
        }

        return false;
    }

    /**
     * Find what entities are in range of the players in the world and set
     * active if in range.
     *
     * @param world
     */
    public static void activateEntities(Level world)
    {
        SpigotTimings.entityActivationCheckTimer.startTiming();
        final int miscActivationRange = world.bridge$spigotConfig().miscActivationRange;
        final int raiderActivationRange = world.bridge$spigotConfig().raiderActivationRange;
        final int animalActivationRange = world.bridge$spigotConfig().animalActivationRange;
        final int monsterActivationRange = world.bridge$spigotConfig().monsterActivationRange;

        int maxRange = Math.max( monsterActivationRange, animalActivationRange );
        maxRange = Math.max( maxRange, raiderActivationRange );
        maxRange = Math.max( maxRange, miscActivationRange );
        maxRange = Math.min( ( world.bridge$spigotConfig().simulationDistance << 4 ) - 8, maxRange );

        for ( Player player : world.players() )
        {
            player.banner$setActivatedTick(BukkitFieldHooks.currentTick());
            if ( world.bridge$spigotConfig().ignoreSpectatorActivation && player.isSpectator() )
            {
                continue;
            }

            ActivationRange.maxBB = player.getBoundingBox().inflate( maxRange, 256, maxRange );
            ActivationType.MISC.boundingBox = player.getBoundingBox().inflate( miscActivationRange, 256, miscActivationRange );
            ActivationType.RAIDER.boundingBox = player.getBoundingBox().inflate( raiderActivationRange, 256, raiderActivationRange );
            ActivationType.ANIMAL.boundingBox = player.getBoundingBox().inflate( animalActivationRange, 256, animalActivationRange );
            ActivationType.MONSTER.boundingBox = player.getBoundingBox().inflate( monsterActivationRange, 256, monsterActivationRange );

            world.getEntities().get(ActivationRange.maxBB, ActivationRange::activateEntity);
        }
        SpigotTimings.entityActivationCheckTimer.stopTiming();
    }

    /**
     * Checks for the activation state of all entities in this chunk.
     *
     * @param chunk
     */
    private static void activateEntity(Entity entity)
    {
        if ( BukkitFieldHooks.currentTick() > entity.bridge$activatedTick() )
        {
            if ( entity.bridge$defaultActivationState() )
            {
                entity.banner$setActivatedTick(BukkitFieldHooks.currentTick());
                return;
            }
            if ( entity.bridge$activationType().boundingBox.intersects( entity.getBoundingBox() ) )
            {
                entity.banner$setActivatedTick(BukkitFieldHooks.currentTick());
            }
        }
    }

    /**
     * If an entity is not in range, do some more checks to see if we should
     * give it a shot.
     *
     * @param entity
     * @return
     */
    public static boolean checkEntityImmunities(Entity entity)
    {
        // quick checks.
        if ( entity.wasTouchingWater || entity.getRemainingFireTicks() > 0 )
        {
            return true;
        }
        if ( !( entity instanceof AbstractArrow ) )
        {
            if ( !entity.onGround() || !entity.passengers.isEmpty() || entity.isPassenger() )
            {
                return true;
            }
        } else if ( !( (AbstractArrow) entity ).inGround )
        {
            return true;
        }
        // special cases.
        if ( entity instanceof LivingEntity )
        {
            LivingEntity living = (LivingEntity) entity;
            if ( /*TODO: Missed mapping? living.attackTicks > 0 || */ living.hurtTime > 0 || living.activeEffects.size() > 0 )
            {
                return true;
            }
            if ( entity instanceof PathfinderMob && ( (PathfinderMob) entity ).getTarget() != null )
            {
                return true;
            }
            if ( entity instanceof Villager && ( (Villager) entity ).canBreed() )
            {
                return true;
            }
            if ( entity instanceof Animal )
            {
                Animal animal = (Animal) entity;
                if ( animal.isBaby() || animal.isInLove() )
                {
                    return true;
                }
                if ( entity instanceof Sheep && ( (Sheep) entity ).isSheared() )
                {
                    return true;
                }
            }
            if (entity instanceof Creeper && ((Creeper) entity).isIgnited()) { // isExplosive
                return true;
            }
        }
        // SPIGOT-6644: Otherwise the target refresh tick will be missed
        if (entity instanceof ExperienceOrb) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the entity is active for this tick.
     *
     * @param entity
     * @return
     */
    public static boolean checkIfActive(Entity entity)
    {
        SpigotTimings.checkIfActiveTimer.startTiming();
        // Never safe to skip fireworks or entities not yet added to chunk
        if ( entity instanceof FireworkRocketEntity ) {
            SpigotTimings.checkIfActiveTimer.stopTiming();
            return true;
        }

        boolean isActive = entity.bridge$activatedTick() >= BukkitFieldHooks.currentTick() || entity.bridge$defaultActivationState();

        // Should this entity tick?
        if ( !isActive )
        {
            if ( ( BukkitFieldHooks.currentTick() - entity.bridge$activatedTick() - 1 ) % 20 == 0 )
            {
                // Check immunities every 20 ticks.
                if ( ActivationRange.checkEntityImmunities( entity ) )
                {
                    // Triggered some sort of immunity, give 20 full ticks before we check again.
                    entity.banner$setActivatedTick(BukkitFieldHooks.currentTick() + 20);
                }
                isActive = true;
            }
            // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if ( !entity.bridge$defaultActivationState() && entity.tickCount % 4 == 0 && !ActivationRange.checkEntityImmunities( entity ) )
        {
            isActive = false;
        }
        SpigotTimings.checkIfActiveTimer.stopTiming();
        return isActive;
    }
}
