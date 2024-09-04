package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.DummyGeneratorAccess;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem extends Item {

    @Shadow public abstract boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result);

    public MixinBucketItem(Properties properties) {
        super(properties);
    }

    @Inject(method = "use", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BucketPickup;pickupBlock(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"))
    private void banner$bucketFill(Level worldIn, Player playerIn, InteractionHand handIn, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, @Local ItemStack stack, @Local BlockHitResult result) {
        if (!DistValidate.isValid(worldIn)) return;
        BlockPos pos = result.getBlockPos();
        BlockState state = worldIn.getBlockState(pos);
        ItemStack dummyFluid = ((BucketPickup) state.getBlock()).pickupBlock(playerIn, DummyGeneratorAccess.INSTANCE, pos, state);
        PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) worldIn, playerIn, pos, pos, result.getDirection(), stack, dummyFluid.getItem(), handIn);
        if (event.isCancelled()) {
            ((ServerPlayer) playerIn).connection.send(new ClientboundBlockUpdatePacket(worldIn, pos));
            ((ServerPlayer) playerIn).getBukkitEntity().updateInventory();
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, stack));
        } else {
            banner$captureItem = event.getItemStack();
        }
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void banner$clean(Level worldIn, Player playerIn, InteractionHand handIn, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        banner$captureItem = null;
        banner$direction = null;
        banner$click = null;
    }

    private transient org.bukkit.inventory.@Nullable ItemStack banner$captureItem;

    @ModifyArg(method = "use", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack banner$useEventItem(ItemStack itemStack) {
        return banner$captureItem == null ? itemStack : CraftItemStack.asNMSCopy(banner$captureItem);
    }

    @Inject(method = "use", require = 0, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private void banner$capture(Level worldIn, Player playerIn, InteractionHand handIn, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack stack, BlockHitResult result) {
        banner$direction = result.getDirection();
        banner$click = result.getBlockPos();
        banner$hand = handIn;
    }

    public boolean emptyContents(Player entity, Level world, BlockPos pos, @Nullable BlockHitResult result, Direction direction, BlockPos clicked, ItemStack itemstack, InteractionHand hand) {
        banner$direction = direction;
        banner$click = clicked;
        banner$hand = hand;
        banner$stack = itemstack;
        try {
            return this.emptyContents(entity, world, pos, result);
        } finally {
            banner$direction = null;
            banner$click = null;
            banner$hand = null;
            banner$stack = null;
        }
    }

    private transient Direction banner$direction;
    private transient BlockPos banner$click;
    private transient InteractionHand banner$hand;
    private transient ItemStack banner$stack;

    @Inject(method = "emptyContents", require = 0, cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;ultraWarm()Z"))
    private void banner$bucketEmpty(Player player, Level worldIn, BlockPos posIn, BlockHitResult rayTrace, CallbackInfoReturnable<Boolean> cir) {
        if (!DistValidate.isValid(worldIn)) return;
        if (player != null && banner$stack != null) {
            PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent((ServerLevel) worldIn, player, posIn, banner$click, banner$direction, banner$stack, banner$hand == null ? InteractionHand.MAIN_HAND : banner$hand);
            if (event.isCancelled()) {
                ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(worldIn, posIn));
                ((ServerPlayer) player).getBukkitEntity().updateInventory();
                cir.setReturnValue(false);
            }
        }
    }


}