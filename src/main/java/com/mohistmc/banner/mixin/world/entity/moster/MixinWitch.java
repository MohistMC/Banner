package com.mohistmc.banner.mixin.world.entity.moster;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Witch.class)
public abstract class MixinWitch extends Raider {

    protected MixinWitch(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void banner$reason(CallbackInfo ci) {
        pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/alchemy/PotionUtils;getMobEffects(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private List<MobEffectInstance> banner$callWitchConsumerEvent(ItemStack stack) {
        // Paper start
        com.destroystokyo.paper.event.entity.WitchConsumePotionEvent event = new com.destroystokyo.paper.event.entity.WitchConsumePotionEvent((org.bukkit.entity.Witch) this.getBukkitEntity(), CraftItemStack.asCraftMirror(stack));
        return event.callEvent() ? PotionUtils.getMobEffects(CraftItemStack.asNMSCopy(event.getPotion())) : null;
        // Paper end
    }

    private AtomicReference<ItemStack> paperPotion = new AtomicReference<>();

    @Inject(method = "performRangedAttack",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$callWitchPotionEvent(LivingEntity target, float velocity, CallbackInfo ci, Vec3 vec3,
                                             double d, double e, double f, double g, Potion potion) {
        // Paper start
        paperPotion.set(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        com.destroystokyo.paper.event.entity.WitchThrowPotionEvent event = new com.destroystokyo.paper.event.entity.WitchThrowPotionEvent((org.bukkit.entity.Witch) this.getBukkitEntity(), (org.bukkit.entity.LivingEntity) target.getBukkitEntity(), CraftItemStack.asCraftMirror(paperPotion.get()));
        if (!event.callEvent()) {
            ci.cancel();
        }
        paperPotion.set(CraftItemStack.asNMSCopy(event.getPotion()));
        // Paper end
    }

    @Redirect(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionUtils;setPotion(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/alchemy/Potion;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack banner$resetPotionStack(ItemStack stack, Potion potion) {
        return paperPotion.get();
    }

}
