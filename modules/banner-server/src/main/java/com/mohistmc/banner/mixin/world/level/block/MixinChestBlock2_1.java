package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("public-target")
@Mixin(targets = "net/minecraft/world/level/block/ChestBlock$2$1")
public class MixinChestBlock2_1 {

    @Shadow(aliases = {"val$container", "field_17360"}) private Container container;
    public CompoundContainer inventorylargechest = (CompoundContainer) container;
}
