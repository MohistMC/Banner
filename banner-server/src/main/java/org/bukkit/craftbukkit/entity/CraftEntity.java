package org.bukkit.craftbukkit.entity;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mohistmc.banner.bukkit.entity.BannerModAbstractVillager;
import com.mohistmc.banner.bukkit.entity.BannerModGolem;
import com.mohistmc.banner.bukkit.entity.BannerModLivingEntity;
import com.mohistmc.banner.bukkit.entity.BannerModMinecart;
import com.mohistmc.banner.bukkit.entity.BannerModMinecartContainer;
import com.mohistmc.banner.bukkit.entity.BannerModMob;
import com.mohistmc.banner.bukkit.entity.BannerModVehicle;
import com.mohistmc.banner.bukkit.entity.BannerModMonster;
import com.mohistmc.banner.bukkit.entity.BannerModProjectile;
import com.mohistmc.banner.bukkit.entity.BannerModRaider;
import com.mohistmc.banner.bukkit.entity.BannerModSkeleton;
import com.mohistmc.banner.bukkit.entity.BannerModChestedHorse;
import com.mohistmc.banner.bukkit.entity.BannerModAnimals;
import com.mohistmc.banner.bukkit.entity.BannerModEntity;
import com.mohistmc.banner.bukkit.entity.BannerModHorse;
import com.mohistmc.banner.bukkit.entity.BannerModTameableAnimal;
import com.mohistmc.banner.bukkit.entity.BannerModThrowableProjectile;
import com.mohistmc.banner.bukkit.entity.BannerModVillager;
import com.mohistmc.banner.bukkit.entity.BannerModWindCharge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.phys.AABB;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftSpawnCategory;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.BaseComponent; // Spigot

public abstract class CraftEntity implements org.bukkit.entity.Entity {
    private static PermissibleBase perm;
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

    protected final CraftServer server;
    protected Entity entity;
    private final EntityType entityType;
    private EntityDamageEvent lastDamageEvent;
    private final CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(CraftEntity.DATA_TYPE_REGISTRY);

    public CraftEntity(final CraftServer server, final Entity entity) {
        this.server = server;
        this.entity = entity;
        this.entityType = CraftEntityType.minecraftToBukkit(entity.getType());
    }

    public static <T extends Entity> CraftEntity getEntity(CraftServer server, T entity) {
        Preconditions.checkArgument(entity != null, "Unknown entity");

        // Special case human, since bukkit use Player interface for ...
        if (entity instanceof net.minecraft.world.entity.player.Player && !(entity instanceof ServerPlayer)) {
            return new CraftHumanEntity(server, (net.minecraft.world.entity.player.Player) entity);
        }

        // Special case complex part, since there is no extra entity type for them
        if (entity instanceof EnderDragonPart complexPart) {
            if (complexPart.parentMob instanceof EnderDragon) {
                return new CraftEnderDragonPart(server, complexPart);
            } else {
                return new CraftComplexPart(server, complexPart);
            }
        }

        CraftEntityTypes.EntityTypeData<?, T> entityTypeData = CraftEntityTypes.getEntityTypeData(CraftEntityType.minecraftToBukkit(entity.getType()));

        if (entityTypeData != null) {
            return (CraftEntity) entityTypeData.convertFunction().apply(server, entity);
        }

        CraftEntity modsEntity = null;
        switch (entity) {
            case AbstractSkeleton abstractSkeleton -> modsEntity = new BannerModSkeleton(server, abstractSkeleton);
            case AbstractChestedHorse chestedHorse -> modsEntity = new BannerModChestedHorse(server, chestedHorse);
            case AbstractHorse abstractHorse -> modsEntity = new BannerModHorse(server, abstractHorse);
            case AbstractGolem abstractGolem -> modsEntity = new BannerModGolem(server, abstractGolem);
            case AbstractMinecartContainer abstractMinecartContainer -> modsEntity = new BannerModMinecartContainer(server, abstractMinecartContainer);
            case AbstractMinecart abstractMinecart -> modsEntity = new BannerModMinecart(server, abstractMinecart);
            case AbstractWindCharge abstractWindCharge -> modsEntity = new BannerModWindCharge(server, abstractWindCharge);
            case ThrowableItemProjectile throwableItemProjectile -> modsEntity = new BannerModThrowableProjectile(server, throwableItemProjectile);
            case Projectile projectile -> modsEntity = new BannerModProjectile(server, projectile);
            case Raider raider -> modsEntity = new BannerModRaider(server, raider);
            case Monster monster -> modsEntity = new BannerModMonster(server, monster);
            case TamableAnimal tamableAnimal -> modsEntity = new BannerModTameableAnimal(server, tamableAnimal);
            case Animal animal -> modsEntity = new BannerModAnimals(server, animal);
            case Villager villager -> modsEntity = new BannerModVillager(server, villager);
            case AbstractVillager abstractVillager -> modsEntity = new BannerModAbstractVillager(server, abstractVillager);
            case Mob mob -> modsEntity = new BannerModMob(server, mob);
            case VehicleEntity vehicle -> modsEntity = new BannerModVehicle(server, vehicle);
            case LivingEntity livingEntity -> modsEntity = new BannerModLivingEntity(server, livingEntity);
            case Entity entity1 -> modsEntity = new BannerModEntity(server, entity1);
        }

        if (modsEntity != null) {
            return modsEntity;
        }

        throw new AssertionError("Unknown entity " + (entity == null ? null : entity.getClass()));
    }

