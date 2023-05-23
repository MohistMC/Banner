package com.mohistmc.banner.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.DummyGeneratorAccess;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem extends Item {

    @Shadow @Final public Fluid content;

    @Shadow public abstract boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result);

    @Shadow protected abstract void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos);

    public MixinBucketItem(Properties properties) {
        super(properties);
    }

    private AtomicReference<PlayerBucketFillEvent> banner$bucketPickEvent = new AtomicReference<>();
    private AtomicReference<ItemStack> banner$usedStack = new AtomicReference<>();
    private AtomicReference<InteractionHand> banner$usedHand = new AtomicReference<>();


    @Inject(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/BucketPickup;pickupBlock(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;",
            shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$bucketEvent(Level level, Player player, InteractionHand usedHand,
                                    CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                    ItemStack itemStack, BlockHitResult blockHitResult, BlockPos blockPos,
                                    Direction direction, BlockPos blockPos2, BlockState blockState,
                                    BucketPickup bucketPickup) {
        // CraftBukkit start
        banner$usedStack.set(itemStack);
        banner$usedHand.set(usedHand);
        ItemStack dummyFluid = bucketPickup.pickupBlock(DummyGeneratorAccess.INSTANCE, blockPos, blockState);
        if (dummyFluid.isEmpty()) cir.setReturnValue(InteractionResultHolder.fail(itemStack)); // Don't fire event if the bucket won't be filled.
        PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) level, player, blockPos, blockPos, blockHitResult.getDirection(), itemStack, dummyFluid.getItem(), usedHand);
        banner$bucketPickEvent.set(event);

        if (event.isCancelled()) {
            ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(level, blockPos)); // SPIGOT-5163 (see PlayerInteractManager)
            ((ServerPlayer) player).getBukkitEntity().updateInventory(); // SPIGOT-4541
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
        }
        // CraftBukkit end
    }

    @Redirect(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack banner$resetFilledResult(ItemStack emptyStack, Player player, ItemStack filledStack) {
        return ItemUtils.createFilledResult(emptyStack, player, CraftItemStack.asNMSCopy(banner$bucketPickEvent.get().getItemStack()));
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private boolean banner$reuseEmptyContent(BucketItem instance, Player player, Level level, BlockPos pos, BlockHitResult result) {
        return emptyContents(player, level, pos, result, result.getDirection(), pos, banner$usedStack.get(), banner$usedHand.get());
    }

    private AtomicReference<Direction> banner$direction = new AtomicReference<>(null);
    private AtomicReference<BlockPos> banner$clicked = new AtomicReference<>(null);
    private AtomicReference<ItemStack> banner$stack = new AtomicReference<>(null);
    private AtomicReference<InteractionHand> banner$hand = new AtomicReference<>(InteractionHand.MAIN_HAND);

    public boolean emptyContents(Player entityhuman, Level world, BlockPos blockposition, @Nullable BlockHitResult movingobjectpositionblock, Direction enumdirection, BlockPos clicked, ItemStack itemstack, InteractionHand enumhand) {
        banner$direction.set(enumdirection);
        banner$clicked.set(blockposition);
        banner$stack.set(itemstack);
        banner$hand.set(enumhand);
        return emptyContents(entityhuman, world, blockposition, movingobjectpositionblock);
    }

    @Inject(method = "emptyContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getMaterial()Lnet/minecraft/world/level/material/Material;",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$handleEmptyContents(Player player, Level level, BlockPos pos, BlockHitResult result,
                                            CallbackInfoReturnable<Boolean> cir, BlockState blockState, Block block) {
        boolean banner$flag = blockState.canBeReplaced(this.content);
        boolean banner$flag2 = blockState.isAir() || banner$flag || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(level, pos, blockState, this.content);
        // CraftBukkit start
        if (banner$flag2 && player != null) {
            PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent((ServerLevel) level, player, pos, banner$clicked.get(), banner$direction.get(), banner$stack.get(), banner$hand.get());
            if (event.isCancelled()) {
                ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(level, pos)); // SPIGOT-4238: needed when looking through entity
                ((ServerPlayer) player).getBukkitEntity().updateInventory(); // SPIGOT-4541
                cir.setReturnValue(false);
            }
        }
        // CraftBukkit end
    }

    @Redirect(method = "emptyContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private boolean banner$reuseEmptyContent0(BucketItem instance, Player player, Level level, BlockPos pos, BlockHitResult result) {
        return emptyContents(player, level, pos, result, banner$direction.get(), banner$clicked.get(), banner$stack.get(), banner$hand.get());
    }
}
