package com.mohistmc.banner.mixin.server.commands;

import com.mohistmc.banner.asm.annotation.TransformAccess;
import java.util.Collection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ReloadCommand.class)
public abstract class MixinReloadCommand {

    @Shadow
    public static Collection<String> discoverNewPacks(PackRepository packRepository, WorldData worldData, Collection<String> selectedIds) {
        return null;
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static void reload(MinecraftServer minecraftserver) {
        PackRepository resourcePackList = minecraftserver.getPackRepository();
        WorldData configuration = minecraftserver.getWorldData();
        Collection<String> collection = resourcePackList.getSelectedIds();
        Collection<String> collection2 = discoverNewPacks(resourcePackList, configuration, collection);
        minecraftserver.reloadResources(collection2);
    }
}