    @Override
    public Location getLocation() {
        return CraftLocation.toBukkit(this.entity.position(), this.getWorld(), this.entity.getBukkitYaw(), this.entity.getXRot());
    }

    @Override
    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(this.getWorld());
            loc.setX(this.entity.getX());
            loc.setY(this.entity.getY());
            loc.setZ(this.entity.getZ());
            loc.setYaw(this.entity.getBukkitYaw());
            loc.setPitch(this.entity.getXRot());
        }

        return loc;
    }

    @Override
    public Vector getVelocity() {
        return CraftVector.toBukkit(this.entity.getDeltaMovement());
    }

    @Override
    public void setVelocity(Vector velocity) {
        Preconditions.checkArgument(velocity != null, "velocity");
        velocity.checkFinite();
        this.entity.setDeltaMovement(CraftVector.toNMS(velocity));
        this.entity.hurtMarked = true;
    }

    @Override
    public double getHeight() {
        return this.getHandle().getBbHeight();
    }

    @Override
    public double getWidth() {
        return this.getHandle().getBbWidth();
    }

    @Override
    public BoundingBox getBoundingBox() {
        AABB bb = this.getHandle().getBoundingBox();
        return new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    @Override
    public boolean isOnGround() {
        if (this.entity instanceof AbstractArrow) {
            return ((AbstractArrow) this.entity).inGround;
        }
        return this.entity.onGround();
    }

    @Override
    public boolean isInWater() {
        return this.entity.isInWater();
    }

    @Override
    public World getWorld() {
        return this.entity.level().getWorld();
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        NumberConversions.checkFinite(pitch, "pitch not finite");
        NumberConversions.checkFinite(yaw, "yaw not finite");

        yaw = Location.normalizeYaw(yaw);
        pitch = Location.normalizePitch(pitch);

        this.entity.setYRot(yaw);
        this.entity.setXRot(pitch);
        this.entity.yRotO = yaw;
        this.entity.xRotO = pitch;
        this.entity.setYHeadRot(yaw);
    }

    @Override
    public boolean teleport(Location location) {
        return this.teleport(location, TeleportCause.PLUGIN);
    }

    @Override
    public boolean teleport(Location location, TeleportCause cause) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        location.checkFinite();

        if (this.entity.isVehicle() || this.entity.isRemoved()) {
            return false;
        }

        // If this entity is riding another entity, we must dismount before teleporting.
        this.entity.stopRiding();

        // Let the server handle cross world teleports
        if (location.getWorld() != null && !location.getWorld().equals(this.getWorld())) {
            // Prevent teleportation to an other world during world generation
            Preconditions.checkState(!this.entity.bridge$generation(), "Cannot teleport entity to an other world during world generation");
            entity.teleportTo(((CraftWorld) location.getWorld()).getHandle(), CraftLocation.toVec3D(location));
            return true;
        }

        // entity.setLocation() throws no event, and so cannot be cancelled
        this.entity.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        // SPIGOT-619: Force sync head rotation also
        this.entity.setYHeadRot(location.getYaw());

        return true;
    }

    @Override
    public boolean teleport(org.bukkit.entity.Entity destination) {
        return this.teleport(destination.getLocation());
    }

    @Override
    public boolean teleport(org.bukkit.entity.Entity destination, TeleportCause cause) {
        return this.teleport(destination.getLocation(), cause);
    }

    @Override
    public List<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) {
        Preconditions.checkState(!this.entity.bridge$generation(), "Cannot get nearby entities during world generation");
        org.spigotmc.AsyncCatcher.catchOp("getNearbyEntities"); // Spigot

        List<Entity> notchEntityList = this.entity.level().getEntities(this.entity, this.entity.getBoundingBox().inflate(x, y, z), Predicates.alwaysTrue());
        List<org.bukkit.entity.Entity> bukkitEntityList = new java.util.ArrayList<org.bukkit.entity.Entity>(notchEntityList.size());

        for (Entity e : notchEntityList) {
            bukkitEntityList.add(e.getBukkitEntity());
        }
        return bukkitEntityList;
    }

    @Override
    public int getEntityId() {
        return this.entity.getId();
    }

    @Override
    public int getFireTicks() {
        return this.entity.getRemainingFireTicks();
    }

    @Override
    public int getMaxFireTicks() {
        return this.entity.getFireImmuneTicks();
    }

    @Override
    public void setFireTicks(int ticks) {
        this.entity.setRemainingFireTicks(ticks);
    }

    @Override
    public void setVisualFire(boolean fire) {
        this.getHandle().hasVisualFire = fire;
    }

    @Override
    public boolean isVisualFire() {
        return this.getHandle().hasVisualFire;
    }

    @Override
    public int getFreezeTicks() {
        return this.getHandle().getTicksFrozen();
    }

    @Override
    public int getMaxFreezeTicks() {
        return this.getHandle().getTicksRequiredToFreeze();
    }

    @Override
    public void setFreezeTicks(int ticks) {
        Preconditions.checkArgument(0 <= ticks, "Ticks (%s) cannot be less than 0", ticks);

        this.getHandle().setTicksFrozen(ticks);
    }

    @Override
    public boolean isFrozen() {
        return this.getHandle().isFullyFrozen();
    }

    @Override
    public void remove() {
        entity.discard();
    }

    @Override
    public boolean isDead() {
        return !this.entity.isAlive();
    }

    @Override
    public boolean isValid() {
        return this.entity.isAlive() && this.entity.bridge$valid() && this.entity.isChunkLoaded() && this.isInWorld();
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public boolean isPersistent() {
        return this.entity.bridge$persist();
    }

    @Override
    public void setPersistent(boolean persistent) {
        this.entity.banner$setPersist(persistent);
    }

    public Vector getMomentum() {
        return this.getVelocity();
    }

    public void setMomentum(Vector value) {
        this.setVelocity(value);
    }

    @Override
    public org.bukkit.entity.Entity getPassenger() {
        return this.isEmpty() ? null : this.getHandle().passengers.get(0).getBukkitEntity();
    }

    @Override
    public boolean setPassenger(org.bukkit.entity.Entity passenger) {
        Preconditions.checkArgument(!this.equals(passenger), "Entity cannot ride itself.");
        if (passenger instanceof CraftEntity) {
            this.eject();
            return ((CraftEntity) passenger).getHandle().startRiding(this.getHandle());
        } else {
            return false;
        }
    }

    @Override
    public List<org.bukkit.entity.Entity> getPassengers() {
        return Lists.newArrayList(Lists.transform(this.getHandle().passengers, (Function<Entity, org.bukkit.entity.Entity>) input -> input.getBukkitEntity()));
    }

    @Override
    public boolean addPassenger(org.bukkit.entity.Entity passenger) {
        Preconditions.checkArgument(passenger != null, "Entity passenger cannot be null");
        Preconditions.checkArgument(!this.equals(passenger), "Entity cannot ride itself.");

        return ((CraftEntity) passenger).getHandle().startRiding(this.getHandle(), true);
    }

    @Override
    public boolean removePassenger(org.bukkit.entity.Entity passenger) {
        Preconditions.checkArgument(passenger != null, "Entity passenger cannot be null");

        ((CraftEntity) passenger).getHandle().stopRiding();
        return true;
    }

    @Override
    public boolean isEmpty() {
        return !this.getHandle().isVehicle();
    }

    @Override
    public boolean eject() {
        if (this.isEmpty()) {
            return false;
        }

        this.getHandle().ejectPassengers();
        return true;
    }

    @Override
    public float getFallDistance() {
        return this.getHandle().fallDistance;
    }

    @Override
    public void setFallDistance(float distance) {
        this.getHandle().fallDistance = distance;
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent event) {
        this.lastDamageEvent = event;
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        return this.lastDamageEvent;
    }

    @Override
    public UUID getUniqueId() {
        return this.getHandle().getUUID();
    }

    @Override
    public int getTicksLived() {
        return this.getHandle().tickCount;
    }

    @Override
    public void setTicksLived(int value) {
        Preconditions.checkArgument(value > 0, "Age value (%s) must be greater than 0", value);
        this.getHandle().tickCount = value;
    }

    public Entity getHandle() {
        return this.entity;
    }

    @Override
    public final EntityType getType() {
        return this.entityType;
    }

    @Override
    public void playEffect(EntityEffect type) {
        Preconditions.checkArgument(type != null, "Type cannot be null");
        Preconditions.checkState(!this.entity.bridge$generation(), "Cannot play effect during world generation");

        if (type.getApplicable().isInstance(this)) {
            this.getHandle().level().broadcastEntityEvent(this.getHandle(), type.getData());
        }
    }

    @Override
    public Sound getSwimSound() {
        return CraftSound.minecraftToBukkit(this.getHandle().getSwimSound0());
    }

    @Override
    public Sound getSwimSplashSound() {
        return CraftSound.minecraftToBukkit(this.getHandle().getSwimSplashSound0());
    }

    @Override
    public Sound getSwimHighSpeedSplashSound() {
        return CraftSound.minecraftToBukkit(this.getHandle().getSwimHighSpeedSplashSound0());
    }

    public void setHandle(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "CraftEntity{" + "id=" + this.getEntityId() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final CraftEntity other = (CraftEntity) obj;
        return (this.getEntityId() == other.getEntityId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.getEntityId();
        return hash;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        this.server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return this.server.getEntityMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return this.server.getEntityMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        this.server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public boolean isInsideVehicle() {
        return this.getHandle().isPassenger();
    }

    @Override
    public boolean leaveVehicle() {
        if (!this.isInsideVehicle()) {
            return false;
        }

        this.getHandle().stopRiding();
        return true;
    }

    @Override
    public org.bukkit.entity.Entity getVehicle() {
        if (!this.isInsideVehicle()) {
            return null;
        }

        return this.getHandle().getVehicle().getBukkitEntity();
    }

    @Override
    public void setCustomName(String name) {
        // sane limit for name length
        if (name != null && name.length() > 256) {
            name = name.substring(0, 256);
        }

        this.getHandle().setCustomName(CraftChatMessage.fromStringOrNull(name));
    }

    @Override
    public String getCustomName() {
        Component name = this.getHandle().getCustomName();

        if (name == null) {
            return null;
        }

        return CraftChatMessage.fromComponent(name);
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        this.getHandle().setCustomNameVisible(flag);
    }

    @Override
    public boolean isCustomNameVisible() {
        return this.getHandle().isCustomNameVisible();
    }

    @Override
    public void setVisibleByDefault(boolean visible) {
        if (this.getHandle().bridge$visibleByDefault() != visible) {
            if (visible) {
                // Making visible by default, reset and show to all players
                for (Player player : this.server.getOnlinePlayers()) {
                    ((CraftPlayer) player).resetAndShowEntity(this);
                }
            } else {
                // Hiding by default, reset and hide from all players
                for (Player player : this.server.getOnlinePlayers()) {
                    ((CraftPlayer) player).resetAndHideEntity(this);
                }
            }

            this.getHandle().banner$setVisibleByDefault(visible);
        }
    }

    @Override
    public boolean isVisibleByDefault() {
        return this.getHandle().bridge$visibleByDefault();
    }

    @Override
    public Set<Player> getTrackedBy() {
        Preconditions.checkState(!this.entity.bridge$generation(), "Cannot get tracking players during world generation");
        ImmutableSet.Builder<Player> players = ImmutableSet.builder();

        ServerLevel world = ((CraftWorld) this.getWorld()).getHandle();
        ChunkMap.TrackedEntity entityTracker = world.getChunkSource().chunkMap.entityMap.get(this.getEntityId());

        if (entityTracker != null) {
            for (ServerPlayerConnection connection : entityTracker.seenBy) {
                players.add(connection.getPlayer().getBukkitEntity());
            }
        }

        return players.build();
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(String... messages) {

    }

    @Override
    public void sendMessage(UUID sender, String message) {
        this.sendMessage(message); // Most entities don't know about senders
    }

    @Override
    public void sendMessage(UUID sender, String... messages) {
        this.sendMessage(messages); // Most entities don't know about senders
    }

    @Override
    public String getName() {
        return CraftChatMessage.fromComponent(this.getHandle().getName());
    }

    @Override
    public boolean isPermissionSet(String name) {
        return CraftEntity.getPermissibleBase().isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return CraftEntity.getPermissibleBase().isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return CraftEntity.getPermissibleBase().hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return CraftEntity.getPermissibleBase().hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return CraftEntity.getPermissibleBase().addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return CraftEntity.getPermissibleBase().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return CraftEntity.getPermissibleBase().addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return CraftEntity.getPermissibleBase().addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        CraftEntity.getPermissibleBase().removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        CraftEntity.getPermissibleBase().recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return CraftEntity.getPermissibleBase().getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return CraftEntity.getPermissibleBase().isOp();
    }

    @Override
    public void setOp(boolean value) {
        CraftEntity.getPermissibleBase().setOp(value);
    }

    @Override
    public void setGlowing(boolean flag) {
        this.getHandle().setGlowingTag(flag);
    }

    @Override
    public boolean isGlowing() {
        return this.getHandle().isCurrentlyGlowing();
    }

    @Override
    public void setInvulnerable(boolean flag) {
        this.getHandle().setInvulnerable(flag);
    }

    @Override
    public boolean isInvulnerable() {
        return this.getHandle().isInvulnerableTo(this.getHandle().damageSources().generic());
    }

    @Override
    public boolean isSilent() {
        return this.getHandle().isSilent();
    }

    @Override
    public void setSilent(boolean flag) {
        this.getHandle().setSilent(flag);
    }

    @Override
    public boolean hasGravity() {
        return !this.getHandle().isNoGravity();
    }

    @Override
    public void setGravity(boolean gravity) {
        this.getHandle().setNoGravity(!gravity);
    }

    @Override
    public int getPortalCooldown() {
        return this.getHandle().portalCooldown;
    }

    @Override
    public void setPortalCooldown(int cooldown) {
        this.getHandle().portalCooldown = cooldown;
    }

    @Override
    public Set<String> getScoreboardTags() {
        return this.getHandle().getTags();
    }

    @Override
    public boolean addScoreboardTag(String tag) {
        return this.getHandle().addTag(tag);
    }

    @Override
    public boolean removeScoreboardTag(String tag) {
        return this.getHandle().removeTag(tag);
    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.getById(this.getHandle().getPistonPushReaction().ordinal());
    }

    @Override
    public BlockFace getFacing() {
        // Use this method over getDirection because it handles boats and minecarts.
        return CraftBlock.notchToBlockFace(this.getHandle().getMotionDirection());
    }

    @Override
    public CraftPersistentDataContainer getPersistentDataContainer() {
        return this.persistentDataContainer;
    }

    @Override
    public Pose getPose() {
        return Pose.values()[this.getHandle().getPose().ordinal()];
    }

    @Override
    public SpawnCategory getSpawnCategory() {
        return CraftSpawnCategory.toBukkit(this.getHandle().getType().getCategory());
    }

    @Override
    public boolean isInWorld() {
        return this.getHandle().bridge$inWorld();
    }

    @Override
    public String getAsString() {
        CompoundTag tag = new CompoundTag();
        if (!this.getHandle().saveAsPassenger(tag)) {
            return null;
        }

        return tag.getAsString();
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return CraftEntitySnapshot.create(this);
    }

    @Override
    public org.bukkit.entity.Entity copy() {
        Entity copy = this.copy(this.getHandle().level());
        Preconditions.checkArgument(copy != null, "Error creating new entity.");

        return copy.getBukkitEntity();
    }

    @Override
    public org.bukkit.entity.Entity copy(Location location) {
        Preconditions.checkArgument(location.getWorld() != null, "Location has no world");

        Entity copy = this.copy(((CraftWorld) location.getWorld()).getHandle());
        Preconditions.checkArgument(copy != null, "Error creating new entity.");

        copy.setPos(location.getX(), location.getY(), location.getZ());
        return location.getWorld().addEntity(copy.getBukkitEntity());
    }

    private Entity copy(net.minecraft.world.level.Level level) {
        CompoundTag compoundTag = new CompoundTag();
        this.getHandle().saveAsPassenger(compoundTag);

        return net.minecraft.world.entity.EntityType.loadEntityRecursive(compoundTag, level, java.util.function.Function.identity());
    }

    public void storeBukkitValues(CompoundTag c) {
        if (!this.persistentDataContainer.isEmpty()) {
            c.put("BukkitValues", this.persistentDataContainer.toTagCompound());
        }
    }

    public void readBukkitValues(CompoundTag c) {
        Tag base = c.get("BukkitValues");
        if (base instanceof CompoundTag) {
            this.persistentDataContainer.putAll((CompoundTag) base);
        }
    }

    protected CompoundTag save() {
        CompoundTag nbttagcompound = new CompoundTag();

        nbttagcompound.putString("id", this.getHandle().getEncodeId());
        this.getHandle().saveWithoutId(nbttagcompound);

        return nbttagcompound;
    }

    // re-sends the spawn entity packet to updated values which cannot be updated otherwise
    protected void update() {
        if (!this.getHandle().isAlive()) {
            return;
        }

        ServerLevel world = ((CraftWorld) this.getWorld()).getHandle();
        ChunkMap.TrackedEntity entityTracker = world.getChunkSource().chunkMap.entityMap.get(this.getEntityId());

        if (entityTracker == null) {
            return;
        }

        entityTracker.broadcast(this.getHandle().getAddEntityPacket(entityTracker.serverEntity));
    }

    public void update(ServerPlayer player) {
        if (!getHandle().isAlive()) {
            return;
        }

        ServerLevel world = ((CraftWorld) getWorld()).getHandle();
        ChunkMap.TrackedEntity entityTracker = world.getChunkSource().chunkMap.entityMap.get(getEntityId());

        if (entityTracker == null) {
            return;
        }

        player.connection.send(getHandle().getAddEntityPacket(entityTracker.serverEntity));
    }


    private static PermissibleBase getPermissibleBase() {
        if (CraftEntity.perm == null) {
            CraftEntity.perm = new PermissibleBase(new ServerOperator() {

                @Override
                public boolean isOp() {
                    return false;
                }

                @Override
                public void setOp(boolean value) {

                }
            });
        }
        return CraftEntity.perm;
    }

    // Spigot start
    private final org.bukkit.entity.Entity.Spigot spigot = new org.bukkit.entity.Entity.Spigot()
    {

        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component)
        {
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components)
        {
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent... components)
        {
        }

        @Override
        public void sendMessage(UUID sender, BaseComponent component)
        {
        }
    };

    public org.bukkit.entity.Entity.Spigot spigot()
    {
        return this.spigot;
    }
    // Spigot end
}
