package com.mohistmc.banner.mixin.core.world.level;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseCommandBlock.class)
public abstract class MixinBaseCommandBlock implements CommandSource {

    // CraftBukkit start
    @Override
    public abstract org.bukkit.command.CommandSender getBukkitSender(CommandSourceStack wrapper);

}
