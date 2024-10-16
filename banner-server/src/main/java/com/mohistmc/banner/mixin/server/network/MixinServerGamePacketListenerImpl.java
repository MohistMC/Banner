package com.mohistmc.banner.mixin.server.network;

import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.injection.server.network.InjectionServerGamePacketListenerImpl;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.SmithingInventory;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl extends MixinServerCommonPacketListenerImpl implements InjectionServerGamePacketListenerImpl {

    @Shadow
    public ServerPlayer player;
    @Mutable
    @Shadow
    @Final
    private FutureChain chatMessageChain;

    @Shadow
    private static boolean containsInvalidValues(double x, double y, double z, float yRot, float xRot) {
        return false;
    }

    @Shadow
    private double vehicleLastGoodZ;
    @Shadow
    private double vehicleLastGoodY;
    @Shadow
    private double vehicleLastGoodX;
    @Shadow
    private boolean clientVehicleIsFloating;

    @Shadow
    protected abstract boolean noBlocksAround(Entity entity);

    @Shadow
    @Final
    static Logger LOGGER;
    @Shadow
    private int receivedMovePacketCount;
    @Shadow
    private int knownMovePacketCount;
    @Shadow
    private double vehicleFirstGoodZ;
    @Shadow
    private double vehicleFirstGoodY;
    @Shadow
    private double vehicleFirstGoodX;
    @Shadow
    @Nullable
    private Vec3 awaitingPositionFromClient;
    @Shadow
    private int tickCount;

    @Shadow
    public abstract void resetPosition();

    @Shadow
    private int awaitingTeleportTime;

    @Shadow
    private static double clampHorizontal(double value) {
        return 0;
    }

    @Shadow
    private static double clampVertical(double value) {
        return 0;
    }

    @Shadow
    private double firstGoodX;
    @Shadow
    private double firstGoodY;
    @Shadow
    private double firstGoodZ;
    @Shadow
    private double lastGoodX;
    @Shadow
    private double lastGoodY;
    @Shadow
    private double lastGoodZ;
    @Shadow
    private boolean clientIsFloating;

    @Shadow
    public abstract void ackBlockChangesUpTo(int i);

    @Shadow
    private static boolean isChatMessageIllegal(String message) {
        return false;
    }

    @Shadow
    protected abstract PlayerChatMessage getSignedMessage(ServerboundChatPacket packet, LastSeenMessages lastSeenMessages) throws SignedMessageChain.DecodeException;

    @Shadow
    protected abstract void handleMessageDecodeFailure(SignedMessageChain.DecodeException exception);

    @Shadow
    protected abstract CompletableFuture<FilteredText> filterTextPacket(String text);

    @Shadow
    protected abstract ParseResults<CommandSourceStack> parseCommand(String command);

    @Shadow
    protected abstract void detectRateSpam();

    @Shadow
    private int dropSpamTickCount;
    @Shadow
    private int awaitingTeleport;

    @Shadow
    @Nullable
    private Entity lastVehicle;

    @Shadow
    protected abstract boolean isPlayerCollidingWithAnythingNew(LevelReader levelReader, AABB aABB, double d, double e, double f);

    @Shadow
    private int chatSpamTickCount;

    @Shadow
    public abstract ServerPlayer getPlayer();

    @Shadow
    public abstract void teleport(double d, double e, double f, float g, float h);
    @Shadow public abstract void teleport(double d, double e, double f, float g, float h, Set<RelativeMovement> set);

    @Shadow protected abstract boolean updateAwaitingTeleport();

    @Shadow public abstract void sendDisguisedChatMessage(Component component, ChatType.Bound bound);

    @Shadow protected abstract <S> Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandSignedPacket serverboundChatCommandSignedPacket, SignableCommand<S> signableCommand, LastSeenMessages lastSeenMessages) throws SignedMessageChain.DecodeException;

    @Shadow protected abstract Optional<LastSeenMessages> unpackAndApplyLastSeen(LastSeenMessages.Update update);

    @Shadow protected abstract void tryHandleChat(String string, Runnable runnable);

    @Shadow protected abstract Filterable<String> filterableFromOutgoing(FilteredText filteredText);

    private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 6 * 6;
    private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 7 * 7;
    private CraftServer cserver;
    public boolean processedDisconnect;
    private int allowedPlayerTicks;
    private int dropCount;
    private int lastTick;
    private volatile int lastBookTick;

    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private float lastPitch;
    private float lastYaw;
    private boolean justTeleported;
    private boolean hasMoved; // Spigot

    @Override
    public CraftPlayer getCraftPlayer() {
        return (this.player == null) ? null : this.player.getBukkitEntity();
    }

    @Inject(method = "<init>",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;chunkSender:Lnet/minecraft/server/network/PlayerChunkSender;", shift = At.Shift.BEFORE))
    private void banner$preHandlePlayer(MinecraftServer minecraftServer, Connection connection,
                                        ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie,
                                        CallbackInfo ci) {
        banner$setPlayer(serverPlayer);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(MinecraftServer server, Connection connection,
                             ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie,
                             CallbackInfo ci) {
        allowedPlayerTicks = 1;
        dropCount = 0;
        lastPosX = Double.MAX_VALUE;
        lastPosY = Double.MAX_VALUE;
        lastPosZ = Double.MAX_VALUE;
        lastPitch = Float.MAX_VALUE;
        lastYaw = Float.MAX_VALUE;
        justTeleported = false;
        this.cserver = ((CraftServer) Bukkit.getServer());
        this.chatMessageChain = new FutureChain(server.bridge$chatExecutor());
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handleMoveVehicle(final ServerboundMoveVehiclePacket packetplayinvehiclemove) {
        PacketUtils.ensureRunningOnSameThread(packetplayinvehiclemove, ((ServerGamePacketListener) (Object) this), this.player.serverLevel());
        if (containsInvalidValues(packetplayinvehiclemove.getX(), packetplayinvehiclemove.getY(), packetplayinvehiclemove.getZ(), packetplayinvehiclemove.getYRot(), packetplayinvehiclemove.getXRot())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
        } else {
            Entity entity = this.player.getRootVehicle();

            if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
                ServerLevel worldserver = this.player.serverLevel();
                double d0 = entity.getX();
                double d1 = entity.getY();
                double d2 = entity.getZ();
                double d3 = clampHorizontal(packetplayinvehiclemove.getX());
                double d4 = clampVertical(packetplayinvehiclemove.getY());
                double d5 = clampHorizontal(packetplayinvehiclemove.getZ());
                float f = Mth.wrapDegrees(packetplayinvehiclemove.getYRot());
                float f1 = Mth.wrapDegrees(packetplayinvehiclemove.getXRot());
                double d6 = d3 - this.vehicleFirstGoodX;
                double d7 = d4 - this.vehicleFirstGoodY;
                double d8 = d5 - this.vehicleFirstGoodZ;
                double d9 = entity.getDeltaMovement().lengthSqr();
                double d10 = d6 * d6 + d7 * d7 + d8 * d8;


                // CraftBukkit start - handle custom speeds and skipped ticks
                this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
                this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                this.lastTick = (int) (System.currentTimeMillis() / 50);

                ++this.receivedMovePacketCount;
                int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                if (i > Math.max(this.allowedPlayerTicks, 5)) {
                    LOGGER.debug(this.player.getScoreboardName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                    i = 1;
                }

                if (d10 > 0) {
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
                speed *= 2f; // TODO: Get the speed of the vehicle instead of the player

                if (d10 - d9 > Math.max(100.0D, Math.pow((double) (10.0F * (float) i * speed), 2)) && !this.isSingleplayerOwner()) {
                    // CraftBukkit end
                    LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{entity.getName().getString(), this.player.getName().getString(), d6, d7, d8});
                    this.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }

                boolean flag = worldserver.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));

                d6 = d3 - this.vehicleLastGoodX;
                d7 = d4 - this.vehicleLastGoodY - 1.0E-6D;
                d8 = d5 - this.vehicleLastGoodZ;
                boolean flag1 = entity.verticalCollisionBelow;

                if (entity instanceof LivingEntity entityliving) {

                    if (entityliving.onClimbable()) {
                        entityliving.resetFallDistance();
                    }
                }

                entity.move(MoverType.PLAYER, new Vec3(d6, d7, d8));

                d6 = d3 - entity.getX();
                d7 = d4 - entity.getY();
                if (d7 > -0.5D || d7 < 0.5D) {
                    d7 = 0.0D;
                }

                d8 = d5 - entity.getZ();
                d10 = d6 * d6 + d7 * d7 + d8 * d8;
                boolean flag2 = false;

                if (d10 > 0.0625D) {
                    flag2 = true;
                    LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", new Object[]{entity.getName().getString(), this.player.getName().getString(), Math.sqrt(d10)});
                }

                entity.absMoveTo(d3, d4, d5, f, f1);
                player.absMoveTo(d3, d4, d5, this.player.getYRot(), this.player.getXRot()); // CraftBukkit
                boolean flag3 = worldserver.noCollision(entity, entity.getBoundingBox().deflate(0.0625D));

                if (flag && (flag2 || !flag3)) {
                    entity.absMoveTo(d0, d1, d2, f, f1);
                    player.absMoveTo(d0, d1, d2, this.player.getYRot(), this.player.getXRot()); // CraftBukkit
                    this.connection.send(new ClientboundMoveVehiclePacket(entity));
                    return;
                }

                // CraftBukkit start - fire PlayerMoveEvent
                Player player = this.getCraftPlayer();
                Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get the Players previous Event location.
                Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

                // If the packet contains movement information then we update the To location with the correct XYZ.
                to.setX(packetplayinvehiclemove.getX());
                to.setY(packetplayinvehiclemove.getY());
                to.setZ(packetplayinvehiclemove.getZ());


                // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
                to.setYaw(packetplayinvehiclemove.getYRot());
                to.setPitch(packetplayinvehiclemove.getXRot());

                // Prevent 40 event-calls for less than a single pixel of movement >.>
                double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

                if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.isImmobile()) {
                    this.lastPosX = to.getX();
                    this.lastPosY = to.getY();
                    this.lastPosZ = to.getZ();
                    this.lastYaw = to.getYaw();
                    this.lastPitch = to.getPitch();

                    // Skip the first time we do this
                    if (from.getX() != Double.MAX_VALUE) {
                        Location oldTo = to.clone();
                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                        this.cserver.getPluginManager().callEvent(event);

                        // If the event is cancelled we move the player back to their old location.
                        if (event.isCancelled()) {
                            teleport(from);
                            return;
                        }

                        // If a Plugin has changed the To destination then we teleport the Player
                        // there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                        // We only do this if the Event was not cancelled.
                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                            this.player.getBukkitEntity().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            return;
                        }

                        // Check to see if the Players Location has some how changed during the call of the event.
                        // This can happen due to a plugin teleporting the player instead of using .setTo()
                        if (!from.equals(this.getCraftPlayer().getLocation()) && this.justTeleported) {
                            this.justTeleported = false;
                            return;
                        }
                    }
                }
                // CraftBukkit end

                this.player.serverLevel().getChunkSource().move(this.player);
                this.player.checkMovementStatistics(this.player.getX() - d0, this.player.getY() - d1, this.player.getZ() - d2);
                this.clientVehicleIsFloating = d7 >= -0.03125D && !flag1 && !this.server.isFlightAllowed() && !entity.isNoGravity() && this.noBlocksAround(entity);
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

    @Inject(method = "performSignedChatCommand", cancellable = true, at = @At("HEAD"))
    private void banner$rejectIfDisconnectSigned(CallbackInfo ci) {
        if (this.player.hasDisconnected()) {
            ci.cancel();
        }
    }

    @Decorate(method = "performUnsignedChatCommand", inject = true, at = @At("HEAD"))
    private void banner$commandPreprocessEvent(String s) throws Throwable {
        if (this.player.hasDisconnected()) {
            DecorationOps.cancel().invoke();
            return;
        }
        String command = "/" + s;
        LOGGER.info(this.player.getScoreboardName() + " issued server command: " + command);

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(getCraftPlayer(), command, new LazyPlayerSet(server));
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            DecorationOps.cancel().invoke();
            return;
        }
        s = event.getMessage().substring(1);
        DecorationOps.blackhole().invoke(s);
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    private void performSignedChatCommand(ServerboundChatCommandSignedPacket serverboundchatcommandsignedpacket, LastSeenMessages lastseenmessages) {
        // CraftBukkit start
        String command = "/" + serverboundchatcommandsignedpacket.command();
        LOGGER.info(this.player.getScoreboardName() + " issued server command: " + command);

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(getCraftPlayer(), command, new LazyPlayerSet(server));
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }
        command = event.getMessage().substring(1);

        ParseResults<CommandSourceStack> parseresults = this.parseCommand(command);
        // CraftBukkit end

        Map map;

        try {
            map = (serverboundchatcommandsignedpacket.command().equals(command)) ? this.collectSignedArguments(serverboundchatcommandsignedpacket, SignableCommand.of(parseresults), lastseenmessages) : Collections.emptyMap(); // CraftBukkit
        } catch (SignedMessageChain.DecodeException signedmessagechain_a) {
            this.handleMessageDecodeFailure(signedmessagechain_a);
            return;
        }

        CommandSigningContext.SignedArguments commandsigningcontext_a = new CommandSigningContext.SignedArguments(map);

        parseresults = Commands.<CommandSourceStack>mapSource(parseresults, (commandlistenerwrapper) -> { // CraftBukkit - decompile error
            return commandlistenerwrapper.withSigningContext(commandsigningcontext_a, this.chatMessageChain);
        });
        this.server.getCommands().performCommand(parseresults, command); // CraftBukkit
    }

    @Inject(method = "tryHandleChat", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getChatVisibility()Lnet/minecraft/world/entity/player/ChatVisiblity;"))
    private void banner$deadMenTellNoTales(String string, Runnable runnable, CallbackInfo ci) {
        if (this.player.isRemoved()) {
            this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
            ci.cancel();
        }
    }

    @Inject(method = "handleAcceptTeleportPacket", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;awaitingTeleport:I"))
    private void banner$confirm(ServerboundAcceptTeleportationPacket packetIn, CallbackInfo ci) {
        if (this.awaitingPositionFromClient == null) {
            ci.cancel();
        }
    }

    @Inject(method = "handleRecipeBookChangeSettingsPacket",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getRecipeBook()Lnet/minecraft/stats/ServerRecipeBook;"))
    private void banner$fireRecipeEvent(ServerboundRecipeBookChangeSettingsPacket serverboundRecipeBookChangeSettingsPacket, CallbackInfo ci) {
        CraftEventFactory.callRecipeBookSettingsEvent(this.player, serverboundRecipeBookChangeSettingsPacket.getBookType(), serverboundRecipeBookChangeSettingsPacket.isOpen(), serverboundRecipeBookChangeSettingsPacket.isFiltering()); // CraftBukkit
    }

    @Inject(method = "handleSelectTrade", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;setSelectionHint(I)V"))
    private void banner$tradeSelect(ServerboundSelectTradePacket packet, CallbackInfo ci, @Local int i, @Local MerchantMenu merchantMenu) {
        var event = CraftEventFactory.callTradeSelectEvent(this.player, i, (MerchantMenu) merchantMenu);
        if (event.isCancelled()) {
            this.player.getBukkitEntity().updateInventory();
            ci.cancel();
        }
    }

    @Inject(method = "handleEditBook", at = @At("HEAD"), cancellable = true)
    private void banner$editBookSpam(ServerboundEditBookPacket packetIn, CallbackInfo ci) {
        if (this.lastBookTick == 0) {
            this.lastBookTick = BukkitFieldHooks.currentTick() - 20;
        }
        if (this.lastBookTick + 20 > BukkitFieldHooks.currentTick()) {
            this.disconnect("Book edited too quickly!");
            ci.cancel();
        }
        this.lastBookTick = BukkitFieldHooks.currentTick();
    }


    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    private void updateBookContents(List<FilteredText> list, int slot) {
        ItemStack old = this.player.getInventory().getItem(slot);
        if (old.is(Items.WRITABLE_BOOK)) {
            ItemStack itemstack = old.copy();
            List<Filterable<String>> list1 = list.stream().map(this::filterableFromOutgoing).toList();
            itemstack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(list1));
            this.player.getInventory().setItem(slot, CraftEventFactory.handleEditBookEvent(this.player, slot, old, itemstack)); // CraftBukkit // Paper - Don't ignore result (see other callsite for handleEditBookEvent)
        }
    }

    @Decorate(method = "signBook", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void banner$editBookEvent(Inventory instance, int i, ItemStack stack, FilteredText text, List<FilteredText> list, int slot, @io.izzel.arclight.mixin.Local(ordinal = 0) ItemStack handStack) throws Throwable {
        CraftEventFactory.handleEditBookEvent(player, i, handStack, stack);
        DecorationOps.callsite().invoke(instance, i, handStack);
    }

    @Inject(method = "updateAwaitingTeleport", at = @At("RETURN"))
    private void banner$setAllowedTicks(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            this.allowedPlayerTicks = 20;
        }
    }

    /**
     * @author wdog5
     * @reason bukkit
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
                if (!this.updateAwaitingTeleport()) {
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
                            boolean fallFlying = this.player.isFallFlying();
                            if (worldserver.tickRateManager().runsNormally()) {
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

                            if (!this.player.noPhysics && !this.player.isSleeping() && (flag1 && worldserver.noCollision(this.player, axisalignedbb) || this.isPlayerCollidingWithAnythingNew(worldserver, axisalignedbb, d0, d1, d2))) {
                                this.bridge$pushNoTeleportEvent();
                                this.teleport(d3, d4, d5, f, f1, Collections.emptySet()); // CraftBukkit - SPIGOT-1807: Don't call teleport event, when the client thinks the player is falling, because the chunks are not loaded on the client yet.
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, packetplayinflying.isOnGround());
                            } else {
                                // Reset to old location first
                                this.player.absMoveTo(prevX, prevY, prevZ, prevYaw, prevPitch);
                                CraftPlayer player = this.getCraftPlayer();
                                if (!this.hasMoved) {
                                    this.lastPosX = prevX;
                                    this.lastPosY = prevY;
                                    this.lastPosZ = prevZ;
                                    this.lastYaw = prevYaw;
                                    this.lastPitch = prevPitch;
                                    this.hasMoved = true;
                                }
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

                                this.player.absMoveTo(d0, d1, d2, f, f1); // Copied from above
                                boolean autoSpinAttack = this.player.isAutoSpinAttack();
                                this.clientIsFloating = d12 >= -0.03125D
                                        && this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                                        && !this.server.isFlightAllowed()
                                        && !(this.player.getAbilities().mayfly)
                                        && !this.player.hasEffect(MobEffects.LEVITATION)
                                        && !fallFlying
                                        && !autoSpinAttack
                                        && this.noBlocksAround(this.player);
                                // CraftBukkit end
                                this.player.serverLevel().getChunkSource().move(this.player);
                                var vec3 = new Vec3(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.player.setOnGroundWithMovement(packetplayinflying.isOnGround(), vec3);
                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, packetplayinflying.isOnGround());
                                this.player.setKnownMovement(vec3);
                                if (flag) {
                                    this.player.resetFallDistance();
                                }

                                if (packetplayinflying.isOnGround() || this.player.hasLandedInLiquid() || this.player.onClimbable() || this.player.isSpectator() || fallFlying || autoSpinAttack) {
                                    this.player.tryResetCurrentImpulseContext();
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

    @Redirect(method = "handlePlayerAction",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V", ordinal = 0))
    private void banner$cancelHeldItem0(ServerPlayer instance, InteractionHand hand, ItemStack stack) {
    }

    @Redirect(method = "handlePlayerAction",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V", ordinal = 1))
    private void banner$cancelHeldItem1(ServerPlayer instance, InteractionHand hand, ItemStack stack) {
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;stopUsingItem()V"))
    private void banner$itemSwapEvent(ServerboundPlayerActionPacket packet, CallbackInfo ci,
                                      @Local ItemStack itemStack) {
        // CraftBukkit start - inspiration taken from DispenserRegistry (See SpigotCraft#394)
        CraftItemStack mainHand = CraftItemStack.asCraftMirror(itemStack);
        CraftItemStack offHand = CraftItemStack.asCraftMirror(this.player.getItemInHand(InteractionHand.MAIN_HAND));
        PlayerSwapHandItemsEvent swapItemsEvent = new PlayerSwapHandItemsEvent(getCraftPlayer(), mainHand.clone(), offHand.clone());
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
            this.player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        } else {
            this.player.setItemInHand(InteractionHand.MAIN_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getMainHandItem()));
        }
        // CraftBukkit end
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;ackBlockChangesUpTo(I)V"),
            cancellable = true)
    private void banner$checkImmobile(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (this.player.isImmobile()) ci.cancel(); // CraftBukkit
    }

    @Inject(method = "handleUseItemOn", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;serverLevel()Lnet/minecraft/server/level/ServerLevel;", ordinal = 1))
    private void banner$frozenUseItem(ServerboundUseItemOnPacket packetIn, CallbackInfo ci) {
        if (!this.checkLimit(packetIn.bridge$timestamp())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private void banner$setStopUsing(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        this.player.stopUsingItem(); // CraftBukkit - SPIGOT-4706
    }

    private int limitedPackets;
    private long lastLimitedPacket = -1;

    @Override
    public boolean checkLimit(long timestamp) {
        if (lastLimitedPacket != -1 && timestamp - lastLimitedPacket < 30 && limitedPackets++ >= 4) {
            return false;
        }

        if (lastLimitedPacket == -1 || timestamp - lastLimitedPacket >= 30) {
            lastLimitedPacket = timestamp;
            limitedPackets = 0;
            return true;
        }

        return true;
    }

    @Inject(method = "handleUseItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;ackBlockChangesUpTo(I)V"), cancellable = true)
    private void banner$checkUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (this.player.isImmobile()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private void banner$handleInteractEvent(ServerboundUseItemPacket packet, CallbackInfo ci,
                                            @Local InteractionHand interactionHand, @Local ItemStack itemStack) {
        // CraftBukkit start
        // Raytrace to look for 'rogue armswings'
        float f1 = this.player.getXRot();
        float f2 = this.player.getYRot();
        double d0 = this.player.getX();
        double d1 = this.player.getY() + (double) this.player.getEyeHeight();
        double d2 = this.player.getZ();
        Vec3 vec3d = new Vec3(d0, d1, d2);

        float f3 = Mth.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = Mth.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -Mth.cos(-f1 * 0.017453292F);
        float f6 = Mth.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = player.gameMode.getGameModeForPlayer() == GameType.CREATIVE ? 5.0D : 4.5D;
        Vec3 vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
        BlockHitResult movingobjectposition = this.player.level().clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        boolean cancelled;
        if (movingobjectposition == null || movingobjectposition.getType() != HitResult.Type.BLOCK) {
            org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemStack, interactionHand);
            cancelled = event.useItemInHand() == Event.Result.DENY;
        } else {
            BlockHitResult movingobjectpositionblock = movingobjectposition;
            if (player.gameMode.bridge$isFiredInteract() && player.gameMode.bridge$getinteractPosition().equals(movingobjectpositionblock.getBlockPos()) && player.gameMode.bridge$getinteractHand() == interactionHand && ItemStack.isSameItem(player.gameMode.bridge$getinteractItemStack(), itemStack)) {
                cancelled = player.gameMode.bridge$getInteractResult();
            } else {
                org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getDirection(), itemStack, true, interactionHand, movingobjectpositionblock.getLocation());
                cancelled = event.useItemInHand() == Event.Result.DENY;
            }
            player.gameMode.bridge$setFiredInteract(false);
        }

        if (cancelled) {
            this.player.getBukkitEntity().updateInventory(); // SPIGOT-2524
            return;
        }
        itemStack = this.player.getItemInHand(interactionHand); // Update in case it was changed in the event
        if (itemStack.isEmpty()) {
            return;
        }
        // CraftBukkit end
    }

    @Inject(method = "onDisconnect", cancellable = true, at = @At("HEAD"))
    private void banner$returnIfProcessed(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
        if (processedDisconnect) {
            ci.cancel();
        } else {
            processedDisconnect = true;
        }
    }

    @Redirect(method = "removePlayerFromWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    public void banner$captureQuit(PlayerList instance, Component message, boolean bypassHiddenChat) {
        // do nothing
    }

    @Inject(method = "removePlayerFromWorld", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/players/PlayerList;remove(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void banner$setQuitMsg(CallbackInfo ci) {
        String quitMessage = this.server.getPlayerList().bridge$quiltMsg();

        // Banner start - avoid quilt msg NPE
        if (quitMessage == null) {
            quitMessage = BukkitSnapshotCaptures.getQuitMessage();
        }
        if ((quitMessage != null) && (!quitMessage.isEmpty())) {
            this.server.getPlayerList().broadcastMessage(CraftChatMessage.fromString(quitMessage));
        }

        BukkitSnapshotCaptures.getQuitMessage();
        // Banner end
    }

    @Inject(method = "sendPlayerChatMessage", cancellable = true, at = @At("HEAD"))
    private void banner$cantSee(PlayerChatMessage playerChatMessage, ChatType.Bound bound, CallbackInfo ci) {
        if (!getCraftPlayer().canSee(playerChatMessage.link().sender())) {
            sendDisguisedChatMessage(playerChatMessage.decoratedContent(), bound);
            ci.cancel();
        }
    }

    @Inject(method = "handleAnimate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"),
            cancellable = true)
    private void banner$checkAnimate(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (this.player.isImmobile()) ci.cancel(); // CraftBukkit
    }

    @Inject(method = "handleAnimate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"),
            cancellable = true)
    private void banner$handleAnimateEvent(ServerboundSwingPacket packet, CallbackInfo ci) {
        // CraftBukkit start - Raytrace to look for 'rogue armswings'
        float f1 = this.player.getXRot();
        float f2 = this.player.getYRot();
        double d0 = this.player.getX();
        double d2 = this.player.getY() + this.player.getEyeHeight();
        double d3 = this.player.getZ();
        Vec3 vec3d = new Vec3(d0, d2, d3);
        float f3 = Mth.cos(-f2 * 0.017453292f - 3.1415927f);
        float f4 = Mth.sin(-f2 * 0.017453292f - 3.1415927f);
        float f5 = -Mth.cos(-f1 * 0.017453292f);
        float f6 = Mth.sin(-f1 * 0.017453292f);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d4 = player.gameMode.getGameModeForPlayer() == GameType.CREATIVE ? 5.0D : 4.5D;
        Vec3 vec3d2 = vec3d.add(f7 * d4, f6 * d4, f8 * d4);
        // SPIGOT-5607: Only call interact event if no block or entity is being clicked. Use bukkit ray trace method, because it handles blocks and entities at the same time
        // SPIGOT-7429: Make sure to call PlayerInteractEvent for spectators and non-pickable entities
        HitResult result = this.player.level().clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.player));
        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.getInventory().getSelected(), InteractionHand.MAIN_HAND);
        }

        // Arm swing animation
        PlayerAnimationEvent event = new PlayerAnimationEvent(this.getCraftPlayer(), packet.getHand() == InteractionHand.MAIN_HAND ? PlayerAnimationType.ARM_SWING : PlayerAnimationType.OFF_ARM_SWING);
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled()) ci.cancel();
        // CraftBukkit end
    }

    @Inject(method = "handleSetCarriedItem", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"))
    private void banner$carriedItemBlocked(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        if (this.player.isImmobile()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCarriedItem", cancellable = true, at = @At(value = "FIELD", ordinal = 0, target = "Lnet/minecraft/world/entity/player/Inventory;selected:I"))
    private void banner$itemHeldEvent(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getCraftPlayer(), this.player.getInventory().selected, packet.getSlot());
        this.cserver.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            this.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
            this.player.resetLastActionTime();
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, remap = false, target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void banner$kickOutOfBoundClick(ServerboundSetCarriedItemPacket serverboundSetCarriedItemPacket, CallbackInfo ci) {
        this.disconnect("Invalid hotbar selection (Hacking?)");
    }

    // TODO ChatType.RAW
    @Override
    public void chat(String s, PlayerChatMessage original, boolean async) {
        if (s.isEmpty() || this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
            return;
        }
        ServerGamePacketListenerImpl handler = (ServerGamePacketListenerImpl) (Object) this;
        var outgoing = OutgoingChatMessage.create(original);
        if (!async && s.startsWith("/")) {
            this.handleCommand(s);
        } else if (this.player.getChatVisibility() != ChatVisiblity.SYSTEM) {
            Player thisPlayer = this.getCraftPlayer();
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, thisPlayer, s, new LazyPlayerSet(this.server));
            String originalFormat = event.getFormat(), originalMessage = event.getMessage();
            this.cserver.getPluginManager().callEvent(event);
            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
                PlayerChatEvent queueEvent = new PlayerChatEvent(thisPlayer, event.getMessage(), event.getFormat(), event.getRecipients());
                queueEvent.setCancelled(event.isCancelled());
                class SyncChat extends Waitable<Object> {

                    @Override
                    protected Object evaluate() {
                        Bukkit.getPluginManager().callEvent(queueEvent);
                        if (queueEvent.isCancelled()) {
                            return null;
                        }
                        String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                        if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                            if (!org.spigotmc.SpigotConfig.bungee && originalFormat.equals(queueEvent.getFormat()) && originalMessage.equals(queueEvent.getMessage()) && queueEvent.getPlayer().getName().equalsIgnoreCase(queueEvent.getPlayer().getDisplayName())) { // Spigot
                                server.getPlayerList().broadcastChatMessage(original, player, ChatType.bind(ChatType.CHAT, player));
                                return null;
                            }
                            for (ServerPlayer recipient : server.getPlayerList().players) {
                                 recipient.getBukkitEntity().sendMessage(player.getUUID(), message);
                            }
                        } else {
                            for (Player player2 : queueEvent.getRecipients()) {
                                player2.sendMessage(thisPlayer.getUniqueId(), message);
                            }
                        }
                        Bukkit.getConsoleSender().sendMessage(message);
                        return null;
                    }
                }
                Waitable waitable = new SyncChat();
                if (async) {
                    server.bridge$queuedProcess(waitable);
                } else {
                    waitable.run();
                }
                try {
                    waitable.get();
                    return;
                } catch (InterruptedException e2) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    throw new RuntimeException("Exception processing chat event", e.getCause());
                }
            }
            if (event.isCancelled()) {
                return;
            }

            s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
            if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
                if (!org.spigotmc.SpigotConfig.bungee && originalFormat.equals(event.getFormat()) && originalMessage.equals(event.getMessage()) && event.getPlayer().getName().equalsIgnoreCase(event.getPlayer().getDisplayName())) { // Spigot
                    server.getPlayerList().broadcastChatMessage(original, player, ChatType.bind(ChatType.CHAT, player));
                    return;
                }

                for (ServerPlayer recipient : server.getPlayerList().players) {
                    recipient.getBukkitEntity().sendMessage(player.getUUID(), s);
                }
            } else {
                for (Player recipient : event.getRecipients()) {
                    recipient.sendMessage(player.getUUID(), s);
                }
            }
            Bukkit.getConsoleSender().sendMessage(s);
        }
    }

    @Override
    public void handleCommand(String s) {
        if ( org.spigotmc.SpigotConfig.logCommands ) // Spigot
            LOGGER.info(this.player.getScoreboardName() + " issued server command: " + s);

        CraftPlayer player = this.getCraftPlayer();

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s, new LazyPlayerSet(server));
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        try {
            if (this.cserver.dispatchCommand(event.getPlayer(), event.getMessage().substring(1))) {
                return;
            }
        } catch (org.bukkit.command.CommandException ex) {
            player.sendMessage(org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(ServerGamePacketListenerImpl.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            return;
        } finally {
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private void broadcastChatMessage(PlayerChatMessage playerchatmessage) {
        String s = playerchatmessage.signedContent();
        if (s.isEmpty()) {
            LOGGER.warn(this.player.getScoreboardName() + " tried to send an empty message");
        } else if (getCraftPlayer().isConversing()) {
            final String conversationInput = s;
            this.server.bridge$queuedProcess(() -> getCraftPlayer().acceptConversationInput(conversationInput));
        } else if (this.player.getChatVisibility() == ChatVisiblity.SYSTEM) { // Re-add "Command Only" flag check
            this.send(new ClientboundSystemChatPacket(Component.translatable("chat.cannotSend").withStyle(ChatFormatting.RED), false));
        } else {
            this.chat(s, playerchatmessage, true);
        }
        // this.server.getPlayerList().broadcastChatMessage(playerchatmessage, this.player, ChatMessageType.bind(ChatMessageType.CHAT, (Entity) this.player));
        this.detectRateSpam(s);
    }

    @Override
    public void detectRateSpam(String s) {
        boolean counted = true;
        for (String exclude : org.spigotmc.SpigotConfig.spamExclusions) {
            if (exclude != null && s.startsWith(exclude)) {
                counted = false;
                break;
            }
        }
        // Spigot end
        this.chatSpamTickCount += 20;
        if (counted && this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
            this.disconnect(Component.translatable("disconnect.spam"));
        }
    }

    @Inject(method = "handlePlayerCommand", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void banner$toggleAction(ServerboundPlayerCommandPacket packetIn, CallbackInfo ci) {
        if (this.player.isRemoved()) {
            ci.cancel();
            return;
        }
        if (packetIn.getAction() == ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY || packetIn.getAction() == ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY) {
            PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getCraftPlayer(), packetIn.getAction() == ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY);
            this.cserver.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        } else if (packetIn.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING || packetIn.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
            PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getCraftPlayer(), packetIn.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING);
            this.cserver.getPluginManager().callEvent(e2);
            if (e2.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleInteract",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;serverLevel()Lnet/minecraft/server/level/ServerLevel;",
            ordinal = 1), cancellable = true)
    private void banner$checkInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.player.isImmobile()) ci.cancel(); // CraftBukkit
    }

    @Inject(method = "handleContainerClose", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;doCloseContainer()V"))
    private void banner$invClose(ServerboundContainerClosePacket packetIn, CallbackInfo ci) {
        if (this.player.isImmobile()) ci.cancel(); // CraftBukkit
      CraftEventFactory.handleInventoryCloseEvent(this.player);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handleContainerClick(ServerboundContainerClickPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, this.player.serverLevel());
        if (this.player.isImmobile()) return; // CraftBukkit
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId == packet.getContainerId() && this.player.containerMenu.stillValid(this.player)) { // CraftBukkit
            boolean cancelled = this.player.isSpectator(); // CraftBukkit - see below if
            if (false/*this.player.isSpectator()*/) { // CraftBukkit
                this.player.containerMenu.sendAllDataToRemote();
            } else if (!this.player.containerMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            } else {
                int i = packet.getSlotNum();

                if (!this.player.containerMenu.isValidSlotIndex(i)) {
                    LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", new Object[]{this.player.getName(), i, this.player.containerMenu.slots.size()});
                } else {
                    boolean flag = packet.getStateId() != this.player.containerMenu.getStateId();

                    this.player.containerMenu.suppressRemoteUpdates();
                    // CraftBukkit start - Call InventoryClickEvent
                    if (packet.getSlotNum() < -1 && packet.getSlotNum() != -999) {
                        return;
                    }

                    BukkitSnapshotCaptures.captureContainerOwner(this.player);
                    InventoryView inventory = this.player.containerMenu.getBukkitView();
                    if(inventory == null) {
                        inventory = new CraftInventoryView(this.player.getBukkitEntity(), Bukkit.createInventory(this.player.getBukkitEntity(), InventoryType.CHEST), this.player.containerMenu);
                        this.player.containerMenu.setBukkitView(inventory);
                    }
                    InventoryType.SlotType type = inventory.getSlotType(packet.getSlotNum());


                    InventoryClickEvent event;
                    ClickType click = ClickType.UNKNOWN;
                    InventoryAction action = InventoryAction.UNKNOWN;

                    ItemStack itemstack = ItemStack.EMPTY;

                    switch (packet.getClickType()) {
                        case PICKUP:
                            if (packet.getButtonNum() == 0) {
                                click = ClickType.LEFT;
                            } else if (packet.getButtonNum() == 1) {
                                click = ClickType.RIGHT;
                            }
                            if (packet.getButtonNum() == 0 || packet.getButtonNum() == 1) {
                                action = InventoryAction.NOTHING; // Don't want to repeat ourselves
                                if (packet.getSlotNum() == -999) {
                                    if (!player.containerMenu.getCarried().isEmpty()) {
                                        action = packet.getButtonNum() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                                    }
                                } else if (packet.getSlotNum() < 0)  {
                                    action = InventoryAction.NOTHING;
                                } else {
                                    Slot slot = this.player.containerMenu.getSlot(packet.getSlotNum());
                                    if (slot != null) {
                                        ItemStack clickedItem = slot.getItem();
                                        ItemStack cursor = player.containerMenu.getCarried();
                                        if (clickedItem.isEmpty()) {
                                            if (!cursor.isEmpty()) {
                                                action = packet.getButtonNum() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                            }
                                        } else if (slot.mayPickup(player)) {
                                            if (cursor.isEmpty()) {
                                                action = packet.getButtonNum() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                            } else if (slot.mayPlace(cursor)) {
                                                if (ItemStack.isSameItem(clickedItem, cursor)) {
                                                    int toPlace = packet.getButtonNum() == 0 ? cursor.getCount() : 1;
                                                    toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.getCount());
                                                    toPlace = Math.min(toPlace, slot.container.getMaxStackSize() - clickedItem.getCount());
                                                    if (toPlace == 1) {
                                                        action = InventoryAction.PLACE_ONE;
                                                    } else if (toPlace == cursor.getCount()) {
                                                        action = InventoryAction.PLACE_ALL;
                                                    } else if (toPlace < 0) {
                                                        action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE; // this happens with oversized stacks
                                                    } else if (toPlace != 0) {
                                                        action = InventoryAction.PLACE_SOME;
                                                    }
                                                } else if (cursor.getCount() <= slot.getMaxStackSize()) {
                                                    action = InventoryAction.SWAP_WITH_CURSOR;
                                                }
                                            } else if (ItemStack.isSameItem(cursor, clickedItem)) {
                                                if (clickedItem.getCount() >= 0) {
                                                    if (clickedItem.getCount() + cursor.getCount() <= cursor.getMaxStackSize()) {
                                                        // As of 1.5, this is result slots only
                                                        action = InventoryAction.PICKUP_ALL;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        // TODO check on updates
                        case QUICK_MOVE:
                            if (packet.getButtonNum() == 0) {
                                click = ClickType.SHIFT_LEFT;
                            } else if (packet.getButtonNum() == 1) {
                                click = ClickType.SHIFT_RIGHT;
                            }
                            if (packet.getButtonNum() == 0 || packet.getButtonNum() == 1) {
                                if (packet.getSlotNum() < 0) {
                                    action = InventoryAction.NOTHING;
                                } else {
                                    Slot slot = this.player.containerMenu.getSlot(packet.getSlotNum());
                                    if (slot != null && slot.mayPickup(this.player) && slot.hasItem()) {
                                        action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                }
                            }
                            break;
                        case SWAP:
                            if ((packet.getButtonNum() >= 0 && packet.getButtonNum() < 9) || packet.getButtonNum() == 40) {
                                click = (packet.getButtonNum() == 40) ? ClickType.SWAP_OFFHAND : ClickType.NUMBER_KEY;
                                Slot clickedSlot = this.player.containerMenu.getSlot(packet.getSlotNum());
                                if (clickedSlot.mayPickup(player)) {
                                    ItemStack hotbar = this.player.getInventory().getItem(packet.getButtonNum());
                                    boolean canCleanSwap = hotbar.isEmpty() || (clickedSlot.container == player.getInventory() && clickedSlot.mayPlace(hotbar)); // the slot will accept the hotbar item
                                    if (clickedSlot.hasItem()) {
                                        if (canCleanSwap) {
                                            action = InventoryAction.HOTBAR_SWAP;
                                        } else {
                                            action = InventoryAction.HOTBAR_MOVE_AND_READD;
                                        }
                                    } else if (!clickedSlot.hasItem() && !hotbar.isEmpty() && clickedSlot.mayPlace(hotbar)) {
                                        action = InventoryAction.HOTBAR_SWAP;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                } else {
                                    action = InventoryAction.NOTHING;
                                }
                            }
                            break;
                        case CLONE:
                            if (packet.getButtonNum() == 2) {
                                click = ClickType.MIDDLE;
                                if (packet.getSlotNum() < 0) {
                                    action = InventoryAction.NOTHING;
                                } else {
                                    Slot slot = this.player.containerMenu.getSlot(packet.getSlotNum());
                                    if (slot != null && slot.hasItem() && player.getAbilities().instabuild && player.containerMenu.getCarried().isEmpty()) {
                                        action = InventoryAction.CLONE_STACK;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                }
                            } else {
                                click = ClickType.UNKNOWN;
                                action = InventoryAction.UNKNOWN;
                            }
                            break;
                        case THROW:
                            if (packet.getSlotNum() >= 0) {
                                if (packet.getButtonNum() == 0) {
                                    click = ClickType.DROP;
                                    Slot slot = this.player.containerMenu.getSlot(packet.getSlotNum());
                                    if (slot != null && slot.hasItem() && slot.mayPickup(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.byBlock(Blocks.AIR)) {
                                        action = InventoryAction.DROP_ONE_SLOT;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                } else if (packet.getButtonNum() == 1) {
                                    click = ClickType.CONTROL_DROP;
                                    Slot slot = this.player.containerMenu.getSlot(packet.getSlotNum());
                                    if (slot != null && slot.hasItem() && slot.mayPickup(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.byBlock(Blocks.AIR)) {
                                        action = InventoryAction.DROP_ALL_SLOT;
                                    } else {
                                        action = InventoryAction.NOTHING;
                                    }
                                }
                            } else {
                                // Sane default (because this happens when they are holding nothing. Don't ask why.)
                                click = ClickType.LEFT;
                                if (packet.getButtonNum() == 1) {
                                    click = ClickType.RIGHT;
                                }
                                action = InventoryAction.NOTHING;
                            }
                            break;
                        case QUICK_CRAFT:
                            this.player.containerMenu.clicked(packet.getSlotNum(), packet.getButtonNum(), packet.getClickType(), this.player);
                            break;
                        case PICKUP_ALL:
                            click = ClickType.DOUBLE_CLICK;
                            action = InventoryAction.NOTHING;
                            if (packet.getSlotNum() >= 0 && !this.player.containerMenu.getCarried().isEmpty()) {
                                ItemStack cursor = this.player.containerMenu.getCarried();
                                action = InventoryAction.NOTHING;
                                // Quick check for if we have any of the item
                                if (inventory.getTopInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem())) || inventory.getBottomInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem()))) {
                                    action = InventoryAction.COLLECT_TO_CURSOR;
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    if (packet.getClickType() != net.minecraft.world.inventory.ClickType.QUICK_CRAFT) {
                        if (click == ClickType.NUMBER_KEY) {
                            event = new InventoryClickEvent(inventory, type, packet.getSlotNum(), click, action, packet.getButtonNum());
                        } else {
                            event = new InventoryClickEvent(inventory, type, packet.getSlotNum(), click, action);
                        }

                        org.bukkit.inventory.Inventory top = inventory.getTopInventory();
                        if (packet.getSlotNum() == 0 && top instanceof CraftingInventory) {
                            org.bukkit.inventory.Recipe recipe = ((CraftingInventory) top).getRecipe();
                            if (recipe != null) {
                                if (click == ClickType.NUMBER_KEY) {
                                    event = new CraftItemEvent(recipe, inventory, type, packet.getSlotNum(), click, action, packet.getButtonNum());
                                } else {
                                    event = new CraftItemEvent(recipe, inventory, type, packet.getSlotNum(), click, action);
                                }
                            }
                        }

                        if (packet.getSlotNum() == 3 && top instanceof SmithingInventory) {
                            org.bukkit.inventory.ItemStack result = ((SmithingInventory) top).getResult();
                            if (result != null) {
                                if (click == ClickType.NUMBER_KEY) {
                                    event = new SmithItemEvent(inventory, type, packet.getSlotNum(), click, action, packet.getButtonNum());
                                } else {
                                    event = new SmithItemEvent(inventory, type, packet.getSlotNum(), click, action);
                                }
                            }
                        }

                        event.setCancelled(cancelled);
                        AbstractContainerMenu oldContainer = this.player.containerMenu; // SPIGOT-1224
                        cserver.getPluginManager().callEvent(event);
                        if (this.player.containerMenu != oldContainer) {
                            return;
                        }

                        switch (event.getResult()) {
                            case ALLOW:
                            case DEFAULT:
                                this.player.containerMenu.clicked(i, packet.getButtonNum(), packet.getClickType(), this.player);
                                break;
                            case DENY:
                                /* Needs enum constructor in InventoryAction
                                if (action.modifiesOtherSlots()) {

                                } else {
                                    if (action.modifiesCursor()) {
                                        this.player.playerConnection.sendPacket(new Packet103SetSlot(-1, -1, this.player.inventory.getCarried()));
                                    }
                                    if (action.modifiesClicked()) {
                                        this.player.playerConnection.sendPacket(new Packet103SetSlot(this.player.activeContainer.windowId, packet102windowclick.slot, this.player.activeContainer.getSlot(packet102windowclick.slot).getItem()));
                                    }
                                }*/
                                switch (action) {
                                    // Modified other slots
                                    case PICKUP_ALL:
                                    case MOVE_TO_OTHER_INVENTORY:
                                    case HOTBAR_MOVE_AND_READD:
                                    case HOTBAR_SWAP:
                                    case COLLECT_TO_CURSOR:
                                    case UNKNOWN:
                                        this.player.containerMenu.sendAllDataToRemote();
                                        break;
                                    // Modified cursor and clicked
                                    case PICKUP_SOME:
                                    case PICKUP_HALF:
                                    case PICKUP_ONE:
                                    case PLACE_ALL:
                                    case PLACE_SOME:
                                    case PLACE_ONE:
                                    case SWAP_WITH_CURSOR:
                                        this.player.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getCarried()));
                                        this.player.connection.send(new ClientboundContainerSetSlotPacket(this.player.containerMenu.containerId, this.player.inventoryMenu.incrementStateId(), packet.getSlotNum(), this.player.containerMenu.getSlot(packet.getSlotNum()).getItem()));
                                        break;
                                    // Modified clicked only
                                    case DROP_ALL_SLOT:
                                    case DROP_ONE_SLOT:
                                        this.player.connection.send(new ClientboundContainerSetSlotPacket(this.player.containerMenu.containerId, this.player.inventoryMenu.incrementStateId(), packet.getSlotNum(), this.player.containerMenu.getSlot(packet.getSlotNum()).getItem()));
                                        break;
                                    // Modified cursor only
                                    case DROP_ALL_CURSOR:
                                    case DROP_ONE_CURSOR:
                                    case CLONE_STACK:
                                        this.player.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, this.player.inventoryMenu.incrementStateId(), this.player.containerMenu.getCarried()));
                                        break;
                                    // Nothing
                                    case NOTHING:
                                        break;
                                }
                        }

                        if (event instanceof CraftItemEvent || event instanceof SmithItemEvent) {
                            // Need to update the inventory on crafting to
                            // correctly support custom recipes
                            player.containerMenu.sendAllDataToRemote();
                        }
                    }
                    // CraftBukkit end

                    for (var entry : Int2ObjectMaps.fastIterable(packet.getChangedSlots())) {
                        this.player.containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), entry.getValue());
                    }

                    this.player.containerMenu.setRemoteCarried(packet.getCarriedItem());
                    this.player.containerMenu.resumeRemoteUpdates();
                    if (flag) {
                        this.player.containerMenu.broadcastFullState();
                    } else {
                        this.player.containerMenu.broadcastChanges();
                    }

                }
            }
        }
    }

    @Inject(method = "handleContainerButtonClick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void banner$noEnchant(ServerboundContainerButtonClickPacket packetIn, CallbackInfo ci) {
        if (player.isImmobile()) {
            ci.cancel();
        }
    }

    private AtomicReference<PlayerRecipeBookClickEvent> banner$recipeClickEvent = new AtomicReference<>();

    @Inject(method = "handlePlaceRecipe",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;"),
            cancellable = true)
    private void banner$recipeClickEvent(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        // CraftBukkit start - implement PlayerRecipeBookClickEvent
        org.bukkit.inventory.Recipe recipe = this.cserver.getRecipe(CraftNamespacedKey.fromMinecraft(packet.getRecipe()));
        if (recipe == null) {
            ci.cancel();
        }
        PlayerRecipeBookClickEvent event =
                CraftEventFactory.callRecipeBookClickEvent(this.player, recipe, packet.isShiftDown());
        banner$recipeClickEvent.set(event);
        // Cast to keyed should be safe as the recipe will never be a MerchantRecipe.
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void handleSetCreativeModeSlot(final ServerboundSetCreativeModeSlotPacket packetplayinsetcreativeslot) {
        PacketUtils.ensureRunningOnSameThread(packetplayinsetcreativeslot, (ServerGamePacketListenerImpl) (Object) this, this.player.serverLevel());
        if (this.player.gameMode.isCreative()) {
            final boolean flag = packetplayinsetcreativeslot.slotNum() < 0;
            ItemStack itemstack = packetplayinsetcreativeslot.itemStack();
            CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);

            if (customdata.contains("x") && customdata.contains("y") && customdata.contains("z") && this.player.getBukkitEntity().hasPermission("minecraft.nbt.copy")) {
                BlockPos blockpos = BlockEntity.getPosFromTag(customdata.getUnsafe());
                if (this.player.level().isLoaded(blockpos)) {
                    BlockEntity blockentity = this.player.level().getBlockEntity(blockpos);
                    if (blockentity != null) {
                        blockentity.saveToItem(itemstack, this.player.level().registryAccess());
                    }
                }
            }
            final boolean flag2 = packetplayinsetcreativeslot.slotNum() >= 1 && packetplayinsetcreativeslot.slotNum() <= 45;
            boolean flag3 = itemstack.isEmpty() || itemstack.getCount() <= itemstack.getMaxStackSize();
            if (flag || (flag2 && !ItemStack.matches(this.player.inventoryMenu.getSlot(packetplayinsetcreativeslot.slotNum()).getItem(), packetplayinsetcreativeslot.itemStack()))) {
                final InventoryView inventory =  this.player.inventoryMenu.getBukkitView();
                final org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(packetplayinsetcreativeslot.itemStack());
                InventoryType.SlotType type = InventoryType.SlotType.QUICKBAR;
                if (flag) {
                    type = InventoryType.SlotType.OUTSIDE;
                } else if (packetplayinsetcreativeslot.slotNum() < 36) {
                    if (packetplayinsetcreativeslot.slotNum() >= 5 && packetplayinsetcreativeslot.slotNum() < 9) {
                        type = InventoryType.SlotType.ARMOR;
                    } else {
                        type = InventoryType.SlotType.CONTAINER;
                    }
                }
                final InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type, flag ? -999 : packetplayinsetcreativeslot.slotNum(), item);
                this.cserver.getPluginManager().callEvent(event);
                itemstack = CraftItemStack.asNMSCopy(event.getCursor());
                switch (event.getResult()) {
                    case ALLOW: {
                        flag3 = true;
                        break;
                    }
                    case DEFAULT:
                        break;
                    case DENY: {
                        if (packetplayinsetcreativeslot.slotNum() >= 0) {
                            this.player.connection.send(new ClientboundContainerSetSlotPacket(this.player.inventoryMenu.containerId, this.player.inventoryMenu.incrementStateId(), packetplayinsetcreativeslot.slotNum(), this.player.inventoryMenu.getSlot(packetplayinsetcreativeslot.slotNum()).getItem()));
                            this.player.connection.send(new ClientboundContainerSetSlotPacket(-1, this.player.inventoryMenu.incrementStateId(), -1, ItemStack.EMPTY));
                        }
                        return;
                    }
                }
            }
            if (flag2 && flag3) {
                this.player.inventoryMenu.getSlot(packetplayinsetcreativeslot.slotNum()).setByPlayer(itemstack);
                this.player.inventoryMenu.broadcastChanges();
            } else if (flag && flag3 && this.dropSpamTickCount < 200) {
                this.dropSpamTickCount += 20;
                this.player.drop(itemstack, true);
            }
        }
    }

    @Redirect(method = "method_17820",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/RecipeBookMenu;handlePlacement(ZLnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/server/level/ServerPlayer;)V"))
    private <C extends Container> void banner$recipeClickEvent0(RecipeBookMenu instance, boolean bl, RecipeHolder<?> recipeHolder, ServerPlayer serverPlayer) {
        ((RecipeBookMenu)this.player.containerMenu).handlePlacement(banner$recipeClickEvent.get().isShiftClick(), recipeHolder, this.player);
    }

    @Inject(method = "updateSignText", cancellable = true, at = @At("HEAD"))
    private void banner$updateSignText(ServerboundSignUpdatePacket packet, List<FilteredText> filteredText, CallbackInfo ci) {
        if (player.isImmobile()) {
            ci.cancel();
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, this.player.serverLevel());
        if (this.player.getAbilities().mayfly && this.player.getAbilities().flying != packet.isFlying()) {
            PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(getCraftPlayer(), packet.isFlying());
            this.cserver.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.player.getAbilities().flying = packet.isFlying();
            } else {
                this.player.onUpdateAbilities();
            }
        }
    }

    private transient PlayerTeleportEvent.TeleportCause banner$cause;

    private transient boolean banner$noTeleportEvent;
    private transient boolean banner$teleportCancelled;

    @Decorate(method = "teleport(DDDFFLjava/util/Set;)V", inject = true, at = @At("HEAD"))
    private void banner$teleportEvent(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet) throws Throwable {
        PlayerTeleportEvent.TeleportCause cause = banner$cause == null ? PlayerTeleportEvent.TeleportCause.UNKNOWN : banner$cause;
        banner$cause = null;
        Player player = this.getCraftPlayer();
        Location from = player.getLocation();
        Location to = new Location(this.getCraftPlayer().getWorld(), x, y, z, yaw, pitch);
        if (!banner$noTeleportEvent && !from.equals(to)) {
            PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), cause);
            this.cserver.getPluginManager().callEvent(event);
            if (event.isCancelled() || !to.equals(event.getTo())) {
                relativeSet.clear();
                to = (event.isCancelled() ? event.getFrom() : event.getTo());
                x = to.getX();
                y = to.getY();
                z = to.getZ();
                yaw = to.getYaw();
                pitch = to.getPitch();
            }
            banner$teleportCancelled = event.isCancelled();
        } else {
            banner$teleportCancelled = false;
        }
        banner$noTeleportEvent = false;

        if (Float.isNaN(yaw)) {
            yaw = 0.0f;
        }
        if (Float.isNaN(pitch)) {
            pitch = 0.0f;
        }
        this.justTeleported = true;
        DecorationOps.blackhole().invoke(x, y, z, yaw, pitch);
    }

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;awaitingTeleportTime:I"))
    private void banner$storeLastPosition(double d, double e, double f, float yaw, float pitch, Set<RelativeMovement> set, CallbackInfo ci) {
        this.lastPosX = this.awaitingPositionFromClient.x;
        this.lastPosY = this.awaitingPositionFromClient.y;
        this.lastPosZ = this.awaitingPositionFromClient.z;
        this.lastYaw = yaw;
        this.lastPitch = pitch;
    }

    @Override
    public void teleport(double d0, double d1, double d2, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
        this.teleport(d0, d1, d2, f, f1, Collections.emptySet(), cause);
    }

    @Override
    public boolean teleport(double d0, double d1, double d2, float f, float f1, Set<RelativeMovement> set, PlayerTeleportEvent.TeleportCause cause) {
        cause = banner$cause == null ? PlayerTeleportEvent.TeleportCause.UNKNOWN : banner$cause;
        banner$cause = null;
        org.bukkit.entity.Player player = this.getCraftPlayer();
        Location from = player.getLocation();

        double x = d0;
        double y = d1;
        double z = d2;
        float yaw = f;
        float pitch = f1;

        Location to = new Location(this.getCraftPlayer().getWorld(), x, y, z, yaw, pitch);
        // SPIGOT-5171: Triggered on join
        if (from.equals(to)) {
            this.internalTeleport(d0, d1, d2, f, f1, set);
            return false; // CraftBukkit - Return event status
        }

        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), cause);
        this.cserver.getPluginManager().callEvent(event);

        if (event.isCancelled() || !to.equals(event.getTo())) {
            set.clear(); // Can't relative teleport
            to = event.isCancelled() ? event.getFrom() : event.getTo();
            d0 = to.getX();
            d1 = to.getY();
            d2 = to.getZ();
            f = to.getYaw();
            f1 = to.getPitch();
        }

        this.internalTeleport(d0, d1, d2, f, f1, set);
        return event.isCancelled(); // CraftBukkit - Return event status
    }

    @Override
    public void teleport(Location dest) {
        banner$noTeleportEvent = true;
        this.teleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch(), Collections.emptySet());
        banner$noTeleportEvent = false;
    }

    @Override
    public void bridge$pushNoTeleportEvent() {
        banner$noTeleportEvent = true;
    }

    @Inject(method = "teleport(DDDFF)V", at = @At("HEAD"))
    private void banner$tpBukkit(double d, double e, double f, float g, float h, CallbackInfo ci) {
        pushTeleportCause(PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    @Override
    public void internalTeleport(double pX, double pY, double pZ, float pYaw, float pPitch, Set<RelativeMovement> pRelativeSet) {
        // CraftBukkit start
        if (Float.isNaN(pYaw)) {
            pYaw = 0;
        }
        if (Float.isNaN(pPitch)) {
            pPitch = 0;
        }

        this.justTeleported = true;
        // CraftBukkit end
        double d0 = pRelativeSet.contains(RelativeMovement.X) ? this.player.getX() : 0.0D;
        double d1 = pRelativeSet.contains(RelativeMovement.Y) ? this.player.getY() : 0.0D;
        double d2 = pRelativeSet.contains(RelativeMovement.Z) ? this.player.getZ() : 0.0D;
        float f = pRelativeSet.contains(RelativeMovement.Y_ROT) ? this.player.getYRot() : 0.0F;
        float f1 = pRelativeSet.contains(RelativeMovement.X_ROT) ? this.player.getXRot() : 0.0F;
        this.awaitingPositionFromClient = new Vec3(pX, pY, pZ);
        if (++this.awaitingTeleport == Integer.MAX_VALUE) {
            this.awaitingTeleport = 0;
        }

        this.awaitingTeleportTime = this.tickCount;
        this.player.absMoveTo(pX, pY, pZ, pYaw, pPitch);
        this.player.connection.send(new ClientboundPlayerPositionPacket(pX - d0, pY - d1, pZ - d2, pYaw - f, pPitch - f1, pRelativeSet, this.awaitingTeleport));
    }

    @Override
    public boolean bridge$processedDisconnect() {
        return processedDisconnect;
    }

    @Override
    public CraftServer bridge$craftServer() {
        return cserver;
    }

    @Override
    public Logger bridge$logger() {
        return LOGGER;
    }

    @Override
    public void pushTeleportCause(PlayerTeleportEvent.TeleportCause cause) {
        banner$cause = cause;
    }

    @Override
    public boolean bridge$teleportCancelled() {
        return banner$teleportCancelled;
    }
}
