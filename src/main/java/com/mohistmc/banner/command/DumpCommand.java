package com.mohistmc.banner.command;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import com.mohistmc.banner.fabric.FabricInjectBukkit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DumpCommand extends BukkitCommand {
    private final List<String> tab_cmd = Arrays.asList("potions", "enchants", "cbcmds", "entitytypes", "biomes", "pattern", "worldtype", "bukkit_material", "vanilla_material");
    private final List<String> tab_mode = List.of("file");

    public DumpCommand(String name) {
        super(name);
        this.description = "Universal Dump, which will print the information you need locally!";
        this.usageMessage = "/dump <file> [potions|enchants|cbcmds|entitytypes|biomes|pattern|worldtype|bukkit_material|vanilla_material]";
        this.setPermission("banner.command.dump");
    }

    @Override
    @NotNull
    public List<String> tabComplete(CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {
        List<String> list = new ArrayList<>();
        if ((sender.isOp() || testPermission(sender))) {
            switch (args.length) {
                case 2 -> {
                    for (String param : tab_cmd) {
                        if (param.toLowerCase().startsWith(args[1].toLowerCase())) {
                            list.add(param);
                        }
                    }
                }
                case 1 -> {
                    for (String param : tab_mode) {
                        if (param.toLowerCase().startsWith(args[0].toLowerCase())) {
                            list.add(param);
                        }
                    }
                }
            }
        }

        return list;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        if (args.length == 2) {
            String mode = args[0];
            switch (args[1].toLowerCase(Locale.ENGLISH)) {
                case "potions" -> dumpPotions(sender, mode);
                case "enchants" -> dumpEnchant(sender, mode);
                case "cbcmds" -> dumpCBCommands(sender, mode);
                case "entitytypes" -> dumpEntityTypes(sender, mode);
                case "biomes" -> dumpBiomes(sender, mode);
                case "pattern" -> dumpPattern(sender, mode);
                case "worldtype" -> dumpWorldType(sender, mode);
                case "bukkit_material" -> dumpBukkitMaterial(sender, mode);
                case "vanilla_material" -> dumpVanillaMaterial(sender, mode);
                default -> {
                    return false;
                }
            }
        }
        return false;
    }

    private void dumpPotions(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (PotionEffectType pet : PotionEffectType.values()) {
            if (pet != null) {
                sb.append(pet).append("\n");
            }
        }
        for (PotionType pet : PotionType.values()) {
            if (pet != null) {
                sb.append(pet).append("\n");
            }
        }
        dump(sender, "potions", sb, mode);
    }

    private void dumpEnchant(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (Enchantment ench : Enchantment.values()) {
            sb.append(ench).append("\n");
        }
        dump(sender, "enchants", sb, mode);
    }

    private void dumpEntityTypes(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (EntityType ent : EntityType.values()) {
            sb.append(ent.toString()).append("\n");
        }
        dump(sender, "entitytypes", sb, mode);
    }

    private void dumpCBCommands(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (Command per : Objects.requireNonNull(BukkitExtraConstants.getServer()).bridge$server().getCommandMap().getCommands()) {
            // Do not print if there is no permission
            if (per.getPermission() == null) {
                continue;
            }
            sb.append(per.getName()).append(": ").append(per.getPermission()).append("\n");
        }
        dump(sender, "cbcommands", sb, mode);
    }

    private void dumpBiomes(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (Biome biome : Biome.values()) {
            sb.append(biome.toString()).append("\n");
        }
        dump(sender, "biomes", sb, mode);
    }

    private void dumpPattern(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (PatternType patternType : PatternType.values()) {
            String key = patternType.getIdentifier();
            sb.append(key).append("_").append(PatternType.getByIdentifier(key)).append("\n");
        }
        dump(sender, "pattern", sb, mode);
    }

    private void dumpWorldType(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (WorldType type : WorldType.values()) {
            String key = type.getName();
            sb.append(type).append("-").append(key).append("\n");
        }
        dump(sender, "worldtype", sb, mode);
    }

    private void dumpBukkitMaterial(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        for (Material material : Material.values()) {
            String key = material.name();
            sb.append(material).append("-").append(key).append("\n");
        }
        dump(sender, "bukkit_material", sb, mode);
    }

    private void dumpVanillaMaterial(CommandSender sender, String mode) {
        StringBuilder sb = new StringBuilder();
        var vanilla_item = BuiltInRegistries.ITEM;
        ResourceLocation resourceLocation;
        for (Item item : vanilla_item) {
            resourceLocation = vanilla_item.getKey(item);
            if (FabricInjectBukkit.isMINECRAFT(resourceLocation)) {
                sb.append(item).append("\n");
            }
        }
        var vanilla_block = BuiltInRegistries.BLOCK;
        for (Block block : vanilla_block) {
            resourceLocation = vanilla_block.getKey(block);
            if (FabricInjectBukkit.isMINECRAFT(resourceLocation)) {
                sb.append(block).append("\n");
            }
        }
        dump(sender, "vanilla_material", sb, mode);
    }

    private void dumpmsg(CommandSender sender, File file, String type) {
        sender.sendMessage("Successfully dump " + type + ", output path: " + file.getAbsolutePath());
    }

    private void dump(CommandSender sender, String type, StringBuilder sb, String mode) {
        if (mode.equals("file")) {
            saveToF(type + ".txt", sb, sender);
        }
    }

    private void saveToF(String child, StringBuilder sb, CommandSender sender) {
        File file = new File("dump", child);
        writeByteArrayToFile(file, sb);
        dumpmsg(sender, file, child.replace(".txt", ""));
    }

    protected void writeByteArrayToFile(File file, StringBuilder sb) {
        try {
            FileUtils.writeByteArrayToFile(file, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}