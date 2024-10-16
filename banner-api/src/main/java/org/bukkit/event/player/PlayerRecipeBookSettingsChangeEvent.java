package org.bukkit.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerRecipeBookSettingsChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final RecipeBookType recipeBookType;
    private final boolean open;
    private final boolean filtering;

    public PlayerRecipeBookSettingsChangeEvent(@NotNull Player player, @NotNull RecipeBookType recipeBookType, boolean open, boolean filtering) {
        super(player);
        this.recipeBookType = recipeBookType;
        this.open = open;
        this.filtering = filtering;
    }

    @NotNull
    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean isFiltering() {
        return this.filtering;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static enum RecipeBookType {
        CRAFTING,
        FURNACE,
        BLAST_FURNACE,
        SMOKER;

        private RecipeBookType() {
        }
    }
}
