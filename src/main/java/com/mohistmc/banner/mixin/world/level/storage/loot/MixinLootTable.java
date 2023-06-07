package com.mohistmc.banner.mixin.world.level.storage.loot;

import com.mohistmc.banner.injection.world.level.storage.loot.InjectionLootTable;
import io.izzel.arclight.mixin.Eject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LootTable.class)
public abstract class MixinLootTable implements InjectionLootTable {

    @Shadow public abstract ObjectArrayList<ItemStack> getRandomItems(LootContext context);

    @Shadow protected abstract List<Integer> getAvailableSlots(Container inventory, RandomSource random);

    @Shadow protected abstract void shuffleAndSplitItems(ObjectArrayList<ItemStack> stacks, int emptySlotsCount, RandomSource random);

    @Shadow @Final
    static Logger LOGGER;

    @Eject(method = "fill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> banner$nonPluginEvent(LootTable lootTable, LootParams lootParams, CallbackInfo ci, Container inv) {
        ObjectArrayList<ItemStack> list = lootTable.getRandomItems(lootParams);
        if (!lootParams.hasParam(LootContextParams.ORIGIN) && !lootParams.hasParam(LootContextParams.THIS_ENTITY)) {
            return list;
        }
        // Banner * TODO
        /**
        LootGenerateEvent event = CraftEventFactory.callLootGenerateEvent(inv, (LootTable) (Object) this, lootParams, list, false);
        if (event.isCancelled()) {
            ci.cancel();
            return null;
        } else {
            return event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(ObjectArrayList.toList());
        }*/
        return list;
    }
}
