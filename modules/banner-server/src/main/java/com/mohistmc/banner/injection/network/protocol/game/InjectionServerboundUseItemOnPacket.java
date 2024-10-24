package com.mohistmc.banner.injection.network.protocol.game;

public interface InjectionServerboundUseItemOnPacket {

    default long bridge$timestamp() {
        return 0;
    }
}
