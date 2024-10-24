package com.mohistmc.banner.mixin.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StandingAndWallBlockItem.class)
public abstract class MixinStandingAndWallBlockItem extends BlockItem {


    public MixinStandingAndWallBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Inject(method = "getPlacementState", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;isUnobstructed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Z"))
    private void banner$blockCanPlace(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir, @Local(ordinal = 1) BlockState defaultReturn) {
        if (defaultReturn != null) {
            var result = context.getLevel().isUnobstructed(defaultReturn, context.getClickedPos(), CollisionContext.empty());
            var player = (context.getPlayer() instanceof ServerPlayer serverPlayer) ? serverPlayer.getBukkitEntity() : null;

            var event = new BlockCanBuildEvent(CraftBlock.at(context.getLevel(), context.getClickedPos()), player, CraftBlockData.fromData(defaultReturn), result);
            Bukkit.getPluginManager().callEvent(event);

            cir.setReturnValue(event.isBuildable() ? defaultReturn : null);
        }
    }
}
