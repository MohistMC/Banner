package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommands;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject methods
@Mixin(Commands.class)
public class MixinCommands implements InjectionCommands {
}
