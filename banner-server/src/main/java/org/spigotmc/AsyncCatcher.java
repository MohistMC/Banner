package org.spigotmc;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.server.MinecraftServer;

public class AsyncCatcher
{

    public static boolean enabled = true;

    public static void catchOp(String reason)
    {
        if ( AsyncCatcher.enabled && Thread.currentThread() != BannerServer.getServer().serverThread )
        {
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
