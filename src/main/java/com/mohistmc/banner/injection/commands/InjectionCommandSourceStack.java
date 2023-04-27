package com.mohistmc.banner.injection.commands;

import com.mojang.brigadier.tree.CommandNode;

public interface InjectionCommandSourceStack {

    default boolean hasPermission(int i, String bukkitPermission) {
        return false;
    }

    default org.bukkit.command.CommandSender getBukkitSender() {
        return null;
    }

    default CommandNode<?> bridge$getCurrentCommand() {
        return null;
    }

    default void banner$setCurrentCommand(CommandNode<?> node) {
    }
}
