package com.mohistmc.banner.mixin.server.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRuleCommand.class)
public class MixinGameRuleCommand {

    @Redirect(method = "setRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules banner$perWorldGameRule(MinecraftServer minecraftServer, CommandContext<CommandSourceStack> context) {
        return context.getSource().getLevel().getGameRules();
    }

    @Redirect(method = "queryRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private static GameRules banner$perWorldGameRule2(MinecraftServer minecraftServer, CommandSourceStack source) {
        return source.getLevel().getGameRules();
    }
}
