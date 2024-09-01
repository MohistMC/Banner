package com.mohistmc.banner.mixin.world.entity.npc;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WanderingTrader.class)
public abstract class MixinWanderingTrader extends net.minecraft.world.entity.npc.AbstractVillager {

    public MixinWanderingTrader(EntityType<? extends net.minecraft.world.entity.npc.AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "updateTrades", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;add(Ljava/lang/Object;)Z"))
    private boolean banner$gainOffer(MerchantOffers merchantOffers, Object e) {
        MerchantOffer offer = (MerchantOffer) e;
        VillagerAcquireTradeEvent event = new VillagerAcquireTradeEvent((AbstractVillager) getBukkitEntity(), offer.asBukkit());
        if (this.bridge$valid()) {
            Bukkit.getPluginManager().callEvent(event);
        }
        if (!event.isCancelled()) {
            return merchantOffers.add(CraftMerchantRecipe.fromBukkit(event.getRecipe()).toMinecraft());
        }
        return false;
    }
}
