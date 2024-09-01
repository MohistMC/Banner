package com.mohistmc.banner.mixin.world.level.block;

import java.util.Collections;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CaveVines.class)
public interface MixinCaveVines {

    @Shadow @Final BooleanProperty BERRIES = BlockStateProperties.BERRIES;

    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    static InteractionResult use(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
        if ((Boolean)blockState.getValue(BERRIES)) {
            // CraftBukkit start
            if (!CraftEventFactory.callEntityChangeBlockEvent(entity, blockPos, (BlockState) blockState.setValue(CaveVines.BERRIES, false))) {
                return InteractionResult.SUCCESS;
            }

            if (entity instanceof Player) {
                PlayerHarvestBlockEvent event = CraftEventFactory.callPlayerHarvestBlockEvent(level, blockPos, (Player) entity, net.minecraft.world.InteractionHand.MAIN_HAND, Collections.singletonList(new ItemStack(Items.GLOW_BERRIES, 1)));
                if (event.isCancelled()) {
                    return InteractionResult.SUCCESS; // We need to return a success either way, because making it PASS or FAIL will result in a bug where cancelling while harvesting w/ block in hand places block
                }
                for (org.bukkit.inventory.ItemStack itemStack : event.getItemsHarvested()) {
                    Block.popResource(level, blockPos, CraftItemStack.asNMSCopy(itemStack));
                }
            } else {
                Block.popResource(level, blockPos, new ItemStack(Items.GLOW_BERRIES, 1));
            }
            // CraftBukkit end
            float f = Mth.randomBetween(level.random, 0.8F, 1.2F);
            level.playSound((Player)null, blockPos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, f);
            BlockState blockState2 = (BlockState)blockState.setValue(BERRIES, false);
            level.setBlock(blockPos, blockState2, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }
}
