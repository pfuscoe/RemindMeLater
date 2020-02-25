package patrick.fuscoe.remindmelater.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.FirebaseMessage;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.receiver.MessageNotificationActionReceiver;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;

/**
 * Receives firebase messages and generates notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "patrick.fuscoe.remindmelater.MyFirebaseMessagingService";

    public static final String NOTIFICATION_CHANNEL_ID = "patrick.fuscoe.remindmelater.NOTIFICATION_CHANNEL_ID";
    public static final String EXTRA_NOTIFICATION_ID = "patrick.fuscoe.remindmelater.EXTRA_NOTIFICATION_ID";
    public static final String MESSAGE_ACTION_FRIEND_ADD = "patrick.fuscoe.remindmelater.MESSAGE_ACTION_FRIEND_ADD";
    public static final String MESSAGE_ACTION_FRIEND_DENY = "patrick.fuscoe.remindmelater.MESSAGE_ACTION_FRIEND_DENY";
    public static final String FIREBASE_MESSAGE_STRING = "patrick.fuscoe.remindmelater.FIREBASE_MESSAGE_STRING";
    public static final String USER_PROFILE_STRING = "patrick.fuscoe.remindmelater.USER_PROFILE_STRING";
    public static final String MESSAGE_NOTIFICATION_ACTION_TYPE = "patrick.fuscoe.remindmelater.MESSAGE_NOTIFICATION_ACTION_TYPE";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;

    private UserProfile userProfile;
    private Map<String, String> messageData;
    private String messageType;
    private String contentTitleString;
    private String contentTextString;
    private String contentTextTemplate;

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

        // Load user profile then filter the message by type
        loadUserProfileFromCloud();

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            // TODO: implement notifications (with no actions) here?
        }
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

        switch (messageType)
        {
            case "friendRequest":
                FirebaseMessage message = FirebaseDocUtils.createFirebaseMessageObj(data);
                contentTitleString = "Friend Request";
                contentTextTemplate = " has sent you a friend request.";
                sendFriendRequestNotification(message);
                return;

            case "friendNotify":
                // TODO: Handle friend confirm
                return;

            case "shareToDoRequest":
                // TODO: Handle to do request
                return;

            case "shareToDoNotify":
                // TODO: Handle to do confirm
                return;

            case "reminderRequest":
                // TODO: Handle reminder request
                return;

            case "reminderNotify":
                // TODO: Handle reminder confirm
                return;

            default:
                Log.d(TAG, "Unknown messageType: " + messageType);
                return;
        }
    }

    private void sendFriendRequestNotification(FirebaseMessage message) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = generateUniqueInt();
        Log.d(TAG, "Message Notification Id: " + notificationId);

        int iconId = this.getResources().getIdentifier("message_friend_add",
                "drawable", this.getPackageName());
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

        Intent friendRequestIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(friendRequestIntent);
        PendingIntent friendRequestPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Add Friend Intent
        Intent friendAcceptIntent = new Intent(this,
                MessageNotificationActionReceiver.class);
        friendAcceptIntent.setAction(MESSAGE_ACTION_FRIEND_ADD + notificationId);
        friendAcceptIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        friendAcceptIntent.putExtra(MESSAGE_NOTIFICATION_ACTION_TYPE, "acceptFriend");
        friendAcceptIntent.putExtra(FIREBASE_MESSAGE_STRING, firebaseMessageString);
        friendAcceptIntent.putExtra(USER_PROFILE_STRING, userProfileString);
        PendingIntent friendAcceptPendingIntent = PendingIntent.getBroadcast(this,
                0, friendAcceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Deny Friend Intent
        Intent friendDenyIntent = new Intent(this,
                MessageNotificationActionReceiver.class);
        friendDenyIntent.setAction(MESSAGE_ACTION_FRIEND_DENY + notificationId);
        friendDenyIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        friendDenyIntent.putExtra(MESSAGE_NOTIFICATION_ACTION_TYPE, "denyFriend");
        friendDenyIntent.putExtra(FIREBASE_MESSAGE_STRING, firebaseMessageString);
        friendDenyIntent.putExtra(USER_PROFILE_STRING, userProfileString);
        PendingIntent friendDenyPendingIntent = PendingIntent.getBroadcast(this,
                0, friendDenyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                .addAction(R.drawable.action_check, getString(R.string.accept), friendAcceptPendingIntent)
                .addAction(R.drawable.ic_menu_close, getString(R.string.deny), friendDenyPendingIntent);

        notificationManager.notify(notificationId, builder.build());

        /*
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
        */

    }

    private int generateUniqueInt()
    {
        return (int) (Math.random() * 1000000);
    }

}
