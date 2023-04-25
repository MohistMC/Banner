package com.mohistmc.banner.mixin.world.inventory;

import com.mohistmc.banner.injection.world.inventory.InjectionAbstractContainerMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractContainerMenu.class)
public class MixinAbstractContainerMenu implements InjectionAbstractContainerMenu {

    public boolean checkReachable = true;

    @Override
    public boolean bridge$checkReachable() {
        return checkReachable;
    }

    @Override
    public void banner$setCheckReachable(boolean checkReachable) {
        this.checkReachable = checkReachable;
    }
}
