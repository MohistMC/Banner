package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/level/block/entity/CommandBlockEntity$1")
public abstract class MixinCommandBlockEntity_1 implements CommandSource {

    @Shadow(aliases = {"field_11921"}, remap = false) private CommandBlockEntity outerThis;

    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return new CraftBlockCommandSender(wrapper, outerThis);
    }

    @Override
    public CommandSender banner$getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitSender(wrapper);
    }
}
