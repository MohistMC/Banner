package com.mohistmc.banner.mixin.world.entity.vehicle;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftMinecartCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecartCommandBlock.MinecartCommandBase.class)
public abstract class MixinMinecartCommandBlock_MinecartCommandBase implements CommandSource {

    @SuppressWarnings("target") @Shadow(aliases = {"field_7745"}, remap = false)
    private MinecartCommandBlock outerThis;

    @Override
    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return (CraftMinecartCommand) outerThis.getBukkitEntity();
    }
}
