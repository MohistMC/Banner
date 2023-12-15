package org.bukkit.command;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public interface RemoteConsoleCommandSender extends CommandSender {

    /**
     * Gets the socket address of this remote sender.
     *
     * @return the remote sender's address
     */
    @NotNull
    @ApiStatus.Experimental
    public SocketAddress getAddress();
}