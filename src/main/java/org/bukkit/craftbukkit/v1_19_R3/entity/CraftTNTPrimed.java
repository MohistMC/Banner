package org.bukkit.craftbukkit.v1_19_R3.entity;

import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;

public class CraftTNTPrimed extends CraftEntity implements TNTPrimed {

    public CraftTNTPrimed(CraftServer server, net.minecraft.world.entity.item.PrimedTnt entity) {
        super(server, entity);
    }

    @Override
    public float getYield() {
        //TODO return getHandle().yield;
        return 0;
    }

    @Override
    public boolean isIncendiary() {
        //TODO return getHandle().isIncendiary;
        return false;
    }

    @Override
    public void setIsIncendiary(boolean isIncendiary) {
        //TODO  getHandle().isIncendiary = isIncendiary;
    }

    @Override
    public void setYield(float yield) {
        //TODO getHandle().yield = yield;
    }

    @Override
    public int getFuseTicks() {
        return getHandle().getFuse();
    }

    @Override
    public void setFuseTicks(int fuseTicks) {
        getHandle().setFuse(fuseTicks);
    }

    @Override
    public net.minecraft.world.entity.item.PrimedTnt getHandle() {
        return (net.minecraft.world.entity.item.PrimedTnt) entity;
    }

    @Override
    public String toString() {
        return "CraftTNTPrimed";
    }

    @Override
    public EntityType getType() {
        return EntityType.PRIMED_TNT;
    }

    @Override
    public Entity getSource() {
        net.minecraft.world.entity.LivingEntity source = getHandle().getOwner();

        return (source != null) ? source.getBukkitEntity() : null;
    }

    @Override
    public void setSource(Entity source) {
        if (source instanceof LivingEntity) {
            getHandle().owner = ((CraftLivingEntity) source).getHandle();
        } else {
            getHandle().owner = null;
        }
    }
}
