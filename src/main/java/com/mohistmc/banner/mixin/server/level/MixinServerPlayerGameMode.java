package com.mohistmc.banner.mixin.server.level;

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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    @Shadow
    public abstract boolean isCreative();

    @Shadow
    private BlockPos destroyPos;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract GameType getGameModeForPlayer();

    // CraftBukkit start - whole method
    public boolean interactResult = false;
    public boolean firedInteract = false;
    public BlockPos interactPosition;
    public InteractionHand interactHand;
    public ItemStack interactItemStack;

    private AtomicReference<PlayerInteractEvent> banner$actEvent = new AtomicReference<>();
    private AtomicReference<BlockPos> banner$pos = new AtomicReference<>();
    private AtomicReference<BlockPos> banner$destroyPos = new AtomicReference<>();
    private AtomicReference<org.bukkit.block.Block> banner$bbBlock = new AtomicReference<>();
    private AtomicReference<Boolean> banner$flag1 = new AtomicReference<>();
    private AtomicReference<Boolean> banner$flag2 = new AtomicReference<>();
    private AtomicReference<BlockBreakEvent> banner$breakEvent = new AtomicReference<>();
    private AtomicReference<BlockState> banner$state = new AtomicReference<>();
    private AtomicReference<org.bukkit.block.BlockState> banner$bbState = new AtomicReference<>();
    private AtomicReference<Boolean> cancelledBlock = new AtomicReference<>();
    private AtomicReference<InteractionResult> banner$result = new AtomicReference<>();

    @Inject(method = "changeGameModeForPlayer", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V"))
    private void banner$gameModeEvent(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(((ServerPlayer) player).getBukkitEntity(), GameMode.getByValue(gameType.getId()));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;mayInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)Z"))
    private void banner$callEvent0(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci) {
        // CraftBukkit start - fire PlayerInteractEvent
        banner$pos.set(pos);
        CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, pos, face, this.player.getInventory().getSelected(), InteractionHand.MAIN_HAND);
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;debugLogging(Lnet/minecraft/core/BlockPos;ZILjava/lang/String;)V", ordinal = 1))
    private void banner$checkBlockEntity(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci) {
        // Update any tile entity data for this block
        BlockEntity tileentity = level.getBlockEntity(pos);
        if (tileentity != null) {
            this.player.connection.send(tileentity.getUpdatePacket());
        }
        // CraftBukkit end
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z", shift = At.Shift.BEFORE), cancellable = true)
    private void banner$callActEvent(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci) {
        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, pos, face, this.player.getInventory().getSelected(), InteractionHand.MAIN_HAND);
        banner$actEvent.set(event);
        if (event.isCancelled()) {
            // Let the client know the block still exists
            BlockEntity tileentity = this.level.getBlockEntity(pos);
            if (tileentity != null) {
                this.player.connection.send(tileentity.getUpdatePacket());
            }
            ci.cancel();
        }
        // CraftBukkit end
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$callEvent0(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci, float f, BlockState blockState) {
        // CraftBukkit start - Swings at air do *NOT* exist.
        if (banner$actEvent.get().useInteractedBlock() == Event.Result.DENY) {
            // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
            BlockState data = this.level.getBlockState(pos);
            if (data.getBlock() instanceof DoorBlock) {
                // For some reason *BOTH* the bottom/top part have to be marked updated.
                boolean bottom = data.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, bottom ? pos.above() : pos.below()));
            } else if (data.getBlock() instanceof TrapDoorBlock) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
            } else if (!blockState.isAir()) {
                blockState.attack(this.level, pos, this.player);
                f = blockState.getDestroyProgress(this.player, this.player.level(), pos);
            }
        }
    }

    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;attack(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"))
    private void banner$cancelAttack(BlockState instance, Level level, BlockPos pos, Player player) {}

    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F", ordinal = 0))
    private float banner$cancelProgress(BlockState instance, Player player, BlockGetter blockGetter, BlockPos pos) {
        return 0;
    }

    @Inject(method = "handleBlockBreakAction",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z",
                    ordinal = 1,
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$fireEvent(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci, float f, BlockState blockState) {
        if (banner$actEvent.get().useItemInHand() == Event.Result.DENY) {
            // If we 'insta destroyed' then the client needs to be informed.
            if (f > 1.0f) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
            }
            ci.cancel();
        }

        BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.player, pos, this.player.getInventory().getSelected(), f >= 1.0f);
        if (blockEvent.isCancelled()) {
            // Let the client know the block still exists
            this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
            ci.cancel();
        }

        if (blockEvent.getInstaBreak()) {
            f = 2.0f;
        }
        // CraftBukkit end
    }

    @Redirect(method = "handleBlockBreakAction", remap = false, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void banner$resetLogger(Logger instance, String s, Object o1, Object o2) {
        LOGGER.debug("Mismatch in destroy block pos: {} {}", this.destroyPos, banner$pos.get());
    }

    @Inject(method = "handleBlockBreakAction", at = @At("TAIL"))
    private void banner$fireEvent0(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci) {
        CraftEventFactory.callBlockDamageAbortEvent(this.player, pos, this.player.getInventory().getSelected()); // CraftBukkit
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void banner$fireEvent1(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        banner$destroyPos.set(pos);
        // CraftBukkit start - fire BlockBreakEvent
        org.bukkit.block.Block bblock = CraftBlock.at(level, pos);
        banner$bbBlock.set(bblock);
        BlockBreakEvent event = null;

        if (this.player instanceof ServerPlayer) {
            // Sword + Creative mode pre-cancel
            boolean isSwordNoBreak = !this.player.getMainHandItem().getItem().canAttackBlock(this.level.getBlockState(pos), this.level, pos, this.player);

            // Tell client the block is gone immediately then process events
            // Don't tell the client if its a creative sword break because its not broken!
            if (level.getBlockEntity(pos) == null && !isSwordNoBreak) {
                ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(pos, Blocks.AIR.defaultBlockState());
                this.player.connection.send(packet);
            }

            event = new BlockBreakEvent(bblock, this.player.getBukkitEntity());

            // Sword + Creative mode pre-cancel
            event.setCancelled(isSwordNoBreak);

            // Calculate default block experience
            BlockState nmsData = this.level.getBlockState(pos);
            Block nmsBlock = nmsData.getBlock();

            ItemStack itemstack = this.player.getItemBySlot(EquipmentSlot.MAINHAND);

            if (nmsBlock != null && !event.isCancelled() && !this.isCreative() && this.player.hasCorrectToolForDrops(nmsBlock.defaultBlockState())) {
                event.setExpToDrop(nmsBlock.getExpDrop(nmsData, this.level, pos, itemstack, true));
            }

            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                if (isSwordNoBreak) {
                    cir.setReturnValue(false);
                }
                // Let the client know the block still exists
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
            }

            // Brute force all possible updates
            for (Direction dir : Direction.values()) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(level, pos.relative(dir)));
            }

            // Update any tile entity data for this block
            BlockEntity tileentity = this.level.getBlockEntity(pos);
            if (tileentity != null) {
                this.player.connection.send(tileentity.getUpdatePacket());
            }
            cir.setReturnValue(false);
        }
        banner$breakEvent.set(event);
        // CraftBukkit end
    }

    @Redirect(method = "destroyBlock",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;canAttackBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean banner$addFalseCheck(Item instance, BlockState state, Level level, BlockPos pos, Player player) {
        return false && !this.player.getMainHandItem().getItem().canAttackBlock(level.getBlockState(pos), this.level, pos, this.player);
    }

    @Inject(method = "destroyBlock",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$addBlockEntityCheck(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState) {
        blockState = this.level.getBlockState(pos); // CraftBukkit - update state from plugins
        if (blockState.isAir()) cir.setReturnValue(false); // CraftBukkit - A plugin set block to air without cancelling
    }

    @Inject(method = "destroyBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V",
                    shift = At.Shift.BEFORE))
    private void banner$addCapture(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        org.bukkit.block.BlockState state = banner$bbBlock.get().getState();
        banner$bbState.set(state);
        level.banner$setCaptureDrops(new ArrayList<>());
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$caputureFlag(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockState, BlockEntity blockEntity, Block block, boolean bl, ItemStack itemStack, ItemStack itemStack2, boolean bl2) {
        banner$flag1.set(bl);
        banner$flag2.set(bl2);
    }
    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$wrapCheck(Block instance, Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        banner$state.set(state);
        if (banner$flag1.get() && banner$flag2.get() && banner$breakEvent.get().isDropItems()) {
            state.getBlock().playerDestroy(this.level, this.player, pos, state, blockEntity, tool);
        }
    }

    @Inject(method = "destroyBlock", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$checkDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        if (banner$breakEvent.get().isDropItems()) {
            CraftEventFactory.handleBlockDropItemEvent(banner$bbBlock.get(), banner$bbState.get(), this.player, level.bridge$captureDrops());
        }
        level.banner$setCaptureDrops(null);

        // Drop event experience
        if (banner$flag1.get() && banner$flag2.get() != null) {
            banner$state.get().getBlock().popExperience(this.level, pos, banner$breakEvent.get().getExpToDrop());
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void banner$postEvent(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult enuminteractionresult = InteractionResult.PASS;
        banner$result.set(enuminteractionresult);
        cancelledBlock.set(false);
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getMenuProvider(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/MenuProvider;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$postEvent0(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir, BlockPos blockPos, BlockState blockState) {
        cancelledBlock.set(!(blockState.getMenuProvider(level, blockPos) instanceof MenuProvider));

        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            cancelledBlock.set(true);
        }

        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, blockPos, hitResult.getDirection(), stack, cancelledBlock.get(), hand);
        firedInteract = true;
        interactResult = event.useItemInHand() == Event.Result.DENY;
        interactPosition = blockPos.immutable();
        interactHand = hand;
        interactItemStack = stack.copy();

        if (event.useInteractedBlock() == Event.Result.DENY) {
            // If we denied a door from opening, we need to send a correcting update to the client, as it already opened the door.
            if (blockState.getBlock() instanceof DoorBlock) {
                boolean bottom = blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                player.connection.send(new ClientboundBlockUpdatePacket(level, bottom ? blockPos.above() : blockPos.below()));
            } else if (blockState.getBlock() instanceof CakeBlock) {
                player.getBukkitEntity().sendHealthUpdate(); // SPIGOT-1341 - reset health for cake
            } else if (interactItemStack.getItem() instanceof DoubleHighBlockItem) {
                // send a correcting update to the client, as it already placed the upper half of the bisected item
                player.connection.send(new ClientboundBlockUpdatePacket(level, blockPos.relative(hitResult.getDirection()).above()));

                // send a correcting update to the client for the block above as well, this because of replaceable blocks (such as grass, sea grass etc)
                player.connection.send(new ClientboundBlockUpdatePacket(level, blockPos.above()));
            }
            player.getBukkitEntity().updateInventory(); // SPIGOT-2867
            banner$result.set(event.useItemInHand() != Event.Result.ALLOW ? InteractionResult.SUCCESS : InteractionResult.PASS);
        } else if (this.getGameModeForPlayer() == GameType.SPECTATOR) {
            MenuProvider menuProvider = blockState.getMenuProvider(level, blockPos);
            cancelledBlock.set(!(menuProvider instanceof MenuProvider));
        }
    }

    @Redirect(method = "changeGameModeForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$changeMessage(PlayerList instance, Packet<?> packet) {
        this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this.player), this.player);
    }
}
