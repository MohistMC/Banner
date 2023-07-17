package org.spigotmc;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;

public class AsyncCatcher
{

    public static boolean enabled = false;

    public static void catchOp(String reason)
    {
        if ( (AsyncCatcher.enabled || io.papermc.paper.util.TickThread.STRICT_THREAD_CHECKS) && Thread.currentThread() != BukkitExtraConstants.getServer().serverThread ) // Paper
        {
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
