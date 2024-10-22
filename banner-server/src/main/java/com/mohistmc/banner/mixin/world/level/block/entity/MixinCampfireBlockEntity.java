package com.mohistmc.banner.mixin.world.level.block.entity;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.CampfireStartEvent;
import org.bukkit.inventory.CampfireRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlockEntity.class)
public abstract class MixinCampfireBlockEntity extends BlockEntity {

    @Shadow @Final public int[] cookingTime;

    public MixinCampfireBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }


    // Banner - fix mixin(locals = LocalCapture.CAPTURE_FAILSOFT)
    private static CraftItemStack source;
    private static  org.bukkit.inventory.ItemStack result;
    private static BlockCookEvent blockCookEvent;

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public static void cookTick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity, RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> cachedCheck) {
        boolean bl = false;

        for(int i = 0; i < campfireBlockEntity.getItems().size(); ++i) {
            ItemStack itemStack = (ItemStack) campfireBlockEntity.getItems().get(i);
            if (!itemStack.isEmpty()) {
                bl = true;
                int var10002 = campfireBlockEntity.cookingProgress[i]++;
                if (campfireBlockEntity.cookingProgress[i] >= campfireBlockEntity.cookingTime[i]) {
                    SingleRecipeInput singleRecipeInput = new SingleRecipeInput(itemStack);
                    ItemStack itemStack2 = (ItemStack)cachedCheck.getRecipeFor(singleRecipeInput, serverLevel).map((recipeHolder) -> {
                        return ((CampfireCookingRecipe)recipeHolder.value()).assemble(singleRecipeInput, serverLevel.registryAccess());
                    }).orElse(itemStack);

                    if (itemStack2.isItemEnabled(serverLevel.enabledFeatures())) {
                        // CraftBukkit start - fire BlockCookEvent
                        source = CraftItemStack.asCraftMirror(itemStack);
                        result = CraftItemStack.asBukkitCopy(itemStack2);

                        blockCookEvent = new BlockCookEvent(CraftBlock.at(serverLevel, blockPos), source, result);
                        serverLevel.getCraftServer().getPluginManager().callEvent(blockCookEvent);

                        if (blockCookEvent.isCancelled()) {
                            return;
                        }

                        result = blockCookEvent.getResult();
                        itemStack2 = CraftItemStack.asNMSCopy(result);
                        // CraftBukkit end
                        Containers.dropItemStack(serverLevel, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack2);
                        campfireBlockEntity.getItems().set(i, ItemStack.EMPTY);
                        serverLevel.sendBlockUpdated(blockPos, blockState, blockState, 3);
                        serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(blockState));
                    }
                }
            }
        }

        if (bl) {
            setChanged(serverLevel, blockPos, blockState);
        }

    }

    @Inject(method = "placeFood", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;cookingProgress:[I"))
    private void banner$cookStart(ServerLevel serverLevel, LivingEntity livingEntity, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir, int i, ItemStack itemStack2, Optional optional) {
        var event = new CampfireStartEvent(CraftBlock.at(this.level, this.worldPosition), CraftItemStack.asCraftMirror(itemStack), (CampfireRecipe) ((RecipeHolder) optional.get()).toBukkitRecipe());
        Bukkit.getPluginManager().callEvent(event);
        this.cookingTime[i] = event.getTotalCookTime();
    }
}
