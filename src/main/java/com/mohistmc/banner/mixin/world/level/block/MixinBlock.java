package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.injection.world.level.block.InjectionBlock;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class MixinBlock extends BlockBehaviour implements InjectionBlock {

    public MixinBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private static void popResource(Level level, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack) {
        if (!level.isClientSide && !stack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            ItemEntity itemEntity = (ItemEntity)itemEntitySupplier.get();
            itemEntity.setDefaultPickUpDelay();
            // CraftBukkit start
            if (level.bridge$captureDrops() != null) {
                level.bridge$captureDrops().add(itemEntity);
            } else {
                level.addFreshEntity(itemEntity);
            }
            // CraftBukkit end
        }
    }

    @Override
    public int getExpDrop(BlockState blockState, ServerLevel world, BlockPos blockPos, ItemStack itemStack, boolean flag) {
        return 0;
    }

    @Override
    public int banner$tryDropExperience(ServerLevel level, BlockPos pos, ItemStack heldItem, IntProvider amount) {
        return tryDropExperience(level, pos, heldItem, amount);
    }

    protected int tryDropExperience(ServerLevel worldserver, BlockPos blockposition, ItemStack itemstack, IntProvider intprovider) {
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
            int i = intprovider.sample(worldserver.random);
            if (i > 0) {
                return i;
            }
        }
        return 0;
    }

    @Inject(method = "playerDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void banner$reason(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
       player.pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.BLOCK_MINED);
    }

}
