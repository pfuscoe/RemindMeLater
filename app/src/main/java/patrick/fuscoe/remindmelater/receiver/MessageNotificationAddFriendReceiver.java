package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import patrick.fuscoe.remindmelater.service.MyFirebaseMessagingService;

/**
 * Receives Firebase message notification 'Confirm' tap action on friend request.
 *
 * Updates user profile and creates friend request confirmation message in cloud.
 */
public class MessageNotificationAddFriendReceiver extends BroadcastReceiver {

    public static final String TAG =
            "patrick.fuscoe.remindmelater.MessageNotificationAddFriendReceiver";

    public static final int DEFAULT_NOTIFICATION_ID = 100;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference messagesCollectionRef = db.collection("messages");

    private Context context;
    private int notificationId;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(MyFirebaseMessagingService.EXTRA_NOTIFICATION_ID,
                DEFAULT_NOTIFICATION_ID);

        Log.d(TAG, "notificationId: " + notificationId);

    }
}
