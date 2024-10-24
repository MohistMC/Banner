package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import com.mohistmc.banner.bukkit.BukkitFieldHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.world.StructureGrowEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public class MixinSaplingBlock {

    @Shadow @Final protected TreeGrower treeGrower;
    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static TreeType treeType; // CraftBukkit


    @Redirect(method = "advanceTree", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/grower/TreeGrower;growTree(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Z"))
    private boolean banner$cancelGrowTree(TreeGrower instance, ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
        return false;
    }

    @Inject(method = "advanceTree", at = @At(value = "INVOKE",
            target =  "Lnet/minecraft/world/level/block/grower/TreeGrower;growTree(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Z",
            shift = At.Shift.AFTER))
    private void banner$fireStructureGrowEvent(ServerLevel level, BlockPos pos, BlockState state,
                                               RandomSource random, CallbackInfo ci) {
        // CraftBukkit start
        if (level.bridge$captureTreeGeneration()) {
            this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
        } else {
            level.banner$setCaptureTreeGeneration(true);
            this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
            level.banner$setCaptureTreeGeneration(false);
            if (!level.bridge$capturedBlockStates().isEmpty()) {
                TreeType treeType = BukkitFieldHooks.treeType();
                BukkitFieldHooks.setTreeType(null);
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
    }
}
