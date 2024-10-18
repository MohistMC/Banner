package com.mohistmc.banner.mixin.world.entity;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.injection.world.entity.InjectionEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.BlockUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.event.CraftPortalEvent;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Banner - TODO fix patches
@Mixin(Entity.class)
public abstract class MixinEntity implements Nameable, EntityAccess, CommandSource, InjectionEntity {

    @Shadow
    private Level level;
    @Shadow @Final public static int TOTAL_AIR_SUPPLY;
    @Shadow private float yRot;

    @Shadow public abstract double getX();

    @Shadow public abstract double getZ();
    @Shadow protected abstract SoundEvent getSwimSound();

    @Shadow protected abstract SoundEvent getSwimSplashSound();

    @Shadow protected abstract SoundEvent getSwimHighSpeedSplashSound();

    @Shadow public abstract boolean isPushable();

    @Shadow public abstract Pose getPose();

    @Shadow public abstract String getScoreboardName();

    @Shadow private float xRot;
    @Shadow public int remainingFireTicks;
    @Shadow public boolean horizontalCollision;

    @Shadow public abstract double getY();

    @Shadow public abstract float getYRot();

    @Shadow public abstract float getXRot();

    @Shadow public int tickCount;

    @Shadow public abstract int getMaxAirSupply();

    @Shadow public abstract void setInvisible(boolean invisible);

    @Shadow @Nullable private Entity vehicle;
    @Shadow public ImmutableList<Entity> passengers;

    @Shadow @Nullable public abstract Entity getFirstPassenger();

    @Shadow @Final private static EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID;

    @Shadow public abstract SynchedEntityData getEntityData();

    @Shadow public abstract int getAirSupply();

    @Shadow @Final protected SynchedEntityData entityData;

    @Shadow public abstract boolean isSwimming();

    @Shadow public abstract boolean fireImmune();

    @Shadow public abstract boolean hurt(DamageSource source, float amount);

    @Shadow public abstract DamageSources damageSources();

    @Shadow protected abstract ListTag newDoubleList(double... ds);
    @Shadow public abstract boolean teleportTo(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yRot, float xRot);
    @Shadow public abstract Level level();

