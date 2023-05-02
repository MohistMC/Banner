package com.mohistmc.banner.eventhandler.dispatcher;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

public class EntityEventDispatcher {

    public static void dispatchEntity() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            entity.damageEntity0(source, amount);
            return false;
        });
    }
}
