package com.mohistmc.banner.bukkit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.event.block.CauldronLevelChangeEvent;
public class BukkitCauldronHooks {

    private static Entity entity;
    private static CauldronLevelChangeEvent.ChangeReason reason = CauldronLevelChangeEvent.ChangeReason.UNKNOWN;
    private static boolean lastRet = true;

    public static void setChangeReason(Entity entity, CauldronLevelChangeEvent.ChangeReason reason) {
        BukkitCauldronHooks.entity = entity;
        BukkitCauldronHooks.reason = reason;
    }

    public static void reset() {
        BukkitCauldronHooks.entity = null;
        BukkitCauldronHooks.reason = CauldronLevelChangeEvent.ChangeReason.UNKNOWN;
        BukkitCauldronHooks.lastRet = true;
    }

    public static Entity getEntity() {
        return entity;
    }

    public static CauldronLevelChangeEvent.ChangeReason getReason() {
        return reason;
    }

    public static boolean getResult() {
        return lastRet;
    }

    public static boolean changeLevel(Level world, BlockPos pos, BlockState state, Entity entity, CauldronLevelChangeEvent.ChangeReason reason) {
        CraftBlockState newState = CraftBlockStates.getBlockState(world, pos);
        newState.setData(state);
        CauldronLevelChangeEvent event = new CauldronLevelChangeEvent(
                CraftBlock.at(world, pos),
                (entity == null) ? null : entity.getBukkitEntity(),
                reason, newState
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return lastRet = false;
        } else {
            newState.update(true);
            return lastRet = true;
        }
    }

    public static boolean lowerFillLevel(BlockState iblockdata, Level world, BlockPos blockposition, Entity entity, CauldronLevelChangeEvent.ChangeReason reason) {
        int i = iblockdata.getValue(LayeredCauldronBlock.LEVEL) - 1;
        BlockState iblockdata1 = i == 0 ? Blocks.CAULDRON.defaultBlockState() : iblockdata.setValue(LayeredCauldronBlock.LEVEL, i);
        return changeLevel(world, blockposition, iblockdata1, entity, reason);
    }
}
