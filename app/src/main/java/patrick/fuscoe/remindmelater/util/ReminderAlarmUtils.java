package patrick.fuscoe.remindmelater.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderAlarmItem;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver;

/**
 * Handles reminder alarm management, cancelling notifications, and local device storage syncing
*/
public class ReminderAlarmUtils {

    private static final String TAG = "patrick.fuscoe.remindmelater.ReminderAlarmUtils";

    private static final String ACTION_ALARM_RECEIVER = "patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver";
    private static final String REMINDER_TITLE = "patrick.fuscoe.remindmelater.REMINDER_TITLE";
    private static final String REMINDER_ICON_NAME = "patrick.fuscoe.remindmelater.REMINDER_ICON_NAME";

    public static final String REMINDER_TIME_HOUR = "patrick.fuscoe.remindmelater.REMINDER_TIME_HOUR";
    public static final String REMINDER_TIME_MINUTE = "patrick.fuscoe.remindmelater.REMINDER_TIME_MINUTE";

    public static final int DEFAULT_REMINDER_TIME_HOUR = 8;
    public static final int DEFAULT_REMINDER_TIME_MINUTE = 0;

    private static final int DEFAULT_REMINDER_BROADCAST_ID = 157;
    private static final int DEFAULT_NOTIFICATION_ID = 100;

