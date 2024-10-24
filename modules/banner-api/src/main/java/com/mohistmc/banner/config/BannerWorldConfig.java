package com.mohistmc.banner.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class BannerWorldConfig {

    private final String worldName;
    private final YamlConfiguration config;
    private boolean verbose;

    public BannerWorldConfig(String worldName)
    {
        this.worldName = worldName;
        this.config = BannerConfig.config;
        init();
    }

    public void init()
    {
        this.verbose = getBoolean( "verbose", false );

        log( "-------- World Settings For [" + worldName + "] --------" );
        BannerConfig.readConfig( BannerWorldConfig.class, this );
    }

    private void log(String s)
    {
        if ( verbose )
        {
            Bukkit.getLogger().info( s );
        }
    }

    private void set(String path, Object val)
    {
        config.set( "world-settings.default." + path, val );
    }

    private boolean getBoolean(String path, boolean def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getBoolean( "world-settings." + worldName + "." + path, config.getBoolean( "world-settings.default." + path ) );
    }

    private double getDouble(String path, double def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getDouble( "world-settings." + worldName + "." + path, config.getDouble( "world-settings.default." + path ) );
    }

    private int getInt(String path)
    {
        return config.getInt( "world-settings." + worldName + "." + path );
    }

    private int getInt(String path, int def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getInt( "world-settings." + worldName + "." + path, config.getInt( "world-settings.default." + path ) );
    }

    private <T> List getList(String path, T def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return (List<T>) config.getList( "world-settings." + worldName + "." + path, config.getList( "world-settings.default." + path ) );
    }

    private String getString(String path, String def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.getString( "world-settings." + worldName + "." + path, config.getString( "world-settings.default." + path ) );
    }

    private Object get(String path, Object def)
    {
        config.addDefault( "world-settings.default." + path, def );
        return config.get( "world-settings." + worldName + "." + path, config.get( "world-settings.default." + path ) );
    }

    public boolean disableEndCredits;

    private void endCredits() {
        disableEndCredits = getBoolean("game-mechanics.disable-end-credits", false);
        log("End credits disabled: " + disableEndCredits);
    }

    public boolean disableChestCatDetection;
    public boolean piglinsGuardChests;
    public boolean zombiesTargetTurtleEggs;

    private void animalBehaviours() {
        disableChestCatDetection = getBoolean("entities.behavior.disableChestCatDetection", false);
        piglinsGuardChests = getBoolean("entities.behavior.piglinsGuardChests", true);
        zombiesTargetTurtleEggs = getBoolean("entities.behavior.zombiesTargetTurtleEggs", true);
    }
}
