package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlock.class)
public abstract class MixinSculkSensorBlock extends Block {

    public MixinSculkSensorBlock(Properties properties) {
        super(properties);
    }

    @Inject(method = "stepOn", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private void banner$stepOn(Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        org.bukkit.event.Cancellable cancellable;
        if (entity instanceof Player) {
            cancellable = CraftEventFactory.callPlayerInteractEvent((Player) entity, org.bukkit.event.block.Action.PHYSICAL, pos, null, null, null);
        } else {
            cancellable = new org.bukkit.event.entity.EntityInteractEvent(entity.getBukkitEntity(), CraftBlock.at(level, pos));
            Bukkit.getPluginManager().callEvent((org.bukkit.event.entity.EntityInteractEvent) cancellable);
        }
        if (cancellable.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "deactivate", cancellable = true, at = @At("HEAD"))
    private static void banner$deactivate(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(CraftBlock.at(level, pos), state.getValue(SculkSensorBlock.POWER), 0);
        Bukkit.getPluginManager().callEvent(eventRedstone);

        if (eventRedstone.getNewCurrent() > 0) {
            level.setBlock(pos, state.setValue(SculkSensorBlock.POWER, eventRedstone.getNewCurrent()), 3);
            ci.cancel();
        }
    }

    @Unique private static int newCurrent;

    @Inject(method = "activate", cancellable = true, at = @At("HEAD"))
    private void banner$activate(Entity entity, Level level, BlockPos blockPos,
                                        BlockState blockState, int i, int j, CallbackInfo ci) {
        BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(CraftBlock.at(level, blockPos), blockState.getValue(SculkSensorBlock.POWER), i);
        level.getCraftServer().getPluginManager().callEvent(eventRedstone);
        if (eventRedstone.getNewCurrent() <= 0) {
            ci.cancel();
        }
        newCurrent = eventRedstone.getNewCurrent();
    }

    @ModifyVariable(method = "activate", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), argsOnly = true)
    private int banner$updateCurrent(int old) {
        return newCurrent;
    }

    @Override
    public int getExpDrop(BlockState blockState, ServerLevel world, BlockPos blockPos, ItemStack itemStack, boolean flag) {
        if (flag) {
            return this.banner$tryDropExperience(world, blockPos, itemStack, ConstantInt.of(5));
        }
        return 0;
    }
}
