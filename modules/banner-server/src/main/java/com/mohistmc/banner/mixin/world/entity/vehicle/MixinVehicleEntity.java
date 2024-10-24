package com.mohistmc.banner.mixin.world.entity.vehicle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VehicleEntity.class)
public abstract class MixinVehicleEntity extends Entity {

    public MixinVehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // @formatter:off
    @Shadow
    public abstract void setHurtDir(int rollingDirection);

    @Shadow
    public abstract int getHurtDir();

    @Shadow
    public abstract void setHurtTime(int rollingAmplitude);

    @Shadow
    public abstract void setDamage(float damage);

    @Shadow
    public abstract float getDamage();

    @Shadow
    protected abstract void destroy(ServerLevel serverLevel, DamageSource source);

    @Shadow
    abstract boolean shouldSourceDestroy(DamageSource p_309621_);
    // @formatter:on

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        }
        if (this.isInvulnerableToBase(source)) {
            return false;
        }
        Vehicle vehicle = (Vehicle) this.getBukkitEntity();
        org.bukkit.entity.Entity passenger = (source.getEntity() == null) ? null : source.getEntity().getBukkitEntity();
        VehicleDamageEvent event = new VehicleDamageEvent(vehicle, passenger, amount);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        amount = (float) event.getDamage();
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.markHurt();
        this.setDamage(this.getDamage() + amount * 10.0f);
        this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
        boolean flag = source.getEntity() instanceof Player && ((Player) source.getEntity()).getAbilities().instabuild;
        if ((flag || this.getDamage() <= 40.0f) && !this.shouldSourceDestroy(source)) {
            if (flag) {
                // CraftBukkit start
                VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
                Bukkit.getPluginManager().callEvent(destroyEvent);

                if (destroyEvent.isCancelled()) {
                    this.setDamage(40.0F); // Maximize damage so this doesn't get triggered again right away
                    return true;
                }
                // CraftBukkit end
                this.discard();
            }
        } else {
            VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
            Bukkit.getPluginManager().callEvent(destroyEvent);

            if (destroyEvent.isCancelled()) {
                this.setDamage(40.0F); // Maximize damage so this doesn't get triggered again right away
                return true;
            }
            this.destroy(serverLevel, source);
        }
        return true;
    }
}
