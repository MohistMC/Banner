package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommandSource;
import net.minecraft.commands.CommandSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSource.class)
public interface MixinCommandSource extends InjectionCommandSource {
}
