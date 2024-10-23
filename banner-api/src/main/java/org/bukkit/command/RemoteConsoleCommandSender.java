package org.bukkit.command;

import java.net.SocketAddress;
import org.jetbrains.annotations.NotNull;

public interface RemoteConsoleCommandSender extends CommandSender {

    /**
     * Gets the socket address of this remote sender.
     *
     * @return the remote sender's address
     */
    @NotNull
    public SocketAddress getAddress();
}
