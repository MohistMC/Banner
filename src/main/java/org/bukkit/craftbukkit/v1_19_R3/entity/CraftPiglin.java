package org.bukkit.craftbukkit.v1_19_R3.entity;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.item.Item;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.inventory.Inventory;

public class CraftPiglin extends CraftPiglinAbstract implements Piglin {

    public CraftPiglin(CraftServer server, net.minecraft.world.entity.monster.piglin.Piglin entity) {
        super(server, entity);
    }

    @Override
    public boolean isAbleToHunt() {
        return getHandle().cannotHunt;
    }

    @Override
    public void setIsAbleToHunt(boolean flag) {
        getHandle().cannotHunt = flag;
    }

    @Override
    public boolean addBarterMaterial(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftMagicNumbers.getItem(material);
        //TODO return getHandle().allowedBarterItems.add(item);
        return false;
    }

    @Override
    public boolean removeBarterMaterial(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftMagicNumbers.getItem(material);
        //TODO return getHandle().allowedBarterItems.remove(item);
        return false;
    }

    @Override
    public boolean addMaterialOfInterest(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftMagicNumbers.getItem(material);
        //TODO return getHandle().interestItems.add(item);
        return false;
    }

    @Override
    public boolean removeMaterialOfInterest(Material material) {
        Preconditions.checkArgument(material != null, "material cannot be null");

        Item item = CraftMagicNumbers.getItem(material);
        //TODO return getHandle().interestItems.remove(item);
        return false;
    }

    @Override
    public Set<Material> getInterestList() {
        //TODO return Collections.unmodifiableSet(getHandle().interestItems.stream().map(CraftMagicNumbers::getMaterial).collect(Collectors.toSet()));
        return null;
    }

    @Override
    public Set<Material> getBarterList() {
        //TODO return Collections.unmodifiableSet(getHandle().allowedBarterItems.stream().map(CraftMagicNumbers::getMaterial).collect(Collectors.toSet()));
        return null;
    }

    @Override
    public Inventory getInventory() {
        return new CraftInventory(getHandle().inventory);
    }

    @Override
    public net.minecraft.world.entity.monster.piglin.Piglin getHandle() {
        return (net.minecraft.world.entity.monster.piglin.Piglin) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.PIGLIN;
    }

    @Override
    public String toString() {
        return "CraftPiglin";
    }
}
