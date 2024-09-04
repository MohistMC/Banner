package com.mohistmc.banner.api.mixin;

import com.mohistmc.banner.api.event.block.BlockDestroyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelWriter;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelWriter.class)
public class MixinLevelWriter {

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void banner$fireDestroyEvent(BlockPos blockPos, boolean bl, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        BlockDestroyEvent banner$event = new BlockDestroyEvent(CraftBlock.at(entity.level(), blockPos), entity.getBukkitEntity());
        if (banner$event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }
}
