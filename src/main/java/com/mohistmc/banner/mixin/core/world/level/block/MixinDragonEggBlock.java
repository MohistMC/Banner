package com.mohistmc.banner.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockFromToEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DragonEggBlock.class)
public class MixinDragonEggBlock {


    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    private void teleport(BlockState state, Level level, BlockPos pos) {
        WorldBorder worldBorder = level.getWorldBorder();

        for(int i = 0; i < 1000; ++i) {
            BlockPos blockPos = pos.offset(level.random.nextInt(16) - level.random.nextInt(16), level.random.nextInt(8) - level.random.nextInt(8), level.random.nextInt(16) - level.random.nextInt(16));
            if (level.getBlockState(blockPos).isAir() && worldBorder.isWithinBounds(blockPos)) {
                // CraftBukkit start
                org.bukkit.block.Block from = level.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
                org.bukkit.block.Block to = level.getWorld().getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                BlockFromToEvent event = new BlockFromToEvent(from, to);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }
                blockPos = new BlockPos(event.getToBlock().getX(), event.getToBlock().getY(), event.getToBlock().getZ());
                // CraftBukkit end
                if (level.isClientSide) {
                    for(int j = 0; j < 128; ++j) {
                        double d = level.random.nextDouble();
                        float f = (level.random.nextFloat() - 0.5F) * 0.2F;
                        float g = (level.random.nextFloat() - 0.5F) * 0.2F;
                        float h = (level.random.nextFloat() - 0.5F) * 0.2F;
                        double e = Mth.lerp(d, (double)blockPos.getX(), (double)pos.getX()) + (level.random.nextDouble() - 0.5) + 0.5;
                        double k = Mth.lerp(d, (double)blockPos.getY(), (double)pos.getY()) + level.random.nextDouble() - 0.5;
                        double l = Mth.lerp(d, (double)blockPos.getZ(), (double)pos.getZ()) + (level.random.nextDouble() - 0.5) + 0.5;
                        level.addParticle(ParticleTypes.PORTAL, e, k, l, (double)f, (double)g, (double)h);
                    }
                } else {
                    level.setBlock(blockPos, state, 2);
                    level.removeBlock(pos, false);
                }

                return;
            }
        }

    }

}
