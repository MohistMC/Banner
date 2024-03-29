package com.mohistmc.banner.eventhandler;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.eventhandler.dispatcher.EntityEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.LevelEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.PlayerEventDispatcher;
import com.mohistmc.banner.util.I18n;

public class BannerEventDispatcherRegistry {

    public static void registerEventDispatchers() {
        BannerServer.LOGGER.info(I18n.as("banner.event_handler.register"));
        LevelEventDispatcher.dispatchLevel();
        PlayerEventDispatcher.dispatcherPlayer();
        EntityEventDispatcher.dispatchEntityEvent();
    }
}
