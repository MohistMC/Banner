/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftCreakingHeart extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.CreakingHeart, org.bukkit.block.data.Orientable {

    public CraftCreakingHeart() {
        super();
    }

    public CraftCreakingHeart(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftCreakingHeart

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> CREAKING = getEnum(net.minecraft.world.level.block.CreakingHeartBlock.class, "creaking");

    @Override
    public org.bukkit.block.data.type.CreakingHeart.Creaking getCreaking() {
        return this.get(CraftCreakingHeart.CREAKING, org.bukkit.block.data.type.CreakingHeart.Creaking.class);
    }

    @Override
    public void setCreaking(org.bukkit.block.data.type.CreakingHeart.Creaking creaking) {
        this.set(CraftCreakingHeart.CREAKING, creaking);
    }

    // org.bukkit.craftbukkit.block.data.CraftOrientable

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> AXIS = getEnum(net.minecraft.world.level.block.CreakingHeartBlock.class, "axis");

    @Override
    public org.bukkit.Axis getAxis() {
        return this.get(CraftCreakingHeart.AXIS, org.bukkit.Axis.class);
    }

    @Override
    public void setAxis(org.bukkit.Axis axis) {
        this.set(CraftCreakingHeart.AXIS, axis);
    }

    @Override
    public java.util.Set<org.bukkit.Axis> getAxes() {
        return this.getValues(CraftCreakingHeart.AXIS, org.bukkit.Axis.class);
    }
}
