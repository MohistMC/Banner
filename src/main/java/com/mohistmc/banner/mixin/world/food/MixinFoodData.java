package com.mohistmc.banner.mixin.world.food;

import com.mohistmc.banner.injection.world.food.InjectionFoodData;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class MixinFoodData implements InjectionFoodData {

    @Shadow public int foodLevel;
    @Shadow private int lastFoodLevel;
    @Shadow public float saturationLevel;
    private Player entityhuman;
    public int saturatedRegenRate = 10;
    public int unsaturatedRegenRate = 80;
    public int starvationRate = 80;

    public void banner$constructor() {
        throw new RuntimeException();
    }

    public void banner$constructor(Player entityhuman) {
        org.apache.commons.lang.Validate.notNull(entityhuman);
        this.entityhuman = entityhuman;
    }

    private transient Item banner$foodItem;
    private transient ItemStack banner$foodStack;

    private AtomicBoolean duplicateCall = new AtomicBoolean(false);

    @Inject(method = "eat(IF)V", at = @At("HEAD"), cancellable = true)
    private void banner$eatCake(int foodLevelModifier, float saturationLevelModifier, CallbackInfo ci) {
        // Banner start
        if (!duplicateCall.getAndSet(false)) {
            int old = this.foodLevel;
            FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, old + foodLevelModifier);
            if (event.isCancelled()) ci.cancel();
            foodLevelModifier = event.getFoodLevel() - old;
        }
        // Banner end
    }

    @Inject(method = "eat(IF)V", at = @At("TAIL"))
    private void banner$sendUpdate(int foodLevelModifier, float saturationLevelModifier, CallbackInfo ci) {
        ((ServerPlayer) entityhuman).getBukkitEntity().sendHealthUpdate(); // Banner
    }

    @Inject(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void banner$setFoodInformation(Item item, ItemStack stack, CallbackInfo ci) {
        this.banner$foodItem = item;
        this.banner$foodStack = stack;
    }

    @Redirect(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE",target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
    private void banner$foodLevelChange(FoodData instance, int foodLevelModifier, float saturationLevelModifier) {
        int oldFoodLevel = foodLevel;
        FoodProperties foodInfo = banner$foodItem.getFoodProperties();
        FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, foodInfo.getNutrition() + oldFoodLevel, banner$foodStack);

        if (!event.isCancelled()) {
            duplicateCall.set(true);
            instance.eat(event.getFoodLevel() - oldFoodLevel, saturationLevelModifier);
        }
        // CraftBukkit end
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_ASSIGN", remap = false, target = "Ljava/lang/Math;max(II)I"))
    public void banner$foodLevelChange2(Player player, CallbackInfo ci) {
        if (entityhuman == null) {
            return;
        }
        FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, Math.max(this.lastFoodLevel - 1, 0));
        duplicateCall.set(true);
        if (!event.isCancelled()) {
            this.foodLevel = event.getFoodLevel();
        } else {
            this.foodLevel = this.lastFoodLevel;
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    public void banner$heal(Player player, CallbackInfo ci) {
        if (entityhuman == null) {
            entityhuman = player;
        }
         player.pushHealReason(EntityRegainHealthEvent.RegainReason.SATIATED);
         player.pushExhaustReason(EntityExhaustionEvent.ExhaustionReason.REGEN);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int banner$changeValue(int constant) {
        return this.saturatedRegenRate; // CraftBukkit
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 80, ordinal = 0))
    private int banner$changeValue0(int constant) {
        return this.unsaturatedRegenRate; // CraftBukkit - add regen rate manipulation
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 80, ordinal = 1))
    private int banner$changeValue1(int constant) {
        return this.starvationRate;  // CraftBukkit - add regen rate manipulation
    }

    @Override
    public Player getEntityhuman() {
        return entityhuman;
    }

    @Override
    public void setEntityhuman(Player entityhuman) {
        this.entityhuman = entityhuman;
    }

    @Override
    public int bridge$saturatedRegenRate() {
        return saturatedRegenRate;
    }

    @Override
    public void banner$setSaturatedRegenRate(int saturatedRegenRate) {
        this.saturatedRegenRate = saturatedRegenRate;
    }

    @Override
    public int bridge$unsaturatedRegenRate() {
        return unsaturatedRegenRate;
    }

    @Override
    public void banner$setUnsaturatedRegenRate(int unsaturatedRegenRate) {
        this.unsaturatedRegenRate = unsaturatedRegenRate;
    }

    @Override
    public int bridge$starvationRate() {
        return starvationRate;
    }

    @Override
    public void banner$setStarvationRate(int starvationRate) {
        this.starvationRate = starvationRate;
    }
}
