package com.mohistmc.banner.mixin.world.item;

import java.util.concurrent.atomic.AtomicReference;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.DummyGeneratorAccess;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem extends Item {

    @Shadow public abstract boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result);

    @Shadow @Final public Fluid content;

    public MixinBucketItem(Properties properties) {
        super(properties);
    }

    private AtomicReference<PlayerBucketFillEvent> banner$bucketFillEvent = new AtomicReference<>();

    @Inject(method = "use",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/BucketPickup;pickupBlock(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void banner$use(Level level, Player player, InteractionHand usedHand,
                            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                            ItemStack itemStack, BlockHitResult blockHitResult, BlockPos blockPos,
                            Direction direction, BlockPos blockPos2, BlockState blockState, BucketPickup bucketPickup) {
        // CraftBukkit start
        ItemStack dummyFluid = bucketPickup.pickupBlock(DummyGeneratorAccess.INSTANCE, blockPos, blockState);
        if (dummyFluid.isEmpty()) cir.setReturnValue(InteractionResultHolder.fail(itemStack)); // Don't fire event if the bucket won't be filled.);
        banner$bucketFillEvent.set(CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) level, player, blockPos, blockPos,
                blockHitResult.getDirection(), itemStack, dummyFluid.getItem(), usedHand));

        if (banner$bucketFillEvent.get().isCancelled()) {
            ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(level, blockPos)); // SPIGOT-5163 (see PlayerInteractManager)
            ((ServerPlayer) player).getBukkitEntity().updateInventory(); // SPIGOT-4541
            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
        }
    }

    public boolean emptyContents(Player entity, Level world, BlockPos pos, @Nullable BlockHitResult result, Direction direction, BlockPos clicked, InteractionHand hand) {
        banner$direction = direction;
        banner$click = clicked;
        banner$hand = hand;
        try {
            return this.emptyContents(entity, world, pos, result);
        } finally {
            banner$direction = null;
            banner$click = null;
        }
    }

    @Inject(method = "use", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    private void banner$capture(Level worldIn, Player playerIn, InteractionHand handIn, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack stack, BlockHitResult result) {
        banner$direction = result.getDirection();
        banner$click = result.getBlockPos();
        banner$hand = handIn;
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

    private transient Direction banner$direction;
    private transient BlockPos banner$click;
    private transient InteractionHand banner$hand;

    @Inject(method = "emptyContents", at = @At("HEAD"),
            cancellable = true)
    private void banner$bucketFillEvent(Player entityhuman, Level world, BlockPos blockposition, BlockHitResult movingobjectpositionblock,
                                        CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        if (this.content instanceof FlowingFluid) {
            BlockState iblockdata = world.getBlockState(blockposition);
            Block block = iblockdata.getBlock();
            boolean flag = iblockdata.canBeReplaced(this.content);
            boolean flag1 = iblockdata.isAir() || flag || block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).canPlaceLiquid(world, blockposition, iblockdata, this.content);

            // CraftBukkit start
            if (flag1 && entityhuman != null) {
                PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent((ServerLevel) world, entityhuman, blockposition, banner$click, banner$direction, entityhuman.getItemInHand(entityhuman.getUsedItemHand()), banner$hand == null ? InteractionHand.MAIN_HAND : banner$hand);
                if (event.isCancelled()) {
                    ((ServerPlayer) entityhuman).connection.send(new ClientboundBlockUpdatePacket(world, blockposition));
                    (((ServerPlayer) entityhuman)).getBukkitEntity().updateInventory();
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
