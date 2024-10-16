package com.mohistmc.banner.mixin.world.level.block;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DetectorRailBlock.class)
public abstract class MixinDetectorRailBlock extends Block {

    public MixinDetectorRailBlock(Properties properties) {
        super(properties);
    }

    @Shadow protected abstract <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos pos, Class<T> cartType, Predicate<Entity> filter);

    @Shadow protected abstract void updatePowerToConnected(Level level, BlockPos pos, BlockState state, boolean powered);

    @Shadow @Final public static BooleanProperty POWERED;

    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    private void checkPressed(Level level, BlockPos pos, BlockState state) {
        if (this.canSurvive(state, level, pos)) {
            boolean bl = (Boolean)state.getValue(POWERED);
            boolean bl2 = false;
            List<AbstractMinecart> list = this.getInteractingMinecartOfType(level, pos, AbstractMinecart.class, (entity) -> {
                return true;
            });
            if (!list.isEmpty()) {
                bl2 = true;
            }

            BlockState blockState;
            // CraftBukkit start
            if (bl != bl2) {
                org.bukkit.block.Block block = level.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());

                BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, bl ? 15 : 0, bl2 ? 15 : 0);
                level.getCraftServer().getPluginManager().callEvent(eventRedstone);

                bl2 = eventRedstone.getNewCurrent() > 0;
            }
            // CraftBukkit end
            if (bl2 && !bl) {
                blockState = (BlockState)state.setValue(POWERED, true);
                level.setBlock(pos, blockState, 3);
                this.updatePowerToConnected(level, pos, blockState, true);
                level.updateNeighborsAt(pos, this);
                level.updateNeighborsAt(pos.below(), this);
                level.setBlocksDirty(pos, state, blockState);
            }

            if (!bl2 && bl) {
                blockState = (BlockState)state.setValue(POWERED, false);
                level.setBlock(pos, blockState, 3);
                this.updatePowerToConnected(level, pos, blockState, false);
                level.updateNeighborsAt(pos, this);
                level.updateNeighborsAt(pos.below(), this);
                level.setBlocksDirty(pos, state, blockState);
            }

            if (bl2) {
                level.scheduleTick(pos, this, 20);
            }

            level.updateNeighbourForOutputSignal(pos, this);
        }
    }
}
