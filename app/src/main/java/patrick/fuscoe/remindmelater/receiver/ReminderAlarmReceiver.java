package patrick.fuscoe.remindmelater.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ReminderDetailsActivity;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;

/**
 * Receives reminder alarms from device and creates notification
*/
public class ReminderAlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderAlarmReceiver";

    public static final String EXTRA_NOTIFICATION_ID = "patrick.fuscoe.remindmelater.EXTRA_NOTIFICATION_ID";
    public static final String NOTIFICATION_ACTION_DONE = "patrick.fuscoe.remindmelater.receiver.NotificationDoneReceiver";
    public static final String NOTIFICATION_ACTION_SNOOZE = "patrick.fuscoe.remindmelater.receiver.NotificationSnoozeReceiver";
    public static final String NOTIFICATION_ACTION_HIBERNATE = "patrick.fuscoe.remindmelater.receiver.NotificationHibernateReceiver";

    public static final String REMINDER_ITEM = "patrick.fuscoe.remindmelater.REMINDERS";
    public static final String REMINDERS_DOC_ID = "patrick.fuscoe.remindmelater.REMINDERS_DOC_ID";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth auth;
    private String userId;

    private SharedPreferences reminderNotificationIds;

    private final CollectionReference reminders = db.collection("reminders");

    private Context context;

    private String remindersDocId;
    private String reminderTitle;
    private ReminderItem reminderItem;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        reminderNotificationIds = context.getSharedPreferences(
                context.getString(R.string.reminder_notification_ids_file_key), Context.MODE_PRIVATE);

        Log.d(TAG, "MainActivity.REMINDER_TITLE: " + MainActivity.REMINDER_TITLE);

        reminderTitle = intent.getStringExtra(MainActivity.REMINDER_TITLE);

        Log.d(TAG, ": Alarm Received: " + reminderTitle);

        loadReminder();
    }

    public void loadReminder()
    {
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
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void buildReminderItem(QueryDocumentSnapshot document)
    {
        Log.d(TAG, "reminderTitle: " + reminderTitle);

        Map<String, Object> docMap = document.getData();

        for (Map.Entry<String, Object> entry : docMap.entrySet())
        {
            if (entry.getKey().equals(reminderTitle))
            {
                reminderItem = FirebaseDocUtils.createReminderItemObj(entry);

                return;
            }
        }

        // Could not find reminder in docMap
        Log.d(TAG, "Error: Could not find reminder in docMap of queryDocumentSnapshot");
        Toast.makeText(context, "Reminder Alarm Error: Could not find reminder data for " +
                reminderTitle + " in cloud database", Toast.LENGTH_LONG).show();
    }

    public void sendNotification()
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        int notificationId;

        if (reminderNotificationIds.contains(reminderTitle))
        {
            notificationId = reminderNotificationIds.getInt(reminderTitle, MainActivity.DEFAULT_NOTIFICATION_ID);
        }
        else
        {
            notificationId = generateUniqueInt();
        }

        SharedPreferences.Editor reminderNotificationIdsEditor = reminderNotificationIds.edit();
        reminderNotificationIdsEditor.putInt(reminderTitle, notificationId);
        reminderNotificationIdsEditor.apply();

        Log.d(TAG, reminderTitle + ": notificationId: " + notificationId);

        int iconId = context.getResources().getIdentifier(
                reminderItem.getCategoryIconName(), "drawable", context.getPackageName());
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(context.getResources(), iconId);

        Log.d(TAG, ": notificationId: " + notificationId);

        Gson gson = new Gson();
        String reminderItemString = gson.toJson(reminderItem);

        Log.d(TAG, ": Gson reminderItemString: " + reminderItemString);

        String contentRecurrenceString;
        String contentTitleString;

        if (reminderItem.isRecurring())
        {
            contentRecurrenceString = reminderItem.getRecurrenceNum() + " " + reminderItem.getRecurrenceInterval();
        }
        else
        {
            contentRecurrenceString = "Non-Recurring";
        }

        contentTitleString = reminderTitle + "  (" + contentRecurrenceString + ")";

        // Notification Tap Intent
        Intent reminderDetailsIntent = new Intent(context, ReminderDetailsActivity.class);
        reminderDetailsIntent.putExtra(RemindersFragment.REMINDERS_DOC_ID, remindersDocId);
        reminderDetailsIntent.putExtra(RemindersFragment.REMINDER_ITEM, reminderItemString);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(reminderDetailsIntent);
        PendingIntent reminderDetailsPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Done Intent
        Intent doneIntent = new Intent(context, NotificationDoneReceiver.class);
        doneIntent.setAction(NOTIFICATION_ACTION_DONE + notificationId);
        doneIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        doneIntent.putExtra(REMINDER_ITEM, reminderItemString);
        doneIntent.putExtra(REMINDERS_DOC_ID, remindersDocId);
        PendingIntent donePendingIntent =
                PendingIntent.getBroadcast(context, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Snooze Intent
        Intent snoozeIntent = new Intent(context, NotificationSnoozeReceiver.class);
        snoozeIntent.setAction(NOTIFICATION_ACTION_SNOOZE + notificationId);
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        snoozeIntent.putExtra(REMINDER_ITEM, reminderItemString);
        snoozeIntent.putExtra(REMINDERS_DOC_ID, remindersDocId);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification Hibernate Intent
        Intent hibernateIntent = new Intent(context, NotificationHibernateReceiver.class);
        hibernateIntent.setAction(NOTIFICATION_ACTION_HIBERNATE + notificationId);
        hibernateIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        hibernateIntent.putExtra(REMINDER_ITEM, reminderItemString);
        hibernateIntent.putExtra(REMINDERS_DOC_ID, remindersDocId);
        PendingIntent hibernatePendingIntent =
                PendingIntent.getBroadcast(context, 0, hibernateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(largeIconBitmap)
                .setSmallIcon(iconId)
                .setContentTitle(contentTitleString)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(reminderDetailsPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setSound(null)
                .setVibrate(null)
                .addAction(R.drawable.action_check, context.getString(R.string.done), donePendingIntent)
                .addAction(R.drawable.action_alarm_snooze, context.getString(R.string.snooze), snoozePendingIntent)
                .addAction(R.drawable.hibernate, context.getString(R.string.hibernate), hibernatePendingIntent);

        if (!reminderItem.getDescription().equals(""))
        {
            builder.setContentText(reminderItem.getDescription());
        }

        notificationManager.notify(notificationId, builder.build());
    }

    private int generateUniqueInt()
    {
        return (int) (Math.random() * 1000000);
    }
}
