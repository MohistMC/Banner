package com.mohistmc.banner.mixin.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidStructure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DesertPyramidStructure.class)
public class MixinDesertPyramidStructure {

    @Inject(method = "placeSuspiciousSand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), cancellable = true)
    private static void banner$lootTable(BoundingBox boundingBox, WorldGenLevel worldGenLevel, BlockPos blockPos, CallbackInfo ci) {
        if (worldGenLevel instanceof org.bukkit.craftbukkit.util.TransformerGeneratorAccess transformerAccess) {
            org.bukkit.craftbukkit.block.CraftBrushableBlock brushableState = (org.bukkit.craftbukkit.block.CraftBrushableBlock) org.bukkit.craftbukkit.block.CraftBlockStates.getBlockState(worldGenLevel, blockPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), null);
            brushableState.setLootTable(org.bukkit.craftbukkit.CraftLootTable.minecraftToBukkit(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY));
            brushableState.setSeed(blockPos.asLong());
            transformerAccess.setCraftBlock(blockPos, brushableState, 2);
            ci.cancel();
        }
    }
}
