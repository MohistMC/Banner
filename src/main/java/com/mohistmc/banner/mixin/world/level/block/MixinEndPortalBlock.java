package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlock.class)
public class MixinEndPortalBlock {

    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;"))
    public void banner$enterPortal(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        EntityPortalEnterEvent event = new EntityPortalEnterEvent(entity.getBukkitEntity(),
                new org.bukkit.Location(level.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
                new Location(level.getWorld(), pos.getX(), pos.getY(), pos.getZ());
        Bukkit.getPluginManager().callEvent(event);
    }
}
