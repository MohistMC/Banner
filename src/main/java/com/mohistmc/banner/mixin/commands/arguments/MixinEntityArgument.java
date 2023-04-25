package com.mohistmc.banner.mixin.commands.arguments;

import com.mohistmc.banner.injection.commands.arguments.InjectionEntityArgument;
import net.minecraft.commands.arguments.EntityArgument;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityArgument.class)
public class MixinEntityArgument implements InjectionEntityArgument {
}
