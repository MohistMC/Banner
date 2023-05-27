package com.mohistmc.banner.mixin.core.server.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Function;

@Mixin(ListPlayersCommand.class)
public class MixinListPlayersCommand {

    @Inject(method = "format",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/chat/ComponentUtils;formatList(Ljava/util/Collection;Ljava/util/function/Function;)Lnet/minecraft/network/chat/Component;",
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$format(CommandSourceStack source, Function<ServerPlayer, Component> nameExtractor, CallbackInfoReturnable<Integer> cir, PlayerList playerList, List<ServerPlayer> list) {
        // CraftBukkit start
        if (source.getBukkitSender() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player sender = (org.bukkit.entity.Player) source.getBukkitSender();
            list = list.stream().filter((ep) -> sender.canSee(ep.getBukkitEntity())).collect(java.util.stream.Collectors.toList());
        }
        // CraftBukkit end
    }
}
