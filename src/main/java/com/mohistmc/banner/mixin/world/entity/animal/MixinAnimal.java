package com.mohistmc.banner.mixin.world.entity.animal;

import com.mohistmc.banner.injection.world.entity.InjectionAnimal;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

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

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void spawnChildFromBreeding(ServerLevel world, Animal animalEntity) {
        AgeableMob child = this.getBreedOffspring(world, animalEntity);
        if (child != null) {
            ServerPlayer serverplayerentity = this.getLoveCause();
            if (serverplayerentity == null && animalEntity.getLoveCause() != null) {
                serverplayerentity = animalEntity.getLoveCause();
            }

            int experience = this.getRandom().nextInt(7) + 1;
            org.bukkit.event.entity.EntityBreedEvent entityBreedEvent = CraftEventFactory.callEntityBreedEvent(child, (Animal) (Object) this, animalEntity, serverplayerentity, this.breedItem, experience);
            if (entityBreedEvent.isCancelled()) {
                return;
            }
            experience = entityBreedEvent.getExperience();

            if (serverplayerentity != null) {
                serverplayerentity.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(serverplayerentity, (Animal) (Object) this, animalEntity, child);
            }

            this.setAge(6000);
            animalEntity.setAge(6000);
            this.resetLove();
            animalEntity.resetLove();
            child.setBaby(true);
            child.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
            world.pushAddEntityReason(CreatureSpawnEvent.SpawnReason.BREEDING);
            world.addFreshEntityWithPassengers(child);
            world.broadcastEntityEvent((Animal) (Object) this, (byte) 18);
            if (world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                if (experience > 0) {
                    world.addFreshEntity(new ExperienceOrb(world, this.getX(), this.getY(), this.getZ(), experience));
                }
            }

        }
    }
}
