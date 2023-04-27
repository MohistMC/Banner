package com.mohistmc.banner.injection.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;

public interface InjectionEntitySelectorParser {

    default EntitySelector parse(boolean overridePermissions) throws CommandSyntaxException {
        return null;
    }

    default void parseSelector(boolean overridePermissions) throws CommandSyntaxException {
    }
}
