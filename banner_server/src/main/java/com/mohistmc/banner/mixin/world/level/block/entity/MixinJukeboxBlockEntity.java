package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.bukkit.DistValidate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(JukeboxBlockEntity.class)
public abstract class MixinJukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem {

    @Shadow private ItemStack item;
    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = MAX_STACK;
    public boolean opened;

    public MixinJukeboxBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public List<ItemStack> getContents() {
        return Collections.singletonList(this.item);
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        if (!DistValidate.isValid(level)) return null;
        return new Location(level.getWorld(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
    }

    // Banner TODO fixme
    /*
    @Redirect(method = "setRecordWithoutPlaying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;updateNeighborsAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V"))
    private void banner$levelNullCheck(Level instance, BlockPos p_46673_, Block p_46674_) {
        if (instance != null) {
            instance.updateNeighborsAt(p_46673_, p_46674_);
        }
    }*/
}
