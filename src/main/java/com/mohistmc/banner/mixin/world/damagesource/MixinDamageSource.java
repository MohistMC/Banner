package com.mohistmc.banner.mixin.world.damagesource;

import com.mohistmc.banner.injection.world.damagesource.InjectionDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class MixinDamageSource implements InjectionDamageSource {

    @Shadow @Final @Nullable private Entity causingEntity;
    @Shadow @Final private Holder<DamageType> type;
    @Shadow @Final @Nullable private Entity directEntity;
    @Shadow @Final @Nullable private Vec3 damageSourcePosition;
    // CraftBukkit start
    @Unique
    private boolean melting;
    @Unique
    private boolean poison;
    @Unique
    private org.bukkit.block.Block directBlock;
    @Unique
    private boolean withSweep;
    @Unique
    private Entity customCausingEntity = null;

    @Override
    public boolean isSweep() {
        return withSweep;
    }

    @Override
    public DamageSource sweep() {
        withSweep = true;
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

    // Banner start
    @Override
    public boolean bridge$sweep() {
        return withSweep;
    }

    @Override
    public boolean bridge$melting() {
        return melting;
    }

    @Override
    public boolean bridge$poison() {
        return poison;
    }
    // Banner end

    @Override
    public Entity getCausingEntity() {
        return this.customCausingEntity == null ? this.causingEntity : this.customCausingEntity;
    }

    @Override
    public Entity bridge$getCausingEntity() {
        return this.getCausingEntity();
    }

    @Override
    public DamageSource bridge$customCausingEntity(Entity entity) {
        var src = cloneInstance();
        return src.bridge$setCustomCausingEntity(entity);
    }

    @Override
    public DamageSource bridge$setCustomCausingEntity(Entity entity) {
        this.customCausingEntity = entity;
        return (DamageSource) (Object) this;
    }

    public Block getDirectBlock() {
        return this.directBlock;
    }

    @Override
    public Block bridge$directBlock() {
        return this.getDirectBlock();
    }

    @Override
    public DamageSource bridge$directBlock(Block block) {
        return cloneInstance().bridge$setDirectBlock(block);
    }

    @Override
    public DamageSource bridge$setDirectBlock(Block block) {
        this.directBlock = block;
        return (DamageSource) (Object) this;
    }

    @Override
    public DamageSource cloneInstance() {
        var damageSource = new DamageSource(this.type, this.directEntity, this.causingEntity, this.damageSourcePosition);
        var br = damageSource;
        br.bridge$setDirectBlock(this.bridge$directBlock());
        br.bridge$setCustomCausingEntity(this.customCausingEntity);
        if (this.withSweep) {
            br.bridge$sweep();
        }
        if (this.poison) {
            br.bridge$poison();
        }
        if (this.melting) {
            br.bridge$melting();
        }
        return damageSource;
    }
}
