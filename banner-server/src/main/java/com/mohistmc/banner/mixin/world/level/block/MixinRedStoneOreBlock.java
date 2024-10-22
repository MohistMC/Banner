package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneOreBlock.class)
public abstract class MixinRedStoneOreBlock {

    @Shadow
    protected static void interact(BlockState state, Level level, BlockPos pos) {
    }

    private static transient Entity banner$entity;

    @Inject(method = "attack", at = @At(value = "HEAD"))
    public void banner$interact1(BlockState state, Level worldIn, BlockPos pos, Player player, CallbackInfo ci) {
        banner$entity = player;
    }

    @Inject(method = "stepOn", cancellable = true, at = @At(value = "HEAD"))
    public void banner$entityInteract(Level worldIn, BlockPos pos, BlockState state, Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof Player) {
            PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(((Player) entityIn), Action.PHYSICAL, pos, null, null, null);
            if (event.isCancelled()) {
                ci.cancel();
                return;
            }
        } else {
            EntityInteractEvent event = new EntityInteractEvent(entityIn.getBukkitEntity(), CraftBlock.at(worldIn, pos));
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
                return;
            }
        }
        banner$entity = entityIn;
    }

    @Inject(method = "useItemOn", at = @At(value = "HEAD"))
    public void banner$interact3(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        banner$entity = player;
    }

    @Inject(method = "randomTick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void banner$blockFade(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (CraftEventFactory.callBlockFadeEvent(worldIn, pos, state.setValue(RedStoneOreBlock.LIT, false)).isCancelled()) {
            ci.cancel();
        }
    }

    private static void interact(BlockState blockState, Level world, BlockPos blockPos, Entity entity) {
        banner$entity = entity;
        interact(blockState, world, blockPos);
    }

    @Inject(method = "interact", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static void banner$entityChangeBlock(BlockState blockState, Level world, BlockPos blockPos, CallbackInfo ci) {
        if (!CraftEventFactory.callEntityChangeBlockEvent(banner$entity, blockPos, blockState.setValue(RedStoneOreBlock.LIT, true))) {
            ci.cancel();
        }
        banner$entity = null;
    }
}
