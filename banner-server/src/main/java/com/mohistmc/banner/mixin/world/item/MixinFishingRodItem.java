package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FishingRodItem.class)
public class MixinFishingRodItem extends Item{

    public MixinFishingRodItem(Properties properties) {
        super(properties);
    }

    // Banner TODO fixme
    /**
     * @author Mgazul
     * @reason
     */
    /*
    @Overwrite
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (player.fishing != null) {
            if (!level.isClientSide) {
                int i = player.fishing.retrieve(itemstack);
                itemstack.hurtAndBreak(i, player, LivingEntity.getSlotForHand(interactionHand));
            }

            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            if (!level.isClientSide) {
                int k = EnchantmentHelper.getFishingTimeReduction(itemstack);
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
    }*/
}
