package org.bukkit.craftbukkit.v1_19_R3.block;

import com.google.common.base.Preconditions;
import java.util.List;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.DecoratedPot;

public class CraftDecoratedPot extends CraftBlockEntityState<DecoratedPotBlockEntity> implements DecoratedPot {

    public CraftDecoratedPot(World world, DecoratedPotBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    @Override
    public List<Material> getShards() {
        // Banner - TODO
        //return getSnapshot().getShards().stream().map(CraftMagicNumbers::getMaterial).collect(Collectors.toUnmodifiableList());
        return null;
    }

    @Override
    public void addShard(Material material) {
        Preconditions.checkArgument(material != null && material.isItem(), "Material must be an item");

        // Banner - TODO
        //getSnapshot().getShards().add(CraftMagicNumbers.getItem(material));
    }

    @Override
    public void setShards(List<Material> shard) {
        // Banner - TODO
        //getSnapshot().getShards().clear();

        for (Material material : shard) {
            addShard(material);
        }
    }
}
