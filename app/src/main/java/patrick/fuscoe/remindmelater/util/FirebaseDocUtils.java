package patrick.fuscoe.remindmelater.util;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.FirebaseMessage;
import patrick.fuscoe.remindmelater.models.Friend;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.ToDoItem;
import patrick.fuscoe.remindmelater.models.UserProfile;

/**
 * Handles creation of documents to be stored in Google FireStore and
 * creation of Java objects from the cloud documents
*/
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
        //userProfileDoc.put("friends", Arrays.asList(userProfile.getFriends()));
        userProfileDoc.put("deviceToken", userProfile.getDeviceToken());

        ArrayList<Friend> friendArrayList = userProfile.getFriendArrayList();
        Map<String, Object> friendListMap = new HashMap<>();

        for (Friend friend : friendArrayList)
        {
            Map<String, Object> friendMap = new HashMap<>();
            friendMap.put("friendDisplayName", friend.getFriendDisplayName());

            friendListMap.put(friend.getFriendId(), friendMap);
        }

        userProfileDoc.put("friendListMap", friendListMap);

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

        /*
        ArrayList<String> friendsList = (ArrayList<String>) docMap.get("friends");
        String[] friends;
        friends = friendsList.toArray(new String[0]);
        */

        String deviceToken = (String) docMap.get("deviceToken");

        Map<String, Object> friendListMap = new HashMap<>();

        // Check for missing field
        if (documentSnapshot.contains("friendListMap"))
        {
            friendListMap = (Map<String, Object>) documentSnapshot.get("friendListMap");
        }

        return new UserProfile(id, displayName, subscriptions, reminderCategories,
                reminderTimeHour, reminderTimeMinute, hibernateLength, deviceToken,
                friendListMap);
    }

    public static Map<String, Object> createToDoGroupDoc(ToDoGroup toDoGroup)
    {
        Map<String, Object> toDoGroupDoc = new HashMap<>();

        toDoGroupDoc.put("title", toDoGroup.getTitle());
        toDoGroupDoc.put("iconName", toDoGroup.getIconName());
        toDoGroupDoc.put("shared", toDoGroup.isShared());
        toDoGroupDoc.put("subscribers", Arrays.asList(toDoGroup.getSubscribers()));

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

    public static Map<String, Object> createFriendRequestMessageDoc(String friendEmail,
                                                                    UserProfile userProfile)
    {
        Map<String, Object> friendRequestMessageDoc = new HashMap<>();

        friendRequestMessageDoc.put("messageType", "friendRequest");
        friendRequestMessageDoc.put("actionType", "none");
        friendRequestMessageDoc.put("friendEmail", friendEmail);
        friendRequestMessageDoc.put("senderId", userProfile.getId());
        friendRequestMessageDoc.put("senderDisplayName", userProfile.getDisplayName());
        friendRequestMessageDoc.put("senderDeviceToken", userProfile.getDeviceToken());
        friendRequestMessageDoc.put("toDoGroupId", "none");
        friendRequestMessageDoc.put("toDoGroupTitle", "none");
        friendRequestMessageDoc.put("reminderDocId", "none");
        friendRequestMessageDoc.put("reminderTitle", "none");

        return friendRequestMessageDoc;
    }

    public static FirebaseMessage createFirebaseMessageObj(Map<String, String> data)
    {
        String messageType = data.get("messageType");
        String actionType = data.get("actionType");
        String friendEmail = data.get("friendEmail");
        String senderId = data.get("senderId");
        String senderDisplayName = data.get("senderDisplayName");
        String senderDeviceToken = data.get("senderDeviceToken");
        String toDoGroupId = data.get("toDoGroupId");
        String toDoGroupTitle = data.get("toDoGroupTitle");
        String reminderDocId = data.get("reminderDocId");
        String reminderTitle = data.get("reminderTitle");

        return new FirebaseMessage(messageType, actionType, friendEmail, senderId,
                senderDisplayName, senderDeviceToken, toDoGroupId, toDoGroupTitle, reminderDocId,
                reminderTitle);
    }

    public static Map<String, Object> createActionResponseDoc(String outgoingMessageType,
                                                              String actionType,
                                                              UserProfile userProfile,
                                                              FirebaseMessage requestMessage)
    {
        Map<String, Object> actionResponseDoc = new HashMap<>();

        actionResponseDoc.put("messageType", outgoingMessageType);
        actionResponseDoc.put("actionType", actionType);
        actionResponseDoc.put("friendEmail", requestMessage.getFriendEmail());
        actionResponseDoc.put("senderId", userProfile.getId());
        actionResponseDoc.put("senderDisplayName", userProfile.getDisplayName());
        actionResponseDoc.put("senderDeviceToken", userProfile.getDeviceToken());
        actionResponseDoc.put("receiverDisplayName", requestMessage.getSenderDisplayName());
        actionResponseDoc.put("receiverDeviceToken", requestMessage.getSenderDeviceToken());
        actionResponseDoc.put("receiverId", requestMessage.getSenderId());
        actionResponseDoc.put("toDoGroupId", requestMessage.getToDoGroupId());
        actionResponseDoc.put("toDoGroupTitle", requestMessage.getToDoGroupTitle());

        return actionResponseDoc;
    }

    public static Map<String, Object> createShareToDoRequestMessageDoc(
            Friend friend, UserProfile userProfile, ToDoGroup toDoGroup)
    {
        Map<String, Object> shareToDoRequestMessageDoc = new HashMap<>();

        shareToDoRequestMessageDoc.put("messageType", "shareToDoRequest");
        shareToDoRequestMessageDoc.put("actionType", "none");
        shareToDoRequestMessageDoc.put("friendEmail", "none");
        shareToDoRequestMessageDoc.put("receiverId", friend.getFriendId());
        shareToDoRequestMessageDoc.put("senderId", userProfile.getId());
        shareToDoRequestMessageDoc.put("senderDisplayName", userProfile.getDisplayName());
        shareToDoRequestMessageDoc.put("senderDeviceToken", userProfile.getDeviceToken());
        shareToDoRequestMessageDoc.put("toDoGroupId", toDoGroup.getId());
        shareToDoRequestMessageDoc.put("toDoGroupTitle", toDoGroup.getTitle());
        shareToDoRequestMessageDoc.put("reminderDocId", "none");
        shareToDoRequestMessageDoc.put("reminderTitle", "none");

        return shareToDoRequestMessageDoc;
    }

    public static Map<String, Object> createSendReminderMessageDoc(
            Friend friend, UserProfile userProfile, String reminderDocId, String reminderTitle)
    {
        Map<String, Object> sendReminderMessageDoc = new HashMap<>();

        sendReminderMessageDoc.put("messageType", "shareToDoRequest");
        sendReminderMessageDoc.put("actionType", "none");
        sendReminderMessageDoc.put("friendEmail", "none");
        sendReminderMessageDoc.put("receiverId", friend.getFriendId());
        sendReminderMessageDoc.put("senderId", userProfile.getId());
        sendReminderMessageDoc.put("senderDisplayName", userProfile.getDisplayName());
        sendReminderMessageDoc.put("senderDeviceToken", userProfile.getDeviceToken());
        sendReminderMessageDoc.put("toDoGroupId", "none");
        sendReminderMessageDoc.put("toDoGroupTitle", "none");
        sendReminderMessageDoc.put("reminderDocId", reminderDocId);
        sendReminderMessageDoc.put("reminderTitle", reminderTitle);

        return sendReminderMessageDoc;
    }

}
