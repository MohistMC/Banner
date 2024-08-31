package com.mohistmc.banner.injection.network;

import io.netty.buffer.ByteBuf;

public interface InjectionDiscardedPayload {

    default void bridge$setData(ByteBuf data) {
        throw new IllegalStateException("Not implemented");
    }

    default ByteBuf bridge$getData() {
        throw new IllegalStateException("Not implemented");
    }

}
