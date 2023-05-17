package com.mohistmc.banner.mixin.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mohistmc.banner.injection.world.level.InjectionExplosion;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    @Shadow public abstract DamageSource getDamageSource();
    @Shadow @Final private Map<Player, Vec3> hitPlayers;
    @Shadow @Final private boolean fire;
    @Shadow @Final private RandomSource random;
    @Shadow private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositionArray, ItemStack stack, BlockPos pos) { }
    @Shadow @Final private ExplosionDamageCalculator damageCalculator;
    @Shadow public abstract boolean interactsWithBlocks();
    @Shadow @Nullable public abstract LivingEntity getIndirectSourceEntity();
    @Shadow public static float getSeenPercent(Vec3 p_46065_, Entity p_46066_) { return 0f; }
    // @formatter:on

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)V",
            at = @At("RETURN"))
    public void banner$adjustSize(Level worldIn, Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn, Explosion.BlockInteraction modeIn, CallbackInfo ci) {
        this.radius = Math.max(sizeIn, 0F);
    }

    public boolean wasCanceled = false; // CraftBukkit - add field


    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void explode() {
        if (this.radius < 0.1F) {
            return;
        }
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = ((float) j / 15.0F * 2.0F - 1.0F);
                        double d1 = ((float) k / 15.0F * 2.0F - 1.0F);
                        double d2 = ((float) l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d6 = this.y;
                        double d8 = this.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                            BlockState blockstate = this.level.getBlockState(blockpos);
                            FluidState fluidstate = this.level.getFluidState(blockpos);

                            if (!this.level.isInWorldBounds(blockpos)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance((Explosion) (Object) this, this.level, blockpos, blockstate, fluidstate);
                            if (optional.isPresent()) {
                                f -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && this.damageCalculator.shouldBlockExplode((Explosion) (Object) this, this.level, blockpos, blockstate, f)) {
                                set.add(blockpos);
                            }

                            d4 += d0 * (double) 0.3F;
                            d6 += d1 * (double) 0.3F;
                            d8 += d2 * (double) 0.3F;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);
        float f3 = this.radius * 2.0F;
        int k1 = Mth.floor(this.x - (double) f3 - 1.0D);
        int l1 = Mth.floor(this.x + (double) f3 + 1.0D);
        int i2 = Mth.floor(this.y - (double) f3 - 1.0D);
        int i1 = Mth.floor(this.y + (double) f3 + 1.0D);
        int j2 = Mth.floor(this.z - (double) f3 - 1.0D);
        int j1 = Mth.floor(this.z + (double) f3 + 1.0D);
        List<Entity> list = this.level.getEntities(this.source, new AABB(k1, i2, j2, l1, i1, j1));
        Vec3 vec3d = new Vec3(this.x, this.y, this.z);

        for (Entity entity : list) {
            if (!entity.ignoreExplosion()) {
                double d12 = Math.sqrt(entity.distanceToSqr(vec3d)) / f3;
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.x;
                    double d7 = entity.getEyeY() - this.y;
                    double d9 = entity.getZ() - this.z;
                    double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = Explosion.getSeenPercent(vec3d, entity);
                        double d10 = (1.0D - d12) * d14;

                        if (entity instanceof EnderDragonPart) {
                            continue;
                        }

                        CraftEventFactory.entityDamage = this.source;
                        entity.banner$setLastDamageCancelled(false);

                        if (entity instanceof EnderDragon) {
                            for (EnderDragonPart entityComplexPart : ((EnderDragon) entity).subEntities) {
                                // Calculate damage separately for each EntityComplexPart
                                double d7part;
                                if (list.contains(entityComplexPart) && (d7part = Math.sqrt(entityComplexPart.distanceToSqr(vec3d)) / f3) <= 1.0D) {
                                    double d13part = (1.0D - d7part) * getSeenPercent(vec3d, entityComplexPart);
                                    entityComplexPart.hurt(this.getDamageSource(), (float) ((int) ((d13part * d13part + d13part) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                                }
                            }
                        } else {
                            entity.hurt(this.getDamageSource(), (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        }

                        CraftEventFactory.entityDamage = null;
                        if (entity.bridge$lastDamageCancelled()) {
                            continue;
                        }

                        double d11;
                        if (entity instanceof LivingEntity livingEntity) {
                            d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, d10);
                        } else {
                            d11 = d10;
                        }

                        d5 *= d11;
                        d7 *= d11;
                        d9 *= d11;
                        Vec3 vec3d1 = new Vec3(d5, d7, d9);

                        entity.setDeltaMovement(entity.getDeltaMovement().add(vec3d1));
                        if (entity instanceof Player playerentity) {
                            if (!playerentity.isSpectator() && (!playerentity.isCreative() || !playerentity.getAbilities().flying)) {
                                this.hitPlayers.put(playerentity, vec3d1);
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
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
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

            float yield = this.callBlockExplodeEvent();

            if (Float.isNaN(yield)) {
                this.wasCanceled = true;
                return;
            }

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
                    if (block.dropFromExplosion(((Explosion) (Object) this))) {
                        Level var11 = this.level;
                        if (var11 instanceof ServerLevel serverLevel) {
                            BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                            LootContext.Builder builder = (new LootContext.Builder(serverLevel)).withRandom(this.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                            if (yield < 1.0F) { // CraftBukkit - add yield
                                builder.withParameter(LootContextParams.EXPLOSION_RADIUS, 1.0F / yield); // CraftBukkit - add yield
                            }

                            blockstate.spawnAfterBreak(serverLevel, blockpos, ItemStack.EMPTY, flag2);
                            blockstate.getDrops(builder).forEach((itemStack) -> {
                                addBlockDrops(objectarraylist, itemStack, blockpos1);
                            });
                        }
                    }

                    this.level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
                    block.wasExploded(this.level, blockpos, ((Explosion) (Object) this));
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
                    BlockIgniteEvent event = CraftEventFactory.callBlockIgniteEvent(this.level, blockpos2.getX(), blockpos2.getY(), blockpos2.getZ(), (Explosion) (Object) this);
                    if (!event.isCancelled()) {
                        this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.level, blockpos2));
                    }
                }
            }
        }
    }

    private float callBlockExplodeEvent() {
        org.bukkit.World world =  this.level.getWorld();
        org.bukkit.entity.Entity exploder = this.source == null ? null :  this.source.getBukkitEntity();
        Location location = new Location(world, this.x, this.y, this.z);
        List<org.bukkit.block.Block> blockList = Lists.newArrayList();
        for (int i = this.toBlow.size() - 1; i >= 0; i--) {
            BlockPos blockPos = this.toBlow.get(i);
            org.bukkit.block.Block block = world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (!block.getType().isAir()) {
                blockList.add(block);
            }
        }

        boolean cancelled;
        List<org.bukkit.block.Block> bukkitBlocks;
        float bukkitYield;

        if (exploder != null) {
            EntityExplodeEvent event = new EntityExplodeEvent(exploder, location, blockList, this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY ? 1.0F / this.radius : 1.0F);
            Bukkit.getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
            bukkitBlocks = event.blockList();
            bukkitYield = event.getYield();
        } else {
            BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY ? 1.0F / this.radius : 1.0F);
            Bukkit.getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
            bukkitBlocks = event.blockList();
            bukkitYield = event.getYield();
        }

        this.toBlow.clear();

        for (org.bukkit.block.Block block : bukkitBlocks) {
            BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
            this.toBlow.add(blockPos);
        }
        return cancelled ? Float.NaN : bukkitYield;
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
