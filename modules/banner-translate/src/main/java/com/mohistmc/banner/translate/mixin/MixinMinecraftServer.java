package com.mohistmc.banner.translate.mixin;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @ModifyConstant(method = "saveAllChunks", constant = @Constant(stringValue = "Saving chunks for level '{}'/{}"))
    private String bosom$localSaveChunk(String constant){
        return BannerMCStart.I18N.as("server.chunk.saving");
    }

    @ModifyConstant(method = "initializeKeyPair", constant = @Constant(stringValue = "Generating keypair"))
    private String bosom$localKeyPair(String constant){
        return BannerMCStart.I18N.as("server.key.pair");
    }

    @ModifyConstant(method = "saveAllChunks", constant = @Constant(stringValue = "ThreadedAnvilChunkStorage ({}): All chunks are saved"))
    private String bosom$localSaveAnvil(String constant){
        return BannerMCStart.I18N.as("server.chunk.saved");
    }

    @ModifyConstant(method = "saveAllChunks", constant = @Constant(stringValue = "ThreadedAnvilChunkStorage: All dimensions are saved"))
    private String bosom$localSaveAnvil0(String constant){
        return BannerMCStart.I18N.as("server.dimension.saved");
    }

    @ModifyConstant(method = "stopServer", constant = @Constant(stringValue = "Stopping server"))
    private String bosom$localStoppingServer(String constant){
        return BannerMCStart.I18N.as("server.stopping");
    }

    @ModifyConstant(method = "stopServer", constant = @Constant(stringValue = "Saving players"))
    private String bosom$localSavePlayer(String constant){
        return BannerMCStart.I18N.as("server.saving.player");
    }

    @ModifyConstant(method = "stopServer", constant = @Constant(stringValue = "Saving worlds"))
    private String bosom$localSaveWorld(String constant){
        return BannerMCStart.I18N.as("server.saving.world");
    }
}
