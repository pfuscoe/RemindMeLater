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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderAlarmItem;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.receiver.BootReceiver;
import patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver;
import patrick.fuscoe.remindmelater.ui.main.SectionsPagerAdapter;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;

public class MainActivity extends AppCompatActivity implements BootReceiver.BootReceiverCallback {

    public static final String TAG = "patrick.fuscoe.remindmelater.MainActivity";

    public static final String NOTIFICATION_CHANNEL_ID = "patrick.fuscoe.remindmelater.NOTIFICATION_CHANNEL_ID";
    public static final String ACTION_ALARM_RECEIVER = "patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver";

    public static final int DEFAULT_REMINDER_TIME_HOUR = 8;
    public static final int DEFAULT_REMINDER_TIME_MINUTE = 0;
    public static final int DEFAULT_HIBERNATE_LENGTH = 14;
    public static final String DEFAULT_REMINDER_CATEGORY_ICON_NAME = "category_alarm";
    public static final String DEFAULT_TO_DO_GROUP_CATEGORY_ICON_NAME = "category_format_list_checkbox";
    public static final int DEFAULT_REMINDER_BROADCAST_ID = 157;
    public static final int DEFAULT_NOTIFICATION_ID = 100;

    public static final String USER_PROFILE = "patrick.fuscoe.remindmelater.USER_PROFILE";
    public static final String REMINDER_TITLE = "patrick.fuscoe.remindmelater.REMINDER_TITLE";
    public static final String REMINDER_ICON_NAME = "patrick.fuscoe.remindmelater.REMINDER_ICON_NAME";
    public static final String BACK_PRESSED_FROM_REMINDER_DETAILS = "patrick.fuscoe.remindmelater.BACK_PRESSED_FROM_REMINDER_DETAILS";
    public static final String USER_PREFERENCES_UPDATED = "patrick.fuscoe.remindmelater.USER_PREFERENCES_UPDATED";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;
    public static DocumentReference remindersDocRef;

    private ProgressBar viewMainProgressBar;
    private UserProfile userProfile;
    private String remindersDocId;

    public static SharedPreferences reminderAlarmStorage;
    public static SharedPreferences reminderIconNames;
    public static SharedPreferences reminderBroadcastIds;
    public static SharedPreferences reminderNotificationIds;

    private AlarmManager alarmManager;

    public List<ReminderAlarmItem> reminderAlarmItemList;
    public List<PendingIntent> alarmIntentList;

    private List<ReminderItem> reminderItemList;

    public static int reminderTimeHour;
    public static int reminderTimeMinute;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private boolean setRemindersTabActive;

    private String newUserDisplayName;

    @Override
    public void bootReceived() {
        loadReminderAlarms();
        setReminderAlarms();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewMainProgressBar = findViewById(R.id.view_main_progress_bar);
        viewMainProgressBar.setVisibility(View.VISIBLE);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);

        setRemindersTabActive = false;
        newUserDisplayName = "New User";

        createNotificationChannel();

        Intent intent = getIntent();

