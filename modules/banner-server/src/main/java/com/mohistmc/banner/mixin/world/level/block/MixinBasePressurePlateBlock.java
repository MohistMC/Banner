package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BasePressurePlateBlock.class)
public abstract class MixinBasePressurePlateBlock {

    @Shadow protected abstract int getSignalStrength(Level level, BlockPos pos);

    @Redirect(method = "checkPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BasePressurePlateBlock;getSignalStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I"))
    private int banner$blockRedstone(BasePressurePlateBlock abstractPressurePlateBlock, Level worldIn, BlockPos pos, Entity entity, Level world, BlockPos blockPos, BlockState state, int oldRedstoneStrength) {
        int newStrength = this.getSignalStrength(worldIn, pos);
        boolean flag = oldRedstoneStrength > 0;
        boolean flag1 = newStrength > 0;

        if (flag != flag1 && DistValidate.isValid(world)) {
            BlockRedstoneEvent event = new BlockRedstoneEvent(CraftBlock.at(worldIn, blockPos), oldRedstoneStrength, newStrength);
            Bukkit.getPluginManager().callEvent(event);
            newStrength = event.getNewCurrent();
        }
        return newStrength;
    }
}
