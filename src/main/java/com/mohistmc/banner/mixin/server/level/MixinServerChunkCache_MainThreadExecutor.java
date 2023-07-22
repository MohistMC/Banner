package com.mohistmc.banner.mixin.server.level;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.server.level.ServerChunkCache$MainThreadExecutor")
public abstract class MixinServerChunkCache_MainThreadExecutor extends BlockableEventLoop<Runnable> {

    // @formatter:off
    @Shadow(aliases = {"field_18810"}, remap = false) @Final private ServerChunkCache outer;
    // @formatter:on

    protected MixinServerChunkCache_MainThreadExecutor(String name) {
        super(name);
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public boolean pollTask() {
        try {
            if (outer.runDistanceManagerUpdates()) {
                return true;
            } else {
                outer.getLightEngine().tryScheduleUpdate();
                return super.pollTask();
            }
        } finally {
            outer.chunkMap.bridge$callbackExecutor().run();
            BukkitExtraConstants.getServer().bridge$drainQueuedTasks();
        }
    }
}