package com.mohistmc.banner.mixin.world.entity.boss.enderdragon.phases;

import com.mohistmc.banner.BannerMod;
import com.mohistmc.banner.api.ServerAPI;
import com.mohistmc.banner.injection.world.entity.boss.enderdragon.phases.InjectionEnderDragonPhase;
import com.mohistmc.dynamicenum.MohistDynamEnum;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderDragonPhase.class)
public class MixinEnderDragonPhase implements InjectionEnderDragonPhase {

    @Shadow private static EnderDragonPhase<?>[] phases;

    @Shadow @Final private String name;

    @Inject(method = "create", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static <T extends DragonPhaseInstance> void banner$addPhase(Class<T> phase, String name,
                                                                        CallbackInfoReturnable<EnderDragonPhase<T>> cir,
                                                                        EnderDragonPhase<T> enderDragonPhase) {
        if (enderDragonPhase.getId() > 10) {
            org.bukkit.entity.EnderDragon.Phase bukkit = MohistDynamEnum.addEnum(org.bukkit.entity.EnderDragon.Phase.class, enderDragonPhase.getName());
            ServerAPI.phasetypeMap.put(enderDragonPhase.getId(), bukkit);
            BannerMod.LOGGER.debug("Registered fabric PhaseType as EnderDragon.Phase {}", bukkit);
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
