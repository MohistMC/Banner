/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftSculkCatalyst extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.SculkCatalyst {

    public CraftSculkCatalyst() {
        super();
    }

    public CraftSculkCatalyst(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftSculkCatalyst

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty BLOOM = getBoolean(net.minecraft.world.level.block.SculkCatalystBlock.class, "bloom");

    @Override
    public boolean isBloom() {
        return this.get(CraftSculkCatalyst.BLOOM);
    }

    @Override
    public void setBloom(boolean bloom) {
        this.set(CraftSculkCatalyst.BLOOM, bloom);
    }
}
