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
import com.google.gson.Gson;

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

    public static final String EXTRA_NOTIFICATION_ID = "patrick.fuscoe.remindmelater.EXTRA_NOTIFICATION_ID";
    public static final String NOTIFICATION_ACTION_DONE = "patrick.fuscoe.remindmelater.receiver.NotificationDoneReceiver";
    public static final String NOTIFICATION_ACTION_SNOOZE = "patrick.fuscoe.remindmelater.receiver.NotificationSnoozeReceiver";
    public static final String NOTIFICATION_ACTION_DISMISS = "patrick.fuscoe.remindmelater.receiver.NotificationDismissReceiver";

    public static final String REMINDER_ITEM = "patrick.fuscoe.remindmelater.REMINDERS";
    public static final String REMINDERS_DOC_ID = "patrick.fuscoe.remindmelater.REMINDERS_DOC_ID";

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

        Log.d(TAG, ": Alarm Received: " + reminderTitle);

        loadReminder();

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

        //Log.d(TAG, ": recurrenceInterval: " + recurrenceInterval);

        reminderItem = new ReminderItem(reminderTitle, recurrenceNum,
                recurrenceInterval, nextOccurrence, category, categoryIcon, description);
    }

    public void sendNotification()
    {
        int notificationId = (int) System.currentTimeMillis();
        int iconId = reminderItem.getCategoryIcon();

        Log.d(TAG, ": reminderItem recurrenceString: " + reminderItem.getRecurrenceString());

        Gson gson = new Gson();
        String reminderItemString = gson.toJson(reminderItem);

        // Notification Tap Intent
        Intent remindersFragmentIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(remindersFragmentIntent);
        PendingIntent remindersFragmentPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Done Intent
        Intent doneIntent = new Intent(context, NotificationDoneReceiver.class);
        doneIntent.setAction(NOTIFICATION_ACTION_DONE);
        doneIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        doneIntent.putExtra(REMINDER_ITEM, reminderItemString);
        doneIntent.putExtra(REMINDERS_DOC_ID, remindersDocId);
        PendingIntent donePendingIntent =
                PendingIntent.getBroadcast(context, 0, doneIntent, 0);

        // Notification Snooze Intent
        Intent snoozeIntent = new Intent(context, NotificationSnoozeReceiver.class);
        snoozeIntent.setAction(NOTIFICATION_ACTION_SNOOZE);
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        snoozeIntent.putExtra(REMINDER_ITEM, reminderItemString);
        snoozeIntent.putExtra(REMINDERS_DOC_ID, remindersDocId);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

        // TODO: Notification Dismiss Intent
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(reminderTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(remindersFragmentPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .addAction(R.drawable.action_check, context.getString(R.string.done), donePendingIntent)
                .addAction(R.drawable.action_alarm_snooze, context.getString(R.string.snooze), snoozePendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
