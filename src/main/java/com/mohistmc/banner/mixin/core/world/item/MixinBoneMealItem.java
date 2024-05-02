package com.mohistmc.banner.mixin.core.world.item;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BoneMealItem.class)
public abstract class MixinBoneMealItem{

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static InteractionResult applyBonemeal(UseOnContext context) {
        return Items.BONE_MEAL.useOn(context);
    }
}
