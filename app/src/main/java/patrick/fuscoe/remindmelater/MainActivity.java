package patrick.fuscoe.remindmelater;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.Map;

import patrick.fuscoe.remindmelater.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.MainActivity";

    public static final String USER_PREF_REMINDER_TIME_HOUR = "reminderTimeHour";
    public static final String USER_PREF_REMINDER_TIME_MINUTE = "reminderTimeMinute";
    public static final int DEFAULT_REMINDER_TIME_HOUR = 10;
    public static final int DEFAULT_REMINDER_TIME_MINUTE = 0;

    public static SharedPreferences userPreferences;
    public static SharedPreferences reminderAlarmStorage;
    public static SharedPreferences reminderBroadcastIds;

    public static int reminderTimeHour;
    public static int reminderTimeMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUserPreferences();
        loadReminderAlarms();

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
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        // TODO: Read from shared preferences using getString to get nextOccurrence
        Map<String, ?> reminderAlarmStorageMap = reminderAlarmStorage.getAll();

        for (Map.Entry<String, ?> entry : reminderAlarmStorageMap.entrySet())
        {

        }

        Map<String, ?> reminderBroadcastIdMap = reminderBroadcastIds.getAll();

        for (Map.Entry<String, ?> entry : reminderBroadcastIdMap.entrySet())
        {

        }

    }

}