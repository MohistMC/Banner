package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import java.util.stream.Collectors;
import org.bukkit.TreeSpecies;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;

public class CraftBoat extends CraftVehicle implements Boat {

    public CraftBoat(CraftServer server, net.minecraft.world.entity.vehicle.Boat entity) {
        super(server, entity);
    }

    @Override
    public TreeSpecies getWoodType() {
        return CraftBoat.getTreeSpecies(this.getHandle().getVariant());
    }

    @Override
    public void setWoodType(TreeSpecies species) {
        this.getHandle().setVariant(CraftBoat.getBoatType(species));
    }

    @Override
    public Type getBoatType() {
        return CraftBoat.boatTypeFromNms(this.getHandle().getVariant());
    }

    @Override
    public void setBoatType(Type type) {
        Preconditions.checkArgument(type != null, "Boat.Type cannot be null");

        this.getHandle().setVariant(CraftBoat.boatTypeToNms(type));
    }

    @Override
    public double getMaxSpeed() {
        return this.getHandle().bridge$maxSpeed();
    }

    @Override
    public void setMaxSpeed(double speed) {
        if (speed >= 0D) {
            this.getHandle().banner$setMaxSpeed(speed);
        }
    }

    @Override
    public double getOccupiedDeceleration() {
        return this.getHandle().bridge$occupiedDeceleration();
    }

    @Override
    public void setOccupiedDeceleration(double speed) {
        if (speed >= 0D) {
            this.getHandle().banner$setOccupiedDeceleration(speed);
        }
    }

    @Override
    public double getUnoccupiedDeceleration() {
        return this.getHandle().bridge$unoccupiedDeceleration();
    }

    @Override
    public void setUnoccupiedDeceleration(double speed) {
        this.getHandle().banner$setUnoccupiedDeceleration(speed);
    }

    @Override
    public boolean getWorkOnLand() {
        return this.getHandle().bridge$landBoats();
    }

    @Override
    public void setWorkOnLand(boolean workOnLand) {
        this.getHandle().banner$setLandBoats(workOnLand);
    }

    @Override
    public Status getStatus() {
        return CraftBoat.boatStatusFromNms(this.getHandle().status);
    }

    @Override
    public net.minecraft.world.entity.vehicle.Boat getHandle() {
        return (net.minecraft.world.entity.vehicle.Boat) this.entity;
    }

    @Override
    public String toString() {
        return "CraftBoat{boatType=" + this.getBoatType() + ",status=" + this.getStatus() + ",passengers=" + this.getPassengers().stream().map(Entity::toString).collect(Collectors.joining("-", "{", "}")) + "}";
    }

    public static Boat.Type boatTypeFromNms(net.minecraft.world.entity.vehicle.Boat.Type boatType) {
        return switch (boatType) {
            default -> throw new EnumConstantNotPresentException(Type.class, boatType.name());
            case OAK -> Type.OAK;
            case BIRCH -> Type.BIRCH;
            case ACACIA -> Type.ACACIA;
            case CHERRY -> Type.CHERRY;
            case JUNGLE -> Type.JUNGLE;
            case SPRUCE -> Type.SPRUCE;
            case DARK_OAK -> Type.DARK_OAK;
            case MANGROVE -> Type.MANGROVE;
            case BAMBOO -> Type.BAMBOO;
        };
    }

    public static net.minecraft.world.entity.vehicle.Boat.Type boatTypeToNms(Boat.Type type) {
        return switch (type) {
            default -> throw new EnumConstantNotPresentException(net.minecraft.world.entity.vehicle.Boat.Type.class, type.name());
            case BAMBOO -> net.minecraft.world.entity.vehicle.Boat.Type.BAMBOO;
            case MANGROVE -> net.minecraft.world.entity.vehicle.Boat.Type.MANGROVE;
            case SPRUCE -> net.minecraft.world.entity.vehicle.Boat.Type.SPRUCE;
            case DARK_OAK -> net.minecraft.world.entity.vehicle.Boat.Type.DARK_OAK;
            case JUNGLE -> net.minecraft.world.entity.vehicle.Boat.Type.JUNGLE;
            case CHERRY -> net.minecraft.world.entity.vehicle.Boat.Type.CHERRY;
            case ACACIA -> net.minecraft.world.entity.vehicle.Boat.Type.ACACIA;
            case BIRCH -> net.minecraft.world.entity.vehicle.Boat.Type.BIRCH;
            case OAK -> net.minecraft.world.entity.vehicle.Boat.Type.OAK;
        };
    }

    public static Status boatStatusFromNms(net.minecraft.world.entity.vehicle.Boat.Status enumStatus) {
        return switch (enumStatus) {
            default -> throw new EnumConstantNotPresentException(Status.class, enumStatus.name());
            case IN_AIR -> Status.IN_AIR;
            case ON_LAND -> Status.ON_LAND;
            case UNDER_WATER -> Status.UNDER_WATER;
            case UNDER_FLOWING_WATER -> Status.UNDER_FLOWING_WATER;
            case IN_WATER -> Status.IN_WATER;
        };
    }

    @Deprecated
    public static TreeSpecies getTreeSpecies(net.minecraft.world.entity.vehicle.Boat.Type boatType) {
        switch (boatType) {
            case SPRUCE:
                return TreeSpecies.REDWOOD;
            case BIRCH:
                return TreeSpecies.BIRCH;
            case JUNGLE:
                return TreeSpecies.JUNGLE;
            case ACACIA:
                return TreeSpecies.ACACIA;
            case DARK_OAK:
                return TreeSpecies.DARK_OAK;
            case OAK:
            default:
                return TreeSpecies.GENERIC;
        }
    }

    @Deprecated
    public static net.minecraft.world.entity.vehicle.Boat.Type getBoatType(TreeSpecies species) {
        switch (species) {
            case REDWOOD:
                return net.minecraft.world.entity.vehicle.Boat.Type.SPRUCE;
            case BIRCH:
                return net.minecraft.world.entity.vehicle.Boat.Type.BIRCH;
            case JUNGLE:
                return net.minecraft.world.entity.vehicle.Boat.Type.JUNGLE;
            case ACACIA:
                return net.minecraft.world.entity.vehicle.Boat.Type.ACACIA;
            case DARK_OAK:
                return net.minecraft.world.entity.vehicle.Boat.Type.DARK_OAK;
            case GENERIC:
            default:
                return net.minecraft.world.entity.vehicle.Boat.Type.OAK;
        }
    }
}
