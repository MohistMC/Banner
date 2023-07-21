package com.mohistmc.banner.mixin.server.level;

import com.google.common.collect.Lists;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import com.mohistmc.banner.bukkit.DistValidate;
import com.mohistmc.banner.bukkit.LevelPersistentData;
import com.mohistmc.banner.fabric.BannerDerivedWorldInfo;
import com.mohistmc.banner.injection.server.level.InjectionServerLevel;
import com.mohistmc.banner.injection.world.level.storage.InjectionLevelStorageAccess;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTicks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_20_R1.util.BlockStateListPopulator;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.WorldUUID;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level implements WorldGenLevel, InjectionServerLevel {

    @Shadow public abstract LevelTicks<Block> getBlockTicks();

    @Shadow @Final private ServerChunkCache chunkSource;

    @Shadow public abstract List<ServerPlayer> players();

    @Shadow public abstract boolean sendParticles(ServerPlayer player, boolean longDistance, double posX, double posY, double posZ, Packet<?> packet);
    @Shadow @Final public ServerLevelData serverLevelData;

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Shadow public abstract <T extends ParticleOptions> int sendParticles(T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed);

    @Shadow @Final public PersistentEntitySectionManager<Entity> entityManager;
    @Shadow protected abstract void wakeUpAllPlayers();

    @Shadow @Final public static BlockPos END_SPAWN_POINT;

    @Shadow public abstract boolean addFreshEntity(Entity entity);

    @Shadow public abstract void addDuringTeleport(Entity entity);
    @Shadow public abstract boolean addWithUUID(Entity entity);

    @Shadow public abstract DimensionDataStorage getDataStorage();

    @Shadow @Final private MinecraftServer server;
    @Shadow private volatile boolean isUpdatingNavigations;
    @Shadow @Final private Set<Mob> navigatingMobs;

    @Shadow public abstract ServerChunkCache getChunkSource();

    public LevelStorageSource.LevelStorageAccess convertable;
    public UUID uuid;
    public PrimaryLevelData K;

    private transient boolean banner$force;
    private transient LightningStrikeEvent.Cause banner$cause;
    private final AtomicReference<CreatureSpawnEvent.SpawnReason> banner$reason = new AtomicReference<>();
    private final AtomicReference<Boolean> banner$timeSkipCancelled = new AtomicReference<>();
    public ResourceKey<LevelStem> typeKey;

    protected MixinServerLevel(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    public void banner$constructor(MinecraftServer minecraftServer, Executor backgroundExecutor, LevelStorageSource.LevelStorageAccess levelSave, ServerLevelData worldInfo, ResourceKey<Level> dimension, LevelStem levelStem, ChunkProgressListener statusListener, boolean isDebug, long seed, List<CustomSpawner> specialSpawners, boolean shouldBeTicking, RandomSequences randomSequences) {
        throw new RuntimeException();
    }

    public void banner$constructor(MinecraftServer minecraftServer, Executor backgroundExecutor, LevelStorageSource.LevelStorageAccess levelSave, PrimaryLevelData worldInfo, ResourceKey<Level> dimension, LevelStem levelStem, ChunkProgressListener statusListener, boolean isDebug, long seed, List<CustomSpawner> specialSpawners, boolean shouldBeTicking, RandomSequences randomSequences, org.bukkit.World.Environment env, org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider biomeProvider) {
        banner$constructor(minecraftServer, backgroundExecutor, levelSave, worldInfo, dimension, levelStem, statusListener, isDebug, seed, specialSpawners, shouldBeTicking, randomSequences);
        this.banner$setGenerator(gen);
        this.banner$setEnvironment(env);
        this.banner$setBiomeProvider(biomeProvider);
        if (gen != null) {
            this.chunkSource.chunkMap.generator = new CustomChunkGenerator((ServerLevel) (Object) this, this.chunkSource.getGenerator(), gen);
        }
        getWorld();
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void banner$initWorldServer(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci) {
        this.banner$setPvpMode(minecraftServer.isPvpAllowed());
        this.uuid = WorldUUID.getUUID(levelStorageAccess.getDimensionPath(this.dimension()).toFile());
        this.convertable = levelStorageAccess;
        var typeKey = ((InjectionLevelStorageAccess) levelStorageAccess).bridge$getTypeKey();
        if (typeKey != null) {
            this.typeKey = typeKey;
        } else {
            var dimensions = BukkitExtraConstants.getServer().registryAccess().registryOrThrow(Registries.LEVEL_STEM);
            var key = dimensions.getResourceKey(levelStem);
            if (key.isPresent()) {
                this.typeKey = key.get();
            } else {
                BannerServer.LOGGER.warn("Assign {} to unknown level stem {}", resourceKey.location(), levelStem);
                this.typeKey = ResourceKey.create(Registries.LEVEL_STEM, resourceKey.location());
            }
        }
        if (serverLevelData instanceof PrimaryLevelData) {
            this.K = (PrimaryLevelData) serverLevelData;
        } else if (serverLevelData instanceof DerivedLevelData) {
            this.K = BannerDerivedWorldInfo.create((DerivedLevelData)serverLevelData);
        }
        K.setWorld(((ServerLevel) (Object) this));
        var data = this.getDataStorage().computeIfAbsent(LevelPersistentData::new,
                () -> new LevelPersistentData(null), "bukkit_pdc");
        this.getWorld().readBukkitValues(data.getTag());
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;getViewDistance()I"))
    private int banner$setViewDistance(PlayerList instance) {
        return this.bridge$spigotConfig().viewDistance;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;getSimulationDistance()I"))
    private int banner$setSimulationDistance(PlayerList instance) {
        return this.bridge$spigotConfig().simulationDistance;
    }

    @Inject(method = "saveLevelData", at = @At("RETURN"))
    private void banner$savePdc(CallbackInfo ci) {
        var data = this.getDataStorage().computeIfAbsent(LevelPersistentData::new, () -> new LevelPersistentData(null), "bukkit_pdc");
        data.save(this.getWorld());
    }

    @Inject(method = "gameEvent", cancellable = true, at = @At("HEAD"))
    private void banner$gameEventEvent(GameEvent gameEvent, Vec3 pos, GameEvent.Context context, CallbackInfo ci) {
        var entity = context.sourceEntity();
        var i = gameEvent.getNotificationRadius();
        GenericGameEvent event = new GenericGameEvent(org.bukkit.GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.GAME_EVENT.getKey(gameEvent))), new Location(this.getWorld(), pos.x(), pos.y(), pos.z()), (entity == null) ? null : entity.getBukkitEntity(), i, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;isDebug()Z"))
    private void banner$timings(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        bridge$timings().doTickPending.startTiming(); // Spigot
    }

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    ordinal = 3))
    private void banner$timings0(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        bridge$timings().doTickPending.stopTiming(); // Spigot
    }

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;runBlockEvents()V"))
    private void banner$timings1(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        bridge$timings().doSounds.startTiming(); // Spigot
    }

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;runBlockEvents()V",
                    shift = At.Shift.AFTER))
    private void banner$timings2(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        bridge$timings().doSounds.stopTiming(); // Spigot
    }

    @Inject(method = "tick",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/server/level/ServerLevel;dragonFight:Lnet/minecraft/world/level/dimension/end/EndDragonFight;",
                    ordinal = 0))
    private void banner$timings3(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        bridge$timings().tickEntities.startTiming(); // Spigot
    }

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V"))
    private void banner$timings4(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        bridge$timings().entityTick.startTiming(); // Spigot
    }

    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 3))
    private void banner$timings5(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        bridge$timings().entityTick.stopTiming(); // Spigot
        bridge$timings().tickEntities.stopTiming(); // Spigot
    }

    @Inject(method = "tickNonPassenger", at = @At("HEAD"))
    private void banner$timings6(Entity entity, CallbackInfo ci) {
        entity.bridge$tickTimer().startTiming(); // Spigot
    }

    @Inject(method = "tickNonPassenger", at = @At("TAIL"))
    private void banner$timings7(Entity entity, CallbackInfo ci) {
        entity.bridge$tickTimer().stopTiming(); // Spigot
    }

    @Override
    public LevelChunk getChunkIfLoaded(int x, int z) {
        return this.chunkSource.getChunk(x, z, false);
    }

    @Override
    public <T extends ParticleOptions> int sendParticles(ServerPlayer sender, T t0, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, boolean force) {
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(t0, force, d0, d1, d2, (float) d3, (float) d4, (float) d5, (float) d6, i);
        int j = 0;
        for (ServerPlayer entity : this.players()) {
            if (sender == null || entity.getBukkitEntity().canSee(sender.getBukkitEntity())) {
                if (this.sendParticles(entity, force, d0, d1, d2, packet)) {
                    ++j;
                }
            }
        }
        return j;
    }

    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void banner$tickPortal(Entity entityIn, CallbackInfo ci) {
        entityIn.postTick();
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    private void banner$tickPortalPassenger(Entity ridingEntity, Entity passengerEntity, CallbackInfo ci) {
        passengerEntity.postTick();
    }

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public void banner$thunder(LevelChunk chunkIn, int randomTickSpeed, CallbackInfo ci) {
        pushAddEntityReason(CreatureSpawnEvent.SpawnReason.LIGHTNING);
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$thunder(ServerLevel serverWorld, Entity entityIn) {
        return strikeLightning(entityIn, LightningStrikeEvent.Cause.WEATHER);
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isThundering()Z"))
    private boolean banner$thunderChance(ServerLevel instance) {
        return this.isRaining() && this.isThundering() && this.bridge$spigotConfig().thunderChance > 0;
    }

    @ModifyConstant(method = "tickChunk", constant = @Constant(intValue = 100000))
    private int banner$configChane(int constant) {
        return this.bridge$spigotConfig().thunderChance;
    }

    @Override
    public boolean strikeLightning(Entity entity) {
        return this.strikeLightning(entity, LightningStrikeEvent.Cause.UNKNOWN);
    }

    @Override
    public boolean strikeLightning(Entity entity, LightningStrikeEvent.Cause cause) {
        if (banner$cause != null) {
            cause = banner$cause;
            banner$cause = null;
        }
        if (DistValidate.isValid((LevelAccessor) this)) {
            LightningStrikeEvent lightning = CraftEventFactory.callLightningStrikeEvent((LightningStrike) entity.getBukkitEntity(), cause);
            if (lightning.isCancelled()) {
                return false;
            }
        }
        return this.addFreshEntity(entity);
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    public boolean banner$snowForm(ServerLevel serverWorld, BlockPos pos, BlockState state) {
        return CraftEventFactory.handleBlockFormEvent(serverWorld, pos, state, null);
    }

    @Inject(method = "save", at = @At(value = "JUMP", ordinal = 0, opcode = Opcodes.IFNULL))
    private void banner$worldSaveEvent(ProgressListener progress, boolean flush, boolean skipSave, CallbackInfo ci) {
        if (DistValidate.isValid((LevelAccessor) this)) {
            Bukkit.getPluginManager().callEvent(new WorldSaveEvent(getWorld()));
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void banner$saveLevelDat(ProgressListener progress, boolean flush, boolean skipSave, CallbackInfo ci) {
        if (this.serverLevelData instanceof PrimaryLevelData worldInfo) {
            worldInfo.setWorldBorder(this.getWorldBorder().createSettings());
            worldInfo.setCustomBossEvents(BukkitExtraConstants.getServer().getCustomBossEvents().save());
            this.convertable.saveDataTag(BukkitExtraConstants.getServer().registryAccess(), worldInfo, BukkitExtraConstants.getServer().getPlayerList().getSingleplayerData());
        }
    }

    @Inject(method = "unload", at = @At("HEAD"))
    public void banner$closeOnChunkUnloading(LevelChunk chunkIn, CallbackInfo ci) {
        for (BlockEntity tileentity : chunkIn.getBlockEntities().values()) {
            if (tileentity instanceof Container) {
                for (HumanEntity h : Lists.newArrayList(((Container) tileentity).getViewers())) {
                    if (h instanceof CraftHumanEntity) {
                        ((CraftHumanEntity) h).getHandle().closeContainer();
                    }
                }
            }
        }
    }

    @Redirect(method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/server/level/ServerPlayer;ZDDDLnet/minecraft/network/protocol/Packet;)Z"))
    public boolean banner$particleVisible(ServerLevel serverWorld, ServerPlayer player, boolean longDistance, double posX, double posY, double posZ, Packet<?> packet) {
        return this.sendParticles(player, banner$force, posX, posY, posZ, packet);
    }

    @Override
    public <T extends ParticleOptions> int sendParticles(T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed, boolean force) {
        banner$force = force;
        return this.sendParticles(type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);
    }

    @Inject(method = "addEntity", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;addNewEntity(Lnet/minecraft/world/level/entity/EntityAccess;)Z"))
    private void banner$addEntityEvent(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        CreatureSpawnEvent.SpawnReason reason = banner$reason.get() == null ? CreatureSpawnEvent.SpawnReason.DEFAULT : banner$reason.get();
        banner$reason.set(null);
        if (DistValidate.isValid((LevelAccessor) this) && !CraftEventFactory.doEntityAddEventCalling((ServerLevel) (Object) this, entityIn, reason)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        pushAddEntityReason(reason);
        return addFreshEntity(entity);
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    public void banner$resetReason(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        banner$reason.set(null);
    }

    @Override
    public boolean addWithUUID(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        pushAddEntityReason(reason);
        return this.addWithUUID(entity);
    }

    @Override
    public void addDuringTeleport(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        pushAddEntityReason(reason);
        addDuringTeleport(entity);
    }

    @Override
    public boolean tryAddFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (entity.getSelfAndPassengers().map(Entity::getUUID).anyMatch(this.entityManager::isLoaded)) {
            return false;
        }else {
           pushAddEntityReason(reason);
           return this.addAllEntities(entity, reason);
        }
    }

    @Override
    public boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return addFreshEntity(entity, reason);
    }

    /**
     * @author wdog5
     * @reason functionallyy replaced
     */
    @Overwrite
    @Nullable
    public MapItemSavedData getMapData(String mapName) {
        return BukkitExtraConstants.getServer().overworld().getDataStorage().get((nbt) -> {
            MapItemSavedData newMap = MapItemSavedData.load(nbt);
            newMap.banner$setId(mapName);
            MapInitializeEvent event = new MapInitializeEvent(newMap.bridge$mapView());
            Bukkit.getServer().getPluginManager().callEvent(event);
            return newMap;
        }, mapName);
    }

    @Inject(method = "setMapData", at = @At("HEAD"))
    private void banner$mapSetId(String id, MapItemSavedData data, CallbackInfo ci) {
        data.banner$setId(id);
    }

    @Inject(method = "blockUpdated", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;updateNeighborsAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V"))
    private void banner$returnIfPopulate(BlockPos pos, Block block, CallbackInfo ci) {
        if (bridge$populating()) {
            ci.cancel();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    private void banner$timeSkip(ServerLevel world, long time) {
        TimeSkipEvent event = new TimeSkipEvent(this.getWorld(), TimeSkipEvent.SkipReason.NIGHT_SKIP, (time - time % 24000L) - this.getDayTime());
        Bukkit.getPluginManager().callEvent(event);
        banner$timeSkipCancelled.set(event.isCancelled());
        if (!event.isCancelled()) {
            world.setDayTime(this.getDayTime() + event.getSkipAmount());
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;wakeUpAllPlayers()V"))
    private void banner$notWakeIfCancelled(ServerLevel world) {
        if (!banner$timeSkipCancelled.get()) {
            this.wakeUpAllPlayers();
        }
        banner$timeSkipCancelled.set(false);
    }

    @ModifyVariable(method = "tickBlock", ordinal = 0, argsOnly = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private BlockPos banner$captureTickingBlock(BlockPos pos) {
        BukkitCaptures.captureTickingBlock((ServerLevel) (Object) this, pos);
        return pos;
    }

    @ModifyVariable(method = "tickBlock", ordinal = 0, argsOnly = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/block/state/BlockState;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private BlockPos banner$resetTickingBlock(BlockPos pos) {
        BukkitCaptures.resetTickingBlock();
        return pos;
    }

    /**
     * @author wdog5
     * @reason
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Overwrite
    public static void makeObsidianPlatform(ServerLevel world) {
        BlockPos blockpos = END_SPAWN_POINT;
        int i = blockpos.getX();
        int j = blockpos.getY() - 2;
        int k = blockpos.getZ();
        BlockStateListPopulator blockList = new BlockStateListPopulator(world);
        BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach((pos) -> {
            blockList.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        });
        BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach((pos) -> {
            blockList.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
        });
        if (!DistValidate.isValid(world)) {
            blockList.updateList();
            BukkitCaptures.getEndPortalEntity();
            return;
        }
        CraftWorld bworld = world.getWorld();
        boolean spawnPortal = BukkitCaptures.getEndPortalSpawn();
        Entity entity = BukkitCaptures.getEndPortalEntity();
        PortalCreateEvent portalEvent = new PortalCreateEvent((List) blockList.getList(), bworld, entity == null ? null : entity.getBukkitEntity(), PortalCreateEvent.CreateReason.END_PLATFORM);
        portalEvent.setCancelled(!spawnPortal);
        Bukkit.getPluginManager().callEvent(portalEvent);
        if (!portalEvent.isCancelled()) {
            blockList.updateList();
        }
    }

    /**
     * @author wdog5
     * @reason bukkit check
     */
    @Overwrite
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
        Iterator<ServerPlayer> var4 = this.server.getPlayerList().getPlayers().iterator();

        // CraftBukkit start
        Player entityhuman = null;
        Entity entity = this.getEntity(breakerId);
        if (entity instanceof Player) entityhuman = (Player) entity;
        // CraftBukkit end

        while(var4.hasNext()) {
            ServerPlayer serverPlayer = (ServerPlayer)var4.next();
            if (serverPlayer != null && serverPlayer.level() == ((ServerLevel) (Object) this) && serverPlayer.getId() != breakerId) {
                double d = (double)pos.getX() - serverPlayer.getX();
                double e = (double)pos.getY() - serverPlayer.getY();
                double f = (double)pos.getZ() - serverPlayer.getZ();

                // CraftBukkit start
                if (entityhuman != null && !serverPlayer.getBukkitEntity().canSee(entityhuman.getBukkitEntity())) {
                    continue;
                }
                // CraftBukkit end
                if (d * d + e * e + f * f < 1024.0) {
                    serverPlayer.connection.send(new ClientboundBlockDestructionPacket(breakerId, pos, progress));
                }
            }
        }

    }

    /**
     * @author wdog5
     * @reason bukkit things
     */
    @Overwrite
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        if (this.isUpdatingNavigations) {
            String string = "recursive call to sendBlockUpdated";
            Util.logAndPauseIfInIde("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
        }

        this.getChunkSource().blockChanged(pos);
        VoxelShape voxelShape = oldState.getCollisionShape(this, pos);
        VoxelShape voxelShape2 = newState.getCollisionShape(this, pos);
        if (Shapes.joinIsNotEmpty(voxelShape, voxelShape2, BooleanOp.NOT_SAME)) {
            List<PathNavigation> list = new ObjectArrayList<>();
            Iterator var8 = this.navigatingMobs.iterator();

            while(var8.hasNext()) {
                // CraftBukkit start - fix SPIGOT-6362
                Mob mob;
                try {
                    mob = (Mob)var8.next();
                } catch (java.util.ConcurrentModificationException ex) {
                    // This can happen because the pathfinder update below may trigger a chunk load, which in turn may cause more navigators to register
                    // In this case we just run the update again across all the iterators as the chunk will then be loaded
                    // As this is a relative edge case it is much faster than copying navigators (on either read or write)
                    sendBlockUpdated(pos, oldState, newState, flags);
                    return;
                }
                // CraftBukkit end
                PathNavigation pathNavigation = mob.getNavigation();
                if (pathNavigation.shouldRecomputePath(pos)) {
                    list.add(pathNavigation);
                }
            }

            try {
                this.isUpdatingNavigations = true;
                var8 = list.iterator();

                while(var8.hasNext()) {
                    PathNavigation pathNavigation2 = (PathNavigation)var8.next();
                    pathNavigation2.recomputePath();
                }
            } finally {
                this.isUpdatingNavigations = false;
            }

        }
    }


    @ModifyVariable(method = "tickChunk", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private BlockPos banner$captureRandomTick(BlockPos pos) {
        BukkitCaptures.captureTickingBlock((ServerLevel) (Object) this, pos);
        return pos;
    }

    @ModifyVariable(method = "tickChunk", ordinal = 0, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private BlockPos banner$resetRandomTick(BlockPos pos) {
        BukkitCaptures.resetTickingBlock();
        return pos;
    }

    @ModifyVariable(method = "tickNonPassenger", argsOnly = true, ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private Entity banner$captureTickingEntity(Entity entity) {
        BukkitCaptures.captureTickingEntity(entity);
        return entity;
    }

    @ModifyVariable(method = "tickNonPassenger", argsOnly = true, ordinal = 0, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private Entity banner$resetTickingEntity(Entity entity) {
        BukkitCaptures.resetTickingEntity();
        return entity;
    }

    @ModifyVariable(method = "tickPassenger", argsOnly = true, ordinal = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    private Entity banner$captureTickingPassenger(Entity entity) {
        BukkitCaptures.captureTickingEntity(entity);
        return entity;
    }

    @ModifyVariable(method = "tickPassenger", argsOnly = true, ordinal = 1, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    private Entity banner$resetTickingPassenger(Entity entity) {
        BukkitCaptures.resetTickingEntity();
        return entity;
    }

    @Override
    public boolean addEntitySerialized(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return addWithUUID(entity, reason);
    }

    @Override
    public PrimaryLevelData bridge$serverLevelDataCB() {
        return K;
    }

    @Override
    public LevelStorageSource.LevelStorageAccess bridge$convertable() {
        return convertable;
    }

    @Override
    public ResourceKey<LevelStem> getTypeKey() {
        return typeKey;
    }

    @Override
    public UUID bridge$uuid() {
        return uuid;
    }
}
