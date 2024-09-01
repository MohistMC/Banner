package com.mohistmc.banner.mixin.world.entity.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Egg;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ThrownEgg.class)
public abstract class MixinThrownEgg extends ThrowableItemProjectile {

    public MixinThrownEgg(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void onHit(final HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            boolean hatching = this.random.nextInt(8) == 0;
            byte b0 = 1;
            if (this.random.nextInt(32) == 0) {
                b0 = 4;
            }
            org.bukkit.entity.EntityType hatchingType = org.bukkit.entity.EntityType.CHICKEN;
            Entity shooter = this.getOwner();
            if (!hatching) {
                b0 = 0;
            }

            if (shooter instanceof ServerPlayer) {
                PlayerEggThrowEvent event = new PlayerEggThrowEvent(((ServerPlayer) shooter).getBukkitEntity(), (Egg) this.getBukkitEntity(), hatching, b0, hatchingType);
                Bukkit.getPluginManager().callEvent(event);
                b0 = event.getNumHatches();
                hatching = event.isHatching();
                hatchingType = event.getHatchingType();
                // If hatching is set to false, ensure child count is 0
                if (!hatching) {
                    b0 = 0;
                }
            }
            if (hatching) {
                for (int i = 0; i < b0; ++i) {
                    Chicken entity = (Chicken) this.level().getWorld().makeEntity(new Location(this.level().getWorld(), this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f), hatchingType.getEntityClass());
                    if (entity != null) {
                        if (entity.getBukkitEntity() instanceof Ageable) {
                            ((Ageable) entity.getBukkitEntity()).setBaby();
                        }
                        this.level().pushAddEntityReason(CreatureSpawnEvent.SpawnReason.EGG);
                        this.level().addFreshEntity(entity);
                    }
                }
            }
            this.level().broadcastEntityEvent((ThrownEgg) (Object) this, (byte) 3);
            this.discard();
        }
    }
}
