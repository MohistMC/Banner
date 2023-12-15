package com.mohistmc.banner.bukkit.pluginfix;

import org.bukkit.permissions.PermissibleBase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/11 23:38:43
 */
public class LuckPerms {

    public static Map<UUID, PermissibleBase> perCache = new HashMap<>();
}
