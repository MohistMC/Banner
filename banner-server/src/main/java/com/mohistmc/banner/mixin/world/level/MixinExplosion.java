package com.mohistmc.banner.mixin.world.level;

import com.mohistmc.banner.injection.world.level.InjectionExplosion;
import com.mojang.datafixers.util.Pair;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
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

    @Decorate(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean banner$handleMultiPart(Entity entity, DamageSource damageSource, float f, @Local(ordinal = -1) List<Entity> list) throws Throwable {
        // Special case ender dragon only give knockback if no damage is cancelled
        // Thinks to note:
        // - Setting a velocity to a ComplexEntityPart is ignored (and therefore not needed)
        // - Damaging ComplexEntityPart while forward the damage to EntityEnderDragon
        // - Damaging EntityEnderDragon does nothing
        // - EntityEnderDragon hitbock always covers the other parts and is therefore always present
        entity.banner$setLastDamageCancelled(false);

        if (entity.bridge$lastDamageCancelled()) {
            throw DecorationOps.jumpToLoopStart();
        }
        return (boolean) DecorationOps.callsite().invoke(entity, damageSource, f);
    }

    @Decorate(method = "explode", at = @At(value = "NEW", ordinal = 0, target = "(DDD)Lnet/minecraft/world/phys/Vec3;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getKnockbackMultiplier(Lnet/minecraft/world/entity/Entity;)F")))
    private Vec3 banner$knockBack(double d, double e, double f, @Local(ordinal = -1) Entity entity) throws Throwable {
        var vec3 = (Vec3) DecorationOps.callsite().invoke(d, e, f);
        double dx = entity.getX() - this.x;
        double dy = entity.getEyeY() - this.y;
        double dz = entity.getZ() - this.z;
        var force = dx * dx + dy * dy + dz * dz;
        if (entity instanceof LivingEntity) {
            var result = entity.getDeltaMovement().add(vec3);
            var event = CraftEventFactory.callEntityKnockbackEvent((CraftLivingEntity) entity.getBukkitEntity(), source, EntityKnockbackEvent.KnockbackCause.EXPLOSION, force, vec3, result.x, result.y, result.z);
            vec3 = (event.isCancelled()) ? Vec3.ZERO : new Vec3(event.getFinalKnockback().getX(), event.getFinalKnockback().getY(), event.getFinalKnockback().getZ()).subtract(entity.getDeltaMovement());
        }
        return vec3;
    }

    @Decorate(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V"))
    private void banner$tntPrime(BlockState instance, Level level, BlockPos pos, Explosion explosion, BiConsumer<?, ?> biConsumer) throws Throwable {
        if (instance.getBlock() instanceof TntBlock) {
            var sourceEntity = source == null ? null : source;
            var sourceBlock = sourceEntity == null ? BlockPos.containing(this.x, this.y, this.z) : null;
            if (!CraftEventFactory.callTNTPrimeEvent(this.level, pos, TNTPrimeEvent.PrimeCause.EXPLOSION, sourceEntity, sourceBlock)) {
                this.level.sendBlockUpdated(pos, Blocks.AIR.defaultBlockState(), instance, 3); // Update the block on the client
                return;
            }
        }
        DecorationOps.callsite().invoke(instance, level, pos, explosion, biConsumer);
    }

    @Redirect(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean banner$blockIgnite(Level instance, BlockPos blockPos, BlockState blockState) throws Throwable {
        BlockIgniteEvent event = CraftEventFactory.callBlockIgniteEvent(this.level, blockPos, (Explosion) (Object) this);
        if (event.isCancelled()) {
            return false;
        }
        return (boolean) DecorationOps.callsite().invoke(instance, blockPos, blockState);
    }

    @Inject(method = "addOrAppendStack", cancellable = true, at = @At("HEAD"))
    private static void banner$fix(List<Pair<ItemStack, BlockPos>> p_311090_, ItemStack stack, BlockPos p_309821_, CallbackInfo ci) {
        if (stack.isEmpty()) ci.cancel();
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
