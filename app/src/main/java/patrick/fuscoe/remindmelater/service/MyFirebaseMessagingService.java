package patrick.fuscoe.remindmelater.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.models.FirebaseMessage;
import patrick.fuscoe.remindmelater.receiver.MessageNotificationAddFriendReceiver;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;

/**
 * Receives firebase messages and generates notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "patrick.fuscoe.remindmelater.MyFirebaseMessagingService";

    public static final String EXTRA_NOTIFICATION_ID = "patrick.fuscoe.remindmelater.EXTRA_NOTIFICATION_ID";
    public static final String MESSAGE_ACTION_FRIEND_ADD = "patrick.fuscoe.remindmelater.MESSAGE_ACTION_FRIEND_ADD";

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
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "Message Received From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();

            if (!data.containsKey("messageType"))
            {
                Log.d(TAG, "No Message Type specified in data.");
                return;
            }

            String messageType = data.get("messageType");

            filterMessageType(messageType, data);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void filterMessageType(String messageType, Map<String, String> data)
    {
        switch (messageType)
        {
            case "friendRequest":
                // TODO: Handle friend request
                FirebaseMessage message = FirebaseDocUtils.createFirebaseMessageObj(data);
                sendFriendRequestNotification(message);
                return;

            case "friendConfirm":
                // TODO: Handle friend confirm
                return;

            case "shareToDoRequest":
                // TODO: Handle to do request
                return;

            case "shareToDoConfirm":
                // TODO: Handle to do confirm
                return;

            case "reminderRequest":
                // TODO: Handle reminder request
                return;

            case "reminderConfirm":
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

        String contentTitleString = "";
        String contentTextString = "";

        // Notification Tap Intent
        Intent friendRequestIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(friendRequestIntent);
        PendingIntent friendRequestPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Add Friend Intent
        Intent friendConfirmIntent = new Intent(this,
                MessageNotificationAddFriendReceiver.class);
        friendConfirmIntent.setAction(MESSAGE_ACTION_FRIEND_ADD + notificationId);
        friendConfirmIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent friendConfirmPendingIntent = PendingIntent.getBroadcast(this,
                0, friendConfirmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Deny Friend Intent


        // Build Notification




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
