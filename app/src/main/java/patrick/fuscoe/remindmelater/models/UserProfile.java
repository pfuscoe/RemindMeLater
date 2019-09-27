package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;

public class UserProfile {

    private String id;
    private String displayName;
    private String[] subscriptions;
    private Map<String, String> reminderCategories;  // Category Name & Icon Name
    private int reminderHour;
    private int reminderMinute;


    public UserProfile() {

    }

    public UserProfile(String id, String displayName)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = new String[]{};
        this.reminderCategories = new HashMap<>();
        this.reminderHour = MainActivity.DEFAULT_REMINDER_TIME_HOUR;
        this.reminderMinute = MainActivity.DEFAULT_REMINDER_TIME_MINUTE;
    }

    public UserProfile(String id, String displayName, String[] subscriptions,
                       Map<String, String> reminderCategories, int reminderHour, int reminderMinute)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = subscriptions;
        this.reminderCategories = reminderCategories;
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
    }


    public void addSubscription(String groupId)
    {
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(subscriptions));
        tempList.add(groupId);
        subscriptions = tempList.toArray(new String[0]);
    }

    public void removeSubscription(String groupId)
    {
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(subscriptions));
        tempList.remove(groupId);
        subscriptions = tempList.toArray(new String[0]);
    }

    public void addReminderCategory(String categoryName, String categoryIconName)
    {
        reminderCategories.put(categoryName, categoryIconName);
    }

    public void removeReminderCategory(String categoryName)
    {
        reminderCategories.remove(categoryName);
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

    public Map<String, String> getReminderCategories() {
        return reminderCategories;
    }

    public int getReminderHour() {
        return reminderHour;
    }

    public void setReminderHour(int reminderHour) {
        this.reminderHour = reminderHour;
    }

    public int getReminderMinute() {
        return reminderMinute;
    }

    public void setReminderMinute(int reminderMinute) {
        this.reminderMinute = reminderMinute;
    }
}
