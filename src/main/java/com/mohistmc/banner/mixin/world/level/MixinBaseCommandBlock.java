package com.mohistmc.banner.mixin.world.level;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.BaseCommandBlock;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseCommandBlock.class)
public abstract class MixinBaseCommandBlock implements CommandSource {

    @Override
    public abstract CommandSender getBukkitSender(CommandSourceStack wrapper);
}
