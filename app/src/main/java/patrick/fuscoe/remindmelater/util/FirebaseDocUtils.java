package patrick.fuscoe.remindmelater.util;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.ToDoItem;
import patrick.fuscoe.remindmelater.models.UserProfile;

public class FirebaseDocUtils {

    public static Map<String, Object> createUserProfileDoc(UserProfile userProfile)
    {
        Map<String, Object> userProfileDoc = new HashMap<>();

        userProfileDoc.put("displayName", userProfile.getDisplayName());
        userProfileDoc.put("subscriptions", Arrays.asList(userProfile.getSubscriptions()));
        userProfileDoc.put("reminderCategories", userProfile.getReminderCategories());
        userProfileDoc.put("reminderHour", userProfile.getReminderHour());
        userProfileDoc.put("reminderMinute", userProfile.getReminderMinute());
        userProfileDoc.put("hibernateLength", userProfile.getHibernateLength());
        userProfileDoc.put("friends", Arrays.asList(userProfile.getFriends()));

        return userProfileDoc;
    }

    public static UserProfile createUserProfileObj(DocumentSnapshot documentSnapshot)
    {
        Map<String, Object> docMap = documentSnapshot.getData();

        String id = documentSnapshot.getId();
        String displayName = documentSnapshot.getString("displayName");

        ArrayList<String> subscriptionsList = (ArrayList<String>) docMap.get("subscriptions");

        String[] subscriptions = new String[subscriptionsList.size()];
        subscriptions = subscriptionsList.toArray(subscriptions);

        Map<String, String> reminderCategories =
                (Map<String, String>) documentSnapshot.get("reminderCategories");

        int reminderTimeHour = Math.toIntExact((long) docMap.get("reminderHour"));
        int reminderTimeMinute = Math.toIntExact((long) docMap.get("reminderMinute"));

        int hibernateLength = Math.toIntExact((long) docMap.get("hibernateLength"));

        ArrayList<String> friendsList = (ArrayList<String>) docMap.get("friends");
        String[] friends;
        friends = friendsList.toArray(new String[0]);

        return new UserProfile(id, displayName, subscriptions, reminderCategories,
                reminderTimeHour, reminderTimeMinute, hibernateLength, friends);
    }

    public static Map<String, Object> createToDoGroupDoc(ToDoGroup toDoGroup)
    {
        Map<String, Object> toDoGroupDoc = new HashMap<>();

        toDoGroupDoc.put("title", toDoGroup.getTitle());
        toDoGroupDoc.put("iconName", toDoGroup.getIconName());
        toDoGroupDoc.put("shared", toDoGroup.isShared());
        toDoGroupDoc.put("subscribers", Arrays.asList(toDoGroup.getSubscribers()));

        //toDoGroupDoc.put("toDoItems", toDoGroup.getToDoItems());

        ArrayList<ToDoItem> toDoItemArrayList = toDoGroup.getToDoItemArrayList();
        Map<String, Object> toDoItemsMap = new HashMap<>();

        for (ToDoItem item : toDoItemArrayList)
        {
            Map<String, Object> toDoItemMap = new HashMap<>();
            toDoItemMap.put("priority", item.getPriority());
            toDoItemMap.put("timestamp", item.getTimestamp());
            toDoItemMap.put("done", item.isDone());

            toDoItemsMap.put(item.getItemName(), toDoItemMap);
        }

        toDoGroupDoc.put("toDoItems", toDoItemsMap);

        return toDoGroupDoc;
    }

    public static ToDoGroup createToDoGroupObj(DocumentSnapshot documentSnapshot)
    {
        String id = documentSnapshot.getId();
        String title = documentSnapshot.getString("title");
        String iconName = documentSnapshot.getString("iconName");
        boolean shared = documentSnapshot.getBoolean("shared");

        ArrayList<String> subscribersList = (ArrayList<String>) documentSnapshot.get("subscribers");

        String[] subscribers = subscribersList.toArray(new String[0]);

        Map<String, Object> toDoItems = (Map<String, Object>) documentSnapshot.get("toDoItems");

        return new ToDoGroup(id, title, iconName, shared, subscribers, toDoItems);
    }

    public static Map<String, Object> createReminderItemMap(ReminderItem reminderItem)
    {
        Map<String, Object> reminderItemMap = new HashMap<>();

        reminderItemMap.put("recurrence", reminderItem.getRecurrenceString());
        reminderItemMap.put("recurrenceNum", reminderItem.getRecurrenceNum());
        reminderItemMap.put("recurrenceInterval", reminderItem.getRecurrenceInterval());
        reminderItemMap.put("nextOccurrence", reminderItem.getNextOccurrence());
        reminderItemMap.put("category", reminderItem.getCategory());
        reminderItemMap.put("categoryIconName", reminderItem.getCategoryIconName());
        reminderItemMap.put("description", reminderItem.getDescription());
        reminderItemMap.put("isRecurring", reminderItem.isRecurring());
        reminderItemMap.put("isSnoozed", reminderItem.isSnoozed());
        reminderItemMap.put("isHibernating", reminderItem.isHibernating());
        reminderItemMap.put("history", reminderItem.getHistory());

        return reminderItemMap;
    }

    public static ReminderItem createReminderItemObj(Map.Entry<String, Object> entry)
    {
        String title = entry.getKey();

        Map<String, Object> reminderItemMap = (Map<String, Object>) entry.getValue();

        boolean isRecurring = (boolean) reminderItemMap.get("isRecurring");
        int recurrenceNum = Math.toIntExact((long) reminderItemMap.get("recurrenceNum"));
        String recurrenceInterval = (String) reminderItemMap.get("recurrenceInterval");
        String nextOccurrence = (String) reminderItemMap.get("nextOccurrence");
        String category = (String) reminderItemMap.get("category");
        String categoryIconName = (String) reminderItemMap.get("categoryIconName");
        String description = (String) reminderItemMap.get("description");
        boolean isSnoozed = (boolean) reminderItemMap.get("isSnoozed");
        boolean isHibernating = (boolean) reminderItemMap.get("isHibernating");
        Map<String, String> history = (Map<String, String>) reminderItemMap.get("history");

        return new ReminderItem(title, isRecurring, recurrenceNum, recurrenceInterval,
                nextOccurrence, category, categoryIconName, description, isSnoozed,
                isHibernating, history);
    }

}
