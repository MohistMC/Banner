package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.BukkitCaptures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Shadow @Final protected ServerPlayer player;

    @Shadow protected ServerLevel level;

    @Shadow public abstract boolean isCreative();

    @Shadow protected abstract void debugLogging(BlockPos blockPos, boolean bl, int i, String string);

    @Shadow public abstract void destroyAndAck(BlockPos pos, int i, String string);

    @Shadow private GameType gameModeForPlayer;

    @Shadow private int destroyProgressStart;

    @Shadow private int gameTicks;

    @Shadow private boolean isDestroyingBlock;

    @Shadow private BlockPos destroyPos;

    @Shadow private int lastSentState;

    @Shadow private boolean hasDelayedDestroy;

    @Shadow private BlockPos delayedDestroyPos;

    @Shadow private int delayedTickStart;

    @Inject(method = "changeGameModeForPlayer", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V"))
    private void banner$gameModeEvent(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(((ServerPlayer) player).getBukkitEntity(), GameMode.getByValue(gameType.getId()));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void handleBlockBreakAction(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j) {
        if (!this.level.hasChunkAt(blockPos)) {
            return;
        }
        if ((!this.isCreative())) { // Restore block and te data
            level.sendBlockUpdated(blockPos, level.getBlockState(blockPos), level.getBlockState(blockPos), 3);
            return;
        }
        if (!(blockPos.getY() >= level.getMaxBuildHeight())) { // Vanilla check is eye-to-center distance < 6, so padding is 6 - 4.5 = 1.5
            this.debugLogging(blockPos, false, j, "too far");
        } else if (blockPos.getY() >= i) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
            this.debugLogging(blockPos, false, j, "too high");
        } else if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            if (!this.level.mayInteract(this.player, blockPos)) {
                CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockPos, direction, this.player.getInventory().getSelected(), InteractionHand.MAIN_HAND);
                this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
                this.debugLogging(blockPos, false, j, "may not interact");
                BlockEntity tileentity = this.level.getBlockEntity(blockPos);
                if (tileentity != null) {
                    this.player.connection.send(tileentity.getUpdatePacket());
                }
                return;
            }
            PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockPos, direction, this.player.getInventory().getSelected(), InteractionHand.MAIN_HAND);
            if (event.isCancelled()) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
                BlockEntity tileentity2 = this.level.getBlockEntity(blockPos);
                if (tileentity2 != null) {
                    this.player.connection.send(tileentity2.getUpdatePacket());
                }
                return;
            }
            if (this.isCreative()) {
                this.destroyAndAck(blockPos, j, "creative destroy");
                return;
            }
            // Spigot start - handle debug stick left click for non-creative
            if (this.player.getMainHandItem().is(net.minecraft.world.item.Items.DEBUG_STICK)
                    && ((net.minecraft.world.item.DebugStickItem) net.minecraft.world.item.Items.DEBUG_STICK).handleInteraction(this.player, this.level.getBlockState(blockPos), this.level, blockPos, false, this.player.getMainHandItem())) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
                return;
            }
            // Spigot end
            if (this.player.blockActionRestricted(this.level, blockPos, this.gameModeForPlayer)) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
                this.debugLogging(blockPos, false, j, "block action restricted");
                return;
            }
            this.destroyProgressStart= this.gameTicks;
            float f = 1.0f;
            BlockState iblockdata = this.level.getBlockState(blockPos);
            if (event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) {
                BlockState data = this.level.getBlockState(blockPos);
                if (data.getBlock() instanceof DoorBlock) {
                    boolean bottom = data.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                    this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
                    this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, bottom ? blockPos.above() : blockPos.below()));
                } else if (data.getBlock() instanceof TrapDoorBlock) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
                }
                if (!iblockdata.isAir()) {
                    iblockdata.attack(this.level, blockPos, this.player);
                    f = iblockdata.getDestroyProgress(this.player, this.player.level, blockPos);
                }
                if (!iblockdata.isAir() && f >= 1.0F) {
                    this.destroyAndAck(blockPos, j, "insta mine");
                }
            }
            BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.player, blockPos, this.player.getInventory().getSelected(), f >= 1.0f);
            if (blockEvent.isCancelled()) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
                return;
            }
            if (blockEvent.getInstaBreak()) {
                f = 2.0f;
            }
            if (!iblockdata.isAir() && f >= 1.0f) {
                this.destroyAndAck(blockPos, j, "insta mine");
            } else {
                if (this.isDestroyingBlock) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                    this.debugLogging(blockPos, false, j, "abort destroying since another started (client insta mine, server disagreed)");
                }
                this.isDestroyingBlock = true;
                this.destroyPos = blockPos;
                int state = (int) (f * 10.0f);
                this.level.destroyBlockProgress(this.player.getId(), blockPos, state);
                this.debugLogging(blockPos, true, j, "actual start of destroying");
                CraftEventFactory.callBlockDamageAbortEvent(this.player, blockPos, this.player.getInventory().getSelected());
                this.lastSentState = state;
            }
        } else if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            if (blockPos.equals(this.destroyPos)) {
                int k = this.gameTicks - this.destroyProgressStart;
                BlockState iblockdata = this.level.getBlockState(blockPos);
                if (!iblockdata.isAir()) {
                    float f2 = iblockdata.getDestroyProgress(this.player, this.player.level, blockPos) * (k + 1);
                    if (f2 >= 0.7f) {
                        this.isDestroyingBlock = false;
                        this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
                        this.destroyAndAck(blockPos, j, "destroyed");
                        return;
                    }
                    if (!this.hasDelayedDestroy) {
                        this.isDestroyingBlock = false;
                        this.hasDelayedDestroy = true;
                        this.delayedDestroyPos = blockPos;
                        this.delayedTickStart = this.destroyProgressStart;
                    }
                }
            }
            this.debugLogging(blockPos, true, j, "stopped destroying");
        } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            this.isDestroyingBlock = false;
            if (!Objects.equals(this.destroyPos, blockPos)) {
                BannerServer.LOGGER.debug("Mismatch in destroy block pos: " + this.destroyPos + " " + blockPos);
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.debugLogging(blockPos, true, j, "aborted mismatched destroying");
            }
            this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
            this.debugLogging(blockPos, true, j, "aborted destroying");
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    public void banner$resetBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BukkitCaptures.BlockBreakEventContext breakEventContext = BukkitCaptures.popPrimaryBlockBreakEvent();

        if (breakEventContext != null) {
            handleBlockDrop(breakEventContext, pos);
        }
    }

    @Inject(method = {"tick", "destroyAndAck"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    public void banner$clearCaptures(CallbackInfo ci) {
        // clear the event stack in case that interrupted events are left here unhandled
        // it should be a new event capture session each time destroyBlock is called from these two contexts
        BukkitCaptures.clearBlockBreakEventContexts();
    }

    private void handleBlockDrop(BukkitCaptures.BlockBreakEventContext breakEventContext, BlockPos pos) {
        BlockBreakEvent breakEvent = breakEventContext.getEvent();
        List<ItemEntity> blockDrops = breakEventContext.getBlockDrops();
        org.bukkit.block.BlockState state = breakEventContext.getBlockBreakPlayerState();

        if (blockDrops != null && (breakEvent == null || breakEvent.isDropItems())) {
            CraftBlock craftBlock = CraftBlock.at(this.level, pos);
            CraftEventFactory.handleBlockDropItemEvent(craftBlock, state, this.player, blockDrops);
        }
    }

    @Redirect(method = "changeGameModeForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$changeMessage(PlayerList instance, Packet<?> packet) {
        this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this.player), this.player);
    }
}
