package com.mohistmc.banner.mixin.world.level.storage.loot;

import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootDataType.class)
public class MixinLootDataType {

    @Inject(method = "createLootTableValidator", cancellable = true, at = @At("RETURN"))
    private static void banner$setHandle(CallbackInfoReturnable<LootDataType.Validator<LootTable>> cir) {
        var validator = cir.getReturnValue();
        cir.setReturnValue((validationContext, resourceKey, object) -> {
            validator.run(validationContext, resourceKey, object);
            object.banner$setCraftLootTable(new CraftLootTable(CraftNamespacedKey.fromMinecraft(resourceKey.location()), object));
        });
    }
}
