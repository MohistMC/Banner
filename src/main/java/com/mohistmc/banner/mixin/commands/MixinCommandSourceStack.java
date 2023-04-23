package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommandSourceStack;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;

//TODO fix inject methods
@Mixin(CommandSourceStack.class)
public class MixinCommandSourceStack implements InjectionCommandSourceStack {
}
