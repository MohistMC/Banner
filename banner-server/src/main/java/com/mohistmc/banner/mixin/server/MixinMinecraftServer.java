package com.mohistmc.banner.mixin.server;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mohistmc.banner.api.color.ColorsAPI;
import com.mohistmc.banner.asm.annotation.TransformAccess;
import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.config.BannerConfig;
import com.mohistmc.banner.config.BannerConfigUtil;
import com.mohistmc.banner.fabric.BukkitRegistry;
import com.mohistmc.banner.injection.server.InjectionMinecraftServer;
import com.mohistmc.banner.util.I18n;
import com.mojang.datafixers.DataFixer;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import it.unimi.dsi.fastutil.longs.LongIterator;
import jline.console.ConsoleReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.Services;
import net.minecraft.server.TickTask;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spigotmc.WatchdogThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Banner - TODO fix inject method
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements InjectionMinecraftServer {

    // @formatter:off
    @Shadow public MinecraftServer.ReloadableResources resources;

    @Shadow public Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> levels;
    @Shadow @Final public static org.slf4j.Logger LOGGER;
    @Shadow public abstract boolean isSpawningMonsters();
    @Shadow public abstract boolean isSpawningAnimals();
    @Shadow private int tickCount;
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow public abstract boolean isStopped();
    // @formatter:on

    @Shadow public ServerConnectionListener connection;

    @Shadow public abstract ServerLevel overworld();

    @Shadow
    private static void setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
    }

    @Shadow protected abstract void setupDebugLevel(WorldData worldData);

    @Shadow public WorldData worldData;

    @Shadow public abstract Set<ResourceKey<net.minecraft.world.level.Level>> levelKeys();

    @Shadow public abstract void executeIfPossible(Runnable task);

    @Shadow @Final public Executor executor;
    @Shadow public abstract RegistryAccess.Frozen registryAccess();

    @Shadow @Final public ChunkProgressListenerFactory progressListenerFactory;
    @Shadow @Nullable public abstract ServerLevel getLevel(ResourceKey<net.minecraft.world.level.Level> dimension);

    @Shadow private long nextTickTimeNanos;
    @Mutable
    @Shadow @Final private static long OVERLOADED_THRESHOLD_NANOS;

    // CraftBukkit start
    public WorldLoader.DataLoadContext worldLoader;
    public CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public ConsoleReader reader;
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static int currentTick = (int) (System.currentTimeMillis() / 50);
    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
    public int autosavePeriod;
    private boolean forceTicks;
    public Commands vanillaCommandDispatcher;
    private boolean hasStopped = false;
    private final Object stopLock = new Object();
    public final double[] recentTps = new double[4];
    private static final int TPS = 20;
    private static final int TICK_TIME = 1000000000 / TPS;
    private static final int SAMPLE_INTERVAL = 20; // Paper
    // CraftBukkit end

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$loadOptions(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        OVERLOADED_THRESHOLD_NANOS  = 30L * TimeUtil.NANOSECONDS_PER_SECOND / 20L; // CraftBukkit
        String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
        OptionParser parser = new Main();
        try {
            options = parser.parse(arguments);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
        }
        Main.handleParser(parser, options);
        this.vanillaCommandDispatcher = worldStem.dataPackResources().getCommands();
        this.worldLoader = BukkitSnapshotCaptures.getDataLoadContext();
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", remap = false, ordinal = 0, shift = At.Shift.AFTER, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"))
    public void banner$unloadPlugins(CallbackInfo ci) {
        if (this.server != null) {
            this.server.disablePlugins();
        }
    }

    // Paper start - Further improve server tick loop
    private static final long SEC_IN_NANO = 1000000000;
    private static final long MAX_CATCHUP_BUFFER = TICK_TIME * TPS * 60L;
    private long lastTick = 0;
    private long catchupTime = 0;
    private static final java.math.BigDecimal TPS_BASE = new java.math.BigDecimal(1E9).multiply(new java.math.BigDecimal(SAMPLE_INTERVAL));
    // Paper End

    @Decorate(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private ServerStatus banner$initTickParam(MinecraftServer instance, @Local(allocate = "tickSection") long tickSection, @Local(allocate = "tickCount") long tickCount) throws Throwable {
        var serverStatus = (ServerStatus) DecorationOps.callsite().invoke(instance);
        Arrays.fill(recentTps, 20);
        tickSection = Util.getMillis();
        tickCount = 1;
        DecorationOps.blackhole().invoke(tickSection, tickCount);
        return serverStatus;
    }

    @Decorate(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startMetricsRecordingTick()V"))
    private void banner$updateTickParam(MinecraftServer instance, @Local(allocate = "tickSection") long tickSection, @Local(allocate = "tickCount") long tickCount) throws Throwable {
        if (tickCount++ % SAMPLE_INTERVAL == 0) {
            long curTime = Util.getMillis();
            double currentTps = 1E3 / (curTime - tickSection) * SAMPLE_INTERVAL;
            recentTps[0] = calcTps(recentTps[0], 0.92, currentTps); // 1/exp(5sec/1min)
            recentTps[1] = calcTps(recentTps[1], 0.9835, currentTps); // 1/exp(5sec/5min)
            recentTps[2] = calcTps(recentTps[2], 0.9945, currentTps); // 1/exp(5sec/15min)
            tickSection = curTime;
        }
        DecorationOps.blackhole().invoke(tickSection, tickCount);
        currentTick = (int) (System.currentTimeMillis() / 50);
        DecorationOps.callsite().invoke(instance);
    }

    @WrapWithCondition(method = "runServer", at = @At(value = "INVOKE", remap = false, target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private boolean banner$warnOnLoad(org.slf4j.Logger instance, String s, Object o1, Object o2) throws Throwable {
        return server.getWarnOnOverload();
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onServerExit()V"))
    private void banner$watchdogExit(CallbackInfo ci) {
        WatchdogThread.doStop();
    }

    private static double calcTps(double avg, double exp, double tps) {
        return (avg * exp) + (tps * (1 - exp));
    }

    @Inject(method = "stopServer", at = @At("HEAD"), cancellable = true)
    private void banner$stop(CallbackInfo ci) {
        synchronized(stopLock) {
            if (hasStopped) ci.cancel();
            hasStopped = true;
        }
    }

    /**
     * @author wdog5
     * @reason colorful motd
     */
    @Overwrite
    public String getMotd() {
        return ColorsAPI.of(BannerConfigUtil.motdFirstLine() + "\n" + BannerConfigUtil.motdSecondLine());
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;removeAll()V"))
    private void banner$stopThread(CallbackInfo ci) {
        try { Thread.sleep(100); } catch (InterruptedException ex) {} // CraftBukkit - SPIGOT-625 - give server at least a chance to send packets
    }

    @Inject(method = "loadLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareLevels(Lnet/minecraft/server/level/progress/ChunkProgressListener;)V",
            shift = At.Shift.AFTER))
    private void banner$loadLevel(CallbackInfo ci) {
        if (!BannerConfig.skipOtherWorldPreparing) {
            for (ServerLevel worldserver : ((MinecraftServer)(Object)this).getAllLevels()) {
                if (worldserver != overworld()) {
                    if (banner$isNether(worldserver) && Bukkit.getAllowNether()) {
                        banner$prepareWorld(worldserver);
                    }else if (banner$isEnd(worldserver) && this.server.getAllowEnd()) {
                        banner$prepareWorld(worldserver);
                    }
                    if (banner$isNotNetherAndEnd(worldserver)) {
                        banner$prepareWorld(worldserver);
                    }
                }
            }
        }
    }

    private boolean banner$isNotNetherAndEnd(ServerLevel worldserver) {
        return !banner$isNether(worldserver) && !banner$isEnd(worldserver);
    }

    private boolean banner$isNether(ServerLevel worldserver) {
        return worldserver == this.getLevel(net.minecraft.world.level.Level.NETHER);
    }

    private boolean banner$isEnd(ServerLevel worldserver) {
        return worldserver == this.getLevel(net.minecraft.world.level.Level.END);
    }

    private void banner$prepareWorld(ServerLevel worldserver) {
        this.prepareLevels(worldserver.getChunkSource().chunkMap.progressListener, worldserver);
        worldserver.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API
        this.server.getPluginManager().callEvent(new WorldLoadEvent(worldserver.getWorld()));
    }

    @Inject(method = "loadLevel", at = @At("RETURN"))
    public void banner$enablePlugins(CallbackInfo ci) {
        this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
        this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        this.connection.acceptConnections();
    }

    @Inject(method = "createLevels", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;)V",
            ordinal = 0))
    private void banner$registerEnv(ChunkProgressListener p_240787_1_, CallbackInfo ci) {
        BukkitRegistry.registerEnvironments(this.registryAccess().registryOrThrow(Registries.LEVEL_STEM));
    }

    @Inject(method = "createLevels", at = @At(value = "INVOKE", remap = false,
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void banner$worldInit(ChunkProgressListener listener, CallbackInfo ci,
                                  @com.llamalad7.mixinextras.sugar.Local ServerLevel serverLevel) {
        banner$initLevel(serverLevel);
    }

    @Redirect(method = "createLevels",
            at = @At(value = "NEW", args = "class=net/minecraft/server/level/ServerLevel", ordinal = 1))
    private ServerLevel banner$resetListener(MinecraftServer server, Executor dispatcher,
                                             LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                             ServerLevelData serverLevelData, ResourceKey dimension,
                                             LevelStem levelStem, ChunkProgressListener progressListener,
                                             boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime,
                                             RandomSequences randomSequences) {
        ChunkProgressListener listener = this.progressListenerFactory.create(11);
        return new ServerLevel(server, dispatcher, levelStorageAccess, serverLevelData,
                dimension, levelStem, listener, isDebug, biomeZoomSeed, customSpawners, tickTime, randomSequences);
    }

    @Inject(method = "createLevels",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1)
    )
    private void banner$initWorld(ChunkProgressListener chunkProgressListener, CallbackInfo ci,
                                  @com.llamalad7.mixinextras.sugar.Local WorldOptions worldOptions,
                                  @com.llamalad7.mixinextras.sugar.Local DerivedLevelData derivedLevelData,
                                  @com.llamalad7.mixinextras.sugar.Local(ordinal = 1) ServerLevel serverLevel2) {
        banner$initLevel(serverLevel2);
        banner$initializedLevel(serverLevel2, derivedLevelData, worldData, worldOptions);
    }

    @Inject(method = "getServerModName", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void banner$setServerModName(CallbackInfoReturnable<String> cir) {
        if (this.server != null) {
            cir.setReturnValue(server.getName());
        }
    }

    @Override
    public boolean hasStopped() {
        synchronized (stopLock) {
            return hasStopped;
        }
    }

    @Override
    public void banner$setServer(CraftServer server) {
        this.server = server;
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static RegistryAccess getDefaultRegistryAccess() {
        return CraftRegistry.getMinecraftRegistry();
    }

    @Override
    public void addLevel(ServerLevel level) {
        ServerWorldEvents.LOAD.invoker().onWorldLoad(((MinecraftServer) (Object) this), level);
        this.levels.put(level.dimension(), level);
    }

    @Override
    public void removeLevel(ServerLevel level) {
        ServerWorldEvents.UNLOAD.invoker().onWorldUnload(((MinecraftServer) (Object) this), level); // Banner
        this.levels.remove(level.dimension());
    }

    @Inject(method = "setInitialSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;getGenerator()Lnet/minecraft/world/level/chunk/ChunkGenerator;", shift = At.Shift.BEFORE), cancellable = true)
    private static void banner$spawnInit(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug, CallbackInfo ci) {
        // CraftBukkit start
        if (level.bridge$generator() != null) {
            Random rand = new Random(level.getSeed());
            org.bukkit.Location spawn = level.bridge$generator().getFixedSpawnLocation(level.getWorld(), rand);

            if (spawn != null) {
                if (spawn.getWorld() != level.getWorld()) {
                    throw new IllegalStateException("Cannot set spawn point for " + levelData.getLevelName() + " to be in another world (" + spawn.getWorld().getName() + ")");
                } else {
                    levelData.setSpawn(new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()), spawn.getYaw());
                    ci.cancel();
                }
            }
        }
    }

    @Override
    public void initWorld(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
        banner$initLevel(serverWorld);
        WorldBorder worldborder = serverWorld.getWorldBorder();
        worldborder.applySettings(worldInfo.getWorldBorder());
        banner$initializedLevel(serverWorld, worldInfo, saveData, worldOptions);
    }

    private void banner$initLevel(ServerLevel serverWorld) {
        this.server.scoreboardManager = new CraftScoreboardManager((MinecraftServer) (Object) this, serverWorld.getScoreboard());

        if (serverWorld.bridge$generator() != null) {
            serverWorld.getWorld().getPopulators().addAll(
                    serverWorld.bridge$generator().getDefaultPopulators(
                            serverWorld.getWorld()));
        }
        Bukkit.getPluginManager().callEvent(new WorldInitEvent(serverWorld.getWorld()));
    }

    private void banner$initializedLevel(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
        boolean flag = saveData.isDebugWorld();

        if (!worldInfo.isInitialized()) {
            try {
                setInitialSpawn(serverWorld, worldInfo, worldOptions.generateBonusChest(), flag);
                worldInfo.setInitialized(true);
                if (flag) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverWorld.fillReportDetails(crashreport);
                } catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashreport);
            }
            worldInfo.setInitialized(true);
        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public final void prepareLevels(ChunkProgressListener listener) {
        ServerLevel worldserver = this.overworld();
        this.forceTicks = true;
        // CraftBukkit end

        LOGGER.info(I18n.as("server.region.prepare"), worldserver.dimension().location());
        BlockPos blockposition = worldserver.getSharedSpawnPos();

        listener.updateSpawnPos(new ChunkPos(blockposition));
        ServerChunkCache chunkproviderserver = worldserver.getChunkSource();

        this.nextTickTimeNanos = Util.getNanos();
        // CraftBukkit start
        if (worldserver.getWorld().getKeepSpawnInMemory()) {
            chunkproviderserver.addRegionTicket(TicketType.START, new ChunkPos(blockposition), 11, Unit.INSTANCE);

            while (chunkproviderserver.getTickingGenerated() != 441) {
                // this.nextTickTime = SystemUtils.getMillis() + 10L;
                this.executeModerately();
            }
        }

        // this.nextTickTime = SystemUtils.getMillis() + 10L;
        this.executeModerately();
        // Iterator iterator = this.levels.values().iterator();
        Iterator var5 = this.levels.values().iterator();

        while(true) {
            ServerLevel serverLevel2;
            ForcedChunksSavedData forcedChunksSavedData;
            do {
                if (!var5.hasNext()) {
                    // CraftBukkit start
                    // this.nextTickTime = SystemUtils.getMillis() + 10L;
                    this.executeModerately();
                    // CraftBukkit end
                    listener.stop();
                    // CraftBukkit start
                    // this.updateMobSpawningFlags();
                    worldserver.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());

                    this.forceTicks = false;
                    // CraftBukkit end
                    return;
                }

                serverLevel2 = (ServerLevel)var5.next();
                forcedChunksSavedData = (ForcedChunksSavedData)serverLevel2.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
            } while(forcedChunksSavedData == null);

            LongIterator longIterator = forcedChunksSavedData.getChunks().iterator();

            while(longIterator.hasNext()) {
                long l = longIterator.nextLong();
                ChunkPos chunkPos = new ChunkPos(l);
                serverLevel2.getChunkSource().updateChunkForced(chunkPos, true);
            }

            worldserver.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API // Banner - tick to sync chunks
            Bukkit.getPluginManager().callEvent(new WorldLoadEvent(serverLevel2.getWorld()));
        }
    }

    @Override
    public void prepareLevels(ChunkProgressListener listener, ServerLevel worldserver) {
        ServerWorldEvents.LOAD.invoker().onWorldLoad(((MinecraftServer) (Object) this), worldserver);// Banner
        // WorldServer worldserver = this.overworld();
        this.forceTicks = true;
        // CraftBukkit end

        LOGGER.info(I18n.as("server.region.prepare"), worldserver.dimension().location());
        BlockPos blockposition = worldserver.getSharedSpawnPos();

        listener.updateSpawnPos(new ChunkPos(blockposition));
        ServerChunkCache chunkproviderserver = worldserver.getChunkSource();

        this.nextTickTimeNanos = Util.getNanos();
        // CraftBukkit start
        if (worldserver.getWorld().getKeepSpawnInMemory()) {
            chunkproviderserver.addRegionTicket(TicketType.START, new ChunkPos(blockposition), 11, Unit.INSTANCE);

            while (chunkproviderserver.getTickingGenerated() != 441) {
                // this.nextTickTime = SystemUtils.getMillis() + 10L;
                this.executeModerately();
            }
        }

        // this.nextTickTime = SystemUtils.getMillis() + 10L;
        this.executeModerately();
        // Iterator iterator = this.levels.values().iterator();

        if (true) {
            ServerLevel worldserver1 = worldserver;
            // CraftBukkit end
            ForcedChunksSavedData forcedchunk = (ForcedChunksSavedData) worldserver1.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");

            if (forcedchunk != null) {
                LongIterator longiterator = forcedchunk.getChunks().iterator();

                while (longiterator.hasNext()) {
                    long i = longiterator.nextLong();
                    ChunkPos chunkcoordintpair = new ChunkPos(i);

                    worldserver1.getChunkSource().updateChunkForced(chunkcoordintpair, true);
                }
            }
        }

        // CraftBukkit start
        // this.nextTickTime = SystemUtils.getMillis() + 10L;
        this.executeModerately();
        // CraftBukkit end
        listener.stop();
        // CraftBukkit start
        // this.updateMobSpawningFlags();
        worldserver.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());

        this.forceTicks = false;
        // CraftBukkit end
    }

    @Override
    public void executeModerately() {
        this.runAllTasks();
        this.bridge$drainQueuedTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Inject(method = "haveTime", cancellable = true, at = @At("HEAD"))
    private void banner$forceAheadOfTime(CallbackInfoReturnable<Boolean> cir) {
        if (this.forceTicks) cir.setReturnValue(true);
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void banner$watchdogThreadStart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        org.spigotmc.WatchdogThread.tick(); // Spigot
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void banner$processStart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        BukkitFieldHooks.setCurrentTick((int) (System.currentTimeMillis() / 50));
        server.getScheduler().mainThreadHeartbeat(this.tickCount);
        this.bridge$drainQueuedTasks();
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                    ordinal = 0))
    private void banner$mainThreadHeartbeat0(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount); // CraftBukkit
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerFunctionManager;tick()V"))
    private void banner$mainThreadHeartbeat1(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount); // CraftBukkit
    }

    @Inject(method = "tickChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    ordinal = 0))
    private void banner$mainThreadHeartbeat2(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.server.getScheduler().mainThreadHeartbeat(this.tickCount); // CraftBukkit
    }

    @Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private void banner$checkHeart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        // CraftBukkit start
        // Run tasks that are waiting on processing
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }

        // Send time updates to everyone, it will get the right time from the world the player is in.
        if (this.tickCount % 20 == 0) {
            for (int i = 0; i < this.getPlayerList().players.size(); ++i) {
                ServerPlayer entityplayer = (ServerPlayer) this.getPlayerList().players.get(i);
                entityplayer.connection.send(new ClientboundSetTimePacket(entityplayer.level().getGameTime(), entityplayer.getPlayerTime(), entityplayer.level().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))); // Add support for per player time
            }
        }
    }

    // CraftBukkit start
    public final java.util.concurrent.ExecutorService chatExecutor = java.util.concurrent.Executors.newCachedThreadPool(
            new com.google.common.util.concurrent.ThreadFactoryBuilder().setDaemon(true).setNameFormat("Async Chat Thread - #%d").build());

    @Inject(method = "method_29440", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/packs/repository/PackRepository;setSelected(Ljava/util/Collection;)V"))
    private void banner$syncCommands(Collection collection, MinecraftServer.ReloadableResources reloadableResources,
                                     CallbackInfo ci) {
        this.server.syncCommands(); // SPIGOT-5884: Lost on reload
    }

    @ModifyConstant(method = "spin", constant = @Constant(intValue = 8))
    private static int banner$configurePriority(int constant) {
        return BannerConfigUtil.serverThread();
    }

    // Banner start
    @Override
    public WorldLoader.DataLoadContext bridge$worldLoader() {
        return worldLoader;
    }

    @Override
    public CraftServer bridge$server() {
        return server;
    }

    @Override
    public OptionSet bridge$options() {
        return options;
    }

    @Override
    public ConsoleCommandSender bridge$console() {
        return console;
    }

    @Override
    public ConsoleReader bridge$reader() {
        return reader;
    }

    @Override
    public boolean bridge$forceTicks() {
        return forceTicks;
    }

    @Override
    public boolean isDebugging() {
        return false;
    }

    @Override
    public void banner$setConsole(ConsoleCommandSender console) {
        this.console = console;
    }

    // Banner end

    @Override
    public void bridge$drainQueuedTasks() {
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }
    }

    @Override
    public void bridge$queuedProcess(Runnable runnable) {
        processQueue.add(runnable);
    }

    @Override
    public Queue<Runnable> bridge$processQueue() {
        return processQueue;
    }

    @Override
    public void banner$setProcessQueue(Queue<Runnable> processQueue) {
        this.processQueue = processQueue;
    }


    @Override
    public Commands bridge$getVanillaCommands() {
        return this.vanillaCommandDispatcher;
    }

    @Override
    public java.util.concurrent.ExecutorService bridge$chatExecutor() {
        return chatExecutor;
    }

    @Override
    public boolean isSameThread() {
        return super.isSameThread() || this.isStopped(); // CraftBukkit - MC-142590
    }

    @Override
    public double[] getTPS() {
        return new double[] {
        };
    }

    @Unique
    private ServerLinks serverLinksVanilla = ServerLinks.EMPTY;

    @Override
    public void setServerLinks(ServerLinks serverLinks) {
        this.serverLinksVanilla = serverLinks;
    }

    @ModifyReturnValue(method = "serverLinks", at = @At("RETURN"))
    private ServerLinks banner$resetServerLinks(ServerLinks original) {
        return serverLinksVanilla;
    }

    @Override
    public int bridge$autosavePeriod() {
        return autosavePeriod;
    }

    @Override
    public void banner$setAutosavePeriod(int autosavePeriod) {
        this.autosavePeriod = autosavePeriod;
    }
}
