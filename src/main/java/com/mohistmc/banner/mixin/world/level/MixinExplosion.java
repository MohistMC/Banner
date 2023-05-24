package com.mohistmc.banner.mixin.world.level;

import com.google.common.collect.Sets;
import com.mohistmc.banner.injection.world.level.InjectionExplosion;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
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
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.block.BlockExplodeEvent;
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
    @Shadow public static float getSeenPercent(Vec3 vec3, Entity entity) { return 0f; }
    // @formatter:on

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)V",
            at = @At("RETURN"))
    public void banner$adjustSize(Level worldIn, Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn, Explosion.BlockInteraction modeIn, CallbackInfo ci) {
        this.radius = Math.max(sizeIn, 0F); // CraftBukkit - clamp bad values
    }

    public boolean wasCanceled = false; // CraftBukkit - add field

    /**
     * @author wdog5
     * @reason bukkit things
     */
    @Overwrite
    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();
        int i = 16;

        int k;
        int l;
        for(int j = 0; j < 16; ++j) {
            for(k = 0; k < 16; ++k) {
                for(l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d = (double)((float)j / 15.0F * 2.0F - 1.0F);
                        double e = (double)((float)k / 15.0F * 2.0F - 1.0F);
                        double f = (double)((float)l / 15.0F * 2.0F - 1.0F);
                        double g = Math.sqrt(d * d + e * e + f * f);
                        d /= g;
                        e /= g;
                        f /= g;
                        float h = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double m = this.x;
                        double n = this.y;
                        double o = this.z;

                        for(float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                            BlockPos blockPos = BlockPos.containing(m, n, o);
                            BlockState blockState = this.level.getBlockState(blockPos);
                            FluidState fluidState = this.level.getFluidState(blockPos);
                            if (!this.level.isInWorldBounds(blockPos)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(((Explosion) (Object) this), this.level, blockPos, blockState, fluidState);
                            if (optional.isPresent()) {
                                h -= ((Float)optional.get() + 0.3F) * 0.3F;
                            }

                            if (h > 0.0F && this.damageCalculator.shouldBlockExplode(((Explosion) (Object) this), this.level, blockPos, blockState, h)) {
                                set.add(blockPos);
                            }

                            m += d * 0.30000001192092896;
                            n += e * 0.30000001192092896;
                            o += f * 0.30000001192092896;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);
        float q = this.radius * 2.0F;
        k = Mth.floor(this.x - (double)q - 1.0);
        l = Mth.floor(this.x + (double)q + 1.0);
        int r = Mth.floor(this.y - (double)q - 1.0);
        int s = Mth.floor(this.y + (double)q + 1.0);
        int t = Mth.floor(this.z - (double)q - 1.0);
        int u = Mth.floor(this.z + (double)q + 1.0);
        List<Entity> list = this.level.getEntities(this.source, new AABB((double)k, (double)r, (double)t, (double)l, (double)s, (double)u));
        Vec3 vec3 = new Vec3(this.x, this.y, this.z);

        for(int v = 0; v < list.size(); ++v) {
            Entity entity = (Entity)list.get(v);
            if (!entity.ignoreExplosion()) {
                double w = Math.sqrt(entity.distanceToSqr(vec3)) / (double)q;
                if (w <= 1.0) {
                    double x = entity.getX() - this.x;
                    double y = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    double z = entity.getZ() - this.z;
                    double aa = Math.sqrt(x * x + y * y + z * z);
                    if (aa != 0.0) {
                        x /= aa;
                        y /= aa;
                        z /= aa;
                        double ab = (double)getSeenPercent(vec3, entity);
                        double ac = (1.0 - w) * ab;
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

                        CraftEventFactory.entityDamage = source;
                        entity.banner$setLastDamageCancelled(false);

                        if (entity instanceof EnderDragon) {
                            for (EnderDragonPart entityComplexPart : ((EnderDragon) entity).subEntities) {
                                // Calculate damage separately for each EntityComplexPart
                                double d7part;
                                if (list.contains(entityComplexPart) && (d7part = Math.sqrt(entityComplexPart.distanceToSqr(vec3)) / q) <= 1.0D) {
                                    double d13part = (1.0D - d7part) * getSeenPercent(vec3, entityComplexPart);
                                    entityComplexPart.hurt(this.getDamageSource(), (float) ((int) ((d13part * d13part + d13part) / 2.0D * 7.0D * (double) q + 1.0D)));
                                }
                            }
                        } else {
                            entity.hurt(this.getDamageSource(), (float) ((int) ((ac * ac + ac) / 2.0D * 7.0D * (double) q + 1.0D)));
                        }

                        CraftEventFactory.entityDamage = null;
                        if (entity.bridge$lastDamageCancelled()) { // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Skip entity if damage event was cancelled
                            continue;
                        }
                        // CraftBukkit end
                        double ad;
                        if (entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity)entity;
                            ad = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, ac);
                        } else {
                            ad = ac;
                        }

                        x *= ad;
                        y *= ad;
                        z *= ad;
                        Vec3 vec32 = new Vec3(x, y, z);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(vec32));
                        if (entity instanceof Player) {
                            Player player = (Player)entity;
                            if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                                this.hitPlayers.put(player, vec32);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * @author wdog5
     * @reason bukkit things
     */
    @Overwrite
    public void finalizeExplosion(boolean spawnParticles) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean bl = this.interactsWithBlocks();
        if (spawnParticles) {
            if (!(this.radius < 2.0F) && bl) {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            }
        }

        if (bl) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList();
            boolean bl2 = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(this.toBlow, this.level.random);
            ObjectListIterator var5 = this.toBlow.iterator();
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

            var5 = this.toBlow.iterator();

            while(var5.hasNext()) {
                BlockPos blockPos = (BlockPos)var5.next();
                BlockState blockState = this.level.getBlockState(blockPos);
                net.minecraft.world.level.block.Block block = blockState.getBlock();
                // CraftBukkit start - TNTPrimeEvent
                if (block instanceof net.minecraft.world.level.block.TntBlock) {
                    Entity sourceEntity = source == null ? null : source;
                    BlockPos sourceBlock = sourceEntity == null ? BlockPos.containing(this.x, this.y, this.z) : null;
                    if (!CraftEventFactory.callTNTPrimeEvent(this.level, blockPos, TNTPrimeEvent.PrimeCause.EXPLOSION, sourceEntity, sourceBlock)) {
                        this.level.sendBlockUpdated(blockPos, Blocks.AIR.defaultBlockState(), blockState, 3); // Update the block on the client
                        continue;
                    }
                }
                // CraftBukkit end

                if (!blockState.isAir()) {
                    BlockPos blockPos2 = blockPos.immutable();
                    this.level.getProfiler().push("explosion_blocks");
                    if (block.dropFromExplosion(((Explosion) (Object) this))) {
                        Level var11 = this.level;
                        if (var11 instanceof ServerLevel) {
                            ServerLevel serverLevel = (ServerLevel)var11;
                            BlockEntity blockEntity = blockState.hasBlockEntity() ? this.level.getBlockEntity(blockPos) : null;
                            LootContext.Builder builder = (new LootContext.Builder(serverLevel)).withRandom(this.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                            if (yield < 1.0F) { // CraftBukkit - add yield
                                builder.withParameter(LootContextParams.EXPLOSION_RADIUS, 1.0F / yield); // CraftBukkit - add yield
                            }

                            blockState.spawnAfterBreak(serverLevel, blockPos, ItemStack.EMPTY, bl2);
                            blockState.getDrops(builder).forEach((itemStack) -> {
                                addBlockDrops(objectArrayList, itemStack, blockPos2);
                            });
                        }
                    }

                    this.level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    block.wasExploded(this.level, blockPos, ((Explosion) (Object) this));
                    this.level.getProfiler().pop();
                }
            }

            var5 = objectArrayList.iterator();

            while(var5.hasNext()) {
                Pair<ItemStack, BlockPos> pair = (Pair)var5.next();
                net.minecraft.world.level.block.Block.popResource(this.level, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
            }
        }

        if (this.fire) {
            ObjectListIterator var13 = this.toBlow.iterator();

            while(var13.hasNext()) {
                BlockPos blockPos3 = (BlockPos)var13.next();
                if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockPos3).isAir() && this.level.getBlockState(blockPos3.below()).isSolidRender(this.level, blockPos3.below())) {
                    // CraftBukkit start - Ignition by explosion
                    if (!CraftEventFactory.callBlockIgniteEvent(this.level, blockPos3.getX(), blockPos3.getY(), blockPos3.getZ(), ((Explosion) (Object) this)).isCancelled()) {
                        this.level.setBlockAndUpdate(blockPos3, BaseFireBlock.getState(this.level, blockPos3));
                    }
                    // CraftBukkit end
                }
            }
        }

    }

    @Inject(method = "addBlockDrops", at = @At("HEAD"), cancellable = true)
    private static void banner$checkDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositionArray,
                                          ItemStack stack, BlockPos pos, CallbackInfo ci) {
        if (stack.isEmpty()) ci.cancel(); // CraftBukkit - SPIGOT-5425
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
