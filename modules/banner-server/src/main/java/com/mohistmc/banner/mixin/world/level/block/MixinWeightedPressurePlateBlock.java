package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WeightedPressurePlateBlock.class)
public abstract class MixinWeightedPressurePlateBlock extends BasePressurePlateBlock {

    protected MixinWeightedPressurePlateBlock(Properties properties, BlockSetType blockSetType) {
        super(properties, blockSetType);
    }

    private static <T extends Entity> java.util.List<T> getEntities(Level world, AABB axisalignedbb, Class<T> oclass) {
        return world.getEntitiesOfClass(oclass, axisalignedbb, EntitySelector.NO_SPECTATORS.and((entity) -> {
            return !entity.isIgnoringBlockTriggers();
        }));
    }

    @Redirect(method = "getSignalStrength", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/WeightedPressurePlateBlock;getEntityCount(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/AABB;Ljava/lang/Class;)I"))
    public int banner$entityInteract(Level level, AABB aabb, Class<Entity> aClass, Level world, BlockPos pos) {
        int i = 0;
        for (Entity entity : getEntities(level, aabb, aClass)) {
            org.bukkit.event.Cancellable cancellable;

            if (entity instanceof Player) {
                cancellable = CraftEventFactory.callPlayerInteractEvent((Player) entity, Action.PHYSICAL, pos, null, null, null);
            } else {
                cancellable = new EntityInteractEvent((entity).getBukkitEntity(), CraftBlock.at(world, pos));
                world.getCraftServer().getPluginManager().callEvent((EntityInteractEvent) cancellable);
            }

            // We only want to block turning the plate on if all events are cancelled
            if (!cancellable.isCancelled()) {
                i++;
            }
        }
        return i;
    }
}
