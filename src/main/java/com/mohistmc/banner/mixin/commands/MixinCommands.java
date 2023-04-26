package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommands;
import com.mohistmc.banner.util.BukkitDispatcher;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.*;

// TODO fix inject methods
@Mixin(Commands.class)
public abstract class MixinCommands implements InjectionCommands {

    @Mutable
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Shadow public abstract int performPrefixedCommand(CommandSourceStack source, String command);

    @Shadow public abstract int performCommand(ParseResults<CommandSourceStack> parseResults, String command);

    public void banner$constructor() {
        this.dispatcher = new BukkitDispatcher((Commands) (Object) this);
        this.dispatcher.setConsumer((context, b, i) -> context.getSource().onCommandComplete(context, b, i));
    }

    @Override
    public int performPrefixedCommand(CommandSourceStack commandlistenerwrapper, String s, String label) {
        return this.performPrefixedCommand(commandlistenerwrapper, s);
    }

    @Override
    public int performCommand(ParseResults<CommandSourceStack> parseresults, String s, String label) {
        return this.performCommand(parseresults, s);
    }

}
