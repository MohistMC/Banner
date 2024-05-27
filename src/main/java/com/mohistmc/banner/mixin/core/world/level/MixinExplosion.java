package com.mohistmc.banner.mixin.core.world.level;

import com.google.common.collect.Sets;
import com.mohistmc.banner.injection.world.level.InjectionExplosion;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class MixinExplosion implements InjectionExplosion {

    // @formatter:off
    @Shadow @Final private Level level;
    @Shadow @Final private Explosion.BlockInteraction blockInteraction;
    @Shadow @Mutable @Final private float radius;
    @Shadow @Final private ObjectArrayList<BlockPos> toBlow;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final public Entity source;
    @Shadow @Final private Map<Player, Vec3> hitPlayers;
    @Shadow @Final private boolean fire;
    @Shadow @Final private RandomSource random;
    @Shadow @Final private ExplosionDamageCalculator damageCalculator;
    @Shadow public abstract boolean interactsWithBlocks();
    @Shadow @Nullable public abstract LivingEntity getIndirectSourceEntity();
    @Shadow public static float getSeenPercent(Vec3 p_46065_, Entity p_46066_) { return 0f; }
    // @formatter:on

    @Shadow @Final private DamageSource damageSource;
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)V",
            at = @At("RETURN"))
    public void banner$adjustSize(Level worldIn, Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn, Explosion.BlockInteraction modeIn, CallbackInfo ci) {
        this.radius = Math.max(sizeIn, 0F);
        this.yield = this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY ? 1.0F / this.radius : 1.0F;
    }

    public boolean wasCanceled = false; // CraftBukkit - add field
    public float yield;

    @Override
    public float bridge$getYield() {
        return yield;
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void explode() {
        // CraftBukkit start
        if (this.radius < 0.1F) {
            return;
        }
        // CraftBukkit end
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();
        boolean flag = true;

        int i;
        int j;

        for (int k = 0; k < 16; ++k) {
            for (i = 0; i < 16; ++i) {
                for (j = 0; j < 16; ++j) {
                    if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                        double d0 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) i / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d5 = this.y;
                        double d6 = this.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockposition = BlockPos.containing(d4, d5, d6);
                            BlockState iblockdata = this.level.getBlockState(blockposition);
                            FluidState fluid = this.level.getFluidState(blockposition);

                            if (!this.level.isInWorldBounds(blockposition)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(((Explosion) (Object) this), this.level, blockposition, iblockdata, fluid);

                            if (optional.isPresent()) {
                                f -= ((Float) optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && this.damageCalculator.shouldBlockExplode(((Explosion) (Object) this), this.level, blockposition, iblockdata, f)) {
                                set.add(blockposition);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d5 += d1 * 0.30000001192092896D;
                            d6 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);
        float f2 = this.radius * 2.0F;

        i = Mth.floor(this.x - (double) f2 - 1.0D);
        j = Mth.floor(this.x + (double) f2 + 1.0D);
        int l = Mth.floor(this.y - (double) f2 - 1.0D);
        int i1 = Mth.floor(this.y + (double) f2 + 1.0D);
        int j1 = Mth.floor(this.z - (double) f2 - 1.0D);
        int k1 = Mth.floor(this.z + (double) f2 + 1.0D);
        List<Entity> list = this.level.getEntities(this.source, new AABB((double) i, (double) l, (double) j1, (double) j, (double) i1, (double) k1));
        Vec3 vec3d = new Vec3(this.x, this.y, this.z);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (!entity.ignoreExplosion(((Explosion) (Object) this))) {
                double d7 = Math.sqrt(entity.distanceToSqr(vec3d)) / (double) f2;

                if (d7 <= 1.0D) {
                    double d8 = entity.getX() - this.x;
                    double d9 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    double d10 = entity.getZ() - this.z;
                    double d11 = Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10);

                    if (d11 != 0.0D) {
                        d8 /= d11;
                        d9 /= d11;
                        d10 /= d11;
                        double d12 = (double) getSeenPercent(vec3d, entity);
                        double d13 = (1.0D - d7) * d12;

                        // CraftBukkit start

                        // Special case ender dragon only give knockback if no damage is cancelled
                        // Thinks to note:
                        // - Setting a velocity to a ComplexEntityPart is ignored (and therefore not needed)
                        // - Damaging ComplexEntityPart while forward the damage to EntityEnderDragon
                        // - Damaging EntityEnderDragon does nothing
                        // - EntityEnderDragon hitbock always covers the other parts and is therefore always present
                        if (entity instanceof EnderDragonPart) {
                            continue;
                        }

                        entity.banner$setLastDamageCancelled(false);

                        if (entity instanceof EnderDragon) {
                            for (EnderDragonPart entityComplexPart : ((EnderDragon) entity).subEntities) {
                                // Calculate damage separately for each EntityComplexPart
                                double d7part;
                                if (list.contains(entityComplexPart) && (d7part = Math.sqrt(entityComplexPart.distanceToSqr(vec3d)) / f2) <= 1.0D) {
                                    double d13part = (1.0D - d7part) * getSeenPercent(vec3d, entityComplexPart);
                                    entityComplexPart.hurt(this.damageSource, (float) ((int) ((d13part * d13part + d13part) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                                }
                            }
                        } else {
                            entity.hurt(this.damageSource, (float) ((int) ((d13 * d13 + d13) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                        }

                        if (entity.bridge$lastDamageCancelled()) { // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Skip entity if damage event was cancelled
                            continue;
                        }
                        // CraftBukkit end
                        double d14;

                        if (entity instanceof LivingEntity) {
                            LivingEntity entityliving = (LivingEntity) entity;

                            d14 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(entityliving, d13);
                        } else {
                            d14 = d13;
                        }

                        d8 *= d14;
                        d9 *= d14;
                        d10 *= d14;
                        Vec3 vec3d1 = new Vec3(d8, d9, d10);

                        entity.setDeltaMovement(entity.getDeltaMovement().add(vec3d1));
                        if (entity instanceof Player) {
                            Player entityhuman = (Player) entity;

                            if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.getAbilities().flying)) {
                                this.hitPlayers.put(entityhuman, vec3d1);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void finalizeExplosion(boolean spawnParticles) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean flag = this.interactsWithBlocks();
        if (spawnParticles) {
            if (!(this.radius < 2.0F) && flag) {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            }
        }

        if (flag) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
            boolean flag2 = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(this.toBlow, this.level.random);

            // CraftBukkit start
            org.bukkit.World bworld = this.level.getWorld();
            org.bukkit.entity.Entity explode = this.source == null ? null : this.source.getBukkitEntity();
            Location location = new Location(bworld, this.x, this.y, this.z);

            List<org.bukkit.block.Block> blockList = new ObjectArrayList<>();
            for (int i1 = this.toBlow.size() - 1; i1 >= 0; i1--) {
                BlockPos cpos = this.toBlow.get(i1);
                org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
                if (!bblock.getType().isAir()) {
                    blockList.add(bblock);
                }
            }

            boolean cancelled;
            List<org.bukkit.block.Block> bukkitBlocks;
            float yield;

            if (explode != null) {
                EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY ? 1.0F / this.radius : 1.0F);
                this.level.getCraftServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            } else {
                BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY ? 1.0F / this.radius : 1.0F);
                this.level.getCraftServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            }

            this.toBlow.clear();
            for (org.bukkit.block.Block bblock : bukkitBlocks) {
                BlockPos coords = new BlockPos(bblock.getX(), bblock.getY(), bblock.getZ());
                toBlow.add(coords);
            }

            if (cancelled) {
                this.wasCanceled = true;
                return;
            }
            // CraftBukkit end

            for (BlockPos blockpos : this.toBlow) {
                BlockState blockstate = this.level.getBlockState(blockpos);
                Block block = blockstate.getBlock();

                // CraftBukkit start - TNTPrimeEvent
                if (block instanceof TntBlock) {
                    Entity sourceEntity = source == null ? null : source;
                    BlockPos sourceBlock = sourceEntity == null ? BlockPos.containing(this.x, this.y, this.z) : null;
                    if (!CraftEventFactory.callTNTPrimeEvent(this.level, blockpos, TNTPrimeEvent.PrimeCause.EXPLOSION, sourceEntity, sourceBlock)) {
                        this.level.sendBlockUpdated(blockpos, Blocks.AIR.defaultBlockState(), blockstate, 3); // Update the block on the client
                        continue;
                    }
                }
                // CraftBukkit end
                if (!blockstate.isAir()) {
                    BlockPos blockpos1 = blockpos.immutable();
                    this.level.getProfiler().push("explosion_blocks");
                    if (block.dropFromExplosion(((Explosion) (Object) this))&& this.level instanceof ServerLevel serverLevel) {
                        BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                        LootParams.Builder builder = (new LootParams.Builder(serverLevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                        if (yield < 1.0F) { // CraftBukkit - add yield
                            builder.withParameter(LootContextParams.EXPLOSION_RADIUS, 1.0F / yield); // CraftBukkit - add yield
                        }

                        blockstate.spawnAfterBreak(serverLevel, blockpos, ItemStack.EMPTY, flag2);
                        blockstate.getDrops(builder).forEach((itemStack) -> {
                            addBlockDrops(objectarraylist, itemStack, blockpos1);
                        });

                    }

                    block.wasExploded(this.level, blockpos, ((Explosion) (Object) this));
                    this.level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3); // Update the block on the client
                    this.level.getProfiler().pop();
                }
            }

            for (Pair<ItemStack, BlockPos> pair : objectarraylist) {
                Block.popResource(this.level, pair.getSecond(), pair.getFirst());
            }
        }

        if (this.fire) {
            for (BlockPos blockpos2 : this.toBlow) {
                if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockpos2).isAir() && this.level.getBlockState(blockpos2.below()).isSolidRender(this.level, blockpos2.below())) {
                    BlockIgniteEvent event = CraftEventFactory.callBlockIgniteEvent(this.level, blockpos2, (Explosion) (Object) this);
                    if (!event.isCancelled()) {
                        this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.level, blockpos2));
                    }
                }
            }
        }
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist, ItemStack itemstack, BlockPos blockposition) {
        if (itemstack.isEmpty()) return; // CraftBukkit - SPIGOT-5425
        int i = objectarraylist.size();

        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = (Pair) objectarraylist.get(j);
            ItemStack itemstack1 = (ItemStack) pair.getFirst();

            if (ItemEntity.areMergable(itemstack1, itemstack)) {
                ItemStack itemstack2 = ItemEntity.merge(itemstack1, itemstack, 16);

                objectarraylist.set(j, Pair.of(itemstack2, (BlockPos) pair.getSecond()));
                if (itemstack.isEmpty()) {
                    return;
                }
            }
        }

        objectarraylist.add(Pair.of(itemstack, blockposition));
    }

    @Override
    public boolean bridge$wasCanceled() {
        return wasCanceled;
    }

    @Override
    public void banner$setWasCanceled(boolean wasCanceled) {
        this.wasCanceled = wasCanceled;
    }
}
