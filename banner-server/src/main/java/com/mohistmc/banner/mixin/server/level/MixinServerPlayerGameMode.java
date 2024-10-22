package com.mohistmc.banner.mixin.server.level;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mohistmc.banner.injection.server.level.InjectionServerPlayerGameMode;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode implements InjectionServerPlayerGameMode {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    @Shadow
    public abstract boolean isCreative();

    @Shadow
    protected abstract void debugLogging(BlockPos blockPos, boolean bl, int i, String string);

    @Shadow
    private GameType gameModeForPlayer;
    @Shadow
    private int destroyProgressStart;
    @Shadow
    private int gameTicks;
    @Shadow
    private boolean isDestroyingBlock;
    @Shadow
    private BlockPos destroyPos;
    @Shadow
    private int lastSentState;
    @Shadow
    private boolean hasDelayedDestroy;
    @Shadow
    private BlockPos delayedDestroyPos;
    @Shadow
    private int delayedTickStart;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow public abstract boolean destroyBlock(BlockPos pos);

    @Shadow public abstract void destroyAndAck(BlockPos pos, int i, String string);

    @Inject(method = "changeGameModeForPlayer", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V"))
    private void banner$gameModeEvent(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(((ServerPlayer) player).getBukkitEntity(), GameMode.getByValue(gameType.getId()));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "changeGameModeForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void banner$changeMessage(PlayerList instance, Packet<?> packet) {
        this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this.player), this.player);
    }

    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;mayInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)Z")))
    private void banner$mayNotInteractEvent(ServerGamePacketListenerImpl instance, Packet<?> packet, BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction) throws Throwable {
        CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockPos, direction, this.player.getInventory().getSelected(), InteractionHand.MAIN_HAND);
        DecorationOps.callsite().invoke(instance, packet);
        BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
        if (blockEntity != null) {
            this.player.connection.send(blockEntity.getUpdatePacket());
        }
    }

    @Decorate(method = "handleBlockBreakAction", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z"))
    private void banner$interactEvent(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction,
                                      @io.izzel.arclight.mixin.Local(allocate = "playerInteractEvent") PlayerInteractEvent event) throws Throwable {
        event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockPos, direction, this.player.getInventory().getSelected(), InteractionHand.MAIN_HAND);
        if (event.isCancelled()) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
            BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                this.player.connection.send(blockEntity.getUpdatePacket());
            }
            DecorationOps.cancel().invoke();
            return;
        }
        DecorationOps.blackhole().invoke();
    }

    @Decorate(method = "handleBlockBreakAction", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean banner$playerInteractCancelled(BlockState instance, BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction,
                                                   @io.izzel.arclight.mixin.Local(allocate = "playerInteractEvent") PlayerInteractEvent event) throws Throwable {
        boolean result = false;
        if (event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) {
            BlockState data = this.level.getBlockState(blockPos);
            if (data.getBlock() instanceof DoorBlock) {
                boolean bottom = data.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, bottom ? blockPos.above() : blockPos.below()));
            } else if (data.getBlock() instanceof TrapDoorBlock) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
            }
            result = true;
        } else {
            result = (boolean) DecorationOps.callsite().invoke(instance);
        }
        return result;
    }

    @Decorate(method = "handleBlockBreakAction", inject = true, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private void banner$blockDamageEvent(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction,
                                         @io.izzel.arclight.mixin.Local(ordinal = -1) float f,
                                         @io.izzel.arclight.mixin.Local(allocate = "playerInteractEvent") PlayerInteractEvent event) throws Throwable {
        if (event.useItemInHand() == Event.Result.DENY) {
            if (f > 1.0f) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
            }
            return;
        }
        BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.player, blockPos, this.player.getInventory().getSelected(), f >= 1.0f);
        if (blockEvent.isCancelled()) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockPos));
            return;
        }
        if (blockEvent.getInstaBreak()) {
            f = 2.0f;
        }
        DecorationOps.blackhole().invoke(f);
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "CONSTANT", args = "stringValue=aborted destroying"))
    private void banner$abortBlockBreak(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
        CraftEventFactory.callBlockDamageAbortEvent(this.player, blockPos, this.player.getInventory().getSelected());
    }

    private final AtomicReference<BlockBreakEvent> banner$event = new AtomicReference<>();

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void banner$fireBreakEvent(BlockPos blockposition, CallbackInfoReturnable<Boolean> cir) {
        BlockState iblockdata = this.level.getBlockState(blockposition);
        // CraftBukkit start - fire BlockBreakEvent
        org.bukkit.block.Block bblock = CraftBlock.at(level, blockposition);
        BlockBreakEvent event = null;

        if (this.player instanceof ServerPlayer) {
            // Sword + Creative mode pre-cancel
            boolean isSwordNoBreak = !this.player.getMainHandItem().getItem().canAttackBlock(iblockdata, this.level, blockposition, this.player);

            // Tell client the block is gone immediately then process events
            // Don't tell the client if its a creative sword break because its not broken!
            if (level.getBlockEntity(blockposition) == null && !isSwordNoBreak) {
                ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(blockposition, Blocks.AIR.defaultBlockState());
                this.player.connection.send(packet);
            }

            event = new BlockBreakEvent(bblock, this.player.getBukkitEntity());
            banner$event.set(event);

            // Sword + Creative mode pre-cancel
            event.setCancelled(isSwordNoBreak);

            // Calculate default block experience
            BlockState nmsData = this.level.getBlockState(blockposition);
            Block nmsBlock = nmsData.getBlock();

            ItemStack itemstack = this.player.getItemBySlot(EquipmentSlot.MAINHAND);

            if (nmsBlock != null && !event.isCancelled() && !this.isCreative() && this.player.hasCorrectToolForDrops(nmsBlock.defaultBlockState())) {
                event.setExpToDrop(nmsBlock.getExpDrop(nmsData, this.level, blockposition, itemstack, true));
            }

            this.level.getCraftServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                if (isSwordNoBreak) {
                    cir.setReturnValue(false);
                    return;
                }
                // Let the client know the block still exists
                this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, blockposition));

                // Brute force all possible updates
                for (Direction dir : Direction.values()) {
                    this.player.connection.send(new ClientboundBlockUpdatePacket(level, blockposition.relative(dir)));
                }

                // Update any tile entity data for this block
                BlockEntity tileentity = this.level.getBlockEntity(blockposition);
                if (tileentity != null) {
                    this.player.connection.send(tileentity.getUpdatePacket());
                }
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;",
            shift = At.Shift.BEFORE))
    private void banner$setDrops(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        level.banner$setCaptureDrops(new ArrayList<>());
    }

    @Inject(method = "destroyBlock", at = @At("TAIL"), cancellable = true)
    private void banner$fireDropEvent(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        org.bukkit.block.BlockState state = CraftBlock.at(level, pos).getState();
        if (level.bridge$captureDrops() != null && banner$event.get().isDropItems()) {
            CraftEventFactory.handleBlockDropItemEvent(CraftBlock.at(level, pos), state, this.player, level.bridge$captureDrops());
        }
        level.banner$setCaptureDrops(null);

        // Drop event experience
        if (this.level.removeBlock(pos, false) && banner$event.get() != null) {
            this.level.getBlockState(pos).getBlock().popExperience(this.level, pos, banner$event.getAndSet(null).getExpToDrop());
        }
        cir.setReturnValue(true);
    }

    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canAttackBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean banner$addFalse(Item instance, BlockState state, Level level, BlockPos pos, Player player) {
        return true && this.player.getMainHandItem().getItem().canAttackBlock(state, this.level, pos, this.player);
    }

    @Inject(method = "destroyBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"), cancellable = true)
    private void banner$resetState(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState blockState) {
        blockState = this.level.getBlockState(pos); // CraftBukkit - update state from plugins
        if (blockState.isAir()) cir.setReturnValue(false); // CraftBukkit - A plugin set block to air without cancelling
    }

    // CraftBukkit start - whole method
    public boolean interactResult = false;
    public boolean firedInteract = false;
    public BlockPos interactPosition;
    public InteractionHand interactHand;
    public ItemStack interactItemStack;

    /**
     * @author wdog4
     * @reason
     */
    @Overwrite
    public InteractionResult useItemOn(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        InteractionResult enuminteractionresult = InteractionResult.PASS;
        boolean cancelledBlock = false;
        if (!blockState.getBlock().isEnabled(level.enabledFeatures())) {
            return InteractionResult.FAIL;
        } else if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider menuProvider = blockState.getMenuProvider(level, blockPos);
            cancelledBlock = !(menuProvider instanceof MenuProvider);
        }
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            cancelledBlock = true;
        }

        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, blockPos, hitResult.getDirection(), stack, cancelledBlock, hand, hitResult.getLocation());
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
            enuminteractionresult = (event.useItemInHand() != Event.Result.ALLOW) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else if (this.gameModeForPlayer == GameType.SPECTATOR) {
            MenuProvider menuProvider = blockState.getMenuProvider(level, blockPos);
            if (menuProvider != null) {
                player.openMenu(menuProvider);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        } else {
            boolean bl = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
            boolean bl2 = player.isSecondaryUseActive() && bl;
            ItemStack itemStack = stack.copy();
            if (!bl2) {
                ItemInteractionResult result = blockState.useItemOn(player.getItemInHand(hand), level, player, hand, hitResult);
                if (result.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockPos, itemStack);
                    return result.result();
                }

                if (result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == InteractionHand.MAIN_HAND) {
                    enuminteractionresult = blockState.useWithoutItem(level, player, hitResult);
                    if (enuminteractionresult.consumesAction()) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockPos, itemStack);
                        return enuminteractionresult;
                    }
                }
            }

            if (!stack.isEmpty() && enuminteractionresult != InteractionResult.SUCCESS && !interactResult) { // add !interactResult SPIGOT-764
                UseOnContext useOnContext = new UseOnContext(player, hand, hitResult);
                InteractionResult interactionResult2;
                if (this.isCreative()) {
                    int i = stack.getCount();
                    interactionResult2 = stack.useOn(useOnContext);
                    stack.setCount(i);
                } else {
                    interactionResult2 = stack.useOn(useOnContext);
                }

                if (interactionResult2.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockPos, itemStack);
                }

                return interactionResult2;
            }
        }
        return enuminteractionresult;
    }

    @Override
    public boolean bridge$isFiredInteract() {
        return firedInteract;
    }

    @Override
    public void bridge$setFiredInteract(boolean firedInteract) {
        this.firedInteract = firedInteract;
    }

    @Override
    public boolean bridge$getInteractResult() {
        return interactResult;
    }

    @Override
    public BlockPos bridge$getinteractPosition() {
        return interactPosition;
    }

    @Override
    public InteractionHand bridge$getinteractHand() {
        return interactHand;
    }

    @Override
    public ItemStack bridge$getinteractItemStack() {
        return interactItemStack;
    }
}
