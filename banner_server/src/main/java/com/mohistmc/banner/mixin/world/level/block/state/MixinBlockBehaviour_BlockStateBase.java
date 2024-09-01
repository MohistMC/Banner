package com.mohistmc.banner.mixin.world.level.block.state;

import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class MixinBlockBehaviour_BlockStateBase {

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void banner$captureBlockCollide(Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        BukkitSnapshotCaptures.captureDamageEventBlock(pos);
    }

    @Inject(method = "entityInside", at = @At("RETURN"))
    private void banner$resetBlockCollide(Level worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        BukkitSnapshotCaptures.captureDamageEventBlock(null);
    }
}
