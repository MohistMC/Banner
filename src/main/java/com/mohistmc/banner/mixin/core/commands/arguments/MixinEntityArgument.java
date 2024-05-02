package com.mohistmc.banner.mixin.core.commands.arguments;

import com.mohistmc.banner.injection.commands.arguments.InjectionEntityArgument;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityArgument.class)
public abstract class MixinEntityArgument implements InjectionEntityArgument {

    @Shadow public abstract EntitySelector parse(StringReader reader) throws CommandSyntaxException;

    private AtomicBoolean banner$overridePerm = new AtomicBoolean(false);

    @Override
    public EntitySelector parse(StringReader stringreader, boolean overridePermissions) throws CommandSyntaxException {
        banner$overridePerm.set(overridePermissions);
        return parse(stringreader);
    }

    @Redirect(method = "parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/commands/arguments/selector/EntitySelector;",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;parse()Lnet/minecraft/commands/arguments/selector/EntitySelector;"))
    private EntitySelector banner$resetParse(EntitySelectorParser instance) throws CommandSyntaxException {
        return instance.parse(banner$overridePerm.getAndSet(false));
    }
}
