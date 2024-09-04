package com.mohistmc.banner.mixin.world.entity.decoration;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import com.mohistmc.banner.injection.world.entity.decoration.InjectionItemFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class MixinItemFrame extends HangingEntity implements InjectionItemFrame {

    protected MixinItemFrame(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract void onItemChanged(ItemStack item);

    @Shadow @Final
    private static EntityDataAccessor<ItemStack> DATA_ITEM;

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;dropItem(Lnet/minecraft/world/entity/Entity;Z)V"))
    private void banner$damageNonLiving(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((ItemFrame) (Object) this, source, amount, false) || this.isRemoved()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void setItem(ItemStack itemstack, final boolean flag, final boolean playSound) {
        if (!itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(1);
        }
        this.onItemChanged(itemstack);
        this.getEntityData().set(DATA_ITEM, itemstack);
        if (!itemstack.isEmpty() && playSound) {
            this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
        }
        if (flag && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static AABB calculateBoundingBoxStatic(BlockPos blockposition, Direction enumdirection) {
        // CraftBukkit end
        float f = 0.46875F;
        Vec3 vec3d = Vec3.atCenterOf(blockposition).relative(enumdirection, -0.46875D);
        Direction.Axis enumdirection_enumaxis = enumdirection.getAxis();
        double d0 = enumdirection_enumaxis == Direction.Axis.X ? 0.0625D : 0.75D;
        double d1 = enumdirection_enumaxis == Direction.Axis.Y ? 0.0625D : 0.75D;
        double d2 = enumdirection_enumaxis == Direction.Axis.Z ? 0.0625D : 0.75D;

        return AABB.ofSize(vec3d, d0, d1, d2);
    }
}
