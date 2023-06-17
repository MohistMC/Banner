package com.mohistmc.banner.mixin.world.entity.animal;

import com.mohistmc.banner.injection.world.entity.InjectionAnimal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Animal.class)
public abstract class MixinAnimal extends AgeableMob implements InjectionAnimal {

    protected MixinAnimal(EntityType<? extends AgeableMob> entityType, Level level) {
        super(entityType, level);
    }

    // @formatter:off
    @Shadow public InteractionResult mobInteract(Player playerIn, InteractionHand hand) { return null; }
    @Shadow public int inLove;
    @Shadow public abstract void resetLove();
    @Shadow @Nullable public abstract ServerPlayer getLoveCause();
    // @formatter:on

    @Shadow public abstract void spawnChildFromBreeding(ServerLevel level, Animal mate);

    @Shadow @org.jetbrains.annotations.Nullable public UUID loveCause;
    public ItemStack breedItem;

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Inject(method = "setInLove(Lnet/minecraft/world/entity/player/Player;)V", cancellable = true, at = @At("HEAD"))
    private void banner$enterLove(Player player, CallbackInfo ci) {
        EntityEnterLoveModeEvent event = CraftEventFactory.callEntityEnterLoveModeEvent(player, (Animal) (Object) this, 600);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            banner$loveTime = event.getTicksInLove();
        }
    }

    private transient int banner$loveTime;

    @Inject(method = "setInLove(Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "FIELD", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/animal/Animal;inLove:I"))
    private void banner$inLove(Player player, CallbackInfo ci) {
        this.inLove = banner$loveTime;
        if (player != null) {
            this.breedItem = player.getInventory().getSelected();
        }
    }

    @Override
    public ItemStack getBreedItem() {
        return breedItem;
    }

    @Redirect(method = "spawnChildFromBreeding", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$cancelAddEntity(ServerLevel instance, Entity entity) {
        return false;
    }

    @Redirect(method = "spawnChildFromBreeding", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$cancelAddEntity0(ServerLevel instance, Entity entity) { }

    private AtomicInteger banner$exp = new AtomicInteger(this.getRandom().nextInt(7) + 1);

    @Inject(method = "spawnChildFromBreeding", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$fixExp0(ServerLevel level, Animal mate,
                                CallbackInfo ci, AgeableMob ageableMob) {
        // CraftBukkit start - call EntityBreedEvent
        ServerPlayer breeder = Optional.ofNullable(this.getLoveCause()).or(() -> {
            return Optional.ofNullable(mate.getLoveCause());
        }).orElse(null);
        int experience = this.getRandom().nextInt(7) + 1;
        org.bukkit.event.entity.EntityBreedEvent entityBreedEvent = CraftEventFactory.callEntityBreedEvent(ageableMob, this, mate, breeder, this.breedItem, experience);
        if (entityBreedEvent.isCancelled()) {
            ci.cancel();
        }
        experience = entityBreedEvent.getExperience();
        banner$exp.set(experience);
    }

    @Inject(method = "spawnChildFromBreeding", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$fixExp1(ServerLevel level, Animal mate, CallbackInfo ci) {
        if (banner$exp.get() > 0) {
            level.addFreshEntity(new ExperienceOrb(level, this.getX(), this.getY(), this.getZ(), banner$exp.get()));
        }
    }
}
