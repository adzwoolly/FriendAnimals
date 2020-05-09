package uk.adamwoollen.friendanimals;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MyListener implements Listener
{
    private final static Set<EntityType> friendlyAnimals = Set.of(EntityType.COW, EntityType.PIG, EntityType.SHEEP);

    private final static Map<UUID, Friend> friends = new HashMap<>();

    // Whether the player's chat is directed to the friend
    private final static Map<UUID, Friend> playerFriendConvoLock = new HashMap<>();
    // Whether the friend can move
    private final static Map<UUID, Friend> playerFriendMovementLock = new HashMap<>();

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(event.getHand().equals(EquipmentSlot.HAND))
        {
            if (event.getRightClicked() instanceof LivingEntity animal)
            {
                Player player = event.getPlayer();
                Friend friend;

                if (friends.containsKey(animal.getUniqueId())) {
                    friend = friends.get(animal.getUniqueId());
                } else {
                    if (!friendlyAnimals.contains(animal.getType())) return;

                    friend = new Friend(animal.getUniqueId());
                    friends.put(animal.getUniqueId(), friend);

                    animal.setCustomName(friend.getName());
                }

                friend.playerInteract(player);
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        FriendAnimals.plugin.getLogger().info(player.getDisplayName() + " sent a message");
        if (playerFriendConvoLock.containsKey(player.getUniqueId())) {
            FriendAnimals.plugin.getLogger().info(player.getDisplayName() + " locked in convo");

            Friend friend = playerFriendConvoLock.get(event.getPlayer().getUniqueId());

            event.setFormat(ChatColor.GRAY + "%s to " + friend.getName() + ": " + ChatColor.WHITE + "%s");
            event.getRecipients().clear();
            event.getRecipients().add(player);

            friend.listen(player, event.getMessage());
        }
    }

    // If the player moves too far away cancel conversation
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Friend friend = null;
        if (playerFriendConvoLock.containsKey(player.getUniqueId())) {
            friend = playerFriendConvoLock.get(player.getUniqueId());
        } else if (playerFriendMovementLock.containsKey(player.getUniqueId())) {
            friend = playerFriendMovementLock.get(player.getUniqueId());
        }
        if (friend != null) {
            double distanceBetweenPlayerAndFriend = event.getTo().distance(Bukkit.getEntity(friend.getEntityId()).getLocation());
            if (distanceBetweenPlayerAndFriend > 7) {
                freeFromConversation(player.getUniqueId());
                playerFriendMovementLock.remove(player.getUniqueId());
                defrostEntity(Bukkit.getEntity(friend.getEntityId()));
                friend.onPlayerLeaveConversation(player);
            }
        }
    }

    public static Friend getRandomFriend() {
        Object[] friendsArr = friends.values().toArray();
        return (Friend) friendsArr[ThreadLocalRandom.current().nextInt(friendsArr.length)];
    }

    public static void lockIntoConversation(UUID player, Friend friend)
    {
        playerFriendConvoLock.put(player, friend);
        playerFriendMovementLock.put(player, friend);
        freezeEntity(Bukkit.getEntity(friend.getEntityId()));
    }

    public static void freeFromConversation(UUID player)
    {
        Friend friend = playerFriendConvoLock.remove(player);
    }

    private static void freezeEntity(Entity entity)
    {
        setEntityAIEnabled(entity, false);
    }

    private static void defrostEntity(Entity entity)
    {
        setEntityAIEnabled(entity, true);
    }

    private static void setEntityAIEnabled(Entity entity, boolean enabled)
    {
        byte enabledByte = (byte) (enabled ? 0 : 1);

        CraftEntity craftEntity = (CraftEntity) entity;
        NBTTagCompound compound = new NBTTagCompound();
        craftEntity.getHandle().c(compound);
        compound.setByte("NoAI", enabledByte);
        craftEntity.getHandle().f(compound);
    }
}
