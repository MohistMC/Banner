package com.mohistmc.banner.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_19_R3.command.VanillaCommandWrapper;

public class BukkitDispatcher extends CommandDispatcher<CommandSourceStack> {

    private final Commands commands;

    public BukkitDispatcher(Commands commands) {
        this.commands = commands;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> command) {
        LiteralCommandNode<CommandSourceStack> node = command.build();
        if (!(node.getCommand() instanceof BukkitCommandWrapper)) {
            VanillaCommandWrapper wrapper = new VanillaCommandWrapper(this.commands, node);
            ((CraftServer) Bukkit.getServer()).getCommandMap().register("fabric", wrapper);
        }
        getRoot().addChild(node);
        return node;
    }
}
