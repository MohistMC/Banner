package com.mohistmc.banner.injection.server.level;

import com.mohistmc.banner.bukkit.BukkitCallbackExecutor;

public interface InjectionChunkMap {

    default BukkitCallbackExecutor bridge$callbackExecutor() {
        throw new IllegalStateException("Not implemented");
    }
}
