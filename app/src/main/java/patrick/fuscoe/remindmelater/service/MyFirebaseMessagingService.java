package patrick.fuscoe.remindmelater.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.FirebaseMessage;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.receiver.MessageNotificationActionReceiver;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;
import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;

/**
 * Receives firebase messages and generates notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "patrick.fuscoe.remindmelater.MyFirebaseMessagingService";

    public static final String NOTIFICATION_CHANNEL_ID = "patrick.fuscoe.remindmelater.NOTIFICATION_CHANNEL_ID";
    public static final String EXTRA_NOTIFICATION_ID = "patrick.fuscoe.remindmelater.EXTRA_NOTIFICATION_ID";
    public static final String MESSAGE_ACTION_ACCEPT = "patrick.fuscoe.remindmelater.MESSAGE_ACTION_ACCEPT";
    public static final String MESSAGE_ACTION_DENY = "patrick.fuscoe.remindmelater.MESSAGE_ACTION_DENY";
    public static final String MESSAGE_ACTION_CLEAR = "patrick.fuscoe.remindmelater.MESSAGE_ACTION_CLEAR";
    public static final String FIREBASE_MESSAGE_STRING = "patrick.fuscoe.remindmelater.FIREBASE_MESSAGE_STRING";
    public static final String USER_PROFILE_STRING = "patrick.fuscoe.remindmelater.USER_PROFILE_STRING";
    public static final String MESSAGE_NOTIFICATION_OUTGOING_MESSAGE_TYPE = "patrick.fuscoe.remindmelater.MESSAGE_NOTIFICATION_OUTGOING_MESSAGE_TYPE";
    public static final String MESSAGE_NOTIFICATION_ACTION_TYPE = "patrick.fuscoe.remindmelater.MESSAGE_NOTIFICATION_ACTION_TYPE";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;

    private UserProfile userProfile;
    private FirebaseMessage message;
    private Map<String, String> messageData;
    private String messageType;
    private String contentTitleString;
    private String contentTextString;
    private String contentTextTemplate;
    private int iconId = 0;
    private String outgoingMessageType;
    private String positiveActionType;
    private String negativeActionType;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        // TODO: update token in FireStore
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "Message Received From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            messageData = remoteMessage.getData();

            if (!messageData.containsKey("messageType"))
            {
                Log.d(TAG, "No Message Type specified in data.");
                return;
            }

            messageType = messageData.get("messageType");
        }
        else
        {
            Log.d(TAG, "Message contains no data payload.");
            return;
        }

        createNotificationChannel();

        // Load user profile then filter the message by type
        loadUserProfileFromCloud();

        /*
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        */
    }

    private void loadUserProfileFromCloud()
    {
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            userProfile = FirebaseDocUtils.createUserProfileObj(documentSnapshot);
                            Log.d(TAG, "User Profile loaded from cloud");
                            filterMessageType(messageType, messageData);
                        }
                        else
                        {
                            Log.d(TAG, "Failed to retrieve user profile from cloud: "
                                    + task.getException());
                        }
                    }
                });
    }

    private void filterMessageType(String messageType, Map<String, String> data)
    {
        contentTitleString = "";
        contentTextString = "";
        contentTextTemplate = "";
        positiveActionType = "";
        negativeActionType = "";
        outgoingMessageType = "";

        switch (messageType)
        {
            case "friendRequest":
                message = FirebaseDocUtils.createFirebaseMessageObj(data);
                iconId = this.getResources().getIdentifier("message_friend_add",
                        "drawable", this.getPackageName());
                contentTitleString = "Friend Request";
                contentTextTemplate = " has sent you a friend request.";
                outgoingMessageType = "friendActionResponse";
                positiveActionType = "acceptFriend";
                negativeActionType = "denyFriend";
                sendRequestNotification(message);
                return;

            case "friendNotify":
                message = FirebaseDocUtils.createFirebaseMessageObj(data);
                contentTitleString = "Friend Response";
                sendFriendNotifyNotification(message);
                return;

            case "shareToDoRequest":
                message = FirebaseDocUtils.createFirebaseMessageObj(data);
                iconId = this.getResources().getIdentifier("ic_menu_list","drawable",
                        this.getPackageName());
                contentTitleString = "Share To Do List Request";
                contentTextTemplate = " would like to share " + message.getToDoGroupTitle() +
                        " with you.";
                outgoingMessageType = "shareToDoActionResponse";
                positiveActionType = "acceptToDoList";
                negativeActionType = "denyToDoList";
                sendRequestNotification(message);
                return;

            case "shareToDoNotify":
                // TODO: Handle to do confirm
                return;

            case "sendReminderRequest":
                message = FirebaseDocUtils.createFirebaseMessageObj(data);
                iconId = this.getResources().getIdentifier("action_alarm_plus",
                        "drawable", this.getPackageName());
                contentTitleString = "Received Reminder Request";
                contentTextTemplate = " has sent you a reminder: " + message.getReminderTitle();
                outgoingMessageType = "sendReminderActionResponse";
                positiveActionType = "acceptReminder";
                negativeActionType = "denyReminder";
                sendRequestNotification(message);
                return;

            case "resetReminderAlarms":
                saveRemindersToStorage();
                return;

            default:
                Log.d(TAG, "Unknown messageType: " + messageType);
                return;
        }
    }

    private void sendRequestNotification(FirebaseMessage message) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = generateUniqueInt();
        Log.d(TAG, "Message Notification Id: " + notificationId);

        Bitmap largeIconBitmap = BitmapFactory.decodeResource(this.getResources(), iconId);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Gson gson = new Gson();
        String firebaseMessageString = gson.toJson(message);
        String userProfileString = gson.toJson(userProfile);

        Log.d(TAG, "Gson FirebaseMessageString: " + firebaseMessageString);
        Log.d(TAG, "Gson userProfileString: " + userProfileString);

        contentTextString = message.getSenderDisplayName() + contentTextTemplate;

        // Notification Tap Intent
        Intent emptyIntent = new Intent();
        PendingIntent emptyPendingIntent = PendingIntent.getBroadcast(this, 0,
                emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent requestIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(requestIntent);
        PendingIntent friendRequestPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Add Friend Intent
        Intent acceptIntent = new Intent(this,
                MessageNotificationActionReceiver.class);
        acceptIntent.setAction(MESSAGE_ACTION_ACCEPT + notificationId);
        acceptIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        acceptIntent.putExtra(MESSAGE_NOTIFICATION_OUTGOING_MESSAGE_TYPE, outgoingMessageType);
        acceptIntent.putExtra(MESSAGE_NOTIFICATION_ACTION_TYPE, positiveActionType);
        acceptIntent.putExtra(FIREBASE_MESSAGE_STRING, firebaseMessageString);
        acceptIntent.putExtra(USER_PROFILE_STRING, userProfileString);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this,
                0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Deny Friend Intent
        Intent denyIntent = new Intent(this,
                MessageNotificationActionReceiver.class);
        denyIntent.setAction(MESSAGE_ACTION_DENY + notificationId);
        denyIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        denyIntent.putExtra(MESSAGE_NOTIFICATION_OUTGOING_MESSAGE_TYPE, outgoingMessageType);
        denyIntent.putExtra(MESSAGE_NOTIFICATION_ACTION_TYPE, negativeActionType);
        denyIntent.putExtra(FIREBASE_MESSAGE_STRING, firebaseMessageString);
        denyIntent.putExtra(USER_PROFILE_STRING, userProfileString);
        PendingIntent denyPendingIntent = PendingIntent.getBroadcast(this,
                0, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(largeIconBitmap)
                .setSmallIcon(iconId)
                .setContentTitle(contentTitleString)
                .setContentText(contentTextString)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(emptyPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .setVibrate(null)
                .addAction(R.drawable.action_check, getString(R.string.accept), acceptPendingIntent)
                .addAction(R.drawable.ic_menu_close, getString(R.string.deny), denyPendingIntent);

        notificationManager.notify(notificationId, builder.build());
    }

    private void sendFriendNotifyNotification(FirebaseMessage message)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = generateUniqueInt();
        Log.d(TAG, "Message Notification Id: " + notificationId);

        switch (message.getActionType())
        {
            case "acceptFriend":
                iconId = this.getResources().getIdentifier("action_check","drawable",
                        this.getPackageName());
                contentTextTemplate = " has accepted your friend request.";
                break;

            case "denyFriend":
                iconId = this.getResources().getIdentifier("ic_menu_close","drawable",
                        this.getPackageName());
                contentTextTemplate = " has denied your friend request";
                break;

            case "acceptToDoList":
                iconId = this.getResources().getIdentifier("action_check","drawable",
                        this.getPackageName());
                contentTextTemplate = " has accepted your share to do list request.";
                break;

            case "denyToDoList":
                iconId = this.getResources().getIdentifier("ic_menu_close","drawable",
                        this.getPackageName());
                contentTextTemplate = " has denied your share to do list request";
                break;

            case "acceptReminder":
                iconId = this.getResources().getIdentifier("action_check","drawable",
                        this.getPackageName());
                contentTextTemplate = " has accepted your reminder request.";
                break;

            case "denyReminder":
                iconId = this.getResources().getIdentifier("ic_menu_close","drawable",
                        this.getPackageName());
                contentTextTemplate = " has denied your reminder request";
                break;
        }

        Bitmap largeIconBitmap = BitmapFactory.decodeResource(this.getResources(), iconId);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Gson gson = new Gson();
        String firebaseMessageString = gson.toJson(message);

        Log.d(TAG, "Gson FirebaseMessageString: " + firebaseMessageString);

        contentTextString = message.getSenderDisplayName() + contentTextTemplate;

        // Notification Tap Intent
        Intent emptyIntent = new Intent();
        PendingIntent emptyPendingIntent = PendingIntent.getBroadcast(this, 0,
                emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent friendNotifyIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(friendNotifyIntent);
        PendingIntent friendNotifyPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Clear Intent
        Intent denyIntent = new Intent(this,
                MessageNotificationActionReceiver.class);
        denyIntent.setAction(MESSAGE_ACTION_CLEAR + notificationId);
        denyIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        denyIntent.putExtra(MESSAGE_NOTIFICATION_ACTION_TYPE, "clear");
        PendingIntent clearPendingIntent = PendingIntent.getBroadcast(this,
                0, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(largeIconBitmap)
                .setSmallIcon(iconId)
                .setContentTitle(contentTitleString)
                .setContentText(contentTextString)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(friendNotifyPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(null)
                .addAction(R.drawable.ic_menu_close, getString(R.string.clear), clearPendingIntent);

        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.firebase_message_notification_channel_name);
            String description = getString(R.string.firebase_message_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void saveRemindersToStorage()
    {
        remindersCollectionRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                //remindersDocRef = document.getReference();
                                String remindersDocId = document.getId();
                                Log.d(TAG, "remindersDocId: " + remindersDocId);

                                List<ReminderItem> reminderItemList = ReminderAlarmUtils.
                                        buildReminderItemList(document);
                                ReminderAlarmUtils.writeRemindersToDisk(getApplicationContext(),
                                        reminderItemList);

                                ReminderAlarmUtils.updateReminderAlarmsOnTimeSet(
                                        getApplicationContext());
                            }
                        }
                        else
                        {
                            Log.d(TAG, "Error getting documents: " + task.getException());
                        }
                    }
                });
    }

    private int generateUniqueInt()
    {
        return (int) (Math.random() * 1000000);
    }

}
