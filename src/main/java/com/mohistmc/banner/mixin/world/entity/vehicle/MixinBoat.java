package com.mohistmc.banner.mixin.world.entity.vehicle;

import com.mohistmc.banner.injection.world.entity.vehicle.InjectionBoat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public abstract class MixinBoat extends Entity implements InjectionBoat {


    // @formatter:off
    @Shadow public abstract float getDamage();
    @Shadow public abstract void setDamage(float damageTaken);
    // @formatter:on

    public double maxSpeed = 0.4D;
    public double occupiedDeceleration = 0.2D;
    public double unoccupiedDeceleration = -1;
    public boolean landBoats = false;
    private Location lastLocation;

    public MixinBoat(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }


    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setHurtDir(I)V"))
    private void banner$damageVehicle(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Vehicle vehicle = (Vehicle) this.getBukkitEntity();
        org.bukkit.entity.Entity attacker = (source.getEntity() == null) ? null : source.getEntity().getBukkitEntity();

        VehicleDamageEvent event = new VehicleDamageEvent(vehicle, attacker, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/vehicle/Boat;getDamage()F"))
    private void banner$destroyVehicle(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.getDamage() > 40.0F) {
            Vehicle vehicle = (Vehicle) this.getBukkitEntity();
            org.bukkit.entity.Entity attacker = (source.getEntity() == null) ? null : source.getEntity().getBukkitEntity();

            VehicleDestroyEvent event = new VehicleDestroyEvent(vehicle, attacker);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                this.setDamage(40F);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "push", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$collideVehicle(Entity entityIn, CallbackInfo ci) {
        if (!isPassengerOfSameVehicle(entityIn)) {
            VehicleEntityCollisionEvent event = new VehicleEntityCollisionEvent((Vehicle) this.getBukkitEntity(), entityIn.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;tickBubbleColumn()V"))
    private void banner$updateVehicle(CallbackInfo ci) {
        org.bukkit.World bworld = this.level().getWorld();
        Location to = CraftLocation.toBukkit(this.position(), bworld, this.getYRot(), this.getXRot());
        Vehicle vehicle = (Vehicle) this.getBukkitEntity();
        Bukkit.getPluginManager().callEvent(new VehicleUpdateEvent(vehicle));
        if (this.lastLocation != null && !this.lastLocation.equals(to)) {
            final VehicleMoveEvent event = new VehicleMoveEvent(vehicle, this.lastLocation, to);
            Bukkit.getPluginManager().callEvent(event);
        }
        this.lastLocation = vehicle.getLocation();
    }

    @Redirect(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;isRemoved()Z"))
    private boolean banner$breakVehicle(Boat boatEntity) {
        if (!boatEntity.isRemoved()) {
            final Vehicle vehicle = (Vehicle) this.getBukkitEntity();
            final VehicleDestroyEvent event = new VehicleDestroyEvent(vehicle, null);
            Bukkit.getPluginManager().callEvent(event);
            return event.isCancelled();
        } else {
            return true;
        }
    }

    @Override
    public double bridge$maxSpeed() {
        return maxSpeed;
    }

    @Override
    public void banner$setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public double bridge$occupiedDeceleration() {
        return occupiedDeceleration;
    }

    @Override
    public void banner$setOccupiedDeceleration(double occupiedDeceleration) {
        this.occupiedDeceleration = occupiedDeceleration;
    }

    @Override
    public double bridge$unoccupiedDeceleration() {
        return unoccupiedDeceleration;
    }

    @Override
    public void banner$setUnoccupiedDeceleration(double unoccupiedDeceleration) {
        this.unoccupiedDeceleration = unoccupiedDeceleration;
    }

    @Override
    public boolean bridge$landBoats() {
        return landBoats;
    }

    @Override
    public void banner$setLandBoats(boolean landBoats) {
        this.landBoats = landBoats;
    }
}
