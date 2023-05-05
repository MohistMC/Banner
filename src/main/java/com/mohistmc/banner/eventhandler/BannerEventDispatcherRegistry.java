package com.mohistmc.banner.eventhandler;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.eventhandler.dispatcher.*;

public class BannerEventDispatcherRegistry {

    public static void registerEventDispatchers() {
        BannerServer.LOGGER.info("Registering Banner Event Dispatchers...");
        ServerEventDispatcher.dispatchServer();
        LevelEventDispatcher.dispatchLevel();
        PlayerEventDispatcher.dispatcherPlayer();
        CommandsEventDispatcher.onCommandDispatch();
        EntityEventDispatcher.dispatchEntity();
    }
}