    @Shadow protected abstract Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle portal);

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow public abstract boolean isRemoved();

    @Shadow public abstract void unRide();

    @Shadow public abstract EntityType<?> getType();

    @Shadow protected abstract void removeAfterChangingDimensions();

    @Shadow public abstract void setDeltaMovement(Vec3 deltaMovement);

    @Shadow public abstract void moveTo(Vec3 vec);

    @Shadow public abstract void moveTo(double x, double y, double z, float yRot, float xRot);

    @Shadow public abstract void positionRider(Entity passenger);
    @Shadow public abstract boolean getSharedFlag(int p_20292_);
    @Shadow private AABB bb;

    @Shadow public abstract void gameEvent(Holder<GameEvent> holder, @Nullable Entity entity);

    @Shadow public abstract void remove(Entity.RemovalReason removalReason);

    @Shadow @Nullable private Entity.RemovalReason removalReason;

    @Shadow public abstract void igniteForSeconds(float f);

    @Shadow protected abstract void handlePortal();

    @Shadow public abstract void setRemainingFireTicks(int i);

    @Shadow public abstract void igniteForTicks(int i);

    @Shadow @Nullable public abstract Entity changeDimension(DimensionTransition dimensionTransition);

    @Shadow @Nullable public PortalProcessor portalProcess;
    private CraftEntity bukkitEntity;
    public final org.spigotmc.ActivationRange.ActivationType activationType =
            org.spigotmc.ActivationRange.initializeEntityActivationType((Entity) (Object) this);
    public boolean defaultActivationState;
    public long activatedTick = Integer.MIN_VALUE;
    public boolean inWorld = false;
    public boolean generation;
    public boolean persist = true;
    public boolean visibleByDefault = true;
    public boolean valid;
    public int maxAirTicks = getDefaultMaxAirSupply(); // CraftBukkit - SPIGOT-6907: re-implement LivingEntity#setMaximumAir()
    public org.bukkit.projectiles.ProjectileSource projectileSource; // For projectiles only
    public boolean lastDamageCancelled; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
    public boolean persistentInvisibility = false;
    public BlockPos lastLavaContact;
    private static final int CURRENT_LEVEL = 2;
    @javax.annotation.Nullable
    private org.bukkit.util.Vector origin;
    @javax.annotation.Nullable
    private UUID originWorld;
    private transient EntityRemoveEvent.Cause banner$removeCause;
    // Marks an entity, that it was removed by a plugin via Entity#remove
    // Main use case currently is for SPIGOT-7487, preventing dropping of leash when leash is removed
    public boolean pluginRemoved = false;

    @Override
    public void setOrigin(@NotNull Location location) {
        this.origin = location.toVector();
        this.originWorld = location.getWorld().getUID();
    }

    @Nullable
    @Override
    public Vector getOriginVector() {
        return this.origin != null ? this.origin.clone() : null;
    }

    @Nullable
    @Override
    public UUID getOriginWorld() {
        return this.originWorld;
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
            this.handlePortal();
        }
    }

    @Override
    public void igniteForSeconds(float i, boolean callEvent) {
        if (callEvent) {
            EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), i);
            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            i = event.getDuration();
        }
    }

    @Inject(method = "igniteForSeconds", at = @At("HEAD"))
    private void banner$setSecondsOnFire(float f, CallbackInfo ci) {
        igniteForSeconds(f, true);
    }

    @Override
    public void banner$setSecondsOnFire(float i, boolean callEvent) {
        if (callEvent) {
            EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), i);
            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            i = event.getDuration();
        }
        this.igniteForTicks(Mth.floor(i * 20.0F));
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

    @Inject(method = "getMaxAirSupply", cancellable = true, at = @At("RETURN"))
    private void banner$useBukkitMaxAir(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.maxAirTicks);
    }

    @Inject(method = "setPose", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V"))
    public void banner$setPose$EntityPoseChangeEvent(Pose pose, CallbackInfo ci) {
        if (pose == this.getPose()) {
            ci.cancel();
            return;
        }
        EntityPoseChangeEvent event = new EntityPoseChangeEvent(this.getBukkitEntity(), org.bukkit.entity.Pose.values()[pose.ordinal()]);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Inject(method = "setRot", at = @At(value = "HEAD"))
    public void banner$infCheck(float yaw, float pitch, CallbackInfo ci) {
        // CraftBukkit start - yaw was sometimes set to NaN, so we need to set it back to 0
        if (Float.isNaN(yaw)) {
            yaw = 0;
        }

        if (yaw == Float.POSITIVE_INFINITY || yaw == Float.NEGATIVE_INFINITY) {
            if (((Object) this) instanceof Player) {
                this.level.getCraftServer().getLogger().warning(this.getScoreboardName() + " was caught trying to crash the server with an invalid yaw");
                ((CraftPlayer) this.getBukkitEntity()).kickPlayer("Infinite yaw (Hacking?)");
            }
            yaw = 0;
        }

        // pitch was sometimes set to NaN, so we need to set it back to 0
        if (Float.isNaN(pitch)) {
            pitch = 0;
        }

        if (pitch == Float.POSITIVE_INFINITY || pitch == Float.NEGATIVE_INFINITY) {
            if (((Object) this) instanceof Player) {
                this.level.getCraftServer().getLogger().warning(this.getScoreboardName() + " was caught trying to crash the server with an invalid pitch");
                ((CraftPlayer) this.getBukkitEntity()).kickPlayer("Infinite pitch (Hacking?)");
            }
            pitch = 0;
        }
        // CraftBukkit end
    }

    @Decorate(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;handlePortal()V"))
    private void banner$baseTick$moveToPostTick(Entity entity) throws Throwable {
        if ((Object) this instanceof ServerPlayer) {
            DecorationOps.callsite().invoke(entity);// CraftBukkit - // Moved up to postTick
        }
    }

    @Decorate(method = "updateFluidHeightAndDoFluidPushing", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 banner$setLava(FluidState instance, BlockGetter level, BlockPos pos) throws Throwable {
        if (instance.getType().is(FluidTags.LAVA)) {
            lastLavaContact = pos.immutable();
        }
        return (Vec3) DecorationOps.callsite().invoke(instance, level, pos);
    }

    @Decorate(method = "baseTick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/Entity;isInLava()Z"))
    private boolean banner$resetLava(Entity instance) throws Throwable {
        var ret = (boolean) DecorationOps.callsite().invoke(instance);
        if (!ret) {
            this.lastLavaContact = null;
        }
        return ret;
    }

    @Decorate(method = "lavaHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    public void banner$setOnFireFromLava$bukkitEvent(Entity instance, float f) throws Throwable {
        if ((Object) this instanceof LivingEntity && remainingFireTicks <= 0) {
            var damager = (lastLavaContact == null) ? null : CraftBlock.at(level(), lastLavaContact);
            var damagee = this.getBukkitEntity();
            EntityCombustEvent combustEvent = new EntityCombustByBlockEvent(damager, damagee, 15);
            Bukkit.getPluginManager().callEvent(combustEvent);

            if (combustEvent.isCancelled()) {
                return;
            }
            f = combustEvent.getDuration();
        }
        DecorationOps.callsite().invoke(instance, f);
    }

    @Decorate(method = "lavaHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;lava()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource banner$resetBlockDamage(DamageSources instance) throws Throwable {
        var damager = (lastLavaContact == null) ? null : CraftBlock.at(level(), lastLavaContact);
        var damageSource = (DamageSource) DecorationOps.callsite().invoke(instance);
        return damageSource.directBlock(damager);
    }

    @ModifyArg(method = "move", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"))
    private BlockPos banner$captureBlockWalk(BlockPos pos) {
        BukkitSnapshotCaptures.captureDamageEventBlock(pos);
        return pos;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$resetBlockWalk(MoverType type, Vec3 pos, CallbackInfo ci) {
        BukkitSnapshotCaptures.captureDamageEventBlock(null);
    }

    @Inject(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;onGround()Z",
            ordinal = 1))
    private void banner$move$blockCollide(MoverType type, Vec3 pos, CallbackInfo ci, @Local(ordinal = 1) Vec3 vec3) {
        // CraftBukkit start
        if (horizontalCollision && getBukkitEntity() instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) this.getBukkitEntity();
            org.bukkit.block.Block cbBlock = this.level.getWorld().getBlockAt(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));

            if (pos.x > vec3.x) {
                cbBlock = cbBlock.getRelative(BlockFace.EAST);
            } else if (pos.x < vec3.x) {
                cbBlock = cbBlock.getRelative(BlockFace.WEST);
            } else if (pos.z > vec3.z) {
                cbBlock = cbBlock.getRelative(BlockFace.SOUTH);
            } else if (pos.z < vec3.z) {
                cbBlock = cbBlock.getRelative(BlockFace.NORTH);
            }

            if (!cbBlock.getType().isAir()) {
                VehicleBlockCollisionEvent event = new VehicleBlockCollisionEvent(vehicle, cbBlock);
                level.getCraftServer().getPluginManager().callEvent(event);
            }
        }
        // CraftBukkit end
    }

    @Inject(method = "saveAsPassenger", cancellable = true, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;getEncodeId()Ljava/lang/String;"))
    public void banner$writeUnlessRemoved$persistCheck(CompoundTag compound, CallbackInfoReturnable<Boolean> cir) {
        if (!this.persist)
            cir.setReturnValue(false);
    }


    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE_ASSIGN", ordinal = 1, target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"))
    public void banner$writeWithoutTypeId$InfiniteValueCheck(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        if (Float.isNaN(this.getYRot())) {
            this.yRot = 0;
        }

        if (Float.isNaN(this.getXRot())) {
            this.xRot = 0;
        }
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/nbt/CompoundTag;putUUID(Ljava/lang/String;Ljava/util/UUID;)V"))
    public void banner$writeWithoutTypeId$CraftBukkitNBT(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        compound.putLong("WorldUUIDLeast", this.level.getWorld().getUID().getLeastSignificantBits());
        compound.putLong("WorldUUIDMost", this.level.getWorld().getUID().getMostSignificantBits());
        compound.putInt("Bukkit.updateLevel", CURRENT_LEVEL);
        compound.putInt("Spigot.ticksLived", this.tickCount);
        if (!this.persist) {
            compound.putBoolean("Bukkit.persist", this.persist);
        }
        if (!this.visibleByDefault) {
            compound.putBoolean("Bukkit.visibleByDefault", this.visibleByDefault);
        }
        if (this.persistentInvisibility) {
            compound.putBoolean("Bukkit.invisible", this.persistentInvisibility);
        }
        if (maxAirTicks != getDefaultMaxAirSupply()) {
            compound.putInt("Bukkit.MaxAirSupply", getMaxAirSupply());
        }
    }

    @Inject(method = "saveWithoutId", at = @At(value = "RETURN"))
    public void banner$writeWithoutTypeId$StoreBukkitValues(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.bukkitEntity != null) {
            this.bukkitEntity.storeBukkitValues(compound);
        }
        // Paper start - Save the entity's origin location
        if (this.origin != null) {
            UUID originWorld = this.originWorld != null ? this.originWorld : this.level != null ? this.level.getWorld().getUID() : null;
            if (originWorld != null) {
                compound.putUUID("Paper.OriginWorld", originWorld);
            }
            compound.put("Paper.Origin", this.newDoubleList(origin.getX(), origin.getY(), origin.getZ()));
        }
        // Paper end
    }

    private static boolean isLevelAtLeast(CompoundTag tag, int level) {
        return tag.contains("Bukkit.updateLevel") && tag.getInt("Bukkit.updateLevel") >= level;
    }

    @Inject(method = "load", at = @At(value = "RETURN"))
    public void banner$read$ReadBukkitValues(CompoundTag compound, CallbackInfo ci) {
        // CraftBukkit start
        if ((Object) this instanceof LivingEntity entity) {
            this.tickCount = compound.getInt("Spigot.ticksLived");
        }
        this.persist = !compound.contains("Bukkit.persist") || compound.getBoolean("Bukkit.persist");
        this.visibleByDefault = !compound.contains("Bukkit.visibleByDefault") || compound.getBoolean("Bukkit.visibleByDefault");
        // CraftBukkit end

        // CraftBukkit start - Reset world
        if ((Object) this instanceof ServerPlayer) {
            Server server = Bukkit.getServer();
            org.bukkit.World bworld = null;

            String worldName = compound.getString("world");

            if (compound.contains("WorldUUIDMost") && compound.contains("WorldUUIDLeast")) {
                UUID uid = new UUID(compound.getLong("WorldUUIDMost"), compound.getLong("WorldUUIDLeast"));
                bworld = server.getWorld(uid);
            } else {
                bworld = server.getWorld(worldName);
            }

            if (bworld == null) {
                bworld = (((CraftServer) server).getServer().getLevel(Level.OVERWORLD)).getWorld();
            }

            ((ServerPlayer) (Object) this).setServerLevel(bworld == null ? null : ((CraftWorld) bworld).getHandle());
        }
        this.getBukkitEntity().readBukkitValues(compound);
        if (compound.contains("Bukkit.invisible")) {
            boolean bukkitInvisible = compound.getBoolean("Bukkit.invisible");
            this.setInvisible(bukkitInvisible);
            this.persistentInvisibility = bukkitInvisible;
        }
        if (compound.contains("Bukkit.MaxAirSupply")) {
            maxAirTicks = compound.getInt("Bukkit.MaxAirSupply");
        }
        // CraftBukkit end
        // Paper start - Restore the entity's origin location
        ListTag originTag = compound.getList("Paper.Origin", 6);
        if (!originTag.isEmpty()) {
            UUID originWorld = null;
            if (compound.contains("Paper.OriginWorld")) {
                originWorld = compound.getUUID("Paper.OriginWorld");
            } else if (this.level != null) {
                originWorld = this.level.getWorld().getUID();
            }
            this.originWorld = originWorld;
            origin = new org.bukkit.util.Vector(originTag.getDouble(0), originTag.getDouble(1), originTag.getDouble(2));
        }
        // Paper end
    }

    @Inject(method = "setInvisible", cancellable = true, at = @At("HEAD"))
    private void banner$preventVisible(boolean invisible, CallbackInfo ci) {
        if (this.persistentInvisibility) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public void banner$entityDropItem(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir, @Local ItemEntity itemEntity) {
        EntityDropItemEvent event = new EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) (itemEntity).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/entity/Entity;setPose(Lnet/minecraft/world/entity/Pose;)V"))
    public void banner$startRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        if (vehicle.getBukkitEntity() instanceof Vehicle && this.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity) {
            VehicleEnterEvent event = new VehicleEnterEvent((Vehicle) vehicle.getBukkitEntity(), this.getBukkitEntity());
            // Suppress during worldgen
            if (this.valid) {
                Bukkit.getPluginManager().callEvent(event);
            }
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
        // CraftBukkit end
        // Spigot start
        org.spigotmc.event.entity.EntityMountEvent event = new org.spigotmc.event.entity.EntityMountEvent(this.getBukkitEntity(), vehicle.getBukkitEntity());
        // Suppress during worldgen
        if (this.valid) {
            Bukkit.getPluginManager().callEvent(event);
        }
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
        // Spigot end
    }

    @Redirect(method = "removeVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removePassenger(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$stopRiding(Entity entity, Entity passenger) {
        if (!entity.banner$removePassenger(passenger)) {
            this.vehicle = entity;
        }
    }

    @Inject(method = "interact", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V"))
    private void banner$leashEvent(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (CraftEventFactory.callPlayerLeashEntityEvent((Entity) (Object) this, player, player, interactionHand).isCancelled()) {
            //player.resendItemInHands(); // SPIGOT-7615: Resend to fix client desync with used item // Banner TODO Fixme
            ((ServerPlayer) player).connection.send(new ClientboundSetEntityLinkPacket((Entity) (Object) this, ((Leashable) this).getLeashHolder()));
            cir.setReturnValue(InteractionResult.PASS);
        }
    }


    @Override
    public boolean banner$removePassenger(Entity entity) {
        if (entity.getVehicle() == (Object) this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        } else {
            // CraftBukkit start
            CraftEntity craft = (CraftEntity) (entity.getBukkitEntity().getVehicle());
            Entity orig = craft == null ? null : craft.getHandle();
            if (getBukkitEntity() instanceof Vehicle && (entity.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity)) {
                VehicleExitEvent event = new VehicleExitEvent(
                        (Vehicle) getBukkitEntity(),
                        (org.bukkit.entity.LivingEntity) (entity.getBukkitEntity()
                ));
                // Suppress during worldgen
                if (this.valid) {
                    Bukkit.getPluginManager().callEvent(event);
                }
                CraftEntity craftn = (CraftEntity) (entity.getBukkitEntity().getVehicle());
                Entity n = craftn == null ? null : craftn.getHandle();
                if (event.isCancelled() || n != orig) {
                    return false;
                }
            }
            // CraftBukkit end
            // Spigot start
            org.spigotmc.event.entity.EntityDismountEvent event = new org.spigotmc.event.entity.EntityDismountEvent((entity).getBukkitEntity(), this.getBukkitEntity());
            // Suppress during worldgen
            if (this.valid) {
                Bukkit.getPluginManager().callEvent(event);
            }
            if (event.isCancelled()) {
                return false;
            }
            // Spigot end
            if (this.passengers.size() == 1 && this.passengers.get(0) == entity) {
                this.passengers = ImmutableList.of();
            } else {
                this.passengers = this.passengers.stream().filter((entity1) -> entity1 != entity)
                        .collect(ImmutableList.toImmutableList());
            }

            entity.boardingCooldown = 60;
            this.gameEvent(GameEvent.ENTITY_DISMOUNT, entity);
        }
        return true; // CraftBukkit
    }

    @Inject(method = "setSwimming", cancellable = true, at = @At(value = "HEAD"))
    public void banner$setSwimming$EntityToggleSwimEvent(boolean flag, CallbackInfo ci) {
        // CraftBukkit start
        if (this.valid && this.isSwimming() != flag && (Object) this instanceof LivingEntity) {
            if (CraftEventFactory.callToggleSwimEvent((LivingEntity) (Object) this, flag).isCancelled()) {
                ci.cancel();
            }
        }
        // CraftBukkit end
    }

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    public void banner$onStruckByLightning$EntityCombustByEntityEvent0(Entity entity, float f) {
        final org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
        final org.bukkit.entity.Entity stormBukkitEntity = entity.getBukkitEntity();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        // CraftBukkit start - Call a combust event when lightning strikes
        EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(stormBukkitEntity, thisBukkitEntity, 8);
        pluginManager.callEvent(entityCombustEvent);
        if (!entityCombustEvent.isCancelled()) {
            this.igniteForSeconds(entityCombustEvent.getDuration());
        }
        // CraftBukkit end
    }

    @Inject(method = "setAirSupply", cancellable = true, at = @At(value = "HEAD"))
    public void banner$setAir$EntityAirChangeEvent(int air, CallbackInfo ci) {
        // CraftBukkit start
        EntityAirChangeEvent event = new EntityAirChangeEvent(this.getBukkitEntity(), air);
        // Suppress during worldgen
        if (this.valid) {
            event.getEntity().getServer().getPluginManager().callEvent(event);
        }
        if (event.isCancelled() && this.getAirSupply() != -1) {
            ci.cancel();
            this.getEntityData().markDirty(DATA_AIR_SUPPLY_ID);
            return;
        }
        this.entityData.set(DATA_AIR_SUPPLY_ID, event.getAmount());
        // CraftBukkit end
    }

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public boolean banner$onStruckByLightning$EntityCombustByEntityEvent1(Entity instance, DamageSource source, float amount) {
        final org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
        final org.bukkit.entity.Entity stormBukkitEntity = instance.getBukkitEntity();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        if (thisBukkitEntity instanceof Hanging) {
            HangingBreakByEntityEvent hangingEvent = new HangingBreakByEntityEvent((Hanging) thisBukkitEntity, stormBukkitEntity);
            pluginManager.callEvent(hangingEvent);

            if (hangingEvent.isCancelled()) {
                return false;
            }
        }

        if (this.fireImmune()) {
            return false;
        }
        if (!this.hurt(this.damageSources().lightningBolt(), amount)) {
            return false;
        }
        return true;
    }

    @Inject(method = "startSeenByPlayer", at = @At("HEAD"))
    private void banner$trackEvent(ServerPlayer serverPlayer, CallbackInfo ci) {
        // Paper start
        if (io.papermc.paper.event.player.PlayerTrackEntityEvent.getHandlerList().getRegisteredListeners().length > 0) {
            new io.papermc.paper.event.player.PlayerTrackEntityEvent(serverPlayer.getBukkitEntity(), this.getBukkitEntity()).callEvent();
        }
        // Paper end
    }

    @Inject(method = "startSeenByPlayer", at = @At("HEAD"))
    private void banner$untrackedEvent(ServerPlayer serverPlayer, CallbackInfo ci) {
        // Paper start
        if(io.papermc.paper.event.player.PlayerUntrackEntityEvent.getHandlerList().getRegisteredListeners().length > 0) {
            new io.papermc.paper.event.player.PlayerUntrackEntityEvent(serverPlayer.getBukkitEntity(), this.getBukkitEntity()).callEvent();
        }
        // Paper end
    }

    private AtomicReference<Vec3> banner$location = new AtomicReference<>();

    @Nullable
    @Override
    public Entity teleportTo(ServerLevel worldserver, Vec3 location) {
        banner$location.set(location);
        DimensionTransition dimensionTransition = this.portalProcess.getPortalDestination(worldserver, ((Entity) (Object) this));
        return changeDimension(dimensionTransition);
    }

    @Override
    public boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        return this.teleportTo(worldserver, d0, d1, d2, set, f, f1);
    }

    @Redirect(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addDuringTeleport(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$skipIfNotInWorld(ServerLevel instance, Entity entity) {
        if (this.inWorld) {
            instance.addDuringTeleport(entity);
        }
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void banner$forwardHandle(Entity entityIn, CallbackInfo ci) {
         entityIn.getBukkitEntity().setHandle((Entity) (Object) this);
         this.bukkitEntity =  entityIn.getBukkitEntity();
        if (entityIn instanceof Mob) {
            ((Mob) entityIn).dropLeash(true, false);
        }
    }

    @Inject(method = "setSharedFlag", at = @At("HEAD"),
            cancellable = true)
    private void banner$forwardHandle(int flag, boolean set, CallbackInfo ci) {
        if (BukkitSnapshotCaptures.banner$stopGlide()) {
            if (!(getSharedFlag(flag) && !CraftEventFactory.callToggleGlideEvent((LivingEntity) (Object)this, false).isCancelled())) {
                BukkitSnapshotCaptures.capturebanner$stopGlide(false);
                ci.cancel();
                return;
            }
        }
    }

    @Override
    public CraftPortalEvent callPortalEvent(Entity entity, ServerLevel exitWorldServer, Vec3 exitPosition, PlayerTeleportEvent.TeleportCause cause, int searchRadius, int creationRadius) {
        CraftEntity bukkitEntity =  entity.getBukkitEntity();
        Location enter = bukkitEntity.getLocation();
        Location exit = CraftLocation.toBukkit(exitPosition, exitWorldServer.getWorld());
        EntityPortalEvent event = new EntityPortalEvent(bukkitEntity, enter, exit, searchRadius);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getTo() == null || event.getTo().getWorld() == null || !entity.isAlive()) {
            return null;
        }
        return new CraftPortalEvent(event);
    }

    protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel serverWorld, BlockPos pos, boolean flag, WorldBorder worldborder, int searchRadius, boolean canCreatePortal, int createRadius) {
        return  serverWorld.getPortalForcer().findPortalAround(pos, worldborder, searchRadius);
    }

    @Redirect(method = "setBoundingBox",
            at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;bb:Lnet/minecraft/world/phys/AABB;"))
    private void banner$resetBBox(Entity instance, AABB axisalignedbb) {
        // CraftBukkit start - block invalid bounding boxes
        double minX = axisalignedbb.minX,
                minY = axisalignedbb.minY,
                minZ = axisalignedbb.minZ,
                maxX = axisalignedbb.maxX,
                maxY = axisalignedbb.maxY,
                maxZ = axisalignedbb.maxZ;
        double len = axisalignedbb.maxX - axisalignedbb.minX;
        if (len < 0) maxX = minX;
        if (len > 64) maxX = minX + 64.0;

        len = axisalignedbb.maxY - axisalignedbb.minY;
        if (len < 0) maxY = minY;
        if (len > 64) maxY = minY + 64.0;

        len = axisalignedbb.maxZ - axisalignedbb.minZ;
        if (len < 0) maxZ = minZ;
        if (len > 64) maxZ = minZ + 64.0;
        this.bb = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        // CraftBukkit end
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

    @Override
    public boolean bridge$generation() {
        return generation;
    }

    @Override
    public void banner$setGeneration(boolean gen) {
        this.generation = gen;
    }

    @Override
    public boolean bridge$persist() {
        return persist;
    }

    @Override
    public void banner$setPersist(boolean persist) {
        this.persist = persist;
    }

    @Override
    public boolean bridge$visibleByDefault() {
        return visibleByDefault;
    }

    @Override
    public void banner$setVisibleByDefault(boolean visibleByDefault) {
        this.visibleByDefault = visibleByDefault;
    }

    @Override
    public boolean bridge$valid() {
        return valid;
    }

    @Override
    public void banner$setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public int bridge$maxAirTicks() {
        return maxAirTicks;
    }

    @Override
    public void banner$setMaxAirTicks(int maxAirTicks) {
        this.maxAirTicks = maxAirTicks;
    }

    @Override
    public ProjectileSource bridge$projectileSource() {
        return projectileSource;
    }

    @Override
    public void banner$setProjectileSource(ProjectileSource projectileSource) {
        this.projectileSource = projectileSource;
    }

    @Override
    public boolean bridge$lastDamageCancelled() {
        return lastDamageCancelled;
    }

    @Override
    public void banner$setLastDamageCancelled(boolean lastDamageCancelled) {
        this.lastDamageCancelled = lastDamageCancelled;
    }

    @Override
    public boolean bridge$persistentInvisibility() {
        return persistentInvisibility;
    }

    @Override
    public void banner$setPersistentInvisibility(boolean persistentInvisibility) {
        this.persistentInvisibility = persistentInvisibility;
    }

    @Override
    public BlockPos bridge$lastLavaContact() {
        return lastLavaContact;
    }

    @Override
    public void banner$setLastLavaContact(BlockPos lastLavaContact) {
        this.lastLavaContact = lastLavaContact;
    }

    @Override
    public CommandSender banner$getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitEntity();
    }

    @Override
    public void banner$setBukkitEntity(CraftEntity bukkitEntity) {
        this.bukkitEntity = bukkitEntity;
    }

    @Override
    public boolean bridge$inWorld() {
        return inWorld;
    }

    @Override
    public void banner$setInWorld(boolean inWorld) {
        this.inWorld = inWorld;
    }

    @Override
    public void refreshEntityData(ServerPlayer to) {
        List<SynchedEntityData.DataValue<?>> list = this.getEntityData().getNonDefaultValues();

        if (list != null) {
            to.connection.send(new ClientboundSetEntityDataPacket(this.getId(), list));
        }
    }

    @Override
    public void discard(EntityRemoveEvent.Cause cause) {
        this.remove(Entity.RemovalReason.DISCARDED, cause);
    }

    @Override
    public void remove(Entity.RemovalReason entity_removalreason, EntityRemoveEvent.Cause cause) {
        this.setRemoved(entity_removalreason, cause);
    }

    @Override
    public void setRemoved(Entity.RemovalReason entity_removalreason, EntityRemoveEvent.Cause cause) {
        CraftEventFactory.callEntityRemoveEvent(((Entity) (Object) this), cause);
        this.setRemoved(removalReason);
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void banner$setRemoved(Entity.RemovalReason removalReason, CallbackInfo ci) {
        CraftEventFactory.callEntityRemoveEvent(((Entity) (Object) this), banner$removeCause != null ? banner$removeCause : null);
    }

    @Override
    public void pushRemoveCause(EntityRemoveEvent.Cause cause) {
        this.banner$removeCause = cause;
    }

    @Inject(method = "kill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void banner$killReason(CallbackInfo ci) {
        pushRemoveCause(EntityRemoveEvent.Cause.DEATH);
    }

    @Inject(method = "onBelowWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;discard()V"))
    private void banner$pushOutOfWorldReason(CallbackInfo ci) {
        pushRemoveCause(EntityRemoveEvent.Cause.OUT_OF_WORLD);
    }
}
