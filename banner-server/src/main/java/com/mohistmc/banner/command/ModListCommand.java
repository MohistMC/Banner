package com.mohistmc.banner.command;

import com.google.common.collect.ImmutableList;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModListCommand extends BukkitCommand {

    public ModListCommand(@NotNull String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/fabricmods";
        this.setPermission("banner.command.mods");
        this.setAliases(List.of("fabricmods"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender.hasPermission("banner.command.mods")) {
            StringBuilder mods = new StringBuilder();
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                String name = mod.getMetadata().getName();

                if (name.startsWith("Fabric") && name.endsWith(")")) continue; // Don't list all modules of FAPI
                if (name.startsWith("Banner") && !name.endsWith("Mod")) continue;// Don't list all modules of Banner
                if (name.startsWith("Fabric API Base")) name = "Fabric API";
                if (name.startsWith("OpenJDK")) name = name.replace(" 64-Bit Server VM",""); // Shorten
                if (name.startsWith("Minecraft")) continue;

                if (!mods.toString().contains(name)) {
                    mods.append(", " + ChatColor.GREEN).append(name).append(ChatColor.WHITE);
                }
            }
            sender.sendMessage("Mods: " + mods.substring(2));
        } else {
            sender.sendMessage("No Permission for command!");
        }
        return true;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return ImmutableList.of();
    }

}
