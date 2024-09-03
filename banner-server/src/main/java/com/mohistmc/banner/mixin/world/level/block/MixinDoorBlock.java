package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.World;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoorBlock.class)
public abstract class MixinDoorBlock extends Block{

    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;

    @Shadow @Final public static BooleanProperty POWERED;

    @Shadow @Final public static BooleanProperty OPEN;

    @Shadow protected abstract void playSound(@Nullable Entity source, Level level, BlockPos pos, boolean isOpening);

    public MixinDoorBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        // CraftBukkit start
        BlockPos otherHalf = pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);
        World bworld = level.getWorld();
        org.bukkit.block.Block bukkitBlock = bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        org.bukkit.block.Block blockTop = bworld.getBlockAt(otherHalf.getX(), otherHalf.getY(), otherHalf.getZ());

        int power = bukkitBlock.getBlockPower();
        int powerTop = blockTop.getBlockPower();
        if (powerTop > power) power = powerTop;
        int oldPower = (Boolean) state.getValue(POWERED) ? 15 : 0;

        if (oldPower == 0 ^ power == 0) {
            BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, oldPower, power);
            level.getCraftServer().getPluginManager().callEvent(eventRedstone);
            boolean bl = eventRedstone.getNewCurrent() > 0;
            if (bl != (Boolean) state.getValue(OPEN)) {
                this.playSound((Entity) null, level, pos, bl);
                level.gameEvent((Entity) null, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
            level.setBlock(pos, (BlockState) ((BlockState) state.setValue(POWERED, bl)).setValue(OPEN, bl), 2);
        }
    }
}
