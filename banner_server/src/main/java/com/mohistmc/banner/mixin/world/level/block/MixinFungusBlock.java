package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.TreeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FungusBlock.class)
public class MixinFungusBlock {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "performBonemeal", at = @At("HEAD"))
    private void banner$captureTree(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state, CallbackInfo ci) {
        if ((Object) this == Blocks.WARPED_FUNGUS) {
            BukkitSnapshotCaptures.captureTreeType(TreeType.WARPED_FUNGUS);
        } else if ((Object) this == Blocks.CRIMSON_FUNGUS) {
            BukkitSnapshotCaptures.captureTreeType(TreeType.CRIMSON_FUNGUS);
        }
    }
}
