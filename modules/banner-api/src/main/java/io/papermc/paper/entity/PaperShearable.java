package io.papermc.paper.entity;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Shearable;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

public interface PaperShearable extends io.papermc.paper.entity.Shearable {

    Shearable getHandle();

    @Override
    default boolean readyToBeSheared() {
        return this.getHandle().readyForShearing();
    }

    @Override
    default void shear(@NotNull Sound source) {
        this.getHandle().shear(SoundSource.valueOf(source.getKey().getKey()));// Banner - since we do not implement kyori so change it
    }
}