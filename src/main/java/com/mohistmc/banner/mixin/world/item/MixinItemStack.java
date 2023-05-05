package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.injection.world.item.InjectionItemStack;
import com.mohistmc.banner.util.ServerUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements InjectionItemStack {

    // @formatter:off
    @Shadow @Deprecated private Item item;
    @Shadow private int count;
    // @formatter:on
    @Shadow public abstract CompoundTag save(CompoundTag compoundTag);

    @Override
    public void convertStack(int version) {
        if (0 < version && version < CraftMagicNumbers.INSTANCE.getDataVersion()) {
            CompoundTag savedStack = new CompoundTag();
            this.save(savedStack);
            savedStack = (CompoundTag) ServerUtils.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic(NbtOps.INSTANCE, savedStack), version, CraftMagicNumbers.INSTANCE.getDataVersion()).getValue();
            this.load(savedStack);
        }
    }

    @Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
    public void banner$callPlayerItemDamageEvent(int i, RandomSource random, ServerPlayer entityplayer, CallbackInfoReturnable<Boolean> ci) {
        if (!((ItemStack)(Object)this).isDamageableItem()) {
            ci.setReturnValue(false);
            return;
        }
        int j;

        if (i > 0) {
            j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, ((ItemStack)(Object)this));
            for (int l = 0; j > 0 && l < i; ++l) if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(((ItemStack)(Object)this), j, random)) i--;

            if (entityplayer != null) {
                PlayerItemDamageEvent event = new PlayerItemDamageEvent(((ServerPlayer)entityplayer).getBukkitEntity(), CraftItemStack.asCraftMirror((ItemStack)(Object)this), i);
                event.getPlayer().getServer().getPluginManager().callEvent(event);

                if (i != event.getDamage() || event.isCancelled()) event.getPlayer().updateInventory();
                if (event.isCancelled()) {
                    ci.setReturnValue(false);
                    return;
                }
                i = event.getDamage();
            }
            if (i <= 0) {
                ci.setReturnValue(false);
                return;
            }
        }
        if (entityplayer != null && i != 0) CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(entityplayer, ((ItemStack)(Object)this), ((ItemStack)(Object)this).getDamageValue() + i);

        ((ItemStack)(Object)this).setDamageValue((j = ((ItemStack)(Object)this).getDamageValue() + i));
        ci.setReturnValue(j >= ((ItemStack)(Object)this).getMaxDamage());
        return;
    }

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private <T extends LivingEntity> void banner$itemBreak(int amount, T entityIn, Consumer<T> onBroken, CallbackInfo ci) {
        if (this.count == 1 && entityIn instanceof Player) {
            CraftEventFactory.callPlayerItemBreakEvent(((Player) entityIn), (ItemStack) (Object) this);
        }
    }
}
