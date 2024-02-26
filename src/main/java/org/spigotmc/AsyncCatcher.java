package org.spigotmc;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;

public class AsyncCatcher
{

    public static boolean enabled = true;

    public static void catchOp(String reason)
    {
        if ( (enabled || io.papermc.paper.util.TickThread.STRICT_THREAD_CHECKS) && Thread.currentThread() != BukkitExtraConstants.getServer().serverThread ) // Paper
        {
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }

    public static boolean catchAsync()
    {
        if ( enabled && Thread.currentThread() != BukkitExtraConstants.getServer().serverThread )
        {
            return true;
        }
        return false;
    }
}

