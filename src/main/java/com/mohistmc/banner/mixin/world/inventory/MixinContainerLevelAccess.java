package com.mohistmc.banner.mixin.world.inventory;

import com.mohistmc.banner.injection.world.inventory.InjectionContainerLevelAccess;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerLevelAccess.class)
public interface MixinContainerLevelAccess extends InjectionContainerLevelAccess {

    @Shadow <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction);

    @Override
    default Level getWorld() {
        return this.evaluate((level, blockpos) -> level).orElse(null); // Mohist
    }

    @Override
    default BlockPos getPosition() {
        return this.evaluate((level, blockpos) -> blockpos).orElse(null); // Mohist
    }

    @Override
    default Location getLocation() {
        return new Location(getWorld().getWorld(), getPosition().getX(), getPosition().getY(), getPosition().getZ());
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    static ContainerLevelAccess create(final Level level, final BlockPos pos) {
        return new ContainerLevelAccess() {
            // CraftBukkit start
            @Override
            public Level getWorld() {
                return level;
            }

            @Override
            public BlockPos getPosition() {
                return pos;
            }
            // CraftBukkit end

            @Override
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> levelPosConsumer) {
                return Optional.of(levelPosConsumer.apply(level, pos));
            }
        };
    }
}