        // User entered login info from sign in activity
        if (intent.getBooleanExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, false))
        {
            // Check if this is a new user by checking if user doc on cloud exists
            if (intent.hasExtra(FirebaseSignInActivity.DISPLAY_NAME))
            {
                newUserDisplayName = intent.getStringExtra(FirebaseSignInActivity.DISPLAY_NAME);
            }

            checkIfNewUser();
        }
        // User already signed-in
        else
        {
            if (intent.hasExtra(BACK_PRESSED_FROM_REMINDER_DETAILS))
            {
                setRemindersTabActive = true;
            }
            else
            {
                setRemindersTabActive = false;
            }

            loadUserProfileFromCloud();
        }

        Toolbar toolbarMain = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbarMain);

        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconNames = getSharedPreferences(getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        reminderNotificationIds = getSharedPreferences(getString(R.string.reminder_notification_ids_file_key), Context.MODE_PRIVATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(BACK_PRESSED_FROM_REMINDER_DETAILS))
        {
            sectionsPagerAdapter.getItem(1);
        }

        if (intent.hasExtra(USER_PREFERENCES_UPDATED))
        {
            Log.d(TAG, "Returned from user prefs which were updated.");

            Gson gson = new Gson();
            Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
            String userProfileString = intent.getStringExtra(USER_PROFILE);
            userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);
            Log.d(TAG, "userProfile Gson String: " + userProfileString);

            updateReminderTimeOfDay();
            loadReminderAlarms();
            setReminderAlarms();
        }

        super.onNewIntent(intent);
    }

    private void setupTabs()
    {
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        if (setRemindersTabActive)
        {
            sectionsPagerAdapter.getItem(1);
        }

        viewMainProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_logout:
                Log.d(TAG, "Menu: Logout clicked");
                logoutUser();
                return true;

            case R.id.menu_main_user_settings:
                Log.d(TAG, "Menu: User Settings clicked");
                openUserSettings();
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
                        clearAllSharedPreferences();
                        Intent intent = new Intent(MainActivity.this, FirebaseSignInActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                });
    }

    public void openUserSettings()
    {
        Intent intent = new Intent(MainActivity.this, UserPreferencesActivity.class);
        Gson gson = new Gson();

        String userProfileString = gson.toJson(userProfile);
        Log.d(TAG, "userProfileString: " + userProfileString);
        intent.putExtra(USER_PROFILE, userProfileString);
        startActivity(intent);
    }

    public void clearAllSharedPreferences()
    {
        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconNames = getSharedPreferences(getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        reminderNotificationIds = getSharedPreferences(getString(R.string.reminder_notification_ids_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderAlarmStorageEditor = reminderAlarmStorage.edit();
        reminderAlarmStorageEditor.clear().commit();

        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        reminderIconNamesEditor.clear().commit();

        SharedPreferences.Editor reminderBroadcastIdsEditor = reminderBroadcastIds.edit();
        reminderBroadcastIdsEditor.clear().commit();

        SharedPreferences.Editor reminderNotificationIdsEditor = reminderNotificationIds.edit();
        reminderNotificationIdsEditor.clear().commit();

        Log.d(TAG, "All SharedPreferences cleared");
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

                            // User already exists
                            if (documentSnapshot.exists())
                            {
                                buildUserProfileObj(documentSnapshot);
                                saveRemindersToStorage();
                                setupTabs();
                            }
                            // New user was created
                            else
                            {
                                createNewReminderDoc();
                            }
                        }
                    }
                });
    }

    public void loadUserProfileFromCloud()
    {
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            buildUserProfileObj(documentSnapshot);
                            Log.d(TAG, "User Profile loaded from cloud");
                            setupTabs();
                        }
                    }
                });
    }

    public void buildUserProfileObj(DocumentSnapshot documentSnapshot)
    {
        userProfile = FirebaseDocUtils.createUserProfileObj(documentSnapshot);

        /*
        Map<String, Object> docMap = documentSnapshot.getData();

        String id = documentSnapshot.getId();
        String displayName = documentSnapshot.getString("displayName");

        Log.d(TAG, "displayName: " + displayName);

        ArrayList<String> subscriptionsList = (ArrayList<String>) docMap.get("subscriptions");

        String[] subscriptions = new String[subscriptionsList.size()];
        subscriptions = subscriptionsList.toArray(subscriptions);

        Map<String, String> reminderCategories =
                (Map<String, String>) documentSnapshot.get("reminderCategories");

        reminderTimeHour = Math.toIntExact((long) docMap.get("reminderHour"));
        reminderTimeMinute = Math.toIntExact((long) docMap.get("reminderMinute"));

        int hibernateLength = Math.toIntExact((long) docMap.get("hibernateLength"));

        ArrayList<String> friendsList = (ArrayList<String>) docMap.get("friends");
        String[] friends;
        friends = friendsList.toArray(new String[0]);

        Log.d(TAG, "buildUserProfileObj: reminderTimeHour = " + reminderTimeHour +
                ". reminderTimeMinute = " + reminderTimeMinute);

        userProfile = new UserProfile(id, displayName, subscriptions, reminderCategories,
                reminderTimeHour, reminderTimeMinute, hibernateLength, friends);
        */


        reminderTimeHour = userProfile.getReminderHour();
        reminderTimeMinute = userProfile.getReminderMinute();

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
                        remindersDocRef = documentReference;
                        remindersDocId = documentReference.getId();
                        Log.d(TAG, "Reminders document written with ID: " + remindersDocId);
                        createNewUserDoc();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding new reminders document", e);
                    }
                });
    }

    public void createNewUserDoc()
    {
        Map<String, Object> userProfileDoc = new HashMap<>();

        userProfileDoc.put("displayName", newUserDisplayName);

        Map<String, String> reminderCategoriesMap = new HashMap<>();
        reminderCategoriesMap.put("Main", DEFAULT_REMINDER_CATEGORY_ICON_NAME);
        userProfileDoc.put("reminderCategories", reminderCategoriesMap);

        ArrayList<String> subscriptionsList = new ArrayList<>();
        userProfileDoc.put("subscriptions", subscriptionsList);

        userProfileDoc.put("reminderHour", DEFAULT_REMINDER_TIME_HOUR);
        userProfileDoc.put("reminderMinute", DEFAULT_REMINDER_TIME_MINUTE);

        userProfileDoc.put("hibernateLength", DEFAULT_HIBERNATE_LENGTH);

        ArrayList<String> friendsList = new ArrayList<>();
        userProfileDoc.put("friends", friendsList);

        // Set new user profile
        String[] subscriptions = new String[0];
        subscriptions = subscriptionsList.toArray(subscriptions);

        String[] friends;
        friends = friendsList.toArray(new String[0]);

        userProfile = new UserProfile(userId, newUserDisplayName, subscriptions,
                reminderCategoriesMap, DEFAULT_REMINDER_TIME_HOUR, DEFAULT_REMINDER_TIME_MINUTE,
                DEFAULT_HIBERNATE_LENGTH, friends);

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User DocumentSnapshot successfully written!");
                        setupTabs();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding new User document", e);
                    }
                });
    }

    public void setActionBarTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }

    public void updateReminderTimeOfDay()
    {
        reminderTimeHour = userProfile.getReminderHour();
        reminderTimeMinute = userProfile.getReminderMinute();
    }

    public void saveRemindersToStorage()
    {
        remindersCollectionRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                remindersDocRef = document.getReference();
                                remindersDocId = document.getId();
                                Log.d(TAG, "remindersDocId: " + remindersDocId);
                                buildReminderItemList(document);
                                writeRemindersToDisk();
                                loadReminderAlarms();
                                setReminderAlarms();
                            }
                        }
                        else
                        {
                            Log.d(TAG, "Error getting documents: " + task.getException());
                        }
                    }
                });

    }

    public void buildReminderItemList(QueryDocumentSnapshot document)
    {
        reminderItemList = new ArrayList<>();

        Map<String, Object> docMap = document.getData();

        for (Map.Entry<String, Object> entry : docMap.entrySet())
        {
            if (!entry.getKey().equals("userId"))
            {
                HashMap<String, Object> reminderItemMap = (HashMap<String, Object>) entry.getValue();

                String reminderTitle = entry.getKey();
                boolean isRecurring = (boolean) reminderItemMap.get("isRecurring");
                int recurrenceNum = Math.toIntExact((long) reminderItemMap.get("recurrenceNum"));
                String recurrenceInterval = (String) reminderItemMap.get("recurrenceInterval");
                String nextOccurrence = (String) reminderItemMap.get("nextOccurrence");
                String category = (String) reminderItemMap.get("category");
                String categoryIconName = (String) reminderItemMap.get("categoryIconName");
                String description = (String) reminderItemMap.get("description");
                boolean isSnoozed = (boolean) reminderItemMap.get("isSnoozed");
                boolean isHibernating = (boolean) reminderItemMap.get("isHibernating");
                Map<String, String> history = (Map<String, String>) reminderItemMap.get("history");

                ReminderItem reminderItem = new ReminderItem(reminderTitle, isRecurring,
                        recurrenceNum, recurrenceInterval, nextOccurrence, category,
                        categoryIconName, description, isSnoozed, isHibernating, history);

                reminderItemList.add(reminderItem);
            }
        }
    }

    public void writeRemindersToDisk()
    {
        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconNames = getSharedPreferences(getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderAlarmEditor = reminderAlarmStorage.edit();
        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        SharedPreferences.Editor reminderBroadcastIdEditor = reminderBroadcastIds.edit();

        for (ReminderItem reminderItem : reminderItemList)
        {
            reminderAlarmEditor.putString(reminderItem.getTitle(), reminderItem.getNextOccurrence());
            reminderIconNamesEditor.putString(reminderItem.getTitle(), reminderItem.getCategoryIconName());

            int broadcastId = generateUniqueInt();
            reminderBroadcastIdEditor.putInt(reminderItem.getTitle(), broadcastId);
        }

        // Using commit() because alarms are loaded immediately after write to disk from cloud
        reminderAlarmEditor.commit();
        reminderIconNamesEditor.commit();
        reminderBroadcastIdEditor.commit();

        Log.d(TAG, "Reminders written to storage");
    }

    public void loadReminderAlarms()
    {
        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconNames = getSharedPreferences(getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        Map<String, ?> reminderAlarmStorageMap = reminderAlarmStorage.getAll();
        Map<String, ?> reminderIconNamesMap = reminderIconNames.getAll();
        Map<String, ?> reminderBroadcastIdMap = reminderBroadcastIds.getAll();

        reminderAlarmItemList = new ArrayList<>();

        Log.d(TAG, ": Reminder Time of Day: " + reminderTimeHour + ":" + reminderTimeMinute);

        for (Map.Entry<String, ?> entry : reminderAlarmStorageMap.entrySet())
        {
            String title = entry.getKey();
            String nextOccurrence = (String) entry.getValue();

            String iconName = (String) reminderIconNamesMap.get(title);
            int broadcastId = (Integer) reminderBroadcastIdMap.get(title);

            ReminderAlarmItem reminderAlarmItem = new ReminderAlarmItem(title, nextOccurrence,
                    iconName, broadcastId, reminderTimeHour, reminderTimeMinute);

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
            intent.putExtra(REMINDER_ICON_NAME, alarmItem.getIconName());

            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarmItem.getBroadcastId(), intent, 0);

            alarmIntentList.add(alarmIntent);
            alarmManager.set(AlarmManager.RTC, alarmTime, alarmIntent);
        }
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static int generateUniqueInt()
    {
        return (int) (Math.random() * 1000000);
    }

}