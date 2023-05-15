package com.mohistmc.banner.mixin.commands;

import com.google.common.collect.Maps;
import com.mohistmc.banner.injection.commands.InjectionCommandNode;
import com.mohistmc.banner.injection.commands.InjectionCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

@Mixin(Commands.class)
public abstract class MixinCommands implements InjectionCommands {

    @Mutable
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Shadow public abstract int performPrefixedCommand(CommandSourceStack source, String command);

    @Shadow public abstract int performCommand(ParseResults<CommandSourceStack> parseResults, String command);

    @Shadow protected abstract void fillUsableCommands(CommandNode<CommandSourceStack> rootCommandSource, CommandNode<SharedSuggestionProvider> rootSuggestion, CommandSourceStack source, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode);

    public void banner$constructor() {
        this.dispatcher.setConsumer((context, b, i) -> context.getSource().onCommandComplete(context, b, i));
    }

    @Override
    public int performPrefixedCommand(CommandSourceStack commandlistenerwrapper, String s, String label) {
        return this.performPrefixedCommand(commandlistenerwrapper, s);
    }

    @Override
    public int performCommand(ParseResults<CommandSourceStack> parseresults, String s, String label) {
        return this.performCommand(parseresults, s);
    }

    @Override
    public int dispatchServerCommand(CommandSourceStack sender, String command) {
        return this.performPrefixedCommand(sender, command);  // Banner - use vanilla like commands instead of bukkit like commands
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
