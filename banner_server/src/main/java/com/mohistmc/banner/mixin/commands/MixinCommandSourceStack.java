package com.mohistmc.banner.mixin.commands;

import com.mohistmc.banner.injection.commands.InjectionCommandSourceStack;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(CommandSourceStack.class)
public abstract class MixinCommandSourceStack implements InjectionCommandSourceStack {

    @Shadow @Final private int permissionLevel;

    @Shadow public abstract ServerLevel getLevel();

    @Mutable
    @Shadow @Final public CommandSource source;
    public volatile CommandNode currentCommand; // CraftBukkit

    @Inject(method = "hasPermission", cancellable = true, at = @At("HEAD"))
    public void banner$checkPermission(int level, CallbackInfoReturnable<Boolean> cir) {
        CommandNode currentCommand =  this.currentCommand;
        if (currentCommand != null) {
            cir.setReturnValue(hasPermission(level, VanillaCommandWrapper.getPermission(currentCommand)));
        }
    }

    @Redirect(method = "broadcastToAdmins", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;isOp(Lcom/mojang/authlib/GameProfile;)Z"))
    private boolean banner$feedbackPermission(PlayerList instance, GameProfile profile) {
        return instance.getPlayer(profile.getId()).getBukkitEntity().hasPermission("minecraft.admin.command_feedback");
    }

    @Override
    public boolean hasPermission(int i, String bukkitPermission) {
        // World is null when loading functions
        return ((getLevel() == null || !((CraftServer) Bukkit.getServer()).ignoreVanillaPermissions) && this.permissionLevel >= i) || getBukkitSender().hasPermission(bukkitPermission);
    }

    public CommandSender getBukkitSender() {
        return this.source.banner$getBukkitSender((CommandSourceStack) (Object) this);
    }

    @Override
    public CommandSender banner$getBukkitSender() {
        return getBukkitSender();
    }

    @Override
    public CommandNode<?> bridge$getCurrentCommand() {
        return currentCommand;
    }

    @Override
    public void banner$setCurrentCommand(CommandNode<?> node) {
        this.currentCommand = node;
    }

    @Override
    public void banner$setSource(CommandSource source) {
        this.source = source;
    }
}
