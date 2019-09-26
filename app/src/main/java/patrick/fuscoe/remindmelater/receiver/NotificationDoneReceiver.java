package patrick.fuscoe.remindmelater.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderAlarmItem;
import patrick.fuscoe.remindmelater.models.ReminderItem;

public class NotificationDoneReceiver extends BroadcastReceiver {

    public static final String TAG = "patrick.fuscoe.remindmelater.NotificationDoneReceiver";
    public static final int DEFAULT_NOTIFICATION_ID = 100;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    private DocumentReference remindersDocRef;

    private Context context;
    private int notificationId;

    private SharedPreferences reminderAlarmStorage;
    private SharedPreferences reminderIconNames;
    private SharedPreferences reminderBroadcastIds;

    private ReminderItem reminderItem;
    private String remindersDocId;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID);

        Log.d(TAG, ": notificationId: " + notificationId);

        initializeSharedPreferences();

        Gson gson = new Gson();
        Type dataTypeReminderItem = new TypeToken<ReminderItem>(){}.getType();
        String reminderItemString = intent.getStringExtra(ReminderAlarmReceiver.REMINDER_ITEM);

        Log.d(TAG, "reminderItemString: " + reminderItemString);

        reminderItem = gson.fromJson(reminderItemString, dataTypeReminderItem);

        //Log.d(TAG, ": reminderItem object toString: " + reminderItem.toString());

        remindersDocId = intent.getStringExtra(ReminderAlarmReceiver.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

        if (reminderItem.isRecurring())
        {
            updateReminderItem();
            saveReminderItem();
        }
        else
        {
            deleteReminder();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    private void initializeSharedPreferences()
    {
        reminderAlarmStorage = context.getSharedPreferences(
                context.getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconNames = context.getSharedPreferences(
                context.getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
    }

    private void updateReminderItem()
    {
        // Using workaround to get recurrence since Gson doesn't support Period java class
        Log.d(TAG, ": reminderItem recurrenceString: " + reminderItem.getRecurrenceString());

        String recurrenceString = reminderItem.getRecurrenceString();
        Period recurrence = Period.parse(recurrenceString);

        //Log.d(TAG, "recurrence Period.toString(): " + recurrence.toString());

        int daysUntilNext = recurrence.getDays();
        int monthsUntilNext = recurrence.getMonths();
        int yearsUntilNext = recurrence.getYears();

        // TODO: Fix recurrence by either using val & interval or toString for new item property due to gson issues

        //Log.d(TAG, ": daysUntilNext: " + daysUntilNext);

        LocalDate now = LocalDate.now();
        Log.d(TAG, ": now: " + now.toString());

        LocalDate nextOccurrence = now.plusDays(daysUntilNext);
        nextOccurrence = nextOccurrence.plusMonths(monthsUntilNext);
        nextOccurrence = nextOccurrence.plusYears(yearsUntilNext);

        reminderItem.setNextOccurrence(nextOccurrence.toString());

        Log.d(TAG, ": nextOccurrence: " + reminderItem.getNextOccurrence());

        reminderItem.setSnoozed(false);

        //Log.d(TAG, "reminderItemToString: " + reminderItem.toString());

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
        reminderItemMap.put("isRecurring", reminderItem.isRecurring());
        reminderItemMap.put("isSnoozed", reminderItem.isSnoozed());

        Log.d(TAG, "category: " + reminderItem.getCategory() +
                ". categoryiconName: " + reminderItem.getCategoryIconName());

        remindersDocRef.update(reminderItem.getTitle(), reminderItemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reminders DocumentSnapshot successfully updated!");
                        Toast.makeText(context, "Reminder Item Updated: " + reminderItem.getTitle(), Toast.LENGTH_SHORT).show();

                        saveReminderToSharedPreferences();
                        setReminderAlarm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        Toast.makeText(context, "Action failed due to network error: " + reminderItem.getTitle(), Toast.LENGTH_LONG).show();
                        // TODO: handle local storage of reminder when cloud sync fails
                    }
                });
    }

    private void saveReminderToSharedPreferences()
    {
        SharedPreferences.Editor reminderAlarmEditor = reminderAlarmStorage.edit();
        reminderAlarmEditor.putString(reminderItem.getTitle(), reminderItem.getNextOccurrence());
        reminderAlarmEditor.apply();

        //SharedPreferences.Editor reminderIconIdEditor = reminderIconIds.edit();
        //reminderIconIdEditor.putInt(reminderItem.getTitle(), reminderItem.getCategoryIcon());
        //reminderIconIdEditor.apply();

        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        reminderIconNamesEditor.putString(reminderItem.getTitle(), reminderItem.getCategoryIconName());
        reminderIconNamesEditor.apply();

        SharedPreferences.Editor reminderBroadcastIdEditor = reminderBroadcastIds.edit();

        int broadcastId = (int) System.currentTimeMillis();
        // TODO: Add check for existing id
        reminderBroadcastIdEditor.putInt(reminderItem.getTitle(), broadcastId);
        reminderBroadcastIdEditor.apply();

        // TODO: using apply() for async saving. Check if commit() needed
    }

    private void deleteReminder()
    {
        Map<String, Object> removeReminderUpdate = new HashMap<>();
        removeReminderUpdate.put(reminderItem.getTitle(), FieldValue.delete());

        remindersDocRef.update(removeReminderUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Delete Reminder Success: Reminders DocumentSnapshot successfully updated!");
                        cancelReminderAlarm();
                        removeReminderLocalStorage();
                        Toast.makeText(context, "Reminder Item Deleted: " + reminderItem.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        Toast.makeText(context, "Action failed due to network error: " + reminderItem.getTitle(), Toast.LENGTH_LONG).show();
                        // TODO: handle local storage of reminder when cloud sync fails
                    }
                });
    }

    private void cancelReminderAlarm()
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        //reminderIconIds = getSharedPreferences(getString(R.string.reminder_icon_ids_file_key), Context.MODE_PRIVATE);
        //reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        int broadcastId = reminderBroadcastIds.getInt(reminderItem.getTitle(), 0);

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.setAction(MainActivity.ACTION_ALARM_RECEIVER);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, broadcastId, intent, 0);

        alarmManager.cancel(alarmIntent);

        Log.d(TAG, " Alarm cancelled: " + reminderItem.getTitle());
    }

    private void setReminderAlarm()
    {
        String title = reminderItem.getTitle();
        Log.d(TAG, "title: " + title);
        String nextOccurrence = reminderItem.getNextOccurrence();
        String iconName = reminderItem.getCategoryIconName();
        int broadcastId = reminderBroadcastIds.getInt(title, MainActivity.DEFAULT_REMINDER_BROADCAST_ID);

        ReminderAlarmItem reminderAlarmItem = new ReminderAlarmItem(title, nextOccurrence,
                iconName, broadcastId, MainActivity.reminderTimeHour, MainActivity.reminderTimeMinute);

        long alarmTime = reminderAlarmItem.getAlarmCalendarObj().getTimeInMillis();

        // Set the alarm
        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.setAction(MainActivity.ACTION_ALARM_RECEIVER);
        intent.putExtra(MainActivity.REMINDER_TITLE, title);
        intent.putExtra(MainActivity.REMINDER_ICON_NAME, iconName);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, broadcastId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, alarmTime, alarmIntent);

        Log.d(TAG, "Alarm set for: " + reminderAlarmItem.getAlarmCalendarObj().toString());
    }

    private void removeReminderLocalStorage()
    {
        SharedPreferences.Editor reminderAlarmStorageEditor = reminderAlarmStorage.edit();
        reminderAlarmStorageEditor.remove(reminderItem.getTitle()).apply();

        //SharedPreferences.Editor reminderIconIdsEditor = reminderIconIds.edit();
        //reminderIconIdsEditor.remove(reminderItem.getTitle()).commit();

        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        reminderIconNamesEditor.remove(reminderItem.getTitle()).apply();

        SharedPreferences.Editor reminderBroadcastIdsEditor = reminderBroadcastIds.edit();
        reminderBroadcastIdsEditor.remove(reminderItem.getTitle()).apply();

        Log.d(TAG, "Reminder removed from local storage: " + reminderItem.getTitle());
    }

}
