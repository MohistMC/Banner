package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionAbstractFurnaceBlockEntity;
import io.izzel.arclight.mixin.Eject;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible, InjectionAbstractFurnaceBlockEntity {


    // @formatter:off
    @Shadow protected NonNullList<ItemStack> items;
    @Shadow protected abstract int getBurnDuration(ItemStack stack);
    @Shadow protected abstract boolean isLit();
    @Shadow @Final private Object2IntOpenHashMap<ResourceLocation> recipesUsed;
    @Shadow public abstract List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel p_154996_, Vec3 p_154997_);

    @Shadow
    protected static boolean canBurn(RegistryAccess p_266924_, @Nullable Recipe<?> p_155006_, NonNullList<ItemStack> p_155007_, int p_155008_) {
        return false;
    }
    // @formatter:on

    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = LARGE_MAX_STACK_SIZE;
    private static AbstractFurnaceBlockEntity banner$captureFurnace;
    private static Player banner$capturePlayer;
    private static ItemStack banner$item;
    private static int banner$captureAmount;

    protected MixinAbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Eject(method = "serverTick",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isLit()Z"))
    private static boolean banner$setBurnTime(AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
        ItemStack itemStack = furnace.getItem(1);
        CraftItemStack fuel = CraftItemStack.asCraftMirror(itemStack);
        FurnaceBurnEvent furnaceBurnEvent = new FurnaceBurnEvent(CraftBlock.at(furnace.level, furnace.getBlockPos()), fuel, furnace.getBurnDuration(itemStack));
        Bukkit.getPluginManager().callEvent(furnaceBurnEvent);

        if (furnaceBurnEvent.isCancelled()) {
            ci.cancel();
            return false;
        }
        return furnace.isLit() && furnaceBurnEvent.isBurning();
    }

    @Redirect(method = "createExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"))
    private static void banner$expEvent(ServerLevel level, Vec3 vec3, int amount) {
        if (banner$capturePlayer != null && banner$captureAmount != 0) {
            FurnaceExtractEvent event = new FurnaceExtractEvent(((ServerPlayer) banner$capturePlayer).getBukkitEntity(), CraftBlock.at(level, banner$captureFurnace.getBlockPos()), CraftMagicNumbers.getMaterial(banner$item.getItem()), banner$captureAmount, amount);
            Bukkit.getPluginManager().callEvent(event);
            amount = event.getExpToDrop();
        }
        ExperienceOrb.award(level, vec3, amount);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private static boolean burn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> nonNullList, int i) {
        if (recipe != null && canBurn(registryAccess, recipe, nonNullList, i)) {
            ItemStack itemStack = (ItemStack)nonNullList.get(0);
            ItemStack itemStack2 = recipe.getResultItem(registryAccess);
            ItemStack itemStack3 = (ItemStack)nonNullList.get(2);
            if (itemStack3.isEmpty()) {
                nonNullList.set(2, itemStack2.copy());
            } else if (itemStack3.is(itemStack2.getItem())) {
                itemStack3.grow(1);
            }

            if (itemStack.is(Blocks.WET_SPONGE.asItem()) && !((ItemStack)nonNullList.get(1)).isEmpty() && ((ItemStack)nonNullList.get(1)).is(Items.BUCKET)) {
                nonNullList.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemStack.shrink(1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel world, Vec3 vec, BlockPos pos, Player entity, ItemStack itemStack, int amount) {
        try {
            banner$item = itemStack;
            banner$captureAmount = amount;
            banner$captureFurnace = (AbstractFurnaceBlockEntity) (Object) this;
            banner$capturePlayer = entity;
            List<Recipe<?>> list = this.getRecipesToAwardAndPopExperience(world, vec);
            entity.awardRecipes(list);
            this.recipesUsed.clear();
            return list;
        } finally {
            banner$item = null;
            banner$captureAmount = 0;
            banner$captureFurnace = null;
            banner$capturePlayer = null;
        }
    }

    @Override
    public List<ItemStack> getContents() {
        return this.items;
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
    public void setOwner(InventoryHolder owner) {
    }

    @Override
    public int getMaxStackSize() {
        if (maxStack == 0) maxStack = LARGE_MAX_STACK_SIZE;
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public List<Recipe<?>> bridge$dropExp(ServerPlayer entity, ItemStack itemStack, int amount) {
        return getRecipesToAwardAndPopExperience(entity.getLevel(), entity.position(), this.worldPosition, entity, itemStack, amount);    }

    @Override
    public Object2IntOpenHashMap<ResourceLocation> getRecipesUsed() {
        return this.recipesUsed;
    }
}
