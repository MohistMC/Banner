package com.mohistmc.banner.mixin.world.level.gameevent.vibrations;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftGameEvent;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VibrationSystem.Listener.class)
public abstract class MixinVibrationSystem_Listener {

    @Shadow @Final private VibrationSystem system;

    @Shadow
    private static boolean isOccluded(Level level, Vec3 vec3, Vec3 vec32) {return false;}

    @Shadow protected abstract void scheduleVibration(ServerLevel serverLevel, VibrationSystem.Data data, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec3, Vec3 vec32);

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean handleGameEvent(ServerLevel serverLevel, Holder<GameEvent> holder, GameEvent.Context context, Vec3 vec3) {
        VibrationSystem.Data data = this.system.getVibrationData();
        VibrationSystem.User user = this.system.getVibrationUser();
        if (data.getCurrentVibration() != null) {
            return false;
        } else if (!user.isValidVibration(holder, context)) {
            return false;
        } else {
            Optional<Vec3> optional = user.getPositionSource().getPosition(serverLevel);
            if (optional.isEmpty()) {
                return false;
            } else {
                Vec3 vec32 = (Vec3)optional.get();

                boolean defaultCancel = !user.canReceiveVibration(serverLevel, BlockPos.containing(vec3), holder, context);
                Entity entity = context.sourceEntity();
                BlockReceiveGameEvent event = new BlockReceiveGameEvent(CraftGameEvent.minecraftToBukkit(holder.value()), CraftBlock.at(serverLevel, BlockPos.containing(vec3)), (entity == null) ? null : entity.getBukkitEntity());
                event.setCancelled(defaultCancel);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    // CraftBukkit end
                    return false;
                } else if (isOccluded(serverLevel, vec3, vec32)) {
                    return false;
                } else {
                    this.scheduleVibration(serverLevel, data, holder, context, vec3, vec32);
                    return true;
                }
            }
        }
    }
}
