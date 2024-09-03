package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResetProfession.class)
public class MixinResetProfession {

    @Redirect(method = "desc=/Z$/", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;setVillagerData(Lnet/minecraft/world/entity/npc/VillagerData;)V"))
    private static void banner$cancelSetData(Villager villagerEntity, VillagerData villagerData) {}

    @Inject(method = "desc=/Z$/",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/npc/Villager;setVillagerData(Lnet/minecraft/world/entity/npc/VillagerData;)V"), cancellable = true)
    private static void banner$careerChangeHook(ServerLevel serverLevel, Villager villager,
                                                long l, CallbackInfoReturnable<Boolean> cir) {
        // CraftBukkit start
        VillagerCareerChangeEvent event = CraftEventFactory.callVillagerCareerChangeEvent(villager, CraftVillager.CraftProfession.minecraftToBukkit(VillagerProfession.NONE), VillagerCareerChangeEvent.ChangeReason.LOSING_JOB);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
        villager.setVillagerData(villager.getVillagerData().setProfession(CraftVillager.CraftProfession.bukkitToMinecraft(event.getProfession())));
        // CraftBukkit end
    }
}
