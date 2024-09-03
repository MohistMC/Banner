package com.mohistmc.banner.mixin.world.entity.animal;

import com.mohistmc.banner.injection.world.entity.InjectionAnimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Shadow public abstract void finalizeSpawnChildFromBreeding(ServerLevel serverLevel, Animal animal, @org.jetbrains.annotations.Nullable AgeableMob ageableMob);

    public ItemStack breedItem;

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

    @Inject(method = "spawnChildFromBreeding", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void banner$reason(ServerLevel level, Animal p_27565_, CallbackInfo ci) {
         level.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.BREEDING);
    }

    private AtomicInteger banner$exp = new AtomicInteger(this.getRandom().nextInt(7) + 1);

    @Redirect(method = "spawnChildFromBreeding", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Animal;finalizeSpawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;Lnet/minecraft/world/entity/AgeableMob;)V"))
    private void banner$resetSpawnChild(Animal instance, ServerLevel serverLevel, Animal animal, AgeableMob ageableMob) {
        // CraftBukkit start - call EntityBreedEvent
        ServerPlayer breeder = Optional.ofNullable(this.getLoveCause()).or(() -> {
            return Optional.ofNullable(animal.getLoveCause());
        }).orElse(null);
        int experience = this.getRandom().nextInt(7) + 1;
        EntityBreedEvent entityBreedEvent = CraftEventFactory.callEntityBreedEvent(ageableMob, this, animal, breeder, this.breedItem, experience);
        if (entityBreedEvent.isCancelled()) {
            return;
        }
        experience = entityBreedEvent.getExperience();
        this.finalizeSpawnChildFromBreeding(serverLevel, animal, ageableMob, experience);
        serverLevel.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.BREEDING);
        // CraftBukkit end
    }

    public void finalizeSpawnChildFromBreeding(ServerLevel worldserver, Animal entityanimal, @Nullable AgeableMob entityageable, int experience) {
        banner$exp.set(experience);
        this.finalizeSpawnChildFromBreeding(worldserver, entityanimal, entityageable);
    }

    @Redirect(method = "finalizeSpawnChildFromBreeding", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean banner$finalizeSpawn(ServerLevel instance, Entity entity) {
        if (banner$exp.get() > 0) {
            return instance.addFreshEntity(new ExperienceOrb(instance, this.getX(), this.getY(), this.getZ(), banner$exp.get()));
        }
        return false;
    }
}
