package com.mohistmc.banner.mixin.world.entity;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Banner TODO fix
@Mixin(ExperienceOrb.class)
public abstract class MixinExperienceOrb {

    @Shadow private Player followingPlayer;
    @Shadow public int value;

    private transient Player banner$lastPlayer;

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void banner$captureLast(CallbackInfo ci) {
        banner$lastPlayer = this.followingPlayer;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void banner$captureReset(CallbackInfo ci) {
        banner$lastPlayer = null;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", ordinal = 4, target = "Lnet/minecraft/world/entity/ExperienceOrb;followingPlayer:Lnet/minecraft/world/entity/player/Player;"))
    private Player banner$targetPlayer(ExperienceOrb entity) {
        if (this.followingPlayer != banner$lastPlayer) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent((ExperienceOrb) (Object) this, this.followingPlayer, (this.followingPlayer != null) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.FORGOT_TARGET);
            LivingEntity target = (event.getTarget() == null) ? null : ((CraftLivingEntity) event.getTarget()).getHandle();

            if (event.isCancelled()) {
                this.followingPlayer = banner$lastPlayer;
                return null;
            } else {
                this.followingPlayer = (target instanceof Player) ? (Player) target : null;
            }
        }
        return this.followingPlayer;
    }

    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private void banner$expChange(Player player, int amount) {
        player.giveExperiencePoints(CraftEventFactory.callPlayerExpChangeEvent(player, amount).getAmount());
    }

    @Decorate(method = "repairPlayerItems", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"))
    private void banner$itemMend(ServerPlayer serverPlayer, int i, @Local(ordinal = -1) ItemStack itemstack,
                                   @Local(ordinal = -1) Optional<EnchantedItemInUse> optional, @Local(ordinal = -1) int k) throws Throwable {
        org.bukkit.event.player.PlayerItemMendEvent event = CraftEventFactory.callPlayerItemMendEvent(serverPlayer, (ExperienceOrb) (Object) this, itemstack, optional.get().inSlot(), k);
        k = event.getRepairAmount();
        if (event.isCancelled()) {
            DecorationOps.cancel().invoke(i);
            return;
        }
        DecorationOps.blackhole().invoke(k);
    }

    @Decorate(method = "repairPlayerItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;repairPlayerItems(Lnet/minecraft/server/level/ServerPlayer;I)I"))
    private int banner$updateXp(ExperienceOrb instance, ServerPlayer serverPlayer, int i) throws Throwable {
        this.value = i;
        return (int) DecorationOps.callsite().invoke(instance, serverPlayer, i);
    }

    @Inject(method = "getExperienceValue", cancellable = true, at = @At("HEAD"))
    private static void banner$higherLevelSplit(int expValue, CallbackInfoReturnable<Integer> cir) {
        // @formatter:off
        if (expValue > 162670129) { cir.setReturnValue(expValue - 100000); return; }
        if (expValue > 81335063) { cir.setReturnValue(81335063); return; }
        if (expValue > 40667527) { cir.setReturnValue(40667527); return; }
        if (expValue > 20333759) { cir.setReturnValue(20333759); return; }
        if (expValue > 10166857) { cir.setReturnValue(10166857); return; }
        if (expValue > 5083423) { cir.setReturnValue(5083423); return; }
        if (expValue > 2541701) { cir.setReturnValue(2541701); return; }
        if (expValue > 1270849) { cir.setReturnValue(1270849); return; }
        if (expValue > 635413) { cir.setReturnValue(635413); return; }
        if (expValue > 317701) { cir.setReturnValue(317701); return; }
        if (expValue > 158849) { cir.setReturnValue(158849); return; }
        if (expValue > 79423) { cir.setReturnValue(79423); return; }
        if (expValue > 39709) { cir.setReturnValue(39709); return; }
        if (expValue > 19853) { cir.setReturnValue(19853); return; }
        if (expValue > 9923) { cir.setReturnValue(9923); return; }
        if (expValue > 4957) { cir.setReturnValue(4957); }
        // @formatter:on
    }
}
