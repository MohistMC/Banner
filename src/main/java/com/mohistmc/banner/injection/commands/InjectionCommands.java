package com.mohistmc.banner.injection.commands;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;

public interface InjectionCommands {

    default int dispatchServerCommand(CommandSourceStack sender, String command) {
        return 0;
    }

    default int performPrefixedCommand(CommandSourceStack commandlistenerwrapper, String s, String label) {
        return 0;
    }

    default int performCommand(ParseResults<CommandSourceStack> parseresults, String s, String label) {
        return 0;
    }
}
