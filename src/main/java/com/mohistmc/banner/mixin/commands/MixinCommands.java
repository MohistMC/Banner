package com.mohistmc.banner.mixin.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.mohistmc.banner.injection.commands.InjectionCommandNode;
import com.mohistmc.banner.injection.commands.InjectionCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Commands.class)
public abstract class MixinCommands implements InjectionCommands {

    @Mutable @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Shadow protected abstract void fillUsableCommands(CommandNode<CommandSourceStack> rootCommandSource, CommandNode<SharedSuggestionProvider> rootSuggestion, CommandSourceStack source, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode);

    @Shadow public abstract int performCommand(ParseResults<CommandSourceStack> parseResults, String command);

    public void banner$constructor() {
        this.dispatcher.setConsumer((context, b, i) -> context.getSource().onCommandComplete(context, b, i));
    }

    /**
     * @author wdog5
     * @reason functionally replaced
     * TODO changed in inject and redirect
     */
    @Overwrite
    public int performPrefixedCommand(CommandSourceStack source, String command) {
        return this.performCommand(this.dispatcher.parse(command, source), command);
    }

    @Override
    public int performPrefixedCommand(CommandSourceStack commandlistenerwrapper, String s, String label) {
        s = s.startsWith("/") ? s.substring(1) : s;
        return this.performCommand(this.dispatcher.parse(s, commandlistenerwrapper), s, label);
    }

    private AtomicReference<String> banner$performLabel = new AtomicReference<>();
    private AtomicReference<String> banner$command = new AtomicReference<>();

    @Override
    public int performCommand(ParseResults<CommandSourceStack> parseResults, String command, String label) {
        this.banner$performLabel.set(label);
        return performCommand(parseResults, command);
    }

    @Inject(method = "performCommand", at = @At("HEAD"))
    private void banner$getCommandInfo(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        banner$command.set(command);
    }

    @Redirect(method = "performCommand", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent banner$resetCommandLabel(MutableComponent instance, ChatFormatting format) {
        var label = this.banner$performLabel.get();
        label = label == null ? "/" + banner$command.get() : label;
        String finalLabel = label;
        return Component.empty().withStyle(ChatFormatting.GRAY).withStyle((style) ->
                style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, finalLabel)));
    }

    @Override
    public int dispatchServerCommand(CommandSourceStack sender, String command) {
        Joiner joiner = Joiner.on(" ");
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        ServerCommandEvent event = new ServerCommandEvent(sender.getBukkitSender(), command);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return 0;
        }
        command = event.getCommand();

        String[] args = command.split(" ");

        String cmd = args[0];
        if (cmd.startsWith("minecraft:")) cmd = cmd.substring("minecraft:".length());
        if (cmd.startsWith("bukkit:")) cmd = cmd.substring("bukkit:".length());

        // Block disallowed commands
        if (cmd.equalsIgnoreCase("stop") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("op")
                || cmd.equalsIgnoreCase("deop") || cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("ban-ip")
                || cmd.equalsIgnoreCase("pardon") || cmd.equalsIgnoreCase("pardon-ip") || cmd.equalsIgnoreCase("reload")) {
            return 0;
        }

        // Handle vanilla commands;
        if (sender.getLevel().getCraftServer().getCommandBlockOverride(args[0])) {
            args[0] = "minecraft:" + args[0];
        }

        String newCommand = joiner.join(args);
        return this.performPrefixedCommand(sender, newCommand, newCommand);
    }

    /**
     * @author wdog5
     * @reason PlayerCommandSendEvent
     */
    @Overwrite
    public void sendCommands(ServerPlayer player) {
        Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map = Maps.newIdentityHashMap();
        RootCommandNode vanillaRoot = new RootCommandNode();

        RootCommandNode<CommandSourceStack> vanilla = player.getServer().bridge$getVanillaCommands().getDispatcher().getRoot();
        map.put(vanilla, vanillaRoot);
        this.fillUsableCommands(vanilla, vanillaRoot, player.createCommandSourceStack(), (Map) map);

        RootCommandNode<SharedSuggestionProvider> rootCommandNode = new RootCommandNode();
        map.put(this.dispatcher.getRoot(), rootCommandNode);
        this.fillUsableCommands(this.dispatcher.getRoot(), rootCommandNode, player.createCommandSourceStack(), map);

        Collection<String> bukkit = new LinkedHashSet<>();
        for (CommandNode node : rootCommandNode.getChildren()) {
            bukkit.add(node.getName());
        }

        PlayerCommandSendEvent event = new PlayerCommandSendEvent(player.getBukkitEntity(), new LinkedHashSet<>(bukkit));
        event.getPlayer().getServer().getPluginManager().callEvent(event);

        // Remove labels that were removed during the event
        for (String orig : bukkit) {
            if (!event.getCommands().contains(orig)) {
                ((InjectionCommandNode) rootCommandNode).removeCommand(orig);
            }
        }
        player.connection.send(new ClientboundCommandsPacket(rootCommandNode));
    }

}
