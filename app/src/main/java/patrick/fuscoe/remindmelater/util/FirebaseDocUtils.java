package patrick.fuscoe.remindmelater.util;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ToDoGroup;
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
        toDoGroupDoc.put("numPriorityOneItems", toDoGroup.getNumPriorityOneItems());
        toDoGroupDoc.put("numUnfinishedItems", toDoGroup.getNumUnfinishedItems());
        toDoGroupDoc.put("totalItems", toDoGroup.getTotalItems());
        toDoGroupDoc.put("subscribers", Arrays.asList(toDoGroup.getSubscribers()));

        toDoGroupDoc.put("toDoItems", toDoGroup.getToDoItems());

        return toDoGroupDoc;
    }

    public static ToDoGroup createToDoGroupObj(DocumentSnapshot documentSnapshot)
    {
        String id = documentSnapshot.getId();
        String title = documentSnapshot.getString("title");
        String iconName = documentSnapshot.getString("iconName");
        boolean shared = documentSnapshot.getBoolean("shared");
        int numPriorityOneItems = documentSnapshot.get("numPriorityOneItems", int.class);
        int numUnfinishedItems = documentSnapshot.get("numUnfinishedItems", int.class);

        ArrayList<String> subscribersList = (ArrayList<String>) documentSnapshot.get("subscribers");

        String[] subscribers = subscribersList.toArray(new String[0]);

        Map<String, Object> toDoItems = (Map<String, Object>) documentSnapshot.get("toDoItems");

        return new ToDoGroup(id, title, iconName, shared,
                numPriorityOneItems, numUnfinishedItems, subscribers, toDoItems);
    }



}
