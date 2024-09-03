package com.mohistmc.banner.mixin.world.entity.decoration;

import com.mohistmc.banner.injection.world.entity.decoration.InjectionArmorStand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Banner TODO fixme
@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity implements InjectionArmorStand {

    @Shadow @Final private NonNullList<ItemStack> handItems;
    @Shadow @Final private NonNullList<ItemStack> armorItems;
    @Shadow private boolean invisible;
    public boolean canMove = true; // Paper

    protected MixinArmorStand(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public float getBukkitYaw() {
        return this.getYRot();
    }

    @Override
    public boolean shouldDropExperience() {
        return true;// MC-157395, SPIGOT-5193 even baby (small) armor stands should drop
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack, boolean silent) {
        this.verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND:
                this.onEquipItem(slot, (ItemStack)this.handItems.set(slot.getIndex(), stack), stack, silent);
                break;
            case HUMANOID_ARMOR:
                this.onEquipItem(slot, (ItemStack)this.armorItems.set(slot.getIndex(), stack), stack, silent);
        }
    }

    @Inject(method = "swapItem", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"))
    public void banner$manipulateEvent(net.minecraft.world.entity.player.Player playerEntity, net.minecraft.world.entity.EquipmentSlot slotType, ItemStack itemStack, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack1 = this.getItemBySlot(slotType);

        org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemStack1);
        org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemStack);

        org.bukkit.entity.Player player = (org.bukkit.entity.Player) playerEntity.getBukkitEntity();
        org.bukkit.entity.ArmorStand self = ((org.bukkit.entity.ArmorStand) ((ArmorStand) (Object) this).getBukkitEntity());

        org.bukkit.inventory.EquipmentSlot slot = CraftEquipmentSlot.getSlot(slotType);
        org.bukkit.inventory.EquipmentSlot bukkitHand = CraftEquipmentSlot.getHand(hand);
        PlayerArmorStandManipulateEvent event = new PlayerArmorStandManipulateEvent(player, self, playerHeldItem, armorStandItem, slot, bukkitHand);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    public void banner$damageDropOut(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, source, amount)) {
            cir.setReturnValue(false);
        }else {
            banner$callEntityDeath();
        }
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/tags/DamageTypeTags;IS_EXPLOSION:Lnet/minecraft/tags/TagKey;"))
    public void banner$damageNormal(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, source, amount, true, this.invisible)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;invisible:Z"))
    private boolean banner$softenCondition(net.minecraft.world.entity.decoration.ArmorStand entity) {
        return false;
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void banner$damageDeath1(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        banner$callEntityDeath();
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void banner$damageDeath2(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        banner$callEntityDeath();
    }

    @Inject(method = "causeDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;kill()V"))
    private void banner$deathEvent2(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfo ci) {
        banner$callEntityDeath();
    }

    @Redirect(method = "brokenByAnything", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ArmorStand;dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void banner$dropLater(ArmorStand instance, ServerLevel serverLevel, DamageSource damageSource) {
    }

    @Redirect(method = "brokenByAnything", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"))
    private void banner$captureDropsDeath(Level worldIn, BlockPos pos, ItemStack stack) {
        banner$addDrops(worldIn, stack);
    }

    @Inject(method = "brokenByAnything", at = @At("RETURN"))
    private void banner$spawnLast(ServerLevel serverLevel, DamageSource damageSource, CallbackInfo ci) {
        this.dropAllDeathLoot(serverLevel, damageSource);
    }

    @Inject(method = "kill", at = @At("HEAD"))
    private void banner$deathEvent(CallbackInfo ci) {
        banner$callEntityDeath();
    }

    private void banner$addDrops(Level worldIn, ItemStack stack) {
        if (!worldIn.isClientSide && !stack.isEmpty() && worldIn.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) { // Banner - prevents item dupe
            this.bridge$drops().add(CraftItemStack.asBukkitCopy(stack));
        }
    }

    private void banner$callEntityDeath() {
        CraftEventFactory.callEntityDeathEvent((net.minecraft.world.entity.decoration.ArmorStand) (Object) this, this.damageSources().genericKill(), this.bridge$drops());// CraftBukkit - call event
    }

    // Paper start
    @Override
    public void move(MoverType type, Vec3 pos) {
        if (this.canMove) {
            super.move(type, pos);
        }
    }
    // Paper end

    @Override
    public boolean bridge$canMove() {
        return canMove;
    }

    @Override
    public void banner$setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
}
