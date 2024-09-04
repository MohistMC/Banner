package com.mohistmc.banner.config;

import com.google.common.base.Throwables;
import com.mohistmc.banner.api.color.ColorsAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class BannerConfig {

    private static File CONFIG_FILE;

    private static final String HEADER = "This is the main configuration file for Banner.\n"
            + "As you can see, there's tons to configure. Some options may impact gameplay, so use\n"
            + "with caution, and make sure you know what each option does before configuring.\n";
    /*========================================================================*/
    public static YamlConfiguration config;
    static int version;
    /*========================================================================*/

    public static void init(File configFile)
    {
        CONFIG_FILE = configFile;
        config = new YamlConfiguration();
        try
        {
            config.load( CONFIG_FILE );
        } catch ( IOException ex )
        {
        } catch ( InvalidConfigurationException ex )
        {
            Bukkit.getLogger().log( Level.SEVERE, "Could not load banner.yml, please correct your syntax errors", ex );
            throw Throwables.propagate( ex );
        }

        config.options().header( HEADER );
        config.options().copyDefaults( true );

        version = getInt( "config-version", 12 );
        set( "config-version", 12 );
        readConfig( BannerConfig.class, null );
    }

    static void readConfig(Class<?> clazz, Object instance)
    {
        for ( Method method : clazz.getDeclaredMethods() )
        {
            if ( Modifier.isPrivate( method.getModifiers() ) )
            {
                if ( method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE )
                {
                    try
                    {
                        method.setAccessible( true );
                        method.invoke( instance );
                    } catch ( InvocationTargetException ex )
                    {
                        throw Throwables.propagate( ex.getCause() );
                    } catch ( Exception ex )
                    {
                        Bukkit.getLogger().log( Level.SEVERE, "Error invoking " + method, ex );
                    }
                }
            }
        }

        try
        {
            config.save( CONFIG_FILE );
        } catch ( IOException ex )
        {
            Bukkit.getLogger().log( Level.SEVERE, "Could not save " + CONFIG_FILE, ex );
        }
    }

    private static void set(String path, Object val)
    {
        config.set( path, val );
    }

    private static boolean getBoolean(String path, boolean def)
    {
        config.addDefault( path, def );
        return config.getBoolean( path, config.getBoolean( path ) );
    }

    private static int getInt(String path, int def)
    {
        config.addDefault( path, def );
        return config.getInt( path, config.getInt( path ) );
    }

    private static <T> List getList(String path, T def)
    {
        config.addDefault( path, def );
        return (List<T>) config.getList( path, config.getList( path ) );
    }

    private static String getString(String path, String def)
    {
        config.addDefault( path, def );
        return config.getString( path, config.getString( path ) );
    }

    private static double getDouble(String path, double def)
    {
        config.addDefault( path, def );
        return config.getDouble( path, config.getDouble( path ) );
    }

    public static int maximumRepairCost;
    public static boolean enchantment_fix;
    public static int max_enchantment_level;
    public static boolean check_update;
    public static boolean check_libraries;
    public static String lang;
    public static boolean showLogo;
    public static boolean stackdeobf;
    public static boolean isSymlinkWorld;
    public static boolean skipOtherWorldPreparing;
    // Thread Priority
    public static int server_thread;

    public static List<String> nospawnEntity;
    public static boolean clear_item;
    public static List<String> clear_item__whitelist;
    public static String clear_item__msg;
    public static int clear_item__time;
    public static String motdFirstLine;
    public static String motdSecondLine;

    public static boolean velocityEnabled;
    public static String velocitySecret;
    public static List<String> banned_entities;
    public static List<String> banned_breakable_entities;
    public static boolean banned_tnt;

    private static void banner() {
        check_update = getBoolean("banner.check_update", false);
        check_libraries = getBoolean("banner.check_libraries", true);
        lang = getString("banner.lang", "xx_XX");
        showLogo = getBoolean("banner.show_logo", true);
        stackdeobf = getBoolean("banner.stackdeobf", true);
        maximumRepairCost = getInt("anvilfix.maximumrepaircost", 40);
        enchantment_fix = getBoolean("anvilfix.enchantment_fix", false);
        max_enchantment_level = getInt("anvilfix.max_enchantment_level", 32767);
        isSymlinkWorld = getBoolean("compat.symlink_world", false);
        skipOtherWorldPreparing = getBoolean("compat.skipOtherWorldPreparing", true);
        server_thread = getInt("threadpriority.server_thread", 8);
        nospawnEntity = getList("entity.nospawn", Collections.emptyList());
        clear_item = getBoolean("entity.clear.item.enable", false);
        clear_item__whitelist = getList("entity.clear.item.whitelist", Collections.emptyList());
        banned_entities = getList("entity.banned_entities", Collections.emptyList());
        banned_entities = getList("entity.banned_breakable_entities", Collections.emptyList());
        clear_item__msg = getString("entity.clear.item.msg", "[Server] Cleaned up %size% drops");
        clear_item__time = getInt("entity.clear.item.time", 1800);
        motdFirstLine = ColorsAPI.of(getString("motd.firstline", "<RAINBOW1>A Minecraft Server</RAINBOW>"));
        motdFirstLine = ColorsAPI.of(getString("motd.secondline", ""));
        velocityEnabled = getBoolean("proxies.velocity.enabled", false);
        velocitySecret = getString("proxies.velocity.secret", "");
        banned_tnt = getBoolean("tnt.banned_tnt", false);
    }
}
