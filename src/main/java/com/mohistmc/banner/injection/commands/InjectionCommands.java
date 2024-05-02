package com.mohistmc.banner.injection.commands;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;

public interface InjectionCommands {

    default void dispatchServerCommand(CommandSourceStack sender, String command) {
    }

    default void performPrefixedCommand(CommandSourceStack commandlistenerwrapper, String s, String label) {
    }

    default void performCommand(ParseResults<CommandSourceStack> parseresults, String s, String label) {
    }
}
