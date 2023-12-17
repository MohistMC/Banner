package com.mohistmc.banner.plugins.pluginmanager;

import com.mohistmc.banner.util.I18n;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginManagers {

    public static String permission = "banner.command.plugin";

    public static boolean loadPluginCommand(CommandSender sender, String label, String[] split) {
        if (split.length < 2) {
            Object[] f = {label};
            sender.sendMessage(I18n.as("pluginscommand.load", f));
            return false;
        }
        String pluginName = split[1];
        String jarName = split[1] + (pluginName.endsWith(".jar") ? "" : ".jar");
        File toLoad = new File("plugins" + File.separator + jarName);

        if (!toLoad.exists()) {
            jarName = pluginName + (pluginName.endsWith(".jar") ? ".unloaded" : ".jar.unloaded");
            toLoad = new File("plugins" + File.separator + jarName);
            if (!toLoad.exists()) {
                sender.sendMessage(I18n.as("pluginscommand.nofile",  pluginName));
                return false;
            } else {
                String fileName = jarName.substring(0, jarName.length() - (".unloaded".length()));
                toLoad = new File("plugins" + File.separator + fileName);
                File unloaded = new File("plugins" + File.separator + jarName);
                unloaded.renameTo(toLoad);
            }
        }

        PluginDescriptionFile desc = Control.getDescription(toLoad);
        if (desc == null) {
            sender.sendMessage(I18n.as("pluginscommand.noyml",  pluginName));
            return false;
        }
        Plugin[] pl = Bukkit.getPluginManager().getPlugins();
        for (Plugin p : pl) {
            if (desc.getName().equals(p.getName())) {
                sender.sendMessage(I18n.as("pluginscommand.alreadyloaded", desc.getName()));
                return true;
            }
        }
        Plugin p = Control.loadPlugin(toLoad);
        if (p != null) {
            Bukkit.getServer().getPluginManager().enablePlugin(p);
            sender.sendMessage(I18n.as("pluginscommand.loaded", p.getDescription().getName(), p.getDescription().getVersion()));
        } else {
            sender.sendMessage(I18n.as("pluginscommand.notload", pluginName));
            return false;
        }

        return true;
    }

    public static boolean unloadPluginCommand(CommandSender sender, String label, String[] split) {
        if (split.length < 2) {
            sender.sendMessage(I18n.as("pluginscommand.unload", label));
            return true;
        }

        String pluginName = split[1];
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin(pluginName);

        if (p == null) {
            sender.sendMessage(I18n.as("pluginscommand.noplugin", pluginName));
        } else {
            if (!Control.unloadPlugin(p)) {
                sender.sendMessage(I18n.as("pluginscommand.unloaderror", p.getDescription().getName(), p.getDescription().getVersion()));
            }
            if (Control.unloadPlugin(p)) {
                sender.sendMessage(I18n.as("pluginscommand.unloaded", p.getDescription().getName(), p.getDescription().getVersion()));
            } else {
                sender.sendMessage(I18n.as("pluginscommand.notunload", pluginName));
                return false;
            }
        }

        return true;
    }

    public static boolean reloadPluginCommand(CommandSender sender, String label, String[] split) {

        if (split.length < 2) {
            sender.sendMessage(I18n.as("pluginscommand.reload", new Object[] {label}));
            return false;
        }

        String pluginName = split[1];
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin(pluginName);

        if (p == null) {
            sender.sendMessage(I18n.as("pluginscommand.noplugin", pluginName));
            return false;
        } else {
            File file = Control.getFile((JavaPlugin) p);

            if (file == null) {
                sender.sendMessage(I18n.as("pluginscommand.nojar", p.getName()));
                return true;
            }

            File name = new File("plugins" + File.separator + file.getName());
            JavaPlugin loaded = null;
            if (!Control.unloadPlugin(p)) {
                sender.sendMessage(I18n.as("pluginscommand.reloaderror", pluginName));
                return false;
            } else if ((loaded = (JavaPlugin) Control.loadPlugin(name)) == null) {
                sender.sendMessage(I18n.as("pluginscommand.nojar", pluginName));
                return false;
            }

            Bukkit.getPluginManager().enablePlugin(loaded);
            sender.sendMessage(I18n.as("pluginscommand.reloaded", split[1], loaded.getDescription().getVersion()));
        }
        return true;
    }
}
