package com.mohistmc.banner.mixin.world.level.block;

import com.mohistmc.banner.injection.world.level.block.InjectionSignBlock;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(SignBlock.class)
public abstract class MixinSignBlock implements InjectionSignBlock {

    @Shadow public abstract void openTextEdit(Player player, SignBlockEntity signEntity, boolean isFrontText);

    private AtomicReference<PlayerOpenSignEvent.Cause> banner$signOpenCause =
            new AtomicReference<>(PlayerOpenSignEvent.Cause.UNKNOWN);

    @Inject(method = "openTextEdit",
            at = @At("HEAD"))
    private void banner$openSignEvent(Player player, SignBlockEntity signEntity, boolean isFrontText, CallbackInfo ci) {
        openTextEdit(player, signEntity, isFrontText, banner$signOpenCause.get());
    }

    @Override
    public void openTextEdit(Player player, SignBlockEntity signEntity, boolean isFrontText, PlayerOpenSignEvent.Cause cause) {
        cause = banner$signOpenCause.get();
        // Paper start
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) player.getBukkitEntity();
        org.bukkit.block.Block bukkitBlock = CraftBlock.at(signEntity.getLevel(), signEntity.getBlockPos());
        CraftSign<?> bukkitSign = (CraftSign<?>) CraftBlockStates.getBlockState(bukkitBlock);
        io.papermc.paper.event.player.PlayerOpenSignEvent event =
                new io.papermc.paper.event.player.PlayerOpenSignEvent(
                        bukkitPlayer,
                        bukkitSign,
                        isFrontText ? org.bukkit.block.sign.Side.FRONT : org.bukkit.block.sign.Side.BACK,
                        cause);
        if (!event.callEvent()) return;
        // Paper end
    }

    @Inject(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/SignBlock;openTextEdit(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/SignBlockEntity;Z)V"))
    private void banner$setCause(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        pushOpenSignCause(PlayerOpenSignEvent.Cause.INTERACT);
    }

    @Override
    public void pushOpenSignCause(PlayerOpenSignEvent.Cause cause) {
        banner$signOpenCause.set(cause);
    }

}
