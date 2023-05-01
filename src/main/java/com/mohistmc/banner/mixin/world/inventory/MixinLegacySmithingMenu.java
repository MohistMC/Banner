package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventorySmithing;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("removal")
@Mixin(LegacySmithingMenu.class)
public abstract class MixinLegacySmithingMenu extends ItemCombinerMenu {


    private CraftInventoryView bukkitEntity;

    public MixinLegacySmithingMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void banner$prepareSmithing(ResultContainer craftResultInventory, int index, ItemStack stack) {
        CraftEventFactory.callPrepareSmithingEvent(getBukkitView(), stack);
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        }
        CraftInventory inventory = new CraftInventorySmithing(this.access.getLocation(), this.inputSlots, this.resultSlots);
        return this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, (LegacySmithingMenu) (Object) this);
    }
}
