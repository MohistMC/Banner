package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import java.util.Comparator;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TicketType.class)
public class MixinTicketType {

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)
    private static final TicketType<Unit> PLUGIN = TicketType.create("plugin", (a, b) -> 0);
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)
    private static final TicketType<Plugin> PLUGIN_TICKET = TicketType.create("plugin_ticket", Comparator.comparing(it -> it.getClass().getName()));

}
