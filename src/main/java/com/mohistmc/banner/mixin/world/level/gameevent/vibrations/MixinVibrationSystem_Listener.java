package com.mohistmc.banner.mixin.world.level.gameevent.vibrations;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(VibrationSystem.Listener.class)
public interface MixinVibrationSystem_Listener {

    // Banner - TODO
    /**
     * @author wdog5
     * @reason
     */
    /**
    @Overwrite
    default boolean handleGameEvent(ServerLevel level, GameEvent gameEvent, GameEvent.Context context, Vec3 pos) {
        VibrationSystem.Data data = this.system.getVibrationData();
        VibrationSystem.User user = this.system.getVibrationUser();
        if (data.getCurrentVibration() != null) {
            return false;
        } else if (!user.isValidVibration(gameEvent, context)) {
            return false;
        } else {
            Optional<Vec3> optional = user.getPositionSource().getPosition(level);
            if (optional.isEmpty()) {
                return false;
            } else {
                Vec3 vec3 = (Vec3)optional.get();

                // CraftBukkit start
                boolean defaultCancel = !this.shouldListen(worldserver, (VibrationListener) (Object) this, BlockPos.containing(vec3d), gameevent, gameevent_a);
                Entity entity = gameevent_a.sourceEntity();
                BlockReceiveGameEvent event = new BlockReceiveGameEvent(org.bukkit.GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.GAME_EVENT.getKey(gameevent))), CraftBlock.at(worldserver, BlockPos.containing(vec3d1)), (entity == null) ? null : entity.getBukkitEntity());
                event.setCancelled(defaultCancel);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return false;
                } else if (isOccluded(level, pos, vec3)) {
                    return false;
                } else {
                    this.scheduleVibration(level, data, gameEvent, context, pos, vec3);
                    return true;
                }
            }
        }
    }*/
}
