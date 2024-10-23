package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.mohistmc.banner.bukkit.BukkitMethodHooks;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.inventory.components.CraftFoodComponent;
import org.bukkit.craftbukkit.inventory.components.CraftJukeboxComponent;
import org.bukkit.craftbukkit.inventory.components.CraftToolComponent;
import org.bukkit.craftbukkit.util.CraftLegacy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CraftItemFactory implements ItemFactory {
    static final Color DEFAULT_LEATHER_COLOR = Color.fromRGB(0xA06540);
    private static final CraftItemFactory instance;
    private static final RandomSource randomSource = RandomSource.create();

    static {
        instance = new CraftItemFactory();
        ConfigurationSerialization.registerClass(SerializableMeta.class);
        ConfigurationSerialization.registerClass(CraftFoodComponent.class);
        ConfigurationSerialization.registerClass(CraftFoodComponent.CraftFoodEffect.class);
        ConfigurationSerialization.registerClass(CraftToolComponent.class);
        ConfigurationSerialization.registerClass(CraftToolComponent.CraftToolRule.class);
        ConfigurationSerialization.registerClass(CraftJukeboxComponent.class);
    }

    private CraftItemFactory() {
    }

    @Override
    public boolean isApplicable(ItemMeta meta, ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        }
        return this.isApplicable(meta, itemstack.getType());
    }

    @Override
    public boolean isApplicable(ItemMeta meta, Material type) {
        type = CraftLegacy.fromLegacy(type); // This may be called from legacy item stacks, try to get the right material
        if (type == null || meta == null) {
            return false;
        }

        Preconditions.checkArgument(meta instanceof CraftMetaItem, "Meta of %s not created by %s", meta.getClass().toString(), CraftItemFactory.class.getName());

        return ((CraftMetaItem) meta).applicableTo(type);
    }

    @Override
    public ItemMeta getItemMeta(Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        return this.getItemMeta(material, null);
    }

    private ItemMeta getItemMeta(Material material, CraftMetaItem meta) {
        material = CraftLegacy.fromLegacy(material); // This may be called from legacy item stacks, try to get the right material

        if (!material.isItem()) {
            // default behavior for none items is a new CraftMetaItem
            return new CraftMetaItem(meta);
        }

        return ((CraftItemType<?>) material.asItemType()).getItemMeta(meta);
    }

    @Override
    public boolean equals(ItemMeta meta1, ItemMeta meta2) {
        if (meta1 == meta2) {
            return true;
        }

        if (meta1 != null) {
            Preconditions.checkArgument(meta1 instanceof CraftMetaItem, "First meta of %s does not belong to %s", meta1.getClass().getName(), CraftItemFactory.class.getName());
        } else {
            return ((CraftMetaItem) meta2).isEmpty();
        }
        if (meta2 != null) {
            Preconditions.checkArgument(meta2 instanceof CraftMetaItem, "Second meta of %s does not belong to %s", meta2.getClass().getName(), CraftItemFactory.class.getName());
        } else {
            return ((CraftMetaItem) meta1).isEmpty();
        }

        return this.equals((CraftMetaItem) meta1, (CraftMetaItem) meta2);
    }

    boolean equals(CraftMetaItem meta1, CraftMetaItem meta2) {
        /*
         * This couldn't be done inside of the objects themselves, else force recursion.
         * This is a fairly clean way of implementing it, by dividing the methods into purposes and letting each method perform its own function.
         *
         * The common and uncommon were split, as both could have variables not applicable to the other, like a skull and book.
         * Each object needs its chance to say "hey wait a minute, we're not equal," but without the redundancy of using the 1.equals(2) && 2.equals(1) checking the 'commons' twice.
         *
         * Doing it this way fills all conditions of the .equals() method.
         */
        return meta1.equalsCommon(meta2) && meta1.notUncommon(meta2) && meta2.notUncommon(meta1);
    }

    public static CraftItemFactory instance() {
        return CraftItemFactory.instance;
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta meta, ItemStack stack) {
        Preconditions.checkArgument(stack != null, "ItemStack stack cannot be null");
        return this.asMetaFor(meta, stack.getType());
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta meta, Material material) {
        Preconditions.checkArgument(material != null, "Material cannot be null");
        Preconditions.checkArgument(meta instanceof CraftMetaItem, "ItemMeta of %s not created by %s", (meta != null ? meta.getClass().toString() : "null"), CraftItemFactory.class.getName());
        return this.getItemMeta(material, (CraftMetaItem) meta);
    }

    @Override
    public Color getDefaultLeatherColor() {
        return CraftItemFactory.DEFAULT_LEATHER_COLOR;
    }

    @Override
    public ItemStack createItemStack(String input) throws IllegalArgumentException {
        try {
            ItemParser.ItemResult arg = new ItemParser(BukkitMethodHooks.getDefaultRegistryAccess()).parse(new StringReader(input));

            Item item = arg.item().value();
            net.minecraft.world.item.ItemStack nmsItemStack = new net.minecraft.world.item.ItemStack(item);

            DataComponentPatch nbt = arg.components();
            if (nbt != null) {
                nmsItemStack.applyComponents(nbt);
            }

            return CraftItemStack.asCraftMirror(nmsItemStack);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not parse ItemStack: " + input, ex);
        }
    }

    @Override
    public Material getSpawnEgg(EntityType type) {
        if (type == EntityType.UNKNOWN) {
            return null;
        }
        net.minecraft.world.entity.EntityType<?> nmsType = CraftEntityType.bukkitToMinecraft(type);
        Item nmsItem = SpawnEggItem.byId(nmsType);

        if (nmsItem == null) {
            return null;
        }

        return CraftItemType.minecraftToBukkit(nmsItem);
    }

    @Override
    public ItemStack enchantItem(Entity entity, ItemStack itemStack, int level, boolean allowTreasures) {
        Preconditions.checkArgument(entity != null, "The entity must not be null");

        return CraftItemFactory.enchantItem(((CraftEntity) entity).getHandle().random, itemStack, level, allowTreasures);
    }

    @Override
    public ItemStack enchantItem(final World world, final ItemStack itemStack, final int level, final boolean allowTreasures) {
        Preconditions.checkArgument(world != null, "The world must not be null");

        return CraftItemFactory.enchantItem(((CraftWorld) world).getHandle().random, itemStack, level, allowTreasures);
    }

    @Override
    public ItemStack enchantItem(final ItemStack itemStack, final int level, final boolean allowTreasures) {
        return CraftItemFactory.enchantItem(CraftItemFactory.randomSource, itemStack, level, allowTreasures);
    }

    private static ItemStack enchantItem(RandomSource source, ItemStack itemStack, int level, boolean allowTreasures) {
        Preconditions.checkArgument(itemStack != null, "ItemStack must not be null");
        Preconditions.checkArgument(!itemStack.getType().isAir(), "ItemStack must not be air");
        itemStack = CraftItemStack.asCraftCopy(itemStack);
        CraftItemStack craft = (CraftItemStack) itemStack;
        RegistryAccess registry = CraftRegistry.getMinecraftRegistry();
        Optional<HolderSet.Named<Enchantment>> optional = (allowTreasures) ? Optional.empty() : registry.registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.IN_ENCHANTING_TABLE);
        return CraftItemStack.asCraftMirror(EnchantmentHelper.enchantItem(source, craft.handle, level, registry, optional));
    }
}
