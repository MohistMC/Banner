package com.mohistmc.banner.mixin.server.commands;

import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

// Banner - TODO fix
@Mixin(SpreadPlayersCommand.class)
public class MixinSpreadPlayersCommand {

    // CraftBukkit start - add a version of getBlockState which force loads chunks
    private static BlockState getBlockState(BlockGetter iblockaccess, BlockPos position) {
        ((ServerLevel) iblockaccess).getChunkSource().getChunk(position.getX() >> 4, position.getZ() >> 4, true);
        return iblockaccess.getBlockState(position);
    }
    // CraftBukkit end
}
