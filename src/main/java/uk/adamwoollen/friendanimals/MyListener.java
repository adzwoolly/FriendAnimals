package uk.adamwoollen.friendanimals;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MyListener implements Listener
{
    private final static Set friendlyAnimals = Set.of(EntityType.COW, EntityType.PIG, EntityType.SHEEP);

    private final Map<UUID, Friend> friends = new HashMap<>();
    private final Plugin plugin;

    public MyListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(event.getHand().equals(EquipmentSlot.HAND))
        {
            if (event.getRightClicked() instanceof LivingEntity) {
                Player player = event.getPlayer();
                LivingEntity animal = (LivingEntity) event.getRightClicked();
                Friend friend;

                if (friends.containsKey(animal.getUniqueId())) {
                    friend = friends.get(animal.getUniqueId());
                } else {
                    if (!friendlyAnimals.contains(animal.getType())) return;

                    friend = new Friend();
                    friends.put(animal.getUniqueId(), friend);

                    animal.setCustomName(friend.getName());
                }

                List<String> introMessages = new ArrayList<>();
                introMessages.add("Hello, I'm " + friend.getName() + ".");
                introMessages.add("Who are you?");
                introMessages.add("Oh, " + player.getDisplayName() + ", nice to meet you!");

                new BukkitRunnable() {
                    public void run() {
                        if (introMessages.size() <= 0) {
                            cancel();
                            return;
                        }
                        player.sendMessage(introMessages.remove(0));
                    }
                }.runTaskTimer(plugin, 0, 40);
            }
        }
    }
}
