package com.mohistmc.banner.mixin.world.entity.projectile;

import com.mohistmc.banner.injection.world.entity.projectile.InjectionThrowableItemProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThrowableItemProjectile.class)
public abstract class MixinThrowableItemProjectile extends ThrowableProjectile implements InjectionThrowableItemProjectile {

    @Shadow protected abstract Item getDefaultItem();

    protected MixinThrowableItemProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Item getDefaultItemPublic() {
        return getDefaultItem();
    }
}
