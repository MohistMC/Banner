package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {

    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER))
    private void banner$putCBState(BlockPlaceContext blockPlaceContext,
                                   CallbackInfoReturnable<InteractionResult> cir,
                                   @Local(ordinal = 1) BlockPlaceContext blockPlaceContext2,
                                   @Share("cbState")LocalRef<org.bukkit.block.BlockState> stateLocalRef) {
        // CraftBukkit start - special case for handling block placement with water lilies and snow buckets
        org.bukkit.block.BlockState blockstate = null;
        if (((BlockItem) (Object) this) instanceof PlaceOnWaterBlockItem || ((BlockItem) (Object) this) instanceof SolidBucketItem) {
            blockstate = org.bukkit.craftbukkit.block.CraftBlockStates.getBlockState(blockPlaceContext2.getLevel(), blockPlaceContext2.getClickedPos());
        }
        stateLocalRef.set(blockstate);
        // CraftBukkit end
    }

    @Inject(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            shift = At.Shift.AFTER), cancellable = true)
    private void banner$placeEvent(BlockPlaceContext blockPlaceContext,
                                   CallbackInfoReturnable<InteractionResult> cir,
                                   @Local(ordinal = 1) BlockPlaceContext blockPlaceContext2,
                                   @Local(ordinal = 0) BlockState blockState,
                                   @Local BlockPos blockPos,
                                   @Local Level level,
                                   @Local Player player,
                                   @Share("cbState")LocalRef<org.bukkit.block.BlockState> stateLocalRer) {
        // CraftBukkit start
        if (stateLocalRer.get() != null) {
            org.bukkit.event.block.BlockPlaceEvent placeEvent =
                    org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent((ServerLevel) level, player, blockPlaceContext2.getHand(), stateLocalRer.get(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                stateLocalRer.get().update(true, false);

                if (((BlockItem) (Object) this) instanceof SolidBucketItem) {
                    ((ServerPlayer) player).getBukkitEntity().updateInventory(); // SPIGOT-4541
                }
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void banner$cancelPlaySound(Level instance, Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g) { }

    @Inject(method = "canPlace", at = @At("HEAD"))
    private void banner$storeValue(BlockPlaceContext blockPlaceContext,
                                   BlockState blockState,
                                   CallbackInfoReturnable<Boolean> cir,
                                   @Share("bannerContext") LocalRef<BlockPlaceContext> placeContextLocalRef,
                                   @Share("bannerState") LocalRef<BlockState> stateLocalRef) {
        placeContextLocalRef.set(blockPlaceContext);
        stateLocalRef.set(blockState);
    }

    @ModifyReturnValue(method = "canPlace", at = @At("RETURN"))
    private boolean banner$buildEvent(boolean original,
                                      @Share("bannerContext") LocalRef<BlockPlaceContext> placeContextLocalRef,
                                      @Share("bannerState") LocalRef<BlockState> stateLocalRef) {
        // CraftBukkit start - store default return
        org.bukkit.entity.Player player = (placeContextLocalRef.get().getPlayer() instanceof ServerPlayer) ? (org.bukkit.entity.Player) placeContextLocalRef.get().getPlayer().getBukkitEntity() : null;
        BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at(placeContextLocalRef.get().getLevel(), placeContextLocalRef.get().getClickedPos()), player, CraftBlockData.fromData(stateLocalRef.get()), original);
        placeContextLocalRef.get().getLevel().getCraftServer().getPluginManager().callEvent(event);
        return event.isBuildable();
        // CraftBukkit end
    }
}
