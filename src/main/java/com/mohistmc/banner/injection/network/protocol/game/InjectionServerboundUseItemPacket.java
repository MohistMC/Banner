package com.mohistmc.banner.injection.network.protocol.game;

public interface InjectionServerboundUseItemPacket {

    default long bridge$timestamp() {
        throw new IllegalStateException("Not implemented");
    }
}
