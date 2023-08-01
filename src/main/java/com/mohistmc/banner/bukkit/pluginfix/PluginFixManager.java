package com.mohistmc.banner.bukkit.pluginfix;

import com.mohistmc.banner.bukkit.pluginfix.plugins.CitizensFixer;
import com.mohistmc.banner.bukkit.pluginfix.plugins.EssentialsFixer;
import com.mohistmc.banner.bukkit.pluginfix.plugins.WorldEditFixer;

import java.util.HashMap;
import java.util.Map;

public class PluginFixManager {

    private static Map<String, IPluginFixer> pluginFixer = new HashMap<>();

    static {
        putPluginFixer("Essentials", new EssentialsFixer());
        putPluginFixer("WorldEdit", new WorldEditFixer());
        putPluginFixer("Citizens", new CitizensFixer());
    }

    public static IPluginFixer getPluginToFix(String pluginName) {
        return pluginFixer.get(pluginName);
    }

    public static boolean putPluginFixer(String pluginName, IPluginFixer fixer) {
        if (!pluginFixer.containsKey(pluginName) && fixer != null) {
            pluginFixer.put(pluginName, fixer);
            return true;
        }
        return false;
    }
}
