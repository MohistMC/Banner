package com.mohistmc.banner.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.AABB;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PressurePlateBlock.class)
public abstract class MixinPressurePlateBlock extends BasePressurePlateBlock {

    protected MixinPressurePlateBlock(Properties properties, BlockSetType blockSetType) {
        super(properties, blockSetType);
    }

    @Shadow protected abstract int getSignalForState(BlockState state);

    @Shadow @Final private PressurePlateBlock.Sensitivity sensitivity;

    /**
     * @author wdig5
     * @reason bukkit event
     */
    @Overwrite
    protected int getSignalStrength(Level level, BlockPos pos) {
        AABB aABB = TOUCH_AABB.move(pos);
        List list;
        switch (this.sensitivity) {
            case EVERYTHING -> list = level.getEntities((Entity) null, aABB);
            case MOBS -> list = level.getEntitiesOfClass(LivingEntity.class, aABB);
            default -> {
                return 0;
            }
        }

        if (!list.isEmpty()) {

            for (Object o : list) {
                Entity entity = (Entity) o;

                // CraftBukkit start - Call interact event when turning on a pressure plate
                if (this.getSignalForState(level.getBlockState(pos)) == 0) {
                    org.bukkit.World bworld = level.getWorld();
                    org.bukkit.plugin.PluginManager manager = level.getCraftServer().getPluginManager();
                    org.bukkit.event.Cancellable cancellable;

                    if (entity instanceof Player) {
                        cancellable = org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory.callPlayerInteractEvent((Player) entity, org.bukkit.event.block.Action.PHYSICAL, pos, null, null, null);
                    } else {
                        cancellable = new EntityInteractEvent(entity.getBukkitEntity(), bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
                        manager.callEvent((EntityInteractEvent) cancellable);
                    }

                    // We only want to block turning the plate on if all events are cancelled
                    if (cancellable.isCancelled()) {
                        continue;
                    }
                }
                // CraftBukkit end

                if (!entity.isIgnoringBlockTriggers()) {
                    return 15;
                }
            }
        }

        return 0;
    }
}
