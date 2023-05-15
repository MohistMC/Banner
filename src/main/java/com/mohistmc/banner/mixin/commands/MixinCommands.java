package com.mohistmc.banner.mixin.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.mohistmc.banner.injection.commands.InjectionCommandNode;
import com.mohistmc.banner.injection.commands.InjectionCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Commands.class)
public abstract class MixinCommands implements InjectionCommands {

    @Mutable @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Shadow protected abstract void fillUsableCommands(CommandNode<CommandSourceStack> rootCommandSource, CommandNode<SharedSuggestionProvider> rootSuggestion, CommandSourceStack source, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode);

    @Shadow @Final private static Logger LOGGER;

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

    @Override
    public int performCommand(ParseResults<CommandSourceStack> parseResults, String command, String label) {
        CommandSourceStack commandSourceStack = (CommandSourceStack)parseResults.getContext().getSource();
        commandSourceStack.getServer().getProfiler().push(() -> {
            return "/" + command;
        });

        byte var18;
        try {
            int var4 = this.dispatcher.execute(parseResults);
            return var4;
        } catch (CommandRuntimeException var13) {
            commandSourceStack.sendFailure(var13.getComponent());
            var18 = 0;
            return var18;
        } catch (CommandSyntaxException var14) {
            commandSourceStack.sendFailure(ComponentUtils.fromMessage(var14.getRawMessage()));
            if (var14.getInput() != null && var14.getCursor() >= 0) {
                int i = Math.min(var14.getInput().length(), var14.getCursor());
                MutableComponent mutableComponent = Component.empty().withStyle(ChatFormatting.GRAY).withStyle((style) -> {
                    return style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, label));
                });
                if (i > 10) {
                    mutableComponent.append(CommonComponents.ELLIPSIS);
                }

                mutableComponent.append(var14.getInput().substring(Math.max(0, i - 10), i));
                if (i < var14.getInput().length()) {
                    Component component = Component.literal(var14.getInput().substring(i)).withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE});
                    mutableComponent.append(component);
                }

                mutableComponent.append(Component.translatable("command.context.here").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}));
                commandSourceStack.sendFailure(mutableComponent);
            }

            var18 = 0;
        } catch (Exception var15) {
            MutableComponent mutableComponent2 = Component.literal(var15.getMessage() == null ? var15.getClass().getName() : var15.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Command exception: /{}", command, var15);
                StackTraceElement[] stackTraceElements = var15.getStackTrace();

                for(int j = 0; j < Math.min(stackTraceElements.length, 3); ++j) {
                    mutableComponent2.append("\n\n").append(stackTraceElements[j].getMethodName()).append("\n ").append(stackTraceElements[j].getFileName()).append(":").append(String.valueOf(stackTraceElements[j].getLineNumber()));
                }
            }

            commandSourceStack.sendFailure(Component.translatable("command.failed").withStyle((style) -> {
                return style.withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, mutableComponent2));
            }));
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                commandSourceStack.sendFailure(Component.literal(Util.describeError(var15)));
                LOGGER.error("'/{}' threw an exception", command, var15);
            }

            byte var19 = 0;
            return var19;
        } finally {
            commandSourceStack.getServer().getProfiler().pop();
        }

        return var18;
    }

    /**
     * @author wdog5
     * @reason functionally replaced
     * TODO changed in inject and redirect
     */
    @Overwrite
    public int performCommand(ParseResults<CommandSourceStack> parseResults, String command) {
        return this.performCommand(parseResults, command, command);
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
