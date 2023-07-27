package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import org.bukkit.TreeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SaplingBlock.class)
public class MixinSaplingBlock {

    @Shadow @Final private AbstractTreeGrower treeGrower;
    private static TreeType treeType = BukkitExtraConstants.treeType; // CraftBukkit

    /*
    @Redirect(method = "advanceTree", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/grower/AbstractTreeGrower;growTree(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Z"))
    private boolean banner$cancelGrowTree(AbstractTreeGrower instance, ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state, RandomSource random) {
        return false;
    }

    @Inject(method = "advanceTree", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/grower/AbstractTreeGrower;growTree(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Z"))
    private void banner$fireStructureGrowEvent(ServerLevel level, BlockPos pos, BlockState state,
                                               RandomSource random, CallbackInfo ci) {
        // CraftBukkit start
        if (level.bridge$captureTreeGeneration()) {
            this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
        } else {
            level.banner$setCaptureTreeGeneration(true);
            this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
            level.banner$setCaptureTreeGeneration(false);
            if (level.bridge$capturedBlockStates().size() > 0) {
                TreeType treeType = BukkitExtraConstants.treeType;
                BukkitExtraConstants.treeType = null;
                Location location = CraftLocation.toBukkit(pos, level.getWorld());
                java.util.List<org.bukkit.block.BlockState> blocks = new java.util.ArrayList<>(level.bridge$capturedBlockStates().values());
                level.bridge$capturedBlockStates().clear();
                StructureGrowEvent event = null;
                if (treeType != null) {
                    event = new StructureGrowEvent(location, treeType, false, null, blocks);
                    org.bukkit.Bukkit.getPluginManager().callEvent(event);
                }
                if (event == null || !event.isCancelled()) {
                    for (org.bukkit.block.BlockState blockstate : blocks) {
                        blockstate.update(true);
                    }
                }
            }
        }
        // CraftBukkit end
    }*/
}
