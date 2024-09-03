package com.mohistmc.banner.mixin.world.item;

import com.mohistmc.banner.bukkit.DistValidate;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ChorusFruitItem.class)
public abstract class MixinChorusFruitItem extends Item {

    public MixinChorusFruitItem(Properties properties) {
        super(properties);
    }

    /**
     * @author wdog5
     * @reason bukkit handle
     */
    @Overwrite
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack itemStack = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide) {
            double d = livingEntity.getX();
            double e = livingEntity.getY();
            double f = livingEntity.getZ();

            for(int i = 0; i < 16; ++i) {
                double g = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
                double h = Mth.clamp(livingEntity.getY() + (double)(livingEntity.getRandom().nextInt(16) - 8), (double)level.getMinBuildHeight(), (double)(level.getMinBuildHeight() + ((ServerLevel)level).getLogicalHeight() - 1));
                double j = livingEntity.getZ() + (livingEntity.getRandom().nextDouble() - 0.5) * 16.0;
                if (livingEntity instanceof ServerPlayer serverPlayer && DistValidate.isValid(level)) {
                    org.bukkit.entity.Player player = serverPlayer.getBukkitEntity();
                    PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getLocation(), new Location(player.getWorld(), g, h, j), PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        break;
                    }
                    g = event.getTo().getX();
                    h = event.getTo().getY();
                    j = event.getTo().getZ();
                }

                if (livingEntity.isPassenger()) {
                    livingEntity.stopRiding();
                }
                Vec3 vec3d = livingEntity.position();
                if (livingEntity.randomTeleport(g, h, j, true)) {
                    level.gameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Context.of(livingEntity));
                    SoundEvent soundevent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    level.playSound(null, d, e, f, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    livingEntity.playSound(soundevent, 1.0F, 1.0F);
                    livingEntity.resetFallDistance();
                    break;
                }
            }

            if (livingEntity instanceof Player) {
                ((Player)livingEntity).getCooldowns().addCooldown(this, 20);
            }
        }

        return itemStack;
    }
}
