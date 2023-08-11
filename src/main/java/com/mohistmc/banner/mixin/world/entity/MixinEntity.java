package com.mohistmc.banner.mixin.world.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.injection.world.entity.InjectionEntity;
import net.minecraft.BlockUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.PositionImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftPortalEvent;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

// Banner - TODO fix patches
@Mixin(Entity.class)
public abstract class MixinEntity implements Nameable, EntityAccess, CommandSource, InjectionEntity {

    @Shadow
    private Level level;
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

    @Shadow public abstract Pose getPose();

    @Shadow public abstract String getScoreboardName();

    @Shadow private float xRot;
    @Shadow public int remainingFireTicks;
    @Shadow public boolean horizontalCollision;

    @Shadow protected abstract Vec3 collide(Vec3 vec);

    @Shadow public abstract double getY();

    @Shadow public abstract float getYRot();

    @Shadow public abstract float getXRot();

    @Shadow public int tickCount;

    @Shadow public abstract int getMaxAirSupply();

    @Shadow public abstract void setInvisible(boolean invisible);

    @Shadow @Nullable private Entity vehicle;

    @Shadow public abstract void gameEvent(net.minecraft.world.level.gameevent.GameEvent event, @Nullable Entity entity);

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

    @Shadow protected BlockPos portalEntrancePos;

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

    @Shadow @Nullable public abstract Entity changeDimension(ServerLevel destination);

    @Shadow public abstract boolean getSharedFlag(int p_20292_);

    @Shadow public abstract void setRemainingFireTicks(int remainingFireTicks);

