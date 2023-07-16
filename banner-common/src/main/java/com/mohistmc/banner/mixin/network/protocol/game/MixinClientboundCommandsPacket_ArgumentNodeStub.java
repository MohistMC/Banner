package com.mohistmc.banner.mixin.network.protocol.game;

import com.mojang.brigadier.arguments.ArgumentType;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundCommandsPacket$ArgumentNodeStub")
public class MixinClientboundCommandsPacket_ArgumentNodeStub {

    private static final int ARCLIGHT_WRAP_INDEX = -256;

    @Inject(method = "serializeCap(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo$Template;)V",
            cancellable = true, at = @At("HEAD"))
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void banner$wrapArgument(FriendlyByteBuf buf, ArgumentTypeInfo<A, T> type, ArgumentTypeInfo.Template<A> node, CallbackInfo ci) {
        if (!SpigotConfig.bungee) {
            return;
        }
        var key = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(type);
        if (key == null || key.getNamespace().equals("minecraft") || key.getNamespace().equals("brigadier")) {
            return;
        }
        ci.cancel();
        buf.writeVarInt(ARCLIGHT_WRAP_INDEX);
        //noinspection deprecation
        buf.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(type));
        var payload = new FriendlyByteBuf(Unpooled.buffer());
        type.serializeToNetwork((T) node, payload);
        buf.writeVarInt(payload.readableBytes());
        buf.writeBytes(payload);
    }
}
