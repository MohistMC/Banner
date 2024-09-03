package com.mohistmc.banner.mixin.world.level.block.entity;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(SignBlockEntity.class)
public abstract class MixinSignBlockEntity extends BlockEntity implements CommandSource {

    @Shadow protected abstract SignText setMessages(Player player, List<FilteredText> list, SignText signText);

    @Shadow
    private static CommandSourceStack createCommandSourceStack(@Nullable Player player, Level level, BlockPos blockPos) {
        return null;
    }

    public MixinSignBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    private AtomicReference<Player> banner$player = new AtomicReference<>();
    private AtomicReference<BlockPos> banner$pos = new AtomicReference<>();

    @Inject(method = "executeClickCommandsIfPresent", at = @At("HEAD"))
    private void banner$getInfo(Player player, Level level, BlockPos blockPos, boolean bl,
                                CallbackInfoReturnable<Boolean> cir) {
        banner$player.set(player);
        banner$pos.set(blockPos);
    }

    @Inject(method = "markUpdated", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V"))
    public void banner$setColor(CallbackInfo ci) {
        if (this.level == null) {
            ci.cancel();
        }
    }

    private AtomicBoolean banner$front = new AtomicBoolean();
    private AtomicBoolean banner$bl = new AtomicBoolean();

    @Inject(method = "updateSignText", at = @At("HEAD"))
    private void banner$getBl(Player player, boolean bl, List<FilteredText> list, CallbackInfo ci) {
        banner$bl.set(bl);
    }

    @Redirect(method = "method_49845",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;setMessages(Lnet/minecraft/world/entity/player/Player;Ljava/util/List;Lnet/minecraft/world/level/block/entity/SignText;)Lnet/minecraft/world/level/block/entity/SignText;"))
    private SignText banner$resetMsg(SignBlockEntity instance, Player player, List<FilteredText> list, SignText signText) {
        return setMessages(player, list, signText, banner$bl.get());
    }

    @Redirect(method = "executeClickCommandsIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;createCommandSourceStack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/commands/CommandSourceStack;"))
    private CommandSourceStack arclight$setSource(Player player, Level level, BlockPos blockPos) {
        var stack = createCommandSourceStack(player, level, blockPos);
        stack.banner$setSource(this);
        return stack;
    }

    @Inject(method = "updateSignText", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false,
            shift = At.Shift.AFTER))
    private void banner$sendPacket(Player player, boolean bl, List<FilteredText> list, CallbackInfo ci) {
        ((ServerPlayer) player).connection.send(this.getUpdatePacket()); // CraftBukkit
    }

    private SignText setMessages(Player player, List<FilteredText> list, SignText signText, boolean front) {
        banner$front.set(front);
        return setMessages(player, list, signText);
    }

    @Inject(method = "setMessages", at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void banner$signChangeEvent(Player player, List<FilteredText> list, SignText signText,
                                        CallbackInfoReturnable<SignText> cir) {
        SignText originalText = signText; // CraftBukkit
        // CraftBukkit start
        org.bukkit.entity.Player cbPlayer = ((ServerPlayer) player).getBukkitEntity();
        String[] lines = new String[4];
        for (int i = 0; i < list.size(); ++i) {
            lines[i] = CraftChatMessage.fromComponent(signText.getMessage(i, player.isTextFilteringEnabled()));
        }

        SignChangeEvent event = new SignChangeEvent(CraftBlock.at(this.level, this.worldPosition), cbPlayer, Arrays.copyOf(lines, lines.length), (banner$front.get()) ? Side.FRONT : Side.BACK);
        player.level().getCraftServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(originalText);
        }
        // CraftBukkit end
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
        return wrapper.getEntity() != null ? wrapper.getEntity().banner$getBukkitSender(wrapper) : new CraftBlockCommandSender(wrapper, (BlockEntity) (Object) this);
    }

    @Override
    public CommandSender banner$getBukkitSender(CommandSourceStack wrapper) {
        return getBukkitSender(wrapper);
    }
}
