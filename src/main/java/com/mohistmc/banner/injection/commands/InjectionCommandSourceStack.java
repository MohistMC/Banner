package com.mohistmc.banner.injection.commands;

public interface InjectionCommandSourceStack {

    default boolean hasPermission(int i, String bukkitPermission) {
        return false;
    }

    default org.bukkit.command.CommandSender getBukkitSender() {
        return null;
    }
}
