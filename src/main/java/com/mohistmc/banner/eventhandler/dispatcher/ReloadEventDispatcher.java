package com.mohistmc.banner.eventhandler.dispatcher;

import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.bukkit.Bukkit;

public class ReloadEventDispatcher {

    public static void dispatchReload() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {

            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(BannerServer.MOD_ID);
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                if (Bukkit.getServer() != null) {
                    BukkitExtraConstants.getServer().bridge$server().syncCommands();
                }
            }
        });
    }
}
