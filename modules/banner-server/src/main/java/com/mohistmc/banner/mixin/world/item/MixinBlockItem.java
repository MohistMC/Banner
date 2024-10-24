package com.mohistmc.banner.mixin.world.item;

import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {

    @Shadow protected abstract boolean mustSurvive();

    @Unique
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

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void banner$cancelPlayerSound(Level instance, Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {}

    @Unique
    private AtomicReference<Player> banner$placePlayer = new AtomicReference<>();
    @Unique
    private AtomicReference<ItemStack> banner$placeStack = new AtomicReference<>();

    @Inject(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$setInfo(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext blockPlaceContext, BlockState blockState, BlockPos blockPos, Level level, Player player, ItemStack itemStack, BlockState blockState2, SoundType soundType) {
        banner$placePlayer.set(player);
        banner$placeStack.set(itemStack);
    }

    @Redirect(method = "place", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z"))
    private boolean banner$checkAbilities(Abilities instance) {
        return banner$placePlayer.getAndSet(null).getAbilities().instabuild && banner$placeStack.getAndSet(null) == ItemStack.EMPTY;
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
