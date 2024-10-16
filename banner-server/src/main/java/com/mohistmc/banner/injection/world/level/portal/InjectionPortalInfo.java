package com.mohistmc.banner.injection.world.level.portal;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftPortalEvent;
import org.jetbrains.annotations.Nullable;

public interface InjectionPortalInfo {

    default void banner$setPortalEventInfo(CraftPortalEvent event){
    }

    default CraftPortalEvent bridge$getPortalEventInfo() {
        return null;
    }

    default void banner$setWorld(ServerLevel world){
    }

    default @Nullable ServerLevel bridge$getWorld(){
        return null;
    }
}
