package com.mohistmc.banner.mixin.network.protocol;

import com.mohistmc.banner.asm.annotation.CreateConstructor;
import com.mohistmc.banner.asm.annotation.ShadowConstructor;
import com.mohistmc.banner.injection.network.InjectionDiscardedPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DiscardedPayload.class)
public class MixinDiscardedPayload implements InjectionDiscardedPayload {

    @ShadowConstructor
    public void banner$constructor(ResourceLocation rl) {
        throw new RuntimeException();
    }

    @CreateConstructor
    public void banner$constructor(ResourceLocation rl, ByteBuf data) {
        this.banner$constructor(rl);
        this.data = data;
    }

    @Unique
    private ByteBuf data;

    @Override
    public void bridge$setData(ByteBuf data) {
        this.data = data;
    }

    @Override
    public ByteBuf bridge$getData() {
        return this.data;
    }
}
