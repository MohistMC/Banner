package com.mohistmc.banner.mixin.commands.arguments;

import com.mohistmc.banner.injection.commands.arguments.InjectionEntityArgument;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityArgument.class)
public abstract class MixinEntityArgument implements InjectionEntityArgument {

    // @formatter:off
    @Shadow @Final boolean single;
    @Shadow @Final boolean playersOnly;
    @Final@Shadow public static SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER;
    @Final@Shadow public static SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY;
    @Final@Shadow public static SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED;
    // @formatter:on

    @Override
    public EntitySelector parse(StringReader stringReader, boolean bl, boolean overridePermissions) throws CommandSyntaxException {
        int i = 0;
        EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringReader, bl);
        EntitySelector entityselector =entityselectorparser.parse(overridePermissions);
        if (entityselector.getMaxResults() > 1 && this.single) {
            if (this.playersOnly) {
                stringReader.setCursor(0);
                throw ERROR_NOT_SINGLE_PLAYER.createWithContext(stringReader);
            } else {
                stringReader.setCursor(0);
                throw ERROR_NOT_SINGLE_ENTITY.createWithContext(stringReader);
            }
        } else if (entityselector.includesEntities() && this.playersOnly && !entityselector.isSelfSelector()) {
            stringReader.setCursor(0);
            throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(stringReader);
        } else {
            return entityselector;
        }
    }
}
