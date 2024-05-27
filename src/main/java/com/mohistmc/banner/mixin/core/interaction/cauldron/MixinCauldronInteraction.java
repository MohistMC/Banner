package com.mohistmc.banner.mixin.core.interaction.cauldron;

import com.mohistmc.banner.bukkit.BukkitCauldronHooks;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Banner TODO fix patches
@Mixin(CauldronInteraction.class)
public interface MixinCauldronInteraction {

    /*
    @Redirect(method = "<clinit>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/core/cauldron/CauldronInteraction;SHULKER_BOX:Lnet/minecraft/core/cauldron/CauldronInteraction;"))
    private static void banner$resetShulkerBox(CauldronInteraction value) {
        value = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            Block block = Block.byItem(itemStack.getItem());
            if (!(block instanceof ShulkerBoxBlock)) {
                return InteractionResult.PASS;
            } else {
                if (!level.isClientSide) {
                    // CraftBukkit start
                    if (!BukkitCauldronHooks.changeLevel(level, blockPos , blockState, player, CauldronLevelChangeEvent.ChangeReason.SHULKER_WASH)) {
                        return InteractionResult.SUCCESS;
                    }
                    // CraftBukkit end
                    ItemStack itemStack2 = new ItemStack(Blocks.SHULKER_BOX);
                    if (itemStack.has()) {
                        itemStack2.setTag(itemStack.getTag().copy());
                    }

                    player.setItemInHand(interactionHand, itemStack2);
                    player.awardStat(Stats.CLEAN_SHULKER_BOX);
                    // LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        };
    }

    @Redirect(method = "<clinit>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/core/cauldron/CauldronInteraction;BANNER:Lnet/minecraft/core/cauldron/CauldronInteraction;"))
    private static void banner$resetBanner(CauldronInteraction value) {
        value =  (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            if (BannerBlockEntity.getPatternCount(itemStack) <= 0) {
                return InteractionResult.PASS;
            } else {
                if (!level.isClientSide) {
                    // CraftBukkit start
                    if (!BukkitCauldronHooks.changeLevel(level, blockPos, blockState, player, CauldronLevelChangeEvent.ChangeReason.BANNER_WASH)) {
                        return InteractionResult.SUCCESS;
                    }
                    // CraftBukkit end
                    ItemStack itemStack2 = itemStack.copy();
                    itemStack2.setCount(1);
                    BannerBlockEntity.removeLastPattern(itemStack2);
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                    if (itemStack.isEmpty()) {
                        player.setItemInHand(interactionHand, itemStack2);
                    } else if (player.getInventory().add(itemStack2)) {
                        player.inventoryMenu.sendAllDataToRemote();
                    } else {
                        player.drop(itemStack2, false);
                    }

                    player.awardStat(Stats.CLEAN_BANNER);
                    // LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        };
    }*/
}
