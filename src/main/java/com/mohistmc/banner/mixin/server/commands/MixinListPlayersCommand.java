package com.mohistmc.banner.mixin.server.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.function.Function;

@Mixin(ListPlayersCommand.class)
public class MixinListPlayersCommand {

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private static int format(CommandSourceStack source, Function<ServerPlayer, Component> nameExtractor) {
        PlayerList playerList = source.getServer().getPlayerList();
        List<ServerPlayer> list = playerList.getPlayers();
        // CraftBukkit start
        if (source.getBukkitSender() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player sender = (org.bukkit.entity.Player) source.getBukkitSender();
            list = list.stream().filter((ep) -> sender.canSee(ep.getBukkitEntity())).collect(java.util.stream.Collectors.toList());
        }
        // CraftBukkit end
        Component component = ComponentUtils.formatList(list, nameExtractor);
        source.sendSuccess(Component.translatable("commands.list.players", new Object[]{list.size(), playerList.getMaxPlayers(), component}), false);
        return list.size();
    }
}
