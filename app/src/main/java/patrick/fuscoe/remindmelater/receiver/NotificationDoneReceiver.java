package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.time.Period;
import java.util.HashMap;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderItem;

public class NotificationDoneReceiver extends BroadcastReceiver {

    public static final String TAG = "patrick.fuscoe.remindmelater.NotificationDoneReceiver";
    public static final int DEFAULT_NOTIFICATION_ID = 100;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    private DocumentReference remindersDocRef;

    private Context context;
    private int notificationId;

    private ReminderItem reminderItem;
    private String remindersDocId;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID);

        Log.d(TAG, ": notificationId: " + notificationId);

        Gson gson = new Gson();
        Type dataTypeReminderItem = new TypeToken<ReminderItem>(){}.getType();
        String reminderItemString = intent.getStringExtra(ReminderAlarmReceiver.REMINDER_ITEM);

        reminderItem = gson.fromJson(reminderItemString, dataTypeReminderItem);

        Log.d(TAG, ": reminderItem object toString: " + reminderItem.toString());

        remindersDocId = intent.getStringExtra(ReminderAlarmReceiver.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

        updateReminderItem();
        saveReminderItem();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    private void updateReminderItem()
    {
        // Using workaround to get recurrence since Gson doesn't support Period java class
        Log.d(TAG, ": reminderItem recurrenceString: " + reminderItem.getRecurrenceString());

        String recurrenceString = reminderItem.getRecurrenceString();
        Period recurrence = Period.parse(recurrenceString);

        int daysUntilNext = recurrence.getDays();

        // TODO: Fix recurrence by either using val & interval or toString for new item property due to gson issues

        Log.d(TAG, ": daysUntilNext: " + daysUntilNext);

        LocalDate now = LocalDate.now();
        Log.d(TAG, ": nextOccurrence: " + now.toString());
        LocalDate nextOccurrence = now.plusDays(daysUntilNext);
        Log.d(TAG, ": nextOccurrence: " + nextOccurrence.toString());
        reminderItem.setNextOccurrence(nextOccurrence.toString());

        Log.d(TAG, ": nextOccurrence: " + reminderItem.getNextOccurrence());

        // TODO: Add action to history
    }

    private void saveReminderItem()
    {
        HashMap<String, Object> reminderItemMap = new HashMap<>();
        reminderItemMap.put("recurrence", reminderItem.getRecurrenceString());
        reminderItemMap.put("recurrenceNum", reminderItem.getRecurrenceNum());
        reminderItemMap.put("recurrenceInterval", reminderItem.getRecurrenceInterval());
        reminderItemMap.put("nextOccurrence", reminderItem.getNextOccurrence());
        reminderItemMap.put("category", reminderItem.getCategory());
        reminderItemMap.put("categoryIconName", reminderItem.getCategoryIconName());
        reminderItemMap.put("description", reminderItem.getDescription());

        saveReminderToSharedPreferences();

        remindersDocRef.update(reminderItem.getTitle(), reminderItemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reminders DocumentSnapshot successfully updated!");
                        Toast.makeText(context, "Reminder Item Updated: " + reminderItem.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        // TODO: handle local storage of reminder when cloud sync fails
                    }
                });
    }

    private void saveReminderToSharedPreferences()
    {
        SharedPreferences reminderAlarmStorage = context.getSharedPreferences(
                context.getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor reminderAlarmEditor = reminderAlarmStorage.edit();

        reminderAlarmEditor.putString(reminderItem.getTitle(), reminderItem.getNextOccurrence());
        reminderAlarmEditor.apply();

        //SharedPreferences reminderIconIds = context.getSharedPreferences(
                //context.getString(R.string.reminder_icon_ids_file_key), Context.MODE_PRIVATE);
        //SharedPreferences.Editor reminderIconIdEditor = reminderIconIds.edit();

        //reminderIconIdEditor.putInt(reminderItem.getTitle(), reminderItem.getCategoryIcon());
        //reminderIconIdEditor.apply();

        SharedPreferences reminderIconNames = context.getSharedPreferences(
                context.getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        reminderIconNamesEditor.putString(reminderItem.getTitle(), reminderItem.getCategoryIconName());
        reminderIconNamesEditor.apply();

        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor reminderBroadcastIdEditor = reminderBroadcastIds.edit();

        int broadcastId = (int) System.currentTimeMillis();
        // TODO: Add check for existing id
        reminderBroadcastIdEditor.putInt(reminderItem.getTitle(), broadcastId);
        reminderBroadcastIdEditor.apply();

        // TODO: using apply() for async saving. Check if commit() needed
    }

}
