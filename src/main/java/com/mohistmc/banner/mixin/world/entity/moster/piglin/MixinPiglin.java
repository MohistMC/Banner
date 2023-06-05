package com.mohistmc.banner.mixin.world.entity.moster.piglin;

import com.mohistmc.banner.injection.world.entity.InjectionPiglin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Piglin.class)
public abstract class MixinPiglin extends AbstractPiglin implements InjectionPiglin {

    public Set<Item> allowedBarterItems = new HashSet<>();
    public Set<Item> interestItems = new HashSet<>();

    public MixinPiglin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void banner$writeAdditional(CompoundTag compound, CallbackInfo ci) {
        ListTag barterList = new ListTag();
        allowedBarterItems.stream().map(BuiltInRegistries.ITEM::getKey).map(ResourceLocation::toString).map(StringTag::valueOf).forEach(barterList::add);
        compound.put("Bukkit.BarterList", barterList);
        ListTag interestList = new ListTag();
        interestItems.stream().map(BuiltInRegistries.ITEM::getKey).map(ResourceLocation::toString).map(StringTag::valueOf).forEach(interestList::add);
        compound.put("Bukkit.InterestList", interestList);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void banner$readAdditional(CompoundTag compound, CallbackInfo ci) {
        this.allowedBarterItems = compound.getList("Bukkit.BarterList", 8).stream().map(Tag::getAsString).map(ResourceLocation::tryParse).map(BuiltInRegistries.ITEM::get).collect(Collectors.toCollection(HashSet::new));
        this.interestItems = compound.getList("Bukkit.InterestList", 8).stream().map(Tag::getAsString).map(ResourceLocation::tryParse).map(BuiltInRegistries.ITEM::get).collect(Collectors.toCollection(HashSet::new));
    }

    @Redirect(method = "holdInOffHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean banner$customBarter(ItemStack instance, Item item) {
        return instance.is(PiglinAi.BARTERING_ITEM) || allowedBarterItems.contains(item);
    }

    @Redirect(method = "canReplaceCurrentItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isLovedItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean banner$customLoved(ItemStack stack) {
        return stack.is(ItemTags.PIGLIN_LOVED) || interestItems.contains(stack.getItem()) || allowedBarterItems.contains(stack.getItem());
    }

    @Override
    public Set<Item> bridge$allowedBarterItems() {
        return allowedBarterItems;
    }

    @Override
    public Set<Item> bridge$interestItems() {
        return interestItems;
    }
}
