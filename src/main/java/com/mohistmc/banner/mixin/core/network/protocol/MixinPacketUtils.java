package com.mohistmc.banner.mixin.core.network.protocol;

import static com.mohistmc.banner.BannerServer.LOGGER;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PacketUtils.class)
public class MixinPacketUtils {

    /**
     * @author wdog5
     * @reason bukkit reason
     */
    /*
    @Overwrite
    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T processor, BlockableEventLoop<?> executor) throws RunningOnDifferentThreadException {
        if (!executor.isSameThread()) {
            executor.executeIfPossible(() -> {
                if (BukkitExtraConstants.getServer().hasStopped() || (processor instanceof ServerCommonPacketListenerImpl && ((ServerCommonPacketListenerImpl) processor).bridge$processedDisconnect())) return; // CraftBukkit, MC-142590
                if (processor.isAcceptingMessages()) {
                    try {
                        packet.handle(processor);
                    } catch (Exception var3) {
                        if (var3 instanceof ReportedException reportedexception) {
                            if (reportedexception.getCause() instanceof OutOfMemoryError) {
                                throw var3;
                            }
                        }
                        if (processor.isAcceptingMessages()) {
                            if (var3 instanceof ReportedException r) {
                                processor.fillCrashReport(r.getReport());
                                throw var3;
                            } else {
                                CrashReport crashreport = CrashReport.forThrowable(var3, "Main thread packet handler");
                                processor.fillCrashReport(crashreport);
                                throw new ReportedException(crashreport);
                            }
                        }

                        LOGGER.error("Failed to handle packet {}, suppressing error", packet, var3);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
                }

            });
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
            // CraftBukkit start - SPIGOT-5477, MC-142590
        } else if (BukkitExtraConstants.getServer().hasStopped() || (processor instanceof ServerCommonPacketListenerImpl && ((ServerCommonPacketListenerImpl) processor).bridge$processedDisconnect())) {
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
            // CraftBukkit end
        }
    }*/
}
