package com.mohistmc.banner.mixin.world.level.storage;

import com.mohistmc.banner.injection.world.level.storage.InjectionPrimaryLevelData;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimaryLevelData.class)
public abstract class MixinPrimaryLevelData implements InjectionPrimaryLevelData {

    @Shadow public abstract boolean isDifficultyLocked();

    @Shadow private boolean raining;

    @Shadow public abstract String getLevelName();

    @Shadow private boolean thundering;
    @Shadow public LevelSettings settings;
    public ServerLevel world;
    public Registry<LevelStem> customDimensions;
    protected Tag pdc;

    @Redirect(method = "setTagData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldGenSettings;encode(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/core/RegistryAccess;)Lcom/mojang/serialization/DataResult;"))
    private <T extends Tag> DataResult<T> banner$customDim(DynamicOps<T> ops, WorldOptions options, RegistryAccess registry) {
        return WorldGenSettings.encode(ops, options, new WorldDimensions(this.customDimensions != null ? this.customDimensions : registry.registryOrThrow(Registries.LEVEL_STEM)));
    }

    @Inject(method = "setThundering", cancellable = true, at = @At("HEAD"))
    private void banner$thunder(boolean thunderingIn, CallbackInfo ci) {
        if (this.thundering == thunderingIn) {
            return;
        }

        World world = Bukkit.getWorld(this.getLevelName());
        if (world != null) {
            ThunderChangeEvent event = new ThunderChangeEvent(world, thunderingIn);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setRaining", cancellable = true, at = @At("HEAD"))
    private void banner$storm(boolean isRaining, CallbackInfo ci) {
        if (this.raining == isRaining) {
            return;
        }

        World world = Bukkit.getWorld(this.getLevelName());
        if (world != null) {
            WeatherChangeEvent event = new WeatherChangeEvent(world, isRaining);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "setDifficulty", at = @At("RETURN"))
    private void banner$sendDiffChange(Difficulty newDifficulty, CallbackInfo ci) {
        ClientboundChangeDifficultyPacket packet = new ClientboundChangeDifficultyPacket(newDifficulty, this.isDifficultyLocked());
        if (this.world != null) {
            for (Player player : this.world.players()) {
                ((ServerPlayer) player).connection.send(packet);
            }
        }
    }

    @Override
    public Registry<LevelStem> bridge$customDimensions() {
        return customDimensions;
    }

    @Override
    public void banner$setCustomDimensions(Registry<LevelStem> customDimensions) {
        this.customDimensions = customDimensions;
    }

    @Override
    public void checkName(String name) {
        if (!this.settings.levelName.equals(name)) {
            this.settings.levelName = name;
        }
    }

    // CraftBukkit start - Add world and pdc
    @Override
    public void setWorld(ServerLevel world) {
        this.world = world;
    }
    // CraftBukkit end
}
