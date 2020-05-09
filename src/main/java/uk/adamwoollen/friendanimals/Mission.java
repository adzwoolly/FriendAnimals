package uk.adamwoollen.friendanimals;

import org.bukkit.Material;

public class Mission
{
    private enum MissionType {
        DELIVERY,
        GIFT_REQUEST
    }

    private MissionType missionType;
    private Friend issuer;
    private Material item;
    private Friend targetFriend;
    private Material reward;
    int progression;

    public Mission(Friend friend)
    {
        missionType = MissionType.DELIVERY;
        issuer = friend;
        item = Material.INK_SAC;
        targetFriend = MyListener.getRandomFriend();
        reward = Material.ACACIA_BOAT;
        progression = 0;
    }

    public Material getItem()
    {
        return item;
    }

    public Friend getTargetFriend() {
        return targetFriend;
    }

    public Material getReward() {
        return reward;
    }
}
