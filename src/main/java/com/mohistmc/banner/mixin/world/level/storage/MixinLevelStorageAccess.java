package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionLevelStorageAccess;
import java.nio.file.Path;
import java.util.logging.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public abstract class MixinLevelStorageAccess implements InjectionLevelStorageAccess {

    @Shadow @Final public LevelStorageSource.LevelDirectory levelDirectory;
    public ResourceKey<LevelStem> dimensionType;

    public void banner$constructor(LevelStorageSource saveFormat, String saveName) {
        throw new RuntimeException();
    }

    public void banner$constructor(LevelStorageSource saveFormat, String saveName, ResourceKey<LevelStem> dimensionType) {
        banner$constructor(saveFormat, saveName);
        this.dimensionType = dimensionType;
    }


    @Inject(method = "getDimensionPath", cancellable = true, at = @At("HEAD"))
    private void banner$useActualType(ResourceKey<Level> dimensionKey, CallbackInfoReturnable<Path> cir) {
        if (dimensionType == LevelStem.OVERWORLD) {
            cir.setReturnValue(this.levelDirectory.path());
        } else if (dimensionType == LevelStem.NETHER) {
            cir.setReturnValue(this.levelDirectory.path().resolve("DIM-1"));
        } else if (dimensionType == LevelStem.END) {
            cir.setReturnValue(this.levelDirectory.path().resolve("DIM1"));
        }
    }

    @Override
    public ResourceKey<LevelStem> bridge$getTypeKey() {
        return this.dimensionType;
    }

    @Override
    public void bridge$setDimType(ResourceKey<LevelStem> typeKey) {
        this.dimensionType = typeKey;
    }
}
