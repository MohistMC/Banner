package com.mohistmc.banner.mixin.world.damagesource;

import com.mohistmc.banner.injection.world.damagesource.InjectionDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DamageSource.class)
public class MixinDamageSource implements InjectionDamageSource {

    // CraftBukkit start
    private boolean withSweep;
    private boolean melting;
    private boolean poison;
    @Nullable
    private org.bukkit.block.Block directBlock; // The block that caused the damage. damageSourcePosition is not used for all block damages
    private Entity customCausingEntity = null; // This field is a helper for when causing entity damage is not set by vanilla
    @Shadow
    @Final
    private Entity causingEntity;
    @Shadow
    @Final
    private Entity directEntity;
    @Shadow
    @Final
    private Holder<DamageType> type;
    @Shadow
    @Final
    private Vec3 damageSourcePosition;
    @Nullable
    private org.bukkit.block.BlockState directBlockState; // The block state of the block relevant to this damage source
    private Entity customEntityDamager = null; // This field is a helper for when direct entity damage is not set by vanilla
    private Entity customCausingEntityDamager = null; // This field is a helper for when causing entity damage is not set by vanilla

    @Override
    public boolean isSweep() {
        return withSweep;
    }

    @Override
    public DamageSource sweep() {
        this.withSweep = true;
        return ((DamageSource) (Object) this);
    }

    @Override
    public boolean isMelting() {
        return melting;
    }

    @Override
    public DamageSource melting() {
        this.melting = true;
        return ((DamageSource) (Object) this);
    }

    @Override
    public boolean isPoison() {
        return poison;
    }

    @Override
    public DamageSource poison() {
        this.poison = true;
        return ((DamageSource) (Object) this);
    }

    // CraftBukkit end

    @Override
    public Entity getCausingEntity() {
        return (this.customCausingEntity != null) ? this.customCausingEntity : this.causingEntity;
    }

    @Override
    public DamageSource customCausingEntity(Entity entity) {
        // This method is not intended for change the causing entity if is already set
        // also is only necessary if the entity passed is not the direct entity or different from the current causingEntity
        if (this.customCausingEntity != null || this.directEntity == entity || this.causingEntity == entity) {
            return ((DamageSource) (Object) this);
        }
        DamageSource damageSource = this.cloneInstance();
        this.customCausingEntity = entity;
        return damageSource;
    }

    @Override
    public org.bukkit.block.Block getDirectBlock() {
        return this.directBlock;
    }

    @Override
    public DamageSource directBlock(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos blockPosition) {
        if (blockPosition == null || world == null) {
            return ((DamageSource) (Object) this);
        }
        return directBlock(org.bukkit.craftbukkit.block.CraftBlock.at(world, blockPosition));
    }

    @Override
    public DamageSource directBlock(org.bukkit.block.Block block) {
        if (block == null) {
            return ((DamageSource) (Object) this);
        }
        // Cloning the instance lets us return unique instances of DamageSource without affecting constants defined in DamageSources
        DamageSource damageSource = this.cloneInstance();
        this.directBlock = block;
        return damageSource;
    }

    @Override
    public DamageSource cloneInstance() {
        DamageSource damageSource = new DamageSource(this.type, this.directEntity, this.causingEntity, this.damageSourcePosition);
        damageSource.banner$setDirectBlock(this.getDirectBlock());
        damageSource.banner$setDirectBlockState(this.getDirectBlockState());
        damageSource.banner$setCustomCausingEntity(this.customEntityDamager);
        this.withSweep = this.isSweep();
        this.poison = this.isPoison();
        this.melting = this.isMelting();
        return damageSource;
    }

    @Override
    public DamageSource directBlockState(org.bukkit.block.BlockState blockState) {
        if (blockState == null) {
            return ((DamageSource) (Object) this);
        }
        // Cloning the instance lets us return unique instances of DamageSource without affecting constants defined in DamageSources
        DamageSource damageSource = this.cloneInstance();
        this.directBlockState = blockState;
        return damageSource;
    }

    @Override
    public BlockState getDirectBlockState() {
        return this.directBlockState;
    }

    @Override
    public Entity getDamager() {
        return (this.customEntityDamager != null) ? this.customEntityDamager : this.directEntity;
    }

    @Override
    public Entity getCausingDamager() {
        return (this.customCausingEntityDamager != null) ? this.customCausingEntityDamager : this.causingEntity;
    }

    @Override
    public DamageSource customEntityDamager(Entity entity) {
        // This method is not intended for change the causing entity if is already set
        // also is only necessary if the entity passed is not the direct entity or different from the current causingEntity
        if (this.customEntityDamager != null || this.directEntity == entity || this.causingEntity == entity) {
            return ((DamageSource) (Object) this);
        }
        DamageSource damageSource = this.cloneInstance();
        this.customEntityDamager = entity;
        return damageSource;
    }

    @Override
    public DamageSource customCausingEntityDamager(Entity entity) {
        // This method is not intended for change the causing entity if is already set
        // also is only necessary if the entity passed is not the direct entity or different from the current causingEntity
        if (this.customCausingEntityDamager != null || this.directEntity == entity || this.causingEntity == entity) {
            return ((DamageSource) (Object) this);
        }
        DamageSource damageSource = this.cloneInstance();
        this.customCausingEntityDamager = entity;
        return damageSource;
    }

    @Override
    public DamageSource banner$setCustomCausingEntity(Entity entity) {
        this.customEntityDamager = entity;
        return (DamageSource) (Object) this;
    }

    @Override
    public DamageSource banner$setCustomCausingEntityDamager(Entity entity) {
        this.customCausingEntityDamager = entity;
        return (DamageSource) (Object) this;
    }

    @Override
    public DamageSource banner$setDirectBlock(Block block) {
        this.directBlock = block;
        return (DamageSource) (Object) this;
    }

    @Override
    public DamageSource banner$setDirectBlockState(BlockState block) {
        this.directBlockState = block;
        return (DamageSource) (Object) this;
    }
}
