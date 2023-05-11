package com.mohistmc.banner.mixin.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ChorusFruitItem.class)
public abstract class MixinChorusFruitItem extends Item {

    public MixinChorusFruitItem(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack itemStack = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide) {
            double d = livingEntity.getX();
            double e = livingEntity.getY();
            double f = livingEntity.getZ();

            for (int i = 0; i < 16; ++i) {
                double g = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
                double h = Mth.clamp(livingEntity.getY() + (double) (livingEntity.getRandom().nextInt(16) - 8), (double) level.getMinBuildHeight(), (double) (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1));
                double j = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
                if (livingEntity.isPassenger()) {
                    livingEntity.stopRiding();
                }

                Vec3 vec3 = livingEntity.position();
                java.util.Optional<Boolean> status = livingEntity.randomTeleport(g, h, j, true, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);

                if (!status.isPresent()) {
                    // teleport event was canceled, no more tries
                    break;
                }

                if (status.get()) {
                // CraftBukkit end
                level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(livingEntity));
                SoundEvent soundEvent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                level.playSound((net.minecraft.world.entity.player.Player) null, d, e, f, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
                livingEntity.playSound(soundEvent, 1.0F, 1.0F);
                break;
                }
            }

            if (livingEntity instanceof net.minecraft.world.entity.player.Player) {
                ((Player) livingEntity).getCooldowns().addCooldown(this, 20);
            }
        }

        return itemStack;
    }
}
