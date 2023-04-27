package com.mohistmc.banner.eventhandler;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.eventhandler.dispatcher.PlayerEventDispatcher;

public class BannerEventDispatcherRegistry {

    public static void registerEventDispatchers() {
        BannerServer.LOGGER.info("Registering Banner Event Dispatchers...");
        PlayerEventDispatcher.dispatcherPlayer();
    }
}
