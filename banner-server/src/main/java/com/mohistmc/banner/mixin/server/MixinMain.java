package com.mohistmc.banner.mixin.server;

import com.google.common.base.Charsets;
import com.llamalad7.mixinextras.sugar.Local;
import com.mohistmc.banner.BannerMod;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.SharedConstants;
import net.minecraft.server.Main;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

@Mixin(Main.class)
public abstract class MixinMain {

    @Inject(method = "main", at = @At(value = "INVOKE",
            target = "Ljoptsimple/OptionParser;nonOptions()Ljoptsimple/NonOptionArgumentSpec;",
            shift = At.Shift.AFTER),
            remap = false
    )
    private static void banner$initMain(String[] strings, CallbackInfo ci, @Local OptionParser optionParser) {
        optionParser.acceptsAll(Arrays.asList("b", "bukkit-settings"), "File for bukkit settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("bukkit.yml"))
                .describedAs("Yml file");

        optionParser.acceptsAll(Arrays.asList("C", "commands-settings"), "File for command settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("commands.yml"))
                .describedAs("Yml file");

        optionParser.acceptsAll(Arrays.asList("P", "plugins"), "Plugin directory to use")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("plugins"))
                .describedAs("Plugin directory");

        // Spigot Start
        optionParser.acceptsAll(Arrays.asList("S", "spigot-settings"), "File for spigot settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("spigot.yml"))
                .describedAs("Yml file");
        // Spigot End

        // Spigot Start
        optionParser.acceptsAll(Arrays.asList("B", "banner-settings"), "File for banner settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("banner-config","banner.yml"))
                .describedAs("Yml file");
        // Spigot End
    }

    @Inject(method = "main", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"),
            remap = false
    )
    private static void banner$addYmlInfo(String[] strings, CallbackInfo ci, @Local OptionSet optionSet) throws IOException {
        // CraftBukkit start - SPIGOT-5761: Create bukkit.yml and commands.yml if not present
        File configFile = (File) optionSet.valueOf("bukkit-settings");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        configuration.options().copyDefaults(true);
        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(BannerMod.class.getClassLoader().getResourceAsStream("configurations/bukkit.yml"), Charsets.UTF_8)));
        configuration.save(configFile);

        File commandFile = (File) optionSet.valueOf("commands-settings");
        YamlConfiguration commandsConfiguration = YamlConfiguration.loadConfiguration(commandFile);
        commandsConfiguration.options().copyDefaults(true);
        commandsConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(BannerMod.class.getClassLoader().getResourceAsStream("configurations/commands.yml"), Charsets.UTF_8)));
        commandsConfiguration.save(commandFile);
        // CraftBukkit end
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/ServerPacksSource;createPackRepository(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)Lnet/minecraft/server/packs/repository/PackRepository;"))
    private static void banner$createBukkitDatapack(String[] strings, CallbackInfo ci, @Local LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        // CraftBukkit start
        File bukkitDataPackFolder = new File(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), "bukkit");
        if (!bukkitDataPackFolder.exists()) {
            bukkitDataPackFolder.mkdirs();
        }
        File mcMeta = new File(bukkitDataPackFolder, "pack.mcmeta");
        try {
            com.google.common.io.Files.write("{\n"
                    + "    \"pack\": {\n"
                    + "        \"description\": \"Data pack for resources provided by Bukkit plugins\",\n"
                    + "        \"pack_format\": " + SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA) + "\n"
                    + "    }\n"
                    + "}\n", mcMeta, com.google.common.base.Charsets.UTF_8);
        } catch (java.io.IOException ex) {
            throw new RuntimeException("Could not initialize Bukkit datapack", ex);
        }
    }

}
