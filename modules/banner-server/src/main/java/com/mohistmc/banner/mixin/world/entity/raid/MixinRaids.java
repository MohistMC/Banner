package com.mohistmc.banner.mixin.world.entity.raid;

import java.util.Map;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Raids.class)
public class MixinRaids {

    @Shadow @Final public Map<Integer, Raid> raidMap;

    /*
    @Inject(method = "createOrExtendRaid", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;absorbBadOmen(Lnet/minecraft/world/entity/player/Player;)V"))
    public void banner$raidTrigger(ServerPlayer playerEntity, CallbackInfoReturnable<Raid> cir,
                                     DimensionType dimensionType, BlockPos pos, BlockPos pos1, List<?> list, int i, Vec3 vec, Raid raid) {
        if (!CraftEventFactory.callRaidTriggerEvent(raid, playerEntity)) {
            playerEntity.removeEffect(MobEffects.BAD_OMEN);
            this.raidMap.remove(raid.getId(), raid);
            cir.setReturnValue(null);
        }
    }*/

}
