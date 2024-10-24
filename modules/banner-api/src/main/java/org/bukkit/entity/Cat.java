package org.bukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Locale;
import org.bukkit.DyeColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.util.OldEnum;
import org.jetbrains.annotations.NotNull;

/**
 * Meow.
 */
public interface Cat extends Tameable, Sittable {

    /**
     * Gets the current type of this cat.
     *
     * @return Type of the cat.
     */
    @NotNull
    public Type getCatType();

    /**
     * Sets the current type of this cat.
     *
     * @param type New type of this cat.
     */
    public void setCatType(@NotNull Type type);

    /**
     * Get the collar color of this cat
     *
     * @return the color of the collar
     */
    @NotNull
    public DyeColor getCollarColor();

    /**
     * Set the collar color of this cat
     *
     * @param color the color to apply
     */
    public void setCollarColor(@NotNull DyeColor color);

    /**
     * Represents the various different cat types there are.
     */
    interface Type extends OldEnum<Type>, Keyed {

        Type TABBY = getType("tabby");
        Type BLACK = getType("black");
        Type RED = getType("red");
        Type SIAMESE = getType("siamese");
        Type BRITISH_SHORTHAIR = getType("british_shorthair");
        Type CALICO = getType("calico");
        Type PERSIAN = getType("persian");
        Type RAGDOLL = getType("ragdoll");
        Type WHITE = getType("white");
        Type JELLIE = getType("jellie");
        Type ALL_BLACK = getType("all_black");

        @NotNull
        private static Type getType(@NotNull String key) {
            return Registry.CAT_VARIANT.getOrThrow(NamespacedKey.minecraft(key));
        }

        /**
         * @param name of the cat type.
         * @return the cat type with the given name.
         * @deprecated only for backwards compatibility, use {@link Registry#get(NamespacedKey)} instead.
         */
        @NotNull
        @Deprecated(since = "1.21")
        static Type valueOf(@NotNull String name) {
            Type type = Registry.CAT_VARIANT.get(NamespacedKey.fromString(name.toLowerCase(Locale.ROOT)));
            Preconditions.checkArgument(type != null, "No cat type found with the name %s", name);
            return type;
        }

        /**
         * @return an array of all known cat types.
         * @deprecated use {@link Registry#iterator()}.
         */
        @NotNull
        @Deprecated(since = "1.21")
        static Type[] values() {
            return Lists.newArrayList(Registry.CAT_VARIANT).toArray(new Type[0]);
        }
    }
}
