package com.mohistmc.banner.mixin.network.protocol.game;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.injection.network.protocol.game.InjectionClientboundSectionBlocksUpdatePacket;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
public class MixinClientboundSectionBlocksUpdatePacket implements InjectionClientboundSectionBlocksUpdatePacket {

    @Shadow @Final @Mutable private SectionPos sectionPos;
    @Shadow @Final @Mutable private short[] positions;
    @Shadow @Final @Mutable private BlockState[] states;

    @CreateConstructor
    public void banner$constructor(SectionPos sectionposition, ShortSet shortset, BlockState[] states) {
        this.sectionPos = sectionposition;
        this.positions = shortset.toShortArray();
        this.states = states;
    }

    @Override
    public void putBukkitPacket(BlockState[] states) {
        this.states = states;
    }
}
