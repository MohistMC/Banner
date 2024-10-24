package com.mohistmc.banner.mixin.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LecternBlockEntity.class)
public abstract class MixinLecternBlockEntity extends BlockEntity implements Clearable, MenuProvider, CommandSource {


    // @formatter:off
    @Shadow @Final public Container bookAccess;
    @Shadow @Final private ContainerData dataAccess;

    public MixinLecternBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }
    // @formatter:on

    @Redirect(method = "createCommandSourceStack", at = @At(value = "NEW", args = "class=net/minecraft/commands/CommandSourceStack"))
    private CommandSourceStack banner$source(CommandSource source, Vec3 vec3d, Vec2 vec2f, ServerLevel world, int i, String s, Component component, MinecraftServer server, @Nullable Entity entity) {
        return new CommandSourceStack(this, vec3d, vec2f, world, i, s, component, server, entity);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player entity) {
        LecternMenu container = new LecternMenu(i, this.bookAccess, this.dataAccess);
         container.bridge$setPlayerInventory(playerInventory);
        return container;
    }

    @Override
    public void sendSystemMessage(@NotNull Component component) {
    }

    @Override
    public boolean acceptsSuccess() {
        return false;
    }

    @Override
    public boolean acceptsFailure() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    public CommandSender getBukkitSender(CommandSourceStack wrapper) {
        return wrapper.getEntity() != null ?  wrapper.getEntity().banner$getBukkitSender(wrapper) : new CraftBlockCommandSender(wrapper, (BlockEntity) (Object) this);
    }

    @Override
    public CommandSender banner$getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitSender(wrapper);
    }

}
