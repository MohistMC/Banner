package com.mohistmc.banner.mixin.world.level.dimension.end;

import com.mohistmc.banner.injection.world.level.dimension.end.InjectionEndDragonFight;
import java.util.List;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndDragonFight.class)
public class MixinEndDragonFight implements InjectionEndDragonFight {

    public boolean banner$respawnDragon = false;

    @Inject(method = "respawnDragon",
            at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;respawnCrystals:Ljava/util/List;",
                    shift = At.Shift.AFTER))
    private void banner$setRespawnResult(List<EndCrystal> crystals, CallbackInfo ci) {
        banner$respawnDragon = true;
    }

    @Override
    public boolean bridge$isRespawnDragon() {
        return banner$respawnDragon;
    }
}
