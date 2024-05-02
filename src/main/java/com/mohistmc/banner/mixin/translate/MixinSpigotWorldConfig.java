package com.mohistmc.banner.mixin.translate;

import com.mohistmc.banner.BannerMCStart;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = SpigotWorldConfig.class, remap = false)
public abstract class MixinSpigotWorldConfig {

    @Shadow protected abstract void log(String s);

    @Shadow public double itemMerge;

    @Shadow public double expMerge;

    @Shadow public int viewDistance;

    @Shadow public byte mobSpawnRange;

    @Shadow public int itemDespawnRate;

    @Shadow public int animalActivationRange;

    @Shadow public int monsterActivationRange;

    @Shadow public int raiderActivationRange;

    @Shadow public int miscActivationRange;

    @Shadow public boolean tickInactiveVillagers;

    @Shadow public boolean ignoreSpectatorActivation;

    @Shadow @Final private String worldName;

    @Shadow public int miscTrackingRange;

    @Shadow public int displayTrackingRange;

    @Shadow public int otherTrackingRange;

    @Shadow public int monsterTrackingRange;

    @Shadow public int animalTrackingRange;

    @Shadow public int playerTrackingRange;

    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$i18nWorldSettings(SpigotWorldConfig instance, String s) {
        log( BannerMCStart.I18N.as("spigotworldconfig.1") + worldName + "] --------" );
    }

    @Redirect(method = "activationRange", at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$i18nEntityRange(SpigotWorldConfig instance, String s) {
        log( BannerMCStart.I18N.as("spigotworldconfig.2") + " "  + animalActivationRange
                + " / Mo " + monsterActivationRange
                + " / Ra " + raiderActivationRange
                + " / Mi " + miscActivationRange
                + " / Tiv " + tickInactiveVillagers
                + " / Isa " + ignoreSpectatorActivation );
    }

    @Redirect(method = "itemDespawnRate",  at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$i18nDespawnRate(SpigotWorldConfig instance, String s) {
        log(BannerMCStart.I18N.as("spigotworldconfig.3") + " " + itemDespawnRate );
    }

    @Redirect(method = "mobSpawnRange",  at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$i18nSpawnRange(SpigotWorldConfig instance, String s) {
        log(BannerMCStart.I18N.as("spigotworldconfig.4") + " " + mobSpawnRange );
    }

    @Redirect(method = "viewDistance",  at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$i18nViewDistance(SpigotWorldConfig instance, String s) {
        log(BannerMCStart.I18N.as("spigotworldconfig.5") + " " + viewDistance );
    }

    @Redirect(method = "expMerge",  at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$i18nExpMerge(SpigotWorldConfig instance, String s) {
        log(BannerMCStart.I18N.as("spigotworldconfig.6") + " " + expMerge );
    }

    @Redirect(method = "itemMerge", at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$i18nItemMerge(SpigotWorldConfig instance, String s) {
        log(BannerMCStart.I18N.as("spigotworldconfig.7") + " " + itemMerge );
    }

    private AtomicInteger banner$modifier = new AtomicInteger();
    private AtomicReference<String> banner$crop = new AtomicReference<>();

    @Inject(method = "getAndValidateGrowth",
            at = @At(value = "INVOKE",
                    target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$getInfo(String crop, CallbackInfoReturnable<Integer> cir, int modifier) {
        banner$modifier.set(modifier);
        banner$crop.set(crop);
    }

    @Redirect(method = "getAndValidateGrowth", at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$validateGrowth(SpigotWorldConfig instance, String s) {
        log( banner$crop.get() + " " + BannerMCStart.I18N.as("spigotworldconfig.8")  + banner$modifier.get() + "%" );
    }

    @Redirect(method = "trackingRange", at = @At(value = "INVOKE",
            target = "Lorg/spigotmc/SpigotWorldConfig;log(Ljava/lang/String;)V"))
    private void bosom$trackingRange(SpigotWorldConfig instance, String s) {
        log(  BannerMCStart.I18N.as("spigotworldconfig.9")  +
                " Pl " + playerTrackingRange
                + " / An " + animalTrackingRange
                + " / Mo " + monsterTrackingRange
                + " / Mi " + miscTrackingRange
                + " / Di " + displayTrackingRange
                + " / Other " + otherTrackingRange );
    }
}
