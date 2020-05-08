package uk.adamwoollen.friendanimals;

import org.bukkit.plugin.java.JavaPlugin;

public final class FriendAnimals extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new MyListener(this), this);
    }

    @Override
    public void onDisable()
    {
    }
}
