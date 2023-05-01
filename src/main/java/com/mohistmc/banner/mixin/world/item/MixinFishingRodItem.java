package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftEquipmentSlot;
import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FishingRodItem.class)
public class MixinFishingRodItem extends Item{

    public MixinFishingRodItem(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (playerIn.fishing != null) {
            if (!worldIn.isClientSide) {
                int i = playerIn.fishing.retrieve(itemstack);
                itemstack.hurtAndBreak(i, playerIn, (player) -> {
                    player.broadcastBreakEvent(handIn);
                });
            }

            playerIn.swing(handIn);
            worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (worldIn.getRandom().nextFloat() * 0.4F + 0.8F));
            playerIn.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            if (!worldIn.isClientSide) {
                int k = EnchantmentHelper.getFishingSpeedBonus(itemstack);
                int j = EnchantmentHelper.getFishingLuckBonus(itemstack);

                FishingHook hook = new FishingHook(playerIn, worldIn, j, k);
                if (DistValidate.isValid(worldIn)) {
                    PlayerFishEvent playerFishEvent = new PlayerFishEvent(((ServerPlayer) playerIn).getBukkitEntity(), null, (FishHook) hook.getBukkitEntity(), CraftEquipmentSlot.getHand(handIn), PlayerFishEvent.State.FISHING);
                    Bukkit.getPluginManager().callEvent(playerFishEvent);

                    if (playerFishEvent.isCancelled()) {
                        playerIn.fishing = null;
                        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
                    }
                }
                worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (worldIn.getRandom().nextFloat() * 0.4F + 0.8F));
                worldIn.addFreshEntity(new FishingHook(playerIn, worldIn, j, k));
            }

            // playerIn.swingArm(handIn);
            playerIn.awardStat(Stats.ITEM_USED.get(this));
            playerIn.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, worldIn.isClientSide());
    }
}
