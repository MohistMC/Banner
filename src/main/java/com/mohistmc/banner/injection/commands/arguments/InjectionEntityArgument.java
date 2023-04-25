package com.mohistmc.banner.injection.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;

public interface InjectionEntityArgument {

    default EntitySelector parse(StringReader stringreader, boolean overridePermissions) throws CommandSyntaxException {
        return null;
    }
}
