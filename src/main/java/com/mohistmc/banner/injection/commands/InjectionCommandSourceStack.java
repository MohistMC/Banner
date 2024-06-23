package com.mohistmc.banner.injection.commands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSource;

public interface InjectionCommandSourceStack {

    default void banner$setSource(CommandSource source) {
        throw new IllegalStateException("Not implemented");
    }

    default boolean hasPermission(int i, String bukkitPermission) {
        throw new IllegalStateException("Not implemented");
    }

    default org.bukkit.command.CommandSender banner$getBukkitSender() {
        throw new IllegalStateException("Not implemented");
    }

    default CommandNode<?> bridge$getCurrentCommand() {
        throw new IllegalStateException("Not implemented");
    }

    default void banner$setCurrentCommand(CommandNode<?> node) {
        throw new IllegalStateException("Not implemented");
    }
}
