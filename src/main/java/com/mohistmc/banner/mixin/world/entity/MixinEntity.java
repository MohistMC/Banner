package com.mohistmc.banner.mixin.world.entity;

import com.mohistmc.banner.injection.world.entity.InjectionEntity;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.PositionImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.event.entity.EntityCombustEvent;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements Nameable, EntityAccess, CommandSource, InjectionEntity {

    @Shadow public Level level;
    @Shadow @Final public static int TOTAL_AIR_SUPPLY;
    @Shadow private float yRot;

    @Shadow public abstract double getX();

    @Shadow public abstract double getZ();

    @Shadow protected abstract void handleNetherPortal();

    @Shadow public abstract void setSecondsOnFire(int seconds);

    @Shadow protected abstract SoundEvent getSwimSound();

    @Shadow protected abstract SoundEvent getSwimSplashSound();

    @Shadow protected abstract SoundEvent getSwimHighSpeedSplashSound();

    @Shadow public abstract boolean isPushable();

    private CraftEntity bukkitEntity;
    public final org.spigotmc.ActivationRange.ActivationType activationType =
            org.spigotmc.ActivationRange.initializeEntityActivationType((Entity) (Object) this);
    public boolean defaultActivationState;
    public long activatedTick = Integer.MIN_VALUE;

    @Override
    public void inactiveTick() {

    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = CraftEntity.getEntity(level.getCraftServer(), ((Entity) (Object) this));
        }
        return bukkitEntity;
    }

    @Override
    public int getDefaultMaxAirSupply() {
        return TOTAL_AIR_SUPPLY;
    }

    @Override
    public float getBukkitYaw() {
        return this.yRot;
    }

    @Override
    public boolean isChunkLoaded() {
        return level.hasChunk((int) Math.floor(this.getX()) >> 4, (int) Math.floor(this.getZ()) >> 4);
    }

    @Override
    public void postTick() {
        // No clean way to break out of ticking once the entity has been copied to a new world, so instead we move the portalling later in the tick cycle
        if (!(((Entity) (Object) this) instanceof ServerPlayer)) {
            this.handleNetherPortal();
        }
    }

    @Override
    public void setSecondsOnFire(int i, boolean callEvent) {
        if (callEvent) {
            EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), i);
            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            i = event.getDuration();
        }
    }

    @Inject(method = "setSecondsOnFire", at = @At("HEAD"))
    private void banner$setSecondsOnFire(int seconds, CallbackInfo ci) {
        setSecondsOnFire(seconds);
    }

    @Override
    public SoundEvent getSwimSound0() {
        return getSwimSound();
    }

    @Override
    public SoundEvent getSwimSplashSound0() {
        return getSwimSplashSound();
    }

    @Override
    public SoundEvent getSwimHighSpeedSplashSound0() {
        return getSwimHighSpeedSplashSound();
    }

    @Override
    public boolean canCollideWithBukkit(Entity entity) {
        return isPushable();
    }

    @Override
    public ActivationRange.ActivationType bridge$activationType() {
        return activationType;
    }

    @Override
    public long bridge$activatedTick() {
        return activatedTick;
    }

    @Override
    public void banner$setActivatedTick(long activatedTick) {
        this.activatedTick = activatedTick;
    }

    @Override
    public boolean bridge$defaultActivationState() {
        return defaultActivationState;
    }

    @Override
    public void banner$setDefaultActivationState(boolean state) {
        defaultActivationState = state;
    }
}
