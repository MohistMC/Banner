package com.mohistmc.banner.mixin.commands.arguments.selector;

import com.mohistmc.banner.injection.commands.arguments.selector.InjectionEntitySelectorParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySelectorParser.class)
public abstract class MixinEntitySelectorParser implements InjectionEntitySelectorParser {

    @Shadow private boolean usesSelectors;
    private Boolean banner$overridePermissions;

    @Shadow protected abstract void parseSelector() throws CommandSyntaxException;

    @Shadow public abstract EntitySelector parse() throws CommandSyntaxException;

    @Override
    public EntitySelector parse(boolean overridePermissions) throws CommandSyntaxException {
        try {
            this.banner$overridePermissions = overridePermissions;
            return this.parse();
        } finally {
            this.banner$overridePermissions = null;
        }
    }

    @Override
    public void parseSelector(boolean overridePermissions) throws CommandSyntaxException {
        this.usesSelectors = !overridePermissions;
        this.parseSelector();
    }

    @Inject(method = "parseSelector", at = @At("HEAD"))
    public void banner$onParserSelector(CallbackInfo ci) {
        if (this.banner$overridePermissions != null) {
            this.usesSelectors = !this.banner$overridePermissions;
        }
    }
}
