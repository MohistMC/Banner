package com.mohistmc.banner.bukkit.pluginfix;

public interface IPluginFixer {

    byte[] injectPluginFix(String className, byte[] clazz);

}
