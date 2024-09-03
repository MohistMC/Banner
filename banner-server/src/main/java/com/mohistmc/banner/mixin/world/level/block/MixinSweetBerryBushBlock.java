package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.world.level.block.SweetBerryBushBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SweetBerryBushBlock.class)
public class MixinSweetBerryBushBlock {

    /*
    @Eject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean banner$cropGrow(ServerLevel world, BlockPos pos, BlockState newState, int flags, CallbackInfo ci) {
        if (!CraftEventFactory.handleBlockGrowEvent(world, pos, newState, flags)) {
            ci.cancel();
        }
        return true;
    }*/

    /*
    @Eject(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SweetBerryBushBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$playerHarvest(BlockState blockState, Level worldIn, BlockPos pos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir, BlockState state, Level worldIn1, BlockPos pos1, Player player1, InteractionHand hand) {
        PlayerHarvestBlockEvent event = CraftEventFactory.callPlayerHarvestBlockEvent(worldIn, pos, player, hand, Collections.singletonList(new ItemStack(Items.SWEET_BERRIES)));// Banner TODO fix
        if (!event.isCancelled()) {
            for (org.bukkit.inventory.ItemStack itemStack : event.getItemsHarvested()) {
                Block.popResource(worldIn, pos, CraftItemStack.asNMSCopy(itemStack));
            }
        } else {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }*/
}
