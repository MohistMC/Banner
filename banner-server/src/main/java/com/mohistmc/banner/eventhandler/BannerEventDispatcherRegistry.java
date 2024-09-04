package com.mohistmc.banner.eventhandler;

import com.mohistmc.banner.BannerMod;
import com.mohistmc.banner.eventhandler.dispatcher.EntityEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.FabricToBukkitEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.LevelEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.PlayerEventDispatcher;
import com.mohistmc.banner.eventhandler.dispatcher.ServerEventDispatcher;
import com.mohistmc.banner.util.I18n;

public class BannerEventDispatcherRegistry {

    public static void registerEventDispatchers() {
        BannerMod.LOGGER.info(I18n.as("banner.event_handler.register"));
        LevelEventDispatcher.dispatchLevel();
        PlayerEventDispatcher.dispatcherPlayer();
        EntityEventDispatcher.dispatchEntityEvent();
        FabricToBukkitEventDispatcher.dispatchFabric2Bukkit();
        ServerEventDispatcher.dispatchServer();
    }
}
