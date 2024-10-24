package com.mohistmc.banner.injection.commands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSource;

public interface InjectionCommandSourceStack {

    default void banner$setSource(CommandSource source) {
    }

    default boolean hasPermission(int i, String bukkitPermission) {
        return false;
    }

    default org.bukkit.command.CommandSender banner$getBukkitSender() {
        return null;
    }

    default CommandNode<?> bridge$getCurrentCommand() {
        return null;
    }

    default void banner$setCurrentCommand(CommandNode<?> node) {
    }
}
