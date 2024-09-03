package com.mohistmc.banner.mixin.server.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(TeleportCommand.class)
public class MixinTeleportCommand {

    @Shadow @Final private static SimpleCommandExceptionType INVALID_POSITION;


    /**
     * @author wdog5
     * @reason Bukkit Tp Event
     */
    @Overwrite
    private static void performTeleport(CommandSourceStack source, Entity entity, ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeList, float yaw, float pitch, @Nullable TeleportCommand.LookAt facing) throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.containing(x, y, z);
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw INVALID_POSITION.create();
        } else {
            float f = Mth.wrapDegrees(yaw);
            float g = Mth.wrapDegrees(pitch);
            // CraftBukkit start - Teleport event
            boolean result;
            if (entity instanceof ServerPlayer player) {
                result = player.teleportTo(level, x, y, z, relativeList, f, g, PlayerTeleportEvent.TeleportCause.COMMAND);
            } else {
                Location to = new Location(level.getWorld(), x, y, z, f, g);
                EntityTeleportEvent event = new EntityTeleportEvent(entity.getBukkitEntity(), entity.getBukkitEntity().getLocation(), to);
                level.getCraftServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }

                x = to.getX();
                y = to.getY();
                z = to.getZ();
                f = to.getYaw();
                g = to.getPitch();

                level = ((CraftWorld) to.getWorld()).getHandle();
                result = entity.teleportTo(level, x, y, z, relativeList, f, g);
            }
            if (result) {
                // CraftBukkit end
                if (facing != null) {
                    facing.perform(source, entity);
                }

                label23: {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity.isFallFlying()) {
                            break label23;
                        }
                    }

                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                    entity.setOnGround(true);
                }

                if (entity instanceof PathfinderMob pathfinderMob) {
                    pathfinderMob.getNavigation().stop();
                }

            }
        }
    }

}
