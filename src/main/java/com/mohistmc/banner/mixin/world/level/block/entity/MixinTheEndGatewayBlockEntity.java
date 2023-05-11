package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TheEndGatewayBlockEntity.class)
public abstract class MixinTheEndGatewayBlockEntity extends TheEndPortalBlockEntity {

    @Shadow
    private static void triggerCooldown(Level level, BlockPos pos, BlockState state, TheEndGatewayBlockEntity blockEntity) {
    }

    protected MixinTheEndGatewayBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }


    @Inject(method = "teleportEntity", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPortalCooldown()V"))
    private static void banner$portal(Level level, BlockPos pos, BlockState state, Entity entityIn, TheEndGatewayBlockEntity blockEntity, CallbackInfo ci, BlockPos dest, Entity entity3) {
        if (entityIn instanceof ServerPlayer) {
            CraftPlayer player = ((ServerPlayer) entityIn).getBukkitEntity();
            Location location = new Location(level.getWorld(), dest.getX() + 0.5D, dest.getY() + 0.5D, dest.getZ() + 0.5D);
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());

            PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.END_GATEWAY);
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
    }
}
