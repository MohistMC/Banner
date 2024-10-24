package com.mohistmc.banner.eventhandler.dispatcher;

import com.mohistmc.banner.fabric.FabricEventFactory;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.event.block.BlockBreakEvent;

public class FabricToBukkitEventDispatcher {

    public static void dispatchFabric2Bukkit() {
        FabricEventFactory.HOOK_BUKKIT.register(bukkitEvent -> {
            if (bukkitEvent instanceof BlockBreakEvent breakEvent) {
                var player = ((CraftPlayer) breakEvent.getPlayer()).getHandle();
                var block = ((CraftBlock) breakEvent.getBlock()).getHandle();
                var level = player.level();
                var pos = ((CraftBlock) breakEvent.getBlock()).getPosition();
                boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, block.getBlockState(pos), block.getBlockEntity(pos));
                breakEvent.setCancelled(!result);
            }
        });
    }
}
