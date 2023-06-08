package com.mohistmc.banner.eventhandler;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.eventhandler.dispatcher.*;

public class BannerEventDispatcherRegistry {

    public static void registerEventDispatchers() {
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("banner.event_handler.register"));
        LevelEventDispatcher.dispatchLevel();
        PlayerEventDispatcher.dispatcherPlayer();
        EntityEventDispatcher.dispatchEntity();
    }
}
