package com.mohistmc.banner.mixin;

import com.mohistmc.banner.injection.world.InjectionSimpleContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(SimpleContainer.class)
public abstract class MixinSimpleContainer implements Container, StackedContentsCompatible, InjectionSimpleContainer {

    // @formatter:off
    @Shadow @Final public NonNullList<ItemStack> items;
    // @formatter:on

    private int maxStack = LARGE_MAX_STACK_SIZE;

    @Override
    public List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void setMaxStackSize(int i) {
        maxStack = i;
    }
}
