package com.mohistmc.banner.mixin.core.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.block.CraftEndGateway;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TheEndGatewayBlockEntity.class)
public abstract class MixinTheEndGatewayBlockEntity extends TheEndPortalBlockEntity {

    @Shadow
    private static void triggerCooldown(Level level, BlockPos pos, BlockState state, TheEndGatewayBlockEntity blockEntity) {
    }

    protected MixinTheEndGatewayBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }


    // Banner TODO fixme
    /*
    @Inject(method = "findOrCreateValidTeleportPos", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TheEndGatewayBlockEntity;findValidSpawnInChunk(Lnet/minecraft/world/level/chunk/LevelChunk;)Lnet/minecraft/core/BlockPos;"))
    private static void banner$portal(ServerLevel level, BlockPos blockPos, CallbackInfoReturnable<BlockPos> cir, BlockPos dest, Entity entityIn) {
        if (entityIn instanceof ServerPlayer) {
            CraftPlayer player = ((ServerPlayer) entityIn).getBukkitEntity();
            Location location = CraftLocation.toBukkit(dest, level.getWorld()).add(0.5D, 0.5D, 0.5D);
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());

            PlayerTeleportEvent event =  new com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent(player, player.getLocation(), location, new CraftEndGateway(level.getWorld(), blockEntity)); // Paper
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
                return;
            }

            entityIn.setPortalCooldown();
            ((((ServerPlayer) entityIn)).connection).teleport(event.getTo());
            triggerCooldown(level, pos, state, blockEntity);
            ci.cancel();
        }
    }*/
}