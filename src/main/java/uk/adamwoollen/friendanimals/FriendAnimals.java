package uk.adamwoollen.friendanimals;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendAnimals extends JavaPlugin
{
    public static Plugin plugin;

    @Override
    public void onEnable()
    {
        plugin = this;
        getServer().getPluginManager().registerEvents(new MyListener(), this);
    }

    @Override
    public void onDisable()
    {
    }
}
