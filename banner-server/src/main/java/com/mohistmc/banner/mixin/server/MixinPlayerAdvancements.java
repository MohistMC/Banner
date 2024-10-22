package com.mohistmc.banner.mixin.server;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancements {

    @Shadow private ServerPlayer player;

    @Shadow @Final private Path playerSavePath;

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "award",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;getOrStartProgress(Lnet/minecraft/advancements/AdvancementHolder;)Lnet/minecraft/advancements/AdvancementProgress;"))
    public void banner$callEvent(AdvancementHolder advancementHolder, String string, CallbackInfoReturnable<Boolean> cir) {
        Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.PlayerAdvancementDoneEvent(this.player.getBukkitEntity(), advancementHolder.bridge$bukkit()));
    }

    private AtomicReference<Map.Entry<ResourceLocation, AdvancementProgress>> banner$entry = new AtomicReference<>();

    @Inject(method = "method_48027",
            at = @At(value = "HEAD"), cancellable = true)
    private void banner$disableAdvancementSaving(Set set, Set set2, AdvancementNode advancementNode, boolean bl, CallbackInfo ci) {
        if (org.spigotmc.SpigotConfig.disableAdvancementSaving) ci.cancel(); // Spigot
    }

}
