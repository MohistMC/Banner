package com.mohistmc.banner.mixin.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AssignProfessionFromJobSite.class)
public class MixinAssignProfessionFromJobSite {

    @Redirect(method = "method_46891", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;setVillagerData(Lnet/minecraft/world/entity/npc/VillagerData;)V"))
    private static void banner$cancelJob(Villager instance, VillagerData villagerData) {}

    @Inject(method = "method_46891", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/npc/Villager;setVillagerData(Lnet/minecraft/world/entity/npc/VillagerData;)V"), cancellable = true)
    private static void banner$jobChange(Villager villager, ServerLevel serverLevel, VillagerProfession villagerProfession, CallbackInfo ci) {
        // CraftBukkit start - Fire VillagerCareerChangeEvent where Villager gets employed
        VillagerCareerChangeEvent event = CraftEventFactory.callVillagerCareerChangeEvent(villager, CraftVillager.CraftProfession.minecraftToBukkit(villagerProfession), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
        if (event.isCancelled()) {
            ci.cancel();
        }

        villager.setVillagerData(villager.getVillagerData().setProfession(CraftVillager.CraftProfession.bukkitToMinecraft(event.getProfession())));
        // CraftBukkit end
    }
}
