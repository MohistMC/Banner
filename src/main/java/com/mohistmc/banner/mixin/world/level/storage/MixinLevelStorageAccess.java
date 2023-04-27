package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.bukkit.BukkitDataPackGenerator;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public abstract class MixinLevelStorageAccess {

    @Shadow public abstract Path getLevelPath(LevelResource folderName);

    @Inject(method = "<init>", at= @At("RETURN"))
    private void banner$addBukkitDataPack(LevelStorageSource levelStorageSource, String string, CallbackInfo ci) {
        BukkitDataPackGenerator.createBukkitDataPack(this.getLevelPath(LevelResource.DATAPACK_DIR).toFile());
    }
}
