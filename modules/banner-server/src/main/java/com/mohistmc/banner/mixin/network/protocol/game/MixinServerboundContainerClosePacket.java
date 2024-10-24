package com.mohistmc.banner.mixin.network.protocol.game;

import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerboundContainerClosePacket.class)
public class MixinServerboundContainerClosePacket {

    @Shadow @Final @Mutable private int containerId;

    @Unique
    public void banner$constructor() {
        throw new RuntimeException();
    }

    @Unique
    public void banner$constructor(int id) {
        banner$constructor();
        this.containerId = id;
    }
}
