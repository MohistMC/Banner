package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BaseFireBlock.class)
public class MixinBaseFireBlock {

    @Redirect(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void banner$onFire(Entity entity, float f) {
        var event = new EntityCombustByBlockEvent(CraftBlock.at(entity.level(), entity.getOnPos()), entity.getBukkitEntity(), f);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            entity.banner$setSecondsOnFire(event.getDuration(), false);
        }
    }

    @Redirect(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    public boolean banner$extinguish2(Level world, BlockPos pos, boolean isMoving) {
        if (!CraftEventFactory.callBlockFadeEvent(world, pos, Blocks.AIR.defaultBlockState()).isCancelled()) {
            world.removeBlock(pos, isMoving);
        }
        return false;
    }

    // Banner - no need
    /**
     @ModifyExpressionValue(method = "inPortalDimension", at = @At("RETURN"))
     private static boolean banner$inPortalDimension(Level level, CallbackInfoReturnable<Boolean> cir) {
     var typeKey = level.getTypeKey();
     return typeKey == LevelStem.NETHER || typeKey == LevelStem.OVERWORLD;
     }*/

}
