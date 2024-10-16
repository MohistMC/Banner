package com.mohistmc.banner.mixin.world.level.block.entity;

import java.util.List;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConduitBlockEntity.class)
public class MixinConduitBlockEntity {

    @Redirect(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private static boolean banner$addEntity(Player player, MobEffectInstance eff) {
        player.pushEffectCause(EntityPotionEffectEvent.Cause.CONDUIT);
        return player.addEffect(eff);
    }

    @Inject(method = "updateDestroyTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private static void banner$attackReason(Level level, BlockPos pos, BlockState p_155411_, List<BlockPos> p_155412_, ConduitBlockEntity p_155413_, CallbackInfo ci) {
        CraftEventFactory.blockDamage = CraftBlock.at(level, pos);
    }

    @Inject(method = "updateDestroyTarget", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private static void banner$attackReasonReset(CallbackInfo ci) {
        CraftEventFactory.blockDamage = null;
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static int getRange(List<BlockPos> list) {
        // CraftBukkit end
        int i = list.size();
        int j = i / 7 * 16;
        // CraftBukkit start
        return j;
    }
}
