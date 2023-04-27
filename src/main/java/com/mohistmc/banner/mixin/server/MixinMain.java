package com.mohistmc.banner.mixin.server;

import joptsimple.OptionParser;
import net.minecraft.server.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Arrays;

@Mixin(value = Main.class)
public abstract class MixinMain {


    @Inject(method = "main", at = @At(value = "INVOKE",
            target = "Ljoptsimple/OptionParser;nonOptions()Ljoptsimple/NonOptionArgumentSpec;",
            shift = At.Shift.AFTER), remap = false)
    private static void banner$initMain(String[] strings, CallbackInfo ci) {
        OptionParser banner$optionParser = new OptionParser();
        banner$optionParser.acceptsAll(Arrays.asList("b", "bukkit-settings"), "File for bukkit settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("bukkit.yml"))
                .describedAs("Yml file");

        banner$optionParser.acceptsAll(Arrays.asList("C", "commands-settings"), "File for command settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("commands.yml"))
                .describedAs("Yml file");

        banner$optionParser.acceptsAll(Arrays.asList("P", "plugins"), "Plugin directory to use")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("plugins"))
                .describedAs("Plugin directory");

        // Spigot Start
        banner$optionParser.acceptsAll(Arrays.asList("S", "spigot-settings"), "File for spigot settings")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("spigot.yml"))
                .describedAs("Yml file");
        // Spigot End
    }
}
