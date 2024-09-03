package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionEntityGetter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(EntityGetter.class)
public interface MixinEntityGetter extends InjectionEntityGetter {

    @Shadow @Nullable Player getNearestPlayer(double d, double e, double f, double g, @Nullable Predicate<Entity> predicate);

    @Shadow List<? extends Player> players();

    @Override
    default @Nullable Player findNearbyPlayer(Entity entity, double maxDistance, @Nullable Predicate<Entity> predicate) {
        return this.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), maxDistance, predicate);
    }
}
