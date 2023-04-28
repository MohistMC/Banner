package com.mohistmc.banner.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(WeightedPressurePlateBlock.class)
public class MixinWeightedPressurePlateBlock {

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
    }
}
