package patrick.fuscoe.remindmelater;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderAlarmItem;
import patrick.fuscoe.remindmelater.receiver.BootReceiver;
import patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver;
import patrick.fuscoe.remindmelater.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements BootReceiver.BootReceiverCallback {

    public static final String TAG = "patrick.fuscoe.remindmelater.MainActivity";

    public static final String NOTIFICATION_CHANNEL_ID = "patrick.fuscoe.remindmelater.NOTIFICATION_CHANNEL_ID";

    public static final String USER_PREF_REMINDER_TIME_HOUR = "reminderTimeHour";
    public static final String USER_PREF_REMINDER_TIME_MINUTE = "reminderTimeMinute";
    public static final int DEFAULT_REMINDER_TIME_HOUR = 10;
    public static final int DEFAULT_REMINDER_TIME_MINUTE = 0;

    public static final String REMINDER_TITLE = "patrick.fuscoe.remindmelater.REMINDER_TITLE";
    //public static final String REMINDER_NEXT_OCCURRENCE = "patrick.fuscoe.remindmelater.REMINDER_NEXT_OCCURRENCE";
    public static final String REMINDER_ICON_ID = "patrick.fuscoe.remindmelater.REMINDER_ICON_ID";

    public static SharedPreferences userPreferences;
    public static SharedPreferences reminderAlarmStorage;
    public static SharedPreferences reminderIconIds;
    public static SharedPreferences reminderBroadcastIds;

    private AlarmManager alarmManager;

    public List<ReminderAlarmItem> reminderAlarmItemList;
    public List<PendingIntent> alarmIntentList;

    public static int reminderTimeHour;
    public static int reminderTimeMinute;

    @Override
    public void bootReceived() {
        loadReminderAlarms();
        setReminderAlarms();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        loadUserPreferences();
        loadReminderAlarms();
        setReminderAlarms();

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Toolbar toolbarMain = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbarMain);

        /*
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public void setActionBarTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }

    public void loadUserPreferences()
    {
        userPreferences = getSharedPreferences(getString(R.string.user_preferences_file_key), Context.MODE_PRIVATE);

        reminderTimeHour = userPreferences.getInt(USER_PREF_REMINDER_TIME_HOUR, DEFAULT_REMINDER_TIME_HOUR);
        reminderTimeMinute = userPreferences.getInt(USER_PREF_REMINDER_TIME_MINUTE, DEFAULT_REMINDER_TIME_MINUTE);
    }

    public void loadReminderAlarms()
    {
        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconIds = getSharedPreferences(getString(R.string.reminder_icon_ids_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        Map<String, ?> reminderAlarmStorageMap = reminderAlarmStorage.getAll();
        Map<String, ?> reminderIconIdMap = reminderIconIds.getAll();
        Map<String, ?> reminderBroadcastIdMap = reminderBroadcastIds.getAll();

        reminderAlarmItemList = new ArrayList<>();

        for (Map.Entry<String, ?> entry : reminderAlarmStorageMap.entrySet())
        {
            String title = entry.getKey();
            String nextOccurrence = (String) entry.getValue();

            int iconId = (Integer) reminderIconIdMap.get(title);
            int broadcastId = (Integer) reminderBroadcastIdMap.get(title);

            ReminderAlarmItem reminderAlarmItem = new ReminderAlarmItem(title, nextOccurrence,
                    iconId, broadcastId, reminderTimeHour, reminderTimeMinute);

            reminderAlarmItemList.add(reminderAlarmItem);
        }
    }

    public void setReminderAlarms()
    {
        Context context = getApplicationContext();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmIntentList = new ArrayList<>();

        for (ReminderAlarmItem alarmItem : reminderAlarmItemList)
        {
            long alarmTime = alarmItem.getAlarmCalendarObj().getTimeInMillis();

            Intent intent = new Intent(context, ReminderAlarmReceiver.class);
            // TODO: setAction might be needed here
            intent.putExtra(REMINDER_TITLE, alarmItem.getTitle());
            intent.putExtra(REMINDER_ICON_ID, alarmItem.getIconId());
            //intent.putExtra(REMINDER_NEXT_OCCURRENCE, alarmItem.getNextOccurrence());

            // TODO: Check alarmIntent Flag
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarmItem.getBroadcastId(), intent, 0);

            alarmIntentList.add(alarmIntent);
            alarmManager.set(AlarmManager.RTC, alarmTime, alarmIntent);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}