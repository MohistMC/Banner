package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionLevelStorageAccess;
import com.mohistmc.banner.injection.world.level.storage.InjectionLevelStorageSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelStorageSource.class)
public abstract class MixinLevelStorageSource implements InjectionLevelStorageSource {

    @Shadow public abstract LevelStorageSource.LevelStorageAccess createAccess(String saveName) throws IOException;


    @Shadow protected abstract Path getLevelPath(String string);

    @Shadow @Final private DirectoryValidator worldDirValidator;

    @Override
    public LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String s, ResourceKey<LevelStem> dimensionType) throws IOException, ContentValidationException {
        Path path = this.getLevelPath(s);
        List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateDirectory(path, true);
        if (!list.isEmpty()) {
            throw new ContentValidationException(path, list);
        } else {
            LevelStorageSource.LevelStorageAccess save = createAccess(s);
            ((InjectionLevelStorageAccess) save).bridge$setDimType(dimensionType);
            return save;
        }
    }
}
