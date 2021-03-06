package patrick.fuscoe.remindmelater.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;

/**
 * Data model for user profiles.
*/
public class UserProfile {

    private String id;
    private String displayName;
    private String[] subscriptions;
    private Map<String, String> reminderCategories;  // Category Name & Icon Name
    private int reminderHour;
    private int reminderMinute;
    private int hibernateLength;
    //private String[] friends;
    private String deviceToken;

    private ArrayList<Friend> friendArrayList;
    private Map<String, Object> friendListMap;


    public UserProfile() {

    }

    public UserProfile(String id, String displayName)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = new String[0];
        this.reminderCategories = new HashMap<>();
        this.reminderHour = MainActivity.DEFAULT_REMINDER_TIME_HOUR;
        this.reminderMinute = MainActivity.DEFAULT_REMINDER_TIME_MINUTE;
        this.hibernateLength = MainActivity.DEFAULT_HIBERNATE_LENGTH;
        //this.friends = new String[0];
        this.deviceToken = "";
    }

    public UserProfile(String id, String displayName, String[] subscriptions,
                       Map<String, String> reminderCategories, int reminderHour, int reminderMinute,
                       int hibernateLength, String deviceToken,
                       Map<String, Object> friendListMap)
    {
        this.id = id;
        this.displayName = displayName;
        this.subscriptions = subscriptions;
        this.reminderCategories = reminderCategories;
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
        this.hibernateLength = hibernateLength;
        //this.friends = friends;
        this.deviceToken = deviceToken;

        this.friendArrayList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : friendListMap.entrySet())
        {
            String friendId = entry.getKey();
            Map<String, Object> friendMap = (Map<String, Object>) entry.getValue();

            String friendNickname = (String) friendMap.get("friendDisplayName");
            Friend friend = new Friend(friendId, friendNickname);

            friendArrayList.add(friend);
        }

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

    public void addFriend(Friend friend)
    {
        friendArrayList.add(friend);
    }

    public void removeFriend(Friend friend)
    {
        friendArrayList.remove(friend);
    }

    /*
    public void addFriend(String friendId)
    {
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(friends));
        tempList.add(friendId);
        friends = tempList.toArray(new String[0]);
    }

    public void removeFriend(String friendId)
    {
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(friends));
        tempList.remove(friendId);
        friends = tempList.toArray(new String[0]);
    }
    */

    public void addReminderCategory(String categoryName, String categoryIconName)
    {
        reminderCategories.put(categoryName, categoryIconName);
    }

    public void removeReminderCategory(String categoryName)
    {
        reminderCategories.remove(categoryName);
    }

    /* Getters and Setters */
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

    public int getHibernateLength() {
        return hibernateLength;
    }

    public void setHibernateLength(int hibernateLength) {
        this.hibernateLength = hibernateLength;
    }

    /*
    public String[] getFriends() {
        return friends;
    }

    public void setFriends(String[] friends) {
        this.friends = friends;
    }
    */

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public ArrayList<Friend> getFriendArrayList() {
        return friendArrayList;
    }


}
