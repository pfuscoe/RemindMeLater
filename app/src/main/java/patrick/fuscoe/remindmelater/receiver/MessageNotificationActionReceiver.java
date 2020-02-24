package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.FirebaseMessage;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.service.MyFirebaseMessagingService;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;

/**
 * Receives Firebase message notification 'Confirm' tap action on friend request.
 *
 * Updates user profile and creates friend request confirmation message in cloud.
 */
public class MessageNotificationActionReceiver extends BroadcastReceiver {

    public static final String TAG =
            "patrick.fuscoe.remindmelater.MessageNotificationActionReceiver";

    public static final int DEFAULT_NOTIFICATION_ID = 100;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference messagesCollectionRef = db.collection("messages");

    private Context context;
    private int notificationId;

    private String actionType;
    private FirebaseMessage firebaseMessage;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(MyFirebaseMessagingService.EXTRA_NOTIFICATION_ID,
                DEFAULT_NOTIFICATION_ID);
        this.actionType = intent.getStringExtra(
                MyFirebaseMessagingService.MESSAGE_NOTIFICATION_ACTION_TYPE);

        Log.d(TAG, "notificationId: " + notificationId);

        Gson gson = new Gson();
        Type dataTypeFirebaseMessage = new TypeToken<FirebaseMessage>(){}.getType();
        String firebaseMessageString = intent.getStringExtra(
                MyFirebaseMessagingService.FIREBASE_MESSAGE_STRING);
        firebaseMessage = gson.fromJson(firebaseMessageString, dataTypeFirebaseMessage);

        filterActionType();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    private void filterActionType()
    {
        switch (actionType)
        {
            case "addFriend":
                // either add friend, send notification back, and they add friend or
                // post message to cloud for server to add both friends
                return;

            case "denyFriend":
                return;

            default:
                return;
        }
    }

    // Write a message to FireStore to trigger cloud function
    private void sendFriendActionResponseMessage(String actionType)
    {
        Map<String, Object> createFriendActionResponseDoc =
                FirebaseDocUtils.createFriendRequestMessageDoc(actionType, userProfile, firebaseMessage);

        db.collection("messages")
                .add(friendRequestMessageDocMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Friend request message successfully written. FireStore" +
                                " messageID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), "Friend request sent " +
                                "successfully!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing friend request document to cloud: " +
                                e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error sending friend " +
                                "request to cloud: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
