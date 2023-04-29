package com.mohistmc.banner.mixin.world.food;

import com.mohistmc.banner.injection.world.food.InjectionFoodData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FoodData.class)
public class MixinFoodData implements InjectionFoodData {

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
