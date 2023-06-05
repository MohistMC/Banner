package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FurnaceResultSlot.class)
public abstract class MixinFurnaceResultSlot extends Slot {

    // @formatter:off
    @Shadow
    private int removeCount;
    // @formatter:on

    public MixinFurnaceResultSlot(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }

    @Redirect(method = "checkTakeAchievements(Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;awardUsedRecipesAndPopExperience(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public void banner$furnaceDropExp(AbstractFurnaceBlockEntity furnace, ServerPlayer player, ItemStack stack) {
        ((AbstractFurnaceBlockEntity) this.container).bridge$dropExp(player, stack, this.removeCount);
    }
}
