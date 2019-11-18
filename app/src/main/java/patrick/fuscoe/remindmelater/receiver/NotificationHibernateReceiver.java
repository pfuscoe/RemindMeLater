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
import java.time.LocalDate;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;
import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;

/**
 * Receives notification 'Hibernate' tap action.
 *
 * Updates reminder in cloud, updates local device storage alarm data, and resets alarm.
 */
public class NotificationHibernateReceiver extends BroadcastReceiver {

    public static final String TAG = "patrick.fuscoe.remindmelater.NotificationHibernateReceiver";

    public static final int DEFAULT_NOTIFICATION_ID = 102;
    public static final int DEFAULT_HIBERNATE_DAYS = 14;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    private DocumentReference remindersDocRef;
    private String remindersDocId;

    private Context context;
    private int notificationId;

    private ReminderItem reminderItem;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID);

        Log.d(TAG, ": notificationId: " + notificationId);

        Gson gson = new Gson();
        Type dataTypeReminderItem = new TypeToken<ReminderItem>(){}.getType();
        String reminderItemString = intent.getStringExtra(ReminderAlarmReceiver.REMINDER_ITEM);

        Log.d(TAG, "reminderItemString: " + reminderItemString);

        reminderItem = gson.fromJson(reminderItemString, dataTypeReminderItem);

        remindersDocId = intent.getStringExtra(ReminderAlarmReceiver.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

        updateReminderItemOnHibernate();
        saveReminderItem();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    private void updateReminderItemOnHibernate()
    {
        LocalDate now = LocalDate.now();
        LocalDate nextOccurrence = now.plusDays(DEFAULT_HIBERNATE_DAYS);

        reminderItem.setNextOccurrence(nextOccurrence.toString());
        reminderItem.setSnoozed(false);
        reminderItem.setHibernating(true);
    }

    private void saveReminderItem()
    {
        Map<String, Object> reminderItemMap = FirebaseDocUtils.createReminderItemMap(reminderItem);

        remindersDocRef.update(reminderItem.getTitle(), reminderItemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reminders DocumentSnapshot successfully updated!");
                        ReminderAlarmUtils.saveReminderToSharedPreferences(context, reminderItem);
                        ReminderAlarmUtils.setReminderAlarm(context, reminderItem);
                        Toast.makeText(context, "Reminder Hibernating: " + reminderItem.getTitle(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        Toast.makeText(context, "Action failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

}
