package org.spigotmc;

import java.io.File;

import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SpigotCommand extends Command {

    public SpigotCommand(String name) {
        super(name);
        this.description = "Spigot related commands";
        this.usageMessage = "/spigot reload";
        this.setPermission("bukkit.command.spigot");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) return true;

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.usageMessage);
            return false;
        }

        if (args[0].equals("reload")) {
            Command.broadcastCommandMessage(sender, ChatColor.RED + "Please note that this command is not supported and may cause issues.");
            Command.broadcastCommandMessage(sender, ChatColor.RED + "If you encounter any issues please use the /stop command to restart your server.");

            MinecraftServer console = BukkitMethodHooks.getServer();
            org.spigotmc.SpigotConfig.init((File) console.bridge$options().valueOf("spigot-settings"));
            for (ServerLevel world : console.getAllLevels()) {
                world.bridge$spigotConfig().init();
            }
            console.bridge$server().reloadCount++;

            Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Reload complete.");
        }

        return true;
    }
}
