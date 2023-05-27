package com.mohistmc.banner.mixin.i18n;

import com.mohistmc.banner.BannerMCStart;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @ModifyConstant(method = "saveAllChunks", constant = @Constant(stringValue = "Saving chunks for level '{}'/{}"))
    private String localSaveChunk(String constant){
        return BannerMCStart.I18N.get("server.chunk.saving");
    }

    @ModifyConstant(method = "saveAllChunks", constant = @Constant(stringValue = "ThreadedAnvilChunkStorage ({}): All chunks are saved"))
    private String localSaveAnvil(String constant){
        return BannerMCStart.I18N.get("server.chunk.saved");
    }

    @ModifyConstant(method = "saveAllChunks", constant = @Constant(stringValue = "ThreadedAnvilChunkStorage: All dimensions are saved"))
    private String localSaveAnvil0(String constant){
        return BannerMCStart.I18N.get("server.dimension.saved");
    }

    @ModifyConstant(method = "stopServer", constant = @Constant(stringValue = "Stopping server"))
    private String localStoppingServer(String constant){
        return BannerMCStart.I18N.get("server.stopping");
    }

    @ModifyConstant(method = "stopServer", constant = @Constant(stringValue = "Saving players"))
    private String localSavePlayer(String constant){
        return BannerMCStart.I18N.get("server.saving.player");
    }

    @ModifyConstant(method = "stopServer", constant = @Constant(stringValue = "Saving worlds"))
    private String localSaveWorld(String constant){
        return BannerMCStart.I18N.get("server.saving.world");
    }
    @ModifyConstant(method = "stopServer", constant = @Constant(stringValue = "Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind"))
    private String localCKP(String constant){
        return BannerMCStart.I18N.get("server.hold.ckp");
    }

}
