package com.mohistmc.banner.api.mixin;

import com.mohistmc.banner.api.event.block.BlockDestroyEvent;
import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelWriter;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelWriter.class)
public interface MixinLevelWriter {

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void banner$fireDestroyEvent(BlockPos blockPos, boolean bl, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null) {
            var bukkitEntity = entity.getBukkitEntity() == null ? CraftEntity.getEntity(BukkitMethodHooks.getServer().bridge$server(), entity) : entity.getBukkitEntity();
            if (bukkitEntity != null) {
                BlockDestroyEvent banner$event = new BlockDestroyEvent(CraftLocation.toBukkit(blockPos, entity.level()), bukkitEntity);
                if (banner$event.isCancelled()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
