package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.injection.server.level.InjectionServerPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorldBorder;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements InjectionServerPlayer {

    @Shadow public int lastSentExp;

    @Shadow protected abstract boolean bedInRange(BlockPos pos, Direction direction);

    @Shadow protected abstract boolean bedBlocked(BlockPos pos, Direction direction);

    @Shadow public abstract void setRespawnPosition(ResourceKey<Level> dimension, @Nullable BlockPos position, float angle, boolean forced, boolean sendMessage);

    @Shadow protected abstract int getCoprime(int i);

    @Shadow @Final public MinecraftServer server;
    @Shadow @Final public ServerPlayerGameMode gameMode;
    @Shadow private ResourceKey<Level> respawnDimension;

    @Shadow @Nullable public abstract BlockPos getRespawnPosition();

    @Shadow public abstract float getRespawnAngle();

    @Shadow public abstract void setServerLevel(ServerLevel serverLevel);

    @Shadow public abstract ServerLevel serverLevel();

    // CraftBukkit start
    public String displayName;
    public Component listName;
    public org.bukkit.Location compassTarget;
    public int newExp = 0;
    public int newLevel = 0;
    public int newTotalExp = 0;
    public boolean keepLevel = false;
    public double maxHealthCache;
    public boolean joining = true;
    public boolean sentListPacket = false;
    public Integer clientViewDistance;
    public String kickLeaveMessage = null; // SPIGOT-3034: Forward leave message to PlayerQuitEvent
    // CraftBukkit end

    public long timeOffset = 0;
    public WeatherType weather = null;
    public boolean relativeTime = true;
    public String locale = "en_us"; // CraftBukkit - add, lowercase
    private boolean banner$initialized = false;

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void banner$init(CallbackInfo ci) {
        this.displayName = this.getGameProfile() != null ? getScoreboardName() : "~FakePlayer~";
        this.banner$setBukkitPickUpLoot(true);
        this.maxHealthCache = this.getMaxHealth();
        this.banner$initialized = true;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void banner$readExtra(CompoundTag compound, CallbackInfo ci) {
        this.getBukkitEntity().readExtraData(compound);
        String spawnWorld = compound.getString("SpawnWorld");
        CraftWorld oldWorld = (CraftWorld) Bukkit.getWorld(spawnWorld);
        if (oldWorld != null) {
            this.respawnDimension = oldWorld.getHandle().dimension();
        }
    }

    @Redirect(method = "addAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hasExactlyOnePlayerPassenger()Z"))
    private boolean banner$nonPersistVehicle(Entity entity) {
        Entity entity1 = this.getVehicle();
        boolean persistVehicle = true;
        if (entity1 != null) {
            Entity vehicle;
            for (vehicle = entity1; vehicle != null; vehicle = vehicle.getVehicle()) {
                if (!vehicle.bridge$persist()) {
                    persistVehicle = false;
                    break;
                }
            }
        }
        return persistVehicle && entity.hasExactlyOnePlayerPassenger();
    }


    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void banner$writeExtra(CompoundTag compound, CallbackInfo ci) {
        this.getBukkitEntity().setExtraData(compound);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void banner$joining(CallbackInfo ci) {
        if (this.joining) {
            this.joining = false;
        }
    }

    @Redirect(method = "doTick", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundSetHealthPacket"))
    private ClientboundSetHealthPacket banner$useScaledHealth(float healthIn, int foodLevelIn, float saturationLevelIn) {
        return new ClientboundSetHealthPacket(this.getBukkitEntity().getScaledHealth(), foodLevelIn, saturationLevelIn);
    }

    @Inject(method = "doTick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;tickCount:I"))
    private void banner$updateHealthAndExp(CallbackInfo ci) {
        if (this.maxHealthCache != this.getMaxHealth()) {
            this.getBukkitEntity().updateScaledHealth();
        }
        if (this.bridge$oldLevel() == -1) {
            this.banner$setOldLevel(this.experienceLevel);
        }
        if (this.bridge$oldLevel() != this.experienceLevel) {
            CraftEventFactory.callPlayerLevelChangeEvent(this.getBukkitEntity(), this.bridge$oldLevel(), this.experienceLevel);
            this.banner$setOldLevel(this.experienceLevel);
        }
        if (this.getBukkitEntity().hasClientWorldBorder()) {
            ((CraftWorldBorder) this.getBukkitEntity().getWorldBorder()).getHandle().tick();
        }
    }


    @Redirect(method = "awardKillScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;forAllObjectives(Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    private void banner$useCustomScoreboard(Scoreboard instance, ObjectiveCriteria criteria, String scoreboardName, Consumer<Score> points) {
        ((CraftServer) Bukkit.getServer()).getScoreboardManager().getScoreboardScores(criteria, scoreboardName, points);
    }

    @Redirect(method = "handleTeamKill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;forAllObjectives(Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    private void banner$teamKill(Scoreboard instance, ObjectiveCriteria criteria, String scoreboardName, Consumer<Score> points) {
        ((CraftServer) Bukkit.getServer()).getScoreboardManager().getScoreboardScores(criteria, scoreboardName, points);
    }

    @Inject(method = "isPvpAllowed", cancellable = true, at = @At("HEAD"))
    private void banner$pvpMode(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue((this.level().bridge$pvpMode()));
    }

    @Override
    public void spawnIn(Level world) {
        this.setLevel(world);
        if (world == null) {
            this.unsetRemoved();
            Vec3 position = null;
            if (this.respawnDimension != null) {
                world = this.server.getLevel(this.respawnDimension);
                if (world != null && this.getRespawnPosition() != null) {
                    position = ServerPlayer.findRespawnPositionAndUseSpawnBlock((ServerLevel) world, this.getRespawnPosition(), this.getRespawnAngle(), false, false).orElse(null);
                }
            }
            if (world == null || position == null) {
                world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
                position = Vec3.atCenterOf(((ServerLevel) world).getSharedSpawnPos());
            }
            this.setLevel(world);
            this.setPos(position.x(), position.y(), position.z());
        }
        this.gameMode.setLevel((ServerLevel) world);
    }

    @Override
    public void resetPlayerWeather() {
        this.weather = null;
        this.setPlayerWeather(this.level().getLevelData().isRaining() ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
    }

    @Override
    public BlockPos getSpawnPoint(ServerLevel worldserver) {
        BlockPos blockposition = worldserver.getSharedSpawnPos();
        if (worldserver.dimensionType().hasSkyLight() && worldserver.serverLevelData.getGameType() != GameType.ADVENTURE) {
            long k;
            long l;
            int i = Math.max(0, this.server.getSpawnRadius(worldserver));
            int j = Mth.floor(worldserver.getWorldBorder().getDistanceToBorder(blockposition.getX(), blockposition.getZ()));
            if (j < i) {
                i = j;
            }
            if (j <= 1) {
                i = 1;
            }
            int i1 = (l = (k = (long) (i * 2 + 1)) * k) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) l;
            int j1 = this.getCoprime(i1);
            int k1 = new Random().nextInt(i1);
            for (int l1 = 0; l1 < i1; ++l1) {
                int i2 = (k1 + j1 * l1) % i1;
                int j2 = i2 % (i * 2 + 1);
                int k2 = i2 / (i * 2 + 1);
                BlockPos blockposition1 = PlayerRespawnLogic.getOverworldRespawnPos(worldserver, blockposition.getX() + j2 - i, blockposition.getZ() + k2 - i);
                if (blockposition1 == null) continue;
                return blockposition1;
            }
        }
        return blockposition;
    }

    @Override
    public Either<BedSleepingProblem, Unit> getBedResult(BlockPos blockposition, Direction enumdirection) {
        if (!this.isSleeping() && this.isAlive()) {
            if (!this.level().dimensionType().natural() || !this.level().dimensionType().bedWorks()) {
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
            }
            if (!this.bedInRange(blockposition, enumdirection)) {
                return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
            }
            if (this.bedBlocked(blockposition, enumdirection)) {
                return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
            }
            this.setRespawnPosition(this.level().dimension(), blockposition, this.getYRot(), false, true);
            if (this.level().isDay()) {
                return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            }
            if (!this.isCreative()) {
                double d0 = 8.0;
                double d1 = 5.0;
                Vec3 vec3d = Vec3.atBottomCenterOf(blockposition);
                List<Monster> list = this.level().getEntitiesOfClass(Monster.class, new AABB(vec3d.x() - 8.0, vec3d.y() - 5.0, vec3d.z() - 8.0, vec3d.x() + 8.0, vec3d.y() + 5.0, vec3d.z() + 8.0), entitymonster -> entitymonster.isPreventingPlayerRest((ServerPlayer) (Object) this));
                if (!list.isEmpty()) {
                    return Either.left(Player.BedSleepingProblem.NOT_SAFE);
                }
            }
            return Either.right(Unit.INSTANCE);
        }
        return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
    }
    
    @Override
    public void reset() {
        float exp = 0.0f;
        if (this.keepLevel) {
            exp = this.experienceProgress;
            this.newTotalExp = this.totalExperience;
            this.newLevel = this.experienceLevel;
        }
        this.setHealth(this.getMaxHealth());
        this.stopUsingItem();
        this.remainingFireTicks = 0;
        this.resetFallDistance();
        this.foodData = new FoodData();
        this.foodData.setEntityhuman((ServerPlayer) (Object) this);
        this.experienceLevel = this.newLevel;
        this.totalExperience = this.newTotalExp;
        this.experienceProgress = 0.0f;
        this.deathTime = 0;
        this.setArrowCount(0, true);
        this.removeAllEffects(EntityPotionEffectEvent.Cause.DEATH);
        this.effectsDirty = true;
        this.containerMenu = this.inventoryMenu;
        this.lastHurtByPlayer = null;
        this.lastHurtByMob = null;
        this.combatTracker = new CombatTracker((ServerPlayer) (Object) this);
        this.lastSentExp = -1;
        if (this.keepLevel) {
            this.experienceProgress = exp;
        } else {
            this.giveExperiencePoints(this.newExp);
        }
        this.keepLevel = false;
        this.setDeltaMovement(0, 0, 0);
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer)super.getBukkitEntity();
    }

    @Override
    public Scoreboard getScoreboard() {
        return getBukkitEntity().getScoreboard().getHandle();
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() || !getBukkitEntity().isOnline();
    }

    @Override
    public String toString() {
        return super.toString() + "(" + this.getScoreboardName() + " at " + this.getX() + "," + this.getY() + "," + this.getZ() + ")";
    }

    @Override
    public Component bridge$listName() {
        return listName;
    }

    @Override
    public void banner$setListName(Component listName) {
        this.listName = listName;
    }

    @Override
    public Location bridge$compassTarget() {
        return compassTarget;
    }

    @Override
    public void banner$setCompassTarget(Location compassTarget) {
        this.compassTarget = compassTarget;
    }

    @Override
    public int bridge$newExp() {
        return newExp;
    }

    @Override
    public void banner$setNewExp(int newExp) {
        this.newExp = newExp;
    }

    @Override
    public int bridge$newLevel() {
        return newLevel;
    }

    @Override
    public void banner$setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    @Override
    public int bridge$newTotalExp() {
        return newTotalExp;
    }

    @Override
    public void banner$setNewTotalExp(int newTotalExp) {
        this.newTotalExp = newTotalExp;
    }

    @Override
    public boolean bridge$keepLevel() {
        return keepLevel;
    }

    @Override
    public void banner$setKeepLevel(boolean keepLevel) {
        this.keepLevel = keepLevel;
    }

    @Override
    public double bridge$maxHealthCache() {
        return maxHealthCache;
    }

    @Override
    public void banner$setMaxHealthCache(double maxHealthCache) {
        this.maxHealthCache = maxHealthCache;
    }

    @Override
    public boolean bridge$joining() {
        return joining;
    }

    @Override
    public void banner$setJoining(boolean joining) {
        this.joining = joining;
    }

    @Override
    public boolean bridge$sentListPacket() {
        return sentListPacket;
    }

    @Override
    public void banner$setSentListPacket(boolean sentListPacket) {
        this.sentListPacket = sentListPacket;
    }

    @Override
    public Integer bridge$clientViewDistance() {
        return clientViewDistance;
    }

    @Override
    public void banner$setClientViewDistance(Integer clientViewDistance) {
        this.clientViewDistance = clientViewDistance;
    }

    @Override
    public String bridge$kickLeaveMessage() {
        return kickLeaveMessage;
    }

    @Override
    public void banner$setKickLeaveMessage(String kickLeaveMessage) {
        this.kickLeaveMessage = kickLeaveMessage;
    }

    @Override
    public String bridge$displayName() {
        return displayName;
    }

    @Override
    public void banner$setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public long bridge$timeOffset() {
        return timeOffset;
    }

    @Override
    public void banner$setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    @Override
    public boolean bridge$relativeTime() {
        return relativeTime;
    }

    @Override
    public void banner$setRelativeTime(boolean relativeTime) {
        this.relativeTime = relativeTime;
    }

    @Override
    public String bridge$locale() {
        return locale;
    }

    @Override
    public void banner$setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public boolean banner$initialized() {
        return  banner$initialized;
    }
}
