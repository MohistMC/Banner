package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionCatalystListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SculkCatalystBlockEntity.CatalystListener.class)
public class MixinSculkCatalystBlockEntity_CatalystListener implements InjectionCatalystListener {

    @Shadow @Final
    SculkSpreader sculkSpreader;

    @Override
    public void banner$setLevel(Level level) {
        this.sculkSpreader.banner$setLevel(level);
    }
}
