package com.mohistmc.banner.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.inventory.view.CraftFurnaceView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceMenu.class)
public abstract class MixinAbstractFurnaceMenu extends RecipeBookMenu<SingleRecipeInput, AbstractCookingRecipe> {

    @Shadow @Final private Container container;
    private CraftFurnaceView bukkitEntity = null;
    private Inventory player;

    public MixinAbstractFurnaceMenu(MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
    at = @At("RETURN"))
    private void banner$init(MenuType<?> menuType, RecipeType<?> recipeType, RecipeBookType recipeBookType, int i, Inventory inventory, Container container, ContainerData containerData, CallbackInfo ci) {
        this.player = inventory;
    }

    @Inject(method = "stillValid", at= @At("HEAD"), cancellable = true)
    private void banner$unreachable(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.bridge$checkReachable()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public CraftFurnaceView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryFurnace inventory = new CraftInventoryFurnace((AbstractFurnaceBlockEntity) this.container);
        bukkitEntity = new CraftFurnaceView (this.player.player.getBukkitEntity(), inventory, (AbstractFurnaceMenu) (Object) this);
        return bukkitEntity;
    }
}