    public static void saveReminderToSharedPreferences(Context context, ReminderItem reminderItem)
    {
        SharedPreferences reminderAlarmStorage = context.getSharedPreferences(
                context.getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderIconNames = context.getSharedPreferences(
                context.getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderAlarmEditor = reminderAlarmStorage.edit();
        reminderAlarmEditor.putString(reminderItem.getTitle(), reminderItem.getNextOccurrence());
        reminderAlarmEditor.apply();

        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        reminderIconNamesEditor.putString(reminderItem.getTitle(), reminderItem.getCategoryIconName());
        reminderIconNamesEditor.apply();

        int broadcastId = generateUniqueInt();
        SharedPreferences.Editor reminderBroadcastIdEditor = reminderBroadcastIds.edit();
        reminderBroadcastIdEditor.putInt(reminderItem.getTitle(), broadcastId);
        reminderBroadcastIdEditor.apply();
    }

    public static void deleteReminderFromSharedPreferences(Context context, String reminderTitle)
    {
        SharedPreferences reminderAlarmStorage = context.getSharedPreferences(
                context.getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderIconNames = context.getSharedPreferences(
                context.getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderAlarmStorageEditor = reminderAlarmStorage.edit();
        reminderAlarmStorageEditor.remove(reminderTitle).apply();

        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        reminderIconNamesEditor.remove(reminderTitle).apply();

        SharedPreferences.Editor reminderBroadcastIdsEditor = reminderBroadcastIds.edit();
        reminderBroadcastIdsEditor.remove(reminderTitle).apply();
    }

    public static void setReminderAlarm(Context context, ReminderItem reminderItem)
    {
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderTimeOfDay = context.getSharedPreferences(
                context.getString(R.string.reminder_time_of_day_file_key), Context.MODE_PRIVATE);

        String title = reminderItem.getTitle();

        String nextOccurrence = reminderItem.getNextOccurrence();
        String iconName = reminderItem.getCategoryIconName();
        int broadcastId = reminderBroadcastIds.getInt(title, DEFAULT_REMINDER_BROADCAST_ID);

        int reminderTimeHour = reminderTimeOfDay.getInt(REMINDER_TIME_HOUR,
                DEFAULT_REMINDER_TIME_HOUR);
        int reminderTimeMinute = reminderTimeOfDay.getInt(REMINDER_TIME_MINUTE,
                DEFAULT_REMINDER_TIME_MINUTE);

        ReminderAlarmItem reminderAlarmItem = new ReminderAlarmItem(title, nextOccurrence,
                iconName, broadcastId, reminderTimeHour, reminderTimeMinute);

        setSingleReminderAlarm(context, reminderAlarmItem);
    }

    public static void cancelAllReminderAlarms(Context context)
    {
        SharedPreferences reminderAlarmStorage = context.getSharedPreferences(
                context.getString(R.string.reminders_file_key), Context.MODE_PRIVATE);

        Map<String, ?> reminderAlarmStorageMap = reminderAlarmStorage.getAll();

        for (Map.Entry<String, ?> entry : reminderAlarmStorageMap.entrySet())
        {
            String reminderTitle = entry.getKey();

            cancelReminderAlarm(context, reminderTitle);
        }

    }

    public static void cancelReminderAlarm(Context context, String reminderTitle)
    {
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int broadcastId = reminderBroadcastIds.getInt(reminderTitle, 0);

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.setAction(ACTION_ALARM_RECEIVER);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                context, broadcastId, intent, 0);

        alarmManager.cancel(alarmIntent);

        Log.d(TAG, " Alarm cancelled: " + reminderTitle);
    }

    public static void cancelNotification(Context context, String reminderTitle)
    {
        SharedPreferences reminderNotificationIds = context.getSharedPreferences(
                context.getString(R.string.reminder_notification_ids_file_key),
                Context.MODE_PRIVATE);

        int notificationId = reminderNotificationIds.getInt(reminderTitle, DEFAULT_NOTIFICATION_ID);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);

        Log.d(TAG, "Notification cancelled for " + reminderTitle +
                ". NotificationId: " + notificationId);
    }

    public static void updateReminderAlarmsOnTimeSet(Context context)
    {
        ArrayList<ReminderAlarmItem> reminderAlarmItemList =
                buildReminderAlarmItemList(context);

        for (ReminderAlarmItem reminderAlarmItem : reminderAlarmItemList)
        {
            setSingleReminderAlarm(context, reminderAlarmItem);
        }
    }

    private static void setSingleReminderAlarm(Context context, ReminderAlarmItem reminderAlarmItem)
    {
        long alarmTime = reminderAlarmItem.getAlarmCalendarObj().getTimeInMillis();

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.setAction(ACTION_ALARM_RECEIVER);
        intent.putExtra(REMINDER_TITLE, reminderAlarmItem.getTitle());
        intent.putExtra(REMINDER_ICON_NAME, reminderAlarmItem.getIconName());

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                reminderAlarmItem.getBroadcastId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, alarmTime, alarmIntent);

        Log.d(TAG, "Alarm set for: " + reminderAlarmItem.getAlarmCalendarObj().toString());
    }

    public static ArrayList<ReminderAlarmItem> buildReminderAlarmItemList(Context context)
    {
        SharedPreferences reminderAlarmStorage = context.getSharedPreferences(
                context.getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderIconNames = context.getSharedPreferences(
                context.getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderTimeOfDay = context.getSharedPreferences(
                context.getString(R.string.reminder_time_of_day_file_key), Context.MODE_PRIVATE);

        Map<String, ?> reminderAlarmStorageMap = reminderAlarmStorage.getAll();
        Map<String, ?> reminderIconNamesMap = reminderIconNames.getAll();
        Map<String, ?> reminderBroadcastIdMap = reminderBroadcastIds.getAll();
        Map<String, ?> reminderTimeOfDayMap = reminderTimeOfDay.getAll();

        ArrayList<ReminderAlarmItem> reminderAlarmItemList = new ArrayList<>();

        int reminderHour = (Integer) reminderTimeOfDayMap.get(REMINDER_TIME_HOUR);
        int reminderMinute = (Integer) reminderTimeOfDayMap.get(REMINDER_TIME_MINUTE);

        Log.d(TAG, "Reminder Time of Day: " + reminderHour + ":" + reminderMinute);

        for (Map.Entry<String, ?> entry : reminderAlarmStorageMap.entrySet())
        {
            String title = entry.getKey();
            String nextOccurrence = (String) entry.getValue();

            String iconName = (String) reminderIconNamesMap.get(title);
            int broadcastId = (Integer) reminderBroadcastIdMap.get(title);

            ReminderAlarmItem reminderAlarmItem = new ReminderAlarmItem(title, nextOccurrence,
                    iconName, broadcastId, reminderHour, reminderMinute);

            Log.d(TAG, "reminderAlarmItem: " + reminderAlarmItem.getTitle() + " rebuilt");

            reminderAlarmItemList.add(reminderAlarmItem);
        }

        return reminderAlarmItemList;
    }

    public static void setReminderTimeOfDay(Context context, int hour, int minute)
    {
        SharedPreferences reminderTimeOfDay = context.getSharedPreferences(
                context.getString(R.string.reminder_time_of_day_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderTimeOfDayEditor = reminderTimeOfDay.edit();
        reminderTimeOfDayEditor.putInt(REMINDER_TIME_HOUR, hour);
        reminderTimeOfDayEditor.putInt(REMINDER_TIME_MINUTE, minute);
        reminderTimeOfDayEditor.commit();

        Log.d(TAG, "Reminder Time of Day saved to local device: - Hour: " + hour +
                " . Minute: " + minute);
    }

    public static List<ReminderItem> buildReminderItemList(QueryDocumentSnapshot documentSnapshot)
    {
        List<ReminderItem> reminderItemList = new ArrayList<>();

        Map<String, Object> docMap = documentSnapshot.getData();

        for (Map.Entry<String, Object> entry : docMap.entrySet())
        {
            if (!entry.getKey().equals("userId"))
            {
                ReminderItem reminderItem = FirebaseDocUtils.createReminderItemObj(entry);

                reminderItemList.add(reminderItem);
            }
        }

        return reminderItemList;
    }

    public static void writeRemindersToDisk(Context context, List<ReminderItem> reminderItemList)
    {
        SharedPreferences reminderAlarmStorage = context.getSharedPreferences(context.getString(
                R.string.reminders_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderIconNames = context.getSharedPreferences(context.getString(
                R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(context.getString(
                R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        //SharedPreferences reminderTimeOfDay = context.getSharedPreferences(context.getString(
        //        R.string.reminder_time_of_day_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderAlarmEditor = reminderAlarmStorage.edit();
        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        SharedPreferences.Editor reminderBroadcastIdEditor = reminderBroadcastIds.edit();
        //SharedPreferences.Editor reminderTimeOfDayEditor = reminderTimeOfDay.edit();

        for (ReminderItem reminderItem : reminderItemList)
        {
            reminderAlarmEditor.putString(reminderItem.getTitle(), reminderItem.getNextOccurrence());
            reminderIconNamesEditor.putString(reminderItem.getTitle(), reminderItem.getCategoryIconName());

            int broadcastId = generateUniqueInt();
            reminderBroadcastIdEditor.putInt(reminderItem.getTitle(), broadcastId);
        }

        /*
        reminderTimeOfDayEditor.putInt(ReminderAlarmUtils.REMINDER_TIME_HOUR,
                userProfile.getReminderHour());
        reminderTimeOfDayEditor.putInt(ReminderAlarmUtils.REMINDER_TIME_MINUTE,
                userProfile.getReminderMinute());
        */

        // Using commit() because alarms are loaded immediately after write to disk from cloud
        reminderAlarmEditor.commit();
        reminderIconNamesEditor.commit();
        reminderBroadcastIdEditor.commit();
        //reminderTimeOfDayEditor.commit();

        Log.d(TAG, "Reminders written to storage");
    }

    private static int generateUniqueInt()
    {
        return (int) (Math.random() * 1000000);
    }

}
