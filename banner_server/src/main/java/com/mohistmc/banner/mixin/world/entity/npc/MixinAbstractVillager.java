package com.mohistmc.banner.mixin.world.entity.npc;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.npc.AbstractVillager.class)
public abstract class MixinAbstractVillager extends AgeableMob implements InventoryCarrier, Npc, Merchant {

    protected MixinAbstractVillager(EntityType<? extends AgeableMob> entityType, Level level) {
        super(entityType, level);
    }

    private CraftMerchant craftMerchant;
    @Shadow @Final private SimpleContainer inventory;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(EntityType<? extends net.minecraft.world.entity.npc.AbstractVillager> type, Level worldIn, CallbackInfo ci) {
         this.inventory.setOwner((InventoryHolder) this.getBukkitEntity());
    }

    @Override
    public CraftMerchant getCraftMerchant() {
        return (craftMerchant == null) ? craftMerchant = new CraftMerchant((net.minecraft.world.entity.npc.AbstractVillager) (Object) this) : craftMerchant;
    }

    @Redirect(method = "addOffersFromItemListings", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;add(Ljava/lang/Object;)Z"))
    private boolean banner$gainOffer(MerchantOffers merchantOffers, Object e) {
        MerchantOffer offer = (MerchantOffer) e;
        VillagerAcquireTradeEvent event = new VillagerAcquireTradeEvent((AbstractVillager) getBukkitEntity(),  offer.asBukkit());
        if (this.bridge$valid()) {
            Bukkit.getPluginManager().callEvent(event);
        }
        if (!event.isCancelled()) {
            return merchantOffers.add(CraftMerchantRecipe.fromBukkit(event.getRecipe()).toMinecraft());
        }
        return false;
    }
}
