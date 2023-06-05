package org.spigotmc;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;

import java.util.Objects;

public class AsyncCatcher
{

    public static boolean enabled = false;

    public static void catchOp(String reason)
    {
        if ( enabled && Thread.currentThread() != Objects.requireNonNull(BukkitExtraConstants.getServer()).serverThread )
        {
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
