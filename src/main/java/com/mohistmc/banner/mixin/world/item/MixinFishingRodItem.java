package com.mohistmc.banner.mixin.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.event.player.PlayerFishEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FishingRodItem.class)
public class MixinFishingRodItem extends Item{

    public MixinFishingRodItem(Properties properties) {
        super(properties);
    }

    /**
     * @author Mgazul
     * @reason
     */
    @Overwrite
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (player.fishing != null) {
            if (!level.isClientSide) {
                int i = player.fishing.retrieve(itemstack);
                itemstack.hurtAndBreak(i, player, (p_41288_) -> {
                    p_41288_.broadcastBreakEvent(interactionHand);
                });
            }

            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            if (!level.isClientSide) {
                int k = EnchantmentHelper.getFishingSpeedBonus(itemstack);
                int j = EnchantmentHelper.getFishingLuckBonus(itemstack);
                // CraftBukkit start
                FishingHook entityfishinghook = new FishingHook(player, level, j, k);
                PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) player.getBukkitEntity(), null, (org.bukkit.entity.FishHook) entityfishinghook.getBukkitEntity(), CraftEquipmentSlot.getHand(interactionHand), PlayerFishEvent.State.FISHING);
                level.getCraftServer().getPluginManager().callEvent(playerFishEvent);

                if (playerFishEvent.isCancelled()) {
                    player.fishing = null;
                    return InteractionResultHolder.pass(itemstack);
                }
                level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                level.addFreshEntity(entityfishinghook);
                // CraftBukkit end
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            player.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
