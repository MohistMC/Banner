package com.mohistmc.banner.mixin.server.players;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;

@Mixin(targets = "net.minecraft.server.players.OldUsersConverter$5")
public class MixinOldUsersConverter_5 {

    @Inject(method = "movePlayerFile", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/OldUsersConverter;ensureDirectoryExists(Ljava/io/File;)V"))
    private void banner$useOldName(File file, String oldFileName, String newFileName, CallbackInfo ci,
                                   @Local(ordinal = 1) File file2) {
        // CraftBukkit start - Use old file name to seed lastKnownName
        final File fileUnknown = new File(file.getParentFile(), "unknownplayers");
        CompoundTag root = null;
        try {
            root = NbtIo.readCompressed(new java.io.FileInputStream(file2), NbtAccounter.unlimitedHeap());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (root != null) {
            if (!root.contains("bukkit")) {
                root.put("bukkit", new CompoundTag());
            }
            CompoundTag data = root.getCompound("bukkit");
            data.putString("lastKnownName", oldFileName);

            try {
                NbtIo.writeCompressed(root, new java.io.FileOutputStream(fileUnknown));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            // CraftBukkit end
        }
    }
}
