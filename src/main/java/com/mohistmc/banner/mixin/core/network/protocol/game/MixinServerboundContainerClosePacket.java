package com.mohistmc.banner.mixin.core.network.protocol.game;

import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundContainerClosePacket.class)
public class MixinServerboundContainerClosePacket {

    @Shadow @Final @Mutable private int containerId;

    public void banner$constructor() {
        throw new RuntimeException();
    }

    public void banner$constructor(int id) {
        banner$constructor();
        this.containerId = id;
    }
}
