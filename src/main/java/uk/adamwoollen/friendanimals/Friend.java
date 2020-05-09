package uk.adamwoollen.friendanimals;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Friend {
    private static List<String> names = new ArrayList<>(Arrays.asList("Anne", "Bernard", "Clare", "Derek", "Edward", "Fred", "Gavin", "Hannah", "Ima", "Jamie", "Kelly", "Lola", "Mark", "Nathan", "Olivia"));
    private enum interests {
        FISHING, MINING
    }
    private static final List<interests> interestsValues = List.of(interests.values());
    private enum Talking {
        SILENT, TALKING, STOPPING
    }

    private UUID entityId;
    private String name;
    private interests interest;
    private Map<UUID, Mission> missions;
    private Map<UUID, Integer> favour;

    // Used to prevent initiating a new interaction when one is already in progress
    private Set<UUID> interactingWith;
    private Talking talking;

    public Friend(UUID entityId) {
        this.entityId = entityId;
        name = names.remove(ThreadLocalRandom.current().nextInt(names.size()));
        interest = interestsValues.get(ThreadLocalRandom.current().nextInt(interestsValues.size()));
        missions = new HashMap<>();
        favour = new HashMap<>();
        interactingWith = new HashSet<>();
        talking = Talking.SILENT;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public boolean hasMetPlayer(UUID player) {
        return favour.containsKey(player);
    }

    public Integer getFavour(UUID player) {
        if (favour.containsKey(player)) return favour.get(player);
        return 0;
    }

    private void greetPlayer(Player player) {
        talk(player, new String[]{
                "Hello, I'm " + getName() + ".",
                "Who are you?",
                "Oh, " + player.getDisplayName() + ", nice to meet you!"
        }, true);
    }

    private void offerMissionToPlayer(Player player) {
        Mission mission = new Mission(this);
        missions.put(player.getUniqueId(), mission);
        talk(player, new String[]{
                "Hey, " +  player.getDisplayName() + "!",
                "This " + Utils.formatItemName(mission.getItem()) + " was delivered to me but, it's addressed to " + ChatColor.AQUA + mission.getTargetFriend().getName(),
                "Please could you deliver it to them for me?  I'd really appreciate it!"
        }, true);
        MyListener.lockIntoConversation(player.getUniqueId(), this);
    }

    public void onPlayerLeaveConversation(Player player) {
        if (talking == Talking.TALKING) talking = Talking.STOPPING;

        interactingWith.add(player.getUniqueId());
        new BukkitRunnable() {
            public void run() {
                if (talking == Talking.SILENT) {
                    talk(player, new String[]{"Goodbye!"}, true);
                    cancel();
                }
            }
        }.runTaskTimer(FriendAnimals.plugin, 20, 2);

        if (missions.containsKey(player.getUniqueId())) {
            Mission mission = missions.get(player.getUniqueId());
            if (mission.progression == 0) {
                missions.remove(player.getUniqueId());
            }
        }
    }

    public void playerInteract(Player player) {
        // Stop multiple dialogs being sent to the player in parallel.
        // Friend can still talk to multiple people at once.
        if (interactingWith.contains(player.getUniqueId())) return;
        interactingWith.add(player.getUniqueId());

        if (!hasMetPlayer(player.getUniqueId()))
        {
            greetPlayer(player);
            favour.put(player.getUniqueId(), 0);
        } else
        {
            if (missions.containsKey(player.getUniqueId()) && missions.get(player.getUniqueId()).progression < 2){
                Mission mission = missions.get(player.getUniqueId());
                // Mission not yet accepted
                if (mission.progression == 0){
                    talk(player, new String[]{
                            "Well, can you do it?",
                            "It would make my day."
                    }, true);
                } else if (mission.progression == 1){
                    talk(player, new String[]{
                            "Did you deliver the " + Utils.formatItemName(mission.getItem()) + " to " + ChatColor.AQUA + mission.getTargetFriend().getName() + ChatColor.WHITE + "?"
                    }, false);
                    MyListener.lockIntoConversation(player.getUniqueId(), this);
                }
            } else{
                if (ThreadLocalRandom.current().nextBoolean()) {
                    offerMissionToPlayer(player);
                } else {
                    talkAboutWeather(player);
                }
            }
        }
    }

    private void talkAboutWeather(Player player)
    {
        Utils.WeatherTime weatherTime = Utils.getWeatherTime(Bukkit.getEntity(entityId).getLocation());
        switch (weatherTime) {
            case SUNNY_DAY -> talk(player, new String[]{
                    "Sun's shining!  It's a wonderful day!"
            }, true);
            case RAINY_DAY -> talk(player, new String[]{
                    "Raining again...  I thought we might get some sun.",
                    "At least the flowers will be happy!"
            }, true);
            case SNOWY_DAY -> talk(player, new String[]{
                    "A perfect day to build a snowman.",
                    "I'd like to but, I'm not really built for it."
            }, true);
            case CLEAR_NIGHT -> talk(player, new String[]{
                    "It's so clear out tonight.  The perfect weather for star gazing.",
                    "My father got me a book with all the constellations in."
            }, true);
            case RAINY_NIGHT -> talk(player, new String[]{
                    "As miserable as it is, there's a somewhat enchanting eeriness to the cold and dark"
            }, true);
            case SNOWY_NIGHT -> talk(player, new String[]{
                    "Be careful not to slip outside.",
                    "I wouldn't want you to get hurt."
            }, true);
        }
    }

//    public void talk(Player player, String[] lines) {
//        talk(player, lines, false);
//    }

    public void talk(Player player, String[] lines, boolean finishInteraction) {
        if (lines != null && lines.length > 0) {
            talking = Talking.TALKING;
            new BukkitRunnable() {
                private int count = 0;

                public void run() {
                    if (count >= lines.length || talking == Talking.STOPPING) {
                        talking = Talking.SILENT;
                        if (finishInteraction) interactingWith.remove(player.getUniqueId());
                        cancel();
                        return;
                    }
                    player.sendMessage(ChatColor.GREEN + "<" + getName() + "> " + ChatColor.WHITE + lines[count]);
                    count++;
                }
            }.runTaskTimer(FriendAnimals.plugin, 0, 40);
        }
    }

    public void listen(Player player, String message) {
        FriendAnimals.plugin.getLogger().info("listening. missions: " + missions.size());
        if (missions.containsKey(player.getUniqueId())) {
            FriendAnimals.plugin.getLogger().info("There is a mission for this player");
            Mission mission = missions.get(player.getUniqueId());
            // Mission not yet accepted
            if (mission.progression == 0) {
                FriendAnimals.plugin.getLogger().info("mission progression 0");
                if (message.equals("yes")) {
                    if (player.getInventory().addItem(new ItemStack(mission.getItem(), 1)).isEmpty()) {
                        talk(player, new String[]{
                                "That's great!  Here's the " + Utils.formatItemName(mission.getItem()) + ".",
                                "Let me know once you've delivered it."
                        }, true);
                        mission.progression = 1;
                    } else {
                        talk(player, new String[]{
                                "There's no room in your inventory for the " + Utils.formatItemName(mission.getItem()) + ".",
                                "You'll have to make some room and come back, or change your mind."
                        }, true);
                    }
                    MyListener.freeFromConversation(player.getUniqueId());
                } else if (message.equals("no")) {
                    talk(player, new String[]{
                            "That's a shame.  I'll have to sort it some other way."
                    }, true);
                    missions.remove(player.getUniqueId());
                    MyListener.freeFromConversation(player.getUniqueId());
                } else {
                    talk(player, new String[]{
                            "Is that a yes or a no?"
                    }, false);
                }
            } else if (mission.progression == 1) {
                FriendAnimals.plugin.getLogger().info("mission progression 1");
                if (message.equals("yes")) {
                    if (player.getInventory().addItem(new ItemStack(mission.getReward(), 1)).isEmpty()) {
                        talk(player, new String[]{
                                "Thank you so much!  Here, have my " + Utils.formatItemName(mission.getReward()) + ".",
                                "Hopefully you'll be able to do something nice with it."
                        }, true);
                        // Validation stage
                        mission.progression++;
                    } else {
                        talk(player, new String[]{
                                "There's no room in your inventory for the " + Utils.formatItemName(mission.getReward()) + ".",
                                "You'll have to make some room and come back, or pass it up."
                        }, true);
                    }
                    MyListener.freeFromConversation(player.getUniqueId());
                } else if (message.equals("no")) {
                    talk(player, new String[]{
                            "I do hope it's not important.",
                            "Perhaps you should deliver it sooner rather than later to be safe.",
                            "I wouldn't want " + ChatColor.AQUA + mission.getTargetFriend().getName() + ChatColor.WHITE + " to be in a bad situation because they're missing their " + Utils.formatItemName(mission.getItem()) + "."
                    }, true);
                    MyListener.freeFromConversation(player.getUniqueId());
                } else {
                    talk(player, new String[]{
                            "Is that a yes or a no?"
                    }, false);
                }
            }
        }
    }
}
