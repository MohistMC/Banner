package com.mohistmc.banner.mixin.server.bossevents;

import com.mohistmc.banner.injection.server.bossevents.InjectionCustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvent;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.craftbukkit.boss.CraftKeyedBossbar;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomBossEvent.class)
public class MixinCustomBossEvent implements InjectionCustomBossEvent {

    private KeyedBossBar bossBar;

    @Override
    public KeyedBossBar getBukkitEntity() {
        if (bossBar == null) {
            bossBar = new CraftKeyedBossbar(((CustomBossEvent) (Object) this));
        }
        return bossBar;
    }
}
