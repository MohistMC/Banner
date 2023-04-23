package org.spigotmc;

import com.mohistmc.banner.util.ServerUtils;

import java.util.Objects;

public class AsyncCatcher
{

    public static boolean enabled = false;

    public static void catchOp(String reason)
    {
        if ( enabled && Thread.currentThread() != Objects.requireNonNull(ServerUtils.getServer()).serverThread )
        {
            throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
