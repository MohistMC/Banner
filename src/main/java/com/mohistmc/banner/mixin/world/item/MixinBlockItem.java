package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {

    // @formatter:off
    @Shadow protected abstract boolean mustSurvive();
    @Shadow
    public static <T extends Comparable<T>> BlockState updateState(BlockState p_219988_0_, Property<T> p_219988_1_, String p_219988_2_) { return null; }
    // @formatter:on

    private AtomicReference<org.bukkit.block.BlockState> banner$state = new AtomicReference<>();

    @Inject(method = "place", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/item/BlockItem;getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void banner$prePlaceLilypad(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext context1) {
        if ((Object) this instanceof PlaceOnWaterBlockItem || (Object) this instanceof SolidBucketItem) {
            this.banner$state.set(CraftBlockStates.getBlockState(context1.getLevel(), context1.getClickedPos()));
        }
    }

    @Inject(method = "place", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$postPlaceLilypad(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext blockPlaceContext, BlockState blockState, BlockPos blockPos, Level level, net.minecraft.world.entity.player.Player player, ItemStack itemStack, BlockState blockState2) {
        // CraftBukkit start
        if (banner$state.get() != null) {
            org.bukkit.event.block.BlockPlaceEvent placeEvent = CraftEventFactory.callBlockPlaceEvent((ServerLevel) level, player, blockPlaceContext.getHand(), banner$state.get(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                banner$state.get().update(true, false);
            }
            if ((Object) this instanceof SolidBucketItem) {
                ((ServerPlayer) player).getBukkitEntity().updateInventory(); // SPIGOT-4541
            }
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    private static BlockState getBlockState(BlockState blockState, CompoundTag nbt) {
        StateDefinition<Block, BlockState> statecontainer = blockState.getBlock().getStateDefinition();
        for (String s : nbt.getAllKeys()) {
            Property<?> iproperty = statecontainer.getProperty(s);
            if (iproperty != null) {
                String s1 = nbt.get(s).getAsString();
                blockState = updateState(blockState, iproperty, s1);
            }
        }
        return blockState;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        net.minecraft.world.entity.player.Player playerentity = context.getPlayer();
        CollisionContext iselectioncontext = playerentity == null ? CollisionContext.empty() : CollisionContext.of(playerentity);
        boolean original = (!this.mustSurvive() || state.canSurvive(context.getLevel(), context.getClickedPos())) && context.getLevel().isUnobstructed(state, context.getClickedPos(), iselectioncontext);

        Player player = (context.getPlayer() instanceof ServerPlayer) ? ((ServerPlayer) context.getPlayer()).getBukkitEntity() : null;
        BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at(context.getLevel(), context.getClickedPos()), player, CraftBlockData.fromData(state), original);
        if (DistValidate.isValid(context)) Bukkit.getPluginManager().callEvent(event);
        return event.isBuildable();
    }
}
