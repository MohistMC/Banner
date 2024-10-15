package com.mohistmc.banner.mixin.interaction.cauldron;

import com.mohistmc.banner.bukkit.BukkitCauldronHooks;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CauldronInteraction.class)
public interface MixinCauldronInteraction {

    @Redirect(method = "<clinit>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/core/cauldron/CauldronInteraction;SHULKER_BOX:Lnet/minecraft/core/cauldron/CauldronInteraction;"))
    private static void banner$resetShulkerBox(CauldronInteraction value) {
        value = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            Block block = Block.byItem(itemStack.getItem());
            if (!(block instanceof ShulkerBoxBlock)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                if (!level.isClientSide) {
                    // CraftBukkit start
                    if (!BukkitCauldronHooks.lowerFillLevel(blockState, level, blockPos, player, CauldronLevelChangeEvent.ChangeReason.SHULKER_WASH)) {
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }
                    // CraftBukkit end
                    ItemStack itemStack2 = itemStack.transmuteCopy(Blocks.SHULKER_BOX, 1);
                    player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2, false));
                    player.awardStat(Stats.CLEAN_SHULKER_BOX);
                    // LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
                }

                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        };
    }

    @Redirect(method = "<clinit>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/core/cauldron/CauldronInteraction;BANNER:Lnet/minecraft/core/cauldron/CauldronInteraction;"))
    private static void banner$resetBanner(CauldronInteraction value) {
        value =  (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            BannerPatternLayers bannerPatternLayers = (BannerPatternLayers)itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
            if (bannerPatternLayers.layers().isEmpty()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                if (!level.isClientSide) {
                    // CraftBukkit start
                    if (!BukkitCauldronHooks.lowerFillLevel(blockState, level, blockPos, player, CauldronLevelChangeEvent.ChangeReason.SHULKER_WASH)) {
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }
                    // CraftBukkit end
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(1);
                    itemStack2.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers.removeLast());
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                    if (itemStack.isEmpty()) {
                        player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2, false));
                    } else if (player.getInventory().add(itemStack2)) {
                        player.inventoryMenu.sendAllDataToRemote();
                    } else {
                        player.drop(itemStack2, false);
                    }

                    player.awardStat(Stats.CLEAN_BANNER);
                    // LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
                }

                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        };
    }
}