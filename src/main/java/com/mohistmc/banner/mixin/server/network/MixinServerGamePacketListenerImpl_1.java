package com.mohistmc.banner.mixin.server.network;

import com.mojang.datafixers.util.Pair;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public abstract class MixinServerGamePacketListenerImpl_1 {

    @Shadow(aliases = {"field_28963"}, remap = false)
    ServerGamePacketListenerImpl outerThis;

    @Shadow(aliases = {"field_28962"}, remap = false)
    Entity outerEntity;
    @Shadow(aliases = {"field_39991"}, remap = false)
    ServerLevel outerLevel;

    private void performInteraction(InteractionHand enumhand, ServerGamePacketListenerImpl.EntityInteraction playerconnection_a, PlayerInteractEntityEvent event) { // CraftBukkit
        ItemStack itemstack = outerThis.player.getItemInHand(enumhand);

        if (itemstack.isItemEnabled(outerLevel.enabledFeatures())) {
            ItemStack itemstack1 = itemstack.copy();
            // CraftBukkit start
            ItemStack itemInHand = outerThis.player.getItemInHand(enumhand);
            boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.LEAD && outerEntity instanceof Mob;
            Item origItem = outerThis.player.getInventory().getSelected() == null ? null : outerThis.player.getInventory().getSelected().getItem();

            outerThis.bridge$craftServer().getPluginManager().callEvent(event);

            // Entity in bucket - SPIGOT-4048 and SPIGOT-6859a
            if ((outerEntity instanceof Bucketable && outerEntity instanceof LivingEntity && origItem != null && origItem.asItem() == Items.WATER_BUCKET) && (event.isCancelled() || outerThis.player.getInventory().getSelected() == null || outerThis.player.getInventory().getSelected().getItem() != origItem)) {
                outerThis.send(new ClientboundAddEntityPacket(outerEntity));
                outerThis.player.containerMenu.sendAllDataToRemote();
            }

            if (triggerLeashUpdate && (event.isCancelled() || outerThis.player.getInventory().getSelected() == null || outerThis.player.getInventory().getSelected().getItem() != origItem)) {
                // Refresh the current leash state
                outerThis.send(new ClientboundSetEntityLinkPacket(outerEntity, ((Mob) outerEntity).getLeashHolder()));
            }

            if (event.isCancelled() || outerThis.player.getInventory().getSelected() == null || outerThis.player.getInventory().getSelected().getItem() != origItem) {
                // Refresh the current entity metadata
                outerEntity.getEntityData().refresh(outerThis.player);
                // SPIGOT-7136 - Allays
                if (outerEntity instanceof Allay) {
                    outerThis.send(new ClientboundSetEquipmentPacket(outerEntity.getId(), Arrays.stream(EquipmentSlot.values()).map((slot) -> Pair.of(slot, ((LivingEntity) outerEntity).getItemBySlot(slot).copy())).collect(Collectors.toList())));
                    outerThis.player.containerMenu.sendAllDataToRemote();
                }
            }

            if (event.isCancelled()) {
                return;
            }
            // CraftBukkit end
            InteractionResult enuminteractionresult = playerconnection_a.run(outerThis.player, outerEntity, enumhand);

            // CraftBukkit start
            if (!itemInHand.isEmpty() && itemInHand.getCount() <= -1) {
                outerThis.player.containerMenu.sendAllDataToRemote();
            }
            // CraftBukkit end

            if (enuminteractionresult.consumesAction()) {
                CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(outerThis.player, itemstack1, outerEntity);
                if (enuminteractionresult.shouldSwing()) {
                    outerThis.player.swing(enumhand, true);
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
        this.performInteraction(enumhand, Player::interactOn, new PlayerInteractEntityEvent(outerThis.getCraftPlayer(), outerEntity.getBukkitEntity(), (enumhand == InteractionHand.OFF_HAND) ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND)); // CraftBukkit
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void onInteraction(InteractionHand enumhand, Vec3 vec3d) {
        this.performInteraction(enumhand, (serverPlayer, entityx, interactionHand) -> {
            return entityx.interactAt(serverPlayer, vec3d, interactionHand);
        }, new PlayerInteractAtEntityEvent(outerThis.getCraftPlayer(), outerEntity.getBukkitEntity(), new org.bukkit.util.Vector(vec3d.x, vec3d.y, vec3d.z), (enumhand == InteractionHand.OFF_HAND) ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND)); // CraftBukkit
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void onAttack() {
        // CraftBukkit
        if (!(outerEntity instanceof ItemEntity) && !(outerEntity instanceof ExperienceOrb) && !(outerEntity instanceof AbstractArrow) && (outerEntity != outerThis.player || outerThis.player.isSpectator())) {
            ItemStack itemstack = outerThis.player.getItemInHand(InteractionHand.MAIN_HAND);

            if (itemstack.isItemEnabled(outerLevel.enabledFeatures())) {
                outerThis.player.attack(outerEntity);
                // CraftBukkit start
                if (!itemstack.isEmpty() && itemstack.getCount() <= -1) {
                    outerThis.player.containerMenu.sendAllDataToRemote();
                }
                // CraftBukkit end
            }
        } else {
            outerThis.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
            outerThis.bridge$logger().warn("Player {} tried to attack an invalid entity", outerThis.player.getName().getString());
        }
    }
}
