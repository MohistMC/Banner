package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Silverfish.class)
public abstract class MixinSilverfish extends Monster {

    protected MixinSilverfish(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author wdog5
     * @reason paper
     */
    @Overwrite
    public static boolean checkSilverfishSpawnRules(EntityType<Silverfish> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        if (checkAnyLightMonsterSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource)) {
            Player player = levelAccessor.getNearestPlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 5.0, true);
            return !(player != null && !player.bridge$affectsSpawning()) && player == null; // Paper - Affects Spawning API
        } else {
            return false;
        }
    }

}
