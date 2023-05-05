package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem extends Item {

    @Shadow @Final public Fluid content;

    public MixinBucketItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BucketPickup;pickupBlock(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    public void banner$use_BF(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!DistValidate.isValid(level)) return;
        BlockHitResult movingobjectpositionblock = getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY : net.minecraft.world.level.ClipContext.Fluid.NONE);
        BlockHitResult movingobjectpositionblock1 = (BlockHitResult) movingobjectpositionblock;
        BlockPos blockposition = movingobjectpositionblock1.getBlockPos();
        BlockState iblockdata = level.getBlockState(blockposition);

        if (iblockdata.getBlock() instanceof BucketPickup) {
            ItemStack dummyFluid = ((BucketPickup) iblockdata.getBlock()).pickupBlock(level, blockposition, iblockdata);
            PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent((ServerLevel) level, player, blockposition, blockposition, movingobjectpositionblock.getDirection(), player.getItemInHand(usedHand), dummyFluid.getItem(), usedHand); // Paper - add enumhand

            if (event.isCancelled()) {
                ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(level, blockposition)); // SPIGOT-5163 (see PlayerInteractManager)
                ((org.bukkit.entity.Player) player.getBukkitEntity()).updateInventory(); // SPIGOT-4541
                cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(usedHand)));
                return;
            }
        }
    }

    @Inject(method = "emptyContents", at = @At("HEAD"), cancellable = true)
    public void banner$placeFluid_BF(Player player, Level level, BlockPos pos, BlockHitResult result, CallbackInfoReturnable<Boolean> cir) {
        if (this.content instanceof FlowingFluid) {
            BlockState iblockdata = level.getBlockState(pos);
            Block block = iblockdata.getBlock();
            boolean flag = iblockdata.canBeReplaced(this.content);
            boolean flag1 = iblockdata.isAir() || flag || block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).canPlaceLiquid(level, pos, iblockdata, this.content);

            // CraftBukkit start
            if (flag1 && player != null) {
                PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent((ServerLevel) level, player, pos, result.getBlockPos(), result.getDirection(), player.getItemInHand(player.getUsedItemHand()), player.getUsedItemHand());
                if (event.isCancelled()) {
                    ((ServerPlayer) player).connection.send(new ClientboundBlockUpdatePacket(level, pos));
                    ((ServerPlayer) player).getBukkitEntity().updateInventory();
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
