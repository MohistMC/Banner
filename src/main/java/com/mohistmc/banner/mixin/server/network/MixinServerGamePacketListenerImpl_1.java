package com.mohistmc.banner.mixin.server.network;

import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public abstract class MixinServerGamePacketListenerImpl_1 {

    @Shadow @Final
    ServerGamePacketListenerImpl field_28963;

    @Shadow @Final
    Entity val$target;

    @Shadow @Final
    ServerLevel val$level;

    private void performInteraction(InteractionHand enumhand, ServerGamePacketListenerImpl.EntityInteraction playerconnection_a, PlayerInteractEntityEvent event) { // CraftBukkit
        ItemStack itemstack = field_28963.player.getItemInHand(enumhand);

        if (itemstack.isItemEnabled(val$level.enabledFeatures())) {
            ItemStack itemstack1 = itemstack.copy();
            // CraftBukkit start
            ItemStack itemInHand = field_28963.player.getItemInHand(enumhand);
            boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.LEAD && val$target instanceof Mob;
            Item origItem = field_28963.player.getInventory().getSelected() == null ? null : field_28963.player.getInventory().getSelected().getItem();

            field_28963.bridge$craftServer().getPluginManager().callEvent(event);

            // Entity in bucket - SPIGOT-4048 and SPIGOT-6859a
            if ((val$target instanceof Bucketable && val$target instanceof LivingEntity && origItem != null && origItem.asItem() == Items.WATER_BUCKET) && (event.isCancelled() || field_28963.player.getInventory().getSelected() == null || field_28963.player.getInventory().getSelected().getItem() != origItem)) {
                field_28963.send(new ClientboundAddEntityPacket(val$target));
                field_28963.player.containerMenu.sendAllDataToRemote();
            }

            if (triggerLeashUpdate && (event.isCancelled() || field_28963.player.getInventory().getSelected() == null || field_28963.player.getInventory().getSelected().getItem() != origItem)) {
                // Refresh the current leash state
                field_28963.send(new ClientboundSetEntityLinkPacket(val$target, ((Mob) val$target).getLeashHolder()));
            }

            if (event.isCancelled() || field_28963.player.getInventory().getSelected() == null || field_28963.player.getInventory().getSelected().getItem() != origItem) {
                // Refresh the current entity metadata
                val$target.getEntityData().refresh(field_28963.player);
                // SPIGOT-7136 - Allays
                if (val$target instanceof Allay) {
                    field_28963.send(new ClientboundSetEquipmentPacket(val$target.getId(), Arrays.stream(EquipmentSlot.values()).map((slot) -> Pair.of(slot, ((LivingEntity) val$target).getItemBySlot(slot).copy())).collect(Collectors.toList())));
                    field_28963.player.containerMenu.sendAllDataToRemote();
                }
            }

            if (event.isCancelled()) {
                return;
            }
            // CraftBukkit end
            InteractionResult enuminteractionresult = playerconnection_a.run(field_28963.player, val$target, enumhand);

            // CraftBukkit start
            if (!itemInHand.isEmpty() && itemInHand.getCount() <= -1) {
                field_28963.player.containerMenu.sendAllDataToRemote();
            }
            // CraftBukkit end

            if (enuminteractionresult.consumesAction()) {
                CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(field_28963.player, itemstack1, val$target);
                if (enuminteractionresult.shouldSwing()) {
                    field_28963.player.swing(enumhand, true);
                }
            }

        }
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void onInteraction(InteractionHand enumhand) {
        this.performInteraction(enumhand, Player::interactOn, new PlayerInteractEntityEvent(field_28963.getCraftPlayer(), val$target.getBukkitEntity(), (enumhand == InteractionHand.OFF_HAND) ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND)); // CraftBukkit
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void onInteraction(InteractionHand enumhand, Vec3 vec3d) {
        this.performInteraction(enumhand, (serverPlayer, entityx, interactionHand) -> {
            return entityx.interactAt(serverPlayer, vec3d, interactionHand);
        }, new PlayerInteractAtEntityEvent(field_28963.getCraftPlayer(), val$target.getBukkitEntity(), new org.bukkit.util.Vector(vec3d.x, vec3d.y, vec3d.z), (enumhand == InteractionHand.OFF_HAND) ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND)); // CraftBukkit
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void onAttack() {
        // CraftBukkit
        if (!(val$target instanceof ItemEntity) && !(val$target instanceof ExperienceOrb) && !(val$target instanceof AbstractArrow) && (val$target != field_28963.player || field_28963.player.isSpectator())) {
            ItemStack itemstack = field_28963.player.getItemInHand(InteractionHand.MAIN_HAND);

            if (itemstack.isItemEnabled(val$level.enabledFeatures())) {
                field_28963.player.attack(val$target);
                // CraftBukkit start
                if (!itemstack.isEmpty() && itemstack.getCount() <= -1) {
                    field_28963.player.containerMenu.sendAllDataToRemote();
                }
                // CraftBukkit end
            }
        } else {
            field_28963.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
            field_28963.bridge$logger().warn("Player {} tried to attack an invalid entity", field_28963.player.getName().getString());
        }
    }
}
