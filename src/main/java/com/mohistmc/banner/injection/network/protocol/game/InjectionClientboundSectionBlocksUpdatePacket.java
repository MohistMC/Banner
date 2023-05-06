package com.mohistmc.banner.injection.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;

public interface InjectionClientboundSectionBlocksUpdatePacket {

    default ClientboundSectionBlocksUpdatePacket banner$init(SectionPos sectionposition, ShortSet shortset, BlockState[] states, boolean flag) {
        return null;
    }
}
