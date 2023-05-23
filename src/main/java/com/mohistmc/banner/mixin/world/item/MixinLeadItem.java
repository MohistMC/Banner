package com.mohistmc.banner.mixin.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_19_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(LeadItem.class)
@SuppressWarnings("deprecation")
public abstract class MixinLeadItem {

    private static AtomicReference<InteractionHand> banner$hand = new AtomicReference<>(InteractionHand.MAIN_HAND);

    /**
     * @author wdog5
     * @reason bukkit event
     */
    @Overwrite
    public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos pos) {
        LeashFenceKnotEntity leashFenceKnotEntity = null;
        boolean bl = false;
        double d = 7.0;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        List<Mob> list = level.getEntitiesOfClass(Mob.class, new AABB((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0));

        for (Mob mob : list) {
            if (mob.getLeashHolder() == player) {
                if (leashFenceKnotEntity == null) {
                    leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, pos);
                    // CraftBukkit start - fire HangingPlaceEvent
                    org.bukkit.inventory.EquipmentSlot hand = CraftEquipmentSlot.getHand(banner$hand.get());
                    HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) leashFenceKnotEntity.getBukkitEntity(), player != null ? (org.bukkit.entity.Player) player.getBukkitEntity() : null, level.getWorld().getBlockAt(i, j, k), org.bukkit.block.BlockFace.SELF, hand);
                    level.getCraftServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        leashFenceKnotEntity.discard();
                        return InteractionResult.PASS;
                    }
                    // CraftBukkit end
                    leashFenceKnotEntity.playPlacementSound();
                }

                // CraftBukkit start
                if (player != null && CraftEventFactory.callPlayerLeashEntityEvent(mob, leashFenceKnotEntity, player, banner$hand.get()).isCancelled()) {
                    continue;
                }

                mob.setLeashedTo(leashFenceKnotEntity, true);
                bl = true;
            }
        }

        if (bl) {
            level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(player));
        }

        return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static InteractionResult bindPlayerMobs(Player entityhuman, Level world, BlockPos blockposition, InteractionHand enumhand) { // CraftBukkit - Add EnumHand
       banner$hand.set(enumhand);
       return bindPlayerMobs(entityhuman, world, blockposition);
    }
}
