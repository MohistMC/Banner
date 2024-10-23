package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.CreakingHeart;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftCreakingHeart extends CraftBlockData implements CreakingHeart {

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> CREAKING = getEnum("creaking");

    @Override
    public org.bukkit.block.data.type.CreakingHeart.Creaking getCreaking() {
        return this.get(CraftCreakingHeart.CREAKING, org.bukkit.block.data.type.CreakingHeart.Creaking.class);
    }

    @Override
    public void setCreaking(org.bukkit.block.data.type.CreakingHeart.Creaking creaking) {
        this.set(CraftCreakingHeart.CREAKING, creaking);
    }
}
