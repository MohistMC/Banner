package com.mohistmc.banner.mixin.world.food;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.world.food.InjectionFoodData;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(FoodData.class)
public abstract class MixinFoodData implements InjectionFoodData {

    @Shadow public int foodLevel;
    @Shadow private int lastFoodLevel;
    @Shadow public float saturationLevel;

    @Shadow protected abstract void add(int i, float f);

    private Player entityhuman;
    public int saturatedRegenRate = 10;
    public int unsaturatedRegenRate = 80;
    public int starvationRate = 80;

    @ShadowConstructor
    public void banner$constructor() {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(Player entityhuman) {
        Validate.notNull(entityhuman);
        this.entityhuman = entityhuman;
    }

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

    @Override
    public void eat(ItemStack itemstack, FoodProperties foodinfo) {
        int oldFoodLevel = foodLevel;

        FoodLevelChangeEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callFoodLevelChangeEvent(entityhuman, foodinfo.nutrition() + oldFoodLevel, itemstack);

        if (!event.isCancelled()) {
            this.add(event.getFoodLevel() - oldFoodLevel, foodinfo.saturation());
        }
        ((ServerPlayer) entityhuman).getBukkitEntity().sendHealthUpdate();
    }

    private transient ItemStack banner$eatStack;

    @Decorate(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;add(IF)V"))
    private void banner$foodLevelChange(FoodData foodStats, int foodLevelIn, float foodSaturationModifier, FoodProperties food) throws Throwable {
        var stack = this.banner$eatStack;
        this.banner$eatStack = null;
        int deltaFoodLevel = foodLevelIn;
        if (this.entityhuman != null && stack != null) {
            int newFoodLevel = Mth.clamp(this.foodLevel + foodLevelIn, 0, 20);
            FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(this.entityhuman, newFoodLevel, stack);
            if (event.isCancelled()) {
                return;
            }
            deltaFoodLevel = event.getFoodLevel() - this.foodLevel;
            ((ServerPlayer) this.entityhuman).getBukkitEntity().sendHealthUpdate();
        }
        DecorationOps.callsite().invoke(foodStats, deltaFoodLevel, foodSaturationModifier);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_ASSIGN", remap = false, target = "Ljava/lang/Math;max(II)I"))
    public void banner$foodLevelChange2(Player player, CallbackInfo ci) {
        if (entityhuman == null) {
            return;
        }
        FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, Math.max(this.lastFoodLevel - 1, 0));

        if (!event.isCancelled()) {
            this.foodLevel = event.getFoodLevel();
        } else {
            this.foodLevel = this.lastFoodLevel;
        }

        ((ServerPlayer) entityhuman).connection.send(new ClientboundSetHealthPacket(((ServerPlayer) entityhuman).getBukkitEntity().getScaledHealth(), this.foodLevel, this.saturationLevel));
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

    @Override
    public void pushEatStack(ItemStack stack) {
        this.banner$eatStack = stack;
    }
}
