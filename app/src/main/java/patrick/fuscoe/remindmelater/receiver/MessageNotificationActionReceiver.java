package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
    private final CollectionReference remindersCollectionRef = db.collection(
            "reminders");

    private Context context;
    private int notificationId;

    private String outgoingMessageType;
    private String actionType;
    private FirebaseMessage firebaseMessage;
    private UserProfile userProfile;
    private String remindersDocId;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(MyFirebaseMessagingService.EXTRA_NOTIFICATION_ID,
                DEFAULT_NOTIFICATION_ID);
        this.actionType = intent.getStringExtra(
                MyFirebaseMessagingService.MESSAGE_NOTIFICATION_ACTION_TYPE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (actionType.equals("clear"))
        {
            notificationManager.cancel(notificationId);
            return;
        }

        this.outgoingMessageType = intent.getStringExtra(
                MyFirebaseMessagingService.MESSAGE_NOTIFICATION_OUTGOING_MESSAGE_TYPE);

        Log.d(TAG, "notificationId: " + notificationId);

        Gson gson = new Gson();
        Type dataTypeFirebaseMessage = new TypeToken<FirebaseMessage>(){}.getType();
        String firebaseMessageString = intent.getStringExtra(
                MyFirebaseMessagingService.FIREBASE_MESSAGE_STRING);
        firebaseMessage = gson.fromJson(firebaseMessageString, dataTypeFirebaseMessage);

        Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
        String userProfileString = intent.getStringExtra(
                MyFirebaseMessagingService.USER_PROFILE_STRING);
        userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);

        remindersDocId = "none";

        if (outgoingMessageType.equals("sendReminderActionResponse"))
        {
            // Get reminderDocId from FireStore then send response
            findReminderDocIdAndRespond();
        }
        else
        {
            sendActionResponseMessage();
        }

        notificationManager.cancel(notificationId);
    }

    /*
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
    */

    // Write a message to FireStore that triggers cloud function
    private void sendActionResponseMessage()
    {
        Map<String, Object> friendActionResponseDoc =
                FirebaseDocUtils.createActionResponseDoc(outgoingMessageType, actionType,
                        userProfile, firebaseMessage, remindersDocId);

        db.collection("messages")
                .add(friendActionResponseDoc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Friend action response message successfully written. " +
                                "FireStore messageID: " + documentReference.getId());
                        Toast.makeText(context, "Response sent successfully!",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing friend request document to cloud: " +
                                e.getMessage());
                        Toast.makeText(context, "Error sending response to cloud: " +
                                e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void findReminderDocIdAndRespond()
    {
        remindersCollectionRef.whereEqualTo("userId", userProfile.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                            {
                                remindersDocId = documentSnapshot.getId();
                                Log.d(TAG, "remindersDocId: " + remindersDocId);
                                sendActionResponseMessage();
                            }
                        } else {
                            Log.d(TAG, "Error getting reminders doc Id in FireStore" +
                                    " for userId: " + userProfile.getId());
                        }
                    }
                });
    }
}
