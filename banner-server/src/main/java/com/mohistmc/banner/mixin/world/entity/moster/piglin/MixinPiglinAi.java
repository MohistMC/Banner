package com.mohistmc.banner.mixin.world.entity.moster.piglin;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinAi.class)
public abstract class MixinPiglinAi {

    @Shadow private static void throwItems(Piglin pilgin, List<ItemStack> stacks) {}
    @Shadow private static List<ItemStack> getBarterResponseItems(Piglin piglin) {return null;}
    @Shadow private static boolean isBarterCurrency(ItemStack stack) {return false;}
    @Shadow protected static boolean isLovedItem(ItemStack item) {return false;}
    @Shadow private static void putInInventory(Piglin piglin, ItemStack stack) {}
    @Shadow private static void eat(Piglin piglin) {}
    @Shadow private static boolean hasEatenRecently(Piglin piglin) {return false;}
    @Shadow private static boolean isFood(ItemStack stack) {return false;}
    @Shadow private static void admireGoldItem(LivingEntity piglin) {}
    @Shadow private static void holdInOffhand(Piglin piglin, ItemStack stack) {}
    @Shadow private static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {return null;}
    @Shadow private static void stopWalking(Piglin piglin) {}

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected static void pickUpItem(Piglin piglinEntity, ItemEntity itemEntity) {
        ItemStack itemstack;
        stopWalking(piglinEntity);
        if (itemEntity.getItem().getItem() == Items.GOLD_NUGGET && !CraftEventFactory.callEntityPickupItemEvent(piglinEntity, itemEntity, 0, false).isCancelled()) {
            piglinEntity.take(itemEntity, itemEntity.getItem().getCount());
            itemstack = itemEntity.getItem();
            itemEntity.discard();
        } else if (!CraftEventFactory.callEntityPickupItemEvent(piglinEntity, itemEntity, itemEntity.getItem().getCount() - 1, false).isCancelled()) {
            piglinEntity.take(itemEntity, 1);
            itemstack = removeOneItemFromItemEntity(itemEntity);
        } else {
            return;
        }

        if (isLovedByPiglin(itemstack, piglinEntity)) {
            piglinEntity.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            holdInOffhand(piglinEntity, itemstack);
            admireGoldItem(piglinEntity);
        } else if (isFood(itemstack) && !hasEatenRecently(piglinEntity)) {
            eat(piglinEntity);
        } else {
            boolean flag = !piglinEntity.equipItemIfPossible(itemstack).equals(ItemStack.EMPTY);
            if (!flag) {
                putInInventory(piglinEntity, itemstack);
            }
        }
    }

    private static boolean isLovedByPiglin(ItemStack itemstack, Piglin piglin) {
        return isLovedItem(itemstack) || piglin.bridge$interestItems().contains(itemstack.getItem())
                ||  piglin.bridge$allowedBarterItems().contains(itemstack.getItem());
    }

    private static boolean isBarterItem(ItemStack itemstack, Piglin piglin) {
        return isBarterCurrency(itemstack) || piglin.bridge$allowedBarterItems().contains(itemstack.getItem());    }

    @Redirect(method = "stopHoldingOffHandItem", at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isBarterCurrency(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean banner$customBarter(ItemStack stack, Piglin piglin) {
        return isBarterItem(stack, piglin);
    }

    @Redirect(method = "stopHoldingOffHandItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;throwItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;Ljava/util/List;)V"))
    private static void banner$barterEvent(Piglin piglin, List<ItemStack> items) {
        ItemStack stack = piglin.getItemInHand(InteractionHand.OFF_HAND);
        PiglinBarterEvent event = CraftEventFactory.callPiglinBarterEvent(piglin, getBarterResponseItems(piglin), stack);
        if (!event.isCancelled()) {
            throwItems(piglin, event.getOutcome().stream().map(CraftItemStack::asNMSCopy).collect(Collectors.toList()));
        }
    }

    @Redirect(method = "stopHoldingOffHandItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isLovedItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean banner$customLove(ItemStack stack, Piglin piglin) {
        return isLovedByPiglin(stack, piglin);
    }

    @Redirect(method = "wantsToPickup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isBarterCurrency(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean banner$customBanter2(ItemStack stack, Piglin piglin) {
        return isBarterItem(stack, piglin);
    }

    @Redirect(method = "canAdmire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isBarterCurrency(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean banner$customBanter3(ItemStack stack, Piglin piglin) {
        return isBarterItem(stack, piglin);
    }

    @Redirect(method = "isNotHoldingLovedItemInOffHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isLovedItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean banner$customLove2(ItemStack stack, Piglin piglin) {
        return isLovedByPiglin(stack, piglin);
    }

    @Inject(method = "angerNearbyPiglins", at = @At("HEAD"), cancellable = true)
    private static void banner$configAnger(Player player, boolean angerOnlyIfCanSee, CallbackInfo ci) {
        if (!player.level().bridge$bannerConfig().piglinsGuardChests) ci.cancel(); // Paper
    }
}
