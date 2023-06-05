package com.mohistmc.banner.mixin.world.level.gameevent.vibrations;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(VibrationSystem.Listener.class)
public abstract class MixinVibrationSystem_Listener {

    @Shadow @Final private VibrationSystem system;

    @Shadow
    private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec32) {return false;}

    @Shadow protected abstract void scheduleVibration(ServerLevel serverLevel, VibrationSystem.Data data, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3, Vec3 vec32);

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean handleGameEvent(ServerLevel level, GameEvent gameEvent, GameEvent.Context context, Vec3 pos) {
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

                boolean defaultCancel = !user.canReceiveVibration(level, BlockPos.containing(pos), gameEvent, context);
                Entity entity = context.sourceEntity();
                BlockReceiveGameEvent event = new BlockReceiveGameEvent(org.bukkit.GameEvent.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.GAME_EVENT.getKey(gameEvent))), CraftBlock.at(level, BlockPos.containing(vec3)), (entity == null) ? null : entity.getBukkitEntity());
                event.setCancelled(defaultCancel);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    // CraftBukkit end
                    return false;
                } else if (isOccluded(level, pos, vec3)) {
                    return false;
                } else {
                    this.scheduleVibration(level, data, gameEvent, context, pos, vec3);
                    return true;
                }
            }
        }
    }
}
