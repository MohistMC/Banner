package com.mohistmc.banner.mixin.core.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(FrostWalkerEnchantment.class)
public class MixinFrostWalkerEnchantment {

    @Redirect(method = "onEntityMoved",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean banner$cancelUpdate(Level instance, BlockPos pos, BlockState state) {
        return false;
    }

    @Redirect(method = "onEntityMoved", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
    private static void banner$canceltick(Level instance, BlockPos pos, Block block, int i) {}

    @Inject(method = "onEntityMoved", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$tick(LivingEntity living, Level level, BlockPos pos,
                                    int levelConflicting, CallbackInfo ci, BlockState blockState,
                                    int i, BlockPos.MutableBlockPos mutableBlockPos, Iterator<BlockPos> var7,
                                    BlockPos blockPos, BlockState blockState2, BlockState blockState3) {
        // CraftBukkit Start - Call EntityBlockFormEvent for Frost Walker
        if (CraftEventFactory.handleBlockFormEvent(level, blockPos, blockState, living)) {
            level.scheduleTick(blockPos, Blocks.FROSTED_ICE, Mth.nextInt(living.getRandom(), 60, 120));
        }
        // CraftBukkit End
    }
}
