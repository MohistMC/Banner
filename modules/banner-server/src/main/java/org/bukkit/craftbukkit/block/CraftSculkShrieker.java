package org.bukkit.craftbukkit.block;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.SculkShrieker;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CraftSculkShrieker extends CraftBlockEntityState<SculkShriekerBlockEntity> implements SculkShrieker {

    public CraftSculkShrieker(World world, SculkShriekerBlockEntity tileEntity) {
        super(world, tileEntity);
    }

    protected CraftSculkShrieker(CraftSculkShrieker state, Location location) {
        super(state, location);
    }

    @Override
    public int getWarningLevel() {
        return this.getSnapshot().warningLevel;
    }

    @Override
    public void setWarningLevel(int level) {
        this.getSnapshot().warningLevel = level;
    }

    @Override
    public void tryShriek(Player player) {
        this.requirePlaced();

        ServerPlayer entityPlayer = (player == null) ? null : ((CraftPlayer) player).getHandle();
        this.getTileEntity().tryShriek(this.world.getHandle(), entityPlayer);
    }

    @Override
    public CraftSculkShrieker copy() {
        return new CraftSculkShrieker(this, null);
    }

    @Override
    public CraftSculkShrieker copy(Location location) {
        return new CraftSculkShrieker(this, location);
    }
}
