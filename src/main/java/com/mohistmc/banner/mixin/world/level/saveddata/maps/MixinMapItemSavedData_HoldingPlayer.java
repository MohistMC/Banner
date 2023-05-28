package com.mohistmc.banner.mixin.world.level.saveddata.maps;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.craftbukkit.v1_19_R3.map.RenderData;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.map.MapCursor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(MapItemSavedData.HoldingPlayer.class)
public abstract class MixinMapItemSavedData_HoldingPlayer {
    
    // @formatter:off
    @SuppressWarnings("target") @Shadow(aliases = {"field_132"}, remap = false) private MapItemSavedData outerThis;
    @Shadow private boolean dirtyData;
    @Shadow private int minDirtyX;
    @Shadow private int minDirtyY;
    @Shadow private int maxDirtyX;
    @Shadow private int maxDirtyY;
    @Shadow private int tick;
    @Shadow @Final public Player player;
    @Shadow private boolean dirtyDecorations;

    @Shadow
    protected abstract MapItemSavedData.MapPatch createPatch();
    // @formatter:on

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    @Nullable
    public Packet<?> nextUpdatePacket(int i) {
        RenderData render =  outerThis.bridge$mapView().render(((ServerPlayer) this.player).getBukkitEntity()); // CraftBukkit        MapItemSavedData.MapPatch patch;
        MapItemSavedData.MapPatch patch;
        if (this.dirtyData) {
            this.dirtyData = false;
            var colors = outerThis.colors;
            outerThis.colors = render.buffer;
            patch = this.createPatch();
            outerThis.colors = colors;
        } else {
            patch = null;
        }

        Collection<MapDecoration> icons;
        if (this.tick++ % 5 == 0) {
            this.dirtyDecorations = false;
            icons = new ArrayList<>();
            for (MapCursor cursor : render.cursors) {
                if (cursor.isVisible()) {
                    icons.add(new MapDecoration(MapDecoration.Type.byIcon(cursor.getRawType()),
                            cursor.getX(), cursor.getY(), cursor.getDirection(), CraftChatMessage.fromStringOrNull(cursor.getCaption())));
                }
            }
        } else {
            icons = null;
        }
        return icons == null && patch == null ? null : new ClientboundMapItemDataPacket(i, outerThis.scale, outerThis.locked, icons, patch);
    }
}
