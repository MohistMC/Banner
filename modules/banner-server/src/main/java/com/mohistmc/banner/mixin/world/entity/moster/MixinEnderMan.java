package com.mohistmc.banner.mixin.world.entity.moster;

import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class MixinEnderMan extends Monster {

    // @formatter:off
    @Shadow private int targetChangeTime;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_CREEPY;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_STARED_AT;
    @Shadow @Final private static AttributeModifier SPEED_MODIFIER_ATTACKING;

    @Shadow abstract boolean isLookingAtMe(Player player);

    protected MixinEnderMan(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    // @formatter:on

    public void bridge$updateTarget(LivingEntity livingEntity) {
        AttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (livingEntity == null) {
            this.targetChangeTime = 0;
            this.entityData.set(DATA_CREEPY, false);
            this.entityData.set(DATA_STARED_AT, false);
            modifiableattributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING.id());
        } else {
            this.targetChangeTime = this.tickCount;
            this.entityData.set(DATA_CREEPY, true);
            if (!modifiableattributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING.id())) {
                modifiableattributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
        }
    }

    @Override
    public boolean setTarget(LivingEntity livingEntity, EntityTargetEvent.TargetReason reason, boolean fireEvent) {
        if (!super.setTarget(livingEntity, reason, fireEvent)) {
            return false;
        }
        bridge$updateTarget(getTarget());
        return true;
    }

    @Inject(method = "isLookingAtMe", at = @At("HEAD"), cancellable = true)
    private void banner$lookingCheck(Player player, CallbackInfoReturnable<Boolean> cir) {
        boolean shouldAttack = isLookingAtMe_check(player);
        com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent event = new com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent((org.bukkit.entity.Enderman) getBukkitEntity(), (org.bukkit.entity.Player) player.getBukkitEntity());
        event.setCancelled(!shouldAttack);
        cir.setReturnValue(event.callEvent());
        cir.cancel();
    }

    private boolean isLookingAtMe_check(Player player) {
        ItemStack itemStack = (ItemStack) player.getInventory().armor.get(3);
        if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem())) {
            return false;
        } else {
            Vec3 vec3 = player.getViewVector(1.0F).normalize();
            Vec3 vec32 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
            double d = vec32.length();
            vec32 = vec32.normalize();
            double e = vec3.dot(vec32);
            return e > 1.0 - 0.025 / d ? player.hasLineOfSight(this) : false;
        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void setTarget(@Nullable LivingEntity entity) {
        this.bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
        super.setTarget(entity);
        if (getBanner$targetSuccess()) {
            bridge$updateTarget(getTarget());
        }
    }
}
