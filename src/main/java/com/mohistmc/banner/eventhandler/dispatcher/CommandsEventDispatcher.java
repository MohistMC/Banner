package com.mohistmc.banner.eventhandler.dispatcher;

import com.mohistmc.banner.api.ServerAPI;
import com.mohistmc.banner.fabric.ModCustomCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.help.GenericCommandHelpTopic;

public class CommandsEventDispatcher {

    public static void onCommandDispatch() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (Bukkit.getServer() instanceof CraftServer craftServer) {
                Commands bukkit$dispatcher = craftServer.getServer().getCommands();
                ModCustomCommand wrapper = new ModCustomCommand(bukkit$dispatcher, dispatcher.getRoot());
                ServerAPI.fabriccmdper.put(wrapper.getName(), wrapper.getPermission());
                craftServer.helpMap.addTopic(new GenericCommandHelpTopic(wrapper));
                if (!wrapper.getName().isEmpty()) {
                    Bukkit.getLogger().info("ModsCommandDispatcher register " + wrapper.getName());
                }
            }
        });
    }
}
