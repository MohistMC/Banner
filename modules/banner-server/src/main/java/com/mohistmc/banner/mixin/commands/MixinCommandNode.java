package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CommandNode.class, remap = false)
public abstract class MixinCommandNode<S> implements Comparable<CommandNode<S>>, InjectionCommandNode {

    @Shadow @Final private Map<String, CommandNode<S>> children;

    @Shadow @Final private Map<String, LiteralCommandNode<S>> literals;

    @Shadow @Final private Map<String, ArgumentCommandNode<S, ?>> arguments;

    @Shadow @Final private Predicate<S> requirement;

    public void removeCommand(String name) {
        children.remove(name);
        literals.remove(name);
        arguments.remove(name);
    }

    // CraftBukkit start
    @Override
    public void banner$removeCommand(String name) {
        removeCommand(name);
    }
    // CraftBukkit end

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void banner$canUse(S source, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        if (source instanceof CommandSourceStack) {
            try {
                ((CommandSourceStack) source).banner$setCurrentCommand(((CommandNode<?>) (Object) this));
                cir.setReturnValue(requirement.test(source));
            } finally {
                ((CommandSourceStack) source).banner$setCurrentCommand(null);
            }
            // CraftBukkit end
        }
    }
}
