package com.mohistmc.banner.injection.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;

public interface InjectionEntityArgument {

    default EntitySelector parse(StringReader stringReader, boolean bl, boolean overridePermissions) throws CommandSyntaxException {
        throw new IllegalStateException("Not implemented");
    }
}
