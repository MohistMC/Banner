package com.mohistmc.banner.mixin.world.entity.moster;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(Witch.class)
public abstract class MixinWitch extends Raider {

    protected MixinWitch(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;)V"))
    private void banner$reason(CallbackInfo ci) {
        pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$callWitchConsumerEvent(CallbackInfo ci, ItemStack itemStack, PotionContents potionContents) {
        // Paper start
        if (itemStack.is(Items.POTION)) {
            com.destroystokyo.paper.event.entity.WitchConsumePotionEvent event = new com.destroystokyo.paper.event.entity.WitchConsumePotionEvent((org.bukkit.entity.Witch) this.getBukkitEntity(), org.bukkit.craftbukkit.inventory.CraftItemStack.asCraftMirror(itemStack));
            potionContents = event.callEvent() ? org.bukkit.craftbukkit.inventory.CraftItemStack.unwrap(event.getPotion()).get(DataComponents.POTION_CONTENTS) : null;
        }
        // Paper end
    }

    private AtomicReference<ItemStack> paperPotion = new AtomicReference<>();

    @Inject(method = "performRangedAttack",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$callWitchPotionEvent(LivingEntity livingEntity, float f, CallbackInfo ci, Vec3 vec3, double d, double e, double g, double h, Holder holder) {
        // Paper start
        paperPotion.set(PotionContents.createItemStack(Items.SPLASH_POTION, holder));
        com.destroystokyo.paper.event.entity.WitchThrowPotionEvent event = new com.destroystokyo.paper.event.entity.WitchThrowPotionEvent((org.bukkit.entity.Witch) this.getBukkitEntity(), (org.bukkit.entity.LivingEntity) getTarget().getBukkitEntity(), org.bukkit.craftbukkit.inventory.CraftItemStack.asCraftMirror(paperPotion.get()));
        if (!event.callEvent()) {
            ci.cancel();
        }
        paperPotion.set(org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(event.getPotion()));
        // Paper end
    }

    @Redirect(method = "performRangedAttack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;setItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$setPaperPotion(ThrownPotion instance, ItemStack itemStack) {
        instance.setItem(paperPotion.get());
    }
}
