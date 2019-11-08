package patrick.fuscoe.remindmelater.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderAlarmItem;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver;

/**
 * Handles reminder alarm management and local device storage syncing
*/
public class ReminderAlarmUtils {

    private static final String TAG = "patrick.fuscoe.remindmelater.ReminderAlarmUtils";

    private static final String ACTION_ALARM_RECEIVER = "patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver";
    private static final String REMINDER_TITLE = "patrick.fuscoe.remindmelater.REMINDER_TITLE";
    private static final String REMINDER_ICON_NAME = "patrick.fuscoe.remindmelater.REMINDER_ICON_NAME";

    private static final int DEFAULT_REMINDER_BROADCAST_ID = 157;

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

    public static void setReminderAlarm(Context context, ReminderItem reminderItem,
                                        int reminderTimeHour, int reminderTimeMinute)
    {
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        String title = reminderItem.getTitle();

        String nextOccurrence = reminderItem.getNextOccurrence();
        String iconName = reminderItem.getCategoryIconName();
        int broadcastId = reminderBroadcastIds.getInt(title, DEFAULT_REMINDER_BROADCAST_ID);

        ReminderAlarmItem reminderAlarmItem = new ReminderAlarmItem(title, nextOccurrence,
                iconName, broadcastId, reminderTimeHour, reminderTimeMinute);

        long alarmTime = reminderAlarmItem.getAlarmCalendarObj().getTimeInMillis();

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.setAction(ACTION_ALARM_RECEIVER);
        intent.putExtra(REMINDER_TITLE, title);
        intent.putExtra(REMINDER_ICON_NAME, iconName);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, broadcastId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, alarmTime, alarmIntent);

        Log.d(TAG, "Alarm set for: " + reminderAlarmItem.getAlarmCalendarObj().toString());
    }

    public static void cancelReminderAlarm(Context context, String reminderTitle)
    {
        SharedPreferences reminderBroadcastIds = context.getSharedPreferences(
                context.getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int broadcastId = reminderBroadcastIds.getInt(reminderTitle, 0);

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.setAction(ACTION_ALARM_RECEIVER);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, broadcastId, intent, 0);

        alarmManager.cancel(alarmIntent);

        Log.d(TAG, " Alarm cancelled: " + reminderTitle);
    }


    private static int generateUniqueInt()
    {
        return (int) (Math.random() * 1000000);
    }

}
