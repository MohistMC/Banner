package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.injection.server.network.InjectionServerGamePacketListenerImpl;
import com.mohistmc.banner.util.ServerUtils;
import com.mojang.brigadier.ParseResults;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R3.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.slf4j.Logger;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.UnaryOperator;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl implements InjectionServerGamePacketListenerImpl {

    // @formatter:off
    @Shadow @Final private MinecraftServer server;
    @Shadow public ServerPlayer player;
    @Shadow @Final public Connection connection;
    @Shadow public abstract void onDisconnect(Component reason);
    @Shadow private Entity lastVehicle;
    @Shadow private double vehicleFirstGoodX;
    @Shadow private double vehicleFirstGoodY;
    @Shadow private double vehicleFirstGoodZ;
    @Shadow protected abstract boolean isSingleplayerOwner();
    @Shadow private double vehicleLastGoodX;
    @Shadow private double vehicleLastGoodY;
    @Shadow private double vehicleLastGoodZ;
    @Shadow private boolean clientVehicleIsFloating;
    @Shadow private int receivedMovePacketCount;
    @Shadow private int knownMovePacketCount;
    @Shadow private Vec3 awaitingPositionFromClient;
    @Shadow private int tickCount;
    @Shadow public abstract void resetPosition();
    @Shadow private int awaitingTeleportTime;
    @Shadow public abstract void teleport(double x, double y, double z, float yaw, float pitch);
    @Shadow private double firstGoodX;
    @Shadow private double firstGoodY;
    @Shadow private double firstGoodZ;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private double lastGoodX;
    @Shadow private double lastGoodY;
    @Shadow private double lastGoodZ;
    @Shadow private boolean clientIsFloating;
    @Shadow private int awaitingTeleport;
    @Shadow public abstract void send(Packet<?> packetIn);
    @Shadow private int chatSpamTickCount;
    @Shadow private int dropSpamTickCount;
    @Shadow protected abstract boolean noBlocksAround(Entity p_241162_1_);
    @Shadow protected abstract boolean isPlayerCollidingWithAnythingNew(LevelReader p_241163_1_, AABB p_241163_2_);
    @Shadow private static double clampHorizontal(double p_143610_) { return 0; }
    @Shadow private static double clampVertical(double p_143654_) { return 0; }
    @Shadow private static boolean containsInvalidValues(double p_143664_, double p_143665_, double p_143666_, float p_143667_, float p_143668_) { return false; }
    @Shadow @Final @Mutable private FutureChain chatMessageChain;
    @Shadow protected abstract void updateBookPages(List<FilteredText> p_143635_, UnaryOperator<String> p_143636_, ItemStack p_143637_);
    @Shadow public abstract void ackBlockChangesUpTo(int p_215202_);
    @Shadow private static boolean isChatMessageIllegal(String p_215215_) { return false; }
    @Shadow protected abstract CompletableFuture<FilteredText> filterTextPacket(String p_243213_);
    @Shadow protected abstract ParseResults<CommandSourceStack> parseCommand(String p_242938_);
    @Shadow protected abstract void detectRateSpam();
    @Shadow protected abstract Optional<LastSeenMessages> tryHandleChat(String p_251364_, Instant p_248959_, LastSeenMessages.Update p_249613_);
    @Shadow protected abstract PlayerChatMessage getSignedMessage(ServerboundChatPacket p_251061_, LastSeenMessages p_250566_) throws SignedMessageChain.DecodeException;
    @Shadow protected abstract void handleMessageDecodeFailure(SignedMessageChain.DecodeException p_252068_);
    @Shadow protected abstract Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandPacket p_249441_, SignableCommand<?> p_250039_, LastSeenMessages p_249207_) throws SignedMessageChain.DecodeException;
    // @formatter:on

    private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 6 * 6;
    private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 7 * 7;
    private CraftServer cserver;
    public boolean processedDisconnect;
    private int allowedPlayerTicks;
    private int dropCount;
    private int lastTick;
    private volatile int lastBookTick;
    private int lastDropTick;

    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private float lastPitch;
    private float lastYaw;
    private boolean justTeleported;
    private boolean hasMoved;
    private int limitedPackets;
    private long lastLimitedPacket = -1;

    @Override
    public CraftPlayer getCraftPlayer() {
        return (this.player == null) ? null : this.player.getBukkitEntity();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(MinecraftServer server, Connection networkManagerIn, ServerPlayer playerIn, CallbackInfo ci) {
        this.cserver = ((CraftServer) Bukkit.getServer());
        allowedPlayerTicks = 1;
        dropCount = 0;
        lastPosX = Double.MAX_VALUE;
        lastPosY = Double.MAX_VALUE;
        lastPosZ = Double.MAX_VALUE;
        lastPitch = Float.MAX_VALUE;
        lastYaw = Float.MAX_VALUE;
        justTeleported = false;
        this.chatMessageChain = new FutureChain(server.bridge$chatExecutor());
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void disconnect(Component textComponent) {
        this.disconnect(CraftChatMessage.fromComponent(textComponent));
    }

    @Override
    public void disconnect(String s) {
        if (this.processedDisconnect) {
            return;
        }
        if (!this.cserver.isPrimaryThread()) {
            Waitable<?> waitable = new Waitable<>() {
                @Override
                protected Object evaluate() {
                    disconnect(s);
                    return null;
                }
            };

            this.server.bridge$queuedProcess(waitable);

            try {
                waitable.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        String leaveMessage = ChatFormatting.YELLOW + this.player.getScoreboardName() + " left the game.";
        PlayerKickEvent event = new PlayerKickEvent(getCraftPlayer(), s, leaveMessage);
        if (this.cserver.getServer().isRunning()) {
            this.cserver.getPluginManager().callEvent(event);
        }
        if (event.isCancelled()) {
            return;
        }
        BukkitCaptures.captureQuitMessage(event.getLeaveMessage());
        Component textComponent = CraftChatMessage.fromString(event.getReason(), true)[0];
        this.connection.send(new ClientboundDisconnectPacket(textComponent), PacketSendListener.thenRun(() -> this.connection.disconnect(textComponent)));
        this.onDisconnect(textComponent);
        this.connection.setReadOnly();
        this.server.executeBlocking(this.connection::handleDisconnection);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handleMoveVehicle(final ServerboundMoveVehiclePacket packetplayinvehiclemove) {
        PacketUtils.ensureRunningOnSameThread(packetplayinvehiclemove, (ServerGamePacketListenerImpl) (Object) this, this.player.serverLevel());
        if (containsInvalidValues(packetplayinvehiclemove.getX(), packetplayinvehiclemove.getY(), packetplayinvehiclemove.getZ(), packetplayinvehiclemove.getYRot(), packetplayinvehiclemove.getXRot())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            Entity entity = this.player.getRootVehicle();
            if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
                ServerLevel worldserver = this.player.serverLevel();
                double d0 = entity.getX();
                double d2 = entity.getY();
                double d3 = entity.getZ();
                double d4 = packetplayinvehiclemove.getX();
                double d5 = packetplayinvehiclemove.getY();
                double d6 = packetplayinvehiclemove.getZ();
                float f = packetplayinvehiclemove.getYRot();
                float f2 = packetplayinvehiclemove.getXRot();
                double d7 = d4 - this.vehicleFirstGoodX;
                double d8 = d5 - this.vehicleFirstGoodY;
                double d9 = d6 - this.vehicleFirstGoodZ;
                double d10 = entity.getDeltaMovement().lengthSqr();
                double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                this.allowedPlayerTicks += (int) (System.currentTimeMillis() / 50L - this.lastTick);
                this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                this.lastTick = (int) (System.currentTimeMillis() / 50L);
                ++this.receivedMovePacketCount;
                int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                if (i > Math.max(this.allowedPlayerTicks, 5)) {
                    LOGGER.debug(this.player.getScoreboardName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                    i = 1;
                }
                if (d11 > 0.0) {
                    --this.allowedPlayerTicks;
                } else {
                    this.allowedPlayerTicks = 20;
                }
                double speed;
                if (this.player.getAbilities().flying) {
                    speed = this.player.getAbilities().flyingSpeed * 20.0f;
                } else {
                    speed = this.player.getAbilities().walkingSpeed * 10.0f;
                }
                speed *= 2.0;
                if (d11 - d10 > Math.max(100.0, Math.pow(10.0f * i * speed, 2.0)) && !this.isSingleplayerOwner()) {
                    LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), this.player.getName().getString(), d7, d8, d9);
                    this.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }
                boolean flag = worldserver.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
                d7 = d4 - this.vehicleLastGoodX;
                d8 = d5 - this.vehicleLastGoodY - 1.0E-6;
                d9 = d6 - this.vehicleLastGoodZ;
                entity.move(MoverType.PLAYER, new Vec3(d7, d8, d9));
                double d12 = d8;
                d7 = d4 - entity.getX();
                d8 = d5 - entity.getY();
                if (d8 > -0.5 || d8 < 0.5) {
                    d8 = 0.0;
                }
                d9 = d6 - entity.getZ();
                d11 = d7 * d7 + d8 * d8 + d9 * d9;
                boolean flag2 = false;
                if (d11 > SpigotConfig.movedWronglyThreshold) {
                    flag2 = true;
                    LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d11));
                }
                Location curPos = this.getCraftPlayer().getLocation();
                entity.absMoveTo(d4, d5, d6, f, f2);
                this.player.absMoveTo(d4, d5, d6, this.player.getYRot(), this.player.getXRot());
                boolean flag3 = worldserver.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
                if (flag && (flag2 || !flag3)) {
                    entity.absMoveTo(d0, d2, d3, f, f2);
                    this.player.absMoveTo(d0, d2, d3, this.player.getYRot(), this.player.getXRot());
                    this.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }
                Player player = this.getCraftPlayer();
                if (!hasMoved) {
                    lastPosX = curPos.getX();
                    lastPosY = curPos.getY();
                    lastPosZ = curPos.getZ();
                    lastYaw = curPos.getYaw();
                    lastPitch = curPos.getPitch();
                    hasMoved = true;
                }
                Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
                Location to = player.getLocation().clone();
                to.setX(packetplayinvehiclemove.getX());
                to.setY(packetplayinvehiclemove.getY());
                to.setZ(packetplayinvehiclemove.getZ());
                to.setYaw(packetplayinvehiclemove.getYRot());
                to.setPitch(packetplayinvehiclemove.getXRot());
                double delta = Math.pow(this.lastPosX - to.getX(), 2.0) + Math.pow(this.lastPosY - to.getY(), 2.0) + Math.pow(this.lastPosZ - to.getZ(), 2.0);
                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());
                if ((delta > 0.00390625 || deltaAngle > 10.0f) && ! this.player.isImmobile()) {
                    this.lastPosX = to.getX();
                    this.lastPosY = to.getY();
                    this.lastPosZ = to.getZ();
                    this.lastYaw = to.getYaw();
                    this.lastPitch = to.getPitch();
                    if (true) {
                        Location oldTo = to.clone();
                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                        this.cserver.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            this.teleport(from);
                            return;
                        }
                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                            this.player.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            return;
                        }
                        if (!from.equals(this.getCraftPlayer().getLocation()) && this.justTeleported) {
                            this.justTeleported = false;
                            return;
                        }
                    }
                }
                this.player.serverLevel().getChunkSource().move(this.player);
                this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d2, this.player.getZ() - d3);
                this.clientVehicleIsFloating = d12 >= -0.03125 && !this.server.isFlightAllowed() && this.noBlocksAround(entity);
                this.vehicleLastGoodX = entity.getX();
                this.vehicleLastGoodY = entity.getY();
                this.vehicleLastGoodZ = entity.getZ();
            }
        }
    }

    @Inject(method = "handleAcceptTeleportPacket",
            at = @At(value = "FIELD", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;awaitingPositionFromClient:Lnet/minecraft/world/phys/Vec3;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension()Z")))
    private void banner$updateLoc(ServerboundAcceptTeleportationPacket packetIn, CallbackInfo ci) {
        if (this.player.bridge$valid()) {
            this.player.serverLevel().getChunkSource().move(this.player);
        }
    }

    @Inject(method = "handleAcceptTeleportPacket", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;awaitingTeleport:I"))
    private void banner$confirm(ServerboundAcceptTeleportationPacket packetIn, CallbackInfo ci) {
        if (this.awaitingPositionFromClient == null) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSelectTrade", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;setSelectionHint(I)V"))
    private void banner$tradeSelect(ServerboundSelectTradePacket packet, CallbackInfo ci, int i, MerchantMenu merchantMenu) {
        var event = CraftEventFactory.callTradeSelectEvent(this.player, i, (MerchantMenu) merchantMenu);
        if (event.isCancelled()) {
            this.player.getBukkitEntity().updateInventory();
            ci.cancel();
        }
    }

    @Inject(method = "handleEditBook", at = @At("HEAD"))
    private void banner$editBookSpam(ServerboundEditBookPacket packetIn, CallbackInfo ci) {
        if (this.lastBookTick == 0) {
            this.lastBookTick = ServerUtils.currentTick - 20;
        }
        if (this.lastBookTick + 20 > ServerUtils.currentTick) {
            this.disconnect("Book edited too quickly!");
            return;
        }
        this.lastBookTick = ServerUtils.currentTick;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private void updateBookContents(List<FilteredText> list, int slot) {
        ItemStack old = this.player.getInventory().getItem(slot);
        if (old.is(Items.WRITABLE_BOOK)) {
            ItemStack itemstack = old.copy();
            this.updateBookPages(list, UnaryOperator.identity(), itemstack);
            CraftEventFactory.handleEditBookEvent(player, slot, old, itemstack);
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private void signBook(FilteredText text, List<FilteredText> list, int slot) {
        ItemStack old = this.player.getInventory().getItem(slot);
        if (old.is(Items.WRITABLE_BOOK)) {
            ItemStack itemStack = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag compoundtag = old.getTag();
            if (compoundtag != null) {
                itemStack.setTag(compoundtag.copy());
            }

            itemStack.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
            if (this.player.isTextFilteringEnabled()) {
                itemStack.addTagElement("title", StringTag.valueOf(text.filteredOrEmpty()));
            } else {
                itemStack.addTagElement("filtered_title", StringTag.valueOf(text.filteredOrEmpty()));
                itemStack.addTagElement("title", StringTag.valueOf(text.raw()));
            }

            this.updateBookPages(list, (p_143659_) -> Component.Serializer.toJson(Component.literal(p_143659_)), itemStack);
            this.player.getInventory().setItem(slot, CraftEventFactory.handleEditBookEvent(this.player, slot, old, itemStack));
        }
    }


    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handleMovePlayer(ServerboundMovePlayerPacket packetplayinflying) {
        PacketUtils.ensureRunningOnSameThread(packetplayinflying, (ServerGamePacketListenerImpl) (Object) this, this.player.serverLevel());
        if (containsInvalidValues(packetplayinflying.getX(0.0D), packetplayinflying.getY(0.0D), packetplayinflying.getZ(0.0D), packetplayinflying.getYRot(0.0F), packetplayinflying.getXRot(0.0F))) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
        } else {
            ServerLevel worldserver = this.player.serverLevel();
            if (!this.player.wonGame && ! this.player.isImmobile()) {
                if (this.tickCount == 0) {
                    this.resetPosition();
                }
                if (this.awaitingPositionFromClient != null) {
                    if (this.tickCount - this.awaitingTeleportTime > 20) {
                        this.awaitingTeleportTime = this.tickCount;
                        this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
                    }
                    this.allowedPlayerTicks = 20;
                } else {
                    this.awaitingTeleportTime = this.tickCount;
                    double d0 = clampHorizontal(packetplayinflying.getX(this.player.getX()));
                    double d1 = clampVertical(packetplayinflying.getY(this.player.getY()));
                    double d2 = clampHorizontal(packetplayinflying.getZ(this.player.getZ()));
                    float f = Mth.wrapDegrees(packetplayinflying.getYRot(this.player.getYRot()));
                    float f1 = Mth.wrapDegrees(packetplayinflying.getXRot(this.player.getXRot()));

                    if (this.player.isPassenger()) {
                        this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        this.player.serverLevel().getChunkSource().move(this.player);
                        this.allowedPlayerTicks = 20; // CraftBukkit
                    } else {
                        // CraftBukkit - Make sure the move is valid but then reset it for plugins to modify
                        double prevX = player.getX();
                        double prevY = player.getY();
                        double prevZ = player.getZ();
                        float prevYaw = player.getYRot();
                        float prevPitch = player.getXRot();
                        // CraftBukkit end
                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = this.player.getY();
                        double d7 = d0 - this.firstGoodX;
                        double d8 = d1 - this.firstGoodY;
                        double d9 = d2 - this.firstGoodZ;
                        double d10 = this.player.getDeltaMovement().lengthSqr();
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;

                        if (this.player.isSleeping()) {
                            if (d11 > 1.0D) {
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }

                        } else {
                            ++this.receivedMovePacketCount;
                            int i = this.receivedMovePacketCount - this.knownMovePacketCount;

                            // CraftBukkit start - handle custom speeds and skipped ticks
                            this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                            this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                            this.lastTick = (int) (System.currentTimeMillis() / 50);

                            if (i > Math.max(this.allowedPlayerTicks, 5)) {
                                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), i);
                                i = 1;
                            }

                            if (packetplayinflying.hasRot || d11 > 0) {
                                allowedPlayerTicks -= 1;
                            } else {
                                allowedPlayerTicks = 20;
                            }
                            double speed;
                            if (player.getAbilities().flying) {
                                speed = player.getAbilities().flyingSpeed * 20f;
                            } else {
                                speed = player.getAbilities().walkingSpeed * 10f;
                            }

                            if (!this.player.isChangingDimension() && (!this.player.serverLevel().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                                float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;

                                if (d11 - d10 > Math.max(f2, Math.pow((double) (org.spigotmc.SpigotConfig.movedTooQuicklyMultiplier * (float) i * speed), 2)) && !this.isSingleplayerOwner()) {
                                    // CraftBukkit end
                                    LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), d7, d8, d9);
                                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                    return;
                                }
                            }

                            AABB axisalignedbb = this.player.getBoundingBox();

                            d7 = d0 - this.lastGoodX;
                            d8 = d1 - this.lastGoodY;
                            d9 = d2 - this.lastGoodZ;
                            boolean flag = d8 > 0.0D;

                            if (this.player.onGround() && !packetplayinflying.isOnGround() && flag) {
                                this.player.jumpFromGround();
                            }

                            this.player.move(MoverType.PLAYER, new Vec3(d7, d8, d9));
                            this.player.onGround = packetplayinflying.isOnGround();
                            double d12 = d8;

                            d7 = d0 - this.player.getX();
                            d8 = d1 - this.player.getY();
                            if (d8 > -0.5D || d8 < 0.5D) {
                                d8 = 0.0D;
                            }

                            d9 = d2 - this.player.getZ();
                            d11 = d7 * d7 + d8 * d8 + d9 * d9;
                            boolean flag1 = false;

                            if (!this.player.isChangingDimension() && d11 > org.spigotmc.SpigotConfig.movedWronglyThreshold && !this.player.isSleeping() && !this.player.gameMode.isCreative() && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) { // Spigot
                                flag1 = true;
                                LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
                            }

                            this.player.absMoveTo(d0, d1, d2, f, f1);
                            if (!this.player.noPhysics && !this.player.isSleeping() && (flag1 && worldserver.noCollision(this.player, axisalignedbb) || this.isPlayerCollidingWithAnythingNew((LevelReader) worldserver, axisalignedbb))) {
                                this.internalTeleport(d3, d4, d5, f, f1, Collections.emptySet()); // CraftBukkit - SPIGOT-1807: Don't call teleport event, when the client thinks the player is falling, because the chunks are not loaded on the client yet.
                                this.player.doCheckFallDamage(this.player.getY() - d6, packetplayinflying.isOnGround());
                            } else {
                                this.player.absMoveTo(prevX, prevY, prevZ, prevYaw, prevPitch);
                                CraftPlayer player = this.getCraftPlayer();
                                Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
                                Location to = player.getLocation().clone();
                                if (packetplayinflying.hasPos) {
                                    to.setX(packetplayinflying.x);
                                    to.setY(packetplayinflying.y);
                                    to.setZ(packetplayinflying.z);
                                }
                                if (packetplayinflying.hasRot) {
                                    to.setYaw(packetplayinflying.yRot);
                                    to.setPitch(packetplayinflying.xRot);
                                }
                                double delta = Math.pow(this.lastPosX - to.getX(), 2.0) + Math.pow(this.lastPosY - to.getY(), 2.0) + Math.pow(this.lastPosZ - to.getZ(), 2.0);
                                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());
                                if ((delta > 1f / 256 || deltaAngle > 10f) && ! this.player.isImmobile()) {
                                    this.lastPosX = to.getX();
                                    this.lastPosY = to.getY();
                                    this.lastPosZ = to.getZ();
                                    this.lastYaw = to.getYaw();
                                    this.lastPitch = to.getPitch();
                                    if (from.getX() != Double.MAX_VALUE) {
                                        Location oldTo = to.clone();
                                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                                        this.cserver.getPluginManager().callEvent(event);
                                        if (event.isCancelled()) {
                                            this.teleport(from);
                                            return;
                                        }
                                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                                            getCraftPlayer().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                            return;
                                        }
                                        if (!from.equals(this.getCraftPlayer().getLocation()) && this.justTeleported) {
                                            this.justTeleported = false;
                                            return;
                                        }
                                    }
                                }
                                this.player.absMoveTo(d0, d1, d2, f, f1); // Copied from above

                                // MC-135989, SPIGOT-5564: isRiptiding
                                this.clientIsFloating = d12 >= -0.03125D && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR && !this.server.isFlightAllowed() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.isFallFlying() && this.noBlocksAround((Entity) this.player) && !this.player.isAutoSpinAttack();
                                // CraftBukkit end
                                this.player.serverLevel().getChunkSource().move(this.player);
                                this.player.doCheckFallDamage(this.player.getY() - d6, packetplayinflying.isOnGround());
                                this.player.setOnGround(packetplayinflying.isOnGround());
                                if (flag) {
                                    this.player.resetFallDistance();
                                }

                                this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.lastGoodX = this.player.getX();
                                this.lastGoodY = this.player.getY();
                                this.lastGoodZ = this.player.getZ();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handlePlayerAction(ServerboundPlayerActionPacket packetplayinblockdig) {
        PacketUtils.ensureRunningOnSameThread(packetplayinblockdig, (ServerGamePacketListenerImpl) (Object) this, this.player.serverLevel());
        if (this.player.isImmobile()) {
            return;
        }
        BlockPos blockposition = packetplayinblockdig.getPos();
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action packetplayinblockdig_enumplayerdigtype = packetplayinblockdig.getAction();
        switch (packetplayinblockdig_enumplayerdigtype) {
            case SWAP_ITEM_WITH_OFFHAND -> {
                if (!this.player.isSpectator()) {
                    ItemStack itemstack = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    CraftItemStack mainHand = CraftItemStack.asCraftMirror(itemstack);
                    CraftItemStack offHand = CraftItemStack.asCraftMirror(this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    PlayerSwapHandItemsEvent swapItemsEvent = new PlayerSwapHandItemsEvent(this.getCraftPlayer(), mainHand.clone(), offHand.clone());
                    this.cserver.getPluginManager().callEvent(swapItemsEvent);
                    if (swapItemsEvent.isCancelled()) {
                        return;
                    }
                    if (swapItemsEvent.getOffHandItem().equals(offHand)) {
                        this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    } else {
                        this.player.setItemInHand(InteractionHand.OFF_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getOffHandItem()));
                    }
                    if (swapItemsEvent.getMainHandItem().equals(mainHand)) {
                        this.player.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
                    } else {
                        this.player.setItemInHand(InteractionHand.MAIN_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getMainHandItem()));
                    }
                    this.player.stopUsingItem();
                }
                return;
            }
            case DROP_ITEM -> {
                if (!this.player.isSpectator()) {
                    if (this.lastDropTick != ServerUtils.currentTick) {
                        this.dropCount = 0;
                        this.lastDropTick = ServerUtils.currentTick;
                    } else {
                        ++this.dropCount;
                        if (this.dropCount >= 20) {
                            LOGGER.warn(this.player.getScoreboardName() + " dropped their items too quickly!");
                            this.disconnect("You dropped your items too quickly (Hacking?)");
                            return;
                        }
                    }
                    this.player.drop(false);
                }
                return;
            }
            case DROP_ALL_ITEMS -> {
                if (!this.player.isSpectator()) {
                    this.player.drop(true);
                }
                return;
            }
            case RELEASE_USE_ITEM -> {
                this.player.releaseUsingItem();
                return;
            }
            case START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK -> {
                this.player.gameMode.handleBlockBreakAction(blockposition, packetplayinblockdig_enumplayerdigtype, packetplayinblockdig.getDirection(), this.player.level().getMaxBuildHeight(), packetplayinblockdig.getSequence());
                this.player.connection.ackBlockChangesUpTo(packetplayinblockdig.getSequence());
                return;
            }
            default -> {
                throw new IllegalArgumentException("Invalid player action");
            }
        }
    }

    @Override
    public boolean isDisconnected() {
        return !this.player.bridge$joining() && !this.connection.isConnected();
    }
}
