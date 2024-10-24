package org.bukkit.craftbukkit.map;

import com.google.common.base.Preconditions;
import java.awt.Color;
import java.awt.Image;
import java.util.Arrays;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapFont.CharacterSprite;
import org.bukkit.map.MapPalette;

public class CraftMapCanvas implements MapCanvas {

    private final byte[] buffer = new byte[128 * 128];
    private final CraftMapView mapView;
    private byte[] base;
    private MapCursorCollection cursors = new MapCursorCollection();

    protected CraftMapCanvas(CraftMapView mapView) {
        this.mapView = mapView;
        Arrays.fill(this.buffer, (byte) -1);
    }

    @Override
    public CraftMapView getMapView() {
        return this.mapView;
    }

    @Override
    public MapCursorCollection getCursors() {
        return this.cursors;
    }

    @Override
    public void setCursors(MapCursorCollection cursors) {
        this.cursors = cursors;
    }

    @Override
    public void setPixelColor(int x, int y, Color color) {
        this.setPixel(x, y, (color == null) ? -1 : MapPalette.matchColor(color));
    }

    @Override
    public Color getPixelColor(int x, int y) {
        byte pixel = this.getPixel(x, y);
        if (pixel == -1) {
            return null;
        }

        return MapPalette.getColor(pixel);
    }

    @Override
    public Color getBasePixelColor(int x, int y) {
        return MapPalette.getColor(this.getBasePixel(x, y));
    }

    @Override
    public void setPixel(int x, int y, byte color) {
        if (x < 0 || y < 0 || x >= 128 || y >= 128)
            return;
        if (this.buffer[y * 128 + x] != color) {
            this.buffer[y * 128 + x] = color;
            this.mapView.worldMap.setColorsDirty(x, y);
        }
    }

    @Override
    public byte getPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= 128 || y >= 128)
            return 0;
        return this.buffer[y * 128 + x];
    }

    @Override
    public byte getBasePixel(int x, int y) {
        if (x < 0 || y < 0 || x >= 128 || y >= 128)
            return 0;
        return this.base[y * 128 + x];
    }

    protected void setBase(byte[] base) {
        this.base = base;
    }

    protected byte[] getBuffer() {
        return this.buffer;
    }

    @Override
    public void drawImage(int x, int y, Image image) {
        byte[] bytes = MapPalette.imageToBytes(image);
        for (int x2 = 0; x2 < image.getWidth(null); ++x2) {
            for (int y2 = 0; y2 < image.getHeight(null); ++y2) {
                this.setPixel(x + x2, y + y2, bytes[y2 * image.getWidth(null) + x2]);
            }
        }
    }

    @Override
    public void drawText(int x, int y, MapFont font, String text) {
        int xStart = x;
        byte color = MapPalette.DARK_GRAY;
        Preconditions.checkArgument(font.isValid(text), "text (%s) contains invalid characters", text);

        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                x = xStart;
                y += font.getHeight() + 1;
                continue;
            } else if (ch == '\u00A7') {
                int j = text.indexOf(';', i);
                Preconditions.checkArgument(j >= 0, "text (%s) unterminated color string", text);
                try {
                    color = Byte.parseByte(text.substring(i + 1, j));
                    i = j;
                    continue;
                } catch (NumberFormatException ex) {
                }
            }

            CharacterSprite sprite = font.getChar(text.charAt(i));
            for (int r = 0; r < font.getHeight(); ++r) {
                for (int c = 0; c < sprite.getWidth(); ++c) {
                    if (sprite.get(r, c)) {
                        this.setPixel(x + c, y + r, color);
                    }
                }
            }
            x += sprite.getWidth() + 1;
        }
    }

}
