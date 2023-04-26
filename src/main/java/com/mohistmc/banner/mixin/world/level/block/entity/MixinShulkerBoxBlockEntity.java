package com.mohistmc.banner.mixin.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ShulkerBoxBlockEntity.class)
public class MixinShulkerBoxBlockEntity implements InjectionShulkerBoxBlockEntity {

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = 64;
    public boolean opened;

    @Override
    public List<HumanEntity> bridge$transaction() {
        return transaction;
    }

    @Override
    public void banner$setTransaction(List<HumanEntity> transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean bridge$opened() {
        return opened;
    }

    @Override
    public void banner$setOpened(boolean opened) {
        this.opened = opened;
    }
}
