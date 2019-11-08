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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;
import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;

/**
 * Receives notification 'Done' tap action.
 *
 * Updates reminder in cloud, updates local device storage alarm data, and resets (or deletes)
 * alarm.
*/
public class NotificationDoneReceiver extends BroadcastReceiver {

    public static final String TAG = "patrick.fuscoe.remindmelater.NotificationDoneReceiver";

    public static final int DEFAULT_NOTIFICATION_ID = 100;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    private DocumentReference remindersDocRef;
    private DocumentReference userDocRef;

    private FirebaseAuth auth;
    private String userId;
    private String remindersDocId;

    private Context context;
    private int notificationId;

    private UserProfile userProfile;
    private ReminderItem reminderItem;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);

        Log.d(TAG, ": notificationId: " + notificationId);

        Gson gson = new Gson();
        Type dataTypeReminderItem = new TypeToken<ReminderItem>(){}.getType();
        String reminderItemString = intent.getStringExtra(ReminderAlarmReceiver.REMINDER_ITEM);

        Log.d(TAG, "reminderItemString: " + reminderItemString);

        reminderItem = gson.fromJson(reminderItemString, dataTypeReminderItem);

        remindersDocId = intent.getStringExtra(ReminderAlarmReceiver.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

        // Get user profile from cloud and handle reminder updates and alarm management
        executeNotificationDoneAction();
    }

    private void executeNotificationDoneAction()
    {
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            userProfile = FirebaseDocUtils.createUserProfileObj(documentSnapshot);

                            if (reminderItem.isRecurring())
                            {
                                updateReminderItem();
                                saveReminderItem();
                            }
                            else
                            {
                                deleteReminder();
                            }

                            NotificationManagerCompat notificationManager =
                                    NotificationManagerCompat.from(context);
                            notificationManager.cancel(notificationId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to retrieve user document from cloud");
                        Toast.makeText(context, "Operation failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateReminderItem()
    {
        Log.d(TAG, ": reminderItem recurrenceString: " + reminderItem.getRecurrenceString());

        String recurrenceString = reminderItem.getRecurrenceString();
        Period recurrence = Period.parse(recurrenceString);

        int daysUntilNext = recurrence.getDays();
        int monthsUntilNext = recurrence.getMonths();
        int yearsUntilNext = recurrence.getYears();

        LocalDate now = LocalDate.now();
        Log.d(TAG, ": now: " + now.toString());

        LocalDate nextOccurrence = now.plusDays(daysUntilNext);
        nextOccurrence = nextOccurrence.plusMonths(monthsUntilNext);
        nextOccurrence = nextOccurrence.plusYears(yearsUntilNext);

        reminderItem.setNextOccurrence(nextOccurrence.toString());
        Log.d(TAG, ": nextOccurrence: " + reminderItem.getNextOccurrence());

        reminderItem.setSnoozed(false);
        reminderItem.setHibernating(false);
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
                        ReminderAlarmUtils.setReminderAlarm(context, reminderItem,
                                userProfile.getReminderHour(), userProfile.getReminderMinute());
                        Toast.makeText(context, "Reminder Updated: " + reminderItem.getTitle(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        Toast.makeText(context, "Action failed due to network error: " +
                                e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteReminder()
    {
        final String reminderTitle = reminderItem.getTitle();

        Map<String, Object> removeReminderUpdate = new HashMap<>();
        removeReminderUpdate.put(reminderTitle, FieldValue.delete());

        remindersDocRef.update(removeReminderUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Delete Reminder Success: Reminders DocumentSnapshot successfully updated!");
                        ReminderAlarmUtils.cancelReminderAlarm(context, reminderTitle);
                        ReminderAlarmUtils.deleteReminderFromSharedPreferences(context,
                                reminderTitle);
                        Toast.makeText(context, "Reminder Deleted: " +
                                reminderTitle, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        Toast.makeText(context, "Action failed due to network error: " +
                                e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}
