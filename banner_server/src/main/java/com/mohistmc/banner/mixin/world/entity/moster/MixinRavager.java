package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Ravager.class)
public class MixinRavager {

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$entityChangeBlock(Level world, BlockPos pos, boolean dropBlock, Entity entityIn) {
        return CraftEventFactory.callEntityChangeBlockEvent((Ravager) (Object) this, pos, Blocks.AIR.defaultBlockState())
                && world.destroyBlock(pos, dropBlock, entityIn);
    }
}
