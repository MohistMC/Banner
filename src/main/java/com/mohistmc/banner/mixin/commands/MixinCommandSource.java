package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommandSource;
import net.minecraft.commands.CommandSource;
import org.spongepowered.asm.mixin.Mixin;

// TODO fix inject methods
@Mixin(CommandSource.class)
public class MixinCommandSource implements InjectionCommandSource {
}
