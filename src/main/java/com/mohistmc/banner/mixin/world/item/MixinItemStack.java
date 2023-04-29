package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.injection.world.item.InjectionItemStack;
import com.mohistmc.banner.util.ServerUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements InjectionItemStack {

    @Shadow public abstract CompoundTag save(CompoundTag compoundTag);

    @Override
    public void convertStack(int version) {
        if (0 < version && version < CraftMagicNumbers.INSTANCE.getDataVersion()) {
            CompoundTag savedStack = new CompoundTag();
            this.save(savedStack);
            savedStack = (CompoundTag) ServerUtils.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic(NbtOps.INSTANCE, savedStack), version, CraftMagicNumbers.INSTANCE.getDataVersion()).getValue();
            this.load(savedStack);
        }
    }
}
