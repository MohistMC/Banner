package com.mohistmc.banner.injection.server.bossevents;

import org.bukkit.boss.KeyedBossBar;

public interface InjectionCustomBossEvent {

    default KeyedBossBar getBukkitEntity() {
        throw new IllegalStateException("Not implemented");
    }
}
