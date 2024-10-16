package com.mohistmc.banner.mixin.world.level.block;

import java.util.Collections;

import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SweetBerryBushBlock.class)
public class MixinSweetBerryBushBlock {


    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$cropGrow(ServerLevel world, BlockPos pos, BlockState newState, int flags, @Cancellable CallbackInfo ci) {
        if (!CraftEventFactory.handleBlockGrowEvent(world, pos, newState, flags)) {
            ci.cancel();
        }
        return true;
    }

    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public void banner$damagePre(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        CraftEventFactory.blockDamage = CraftBlock.at(worldIn, pos);
    }

    @Inject(method = "entityInside", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    public void banner$damagePost(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        CraftEventFactory.blockDamage = null;
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SweetBerryBushBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$playerHarvest(Level level, BlockPos blockPos, ItemStack itemStack, BlockState state, Level worldIn1, BlockPos pos1, Player player, InteractionHand hand, @Cancellable CallbackInfoReturnable<InteractionResult> cir) {
        PlayerHarvestBlockEvent event = CraftEventFactory.callPlayerHarvestBlockEvent(level, blockPos, player, hand, Collections.singletonList(itemStack));
        if (!event.isCancelled()) {
            for (org.bukkit.inventory.ItemStack stack : event.getItemsHarvested()) {
                Block.popResource(level, blockPos, CraftItemStack.asNMSCopy(stack));
            }
        } else {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
