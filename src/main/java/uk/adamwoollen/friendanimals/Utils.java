package uk.adamwoollen.friendanimals;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Utils {
    public enum WeatherTime {
        SUNNY_DAY, RAINY_DAY, SNOWY_DAY,
        CLEAR_NIGHT, RAINY_NIGHT, SNOWY_NIGHT
    }

    public static WeatherTime getWeatherTime(Location location) {
        World world = location.getWorld();
        if (world.getTime() >= 1_000 && world.getTime() <= 13_000) {
            if (world.hasStorm()) {
                double temperature = world.getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                if (temperature < 0.15) {
                    return WeatherTime.SNOWY_DAY;
                } else if (temperature < 0.95) {
                    return WeatherTime.RAINY_DAY;
                } else {
                    return WeatherTime.SUNNY_DAY;
                }
            } else {
                return WeatherTime.SUNNY_DAY;
            }
        } else {
            double temperature = world.getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (temperature < 0.15) {
                return WeatherTime.SNOWY_NIGHT;
            } else if (temperature < 0.95) {
                return WeatherTime.RAINY_NIGHT;
            } else {
                return WeatherTime.CLEAR_NIGHT;
            }
        }
    }

    public static String formatItemName(Material item) {
        return ChatColor.AQUA + Arrays.stream(item.toString().toLowerCase().split("_")).map(word -> {
            String firstLetter = word.substring(0, 1);
            return firstLetter.toUpperCase() + word.substring(1);
        }).collect(Collectors.joining(" ")) + ChatColor.WHITE;
    }
}
