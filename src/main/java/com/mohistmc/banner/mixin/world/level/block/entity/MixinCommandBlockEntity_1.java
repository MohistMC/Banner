package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.command.CraftBlockCommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/level/block/entity/CommandBlockEntity$1")
public abstract class MixinCommandBlockEntity_1 implements CommandSource {

    @Shadow(aliases = {"field_11921"}, remap = false) private CommandBlockEntity outerThis;

    @Override
    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return new CraftBlockCommandSender(wrapper, outerThis);
    }
}
