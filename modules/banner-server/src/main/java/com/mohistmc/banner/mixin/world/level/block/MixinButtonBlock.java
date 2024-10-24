package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ButtonBlock.class)
public class MixinButtonBlock {

    @Shadow @Final public static BooleanProperty POWERED;

    @Inject(method = "checkPressed", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    public void banner$entityInteract(BlockState state, Level worldIn, BlockPos pos, CallbackInfo ci,
                                        AbstractArrow abstractarrow, boolean flag) {
        boolean flag1 = state.getValue(ButtonBlock.POWERED);
        if (flag1 != flag && flag) {
            Block block = CraftBlock.at(worldIn, pos);
            EntityInteractEvent event = new EntityInteractEvent(abstractarrow.getBukkitEntity(), block);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "checkPressed", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public void banner$blockRedstone3(BlockState state, Level worldIn, BlockPos pos, CallbackInfo ci,
                                        AbstractArrow abstractarrow, boolean flag, boolean flag1) {
        Block block = CraftBlock.at(worldIn, pos);
        int old = (flag1) ? 15 : 0;
        int current = (!flag1) ? 15 : 0;

        BlockRedstoneEvent event = new BlockRedstoneEvent(block, old, current);
        Bukkit.getPluginManager().callEvent(event);

        if ((flag && event.getNewCurrent() <= 0) || (!flag && event.getNewCurrent() > 0)) {
            ci.cancel();
        }
    }

    @Inject(method = "useWithoutItem", cancellable = true, at = @At(value = "HEAD"))
    public void banner$blockRedstone1(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!state.getValue(POWERED)) {
            boolean powered = state.getValue(POWERED);
            Block block = CraftBlock.at(worldIn, pos);
            int old = (powered) ? 15 : 0;
            int current = (!powered) ? 15 : 0;

            BlockRedstoneEvent event = new BlockRedstoneEvent(block, old, current);
            Bukkit.getPluginManager().callEvent(event);

            if ((event.getNewCurrent() > 0) == (powered)) {
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
