/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftPistonMoving extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.TechnicalPiston, org.bukkit.block.data.Directional {

    public CraftPistonMoving() {
        super();
    }

    public CraftPistonMoving(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftTechnicalPiston

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> TYPE = getEnum(net.minecraft.world.level.block.piston.MovingPistonBlock.class, "type");

    @Override
    public org.bukkit.block.data.type.TechnicalPiston.Type getType() {
        return this.get(CraftPistonMoving.TYPE, org.bukkit.block.data.type.TechnicalPiston.Type.class);
    }

    @Override
    public void setType(org.bukkit.block.data.type.TechnicalPiston.Type type) {
        this.set(CraftPistonMoving.TYPE, type);
    }

    // org.bukkit.craftbukkit.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.piston.MovingPistonBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return this.get(CraftPistonMoving.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        this.set(CraftPistonMoving.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return this.getValues(CraftPistonMoving.FACING, org.bukkit.block.BlockFace.class);
    }
}