    private CraftEntity bukkitEntity;
    public final org.spigotmc.ActivationRange.ActivationType activationType =
            org.spigotmc.ActivationRange.initializeEntityActivationType((Entity) (Object) this);
    public boolean defaultActivationState;
    public long activatedTick = Integer.MIN_VALUE;
    public boolean generation;
    public boolean persist = true;
    public boolean visibleByDefault = true;
    public boolean valid;
    public int maxAirTicks = getDefaultMaxAirSupply(); // CraftBukkit - SPIGOT-6907: re-implement LivingEntity#setMaximumAir()
    public org.bukkit.projectiles.ProjectileSource projectileSource; // For projectiles only
    public boolean lastDamageCancelled; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
    public boolean persistentInvisibility = false;
    public BlockPos lastLavaContact;
    private static transient BlockPos banner$damageEventBlock;
    private static final int CURRENT_LEVEL = 2;
    @javax.annotation.Nullable
    private org.bukkit.util.Vector origin;
    @javax.annotation.Nullable
    private UUID originWorld;

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
        setSecondsOnFire(seconds, true);
    }

    @Override
    public void banner$setSecondsOnFire(int i, boolean callEvent) {
        if (callEvent) {
            EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), i);
            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
            i = event.getDuration();
        }
        int secs = i * 20;
        if (((Entity) (Object) this) instanceof LivingEntity) {
            secs = ProtectionEnchantment.getFireAfterDampener((LivingEntity) (Object) this, secs);
        }
        if (this.remainingFireTicks < secs) {
            this.setRemainingFireTicks(secs);
        }
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

    @Inject(method = "setRot", cancellable = true, at = @At(value = "HEAD"))
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

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;handleNetherPortal()V"))
    public void banner$baseTick$moveToPostTick(Entity entity) {
        if ((Object) this instanceof ServerPlayer) this.handleNetherPortal();// CraftBukkit - // Moved up to postTick
    }

    @Redirect(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 banner$setLava(FluidState instance, BlockGetter level, BlockPos pos) {
        if (instance.getType().is(FluidTags.LAVA)) {
            lastLavaContact = pos.immutable();
        }
        return instance.getFlow(level, pos);
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/Entity;isInLava()Z"))
    private boolean banner$resetLava(Entity instance) {
        var ret = instance.isInLava();
        if (!ret) {
            this.lastLavaContact = null;
        }
        return ret;
    }

    @Redirect(method = "lavaHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    public void banner$setOnFireFromLava$bukkitEvent(Entity entity, int seconds) {
        var damager = (lastLavaContact == null) ? null : CraftBlock.at(level, lastLavaContact);
        CraftEventFactory.blockDamage = damager;
        if ((Object) this instanceof LivingEntity && remainingFireTicks <= 0) {
            var damagee = this.getBukkitEntity();
            EntityCombustEvent combustEvent = new EntityCombustByBlockEvent(damager, damagee, 15);
            Bukkit.getPluginManager().callEvent(combustEvent);

            if (!combustEvent.isCancelled()) {
                this.setSecondsOnFire(combustEvent.getDuration());
            }
        } else {
            // This will be called every single tick the entity is in lava, so don't throw an event
            this.setSecondsOnFire(15);
        }
    }

    @Inject(method = "lavaHurt", at = @At("RETURN"))
    private void banner$resetBlockDamage(CallbackInfo ci) {
        CraftEventFactory.blockDamage = null;
    }

    @ModifyArg(method = "move", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"))
    private BlockPos banner$captureBlockWalk(BlockPos pos) {
        banner$damageEventBlock = pos;
        return pos;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$resetBlockWalk(MoverType type, Vec3 pos, CallbackInfo ci) {
        banner$damageEventBlock = null;
    }

    @Inject(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;onGround()Z",
            ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$move$blockCollide(MoverType type, Vec3 pos, CallbackInfo ci, Vec3 vec3,
                                          double d, boolean bl, boolean bl2, BlockPos blockPos,
                                          BlockState blockState, Block block) {
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
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public void banner$entityDropItem(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir, ItemEntity itemEntity) {
        EntityDropItemEvent event = new EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) (itemEntity).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(null);
        }
    }

    @Redirect(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addPassenger(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$startRiding(Entity entity, Entity pPassenger) {
        if (!(entity).banner$addPassenger(pPassenger)) {
            this.vehicle = null;
        }
    }

    @Redirect(method = "removeVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removePassenger(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$stopRiding(Entity entity, Entity passenger) {
        if (!(entity).banner$removePassenger(passenger)) {
            this.vehicle = entity;
        }
    }

    @Override
    public boolean banner$addPassenger(Entity entity) {
        if (entity.getVehicle() != (Object) this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        } else {
            // CraftBukkit start
            com.google.common.base.Preconditions.checkState(!(entity).getPassengers().contains(((Entity) (Object) this)), "Circular entity riding! %s %s", this, entity);

            CraftEntity craft = (CraftEntity) (entity.getBukkitEntity().getVehicle());
            Entity orig = craft == null ? null : craft.getHandle();
            if (getBukkitEntity() instanceof Vehicle && (entity).getBukkitEntity() instanceof org.bukkit.entity.LivingEntity) {
                VehicleEnterEvent event = new VehicleEnterEvent(
                        (Vehicle) getBukkitEntity(),
                        (entity.getBukkitEntity()
                ));
                // Suppress during worldgen
                if (this.valid) {
                    Bukkit.getPluginManager().callEvent(event);
                }
                CraftEntity craftn = (CraftEntity) entity.getBukkitEntity().getVehicle();
                Entity n = craftn == null ? null : craftn.getHandle();
                if (event.isCancelled() || n != orig) {
                    return false;
                }
            }
            // CraftBukkit end
            // Spigot start
            org.spigotmc.event.entity.EntityMountEvent event = new org.spigotmc.event.entity.EntityMountEvent((entity).getBukkitEntity(), this.getBukkitEntity());
            // Suppress during worldgen
            if (this.valid) {
                Bukkit.getPluginManager().callEvent(event);
            }
            if (event.isCancelled()) {
                return false;
            }
            // Spigot end
            if (this.passengers.isEmpty()) {
                this.passengers = ImmutableList.of(entity);
            } else {
                List<Entity> list = Lists.newArrayList(this.passengers);

                if (!this.level.isClientSide && entity instanceof Player && !(this.getFirstPassenger() instanceof Player)) {
                    list.add(0, entity);
                } else {
                    list.add(entity);
                }

                this.passengers = ImmutableList.copyOf(list);
            }

            this.gameEvent(GameEvent.ENTITY_MOUNT, entity);
        }
        return true; // CraftBukkit
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

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    public void banner$onStruckByLightning$EntityCombustByEntityEvent0(Entity entity, int seconds) {
        final org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
        final org.bukkit.entity.Entity stormBukkitEntity = entity.getBukkitEntity();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        // CraftBukkit start - Call a combust event when lightning strikes
        EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(stormBukkitEntity, thisBukkitEntity, 8);
        pluginManager.callEvent(entityCombustEvent);
        if (!entityCombustEvent.isCancelled()) {
            this.setSecondsOnFire(entityCombustEvent.getDuration());
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
        CraftEventFactory.entityDamage = instance;
        if (!this.hurt(this.damageSources().lightningBolt(), amount)) {
            CraftEventFactory.entityDamage = null;
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

    private AtomicReference<PositionImpl> banner$location = new AtomicReference<>();

    @Nullable
    @Override
    public Entity teleportTo(ServerLevel worldserver, PositionImpl location) {
        banner$location.set(location);
        return changeDimension(worldserver);
    }

    @Override
    public boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        return this.teleportTo(worldserver, d0, d1, d2, set, f, f1);
    }

    /*
    @Overwrite
    @Nullable
    public Entity changeDimension(ServerLevel destination) {
        if (this.level() instanceof ServerLevel && !this.isRemoved()) {
            this.level().getProfiler().push("changeDimension");
            if (destination == null) {
                return null;
            }
            this.level().getProfiler().push("reposition");
            var bukkitPos = banner$location.getAndSet(null);
            PortalInfo portalInfo = bukkitPos == null ? this.findDimensionEntryPoint(destination) : new PortalInfo(new Vec3(bukkitPos.x(), bukkitPos.y(), bukkitPos.z()), Vec3.ZERO, this.yRot, this.xRot);
            portalInfo.banner$setWorld(destination);
            portalInfo.banner$setPortalEventInfo(null);
            if (portalInfo == null) {
                return null;
            } else {
                destination = portalInfo.bridge$getWorld();
                if (destination == this.level()) {
                    this.moveTo(portalInfo.pos.x, portalInfo.pos.y, portalInfo.pos.z, portalInfo.yRot, portalInfo.xRot);
                    this.setDeltaMovement(portalInfo.speed);
                    return (Entity) (Object) this;
                }
                this.unRide();

                this.level().getProfiler().popPush("reloading");
                Entity entity = this.getType().create(destination);

                if (entity != null) {
                    entity.restoreFrom(((Entity) (Object) this));
                    entity.moveTo(portalInfo.pos.x, portalInfo.pos.y, portalInfo.pos.z, portalInfo.yRot, entity.getXRot());
                    entity.setDeltaMovement(portalInfo.speed);
                    destination.addDuringTeleport(entity);
                    if (destination.dimension() == Level.END) { // CraftBukkit
                        BukkitCaptures.captureEndPortalEntity((Entity) (Object) this, true);
                        ServerLevel.makeObsidianPlatform(destination); // CraftBukkit
                    }
                    // CraftBukkit start - Forward the CraftEntity to the new entity
                    this.getBukkitEntity().setHandle(entity);
                    entity.banner$setBukkitEntity(this.getBukkitEntity());

                    if (((Entity) (Object) this) instanceof Mob) {
                        ((Mob) (Object) this).dropLeash(true, false); // Unleash to prevent duping of leads.
                    }
                    // CraftBukkit end
                }

                this.removeAfterChangingDimensions();
                this.level().getProfiler().pop();
                ((ServerLevel)this.level()).resetEmptyTime();
                destination.resetEmptyTime();
                this.level().getProfiler().pop();
                return entity;
            }
        } else {
            return null;
        }
    }*/

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
        if (BukkitCaptures.banner$stopGlide()) {
            if (!(getSharedFlag(flag) && !CraftEventFactory.callToggleGlideEvent((LivingEntity) (Object)this, false).isCancelled())) {
                BukkitCaptures.capturebanner$stopGlide(false);
                ci.cancel();
                return;
            }
        }
    }

    /*
    @Nullable
    @Overwrite
    protected PortalInfo findDimensionEntryPoint(ServerLevel worldserver) {
        // CraftBukkit start
        if (worldserver == null) {
            return null;
        }
        boolean flag = this.level().dimension() == Level.END && worldserver.dimension() == Level.OVERWORLD; // fromEndToOverworld
        boolean flag1 = worldserver.dimension() == Level.END; // targetIsEnd
        // CraftBukkit end

        if (!flag && !flag1) {
            boolean flag2 = worldserver.dimension() == Level.NETHER; // CraftBukkit

            if (this.level().dimension() != Level.NETHER && !flag2) { // CraftBukkit
                return null;
            } else {
                WorldBorder worldborder = worldserver.getWorldBorder();
                double d0 = DimensionType.getTeleportationScale(this.level().dimensionType(), worldserver.dimensionType());
                BlockPos blockposition = worldborder.clampToBounds(this.getX() * d0, this.getY(), this.getZ() * d0);
                // CraftBukkit start
                CraftPortalEvent event = callPortalEvent(((Entity) (Object) this), worldserver, new PositionImpl(blockposition.getX(), blockposition.getY(), blockposition.getZ()), PlayerTeleportEvent.TeleportCause.NETHER_PORTAL, flag2 ? 16 : 128, 16);
                if (event == null) {
                    return null;
                }
                final ServerLevel worldserverFinal = worldserver = ((CraftWorld) event.getTo().getWorld()).getHandle();
                worldborder = worldserverFinal.getWorldBorder();
                blockposition = worldborder.clampToBounds(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());

                return (PortalInfo) this.getExitPortal(worldserver, blockposition, flag2, worldborder, event.getSearchRadius(), event.getCanCreatePortal(), event.getCreationRadius()).map((blockutil_rectangle) -> {
                    // CraftBukkit end
                    BlockState iblockdata = this.level().getBlockState(this.portalEntrancePos);
                    Direction.Axis enumdirection_enumaxis;
                    Vec3 vec3d;

                    if (iblockdata.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                        enumdirection_enumaxis = (Direction.Axis) iblockdata.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                        BlockUtil.FoundRectangle blockutil_rectangle1 = BlockUtil.getLargestRectangleAround(this.portalEntrancePos, enumdirection_enumaxis, 21, Direction.Axis.Y, 21, (blockposition1) -> {
                            return this.level().getBlockState(blockposition1) == iblockdata;
                        });

                        vec3d = this.getRelativePortalPosition(enumdirection_enumaxis, blockutil_rectangle1);
                    } else {
                        enumdirection_enumaxis = Direction.Axis.X;
                        vec3d = new Vec3(0.5D, 0.0D, 0.0D);
                    }

                    var bukkitfiedPortalInfo = PortalShape.createPortalInfo(worldserverFinal, blockutil_rectangle, enumdirection_enumaxis, vec3d, ((Entity) (Object) this), this.getDeltaMovement(), this.getYRot(), this.getXRot()); // CraftBukkit
                    bukkitfiedPortalInfo.banner$setPortalEventInfo(event);
                    return bukkitfiedPortalInfo;
                }).orElse(null); // CraftBukkit - decompile error
            }
        } else {
            BlockPos blockposition1;

            if (flag1) {
                blockposition1 = ServerLevel.END_SPAWN_POINT;
            } else {
                blockposition1 = worldserver.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldserver.getSharedSpawnPos());
            }
            // CraftBukkit start
            CraftPortalEvent event = callPortalEvent(((Entity) (Object) this), worldserver, new PositionImpl(blockposition1.getX() + 0.5D, blockposition1.getY(), blockposition1.getZ() + 0.5D), PlayerTeleportEvent.TeleportCause.END_PORTAL, 0, 0);
            if (event == null) {
                return null;
            }

            var newPortalInfo = new PortalInfo(new Vec3(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ()), this.getDeltaMovement(), this.getYRot(), this.getXRot());
            newPortalInfo.banner$setWorld(((CraftWorld) event.getTo().getWorld()).getHandle());
            newPortalInfo.banner$setPortalEventInfo(event);
            return newPortalInfo;

            // CraftBukkit end
        }
    }*/

    @Override
    public CraftPortalEvent callPortalEvent(Entity entity, ServerLevel exitWorldServer, PositionImpl exitPosition, PlayerTeleportEvent.TeleportCause cause, int searchRadius, int creationRadius) {
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

    @Override
    public boolean canCollideWith(Entity entity) {
        return this.isPushable();
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
    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitEntity();
    }

    @Override
    public void banner$setBukkitEntity(CraftEntity bukkitEntity) {
        this.bukkitEntity = bukkitEntity;
    }
}
