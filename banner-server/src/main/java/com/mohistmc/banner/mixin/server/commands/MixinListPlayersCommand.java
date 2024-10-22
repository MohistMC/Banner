package com.mohistmc.banner.mixin.server.commands;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ListPlayersCommand.class)
public class MixinListPlayersCommand {

    @Inject(method = "format",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/ComponentUtils;formatList(Ljava/util/Collection;Ljava/util/function/Function;)Lnet/minecraft/network/chat/Component;"))
    private static void banner$format(CommandSourceStack source, Function<ServerPlayer, Component> nameExtractor, CallbackInfoReturnable<Integer> cir, @Local List<ServerPlayer> list) {
        // CraftBukkit start
        if (source.banner$getBukkitSender() instanceof org.bukkit.entity.Player sender) {
            list = list.stream().filter((ep) -> sender.canSee(ep.getBukkitEntity())).collect(java.util.stream.Collectors.toList());
        }
        // CraftBukkit end
    }
}
