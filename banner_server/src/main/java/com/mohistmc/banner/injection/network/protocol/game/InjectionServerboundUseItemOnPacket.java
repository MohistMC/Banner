package com.mohistmc.banner.injection.network.protocol.game;

public interface InjectionServerboundUseItemOnPacket {

    default long bridge$timestamp() {
        throw new IllegalStateException("Not implemented");
    }
}
