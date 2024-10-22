package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.injection.world.level.block.InjectionChestBlock;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public abstract class MixinChestBlock implements InjectionChestBlock {

    @Shadow public abstract DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState state, Level level, BlockPos pos, boolean override);

    @Shadow @Final private static DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER;

    @Override
    public MenuProvider getMenuProvider(BlockState iblockdata, Level world, BlockPos blockposition, boolean ignoreObstructions) {
        return this.combine(iblockdata, world, blockposition, ignoreObstructions).apply(MENU_PROVIDER_COMBINER).orElse(null);
    }

    @Inject(method = "isCatSittingOnChest", at = @At("HEAD"), cancellable = true)
    private static void banner$configCatSitting(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Paper start - Option to disable chest cat detection
        if (level.getMinecraftWorld().bridge$bannerConfig().disableChestCatDetection) {
            cir.setReturnValue(false);
        }
        // Paper end
    }
}
