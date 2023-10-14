package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionArrow;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Arrow.class)
public abstract class MixinArrow extends AbstractArrow implements InjectionArrow {

    @Shadow @Final private static EntityDataAccessor<Integer> ID_EFFECT_COLOR;

    @Shadow private Potion potion;

    @Shadow @Final public Set<MobEffectInstance> effects;

    protected MixinArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doPostHurtEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$pushArrowCause(LivingEntity target, CallbackInfo ci) {
        target.pushEffectCause(EntityPotionEffectEvent.Cause.ARROW);
    }

    @Override
    public void refreshEffects() {
        this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

    @Override
    public String getPotionType() {
        return BuiltInRegistries.POTION.getKey(this.potion).toString();
    }

    @Override
    public void setPotionType(String string) {
        this.potion = BuiltInRegistries.POTION.get(new ResourceLocation(string));
        this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

    @Override
    public boolean isTipped() {
        return !this.effects.isEmpty() || this.potion != Potions.EMPTY;
    }
}
