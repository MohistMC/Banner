package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.Brushable;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftBrushable extends CraftBlockData implements Brushable {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty DUSTED = getInteger("dusted");

    @Override
    public int getDusted() {
        return this.get(CraftBrushable.DUSTED);
    }

    @Override
    public void setDusted(int dusted) {
        this.set(CraftBrushable.DUSTED, dusted);
    }

    @Override
    public int getMaximumDusted() {
        return getMax(CraftBrushable.DUSTED);
    }
}
