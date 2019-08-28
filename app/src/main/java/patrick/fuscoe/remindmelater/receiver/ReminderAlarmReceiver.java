package patrick.fuscoe.remindmelater.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;
import patrick.fuscoe.remindmelater.ui.main.RemindersViewModel;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderAlarmReceiver";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = auth.getUid();

    private final CollectionReference reminders = db.collection("reminders");

    private Context context;

    private String remindersDocId;
    private String reminderTitle;
    private ReminderItem reminderItem;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        //int notificationId = (int) System.currentTimeMillis();

        reminderTitle = intent.getStringExtra(MainActivity.REMINDER_TITLE);
        //int iconId = intent.getIntExtra(MainActivity.REMINDER_ICON_ID, R.drawable.category_note);

        /*
        Intent remindersFragmentIntent = new Intent(context, RemindersFragment.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(remindersFragmentIntent);
        PendingIntent remindersFragmentPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup send notification for received alarm
        // Setup action buttons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(reminderTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(remindersFragmentPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
        */
    }

    public void loadReminder()
    {
        //RemindersViewModel remindersViewModel = new RemindersViewModel();
        // TODO: get reminders doc from firestore
        reminders.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                remindersDocId = document.getId();
                                Log.d(TAG, ": remindersDocId: " + remindersDocId);
                                buildReminderItem(document);
                                sendNotification();

                                //Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void buildReminderItem(QueryDocumentSnapshot document)
    {
        Map<String, Object> docMap = document.getData();
        HashMap<String, Object> reminderItemMap = (HashMap<String, Object>) docMap.get(reminderTitle);

        int recurrenceNum = Math.toIntExact((long) reminderItemMap.get("recurrenceNum"));
        String recurrenceInterval = (String) reminderItemMap.get("recurrenceInterval");
        String nextOccurrence = (String) reminderItemMap.get("nextOccurrence");
        String category = (String) reminderItemMap.get("category");
        int categoryIcon = Math.toIntExact((long) reminderItemMap.get("categoryIcon"));
        String description = (String) reminderItemMap.get("description");

        reminderItem = new ReminderItem(reminderTitle, recurrenceNum,
                recurrenceInterval, nextOccurrence, category, categoryIcon, description);
    }

    public void sendNotification()
    {
        int notificationId = (int) System.currentTimeMillis();
        int iconId = reminderItem.getCategoryIcon();

        // Notification Tap Intent
        Intent remindersFragmentIntent = new Intent(context, RemindersFragment.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(remindersFragmentIntent);
        PendingIntent remindersFragmentPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: Notification Done Intent
        Intent doneIntent = new Intent(context, NotificationDoneReceiver.class);

        // TODO: Notification Snooze Intent
        Intent snoozeIntent = new Intent(context, NotificationSnoozeReceiver.class);
        
        // TODO: Notification Dismiss Intent
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(reminderTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(remindersFragmentPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
