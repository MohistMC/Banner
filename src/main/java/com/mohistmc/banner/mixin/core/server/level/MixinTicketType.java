package com.mohistmc.banner.mixin.core.server.level;

import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Comparator;

@Mixin(TicketType.class)
public class MixinTicketType {

    private static final TicketType<Unit> PLUGIN = TicketType.create("plugin", (a, b) -> 0);
    private static final TicketType<Plugin> PLUGIN_TICKET = TicketType.create("plugin_ticket", Comparator.comparing(it -> it.getClass().getName()));

}
