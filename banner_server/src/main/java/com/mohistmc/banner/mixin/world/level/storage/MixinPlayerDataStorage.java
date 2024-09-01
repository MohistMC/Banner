package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionPlayerDataStorage;
import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerDataStorage.class)
public abstract class MixinPlayerDataStorage implements InjectionPlayerDataStorage {

    @Shadow @Final private File playerDir;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final protected DataFixer fixerUpper;
    @Shadow @Final private static DateTimeFormatter FORMATTER;

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void banner$allowDataSaving(Player player, CallbackInfo ci) {
        if (org.spigotmc.SpigotConfig.disablePlayerDataSaving) ci.cancel(); // Spigot
    }

    @Inject(method = "load(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;)Ljava/util/Optional;", at = @At("RETURN"))
    private void banner$lastSeenTime(Player player, String string, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        cir.getReturnValue().ifPresent((tag) -> {
            if (player instanceof ServerPlayer) {
                CraftPlayer craftPlayer = ((ServerPlayer) player).getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDir, player.getUUID() + ".dat").lastModified();
                if (modified < craftPlayer.getFirstPlayed()) {
                    craftPlayer.setFirstPlayed(modified);
                }
            }
        });
    }

    @Override
    public void backup(String name, String s1, String s) { // name, uuid, extension
        Path path = this.playerDir.toPath();
        // String s1 = entityhuman.getStringUUID(); // CraftBukkit - used above
        Path path1 = path.resolve(s1 +  s);

        // s1 = entityhuman.getStringUUID(); // CraftBukkit - used above
        Path path2 = path.resolve(s1 +  "_corrupted_" +  LocalDateTime.now().format(FORMATTER) + s);

        if (Files.isRegularFile(path1, new LinkOption[0])) {
            try {
                Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception exception) {
                LOGGER.warn("Failed to copy the player.dat file for {}", name, exception); // CraftBukkit
            }
        }
    }

    @Override
    public Optional<CompoundTag> load(String name, String uuid) {
        // CraftBukkit end
        Optional<CompoundTag> optional = this.load(name, uuid, ".dat"); // CraftBukkit

        if (optional.isEmpty()) {
            this.backup(name, uuid, ".dat"); // CraftBukkit
        }

        return optional.or(() -> {
            return this.load(name, uuid, ".dat_old"); // CraftBukkit
        }).map((nbttagcompound) -> {
            int i = NbtUtils.getDataVersion(nbttagcompound, -1);

            nbttagcompound = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, nbttagcompound, i);
            // entityhuman.load(nbttagcompound); // CraftBukkit - handled above
            return nbttagcompound;
        });
    }

    @Override
    public CompoundTag getPlayerData(String uuid) {
        try {
            final File file1 = new File(this.playerDir, uuid + ".dat");
            if (file1.exists()) {
                return NbtIo.readCompressed(new FileInputStream(file1), NbtAccounter.unlimitedHeap());
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load player data for " + uuid);
        }
        return null;
    }

    // CraftBukkit start
    @Override
    public Optional<CompoundTag> load(String name, String s1, String s) { // name, uuid, extension
        // CraftBukkit end
        File file = this.playerDir;
        // String s1 = entityhuman.getStringUUID(); // CraftBukkit - used above
        File file1 = new File(file, s1 +  s);
        // Spigot Start
        boolean usingWrongFile = false;
        if ( !file1.exists() )
        {
            file1 = new File( file, java.util.UUID.nameUUIDFromBytes( ( "OfflinePlayer:" +  name ).getBytes( java.nio.charset.StandardCharsets.UTF_8 ) ).toString() +  s );
            if ( file1.exists() )
            {
                usingWrongFile = true;
                org.bukkit.Bukkit.getServer().getLogger().warning( "Using offline mode UUID file for player " +  name +  " as it is the only copy we can find." );
            }
        }
        // Spigot End

        if (file1.exists() && file1.isFile()) {
            try {
                // Spigot Start
                Optional<CompoundTag> optional = Optional.of(NbtIo.readCompressed(file1.toPath(), NbtAccounter.unlimitedHeap()));
                if ( usingWrongFile )
                {
                    file1.renameTo( new File( file1.getPath() +  ".offline-read" ) );
                }
                return optional;
                // Spigot End
            } catch (Exception exception) {
                LOGGER.warn("Failed to load player data for {}", name); // CraftBukkit
            }
        }

        return Optional.empty();
    }

    // CraftBukkit start
    @Override
    public File getPlayerDir() {
        return this.playerDir;
    }
    // CraftBukkit end
}
