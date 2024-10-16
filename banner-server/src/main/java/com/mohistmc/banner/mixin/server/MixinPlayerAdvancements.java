package com.mohistmc.banner.mixin.server;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {

    @Shadow private ServerPlayer player;

    @Shadow @Final private Path playerSavePath;

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "award",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/Advancement;getRewards()Lnet/minecraft/advancements/AdvancementRewards;"))
    public void banner$callEvent(Advancement advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
        Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.PlayerAdvancementDoneEvent(this.player.getBukkitEntity(), advancement.bridge$bukkit()));
    }

    @Unique
    private AtomicReference<Map.Entry<ResourceLocation, AdvancementProgress>> banner$entry = new AtomicReference<>();

    @Inject(method = "method_48026",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/ServerAdvancementManager;getAdvancement(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/advancements/Advancement;"))
    private void banner$getEntry(ServerAdvancementManager serverAdvancementManager, Map.Entry<ResourceLocation, AdvancementProgress> entry, CallbackInfo ci) {
        banner$entry.set(entry);
    }

    @Redirect(method = "method_48026",
            at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false))
    private void banner$checkLogger(Logger instance, String s, Object o1, Object o2) {
        if (banner$entry.get().getKey().getNamespace().equals("minecraft")) {
            LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", banner$entry.getAndSet(null).getKey(), this.playerSavePath);
        }
    }

    @Inject(method = "method_48027",
            at = @At(value = "HEAD"), cancellable = true)
    private void banner$disableAdvancementSaving(Set<Advancement> set, Set<ResourceLocation> set2, Advancement advancement, boolean bl, CallbackInfo ci) {
        if (org.spigotmc.SpigotConfig.disableAdvancementSaving) ci.cancel(); // Spigot
    }

    @Inject(method = "award", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerAdvancements;unregisterListeners(Lnet/minecraft/advancements/Advancement;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void banner$fireAdvancementEvent(Advancement advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir,
                                             boolean bl, AdvancementProgress advancementProgress, boolean bl2) {
        // Paper start
        if (!new com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent(this.player.getBukkitEntity(), advancement.bridge$bukkit(), criterionKey).callEvent()) {
            advancementProgress.revokeProgress(criterionKey);
            cir.setReturnValue(false);
        }
        // Paper end
    }
}
