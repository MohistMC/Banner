package com.mohistmc.banner.mixin.world.level.block;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.TNTPrimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(TntBlock.class)
public class MixinTntBlock {

    private AtomicReference<BlockPos> banner$fromPos = new AtomicReference<>();
    private AtomicReference<BlockPos> banner$originPos = new AtomicReference<>();
    private AtomicReference<Player> banner$useTntPlayer = new AtomicReference<>();

    @WrapWithCondition(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private boolean banner$warpTntEvent(Level level, BlockPos pos) {
        return CraftEventFactory.callTNTPrimeEvent(level, pos, TNTPrimeEvent.PrimeCause.REDSTONE, null, null); // CraftBukkit - TNTPrimeEvent
    }

    @WrapWithCondition(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean banner$warpTntEvent0(Level level, BlockPos pos, boolean isMoving) {
        return CraftEventFactory.callTNTPrimeEvent(level, pos, TNTPrimeEvent.PrimeCause.REDSTONE, null, null); // CraftBukkit - TNTPrimeEvent
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"))
    private void banner$getFromPos(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving, CallbackInfo ci) {
        banner$fromPos.set(fromPos);
    }

    @Redirect(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasNeighborSignal(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean banner$addTntCheck0(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) && CraftEventFactory.callTNTPrimeEvent(level, pos, TNTPrimeEvent.PrimeCause.REDSTONE, null, banner$fromPos.get()); // CraftBukkit - TNTPrimeEvent
    }

    @Inject(method = "playerWillDestroy", at = @At("HEAD"))
    private void banner$getTntInfo(Level level, BlockPos blockPos, BlockState blockState, Player player, CallbackInfoReturnable<BlockState> cir) {
        banner$originPos.set(blockPos);
        banner$useTntPlayer.set(player);
    }

    @Redirect(method = "playerWillDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    private boolean banner$addTntCheck1(Level level) {
        return !level.isClientSide() && CraftEventFactory.callTNTPrimeEvent(level, banner$originPos.get(), TNTPrimeEvent.PrimeCause.BLOCK_BREAK, banner$useTntPlayer.get(), null); // CraftBukkit - TNTPrimeEvent
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void banner$TntPrime(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        // CraftBukkit start - TNTPrimeEvent
        if (!CraftEventFactory.callTNTPrimeEvent(level, blockPos, TNTPrimeEvent.PrimeCause.PLAYER, player, null)) {
            cir.setReturnValue(ItemInteractionResult.CONSUME);
        }
        // CraftBukkit end
    }

    @Inject(method = "onProjectileHit", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;isOnFire()Z"))
    public void banner$entityChangeBlock(Level worldIn, BlockState state, BlockHitResult hit, Projectile projectile, CallbackInfo ci) {
        if (!CraftEventFactory.callEntityChangeBlockEvent(projectile, hit.getBlockPos(), Blocks.AIR.defaultBlockState())) {
            ci.cancel();
        }
    }
}
