package com.mohistmc.banner.eventhandler;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.eventhandler.dispatcher.CommandsEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.EntityEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.LevelEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.PlayerEventDispatcher;

public class BannerEventDispatcherRegistry {

    public static void registerEventDispatchers() {
        BannerServer.LOGGER.info("Registering Banner Event Dispatchers...");
        LevelEventDispatcher.dispatchLevel();
        PlayerEventDispatcher.dispatcherPlayer();
        CommandsEventDispatcher.onCommandDispatch();
        EntityEventDispatcher.dispatchEntity();
    }
}
