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

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderAlarmItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.receiver.BootReceiver;
import patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver;
import patrick.fuscoe.remindmelater.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements BootReceiver.BootReceiverCallback {

    public static final String TAG = "patrick.fuscoe.remindmelater.MainActivity";

    public static final String NOTIFICATION_CHANNEL_ID = "patrick.fuscoe.remindmelater.NOTIFICATION_CHANNEL_ID";
    public static final String ACTION_ALARM_RECEIVER = "patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver";

    public static final String USER_PREF_REMINDER_TIME_HOUR = "reminderTimeHour";
    public static final String USER_PREF_REMINDER_TIME_MINUTE = "reminderTimeMinute";
    public static final int DEFAULT_REMINDER_TIME_HOUR = 9;
    public static final int DEFAULT_REMINDER_TIME_MINUTE = 30;

    public static final String REMINDER_TITLE = "patrick.fuscoe.remindmelater.REMINDER_TITLE";
    //public static final String REMINDER_NEXT_OCCURRENCE = "patrick.fuscoe.remindmelater.REMINDER_NEXT_OCCURRENCE";
    public static final String REMINDER_ICON_ID = "patrick.fuscoe.remindmelater.REMINDER_ICON_ID";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");
    private final String userId = auth.getUid();
    private final DocumentReference userDocRef = db.collection("users").document(userId);

    private UserProfile userProfile;
    private String remindersDocId;

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

        Intent intent = getIntent();

        if (intent.getBooleanExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, false))
        {
            // TODO: Also need to load reminders to device storage..
            // Check if this is a new user by checking if user doc on cloud exists
            checkIfNewUser();
        }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: Setup logout option from menu
        switch (item.getItemId())
        {
            case R.id.menu_main_logout:
                Log.d(TAG, "Menu: Logout clicked");
                logoutUser();
                return true;

            case R.id.menu_main_user_settings:
                Log.d(TAG, "Menu: User Settings clicked");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logoutUser()
    {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(MainActivity.this, FirebaseSignInActivity.class);
                        startActivity(intent);
                    }
                });
    }

    public void checkIfNewUser()
    {
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists())
                            {
                                buildUserProfileObj(documentSnapshot);
                            }
                            else
                            {
                                // TODO: create new reminders doc and user doc
                                createNewReminderDoc();
                            }
                        }
                    }
                });
    }

    public void buildUserProfileObj(DocumentSnapshot documentSnapshot)
    {
        Map<String, Object> docMap = documentSnapshot.getData();

        String id = documentSnapshot.getId();
        String displayName = documentSnapshot.getString("displayName");

        ArrayList<String> subscriptionsList = (ArrayList<String>) docMap.get("subscriptions");

        String[] subscriptions = new String[subscriptionsList.size()];
        subscriptions = subscriptionsList.toArray(subscriptions);

        Map<String, Integer> reminderCategories =
                (Map<String, Integer>) documentSnapshot.get("reminderCategories");

        userProfile = new UserProfile(id, displayName, subscriptions, reminderCategories);

        Log.d(TAG, ": userProfile loaded from cloud");
    }

    public void createNewReminderDoc()
    {
        Map<String, Object> reminderDocMap = new HashMap<>();
        reminderDocMap.put("userId", userId);

        remindersCollectionRef.add(reminderDocMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        remindersDocId = documentReference.getId();
                        Log.d(TAG, "Reminders document written with ID: " + remindersDocId);
                        createNewUserDoc();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding reminders document", e);
                    }
                });
    }

    public void createNewUserDoc()
    {
        Map<String, Object> userProfileDoc = new HashMap<>();
        userProfileDoc.put("displayName", "");

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing User document", e);
                    }
                });
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

        Log.d(TAG, ": Reminder Time of Day: " + reminderTimeHour + ":" + reminderTimeMinute);

        for (Map.Entry<String, ?> entry : reminderAlarmStorageMap.entrySet())
        {
            String title = entry.getKey();
            String nextOccurrence = (String) entry.getValue();

            int iconId = (Integer) reminderIconIdMap.get(title);
            int broadcastId = (Integer) reminderBroadcastIdMap.get(title);

            ReminderAlarmItem reminderAlarmItem = new ReminderAlarmItem(title, nextOccurrence,
                    iconId, broadcastId, reminderTimeHour, reminderTimeMinute);

            Log.d(TAG, ": reminderAlarmItem: " + reminderAlarmItem.getTitle() + " built");

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
            Log.d(TAG, ": Alarm Calendar Object: " + alarmItem.getAlarmCalendarObj().toString());

            long alarmTime = alarmItem.getAlarmCalendarObj().getTimeInMillis();

            Intent intent = new Intent(context, ReminderAlarmReceiver.class);
            intent.setAction(ACTION_ALARM_RECEIVER);
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