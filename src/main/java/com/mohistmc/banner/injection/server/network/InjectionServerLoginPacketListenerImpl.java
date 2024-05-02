package com.mohistmc.banner.injection.server.network;

import com.mojang.authlib.GameProfile;

public interface InjectionServerLoginPacketListenerImpl {

    default void disconnect(String s) {
    }

    default void callPlayerPreLoginEvents(GameProfile gameprofile) throws Exception {
    }
}
