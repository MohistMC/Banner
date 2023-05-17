package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionPlayerDataStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;

@Mixin(PlayerDataStorage.class)
public class MixinPlayerDataStorage implements InjectionPlayerDataStorage {

    @Shadow @Final private File playerDir;

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void banner$allowDataSaving(Player player, CallbackInfo ci) {
        if (org.spigotmc.SpigotConfig.disablePlayerDataSaving) ci.cancel(); // Spigot
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtUtils;getDataVersion(Lnet/minecraft/nbt/CompoundTag;I)I"))
    private void banner$lastSeenTime(Player player, CallbackInfoReturnable<CompoundTag> cir) {
        if (player instanceof ServerPlayer) {
            CraftPlayer craftPlayer = ((ServerPlayer) player).getBukkitEntity();
            // Only update first played if it is older than the one we have
            long modified = new File(this.playerDir, player.getUUID() + ".dat").lastModified();
            if (modified < craftPlayer.getFirstPlayed()) {
                craftPlayer.setFirstPlayed(modified);
            }
        }
    }

    @Override
    public CompoundTag getPlayerData(String uuid) {
        try {
            final File file1 = new File(this.playerDir, uuid + ".dat");
            if (file1.exists()) {
                return NbtIo.readCompressed(new FileInputStream(file1));
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load player data for " + uuid);
        }
        return null;
    }

    @Override
    public File getPlayerDir() {
        return this.playerDir;
    }
}
