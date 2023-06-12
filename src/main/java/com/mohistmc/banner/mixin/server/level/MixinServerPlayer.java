package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.bukkit.DoubleChestInventory;
import com.mohistmc.banner.injection.server.level.InjectionServerPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.level.ChunkPos;
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
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorldBorder;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftScoreboardManager;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
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

    @Shadow @Nullable private Entity camera;
    @Shadow private int containerCounter;
    @Shadow public ServerGamePacketListenerImpl connection;

    @Shadow public abstract void initMenu(AbstractContainerMenu abstractContainerMenu);

    @Shadow public abstract void nextContainerCounter();

    @Shadow public abstract void initInventoryMenu();

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

    @Redirect(method = "doTick", at = @At(value = "NEW", args = "class=net/minecraft/network/protocol/game/ClientboundSetHealthPacket"))
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
        this.setRemainingFireTicks(0);
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

    @Inject(method = "doCloseContainer", at = @At("HEAD"))
    private void banner$invClose(CallbackInfo ci) {
        if (this.containerMenu != this.inventoryMenu) {
            var old = BukkitCaptures.getContainerOwner();
            BukkitCaptures.captureContainerOwner((ServerPlayer) (Object) this);
            CraftEventFactory.handleInventoryCloseEvent((ServerPlayer) (Object) this);
            BukkitCaptures.captureContainerOwner(old);
        }
    }

    @Inject(method = "setPlayerInput", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setShiftKeyDown(Z)V"))
    private void banner$toggleSneak(float strafe, float forward, boolean jumping, boolean sneaking, CallbackInfo ci) {
        if (sneaking != this.isShiftKeyDown()) {
            PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getBukkitEntity(), sneaking);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "awardStat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;forAllObjectives(Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    private void banner$addStats(Scoreboard instance, ObjectiveCriteria criteria, String scoreboardName, Consumer<Score> points) {
        ((CraftScoreboardManager) Bukkit.getScoreboardManager()).getScoreboardScores(criteria, scoreboardName, points);
    }

    @Redirect(method = "resetStat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;forAllObjectives(Lnet/minecraft/world/scores/criteria/ObjectiveCriteria;Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    private void banner$takeStats(Scoreboard instance, ObjectiveCriteria criteria, String scoreboardName, Consumer<Score> points) {
        ((CraftScoreboardManager) Bukkit.getScoreboardManager()).getScoreboardScores(criteria, scoreboardName, points);
    }

    @Inject(method = "resetSentInfo", at = @At("HEAD"))
    private void banner$setExpUpdate(CallbackInfo ci) {
        this.lastSentExp = -1;
    }

    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void banner$settingChange(ServerboundClientInformationPacket packetIn, CallbackInfo ci) {
        if (this.getMainArm() != packetIn.mainHand()) {
            PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent(this.getBukkitEntity(), (this.getMainArm() == HumanoidArm.LEFT) ? MainHand.LEFT : MainHand.RIGHT);
            Bukkit.getPluginManager().callEvent(event);
        }
        if (!this.locale.equals(packetIn.language())) {
            PlayerLocaleChangeEvent event2 = new PlayerLocaleChangeEvent(this.getBukkitEntity(), packetIn.language());
            Bukkit.getPluginManager().callEvent(event2);
        }
        this.locale = packetIn.language();
        this.clientViewDistance = packetIn.viewDistance();
    }

    @Inject(method = "trackChunk",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            shift = At.Shift.AFTER))
    private void banner$chunkLoad(ChunkPos chunkPos, Packet<?> packet, CallbackInfo ci) {
        // Paper start
        if(io.papermc.paper.event.packet.PlayerChunkLoadEvent.getHandlerList().getRegisteredListeners().length > 0){
            new io.papermc.paper.event.packet.PlayerChunkLoadEvent(this.getBukkitEntity().getWorld().getChunkAt(chunkPos.x, chunkPos.z), this.getBukkitEntity()).callEvent();
        }
        // Paper end
    }

    @Inject(method = "untrackChunk",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            shift = At.Shift.AFTER))
    private void banner$chunkUnload(ChunkPos chunkPos, CallbackInfo ci) {
        // Paper start
        if(io.papermc.paper.event.packet.PlayerChunkUnloadEvent.getHandlerList().getRegisteredListeners().length > 0){
            new io.papermc.paper.event.packet.PlayerChunkUnloadEvent(this.getBukkitEntity().getWorld().getChunkAt(chunkPos.x, chunkPos.z), this.getBukkitEntity()).callEvent();
        }
        // Paper end
    }

    @Inject(method = "setCamera",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;level()Lnet/minecraft/world/level/Level;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$spectorEvent(Entity entityToSpectate, CallbackInfo ci, Entity entity) {
        // Paper start - Add PlayerStartSpectatingEntityEvent and PlayerStopSpectatingEntity Event
        if (this.camera == this) {
            com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent playerStopSpectatingEntityEvent = new com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity());
            if (!playerStopSpectatingEntityEvent.callEvent()) {
                ci.cancel();
            }
        } else {
            com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent playerStartSpectatingEntityEvent = new com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), entity.getBukkitEntity());
            if (!playerStartSpectatingEntityEvent.callEvent()) {
                ci.cancel();
            }
        }
        // Paper end
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
    public int nextContainerCounterInt() {
        this.containerCounter = this.containerCounter % 100 + 1;
        return containerCounter; // CraftBukkit
    }

    private AtomicReference<AbstractContainerMenu> banner$containerMenu = new AtomicReference<>();

    @Inject(method = "openMenu", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$invOpen(MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> cir) {
        AbstractContainerMenu banner$container = menuProvider.createMenu(this.containerCounter, this.getInventory(), this);
        banner$containerMenu.set(banner$container);
        if (banner$container != null) {
            banner$container.setTitle(menuProvider.getDisplayName());
            boolean cancelled = false;
            BukkitCaptures.captureContainerOwner((ServerPlayer) (Object) this);
            banner$container = CraftEventFactory.callInventoryOpenEvent((ServerPlayer) (Object) this, banner$container, cancelled);
            BukkitCaptures.resetContainerOwner();
            if (banner$container == null && !cancelled) {
                if (menuProvider instanceof Container) {
                    ((Container) menuProvider).stopOpen((ServerPlayer) (Object) this);
                } else if (menuProvider instanceof DoubleChestInventory) {
                    ((DoubleChestInventory) menuProvider).inventorylargechest.stopOpen(this);
                }
                cir.setReturnValue(OptionalInt.empty());
            }
        }
    }

    @Redirect(method = "openMenu", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private AbstractContainerMenu banner$resetMenu(MenuProvider instance, int i, Inventory inventory, Player player) {
        return banner$containerMenu.get();
    }

    @Redirect(method = "openMenu", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/MenuProvider;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    private Component banner$sendData(MenuProvider instance) {
        return banner$containerMenu.get().getTitle();
    }

    private AtomicReference<HorseInventoryMenu> banner$horseMenu = new AtomicReference<>();

    @Inject(method = "openHorseInventory", at = @At("HEAD"), cancellable = true)
    private void banner$menuEvent(AbstractHorse abstractHorse, Container container, CallbackInfo ci) {
        // CraftBukkit start - Inventory open hook
        this.nextContainerCounter();
        AbstractContainerMenu banner$container = new HorseInventoryMenu(this.containerCounter, this.getInventory(), container, abstractHorse);
        banner$horseMenu.set((HorseInventoryMenu) banner$container);
        banner$container.setTitle(abstractHorse.getDisplayName());
        banner$container = CraftEventFactory.callInventoryOpenEvent(((ServerPlayer) (Object) this), banner$container);
        if (banner$container == null) {
            container.stopOpen(this);
            ci.cancel();
        }
    }

    @Redirect(method = "openHorseInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;nextContainerCounter()V"))
    private void banner$cancelNext(ServerPlayer instance) {}

    @Redirect(method = "openHorseInventory", at = @At(value = "NEW", args = "class=net/minecraft/world/inventory/HorseInventoryMenu"))
    private HorseInventoryMenu banner$resetHorseMenu(int i, Inventory inventory, Container container, AbstractHorse abstractHorse) {
        return banner$horseMenu.get();
    }

    @Inject(method = "closeContainer", at = @At("HEAD"))
    private void banner$closeMenu(CallbackInfo ci) {
        CraftEventFactory.handleInventoryCloseEvent(this); // CraftBukkit
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
