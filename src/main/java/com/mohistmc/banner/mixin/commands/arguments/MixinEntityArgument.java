package com.mohistmc.banner.mixin.commands.arguments;

import com.mohistmc.banner.injection.commands.arguments.InjectionEntityArgument;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static net.minecraft.commands.arguments.EntityArgument.*;

@Mixin(EntityArgument.class)
public class MixinEntityArgument implements InjectionEntityArgument {

    @Shadow @Final
    boolean playersOnly;

    @Shadow @Final
    boolean single;

    @Override
    public EntitySelector parse(StringReader stringreader, boolean overridePermissions) throws CommandSyntaxException {
        int i = 0;
        EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringreader);
        EntitySelector entityselector = entityselectorparser.parse(overridePermissions);
        if (entityselector.getMaxResults() > 1 && this.single) {
            if (this.playersOnly) {
                stringreader.setCursor(0);
                throw ERROR_NOT_SINGLE_PLAYER.createWithContext(stringreader);
            } else {
                stringreader.setCursor(0);
                throw ERROR_NOT_SINGLE_ENTITY.createWithContext(stringreader);
            }
        } else if (entityselector.includesEntities() && this.playersOnly && !entityselector.isSelfSelector()) {
            stringreader.setCursor(0);
            throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(stringreader);
        } else {
            return entityselector;
        }
    }
}
