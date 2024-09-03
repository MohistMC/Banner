package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(BellBlockEntity.class)
public class MixinBellBlockEntity {

    @Redirect(method = "makeRaidersGlow", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V"))
    private static void banner$bellResonate(Stream<LivingEntity> instance, Consumer<? super LivingEntity> consumer, Level level, BlockPos pos) {
        var list = instance.map(it -> (org.bukkit.entity.LivingEntity) (it).getBukkitEntity()).toList();
        CraftEventFactory.handleBellResonateEvent(level, pos, list).forEach(consumer);
    }
}
