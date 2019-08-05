package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UserProfile {

    private String id;
    private String displayName;
    private String[] subscriptions;
    private HashMap<String, Integer> reminderCategories;


    public UserProfile() {

    }

    public UserProfile(String id, String displayName)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = new String[]{};
        this.reminderCategories = new HashMap<>();
    }

    public UserProfile(String id, String displayName, String[] subscriptions, HashMap<String, Integer> reminderCategories)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = subscriptions;
        this.reminderCategories = reminderCategories;
    }


    public void addSubscription(String groupId)
    {
        ArrayList<String> tempList = new ArrayList<String>(Arrays.asList(subscriptions));
        tempList.add(groupId);
        subscriptions = tempList.toArray(subscriptions);
    }

    public void removeSubscription(String groupId)
    {
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(subscriptions));
        tempList.remove(groupId);
        subscriptions = tempList.toArray(subscriptions);
    }

    /** Getters and Setters **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(String[] subscriptions) {
        this.subscriptions = subscriptions;
    }
}
