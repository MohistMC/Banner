package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeehiveBlock.class)
public class MixinBeehiveBlock extends Block {

    public MixinBeehiveBlock(Properties properties) {
        super(properties);
    }

    @Redirect(method = "angerNearbyBees", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Bee;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void banner$targetReason(Bee beeEntity, LivingEntity livingEntity) {
        beeEntity.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
        beeEntity.setTarget(livingEntity);
    }

    // CraftBukkit start - fix MC-227255
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BeehiveBlock.FACING, rotation.rotate(state.getValue(BeehiveBlock.FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BeehiveBlock.FACING)));
    }
    // CraftBukkit end
}
