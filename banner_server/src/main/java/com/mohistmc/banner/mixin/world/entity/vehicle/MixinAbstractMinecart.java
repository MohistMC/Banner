package com.mohistmc.banner.mixin.world.entity.vehicle;

import com.mohistmc.banner.injection.world.entity.vehicle.InjectionAbstractMinecart;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class MixinAbstractMinecart extends VehicleEntity implements InjectionAbstractMinecart {

    public MixinAbstractMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // @formatter:off
    @Shadow protected abstract void moveAlongTrack(BlockPos pos, BlockState state);
    @Shadow public abstract void activateMinecart(int x, int y, int z, boolean receivingPower);
    @Shadow private boolean flipped;
    @Shadow public abstract AbstractMinecart.Type getMinecartType();
    // @formatter:on

    @Shadow private boolean onRails;
    @Shadow private int lerpSteps;
    @Shadow private double lerpX;
    @Shadow private double lerpY;
    @Shadow private double lerpZ;
    @Shadow private double lerpYRot;
    @Shadow private double lerpXRot;
    public boolean slowWhenEmpty = true;
    private double derailedX = 0.5;
    private double derailedY = 0.5;
    private double derailedZ = 0.5;
    private double flyingX = 0.95;
    private double flyingY = 0.95;
    private double flyingZ = 0.95;
    public double maxSpeed = 0.4D;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void banner$init(EntityType<?> type, Level worldIn, CallbackInfo ci) {
        slowWhenEmpty = true;
        derailedX = 0.5;
        derailedY = 0.5;
        derailedZ = 0.5;
        flyingX = 0.95;
        flyingY = 0.95;
        flyingZ = 0.95;
        maxSpeed = 0.4D;
    }

    // Banner start - fix mixin by Spelunkery mod
    private double prevX;
    private double prevY;
    private double prevZ;
    private float prevYaw;
    private float prevPitch;
    // Banner end

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void tick() {
        // CraftBukkit start
        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        this.prevYaw = this.getYRot();
        this.prevPitch = this.getXRot();
        // CraftBukkit end

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        this.checkBelowWorld();
        // this.handleNetherPortal(); // CraftBukkit - handled in postTick
        if (this.level().isClientSide) {
            if (this.lerpSteps > 0) {
                this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
                --this.lerpSteps;
            } else {
                this.reapplyPosition();
                this.setRot(this.getYRot(), this.getXRot());
            }

        } else {
            if (!this.isNoGravity()) {
                double d = this.isInWater() ? -0.005D : -0.04D;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d, 0.0D));
            }

            int i = Mth.floor(this.getX());
            int j = Mth.floor(this.getY());
            int k = Mth.floor(this.getZ());
            if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
                --j;
            }

            BlockPos blockposition = new BlockPos(i, j, k);
            BlockState iblockdata = this.level().getBlockState(blockposition);
            this.onRails = BaseRailBlock.isRail(iblockdata);
            if (this.onRails) {
                this.moveAlongTrack(blockposition, iblockdata);
                if (iblockdata.is(Blocks.ACTIVATOR_RAIL)) {
                    this.activateMinecart(i, j, k, (Boolean) iblockdata.getValue(PoweredRailBlock.POWERED));
                }
            } else {
                this.comeOffTrack();
            }

            this.checkInsideBlocks();
            this.setXRot(0.0F);
            double d4 = this.xo - this.getX();
            double d5 = this.zo - this.getZ();

            if (d4 * d4 + d5 * d5 > 0.001D) {
                this.setYRot((float) (Mth.atan2(d5, d4) * 180.0D / 3.141592653589793D));
                if (this.flipped) {
                    this.setYRot(this.getYRot() + 180.0F);
                }
            }

            double d6 = (double) Mth.wrapDegrees(this.getYRot() - this.yRotO);

            if (d6 < -170.0D || d6 >= 170.0D) {
                this.setYRot(this.getYRot() + 180.0F);
                this.flipped = !this.flipped;
            }

            this.setRot(this.getYRot(), this.getXRot());
            // CraftBukkit start
            org.bukkit.World bworld = this.level().getWorld();
            Location from = new Location(bworld, prevX, prevY, prevZ, prevYaw, prevPitch);
            Location to = CraftLocation.toBukkit(this.position(), bworld, this.getYRot(), this.getXRot());
            Vehicle vehicle = (Vehicle) this.getBukkitEntity();

            this.level().getCraftServer().getPluginManager().callEvent(new org.bukkit.event.vehicle.VehicleUpdateEvent(vehicle));

            if (!from.equals(to)) {
                this.level().getCraftServer().getPluginManager().callEvent(new org.bukkit.event.vehicle.VehicleMoveEvent(vehicle, from, to));
            }
            // CraftBukkit end
            if (this.getMinecartType() == AbstractMinecart.Type.RIDEABLE && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
                List<Entity> list = this.level().getEntities((Entity) this, this.getBoundingBox().inflate(0.20000000298023224D, 0.0D, 0.20000000298023224D), EntitySelector.pushableBy(this));

                if (!list.isEmpty()) {
                    for (Entity value : list) {
                        Entity entity = (Entity) value;

                        if (!(entity instanceof Player) && !(entity instanceof IronGolem) && !(entity instanceof AbstractMinecart) && !this.isVehicle() && !entity.isPassenger()) {
                            // CraftBukkit start
                            VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity.getBukkitEntity());
                            this.level().getCraftServer().getPluginManager().callEvent(collisionEvent);

                            if (collisionEvent.isCancelled()) {
                                continue;
                            }
                            // CraftBukkit end
                            entity.startRiding(this);
                        } else {
                            // CraftBukkit start
                            if (!this.isPassengerOfSameVehicle(entity)) {
                                VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity.getBukkitEntity());
                                this.level().getCraftServer().getPluginManager().callEvent(collisionEvent);

                                if (collisionEvent.isCancelled()) {
                                    continue;
                                }
                            }
                            // CraftBukkit end
                            entity.push(this);
                        }
                    }
                }
            } else {

                for (Entity entity1 : this.level().getEntities(this, this.getBoundingBox().inflate(0.20000000298023224D, 0.0D, 0.20000000298023224D))) {
                    if (!this.hasPassenger(entity1) && entity1.isPushable() && entity1 instanceof AbstractMinecart) {
                        // CraftBukkit start
                        VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity1.getBukkitEntity());
                        this.level().getCraftServer().getPluginManager().callEvent(collisionEvent);

                        if (collisionEvent.isCancelled()) {
                            continue;
                        }
                        // CraftBukkit end
                        entity1.push(this);
                    }
                }
            }

            this.updateInWaterStateAndDoFluidPushing();
            if (this.isInLava()) {
                this.lavaHurt();
                this.fallDistance *= 0.5F;
            }

            this.firstTick = false;
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void comeOffTrack() {
        double d0 = this.getMaxSpeed();
        Vec3 vec3d = this.getDeltaMovement();

        this.setDeltaMovement(Mth.clamp(vec3d.x, -d0, d0), vec3d.y, Mth.clamp(vec3d.z, -d0, d0));
        if (this.onGround()) {
            // CraftBukkit start - replace magic numbers with our variables
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x * this.derailedX, this.getDeltaMovement().y * this.derailedY, this.getDeltaMovement().z * this.derailedZ));
            // CraftBukkit end
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround()) {
            // CraftBukkit start - replace magic numbers with our variables
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x * this.flyingX, this.getDeltaMovement().y * this.flyingY, this.getDeltaMovement().z * this.flyingZ));
            // CraftBukkit end
        }
    }

    @Redirect(method = "applyNaturalSlowdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;isVehicle()Z"))
    private boolean banner$slowWhenEmpty(AbstractMinecart abstractMinecartEntity) {
        return this.isVehicle() || !this.slowWhenEmpty;
    }

    @Inject(method = "push", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;hasPassenger(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$vehicleCollide(Entity entityIn, CallbackInfo ci) {
        if (!this.hasPassenger(entityIn)) {
            VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent((Vehicle) this.getBukkitEntity(), entityIn.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(collisionEvent);
            if (collisionEvent.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Override
    public Vector getFlyingVelocityMod() {
        return new Vector(flyingX, flyingY, flyingZ);
    }

    @Override
    public void setFlyingVelocityMod(Vector flying) {
        flyingX = flying.getX();
        flyingY = flying.getY();
        flyingZ = flying.getZ();
    }

    @Override
    public Vector getDerailedVelocityMod() {
        return new Vector(derailedX, derailedY, derailedZ);
    }

    @Override
    public void setDerailedVelocityMod(Vector derailed) {
        derailedX = derailed.getX();
        derailedY = derailed.getY();
        derailedZ = derailed.getZ();
    }

    @Override
    public double bridge$maxSpeed() {
        return maxSpeed;
    }

    @Override
    public void banner$setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public boolean bridge$slowWhenEmpty() {
        return slowWhenEmpty;
    }

    @Override
    public void banner$setSlowWhenEmpty(boolean slowWhenEmpty) {
        this.slowWhenEmpty = slowWhenEmpty;
    }
}
