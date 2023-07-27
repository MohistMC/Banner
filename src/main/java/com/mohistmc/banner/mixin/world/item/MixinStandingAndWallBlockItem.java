package com.mohistmc.banner.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StandingAndWallBlockItem.class)
public abstract class MixinStandingAndWallBlockItem extends BlockItem {

    @Shadow @Final public Block wallBlock;

    @Shadow @Final private Direction attachmentDirection;

    public MixinStandingAndWallBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Shadow protected abstract boolean canPlace(LevelReader level, BlockState state, BlockPos pos);

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState blockState = this.wallBlock.getStateForPlacement(context);
        BlockState blockState2 = null;
        LevelReader levelReader = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Direction[] var6 = context.getNearestLookingDirections();
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            Direction direction = var6[var8];
            if (direction != this.attachmentDirection.getOpposite()) {
                BlockState blockState3 = direction == this.attachmentDirection ? this.getBlock().getStateForPlacement(context) : blockState;
                if (blockState3 != null && this.canPlace(levelReader, blockState3, blockPos)) {
                    blockState2 = blockState3;
                    break;
                }
            }
        }

        // CraftBukkit start
        if (blockState2 != null) {
            boolean defaultReturn = levelReader.isUnobstructed(blockState2, blockPos, CollisionContext.empty());
            org.bukkit.entity.Player player = (context.getPlayer() instanceof ServerPlayer) ? (org.bukkit.entity.Player) context.getPlayer().getBukkitEntity() : null;
            BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at(context.getLevel(), blockPos), player, CraftBlockData.fromData(blockState2), defaultReturn);
            context.getLevel().getCraftServer().getPluginManager().callEvent(event);
            return (event.isBuildable()) ? blockState2 : null;
        } else {
            return null;
        }
        // CraftBukkit end
    }
}
