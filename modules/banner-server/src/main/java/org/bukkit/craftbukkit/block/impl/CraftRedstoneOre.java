/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftRedstoneOre extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.Lightable {

    public CraftRedstoneOre() {
        super();
    }

    public CraftRedstoneOre(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.CraftLightable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT = getBoolean(net.minecraft.world.level.block.RedStoneOreBlock.class, "lit");

    @Override
    public boolean isLit() {
        return this.get(CraftRedstoneOre.LIT);
    }

    @Override
    public void setLit(boolean lit) {
        this.set(CraftRedstoneOre.LIT, lit);
    }
}
