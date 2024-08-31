package com.mohistmc.banner.injection.network.protocol.game;

import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Mgazul by MohistMC
 * @date 2023/5/6 20:46:36
 */
public interface InjectionClientboundSectionBlocksUpdatePacket {

    default void putBukkitPacket(BlockState[] states) {
        throw new IllegalStateException("Not implemented");
    }
}
