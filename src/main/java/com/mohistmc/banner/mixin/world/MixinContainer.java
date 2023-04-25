package com.mohistmc.banner.mixin.world;

import com.mohistmc.banner.injection.world.InjectionContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Container.class)
public interface MixinContainer extends InjectionContainer {

}
