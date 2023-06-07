package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WeightedPressurePlateBlock.class)
public class MixinWeightedPressurePlateBlock {

    // Banner TODO
    /*
    @Redirect(method = "getSignalStrength", at = @At(value = "INVOKE", remap = false, target = "Ljava/util/List;size()I"))
    public int banner$entityInteract(List<Entity> list, Level worldIn, BlockPos pos) {
        int i = 0;
        for (Entity entity : list) {
            Cancellable cancellable;

            if (entity instanceof Player) {
                cancellable = CraftEventFactory.callPlayerInteractEvent((Player) entity, Action.PHYSICAL, pos, null, null, null);
            } else {
                cancellable = new EntityInteractEvent(entity.getBukkitEntity(), CraftBlock.at(worldIn, pos));
                Bukkit.getPluginManager().callEvent((EntityInteractEvent) cancellable);
            }

            // We only want to block turning the plate on if all events are cancelled
            if (!cancellable.isCancelled()) {
                i++;
            }
        }
        return i;
    }*/
}
