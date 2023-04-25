package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.injection.world.item.InjectionItemStack;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class MixinItemStack implements InjectionItemStack {
}
