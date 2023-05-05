package com.destroystokyo.paper.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_19_R3.command.CraftConsoleCommandSender;

public class TerminalConsoleCommandSender extends CraftConsoleCommandSender {

    private static final Logger LOGGER = LogManager.getRootLogger();

    @Override
    public void sendRawMessage(String message) {
        // TerminalConsoleAppender supports color codes directly in log messages
        LOGGER.info(message);
    }

}