package com.mohistmc.banner.mixin.world.item;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {

    @Shadow protected abstract boolean mustSurvive();

    private AtomicReference<org.bukkit.block.BlockState> banner$stateCB = new AtomicReference<>(null);

    @Inject(method = "place", at= @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BlockItem;getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$postPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext blockPlaceContext) {
        // CraftBukkit start - special case for handling block placement with water lilies and snow buckets
        if (((BlockItem) (Object) this) instanceof PlaceOnWaterBlockItem || ((BlockItem) (Object) this)  instanceof SolidBucketItem) {
            banner$stateCB.set(CraftBlockStates.getBlockState(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos()));
        }
        // CraftBukkit end
    }

    @Inject(method = "place",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$postPlace0(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext blockPlaceContext, BlockState blockState, BlockPos blockPos, Level level, Player player, ItemStack itemStack, BlockState blockState2) {
        // CraftBukkit start
        org.bukkit.block.BlockState state = banner$stateCB.getAndSet(null);
        if (state != null) {
            BlockPlaceEvent placeEvent = CraftEventFactory.callBlockPlaceEvent((ServerLevel) level, player, blockPlaceContext.getHand(), state, blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                state.update(true, false);
                if (((BlockItem) (Object) this) instanceof SolidBucketItem) {
                    ((ServerPlayer) player).getBukkitEntity().updateInventory(); // SPIGOT-4541
                }
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
        // CraftBukkit end
    }

    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player entityhuman = context.getPlayer();
        CollisionContext collisionContext = entityhuman == null ? CollisionContext.empty() : CollisionContext.of(entityhuman);
        // CraftBukkit start - store default return
        boolean defaultReturn = (!this.mustSurvive() || state.canSurvive(context.getLevel(), context.getClickedPos())) && context.getLevel().isUnobstructed(state, context.getClickedPos(), collisionContext);
        org.bukkit.entity.Player player = (context.getPlayer() instanceof ServerPlayer) ? (org.bukkit.entity.Player) context.getPlayer().getBukkitEntity() : null;
        BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at(context.getLevel(), context.getClickedPos()), player, CraftBlockData.fromData(state), defaultReturn);
        context.getLevel().getCraftServer().getPluginManager().callEvent(event);
        return event.isBuildable();
        // CraftBukkit end
    }
}
