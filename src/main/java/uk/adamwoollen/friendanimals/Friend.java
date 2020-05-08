package uk.adamwoollen.friendanimals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Friend
{
    private static List<String> names = new ArrayList<>(Arrays.asList("Anne", "Bernard", "Clare", "Derek", "Edward", "Fred", "Gavin", "Hannah", "Ima", "Jamie", "Kelly", "Lola", "Mark", "Nathan", "Olivia"));
    private enum interests {
        FISHING, MINING
    }
    private static final List<interests> interestsValues = List.of(interests.values());
    private static Random rnd = new Random();

    private String name;
    private interests interest;

    public Friend()
    {
        name = names.remove(rnd.nextInt(names.size()));
        interest = interestsValues.get(rnd.nextInt(interestsValues.size()));
    }

    public String getName()
    {
        return name;
    }
}
